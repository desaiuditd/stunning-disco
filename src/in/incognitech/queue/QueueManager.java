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

import de.l3s.boilerpipe.extractors.ArticleExtractor;
import in.incognitech.analyse.WordCount;
import in.incognitech.cleaner.Cleaner;
import in.incognitech.model.HTTPInfo;
import in.incognitech.model.SiteInfo;

public class QueueManager implements Runnable {

	SiteInfo sitesVisited;
	BlockingQueue<String> newSites;
	int MaxDownReq = 0;
	Thread th;
	final int maxdownThreads = 4;
	Semaphore Qsem;
	int Qtest;
	int counter;
	DownloadThread[] workers;
	String sourceLink;
	String HTMLPath = ".\\repository\\";
	String TextPath = ".\\TextRepository\\";
	String DomRestrict;

	public QueueManager(String startSeed, int maxLinks, String RestrictDomain) {
		Qtest = 1;
		counter = 0;
		newSites = new LinkedBlockingQueue<String>();
		newSites.add(startSeed);
		MaxDownReq = maxLinks;
		Qsem = new Semaphore(1);
		sourceLink = startSeed;
		DomRestrict = RestrictDomain;
	}

	public void downloadHTMLContent() {
		sitesVisited = new SiteInfo();

		th = new Thread(this);
		th.start();
		workers = new DownloadThread[maxdownThreads];
		for (DownloadThread worker : workers) {
			worker = new DownloadThread();
		}

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		//System.out.println("Thread starts");
		while (!newSites.isEmpty() || Qtest < 1) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		GenerateCrawlReport();
		WordCount analyse = new WordCount();
    	analyse.Analyze(TextPath);
	}
	
	void GenerateCrawlReport(){
		ArrayList<HTTPInfo> arr = sitesVisited.getSitesArray();
		File resFile = new File("Result.html");
		
		try {
			FileWriter fw = new FileWriter(resFile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("<!DOCTYPE html>" + "\n"+
					"<html lang=\"en\">" + "\n" +
						"<head>" +"\n"+
							"    <meta charset=\"utf-8\">");
			bw.write("<title>Crawl Result</title>");
			bw.write("</head>");
			bw.write("<body>");
			bw.write("<table>");
			bw.write("<tr><th>File</th><th>Status code</th><th>File on disk</th><th>link</th></tr>");
			for (HTTPInfo info : arr) {
				/*bw.write("File : " + info.getFName() + " Status code : " + info.getHTTPStatus()
						+ " File on disk : " + info.getFileLoc() + info.getFName() + " link : " + info.getLink());*/
				bw.write("<tr><td>"+info.getFName()+"</td><td>"+info.getHTTPStatus()+"</td><td>"+info.getFileLoc()+"</td><td><a href=\""+info.getLink()+"\">"+info.getLink()+"</a></td></tr>");
			}
			
			bw.write("</table>");
			bw.write("</body>");
			bw.write("</html>");
			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	protected synchronized void addlinksToQueue(ArrayList<String> links) {
		try {
			Qsem.acquire();
			ArrayList<HTTPInfo> curList = sitesVisited.getSitesArray();
			for (String link : links) {
				boolean linkFound = false;
				for (HTTPInfo struct : curList) {
					if (link.equals(struct.getLink())) {
						linkFound = true;
						break;
					}
				}
				if (!linkFound) {
					for (String l : newSites) {
						if (link.equals(l)) {
							linkFound = true;
							break;
						}
					}
				}
				if (!linkFound) {
					newSites.add(link);
				}
			}
			Qsem.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	class DownloadThread implements Runnable {

		Thread th;
		int curCtr;

		public DownloadThread() {
			th = new Thread(this);
			th.start();
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// System.out.println("Current Thread : "+this.toString());
			while (!newSites.isEmpty() || Qtest < 1) {
				if (MaxDownReq != 0 && counter >= MaxDownReq) {
					while (!newSites.isEmpty()) {
						try {
							newSites.take();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					break;
				}
				if (newSites.isEmpty()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						Qtest--;
						DownloadwebPage(newSites.take());
						Qtest++;
						Cleaner cleaner = new Cleaner();
						//cleaner.removeNoise(htmlMarkup);
						Thread.sleep(30);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}

		void DownloadwebPage(String surl) {
			File dir = new File(HTMLPath);
			if(!dir.exists()){
				dir.mkdirs();
			}
			curCtr = ++counter;
			String filename = curCtr + ".html";
			File file = new File(HTMLPath + filename);
			// System.out.println("File"+filename+"Current Thread :
			// "+this.toString());
			
			try {

				URL url = new URL(surl);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				int conCode = con.getResponseCode();
				//System.out.println(conCode);
				
				if (conCode == 200) {
					HTTPInfo st = new HTTPInfo(conCode, filename, surl);
					sitesVisited.addds(st);
					FileWriter fw = new FileWriter(file);
					BufferedWriter bw = new BufferedWriter(fw);
					InputStream is = con.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(is));
					String s;
					while ((s = br.readLine()) != null) {
						s = s + "\n";
						// System.out.print(s);
						bw.write(s);
					}

					bw.close();
					br.close();
					is.close();
					ArrayList<String> links = getLinks(HTMLPath + filename, surl);
					links = filterLinks(links);
					addlinksToQueue(links);
				}
				else {
					HTTPInfo st = new HTTPInfo(conCode, "", surl);
					sitesVisited.addds(st);
				}
				

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		ArrayList<String> filterLinks(ArrayList<String> links) {
			// This implements the code for checking the robots.txt file
			ArrayList<String> neededLinks = new ArrayList<String>();
			for(String link : links){
				boolean addLink = true;
				if(!DomRestrict.equals("")||DomRestrict.equals(null)){
					if(link.indexOf(DomRestrict)<0){
						addLink = false;
					}
				}
				//Check for Robots.txt
				
				
				if(addLink){
					neededLinks.add(link);
				}
			}
			return neededLinks;
		}

		ArrayList<String> getLinks(String sfname, String url) {
			ArrayList<String> urls = new ArrayList<String>();

			File file = new File(sfname);
			try {
				Document doc = Jsoup.parse(file, "UTF-8");
				Elements aLinks = doc.select("a[href]");
				for (Element e : aLinks) {
					String annotation = e.outerHtml();
					int beginIndex = annotation.indexOf("href");
					beginIndex = beginIndex + 6;
					int endIndex = annotation.indexOf("\"", beginIndex);
					String link = annotation.substring(beginIndex, endIndex);
					
					if (!link.startsWith("http")) {
						if (link.length() > 1) {
							if (!link.startsWith("#"))
								link = sourceLink + link.substring(1);
							else
								link = "";
						} else {
							link = "";
						}
					}
					// System.out.println(link);
					if (!link.equals(""))
						urls.add(link);
				}

				Cleaner.removeNoise(doc.outerHtml(), curCtr);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return urls;
		}

	}
	
}
