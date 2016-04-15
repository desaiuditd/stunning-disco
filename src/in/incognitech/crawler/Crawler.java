package in.incognitech.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;

public class Crawler {
	
	public Crawler() {
		// TODO Auto-generated constructor stub
	}

	public void main(String[] args){
    	try {
            File file = new File("Links.csv");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            do{
                String url = br.readLine();
                //crawl(url);
            }
            while(br!=null);
            
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }	
}
