package ch.fhnw.bayersfilter.spamfilter;

import java.io.File;
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
		}
	}

}
