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


import in.incognitech.analyse.WordCount;
import in.incognitech.cleaner.Cleaner;
import in.incognitech.model.HTTPInfo;
import in.incognitech.model.NewSiteUrl;
import in.incognitech.model.SiteInfo;


public class QueueManager implements Runnable {

	SiteInfo sitesVisited;
	BlockingQueue<NewSiteUrl> newSites;
	int MaxDownReq = 0;
	Thread th;
	final int maxdownThreads = 4;
	Semaphore Qsem;
	int Qtest;
	int counter;
	DownloadThread[] workers;
	String sourceLink;
	String HTMLPath = "./repository/html/";
	String TextPath = "./repository/text/";
	String DomRestrict;

	public QueueManager(String startSeed, int maxLinks, String RestrictDomain) {
		Qtest = 1;
		counter = 0;
		newSites = new LinkedBlockingQueue<NewSiteUrl>();
		newSites.add(new NewSiteUrl(startSeed, "- NA since it's seed URL -"));
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
    	System.out.println("Process completed successfully. Please review the output files.");
	}

	void GenerateCrawlReport(){
		ArrayList<HTTPInfo> arr = sitesVisited.getSitesArray();
		File resFile = new File("Report.html");

		try {
			FileWriter fw = new FileWriter(resFile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("<!DOCTYPE html>"
						+ "<html lang=\"en\">"

							+ "<head>"
								+ "<meta charset=\"utf-8\">"
								+ "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">"
								+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
								+ "<title>Stunning Disco Web Crawler Report</title>"
								+ "<link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css\" rel=\"stylesheet\">"
							+ "</head>"

							+ "<body role=\"document\" style=\"padding-top: 70px; padding-bottom: 30px;\">"

								+ "<nav class=\"navbar navbar-inverse navbar-fixed-top\">"
									+ "<div class=\"container\">"
										+ "<div class=\"navbar-header\">"
											+ "<button type=\"button\" class=\"navbar-toggle collapsed\" data-toggle=\"collapse\" data-target=\"#navbar\" aria-expanded=\"false\" aria-controls=\"navbar\">"
												+ "<span class=\"sr-only\">Toggle navigation</span>"
												+ "<span class=\"icon-bar\"></span>"
												+ "<span class=\"icon-bar\"></span>"
												+ "<span class=\"icon-bar\"></span>"
											+ "</button>"
											+ "<a class=\"navbar-brand\" href=\"#\">Stunning Disco</a>"
										+ "</div>"
									+ "</div>"
								+ "</nav>"

								+ "<div class=\"container theme-showcase\" role=\"main\">"
									+ "<div class=\"page-header\">"
										+ "<h1>Report</h1>"
										+ "<p class=\"bg-info text-muted\" style=\"padding: 15px;\">"
											+ "- Seed: <strong>" + this.sourceLink + "</strong><br />"
											+ "- Max. No. of URLs to crawl: <strong>" + this.MaxDownReq + "</strong><br />"
											+ "- Domain Restrictions: <strong>" + this.DomRestrict + "</strong><br />"
										+ "</p>"
									+ "</div>"

									+ "<div class=\"row\">"
										+ "<div class=\"col-md-12 col-lg-12\">"
											+ "<div class=\"table-responsive\">"
												+ "<table class=\"table table-responsive table-striped table-bordered table-hover table-condensed\">"
													+ "<thead>"
														+ "<tr>"
															+ "<th>#</th>"
															+ "<th>URL with Title</th>"
															+ "<th>Status Code</th>"
															+ "<th>File on Disk</th>"
															+ "<th># Outlinks</th>"
															+ "<th># Images</th>"
														+ "</tr>"
													+ "</thead>"
													+ "<tbody>");

										if (arr.size() > 0) {
											for( int i=0; i<arr.size(); i++ ) {
												bw.write("<tr>"
															+ "<td>"+(i+1)+"</td>"
															+ "<td><a href=\"" + arr.get(i).getLink() + "\">" + arr.get(i).getTitle() + "</a></td>"
															+ "<td>"+arr.get(i).getHTTPStatus()+"</td>"
															+ "<td>"+arr.get(i).getFileLoc()+"</td>"
															+ "<td>" + arr.get(i).getOutLinksCount() + "</td>"
															+ "<td>" + arr.get(i).getImageCount() + "</td>"
														+ "</tr>");
											}
										} else {
											bw.write("<tr><td colspan=\"6\">No links found.</td></tr>");
										}

											bw.write("</tbody>"
												+ "</table>"
											+ "</div>"
										+ "</div>"
									+ "</div>"
								+ "</div>"
							+ "</body>"
						+ "</html>");

			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected synchronized void addlinksToQueue(ArrayList<NewSiteUrl> links) {
		try {
			Qsem.acquire();
			ArrayList<HTTPInfo> curList = sitesVisited.getSitesArray();
			for (NewSiteUrl link : links) {
				boolean linkFound = false;
				for (HTTPInfo struct : curList) {
					if (link.getUrl().equals(struct.getLink())) {
						linkFound = true;
						break;
					}
				}
				if (!linkFound) {
					for (NewSiteUrl l : newSites) {
						if (link.getUrl().equals(l.getUrl())) {
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
						Thread.sleep(30);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}

		void DownloadwebPage(NewSiteUrl surl) {

			System.out.println("Crawling for: " + surl.getUrl());

			File dir = new File(HTMLPath);
			if(!dir.exists() || !dir.isDirectory()){
				dir.mkdirs();
			}
			curCtr = ++counter;
			String filename = curCtr + ".html";
			File file = new File(HTMLPath + filename);

			try {

				URL url = new URL(surl.getUrl());
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				int conCode = con.getResponseCode();

				if (conCode == 200) {
					FileWriter fw = new FileWriter(file);
					BufferedWriter bw = new BufferedWriter(fw);
					InputStream is = con.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(is));
					String s;
					while ((s = br.readLine()) != null) {
						s = s + "\n";
						bw.write(s);
					}

					bw.close();
					br.close();
					is.close();
					ArrayList<NewSiteUrl> links = getLinks(HTMLPath + filename, surl.getUrl());
					int imageCount = getImageCount(HTMLPath + filename, surl.getUrl());
					links = filterLinks(links);
					addlinksToQueue(links);
					Cleaner.saveCleanText(file.getAbsolutePath());
					HTTPInfo st = new HTTPInfo(conCode, file.getAbsolutePath(), surl.getUrl(), surl.getTitle(), links.size(), imageCount);
					sitesVisited.addds(st);
				} else {
					HTTPInfo st = new HTTPInfo(conCode, "- NA since no webpage was downloaded -", surl.getUrl(), surl.getTitle(), 0, 0);
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

		public synchronized boolean RobotAllowed (String url) {
			ArrayList<String> allowList = new ArrayList<String>();
			ArrayList<String> disallowList = new ArrayList<String>();
			try {
				URL newURL = new URL(url);
				String newBase = "http://" + newURL.getHost();
				String newRobot = newBase + "/robots.txt";
				URL Robot = new URL(newRobot);
				HttpURLConnection con = (HttpURLConnection) Robot.openConnection();
				int conCode = con.getResponseCode();
				if(conCode!=200){
					newBase = "https://" + newURL.getHost();
					newRobot = newBase + "/robots.txt";
					//System.out.println(newRobot);
					Robot = new URL(newRobot);
					con = (HttpURLConnection) Robot.openConnection();
					conCode = con.getResponseCode();
					if(conCode!=200)
						return true;
				}
				InputStream RobotStream = con.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(RobotStream));
				String Content = "";
				while((Content = br.readLine()) != null){
					//System.out.println(Content);
					if(Content.equals("User-agent: *")){
						Content = br.readLine();
						
						do{
							Content.trim();
							if(Content.startsWith("Allow")){
								String newUrl = Content.substring(6);
								newUrl = newBase + newUrl.trim();
								allowList.add(newUrl);
							}
							else if(Content.startsWith("Disallow")){
								String newUrl = Content.substring(9);
								newUrl = newBase + newUrl.trim();
								disallowList.add(newUrl);
							}
							Content = br.readLine();
						}while(Content!=null &&( Content.startsWith("Allow") || Content.startsWith("DisAllow")));
					}
					
				}
				String sAllow = "";
				for(String s : allowList){
					if(url.contains(s)){
						if(s.length()>sAllow.length()){
							sAllow = s;
						}
					}
				}
				//System.out.println("Allowed URL : " + sAllow);
				String sDisAllow = "";
				for(String s : disallowList){
					if(url.contains(s)){
						if(s.length()>sDisAllow.length()){
							sDisAllow = s;
						}
					}
				}
				//System.out.println("Disallowed URL : " + sDisAllow);
				if(sAllow.length()>sDisAllow.length())
					return true;
				else
					return false;
				
			} catch(NullPointerException e){
				
			}
			catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			
			return true;
		}

		
		
		ArrayList<NewSiteUrl> filterLinks(ArrayList<NewSiteUrl> links) {
			// This implements the code for checking the robots.txt file
			ArrayList<NewSiteUrl> neededLinks = new ArrayList<NewSiteUrl>();
			for(NewSiteUrl link : links){
				boolean addLink = true;
				if(!DomRestrict.equals("")||DomRestrict.equals(null)){
					if(link.getUrl().indexOf(DomRestrict)<0){
						addLink = false;
					}
				}
				if(addLink){
					addLink = RobotAllowed(link.getUrl());
				}

				if(addLink){
					neededLinks.add(link);
				}
			}
			return neededLinks;
		}
		

		int getImageCount(String sfname, String url) {
			File file = new File(sfname);
			int count = 0;
			try {
				Document doc = Jsoup.parse(file, "UTF-8");
				Elements aLinks = doc.select("img");
				count = aLinks.size();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return count;
		}

		ArrayList<NewSiteUrl> getLinks(String sfname, String url) {
			ArrayList<NewSiteUrl> urls = new ArrayList<NewSiteUrl>();

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
					String title = e.html();
					if ( e.children().size() > 0 ) {
						title = "- NA since it has a nested HTML markup -";
					}

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
						urls.add(new NewSiteUrl(link, title));
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return urls;
		}

	}

}
