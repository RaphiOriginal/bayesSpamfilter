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
	
	private int amountOfSpamMails = 0;
	private int amountOfHamMails = 0;
	private Map<String, Double> spam = new HashMap<String, Double>();
	private Map<String, Double> ham = new HashMap<String, Double>();
	HashMap<String, Word> wordsList = new HashMap<String, Word>();
	
	/**
	 * This method is used to learn the system what spam looks like.
	 * The param is the path to the folder where a lot of spam or ham mails are saved.
	 * Type is a Flag to tell what kind of mail there are saved
	 * @param File folder
	 * @param Type mail
	 */
	public void learnMails(File folder, Type mail){
		if(folder.exists() && folder.isDirectory()){
			File[] files = folder.listFiles();
			for(File f: files){
				if(mail == Type.SPAM) {
					learn(f, spam, ham);
					amountOfSpamMails++;
				}else {
					learn(f, ham, spam);
					amountOfHamMails++;
				}
			}
		}
		wordsList = getSignificantList();
	}
	/**
	 * This method is used to add a new mail to the learned ones and set a flag if
	 * it is a spam or ham mail.
	 * @param file
	 * @param mail
	 * @return double probability of spam
	 */
	public double addMail(File file, Type mail){
		String[] words;
		if(Type.SPAM == mail){
			words = learn(file, spam, ham);
			amountOfSpamMails++;
		} else {
			words = learn(file, ham, spam);
			amountOfHamMails++;
		}
		return calculateSpam(reduceRedundanz(words));
	}
	/**
	 * Calibrates the mails in the folder and add the words to our filter
	 * @param folder
	 * @param mail
	 */
	public void calibrate(File folder, Type mail){
		if(folder.exists() && folder.isDirectory()){
			File[] files = folder.listFiles();
			for(File f:files){
				double result = addMail(f, mail);
				int value = (int)(result * 100);
				if(mail == Type.SPAM){
					System.out.println(value + "% of Spam should be Spam");
				} else {
					System.out.println(value + "% of Spam should be Ham");
				}
			}
			wordsList = getSignificantList();
		}
	}
	/**
	 * checks all files in a folder and prints the result to console
	 * @param folder
	 */
	public void checkFolder(File folder){
		if(folder.exists() && folder.isDirectory()){
			File[] files = folder.listFiles();
			for(File f:files){
				String[] words = getStringsOutOfFile(f);
				double probability = calculateSpam(reduceRedundanz(words));
				String result = "Mail ist ";
				int value = (int)(probability * 100);
				result += (probability < PROBABILITY_OF_SPAM)? "HAM mit " + value + "%" : "SPAM mit " + value + "%";
				System.out.println(result);
			}
		}
	}
	private HashMap<String, Word> getSignificantList(){
		Word[] words = new Word[50];
		for(String w : spam.keySet()){
			Word word = new Word(w, spam.get(w), ham.get(w));
			for(int i = 0; i < words.length; i++){
				if(words[i] == null){
					words[i] = word;
				} else {
					if(words[i].difference < word.difference){
						Word temp = words[i];
						words[i] = word;
						word = temp;
					}
				}
			}
		}
		return convertArrayToList(words);
	}
	private HashMap<String, Word> convertArrayToList(Word[] words){
		HashMap<String, Word> list = new HashMap<String, Word>();
		for(Word w : words){
			list.put(w.word, w);
		}
		return list;
	}
	private String[] learn(File file, Map<String, Double> target, Map<String, Double> check){
		String[] words = getStringsOutOfFile(file);
		fillMapWithWords(reduceRedundanz(words), target, check);
		return words;
	}
	private HashSet<String> reduceRedundanz(String[] words){
		//each word only one time per mail
		HashSet<String> redundanzList = new HashSet<String>();
		for(String w:words){
			redundanzList.add(w);
		}
		return redundanzList;
	}
	private void fillMapWithWords(HashSet<String> words, Map<String, Double> target, Map<String, Double> check){
		for(String w: words){
			if(target.get(w) != null){
				target.put(w, target.get(w) + 1);
			} else {
				target.put(w, 1.0);
			}
			if(check.get(w) == null){
				check.put(w, 0.001);
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
	private double calculateSpam(HashSet<String> words){
		double probability = 1;
		for(String w: words){
			Word wo = wordsList.get(w);
			if(wo != null){
				probability = probability * (wo.probabilityHam / amountOfHamMails)/
						(wo.probabilitySpam / amountOfSpamMails);
			}
		}
		return 1/(1 + probability);
	}
	/*private double calculateSpam(HashSet<String> words){
		double probability = 1;
		for(String w: words){
			if(spam.get(w) != null){
				probability = probability * ((double)ham.get(w) / (double)amountOfHamMails)/
						((double)spam.get(w) / (double)amountOfSpamMails);
			}
		}
		return 1/(1 + probability);
	}*/

}
