package in.incognitech.cleaner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

	public static void removeNoise(String htmlMarkup, int ctr) {
		// Remove Noise
		String filename = ".\\TextRepository\\";
		try {
			File dir = new File(filename);
			String output = ArticleExtractor.getInstance().getText(htmlMarkup);
			if(!dir.exists()){
				dir.mkdirs();
			}
			filename +=ctr + ".txt";
			File file = new File(filename);
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(output);
			bw.close();
		} catch (BoilerpipeProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String readFile(String filePath) {
		String content = "";
		String line = "";
		try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(filePath);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                content += line;
            }

            // Always close files.
            bufferedReader.close();

		} catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + filePath + "'");
        } catch(IOException ex) {
            System.out.println("Error reading file '" + filePath + "'");
            // Or we could just do this: 
            // ex.printStackTrace();
        }

		return content;
	}
	
	public static void saveCleanText(String filePath) {
		String content = Cleaner.readFile(filePath);
		String cleanContent = Cleaner.removeNoise(content);
		File htmlFile = new File(filePath);
		String fileName = htmlFile.getName();
		String path = "./repository/text/";
		File repoDir = new File(path);
		if(!repoDir.exists() || !repoDir.isDirectory()) {
			repoDir.mkdir();
		}
		File textFile = new File(path + fileName.replace("html", "txt"));
		try {
			FileWriter fw = new FileWriter(textFile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(cleanContent);
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*public static void main(String args[]) {
		String htmlMarkup = Cleaner.downloadPage("https://rtcamp.com/blog/rtbiz-new-home/");
		Cleaner.removeNoise(htmlMarkup);
	}*/

	private static String removeNoise(String htmlMarkup) {
		// Remove Noise
		String output = "";
		try {
			output = ArticleExtractor.getInstance().getText(htmlMarkup);
		} catch (BoilerpipeProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

}
