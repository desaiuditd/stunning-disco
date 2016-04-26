package in.incognitech.analyse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class WordCount {
	HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
	String delimiters = "[.? -/\\\";:<>_,(){}|*&#=\t]";

	public void Analyze(String dirPath) {
		File directory = new File(dirPath);
		File[] files = directory.listFiles();
		for (File f : files) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				String line;
				while ((line = br.readLine()) != null) {
					String[] words = line.split(delimiters);
					for (String word : words) {
						if (!word.equals("")&&!word.equals(" ")) {
							if (wordCount.containsKey(word)) {
								int x = wordCount.get(word);
								x++;
								wordCount.put(word, x);
							} else {
								wordCount.put(word, 1);
							}
						}
					}
				}
				br.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		Iterator iter = wordCount.entrySet().iterator();
		HashMap<Integer, ArrayList<String>> revList = new HashMap<Integer, ArrayList<String>>();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String val = (String) entry.getKey();
			Integer key = (Integer) entry.getValue();
			ArrayList<String> arr = ((revList.containsKey(key)) ? revList.get(key) : new ArrayList<String>());
			arr.add(val);
			revList.put(key, arr);
			// System.out.println(entry.getKey() + " : " + entry.getValue());
		}

		ArrayList<Integer> freq = new ArrayList<Integer>();
		Set<Integer> keySet = revList.keySet();
		for (int x : keySet) {
			freq.add(x);
		}
		Collections.sort(freq, Collections.reverseOrder());
		File resFile = new File("Result.csv");
		
		int rank = 0;
		try {
			FileWriter fw = new FileWriter(resFile);
			BufferedWriter bw = new BufferedWriter(fw);
			
			for (int i : freq) {
				ArrayList<String> arr = revList.get(i);
				for (String str : arr) {
					rank++;
					bw.write(rank+","+i + "," + str + "\n");
				}
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
