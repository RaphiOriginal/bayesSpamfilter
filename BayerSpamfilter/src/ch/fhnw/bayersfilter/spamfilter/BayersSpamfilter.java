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
	
	/**
	 * A type used for the learning process to mark a folder or a mail as spam or ham
	 * @author raphaelbrunner
	 *
	 */
	public enum Type {
		HAM,
		SPAM
	}
	
	//the point in % when a mail is busted as Spam
	private final double PROBABILITY_OF_SPAM = 0.5;
	
	//count the learned mails of each Type
	private int amountOfSpamMails = 0;
	private int amountOfHamMails = 0;
	
	//save all the Words in an map
	private Map<String, Word> wordList = new HashMap<String, Word>();
	
	//save all the specific words in an map to call them fast
	HashMap<String, Word> mostSpecificWords = new HashMap<String, Word>();
	
	//all specific words in an array to update them easier
	Word[] mostSpecificWordsArray = new Word[50];
	
	/**
	 * This method is used to learn the system what spam looks like.
	 * The param is the path to the folder where a lot of spam or ham mails are saved.
	 * Type is a Flag to tell what kind of mail there are saved
	 * @param File folder
	 * @param Type mail
	 */
	public void learnMails(File folder, Type mail){
		File[] files = getFiles(folder);
		for(File f: files){
			learn(f, mail);
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
		String[] words = learn(file, mail);
		return calculateSpam(reduceRedundanz(words));
	}
	/**
	 * Calibrates the mails in the folder and add the words to our filter
	 * @param folder
	 * @param mail
	 */
	public void calibrate(File folder, Type mail){
		File[] files = getFiles(folder);
		for(File f:files){
			double result = addMail(f, mail);
			updateSignificantList();
			mostSpecificWords = convertArrayToList(mostSpecificWordsArray);
			int value = (int)(result * 100);
			if(mail == Type.SPAM){
				System.out.println(value + "% of Spam should be Spam");
			} else {
				System.out.println(value + "% of Spam should be Ham");
			}
		}
	}
	/**
	 * checks all files in a folder and prints the result to console
	 * @param folder
	 */
	public void checkFolder(File folder){
		updateSignificantList();
		mostSpecificWords = convertArrayToList(mostSpecificWordsArray);
		File[] files = getFiles(folder);
		for(File f:files){
			String[] words = getStringsOutOfFile(f);
			double probability = calculateSpam(reduceRedundanz(words));
			String result = "Mail ist ";
			int value = (int)(probability * 100);
			result += (probability < PROBABILITY_OF_SPAM)? "HAM mit " + value + "%" : "SPAM mit " + value + "%";
			System.out.println(result);
		}
	}
	/**
	 * returns an File array if the file is an Folder
	 * @param folder
	 * @return File[]
	 */
	private File[] getFiles(File folder){
		if(folder.exists() && folder.isDirectory()){
			return folder.listFiles();
		} else {
			return new File[0];
		}
	}
	/**
	 * searches the most significant words and puts them in an array
	 */
	private void updateSignificantList(){
		for(String w : wordList.keySet()){
			Word word = wordList.get(w);
			for(int i = 0; i < mostSpecificWordsArray.length; i++){
				if(mostSpecificWordsArray[i] == null){
					mostSpecificWordsArray[i] = word;
				} else {
					if(mostSpecificWordsArray[i].difference < word.difference){
						Word temp = mostSpecificWordsArray[i];
						mostSpecificWordsArray[i] = word;
						word = temp;
					}
				}
			}
		}
	}
	/**
	 * converts a Word array to a list
	 * @param words
	 * @return
	 */
	private HashMap<String, Word> convertArrayToList(Word[] words){
		HashMap<String, Word> list = new HashMap<String, Word>();
		for(Word w : words){
			list.put(w.word, w);
		}
		return list;
	}
	/**
	 * learns the mail as given mail type
	 * @param file
	 * @param mail
	 * @return String[]
	 */
	private String[] learn(File file, Type mail){
		String[] words = getStringsOutOfFile(file);
		fillMapWithWords(reduceRedundanz(words), mail);
		return words;
	}
	/**
	 * reduces redundanz in the String[] 
	 * @param words
	 * @return HashSet<String>
	 */
	private HashSet<String> reduceRedundanz(String[] words){
		//each word only one time per mail
		HashSet<String> redundanzList = new HashSet<String>();
		for(String w:words){
			redundanzList.add(w);
		}
		return redundanzList;
	}
	/**
	 * fills the found words in the wordList
	 * @param words
	 * @param mail
	 */
	private void fillMapWithWords(HashSet<String> words, Type mail){
		for(String w: words){
			Word word = wordList.get(w);
			if(word != null){
				if(mail == Type.SPAM){
					word.probabilitySpam += 1;
				} else {
					word.probabilityHam += 1;
				}
			} else {
				if(mail == Type.SPAM) {
					wordList.put(w, new Word(w, 1, 0.001));
				} else {
					wordList.put(w, new Word(w, 0.001, 1));
				}
			}
		}
	}
	/**
	 * Reads the words in this file end returns it in a String array
	 * @param f email
	 * @return String[]
	 */
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
	/**
	 * calculates the Spam with the most Significant Words
	 * @param words
	 * @return double how much % of Spam the mail is
	 */
	private double calculateSpam(HashSet<String> words){
		final double PROBABILITY_OF_HAM = 1 - PROBABILITY_OF_SPAM;
		double probability = 1;
		for(String w: words){
			Word wo = mostSpecificWords.get(w);
			if(wo != null){
				probability = probability * (PROBABILITY_OF_HAM * wo.probabilityHam / amountOfHamMails)/
						(PROBABILITY_OF_SPAM * wo.probabilitySpam / amountOfSpamMails);
			}
		}
		return 1/(1 + probability);
	}
	/**
	 * calculates the Spam with all Words in wordList
	 * @param words
	 * @return double how much & of Spam the mail is
	 */
	private double calculateSpamAll(HashSet<String> words){
		double probability = 1;
		for(String w: words){
			Word word = wordList.get(w);
			if(word != null){
				probability = probability * (word.probabilityHam / (double)amountOfHamMails)/
						(word.probabilitySpam / (double)amountOfSpamMails);
			}
		}
		return 1/(1 + probability);
	}

}
