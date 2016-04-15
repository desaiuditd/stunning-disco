package in.incognitech.queue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import in.incognitech.model.SiteInfo;

public class QueueManager implements Runnable {

	SiteInfo sitesVisited;
	BlockingQueue<String> newSites;
	int MaxReq = 0;
	Thread th;
	final int maxdownThreads = 4;
	final int maxParserThreads = 4;
	
	void downloadHTMLContent(){
		sitesVisited = new SiteInfo();
		
		
		
		
		th = new Thread(this);
		th.start();
		
	}

	public QueueManager() {
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
			
		
	}
	
	class DownloadThread implements Runnable{

		Thread th;
		public DownloadThread(){
			th = new Thread(this);
			th.start();
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub

		}
		
		void DownloadwebPage(String str){
			File file = new File("1.txt");
			
			try {
				FileWriter fw = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);
				URL url = new URL(str);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				
				InputStream is = con.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String s;
				while((s = br.readLine())!=null){
					s = s+ "\n";
					System.out.print(s);
					bw.write(s);
				}
				bw.close();
				br.close();
				is.close();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	class FileReaderThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
		
		ArrayList<String> readFile(String sfname){
			ArrayList<String> urls = new ArrayList<String>();
			File file = new File(sfname);
			try {
				Document doc = Jsoup.parse(file, "UTF-8");
				Elements aLinks = doc.select("a[href]");
				Elements lLinks = doc.select("link[href]");
				for(Element e: aLinks){
					urls.add(e.outerHtml());
				}
				for(Element e: lLinks){
					urls.add(e.outerHtml());
				}
				for(String str: urls){
					System.out.println(str);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return urls;
		}

		
	}
}
