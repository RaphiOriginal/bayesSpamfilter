package ch.fhnw.bayersfilter.main;

import java.io.File;

import ch.fhnw.bayersfilter.spamfilter.BayersSpamfilter;

public class Main {

	public static void main(String[] args) {
		//set true if you like to see more details for each mails and the specific lists
		final boolean withDetails = false;
		
		//set the value at witch level a mail has to be marked as spam
		final double PROBABILITY_OF_SPAM = 0.5;
		
		//learn the mails
		BayersSpamfilter filter = new BayersSpamfilter(PROBABILITY_OF_SPAM);
		filter.learnMails(new File("../spam-anlern"), BayersSpamfilter.Type.SPAM);
		filter.learnMails(new File("../ham-anlern"), BayersSpamfilter.Type.HAM);
		
		//check the mails and learn them
		System.out.println("Ham kalibrierung:");
		System.out.println("______________________");
		filter.calibrate(new File("../ham-kallibrierung"), BayersSpamfilter.Type.HAM, withDetails);
		System.out.println("Spam kalibrierung:");
		System.out.println("______________________");
		filter.calibrate(new File("../spam-kallibrierung"),BayersSpamfilter.Type.SPAM, withDetails);
		
		//check the mails
		System.out.println("Ham test:");
		System.out.println("______________________");
		filter.checkFolder(new File("../ham-test"), withDetails);
		System.out.println("Spam test:");
		System.out.println("______________________");
		filter.checkFolder(new File("../spam-test"), withDetails);
		if(withDetails) filter.printSpecificLists();
	}

}
