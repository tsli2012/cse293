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
 
public class CreateProfiler {
  String[] all = new String[1440];
	
  public static void main(String[] args) {
    CreateProfiler obj = new CreateProfiler();
    int select = 1;
    String csvFileIn;
    String csvFileOut;
    if(select == 1){
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat84.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler84.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat-800.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler-800.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat-700.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler-700.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat-600.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler-600.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat-500.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler-500.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat-400.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler-400.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat-300.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler-300.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat0.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+0.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat100.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+100.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat200.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+200.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat300.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+300.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat400.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+400.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat500.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+500.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat600.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+600.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat700.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+700.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat800.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+800.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat900.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+900.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\outStat1000.csv";
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+1000.csv";
    	obj.run(csvFileIn, csvFileOut);
    	csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profilerAll.csv";
    	try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(csvFileOut));
			bw.append("target,-800,-700,-600,-500,-400,-300,0,100,200,300,400,500,600,700,800,900,1000\n");
			for(int i=0;i<1440;i++){
				bw.append(obj.all[i] + "\n");
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
  }
 
  public void run(String in, String out) {
	String csvFileIn = in;
	String csvFileOut = out;
	BufferedReader br = null;
	BufferedWriter bw = null;
	String line = "";
	String cvsSplitBy = ",";
 
	try {
		br = new BufferedReader(new FileReader(csvFileIn));
		bw = new BufferedWriter(new FileWriter(csvFileOut));
		String[] current = new String[2];
		LinkedHashMap<String,Integer> stat = new LinkedHashMap<String,Integer>();
		int hrs = 0;
		int min = 0;
		while(true){
			String minute = min <= 9 ? "0" + min : "" + min;
			String reportDate = hrs + ":" + minute;
			System.out.println(reportDate);
			stat.put(reportDate, new Integer(0));
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
					break;
				}
			}
		}
		boolean start = false;
		while ((line = br.readLine()) != null) {
			current = line.split(cvsSplitBy);
			String c1 = current[0];
			String c2 = current[1];
			if(c1.equals("5/31/2014 0:00")){
				start = true;
			}
			if(c1.equals("6/4/2014 0:00")){
				break;
			}
			if(start == false){
				continue;
			}
			System.out.println(line);
			String[] date = c1.split(" ");
			Integer v = stat.get(date[1]);
			stat.put(date[1], new Integer(v.intValue() + Integer.parseInt(c2)));
	    }
		
		float sum = 0;
		
		for (String key : stat.keySet()) {
			Integer v = stat.get(key);
			sum += v.intValue();
		}
				
		// print stats
		int count = 1;
		for (String key : stat.keySet()) {
			Integer v = stat.get(key);
			float cal = v.floatValue()/sum;
			bw.append(key + "," + v.intValue() + "," + cal + "\n");
			if(all[count-1] == null)
				all[count-1] = cal+"";
			else
				all[count-1] = all[count-1] + "," + cal;
			count++;
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