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
 
public class CheckCVSErrors {
	
	private static long compareTwoDate(String currentSessionDate,String prevSessionDate) {
		// "2014-03-26 22:09:09"
		long diffMinutes = -1;
		SimpleDateFormat fromCvs = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		try {
			Date prevDate = fromCvs.parse(prevSessionDate);
			Date currDate = fromCvs.parse(currentSessionDate);
			long diff = currDate.getTime() - prevDate.getTime();
			diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000) % 24;
			diffMinutes = diffMinutes + diffHours * 60;
			return diffMinutes;
			//System.out.println(diffMinutes);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return diffMinutes;
	}
 
  public static void main(String[] args) {
	//String prevSessionDate = "3/31/2014 22:20";
	//String currentSessionDate = "4/1/2014 0:20"; 
	//String prevSessionIp = "105.228.88.7";
	//String currentSessionIp = "105.228.88.7";
    //boolean test = ReadCVS.compareTwoDate(prevSessionDate, currentSessionDate, prevSessionIp, currentSessionIp);
	//System.out.println(test);
    CheckCVSErrors obj = new CheckCVSErrors();
	obj.run();
  }
  
  private void printSession(String[] sessionRecord){
		System.out.println("fk_test_id:[" + sessionRecord[0] + "]" + 
                "peer_ip:[" + sessionRecord[1] + "]" +
		           "peer_port:[" + sessionRecord[2] + "]" +
                "peer_id:[" + sessionRecord[3] + "]" + 
		           "session_num:[" + sessionRecord[4] + "]" +
                "start_time:[" + sessionRecord[5] + "]" +
		           "end_time:[" + sessionRecord[6] + "]" +
                "completion_rate:[" + sessionRecord[9] + "]" +
		           "client:[" + sessionRecord[10] + "]\n");
  }
  
  private static final int lineSize = 12;
 
  public void run() {
 
	String csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\sessions194.csv";
	BufferedReader br = null;
	String line = "";
	String cvsSplitBy = ",";
 
	try {
		br = new BufferedReader(new FileReader(csvFileIn));
		String[] currentSession = new String[lineSize];
		String[] previousSession = new String[lineSize];
		
		line = br.readLine();
		String[] sessionRecord = line.split(cvsSplitBy);
		for(int i=0; i<lineSize; i++){
			previousSession[i] = sessionRecord[i];
		}
		
		while ((line = br.readLine()) != null) {
			sessionRecord = line.split(cvsSplitBy);
			for(int i=0; i<lineSize; i++){
				currentSession[i] = sessionRecord[i];
			}
			
			// check 1
			/*long diff = compareTwoDate(currentSession[5],previousSession[6]);
			if(diff < -5 && currentSession[1].equals(previousSession[1])){
				System.out.print("--pre--");
				printSession(previousSession);
				System.out.print("--cur--");
				printSession(currentSession);
			}*/
			
			// check 2
			if(currentSession[1].equals(currentSession[11]) == false){
				System.out.println("IP not match!");
				printSession(currentSession);
			}
			
			for(int i=0; i<lineSize; i++){
				previousSession[i] = sessionRecord[i];
			}		
	   }

	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
 
	System.out.println("Done");
  }
}