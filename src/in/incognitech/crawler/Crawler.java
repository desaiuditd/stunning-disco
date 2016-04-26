package in.incognitech.crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import in.incognitech.queue.QueueManager;

public class Crawler {

	public Crawler() {
		// TODO Auto-generated constructor stub
	}

	static void printHelp() {
		System.out.println("Please enter file path of a CSV file of below format.");
		System.out.println("<site-url>, <max. no. of pages to crawl>, <restriction-domain>");
		System.out.println("http://example.com, 1000, example.com");
		System.out.println("<restriction-domain> is optional.");
	}

	public static void main(String[] args) {

		if ( args.length != 1 ) {
			Crawler.printHelp();
			return;
		}
		
		String filepath = args[0];

        String websiteURL = "";
        String pagesToCrawlStr = "";
        String restrictionDomain = "";

		try {
            File file = new File(filepath);
            Scanner inputStream = new Scanner(file);
            inputStream.useDelimiter(",");
            int count = 1;

            while(inputStream.hasNext()){
                //read single line, put in string
                String data = inputStream.next();
                data = data.replaceAll(System.getProperty("line.separator"), "");

                switch(count) {
            		case 1:
            			websiteURL = data;
            			break;
            		case 2:
            			pagesToCrawlStr = data;
            			break;
            		case 3:
            			restrictionDomain = data;
            			break;
            	}
            	count++;
            }
            // after loop, close scanner
            inputStream.close();
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

    	if ( websiteURL.equals("") || pagesToCrawlStr.equals("") ) {
    		Crawler.printHelp();
    		return;
    	}

    	int pagesToCrawl = 0;
    	try {
    		pagesToCrawl = Integer.parseInt(pagesToCrawlStr);
    	} catch( NumberFormatException e ) {
    		Crawler.printHelp();
    		return;
    	}

    	URI uri = null;
    	try {
			uri = new URI(websiteURL);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			Crawler.printHelp();
			return;
		}

    	QueueManager manager = new QueueManager(uri.toString(), pagesToCrawl, restrictionDomain);
    	manager.downloadHTMLContent();
    	
    	// URL
    	// max no to crawl
    	// restriction
    	
    	// crawl()
	}
}
