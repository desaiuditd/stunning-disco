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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import in.incognitech.model.HTTPInfo;
import in.incognitech.model.SiteInfo;

public class QueueManager implements Runnable {

	SiteInfo sitesVisited;
	BlockingQueue<String> newSites;
	int MaxReq = 0;
	Thread th;
	final int maxdownThreads = 4;
	Semaphore Qsem;
	int Qtest;
	int counter;
	DownloadThread[] workers;
	
	public QueueManager(String startSeed){
		Qtest = 1;
		counter = 0;
		sitesVisited = new SiteInfo();
		newSites = new LinkedBlockingQueue<String>();
		newSites.add(startSeed);
		workers = new DownloadThread[maxdownThreads];
		for(DownloadThread worker:workers){
			worker = new DownloadThread();
			System.out.println(worker.toString());
		}
		Qsem = new Semaphore(1);
	}
	
	
	void downloadHTMLContent(){
		sitesVisited = new SiteInfo();
		
		
		
		
		th = new Thread(this);
		th.start();
		
	}

	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(!newSites.isEmpty() && Qtest==1){}
		
		
	}
	
	protected synchronized void addlinksToQueue(ArrayList<String> links){
		try {
			Qsem.acquire();
			ArrayList<HTTPInfo> curList =  sitesVisited.getSitesArray();
			for(String link: links){
				boolean linkFound = false;
				for(HTTPInfo struct : curList){
					if(link.equals(struct.getLink())){
						linkFound = true;
						break;
					}
				}
				if(!linkFound){
				for(String l : newSites){
					if(link.equals(l)){
						linkFound = true;
						break;
					}
				}
				}
				if(!linkFound){
					newSites.add(link);
				}
			}
			Qsem.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
			while(!newSites.isEmpty() && Qtest==1){
				Qtest--;
				if(newSites.isEmpty()){
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					try {
						DownloadwebPage(newSites.take());
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}
		
		void DownloadwebPage(String surl){
			counter++;
			String filename = counter + ".html";
			File file = new File(filename);
			
			try {
				FileWriter fw = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);
				URL url = new URL(surl);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				int conCode = con.getResponseCode();
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
				HTTPInfo st = new HTTPInfo(conCode, filename, surl);
				sitesVisited.addds(st);
				ArrayList<String> links = getLinks(filename, surl);
				links = filterLinks(links);
				addlinksToQueue(links);
				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Qtest++;
			
		}
		
		ArrayList<String> filterLinks(ArrayList<String> links){
			//This implements the code for checking the robots.txt file
			return links;
		}
		
		ArrayList<String> getLinks(String sfname, String url){
			ArrayList<String> urls = new ArrayList<String>();
			File file = new File(sfname);
			try {
				Document doc = Jsoup.parse(file, "UTF-8");
				Elements aLinks = doc.select("a[href]");
				for(Element e: aLinks){
					String annotation = e.outerHtml();
					int beginIndex = annotation.indexOf("href");
					beginIndex = beginIndex+6;
					int endIndex = annotation.indexOf("\"",beginIndex);
					String link =annotation.substring(beginIndex, endIndex);
					if(!link.startsWith("http")){
						if(link.length()>1){
							link = url + link.substring(1);
						}
						else{
							link="";
						}
					}
					System.out.println(link);
					if(!link.equals(""))
						urls.add(link);
				}
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return urls;
		}

	}
}
