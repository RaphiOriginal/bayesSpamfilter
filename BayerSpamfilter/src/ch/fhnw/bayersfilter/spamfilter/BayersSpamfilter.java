package ch.fhnw.bayersfilter.spamfilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/***
 * 
 * @author raphaelbrunner
 * @author martineberle
 */
public class BayersSpamfilter {
	
	public enum Type {
		HAM,
		SPAM
	}
	
	private final double PROBABILITY_OF_SPAM = 0.5;
	
	private int amountOfMails = 0;
	private Map<String, Integer> spam = new HashMap<String, Integer>();
	private Map<String, Integer> ham = new HashMap<String, Integer>();
	
	/**
	 * This method is used to learn the system what spam looks like.
	 * The param is the path to the folder where a lot of spam or ham mails are saved.
	 * Type is a Flag to tell what kind of mail there are saved
	 * @param File folder
	 * @param Type mail
	 */
	public void learn(File folder, Type mail){
		if(folder.exists() && folder.isDirectory()){
			File[] files = folder.listFiles();
			for(File f: files){
				if(mail == Type.SPAM) learn(f, spam, ham);
				else learn(f, ham, spam);
			}
		}
	}
	/**
	 * This method is used to add a new mail to the learned ones and set a flag if
	 * it is a spam or ham mail.
	 * @param file
	 * @param mail
	 * @return double probability of spam
	 */
	public double addMail(File file, Type mail){
		//TODO add return value to show how much precentage of spam it is: Task 2 e)
		if(Type.SPAM == mail){
			learn(file, spam, ham);
		} else {
			learn(file, ham, spam);
		}
		return 0;
	}
	private void learn(File file, Map<String, Integer> target, Map<String, Integer> check){
		String[] words = getStringsOutOfFile(file);
		fillMapWithWords(reduceRedundanz(words), target, check);
		amountOfMails++;
	}
	private HashSet<String> reduceRedundanz(String[] words){
		//each word only one time per mail
		HashSet<String> redundanzList = new HashSet<String>();
		for(String w:words){
			redundanzList.add(w);
		}
		return redundanzList;
	}
	private void fillMapWithWords(HashSet<String> words, Map<String, Integer> target, Map<String, Integer> check){
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
	private double calculateSpam(String[] words){
		
		return 0;
	}

}
