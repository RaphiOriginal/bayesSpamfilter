package ch.fhnw.bayersfilter.spamfilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/***
 * 
 * @author raphaelbrunner
 * @author martineberle
 */
public class BayersSpamfilter {
	private Map<String, Integer> spam = new HashMap<String, Integer>();
	private Map<String, Integer> ham = new HashMap<String, Integer>();
	
	/**
	 * This method is used to learn the system what spam looks like.
	 * The param is the path to the folder where a lot of spam mails are saved
	 * @param folder
	 */
	public void learnSpam(File folder){
		if(folder.exists() && folder.isDirectory()){
			File[] files = folder.listFiles();
			for(File f: files){
				learn(f, spam, ham);
			}
		}
	}
	/**
	 * This method is used to learn the system what ham looks like.
	 * The param is the path to the folder where a lot of ham mails are saved
	 * @param folder
	 */
	public void learnHam(File folder){
		if(folder.exists() && folder.isDirectory()){
			File[] files = folder.listFiles();
			for(File f: files){
				learn(f, ham, spam);
			}
		}
	}
	/**
	 * This method is used to add a new mail to the learned ones and set a flag if
	 * it is a spam or ham mail.
	 * @param file
	 * @param isSpam
	 */
	public void addMail(File file, boolean isSpam){
		//TODO add return value to show how much precentage of spam it is: Task 2 e)
		if(isSpam){
			learn(file, spam, ham);
		} else {
			learn(file, ham, spam);
		}
	}
	private void learn(File file, Map<String, Integer> target, Map<String, Integer> check){
		String[] text = getStringsOutOfFile(file);
		fillMapWithWords(text, ham, spam);
	}
	private void fillMapWithWords(String[] words, Map<String, Integer> target, Map<String, Integer> check){
		for(String w: words){
			if(target.get(w) != null){
				target.put(w, target.get(w) + 1);
			} else {
				target.put(w, 1);
			}
			if(check.get(w) == null){
				check.put(w, 1);
			}
		}
	}
	private String[] getStringsOutOfFile(File f){
		StringBuffer text = new StringBuffer();
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(f));
			String line = fileReader.readLine();
			while(line != null){
				text.append(line);
				line = fileReader.readLine();
			}
			fileReader.close();
		} catch (FileNotFoundException e) {
			// TODO something is wrong with this file...
			e.printStackTrace();
		} catch (IOException e) {
			// TODO couldn't be closed for some reason...
			e.printStackTrace();
		}
		return text.toString().split("\\s+|(\\r?\\n)");
	}

}
