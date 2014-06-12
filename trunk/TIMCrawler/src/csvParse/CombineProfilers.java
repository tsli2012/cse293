package csvParse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
 
public class CombineProfilers {
	
  public static void main(String[] args) {
    CombineProfilers obj = new CombineProfilers();
    int select = 1;
    String csvFileIn;
    String csvFileOut = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\FinalProfiler94.csv";
    if(select == 1){
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler-800.csv";
    	obj.run(csvFileIn, csvFileOut,-800);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler-700.csv";
    	obj.run(csvFileIn, csvFileOut,-700);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler-600.csv";
    	obj.run(csvFileIn, csvFileOut,-600);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler-500.csv";
    	obj.run(csvFileIn, csvFileOut,-500);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler-400.csv";
    	obj.run(csvFileIn, csvFileOut,-400);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler-300.csv";
    	obj.run(csvFileIn, csvFileOut,-300);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+0.csv";
    	obj.run(csvFileIn, csvFileOut,0);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+100.csv";
    	obj.run(csvFileIn, csvFileOut,100);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+200.csv";
    	obj.run(csvFileIn, csvFileOut,200);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+300.csv";
    	obj.run(csvFileIn, csvFileOut,300);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+400.csv";
    	obj.run(csvFileIn, csvFileOut,400);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+500.csv";
    	obj.run(csvFileIn, csvFileOut,500);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+600.csv";
    	obj.run(csvFileIn, csvFileOut,600);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+700.csv";
    	obj.run(csvFileIn, csvFileOut,700);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+800.csv";
    	obj.run(csvFileIn, csvFileOut,800);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+900.csv";
    	obj.run(csvFileIn, csvFileOut,900);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler+1000.csv";
    	obj.run(csvFileIn, csvFileOut,1000);
    	csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\profiler94.csv";
    	obj.run(csvFileIn, csvFileOut,94);
    }
  }
 
  public void run(String in, String out, int count) {
	String csvFileIn = in;
	String csvFileOut = out;
	BufferedReader br = null;
	BufferedReader br2 = null;
	BufferedWriter bw = null;
	String line = "";
	String cvsSplitBy = ",";
 
	try {
		br = new BufferedReader(new FileReader(csvFileIn));
		bw = new BufferedWriter(new FileWriter(csvFileOut));
		br2 = new BufferedReader(new FileReader(csvFileOut));
		String[] current = new String[3];
		while ((line = br.readLine()) != null) {
			current = line.split(cvsSplitBy);
			String vector = current[2];
			line = br2.readLine();
			if(line != null){
				bw.append(line + "," + vector);
			}else{
				bw.append(vector);
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
				br2.close();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
 
	System.out.println("Done");
  }
}