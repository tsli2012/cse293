/** Copyright (C) 2011 Turn, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.turn.ttorrent.client;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import timc.common.TIMConfigurator;
import timc.stats.logger.StatsLogger;

import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;
import com.turn.ttorrent.client.peer.PeerActivityListener;
import com.turn.ttorrent.client.peer.SharingPeer;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;

/** A pure-java BitTorrent client.
 *
 * <p>
 * A BitTorrent client in its bare essence shares a given torrent. If the
 * torrent is not complete locally, it will continue to download it. If or
 * after the torrent is complete, the client may eventually continue to seed it
 * for other clients.
 * </p>
 *
 * <p>
 * This BitTorrent client implementation is made to be simple to embed and
 * simple to use. First, initialize a ShareTorrent object from a torrent
 * meta-info source (either a file or a byte array, see
 * com.turn.ttorrent.SharedTorrent for how to create a SharedTorrent object).
 * Then, instanciate your Client object with this SharedTorrent and call one of
 * {@link #download} to simply download the torrent, or {@link #share} to
 * download and continue seeding for the given amount of time after the
 * download completes.
 * </p>
 *
 * @author mpetazzoni
 */
public class Client extends Observable implements Runnable,
	   AnnounceResponseListener, IncomingConnectionListener,
		PeerActivityListener {

	private static final Logger logger =
		LoggerFactory.getLogger(Client.class);

	/** Peers unchoking frequency, in seconds. Current BitTorrent specification
	 * recommends 10 seconds to avoid choking fibrilation. */
	private static final int UNCHOKING_FREQUENCY = 600;

	/** Optimistic unchokes are done every 2 loop iterations, i.e. every
	 * 2*UNCHOKING_FREQUENCY seconds. */
	private static final int OPTIMISTIC_UNCHOKE_ITERATIONS = 3;

	private static final int RATE_COMPUTATION_ITERATIONS = 2;
	private static final int MAX_DOWNLOADERS_UNCHOKE = 4;
	private static final int VOLUNTARY_OUTBOUND_CONNECTIONS = 20;

	public enum ClientState {
		WAITING,
		VALIDATING,
		SHARING,
		SEEDING,
		ERROR,
		DONE;
	};

	private static final String BITTORRENT_ID_PREFIX = "-TO0042-";

	private SharedTorrent torrent;
	private ClientState state;

	private String id;
	private String hexId;

	private Thread thread;
	private boolean stop;
	private long seed;

	private InetSocketAddress address;
	private ConnectionHandler service;
	private Announce announce;
	private ThreadPoolExecutor connectionTp;
	
	private Date lastAnnounce;
	private Date lastConnection;
	
	/** Added when received from tracker, removed if initial connection failed.
	 * 	Also contains peers which are currently handled by Reconnector, or that we don't want
	 * 	to connect with them again (i.e seeders). */
	private ConcurrentMap<String, SharingPeer> peers;
	/** Added when handshake is completed, removed when IOException occurred 
	 * on OutgoingThread or IncomingThread */
	private ConcurrentMap<String, SharingPeer> connected;
	/** Peer to be followed - were connected to us, and still not seeders */
	private ConcurrentMap<String, SharingPeer> retryPeers;
	
	private StatsLogger statsLogger;

	private Random random;
	
	/** Initialize the BitTorrent client.
	 *
	 * @param address The address to bind to.
	 * @param torrent The torrent to download and share.
	 */
	public Client(InetAddress address, SharedTorrent torrent)
		throws UnknownHostException, IOException {
		this.torrent = torrent;
		this.state = ClientState.WAITING;

		this.id = Client.BITTORRENT_ID_PREFIX + UUID.randomUUID()
			.toString().split("-")[4];
		this.hexId = Torrent.toHexString(this.id);

		// Initialize the incoming connection handler and register ourselves to
		// it.
		this.service = new ConnectionHandler(this.torrent, this.id, address);
		this.service.register(this);
		this.address = this.service.getSocketAddress();

		// Initialize the announce request thread, and register ourselves to it
		// as well.
		this.announce = new Announce(this.torrent, this.id, this.address);
		this.announce.register(this);
		
		// Initialize the connection thread pool
		int poolSize = Integer.parseInt(TIMConfigurator.getProperty("tp_pool_size"));
		final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
		this.connectionTp = new ThreadPoolExecutor(poolSize, poolSize, Long.MAX_VALUE, TimeUnit.NANOSECONDS, queue);
		this.retryPeers = new ConcurrentHashMap<String, SharingPeer>();
		
		// Initializing global configuration
		TIMConfigurator.initialize();

		logger.info("BitTorrent client [..{}] for {} started and " +
			"listening at {}:{}...",
			new Object[] {
				this.hexId.substring(this.hexId.length()-6),
				this.torrent.getName(),
				this.address.getAddress().getHostName(),
				this.address.getPort()
			});
		
		logger.info("Client running at mode {}", TIMConfigurator.getOpMode().toString());

		this.peers = new ConcurrentHashMap<String, SharingPeer>();
		this.connected = new ConcurrentHashMap<String, SharingPeer>();
		this.random = new Random(System.currentTimeMillis());
		
		this.statsLogger = new StatsLogger(this.connected, this.torrent, 
				this.announce , getID());
	}
	
	/** Get this client's peer ID.
	 */
	public String getID() {
		return this.id;
	}

	/** Return the torrent this client is exchanging on.
	 */
	public SharedTorrent getTorrent() {
		return this.torrent;
	}

	/** Change this client's state and notify its observers.
	 *
	 * If the state has changed, this client's observers will be notified.
	 *
	 * @param state The new client state.
	 */
	private synchronized void setState(ClientState state) {
		if (this.state != state) {
			this.setChanged();
		}
		this.state = state;
		this.notifyObservers(this.state);
	}

	/** Return the current state of this BitTorrent client.
	 */
	public ClientState getState() {
		return this.state;
	}

	/** Download the torrent without seeding after completion.
	 */
	public void download() {
		this.share(0);
	}

	/** Download and share this client's torrent until interrupted.
	 */
	public void share() {
		this.share(-1);
	}

	/** Download and share this client's torrent.
	 *
	 * @param seed Seed time in seconds after the download is complete. Pass
	 * <code>0</code> to immediately stop after downloading.
	 */
	public synchronized void share(int seed) {
		this.seed = seed;
		this.stop = false;

		if (this.thread == null || !this.thread.isAlive()) {
			this.thread = new Thread(this);
			this.thread.setName("bt-client(.." +
					this.hexId.substring(this.hexId.length()-6)
					.toUpperCase() + ")");
			this.thread.start();
		}
	}

	/** Immediately but gracefully stop this client.
	 */
	public void stop() {
		this.stop(true);
	}

	/** Immediately but gracefully stop this client.
	 *
	 * @param wait Whether to wait for the client execution thread to complete
	 * or not. This allows for the client's state to be settled down in one of
	 * the <tt>DONE</tt> or <tt>ERROR</tt> states when this method returns.
	 */
	public void stop(boolean wait) {
		this.stop = true;

		if (this.thread != null && this.thread.isAlive()) {
			this.thread.interrupt();
			if (wait) {
				try {
					this.thread.join();
				} catch (InterruptedException ie) {
					// Ignore
				}
			}
		}

		this.thread = null;
	}

	/** Tells whether we are a seed for the torrent we're sharing.
	 */
	public boolean isSeed() {
		return this.torrent.isComplete();
	}

	/** This is the main client loop.
	 *
	 * The main client download loop is very simple: it starts the announce
	 * request thread, the incoming connection handler service, and loops
	 * unchoking peers every UNCHOKING_FREQUENCY seconds until told to stop.
	 * Every OPTIMISTIC_UNCHOKE_ITERATIONS, an optimistic unchoke will be
	 * attempted to try out other peers.
	 *
	 * Once done, it stops the announce and connection services, and returns.
	 */
	@Override
	public void run() {
		// First, analyze the torrent's local data.
		try {
			this.setState(ClientState.VALIDATING);
			this.torrent.init();
		} catch (IOException ioe) {
			logger.warn("Error while initializing torrent data: {}!",
				ioe.getMessage(), ioe);
		} catch (InterruptedException ie) {
			logger.warn("Client was interrupted during initialization. " +
					"Aborting right away.");
		} finally {
			if (!this.torrent.isInitialized()) {
				this.setState(ClientState.ERROR);
				this.torrent.close();
				return;
			}
		}

		// Initial completion test
		if (this.torrent.isComplete()) {
			this.seed();
		} else {
			this.setState(ClientState.SHARING);
		}

		this.statsLogger.start();
		this.announce.start();
		this.service.start();
		
		int idx = this.address.getPort() - 17301;
		System.out.println(idx + ":" + this.address.getPort());

		/*int optimisticIterations = 0;
		int rateComputationIterations = 0;
		int peerDLRate1MaxIterations = Integer.parseInt(TIMConfigurator.getProperty("peer_dl1_interval"));
		int peerDLRate2MaxIterations = Integer.parseInt(TIMConfigurator.getProperty("peer_dl2_interval"));
		int peerDLRate3MaxIterations = Integer.parseInt(TIMConfigurator.getProperty("peer_dl3_interval"));
		int peerDLRate1Iterations = 0;
		int peerDLRate2Iterations = 0;
		int peerDLRate3Iterations = 0;*/

		while (!this.stop) {
			/*optimisticIterations =
				(optimisticIterations == 0 ?
				 Client.OPTIMISTIC_UNCHOKE_ITERATIONS :
				 optimisticIterations - 1);

			rateComputationIterations =
				(rateComputationIterations == 0 ?
				 Client.RATE_COMPUTATION_ITERATIONS :
				 rateComputationIterations - 1);

			peerDLRate1Iterations = (peerDLRate1Iterations == 0 ? peerDLRate1MaxIterations : peerDLRate1Iterations - 1);
			peerDLRate2Iterations = (peerDLRate2Iterations == 0 ? peerDLRate2MaxIterations : peerDLRate2Iterations - 1);
			peerDLRate3Iterations = (peerDLRate3Iterations == 0 ? peerDLRate3MaxIterations : peerDLRate3Iterations - 1);
			*/
			try {
				//this.unchokePeers(optimisticIterations == 0);
				//this.info();
				
				/*if (rateComputationIterations == 0)
					this.resetPeerRates();
				
				if (peerDLRate1Iterations == 0)
					this.resetPeersDLRate1();
				
				if (peerDLRate2Iterations == 0)
					this.resetPeersDLRate2();
				
				if (peerDLRate3Iterations == 0)
					this.resetPeersDLRate3();*/
				
				reconnectToPeers(); //tsli TODO
				
			} catch (Exception e) {
				logger.error("An exception occurred during the BitTorrent " +
						"client main loop execution!", e);
			}

			try {
				Thread.sleep(Client.UNCHOKING_FREQUENCY*1000);
			} catch (InterruptedException ie) {
				logger.trace("BitTorrent main loop interrupted.");
			}
		}

		logger.error("Stopping BitTorrent client connection service " +
				"and announce threads...");
		this.service.stop();
		this.announce.stop();
		
		this.connectionTp.shutdown();

		// Close all peer connections
		logger.error("Closing all remaining peer connections...");
		//boolean unblindStop = false;
		//while(unblindStop == false){
			//unblindStop = true;
		for (SharingPeer peer : this.connected.values()) {
			peer.unbind(true);
			//if(peer.unbindStatus == false){
				//unblindStop = false;
			//}
		}
		//}
		this.torrent.close();
		this.statsLogger.stop();

		// Determine final state
		if (this.torrent.isFinished()) {
			this.setState(ClientState.DONE);
		} else {
			this.setState(ClientState.ERROR);
		}

		logger.error("BitTorrent client signing off.");
	}

	/** Display information about the BitTorrent client state.
	 *
	 * This emits an information line in the log about this client's state. It
	 * includes the number of choked peers, number of connected peers, number
	 * of known peers, information about the torrent availability and
	 * completion and current transmission rates.
	 */
	public synchronized void info() {
		logger.info(infoStr());
	}
	
	/** Return information about the BitTorrent client state.
	 *
	 * It includes the number of choked peers, number of connected peers, number
	 * of known peers, information about the torrent availability and
	 * completion and current transmission rates.
	 */
	public synchronized String infoStr() {
		
		StringBuilder sb = new StringBuilder(
				String.format("BitTorrent client %s", this.getState().name()));
		
		if ((ClientState.SEEDING.equals(state) || ClientState.SHARING.equals(state))) {
			
			float dl = 0;
			float ul = 0;
			int choked = 0;
			for (SharingPeer peer : this.connected.values()) {
				dl += peer.getDLRate().get();
				ul += peer.getULRate().get();
				if (peer.isChoked()) {
					choked++;
				}
			}
			
			// [how many choked us]/[how many we're connected to now]/[how many we were ever connected to]
			String infoString = String.format(", %d/%d/%d peers, %d/%d/%d pieces " +
				"(%s%% , %d requested), %s/%s kB/s.",
				choked,
				this.connected.size(),
				this.peers.size(),
				this.torrent.getCompletedPieces().cardinality(),
				this.torrent.getAvailablePieces().cardinality(),
				this.torrent.getPieceCount(),
				String.format("%.2f", this.torrent.getCompletion()),
				this.torrent.getRequestedPieces().cardinality(),
				String.format("%.2f", dl/1024.0),
				String.format("%.2f", ul/1024.0)
				);
			
			sb.append(infoString);
		}
		
		return sb.toString();
	}
	
	public String statusStr() {
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		String status = String.format("Reconnector has %d waiting peers, " +
						"ConnectionTP has %d queued tasks, and %d(%d) threads, " +
						"Now is %s, Last Announce %s, Last Connection %s.",
						this.retryPeers.size(),
						this.connectionTp.getQueue().size(),
						this.connectionTp.getPoolSize(),
						this.connectionTp.getActiveCount(),
						df.format(new Date()), 
						(this.lastAnnounce != null) ? df.format(this.lastAnnounce) : null,
						(this.lastConnection != null) ? df.format(this.lastConnection) : null);
		return status;
	}

	/** Reset download and upload rates from peers.
	 *
	 * This method is called every RATE_COMPUTATION_ITERATIONS to reset the
	 * download and upload rates of all peers. This contributes to making the
	 * download and upload rate computations rolling averages every
	 * UNCHOKING_FREQUENCY * RATE_COMPUTATION_ITERATIONS seconds (usually 20
	 * seconds).
	 */
	private synchronized void resetPeerRates() {
		for (SharingPeer peer : this.connected.values()) {
			peer.getDLRate().reset();
			peer.getULRate().reset();
		}
	}
	
	/** Reset peers download rates.
	 */
	private synchronized void resetPeersDLRate1() {
		for (SharingPeer peer : this.connected.values()) {
			peer.getPeerDLRate1().reset();
		}
	}
	private synchronized void resetPeersDLRate2() {
		for (SharingPeer peer : this.connected.values()) {
			peer.getPeerDLRate2().reset();
		}
	}
	private synchronized void resetPeersDLRate3() {
		for (SharingPeer peer : this.connected.values()) {
			peer.getPeerDLRate3().reset();
		}
	}

	/** Retrieve a SharingPeer object from the given peer ID, IP address and
	 * port number.
	 *
	 * This function tries to retrieve an existing peer object based on the
	 * provided peer ID, or IP+Port if no peer ID is known, or otherwise
	 * instantiates a new one and adds it to our peer repository.
	 *
	 * @param peerId The byte-encoded string containing the peer ID. It will be
	 * converted to its hexadecimal representation to lookup the peer in the
	 * repository.
	 * @param ip The peer IP address.
	 * @param port The peer listening port number.
	 */
	private SharingPeer getOrCreatePeer(byte[] peerId, String ip, int port) {
		Peer search = new Peer(ip, port,
				(peerId != null ? ByteBuffer.wrap(peerId) : (ByteBuffer)null));
		SharingPeer peer = null;
		
		//System.err.println("search:" + search.getPeerId());

		//synchronized (this.peers) {
		    //System.err.println("epeer:" + search.getIp()+search.getPort());
			peer = this.peers.get(search.getIp()+search.getPort());

			if (peer != null && peer.hasPeerId()) {
				//System.err.println("apeer:" + peer.getIp() + peer.getPeerId());
				return peer;
			}
			
			if (search.hasPeerId()) {
				peer = this.peers.get(search.getIp()+search.getPort());
				if (peer != null) {
					// Set peer ID for perviously known peer.
					peer.setPeerId(search.getPeerId());
					
					// Replace the mapping for this peer from its host
					// identifier to its now known peer ID.
					this.peers.remove(peer.getIp() + peer.getPort());
					this.peers.put(peer.getIp() + peer.getPort(), peer);
					return peer;
				}
			}

			// Last case, it really didn't exist already, add it, either from
			// peer ID or host identifier, whatever we have so that we can find
			// it later.
			peer = new SharingPeer(ip, port, search.getPeerId(), this.torrent);
			this.peers.put(peer.getIp() + peer.getPort(),peer);
			logger.trace("Created new peer {}.", peer);
		//}
			
	    //System.err.println("peer:" + peer.getHexPeerId());

		return peer;
	}

	/** Retrieve a peer comparator.
	 *
	 * Returns a peer comparator based on either the download rate or the
	 * upload rate of each peer depending on our state. While sharing, we rely
	 * on the download rate we get from each peer. When our download is
	 * complete and we're only seeding, we use the upload rate instead.
	 *
	 * @return A SharingPeer comparator that can be used to sort peers based on
	 * the download or upload rate we get from them.
	 */
	private Comparator<SharingPeer> getPeerRateComparator() {
		if (ClientState.SHARING.equals(this.state)) {
			return new SharingPeer.DLRateComparator();
		} else if (ClientState.SEEDING.equals(this.state)) {
			return new SharingPeer.ULRateComparator();
		} else {
			throw new IllegalStateException("Client is neither sharing nor " +
					"seeding, we shouldn't be comparing peers at this point.");
		}
	}

	/** Unchoke connected peers.
	 *
	 * This is one of the "clever" places of the BitTorrent client. Every
	 * OPTIMISTIC_UNCHOKING_FREQUENCY seconds, we decide which peers should be
	 * unchocked and authorized to grab pieces from us.
	 *
	 * Reciprocation (tit-for-tat) and upload capping is implemented here by
	 * carefully choosing which peers we unchoke, and which peers we choke.
	 *
	 * The four peers with the best download rate and are interested in us get
	 * unchoked. This maximizes our download rate as we'll be able to get data
	 * from there four "best" peers quickly, while allowing these peers to
	 * download from us and thus reciprocate their generosity.
	 *
	 * Peers that have a better download rate than these four downloaders but
	 * are not interested get unchoked too, we want to be able to download from
	 * them to get more data more quickly. If one becomes interested, it takes
	 * a downloader's place as one of the four top downloaders (i.e. we choke
	 * the downloader with the worst upload rate).
	 *
	 * @param optimistic Whether to perform an optimistic unchoke as well.
	 */
	private synchronized void unchokePeers(boolean optimistic) {
		// Build a set of all connected peers, we don't care about peers we're
		// not connected to.
		TreeSet<SharingPeer> bound = new TreeSet<SharingPeer>(
				this.getPeerRateComparator());
		bound.addAll(this.connected.values());

		if (bound.size() == 0) {
			logger.trace("No connected peers, skipping unchoking.");
			return;
		} else {
			logger.trace("Running unchokePeers() on {} connected peers.",
				bound.size());
		}

		int downloaders = 0;
		Set<SharingPeer> choked = new HashSet<SharingPeer>();

		// We're interested in the top downloaders first, so use a descending
		// set.
		for (SharingPeer peer : bound.descendingSet()) {
			if (downloaders < Client.MAX_DOWNLOADERS_UNCHOKE) {
				// Unchoke up to MAX_DOWNLOADERS_UNCHOKE interested peers
				if (peer.isChoking()) {
					if (peer.isInterested()) {
						downloaders++;
					}

					peer.unchoke();
				}
			} else {
				// Choke everybody else
				choked.add(peer);
			}
		}

		// Actually choke all chosen peers (if any), except the eventual
		// optimistic unchoke.
		if (choked.size() > 0) {
			SharingPeer randomPeer = choked.toArray(
					new SharingPeer[0])[this.random.nextInt(choked.size())];

			for (SharingPeer peer : choked) {
				if (optimistic && peer == randomPeer) {
					logger.debug("Optimistic unchoke of {}.", peer);
					continue;
				}

				peer.choke();
			}
		}
	}


	/** AnnounceResponseListener handler(s). **********************************/

	/** Handle a tracker announce response.
	 *
	 * The torrent's tracker answers each announce request by a response
	 * containing peers exchanging on this torrent. This information is crucial
	 * as it is the base to building our peer swarm.
	 *
	 * @param answer The B-decoded answer map.
	 * @see <a href="http://wiki.theory.org/BitTorrentSpecification#Tracker_Response">BitTorrent tracker response specification</a>
	 */
	@Override
	public void handleAnnounceResponse(Map<String, BEValue> answer) {
		
		// First, reconnect to known peers
		//logger.error("Client:handleAnnounceResponse3: " + this.connectionTp.getQueue().size());
		//long startTime = System.nanoTime();
		//reconnectToPeers();
		//long endTime = System.nanoTime();
		//long duration = endTime - startTime;
		
		try {
			//logger.error("Client:handleAnnounceResponse4: " + this.connectionTp.getQueue().size() + 
					//" :handleAnnounceResponse5: " + duration/1000000);
			this.lastAnnounce = new Date();
			int numSeeders = -1;
			int numLeechers = -1;	
			if (answer.containsKey("complete"))
				numSeeders = answer.get("complete").getInt();
			if (answer.containsKey("incomplete"))
				numLeechers = answer.get("incomplete").getInt();
			logger.error("Swarm has {} seeders and {} leechers.", numSeeders, numLeechers);
			
			// Don't allow too many simultaneous connection
			/*if (this.connected.size() >= 
					Integer.valueOf(TIMConfigurator.getProperty("max_connections"))) {
				logger.error("Connection size too large! {}.", this.connected.size());
				return;
			}*/
			
			if (!answer.containsKey("peers")) {
				// No peers returned by the tracker. Apparently we're alone on
				// this one for now.
				logger.error("No peers returned by the tracker.");
				return;
			}

			try { 
				List<BEValue> peers = answer.get("peers").getList();
				logger.error("Got tracker response with {} peer(s).",
					peers.size());
				for (BEValue peerInfo : peers) {
					Map<String, BEValue> info = peerInfo.getMap();

					try {
						byte[] peerId = info.get("peer id").getBytes();
						String ip = new String(info.get("ip").getBytes(),
								Torrent.BYTE_ENCODING);
						int port = info.get("port").getInt();
						this.processAnnouncedPeerAsynchronously(peerId, ip, port);
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
				logger.error("Got compact tracker response with {} peer(s).",
					nPeers);

				for (int i=0; i < nPeers ; i++) {
					byte[] ipBytes = new byte[4];
					peers.get(ipBytes);
					String ip = InetAddress.getByAddress(ipBytes)
						.getHostAddress();
					int port = (0xFF & (int)peers.get()) << 8
						| (0xFF & (int)peers.get());
					this.processAnnouncedPeerAsynchronously(null, ip, port);
				}
			}
		} catch (UnknownHostException uhe) {
			logger.error("Invalid compact tracker response!", uhe);
		} catch (ParseException pe) {
			logger.error("Invalid tracker response!", pe);
		} catch (InvalidBEncodingException ibee) {
			logger.error("Invalid tracker response!", ibee);
		} catch (UnsupportedEncodingException uee) {
			logger.error("{}", uee.getMessage(), uee);
		}
	}

	private void reconnectToPeers() {
		synchronized (this.retryPeers) {
			for (SharingPeer peer : this.retryPeers.values()) {
				processAnnouncedPeerAsynchronously(
						(peer.hasPeerId() ? peer.getPeerId().array() : null),
						peer.getIp(), peer.getPort());
			}
		}
	}
	
	/** Create a new <code>PeerConnector</code> and execute it on the ThreadPool.
	 *
	 * @param peerId An optional peerId byte array.
	 * @param ip The peer's IP address.
	 * @param port The peer's port.
	 */
	private void processAnnouncedPeerAsynchronously(byte[] peerId, String ip, int port) {
		int idx = this.address.getPort() - 17301;
		long result = 0;
		try {
			for (byte b: InetAddress.getByName(ip).getAddress())  
			{  
			    result = result << 8 | (b & 0xFF);  
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		//System.out.println(idx + ":" + port);
		//System.err.println(idx + ":" + result % 6);
		/*if((result % 9) != idx){
		//if((port % 6) != idx){
			return;
		}*/
		PeerConnector pc = new PeerConnector(peerId, ip, port);
		if (!(this.connectionTp.getQueue().contains(pc))) {
			try {
				this.connectionTp.execute(pc);
			} catch (RejectedExecutionException ree) {
				logger.error("RejectedExecutionException: {}", ree.getMessage());
			}
		} else {
			logger.debug("PeerConnector already in execution queue." + ":" + peerId + ":" + ip + ":" + port);
		}
	}
	
	
	/** Process a peer's information obtained in an announce reply.
	 *
	 * <p>
	 * Retrieve or create a new peer for the peer information obtained, and
	 * eventually connect to it.
	 * </p>
	 *
	 * @param peerId An optional peerId byte array.
	 * @param ip The peer's IP address.
	 * @param port The peer's port.
	 */
	private void processAnnouncedPeer(byte[] peerId, String ip, int port) {
		SharingPeer peer = this.getOrCreatePeer(peerId, ip, port);

		synchronized (peer) {
			// Attempt to connect to the peer if and only if:
			//   - We're not already connected to it;
			//   - We're not a seeder (we leave the responsibility
			//	   of connecting to peers that need to download
			//     something), or we are a seeder but we're still
			//     willing to initiate some outbound connections.
			if (!peer.isBound()){	
				if (!this.service.connect(peer)) {
					//logger.debug("Removing peer {}.", peer);
					this.peers.remove(peer.getIp()+peer.getPort());
				}
			}
		}
	}


	/** IncomingConnectionListener handler(s). ********************************/

	/** Handle a new peer connection.
	 *
	 * This handler is called once the connection has been successfully
	 * established and the handshake exchange made.
	 *
	 * This generally simply means binding the peer to the socket, which will
	 * put in place the communication thread and logic with this peer.
	 *
	 * @param s The connected socket to the remote peer. Note that if the peer
	 * somehow rejected our handshake reply, this socket might very soon get
	 * closed, but this is handled down the road.
	 * @param peerId The byte-encoded peerId extracted from the peer's
	 * handshake, after validation.
	 * @see com.turn.ttorrent.client.peer.SharingPeer
	 */
	@Override
	public void handleNewPeerConnection(Socket s, byte[] peerId) {
		this.lastConnection = new Date();
		long startTime = System.nanoTime();
		SharingPeer peer = this.getOrCreatePeer(peerId,
				s.getInetAddress().getHostAddress(), s.getPort());
		//System.err.println("handleNewPeerConnection" + peer.getHexPeerId());
		long endTime = System.nanoTime();
		long duration = endTime - startTime;

		try {
			synchronized (peer) {
				peer.register(this);
				peer.bind(s);
			}

			this.connected.put(peer.getIp()+peer.getPort(), peer);
			this.retryPeers.remove(peer.getIp()+peer.getPort());
			peer.register(this.torrent);
			
			this.statsLogger.addNewConnectedPeer(peer);
			peer.register(this.statsLogger);
						
			logger.error("New peer connection with {} [{}/{}] {}.",
				new Object[] {
					peer,
					this.connected.size(),
					this.peers.size(),
					duration
				});
			
			//peer.unbind(true);
		} catch (SocketException se) {
			this.connected.remove(peer.getIp() + peer.getPort());
			logger.warn("Could not handle new peer connection " +
					"with {}: {}", peer, se.getMessage());
		}
	}


	/** PeerActivityListener handler(s). **************************************/

	@Override
	public void handlePeerChoked(SharingPeer peer) { /* Do nothing */ }

	@Override
	public void handlePeerReady(SharingPeer peer) { /* Do nothing */ }

	@Override
	public void handlePieceAvailability(SharingPeer peer,
			Piece piece) { /* Do nothing */ }

	@Override
	public void handleBitfieldAvailability(SharingPeer peer,
			BitSet availablePieces) { /* Do nothing */ }

	@Override
	public void handlePieceSent(SharingPeer peer,
			Piece piece) { /* Do nothing */ }

	/** Piece download completion handler.
	 *
	 * When a piece is completed, and valid, we announce to all connected peers
	 * that we now have this piece.
	 *
	 * We use this handler to identify when all of the pieces have been
	 * downloaded. When that's the case, we can start the seeding period, if
	 * any.
	 *
	 * @param peer The peer we got the piece from.
	 * @param piece The piece in question.
	 */
	@Override
	public void handlePieceCompleted(SharingPeer peer, Piece piece)
		throws IOException {
		synchronized (this.torrent) {
			if (piece.isValid()) {
				// Make sure the piece is marked as completed in the torrent
				// Note: this is required because the order the
				// PeerActivityListeners are called is not defined, and we
				// might be called before the torrent's piece completion
				// handler is.
				this.torrent.markCompleted(piece);
				logger.debug("Completed download of {}, now has {}/{} pieces.",
					new Object[] {
						piece,
						this.torrent.getCompletedPieces().cardinality(),
						this.torrent.getPieceCount()
					});

//				// Send a HAVE message to all connected peers
//				Message have = Message.HaveMessage.craft(piece.getIndex());
//				for (SharingPeer remote : this.connected.values()) {
//					remote.send(have);
//				}
				
				// *** ADDED BY CHIKO
				//boolean sendHaveMessages = true;
				
				/*if (!sendHaveMessages) {
					logger.info("{} mode: After piece completed, reached completion rate {}%/{}%. NOT sending HAVE messages to peers",
							new Object[] {
							TIMConfigurator.getOpMode(),
							String.format("%.2f", this.torrent.getCompletion()),
							String.format("%.2f", TIMConfigurator.getHalfSeedCompletionRate()*100)
							});
				}*/
				// tsli
				/*else {
					// Send a HAVE message to all connected peers
					Message have = Message.HaveMessage.craft(piece.getIndex());
					for (SharingPeer remote : this.connected.values()) {
						remote.send(have);
					}
				}*/
				// *** ADDED BY CHIKO

				// Force notify after each piece is completed to propagate download
				// completion information (or new seeding state)
				this.setChanged();
				this.notifyObservers(this.state);
			}

			if (this.torrent.isComplete()) {
				logger.info("Last piece validated and completed, " +
						"download is complete.");
				this.torrent.finish();
				this.seed();
			}
		}
	}

	@Override
	public void handlePeerDisconnected(SharingPeer peer) {
		if (this.connected.remove(peer.getIp()+peer.getPort()) != null) {
			logger.error("Peer {} disconnected, [{}/{}].",
				new Object[] {
					peer,
					this.connected.size(),
					this.peers.size()
				});
		}
			
	   this.retryPeers.put(peer.getIp()+peer.getPort(), peer);
		peer.reset();
	}

	@Override
	public void handleIOException(SharingPeer peer, IOException ioe) {
		logger.error("I/O problem occured when reading or writing piece " +
				"data for peer {}: {}.", peer, ioe.getMessage());
		this.stop();
		this.setState(ClientState.ERROR);
	}


	/** Post download seeding. ************************************************/

	/** Start the seeding period, if any.
	 *
	 * This method is called when all the pieces of our torrent have been
	 * retrieved. This may happen immediately after the client starts if the
	 * torrent was already fully download or we are the initial seeder client.
	 *
	 * When the download is complete, the client switches to seeding mode for
	 * as long as requested in the <code>share()</code> call, if seeding was
	 * requested. If not, the StopSeedingTask will execute immediately to stop
	 * the client's main loop.
	 *
	 * @see StopSeedingTask
	 */
	private synchronized void seed() {
		// Silently ignore if we're already seeding.
		if (ClientState.SEEDING.equals(this.getState())) {
			return;
		}

		logger.info("Download of {} pieces completed.",
			this.torrent.getPieceCount());

		if (this.seed == 0) {
			logger.info("No seeding requested, stopping client...");
			this.stop();
			return;
		}

		this.setState(ClientState.SEEDING);
		if (this.seed < 0) {
			logger.info("Seeding indefinetely...");
			return;
		}

		logger.info("Seeding for {} seconds...", this.seed);
		Timer seedTimer = new Timer();
		seedTimer.schedule(new StopSeedingTask(this), this.seed*1000);
	}

	/** Timer task to stop seeding.
	 *
	 * This TimerTask will be called by a timer set after the download is
	 * complete to stop seeding from this client after a certain amount of
	 * requested seed time (might be 0 for immediate termination).
	 *
	 * This task simply contains a reference to this client instance and calls
	 * its <code>stop()</code> method to interrupt the client's main loop.
	 *
	 * @author mpetazzoni
	 */
	private static class StopSeedingTask extends TimerTask {

		private Client client;

		StopSeedingTask(Client client) {
			this.client = client;
		}

		@Override
		public void run() {
			this.client.stop();
		}
	};
	
	/** A Runnable object which is used for connecting to a given peer.
	 * 
	 */
	private class PeerConnector implements Runnable {
		private byte[] peerId;
		private String ip;
		private int port;
		
		public PeerConnector(byte[] peerId, String ip, int port) {
			this.peerId = peerId;
			this.ip = ip;
			this.port = port;
		}
		
		public void run() {
			processAnnouncedPeer(this.peerId, this.ip, this.port);
		}
		
		@Override
		public boolean equals(Object other) {
			if (this == other) return true;
			if (!(other instanceof PeerConnector)) return false;
			PeerConnector that = (PeerConnector)other;
			return ((this.ip.equals(that.ip)) && (this.port == that.port));
		}
		
		@Override
		public int hashCode() {
			int hash = 7;
			hash += this.ip.hashCode() * this.port;
			return hash;
		}
	}

	/** Main client entry point for standalone operation.
	 */
	/*public static void main(String[] args) {
		DOMConfigurator.configure("config/log4j.xml");

		if (args.length < 1) {
			System.err.println("usage: Client <torrent> [directory]");
			System.exit(1);
		}

		try {
			Client c = new Client(
					InetAddress.getByName(System.getenv("HOSTNAME")),
					SharedTorrent.fromFile(
					new File(args[0]),
					new File(args.length > 1 ? args[1] : "/tmp")));
			c.share();
			if (ClientState.ERROR.equals(c.getState())) {
				System.exit(1);
			}
		} catch (Exception e) {
			logger.error("Fatal error: {}", e.getMessage(), e);
			System.exit(2);
		}
	}*/
}
