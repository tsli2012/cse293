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
 
public class ParseToGMT { 
  public static void main(String[] args) {
    ParseToGMT obj = new ParseToGMT();
	try {
		obj.run();
	} catch (IOException e) {
		e.printStackTrace();
	}
  }
 
  public void run() throws IOException {
	String csvFileIn = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\sessions84.csv";
	String csvFileOut1 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G-800.csv";
	String csvFileOut2 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G-700.csv";
	String csvFileOut3 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G-600.csv";
	String csvFileOut4 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G-500.csv";
	String csvFileOut5 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G-400.csv";
	String csvFileOut6 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G-300.csv";
	String csvFileOut7 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G0.csv";
	String csvFileOut8 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G100.csv";
	String csvFileOut9 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G200.csv";
	String csvFileOut10 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G300.csv";
	String csvFileOut11 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G400.csv";
	String csvFileOut12 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G500.csv";
	String csvFileOut13 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G600.csv";
	String csvFileOut14 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G700.csv";
	String csvFileOut15 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G800.csv";
	String csvFileOut16 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G900.csv";
	String csvFileOut17 = "C:\\Users\\tsli\\workspace\\TIM\\trunk\\TIMCrawler\\src\\csvParse\\G1000.csv";
	BufferedReader br  = new BufferedReader(new FileReader(csvFileIn));
	BufferedWriter bw1 = new BufferedWriter(new FileWriter(csvFileOut1));
	BufferedWriter bw2 = new BufferedWriter(new FileWriter(csvFileOut2));
	BufferedWriter bw3 = new BufferedWriter(new FileWriter(csvFileOut3));
	BufferedWriter bw4 = new BufferedWriter(new FileWriter(csvFileOut4));
	BufferedWriter bw5 = new BufferedWriter(new FileWriter(csvFileOut5));
	BufferedWriter bw6 = new BufferedWriter(new FileWriter(csvFileOut6));
	BufferedWriter bw7 = new BufferedWriter(new FileWriter(csvFileOut7));
	BufferedWriter bw8 = new BufferedWriter(new FileWriter(csvFileOut8));
	BufferedWriter bw9 = new BufferedWriter(new FileWriter(csvFileOut9));
	BufferedWriter bw10 = new BufferedWriter(new FileWriter(csvFileOut10));
	BufferedWriter bw11 = new BufferedWriter(new FileWriter(csvFileOut11));
	BufferedWriter bw12 = new BufferedWriter(new FileWriter(csvFileOut12));
	BufferedWriter bw13 = new BufferedWriter(new FileWriter(csvFileOut13));
	BufferedWriter bw14 = new BufferedWriter(new FileWriter(csvFileOut14));
	BufferedWriter bw15 = new BufferedWriter(new FileWriter(csvFileOut15));
	BufferedWriter bw16 = new BufferedWriter(new FileWriter(csvFileOut16));
	BufferedWriter bw17 = new BufferedWriter(new FileWriter(csvFileOut17));
	String line = "";
	String cvsSplitBy = ",";
	try {
		while ((line = br.readLine()) != null) {
			String[] current = line.split(cvsSplitBy);
			String gmt = current[29];
			System.out.println(gmt);
			int gmtOffset = Integer.parseInt(gmt);
			line = line + "\n";
			if(gmtOffset >= -800 && gmtOffset <= -701){
				bw1.append(line);
			} else if(gmtOffset >= -700 && gmtOffset <= -601){
				bw2.append(line);
			} else if(gmtOffset >= -600 && gmtOffset <= -501){
				bw3.append(line);
			} else if(gmtOffset >= -500 && gmtOffset <= -401){
				bw4.append(line);
			} else if(gmtOffset >= -400 && gmtOffset <= -301){
				bw5.append(line);
			} else if(gmtOffset >= -300 && gmtOffset <= -201){
				bw6.append(line);
			} else if(gmtOffset >= 0 && gmtOffset <= 99){
				bw7.append(line);
			} else if(gmtOffset >= 100 && gmtOffset <= 199){
				bw8.append(line);
			} else if(gmtOffset >= 200 && gmtOffset <= 299){
				bw9.append(line);
			}else if(gmtOffset >= 300 && gmtOffset <= 399){
				bw10.append(line);
			}else if(gmtOffset >= 400 && gmtOffset <= 499){
				bw11.append(line);
			}else if(gmtOffset >= 500 && gmtOffset <= 599){
				bw12.append(line);
			}else if(gmtOffset >= 600 && gmtOffset <= 699){
				bw13.append(line);
			}else if(gmtOffset >= 700 && gmtOffset <= 799){
				bw14.append(line);
			}else if(gmtOffset >= 800 && gmtOffset <= 899){
				bw15.append(line);
			}else if(gmtOffset >= 900 && gmtOffset <= 999){
				bw16.append(line);
			}else if(gmtOffset >= 1000 && gmtOffset <= 1099){
				bw17.append(line);
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
				bw1.close();
				bw2.close();
				bw3.close();
				bw4.close();
				bw5.close();
				bw6.close();
				bw7.close();
				bw8.close();
				bw9.close();
				bw10.close();
				bw11.close();
				bw12.close();
				bw13.close();
				bw14.close();
				bw15.close();
				bw16.close();
				bw17.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
 
	System.out.println("Done");
  }
}