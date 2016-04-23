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
	int MaxDownReq = 0;
	Thread th;
	final int maxdownThreads = 4;
	Semaphore Qsem;
	int Qtest;
	int counter;
	DownloadThread[] workers;
	String sourceLink;

	public QueueManager(String startSeed, int maxLinks) {
		Qtest = 1;
		counter = 0;
		newSites = new LinkedBlockingQueue<String>();
		newSites.add(startSeed);
		MaxDownReq = maxLinks;
		Qsem = new Semaphore(1);
		sourceLink = startSeed;
	}

	public void downloadHTMLContent() {
		sitesVisited = new SiteInfo();

		th = new Thread(this);
		th.start();
		workers = new DownloadThread[maxdownThreads];
		for (DownloadThread worker : workers) {
			worker = new DownloadThread();
			//System.out.println(worker.toString());
		}
		//System.out.println("Function ends");

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
		//System.out.println("Thread ends");

		ArrayList<HTTPInfo> arr = sitesVisited.getSitesArray();
		for (HTTPInfo info : arr) {
			System.out.println("File : " + info.getFName() + " Status code : " + info.getHTTPStatus()
					+ " File on disk : " + info.getFileLoc() + info.getFName() + " link : " + info.getLink());
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
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}

		void DownloadwebPage(String surl) {
			String path = ".\\repository\\";
			counter++;
			String filename = path + counter + ".html";
			File file = new File(filename);
			// System.out.println("File"+filename+"Current Thread :
			// "+this.toString());
			try {

				URL url = new URL(surl);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				int conCode = con.getResponseCode();
				//System.out.println(conCode);
				if (conCode == 200) {
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
				}
				HTTPInfo st = new HTTPInfo(conCode, filename, surl);
				sitesVisited.addds(st);
				if (conCode == 200) {
					ArrayList<String> links = getLinks(filename, surl);
					links = filterLinks(links);
					addlinksToQueue(links);
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
			return links;
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

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return urls;
		}

	}
}
