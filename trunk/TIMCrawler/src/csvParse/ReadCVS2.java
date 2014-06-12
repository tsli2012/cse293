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
 
public class ReadCVS2 {
	
  public static void main(String[] args) {
    ReadCVS2 obj = new ReadCVS2();
	obj.run();
  }
  
  public void run() {
 
	String csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\out84.csv";
	String csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outDebug84.csv";
	BufferedReader br = null;
	BufferedWriter bw = null;
	String line = "";
	String cvsSplitBy = ",";
 
	try {
		br = new BufferedReader(new FileReader(csvFileIn));
		bw = new BufferedWriter(new FileWriter(csvFileOut));
		LinkedHashMap<String, Stat> stat = new LinkedHashMap<String,Stat>();
		while ((line = br.readLine()) != null) {
			String[] sessionRecord = line.split(cvsSplitBy);
			String ip = sessionRecord[1];
			//int duration = Integer.parseInt(sessionRecord[7]);
			System.err.println(ip);
			double duration = Double.parseDouble(sessionRecord[7]);
			if(stat.containsKey(ip)){
				Stat tmp = stat.get(ip);
				tmp.sessionCount++;
				tmp.totalTime += duration;
				stat.put(ip, tmp);
			}else{
				Stat tmp = new Stat();
				tmp.sessionCount = 1;
				tmp.totalTime = duration;
				tmp.clientid = sessionRecord[10];
				stat.put(ip, tmp);
			}
		}
		
		for (String key : stat.keySet()) {
			Stat v = stat.get(key);
			bw.append(key + "," + v.sessionCount + "," + v.totalTime + "," + v.clientid + "\n");
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
 
	System.out.println("Done");
  }
}