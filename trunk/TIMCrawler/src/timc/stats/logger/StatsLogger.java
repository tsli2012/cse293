package timc.stats.logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import timc.common.TIMConfigurator;
import timc.stats.SessionRecord;
import timc.stats.TestRecord;
import timc.stats.StatsWriter;
import timc.stats.db.DBStatsWriter;
import timc.stats.PeerStats;

import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;
import com.turn.ttorrent.client.Announce;
import com.turn.ttorrent.client.AnnounceResponseListener;
import com.turn.ttorrent.client.Piece;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.client.peer.PeerActivityListener;
import com.turn.ttorrent.client.peer.SharingPeer;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;

import java.util.Date;

public class StatsLogger implements AnnounceResponseListener, PeerActivityListener {		
	
	private static final Logger logger =
			LoggerFactory.getLogger(StatsLogger.class);
	
	private ConcurrentMap<String, SharingPeer> connectedPeers;  // same instance as Client's "connected"	
	private ConcurrentMap<String, PeerStats> sessionsMap;
	private String crawlerPeerID;
	private SharedTorrent torrent;
	private Announce announce;
	private StatsWriter statsWriter;
	private TestRecord testRecord;
	private String testId;
	private int numSeeders;
	private int numLeechers;
		
	public StatsLogger(ConcurrentMap<String, SharingPeer> connected, SharedTorrent torrent, 
			Announce announce, String crawlerPeerID)
			throws IOException {
		this.torrent = torrent;
		this.connectedPeers = connected;
		this.crawlerPeerID = crawlerPeerID;
		this.announce = announce;
		this.announce.register(this);	
		this.statsWriter = new DBStatsWriter();		
		this.statsWriter.initWriter();
		this.sessionsMap = new ConcurrentHashMap<String, PeerStats>();
		
		this.testRecord = new TestRecord();
	}
	
	public void start() {		
		logger.info("StatsLogger: Starting statsLogger for torrent" +
				torrent.getName() + " with crawlerPeerID " +
				this.crawlerPeerID + "...");		

		int numSeedsInTorrent = getNumSeedsInTorrent();
		int numLeechInTorrent = getNumLeechInTorrent();
		
		// write the general test record, and get testId while doing so
		this.testRecord.mode = TIMConfigurator.getOpMode().getMode();
		this.testRecord.modeSettings = String.valueOf(TIMConfigurator.getHalfSeedCompletionRate());
		this.testRecord.startTime = new Date();
		this.testRecord.endTime = this.testRecord.startTime;
		this.testRecord.infoHash = this.torrent.getHexInfoHash();
		this.testRecord.totalSize = this.torrent.getSize();
		this.testRecord.pieceSize = this.torrent.getPieceLength();
		this.testRecord.numPieces = this.torrent.getPieceCount();
		this.testRecord.initialNumSeeders = numSeedsInTorrent;
		this.testRecord.initialNumLeechers = numLeechInTorrent;
		this.testRecord.crawlerPeerID = this.crawlerPeerID;
		
		this.testId = this.statsWriter.writeTestStats(this.testRecord);	
		logger.info("StatsLogger: writing to TestStats table, with testRecord: " + testRecord.toString());
		
		// init peer stats for peers that have already been connected
		for (SharingPeer peer : this.connectedPeers.values()) {
			if (!(sessionsMap.containsKey(peer.getIp()+peer.getPort())) ||  sessionsMap.get(peer.getIp()+peer.getPort()).getCurrentSessionNum() == 0 ){
				PeerStats ps = new PeerStats();			
				ps.setCurrentSessionNum(1);
				SessionRecord rec1 = createNewRecord(numSeedsInTorrent,
						numLeechInTorrent, peer, ps.getCurrentSessionNum());
				ps.setCurrentSessionRecord(rec1);
				ps.setConnected(true);	
				ps.setSharingPeer(peer);
				sessionsMap.put(peer.getIp()+peer.getPort(), ps);
			}		
		}
	}
	
	public void stop() {
		
		for (PeerStats ps : sessionsMap.values()) {
			
			//SessionRecord zeroRec = null;
			SessionRecord currentRec = null;
			
			synchronized(ps) {
				//zeroRec = ps.getZeroSessionRecord();
				if (ps.isConnected())
					currentRec = ps.getCurrentSessionRecord();	
			}
			
			// write all zero records that have been initialized (thru announce response handler
			/*if (zeroRec != null) {
				try {
					this.statsWriter.writeTrackerSessionStats(testId, zeroRec);	
					logger.error("StatsLogger: stop method was invoked. Writing to Sessions table, for testID: "
							+ testId + " with a Zero session record: " + zeroRec.toString());
				} catch (Exception e) {
					logger.error("StatsLogger error: {}", e.getMessage(), e);
				}	
			}*/
			
			// write all last records of peers who haven't been marked as disconnected
			if (currentRec != null) {
				updateRecordOnDisconnection(currentRec, ps.getSharingPeer(), true, getNumSeedsInTorrent(), getNumLeechInTorrent());
				try {
					this.statsWriter.writeSessionStats(testId, currentRec);	
					logger.error("StatsLogger: stop method was invoked. Writing to Sessions table, for testID: " + testId 
							+ " with session record: " + currentRec.toString());
				} catch (Exception e) {
					logger.error("{}", e.getMessage(), e);
				}
			}
		}		
		this.testRecord.endTime = new Date();
		this.statsWriter.updateTestStats(this.testId, this.testRecord);
		this.statsWriter.closeWriter();
	}

	@Override
	public void handlePeerChoked(SharingPeer peer) { /* ignore */ }

	@Override
	public void handlePeerReady(SharingPeer peer) { /* ignore */ }

	@Override
	public void handleBitfieldAvailability(SharingPeer peer, BitSet availablePieces) { /* ignore */ }

	@Override
	/*
	 * Trigger by unbind
	 */
	public void handlePeerDisconnected(SharingPeer peer) {
		if (!sessionsMap.containsKey(peer.getIp()+peer.getPort()) || 
				sessionsMap.get(peer.getIp()+peer.getPort()).getCurrentSessionNum() == 0){
			logger.error("StatsLoger: error - peer with peerId: " + 
				peer.getPeerId() + 
				" disconnected, but we didn't have it in sessionsMap, or didn't have it at all");
			return;
		}
		
		PeerStats ps = sessionsMap.get(peer.getIp()+peer.getPort());
		SessionRecord currentRec;
		synchronized (ps) {
			if (!ps.isConnected())
				return;
			currentRec = ps.getCurrentSessionRecord();
			ps.setConnected(false);
			ps.setCurrentSessionRecord(null);
		}
		
		updateRecordOnDisconnection(currentRec, peer, false, getNumSeedsInTorrent(), getNumLeechInTorrent());
		try {
			this.statsWriter.writeSessionStats(testId, currentRec);
			logger.info("StatsLogger: the following peer has disconnected: " + peer.toString() + " Writing to Sessions table.");
		} catch (Exception e) {
			logger.error("StatsLogger error: {}", e.getMessage(), e);
		}
	}

	@Override
	public void handleIOException(SharingPeer peer, IOException ioe) {  /* ignore */ }

	@Override
	// update the zero record for all relevant peers
	public void handleAnnounceResponse(Map<String, BEValue> answer) {
		try {
			if (!answer.containsKey("peers")) {
				// No peers returned by the tracker. Apparently we're alone on this one for now.
				return;
			}	
			
			// Whether this is the first time we get the number of seeders and leechers
			boolean updateNumSeeders = (this.numSeeders == 0);
			
			if (answer.containsKey("complete"))
				this.numSeeders = answer.get("complete").getInt();
			if (answer.containsKey("incomplete"))
				this.numLeechers = answer.get("incomplete").getInt();
			
			// Update number of seeders and leechers only if this is the first time we get them
			this.testRecord.initialNumLeechers = this.numLeechers;
			this.testRecord.initialNumSeeders = this.numSeeders;
			if (updateNumSeeders)
				this.statsWriter.updateTestStats(this.testId, this.testRecord);

			try { 
				List<BEValue> peers = answer.get("peers").getList();
				for (BEValue peerInfo : peers) {
					Map<String, BEValue> info = peerInfo.getMap();

					try {
						// get peer id
						byte[] peerId = info.get("peer id").getBytes();
						// get peer ip
						String ip = new String(info.get("ip").getBytes(),
								Torrent.BYTE_ENCODING);
						// get peer port
						int port = info.get("port").getInt();
						// insert zero session record
						this.upsertZeroRecord(peerId, ip, port);
					} catch (NullPointerException npe) {
						throw new ParseException("Missing field from peer " +
								"information in tracker response!", 0);
					}
				}
			} catch (InvalidBEncodingException ibee) {
				byte[] data = answer.get("peers").getBytes();
				int nPeers = data.length / 6;
				if (data.length % 6 != 0) {
					throw new InvalidBEncodingException("Invalid peers " +
							"binary information string!");
				}

				ByteBuffer peers = ByteBuffer.wrap(data);

				for (int i=0; i < nPeers ; i++) {
					byte[] ipBytes = new byte[4];
					peers.get(ipBytes);
					String ip = InetAddress.getByAddress(ipBytes)
						.getHostAddress();
					int port = (0xFF & (int)peers.get()) << 8
						| (0xFF & (int)peers.get());
					this.upsertZeroRecord(null, ip, port);
				}
			}
		} catch (UnknownHostException uhe) {
			logger.warn("Invalid compact tracker response!", uhe);
		} catch (ParseException pe) {
			logger.warn("Invalid tracker response!", pe);
		} catch (InvalidBEncodingException ibee) {
			logger.warn("Invalid tracker response!", ibee);
		} catch (UnsupportedEncodingException uee) {
			logger.error("StatsLogger error: {}", uee.getMessage(), uee);
		}
	}					
				
	private void upsertZeroRecord(byte[] initialPeerId, String ip, int port) {
		Peer basePeer = generatePeerLikeClientDoes(initialPeerId, ip, port);
		// get the hex version of peer id
		String peerId = basePeer.hasPeerId() ? basePeer.getHexPeerId() : basePeer.getHostIdentifier();
		
		if (sessionsMap.containsKey(ip+port)){							
			SessionRecord rec0 = sessionsMap.get(ip+port).getZeroSessionRecord();			
			if (rec0 == null) { // need to init the zero record
				rec0 = createZeroRecord(ip, port, basePeer);
				sessionsMap.get(ip+port).setZeroSessionRecord(rec0);
			} else { // just update the zero record				
				rec0.lastSeenByTracker = new Date();
				rec0.lastNumLeechers = this.getNumLeechInTorrent();
				rec0.lastNumSeeders = this.getNumSeedsInTorrent();	
			}			
		} else { // first time we encounter this peerId
			SessionRecord rec0 = createZeroRecord(ip, port, basePeer);
			PeerStats ps = new PeerStats();
			ps.setZeroSessionRecord(rec0);
			ps.setBasePeer(basePeer);
			sessionsMap.put(ip+port, ps);
		}				
	}
	
	private Peer generatePeerLikeClientDoes(byte[] initialPeerId,
			String ip, int port) {
		Peer peer = new Peer(ip, port,
				(initialPeerId != null ? ByteBuffer.wrap(initialPeerId) : (ByteBuffer)null));
		return peer;
	}

	private SessionRecord createZeroRecord(String ip, int port, Peer basePeer) {
		SessionRecord rec0 = new SessionRecord();		
		
		//synchronized(basePeer) {
			rec0.peerIdHex = basePeer.getHexPeerId();
			rec0.peerIdStr = basePeer.getPeerIdStr();
		//}
		
		rec0.peerIP = ip;
		rec0.peerPort = port;
		rec0.sessionSeqNum = 0;
		rec0.lastSeenByTracker = new Date();
		rec0.lastNumLeechers = this.getNumLeechInTorrent();
		rec0.lastNumSeeders = this.getNumSeedsInTorrent();
		
		return rec0;
	}

	private SessionRecord createNewRecord(int numSeedsInTorrent,
			int numLeechInTorrent, SharingPeer peer, int sessionSeqNum) {
		SessionRecord rec = new SessionRecord();
		
		//synchronized (peer) {
			rec.totalDownloadRate = peer.getPeerTotalDLRate().get() / (float)1024;
			rec.lastDLRate1 = peer.getPeerDLRate1().get() / (float)1024;
			rec.lastDLRate2 = peer.getPeerDLRate2().get() / (float)1024;
			rec.lastDLRate3 = peer.getPeerDLRate3().get() / (float)1024;
			rec.completionRate = (float)peer.getAvailablePieces().cardinality() / (float)torrent.getPieceCount();
			rec.initialBitfield = peer.getAvailablePieces();
			rec.lastBitfield = peer.getAvailablePieces();
			rec.bitfieldReceived = peer.isBitfieldReceived();
			// dont confuse getPeerIdStr (for output in log records) and peer.getHexPeerId (which is for MAPs)
			rec.peerIdHex = peer.getHexPeerId();
			rec.peerIdStr = peer.getPeerIdStr();
			//System.err.println(rec.peerIdHex + ":" + rec.peerIdStr);
			rec.peerIP = peer.getIp();
			rec.peerPort = peer.getPort();
		//}
		
		rec.lastNumLeechers = numLeechInTorrent;
		rec.lastNumSeeders = numSeedsInTorrent;
		rec.sessionSeqNum = sessionSeqNum;
		rec.startTime = new Date();
		rec.lastSeen = new Date();
		return rec;
	}

	private void updateRecordOnDisconnection(SessionRecord rec, SharingPeer peer, 
			boolean isDisconnectedByCrawler, int numSeedsInTorrent, int numLeechInTorrent) {
		
		//synchronized (peer) {
			rec.completionRate = (float)peer.getAvailablePieces().cardinality() / (float)torrent.getPieceCount();
			rec.lastBitfield = peer.getAvailablePieces();
			rec.bitfieldReceived = peer.isBitfieldReceived();
			rec.totalDownloadRate = peer.getPeerTotalDLRate().get() / (float)1024;
			rec.lastDLRate1 = peer.getPeerDLRate1().get() / (float)1024;
			rec.lastDLRate2 = peer.getPeerDLRate2().get() / (float)1024;
			rec.lastDLRate3 = peer.getPeerDLRate3().get() / (float)1024;
		//}
		
		rec.isDisconnectedByCrawler = isDisconnectedByCrawler;
		rec.lastNumLeechers = numLeechInTorrent;
		rec.lastNumSeeders = numSeedsInTorrent;
		rec.lastSeen = new Date();		
	}
	
	private int getNumLeechInTorrent() {
		return this.numLeechers;
	}

	private int getNumSeedsInTorrent() {
		return this.numSeeders;
	}

	@Override
	public void handlePieceAvailability(SharingPeer peer, Piece piece) { /* ignore */ }

	@Override
	public void handlePieceSent(SharingPeer peer, Piece piece) { /* ignore */ }

	@Override
	public void handlePieceCompleted(SharingPeer peer, Piece piece)
			throws IOException { /* ignore */ }

	public void addNewConnectedPeer(SharingPeer peer) {
		
		int numSeedsInTorrent = getNumSeedsInTorrent();
		int numLeechInTorrent = getNumLeechInTorrent();
		
		if (sessionsMap.containsKey(peer.getIp()+peer.getPort())) {  
			// means we already have some record of this peer - a regular record and/or a zero record	
			PeerStats ps = sessionsMap.get(peer.getIp()+peer.getPort());
			synchronized (ps) {
				ps.setConnected(true);
				ps.setCurrentSessionNum(ps.getCurrentSessionNum() + 1);
				SessionRecord newRecord = createNewRecord(numSeedsInTorrent, numLeechInTorrent, peer,
						ps.getCurrentSessionNum());
				ps.setCurrentSessionRecord(newRecord);
				ps.setSharingPeer(peer);  // this line is necessary in case we only had a zero record for this peerId
			}
		} else {  // means we don't have any record for this peer					
			// init peersStats and first record
			PeerStats ps = new PeerStats();
			ps.setConnected(true);
			ps.setCurrentSessionNum(1);
			ps.setSharingPeer(peer);
			SessionRecord rec1 = createNewRecord(numSeedsInTorrent, numLeechInTorrent, peer, 1);
			ps.setCurrentSessionRecord(rec1);
			this.sessionsMap.put(peer.getIp()+peer.getPort(), ps);
		}
	}	
}
