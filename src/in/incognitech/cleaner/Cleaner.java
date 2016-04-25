package in.incognitech.cleaner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import de.l3s.boilerpipe.BoilerpipeExtractor;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import in.incognitech.queue.QueueManager;

public class Cleaner {

	public Cleaner() {
		// TODO Auto-generated constructor stub
	}

	public static void removeNoise(String htmlMarkup) {
		// Remove Noise
		try {
			String output = ArticleExtractor.getInstance().getText(htmlMarkup);
			System.out.println(output);
		} catch (BoilerpipeProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String downloadPage(String surl) {

		String s = "";
		try {
			URL url = new URL(surl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			InputStream is = con.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String buffer = "";
			while((buffer = br.readLine())!=null){
				s = s+ buffer;
			}
			br.close();
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return s;
	}

	public static void main(String args[]) {
		String htmlMarkup = Cleaner.downloadPage("https://rtcamp.com/blog/rtbiz-new-home/");
		Cleaner.removeNoise(htmlMarkup);
	}

}
