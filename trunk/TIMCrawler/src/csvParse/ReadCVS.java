package csvParse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
 
public class ReadCVS {
	
	private static boolean compareTwoDate(String prevSessionDate, String currentSessionDate, String prevSessionIp, String currentSessionIp) {
		// "2014-03-26 22:09:09"
		if(prevSessionDate == null || prevSessionIp == null)
			return false;
		
		if(prevSessionIp.equals(currentSessionIp) == false)
			return false;
		
		SimpleDateFormat fromCvs = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		try {
			Date prevDate = fromCvs.parse(prevSessionDate);
			Date currDate = fromCvs.parse(currentSessionDate);
			long diff = currDate.getTime() - prevDate.getTime();
			long diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000) % 24;
			diffMinutes = diffMinutes + diffHours * 60;
			//System.out.println(diffMinutes);
			if(diffMinutes <= 60 && diffMinutes >= 0)
				return true;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
 
  public static void main(String[] args) {
	int select = 2;  
	  
	if(select == 1){
		 ReadCVS obj = new ReadCVS();
		 String csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\sessions84.csv";
	     String csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out84.csv";
	     String csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat84.csv";
	     obj.run(csvFileIn, csvFileOut, csvFileStat);
	} else {
	    ReadCVS obj = new ReadCVS();
	    String csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G-800.csv";
		String csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out-800.csv";
		String csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat-800.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
		csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G-700.csv";
		csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out-700.csv";
		csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat-700.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
		csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G-600.csv";
		csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out-600.csv";
		csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat-600.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
		csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G-500.csv";
		csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out-500.csv";
		csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat-500.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
		csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G-400.csv";
		csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out-400.csv";
		csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat-400.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
		csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G-300.csv";
		csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out-300.csv";
		csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat-300.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
		csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G0.csv";
		csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out0.csv";
		csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat0.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
		csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G100.csv";
		csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out100.csv";
		csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat100.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
		csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G200.csv";
		csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out200.csv";
		csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat200.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
		csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G300.csv";
		csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out300.csv";
		csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat300.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
		csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G400.csv";
		csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out400.csv";
		csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat400.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
		csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G500.csv";
		csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out500.csv";
		csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat500.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
		csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G600.csv";
		csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out600.csv";
		csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat600.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
		csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G700.csv";
		csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out700.csv";
		csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat700.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
		csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G800.csv";
		csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out800.csv";
		csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat800.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
		csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G900.csv";
		csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out900.csv";
		csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat900.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
		csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G1000.csv";
		csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out1000.csv";
		csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat1000.csv";
		obj.run(csvFileIn, csvFileOut, csvFileStat);
	}
  }
  
  private void printSession(String[] sessionRecord, BufferedWriter bdw){
		/*System.out.println("fk_test_id:[" + sessionRecord[0] + "]" + 
                "peer_ip:[" + sessionRecord[1] + "]" +
		           "peer_port:[" + sessionRecord[2] + "]" +
                "peer_id:[" + sessionRecord[3] + "]" + 
		           "session_num:[" + sessionRecord[4] + "]" +
                "start_time:[" + sessionRecord[5] + "]" +
		           "end_time:[" + sessionRecord[6] + "]" +
                "completion_rate:[" + sessionRecord[9] + "]" +
		           "client:[" + sessionRecord[10] + "]\n");*/
		try {
			bdw.append(sessionRecord[0] + "," + sessionRecord[1] + "," +
					sessionRecord[2] + "," +sessionRecord[3] + "," + sessionRecord[4] + "," + 
					sessionRecord[5] + "," + sessionRecord[6] + "," + sessionRecord[7] + "," + 
					sessionRecord[8] + "," + sessionRecord[9] + "," + sessionRecord[10] + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }
 
  public void run(String fileIn, String fileOut, String fileStat) {
 
	//String csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\sessions87.csv";
	//String csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out87.csv";
	//String csvFileStat = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat87.csv";
	String csvFileIn = fileIn;
	String csvFileOut = fileOut;
	String csvFileStat = fileStat;
	
	BufferedReader br = null;
	BufferedWriter bw = null;
	BufferedWriter bsw = null;
	//BufferedWriter bdw = null;
	String line = "";
	String cvsSplitBy = ",";
 
	try {
		br = new BufferedReader(new FileReader(csvFileIn));
		bw = new BufferedWriter(new FileWriter(csvFileOut));
		bsw = new BufferedWriter(new FileWriter(csvFileStat));
		//bdw =  new BufferedWriter(new FileWriter(debugOut));
		String[] firstSession = new String[11];
		String[] lastSession = new String[11];
		boolean findFirstSession = false;
		
		LinkedHashMap<String,Integer> stat = new LinkedHashMap<String,Integer>();
		int year = 2014;
		int month = 5;
		int day = 30;
		int hrs = 22;
		int min = 0;
		
		while((hrs != 22) || (day != 4)){
			String minute = min <= 9 ? "0" + min : "" + min;
			String reportDate = month + "/" + day + "/" + year + " " + hrs + ":" + minute;
			if(min < 59){
				min++;
			}
			else{
				min = 0;
				if(hrs < 23){
					hrs++;
				}
				else{
					hrs = 0;
					if(day < 31){
						day++;
					} else {
						day = 1;
						month ++;
					}
				}
			}
			
			//System.out.println(reportDate);
			stat.put(reportDate, new Integer(0));
		}
		
		//int count = 0;
		
		//while ((line = br.readLine()) != null) {
		        // use comma as separator
			line = br.readLine();
			if(line == null){
				System.err.println(csvFileIn);
				br.close();
				bw.close();
				bsw.close();
				return;
			}
				
			String[] sessionRecord = line.split(cvsSplitBy);
			
			
			if(findFirstSession == false){
				for(int i=0; i<11; i++){
					firstSession[i] = sessionRecord[i];
					lastSession[i] = sessionRecord[i];
				}
				//findFirstSession = true;
				//continue;
			}
			
			String prevSessionDate = null;
			String currentSessionDate = null; 
			String prevSessionIp = null;
			String currentSessionIp = null;
			boolean isSameSession = false;
			boolean stop = false;
			
			//if(findFirstSession == true){
				while (!stop) {
					 line = br.readLine();
					 if(line != null){
						 //System.err.println(line);
						 sessionRecord = line.split(cvsSplitBy);
						 currentSessionDate = sessionRecord[5];
						 currentSessionIp = sessionRecord[1]; 
						 isSameSession = compareTwoDate(prevSessionDate,currentSessionDate,prevSessionIp,currentSessionIp);
						 prevSessionDate = currentSessionDate;
						 prevSessionIp = currentSessionIp;
					 }else {
						 stop = true;
					 }
					 
					 if(stop == false && (isSameSession == true ||  
							 (sessionRecord[3].equals(firstSession[3]) &&
							 sessionRecord[1].equals(firstSession[1])))){ // equal ip and peer id
					 // find last session			 
						 for(int i=0; i<11; i++){
							lastSession[i] = sessionRecord[i];
						 }
					 } else {
						 // fill up stat
						 boolean findStartTime = false;
						 Integer v = null;
						 for (String key : stat.keySet()) {
							 //System.out.println("1:" + key);
							 //System.out.println("2:" + firstSession[5]);
							 if(key.equals(firstSession[5]) && findStartTime == false){
								 //System.out.println("ddd");
								 //printSession(firstSession, bdw);
								 findStartTime = true;
								 v = stat.get(key);
								 stat.put(key, new Integer(v.intValue() + 1));
								 // check if start time equeals last time
								 if(key.equals(lastSession[6])){
									 //System.out.println("xxx");
									 break;
								 }
								 continue;
							 }
							 
							 if(findStartTime == true){
								 v = stat.get(key);
								 stat.put(key, new Integer(v.intValue() + 1));
								 //System.out.println("3:" + key);
								 //System.out.println("4:" + lastSession[6]);
								 if(key.equals(lastSession[6])){
									 //System.out.println("xxx");
									 //printSession(lastSession, bdw);
									 break;
								 }
							 }
						 }
						 bw.append(firstSession[0] + "," + firstSession[1] + "," +
									firstSession[2] + "," +firstSession[3] + "," + firstSession[4] + "," + 
									firstSession[5] + "," + lastSession[6] + "," + "=(G1-F1)*1440" + "," + 
									firstSession[8] + "," + firstSession[9] + "," + firstSession[10] + "\n");
						 for(int i=0; i<11; i++){
							if(i==10){
								System.err.println(sessionRecord[1]);
							}
							firstSession[i] = sessionRecord[i];
							lastSession[i] = sessionRecord[i];
						 }
					 }
				}
			//}
		//}
				
		// print stats
		for (String key : stat.keySet()) {
			Integer v = stat.get(key);
			bsw.append(key + "," + v.intValue() + "\n");
		}
		
		
 
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		if (br != null) {
			try {
				br.close();
				bw.close();
				bsw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
 
	System.out.println("Done");
  }
}