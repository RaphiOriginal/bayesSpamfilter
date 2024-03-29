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
	private double probabilityOfSpam;
	
	//these two values are used to calibrate the new probabilityOfSpam value
	private double highHamPeak = 0.0;
	private double lowSpamPeak = 1.0;
	
	//the size of the specific array
	private final int SIZE_OF_SPECIFIC_ARRAY;
	
	//count the learned mails of each Type
	private int amountOfSpamMails = 0;
	private int amountOfHamMails = 0;
	
	//save all the Words in an map
	private Map<String, Word> wordList = new HashMap<String, Word>();
	
	//save all the specific words in an map to call them fast
	private HashMap<String, Word> mostSpecificSpam = new HashMap<String, Word>();
	private HashMap<String, Word> mostSpecificHam = new HashMap<String, Word>();
 	
	//all specific words in an array to update them easier
	private Word[] mostSpecificSpamArray;
	private Word[] mostSpecificHamArray;
		
	//amount of mails classified as Spam in an check
	private int classifiedAsSpam = 0;
	
	//amount of mails classified as Ham in an check
	private int classifiedAsHam = 0;
	
	public BayersSpamfilter(double probabilityOfSpam, int arraySize){
		if(probabilityOfSpam > 1){
			probabilityOfSpam = 1;
		} else if(probabilityOfSpam < 0){
			probabilityOfSpam = 0;
		} else {
			this.probabilityOfSpam = probabilityOfSpam;
		}
		if(arraySize < 10){
			SIZE_OF_SPECIFIC_ARRAY = 10;
		} else if(arraySize > 10000){
			SIZE_OF_SPECIFIC_ARRAY = 10000;
		} else {
			SIZE_OF_SPECIFIC_ARRAY = arraySize;
		}
		mostSpecificSpamArray = new Word[SIZE_OF_SPECIFIC_ARRAY];
		mostSpecificHamArray = new Word[SIZE_OF_SPECIFIC_ARRAY];
	}
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
		return calculateSpamAll(reduceRedundanz(words));
	}
	/**
	 * Calibrates the mails in the folder and add the words to our filter. It also does
	 * recalculate the threshold of spam with the average between the highest probability in Ham
	 * and the lowest probability found in Spam
	 * with showDetail can you decide if you want to see a result for each mail
	 * @param folder
	 * @param mail
	 * @param showDetail
	 */
	public void calibrate(File folder, Type mail, boolean showDetail){
		File[] files = getFiles(folder);
		for(File f:files){
			double result = addMail(f, mail);
			if(result < probabilityOfSpam) classifiedAsHam++; else classifiedAsSpam++;
			int value = (int)(result * 100);
				if(mail == Type.SPAM){
					if(result < lowSpamPeak) lowSpamPeak = result;
					if(showDetail) System.out.println(value + "% of Spam should be Spam");
				} else {
					if(result > highHamPeak) highHamPeak = result;
					if(showDetail) System.out.println(value + "% of Spam should be Ham");
				}
		}
		probabilityOfSpam = (highHamPeak + lowSpamPeak)/2;
		System.out.println("New threshold of spam is " + probabilityOfSpam);
		printStats();
	}
	/**
	 * checks all files in a folder and prints the result to console
	 * with showDetail can you decide if you want to see the result for each mail
	 * @param folder
	 * @param showDetail
	 */
	public void checkFolder(File folder, boolean showDetail){
		updateSignificantList();
		mostSpecificSpam = convertArrayToList(mostSpecificSpamArray);
		mostSpecificHam = convertArrayToList(mostSpecificHamArray);
		File[] files = getFiles(folder);
		for(File f:files){
			String[] words = getStringsOutOfFile(f);
			double probability = calculateSpam(reduceRedundanz(words));
			String result = "Mail ist ";
			int value = (int)(probability * 100);
			result += (probability < probabilityOfSpam)? "HAM mit " + value + "%" : "SPAM mit " + value + "%";
			if(probability < probabilityOfSpam) classifiedAsHam++; else classifiedAsSpam++;
			if(showDetail) System.out.println(result);
		}
		System.out.println("WITH MOST SPECIFIC WORDS");
		printStats();
		for(File f:files){
			String[] words = getStringsOutOfFile(f);
			double probability = calculateSpamAll(reduceRedundanz(words));
			String result = "Mail ist ";
			int value = (int)(probability * 100);
			result += (probability < probabilityOfSpam)? "HAM mit " + value + "%" : "SPAM mit " + value + "%";
			if(probability < probabilityOfSpam) classifiedAsHam++; else classifiedAsSpam++;
			if(showDetail) System.out.println(result);
		}
		System.out.println("WITH ALL WORDS IN LIST");
		printStats();
	}
	/**
	 * resets the Stats values
	 */
	private void resetStats(){
		classifiedAsSpam = 0;
		classifiedAsHam = 0;
	}
	/**
	 * prints the stats made by checking mails
	 */
	private void printStats(){
		int total = classifiedAsSpam + classifiedAsHam;
		double pHam = (double)Math.round(100.0 / (double)total * (double)classifiedAsHam*100)/100;
		double pSpam = (double)Math.round(100.0 / (double)total * (double)classifiedAsSpam*100)/100;
		System.out.println("#####################################");
		System.out.println("Total checked Mails: " + total);
		System.out.println("Found amount of Ham: " + classifiedAsHam);
		System.out.println("Percentage: " + pHam + "%");
		System.out.println("Found amount of Spam: " + classifiedAsSpam);
		System.out.println("Percentage: " + pSpam + "%");
		System.out.println("#####################################");
		resetStats();
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
			if(word.ham < word.spam){
				fillTheSpecificArray(word, mostSpecificSpamArray);
			} else {
				fillTheSpecificArray(word, mostSpecificHamArray);
			}
		}
	}
	/**
	 * checks if the difference is bigger than an other word in the list and sorts the smallest out
	 * @param word
	 * @param array
	 */
	private void fillTheSpecificArray(Word word, Word[] array){
		for(int i = 0; i < array.length; i++){
			Word w = array[i];
			if(w == null){
				array[i] = word;
			} else {
				if(w.difference < word.difference){
					Word temp = w;
					array[i] = word;
					word = temp;
					if(w.equals(word)) return;
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
		if(mail == Type.HAM) amountOfHamMails++; else amountOfSpamMails++;
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
					word.addSpam();
				} else {
					word.addHam();
				}
			} else {
				if(mail == Type.SPAM) {
					wordList.put(w, new Word(w, 1, 0));
				} else {
					wordList.put(w, new Word(w, 0, 1));
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
		double probability = 1;
		for(String w: words){
			Word ws = mostSpecificSpam.get(w);
			Word wh = mostSpecificHam.get(w);
			if(ws != null){
				probability = probability * calculateHamOverSpam(ws);
			} else if(wh != null){
				probability = probability * calculateHamOverSpam(wh);
			}
		}
		return calculateBayersFormula(probability);
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
				probability = probability * calculateHamOverSpam(word);
			}
		}
		return calculateBayersFormula(probability);
	}
	/**
	 * divides the probability of ham with the probability of spam
	 * @param word
	 * @return double the divided result
	 */
	private double calculateHamOverSpam(Word word){
		return (word.ham / (double)amountOfHamMails)/
				(word.spam / (double)amountOfSpamMails);
	}
	/**
	 * calculates the probability of beeing a spam mail
	 * @param probability
	 * @return probability of beeing a spam mail
	 */
	private double calculateBayersFormula(double probability){
		final double PROBABILITY_OF_HAM = 1 - probabilityOfSpam;
		double spamFactor = PROBABILITY_OF_HAM/probabilityOfSpam;
		return 1/(1 + spamFactor * probability);
	}
	/**
	 * print all Words saved in the specific arrays
	 */
	public void printSpecificLists(){
		System.out.println("----------------------------------------------------");
		System.out.println("Words in the Spam Array:");
		int counter = 1;
		for(Word w: mostSpecificSpamArray){
			System.out.println((counter++) + ": " + w.toString());
		}
		System.out.println("Words in the Ham Array:");
		counter = 1;
		for(Word w: mostSpecificHamArray){
			System.out.println((counter++) + ": " + w.toString());
		}
		System.out.println("----------------------------------------------------");
	}

}
