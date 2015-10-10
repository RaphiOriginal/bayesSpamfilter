package ch.fhnw.bayersfilter.main;

import java.io.File;

import ch.fhnw.bayersfilter.spamfilter.BayersSpamfilter;

public class Main {

	public static void main(String[] args) {
		BayersSpamfilter filter = new BayersSpamfilter();
		filter.learnMails(new File("../spam-anlern"), BayersSpamfilter.Type.SPAM);
		filter.learnMails(new File("../ham-anlern"), BayersSpamfilter.Type.HAM);
		
		System.out.println("Ham kalibrierung:");
		System.out.println("______________________");
		filter.calibrate(new File("../ham-kallibrierung"), BayersSpamfilter.Type.HAM, false);
		System.out.println("Spam kalibrierung:");
		System.out.println("______________________");
		filter.calibrate(new File("../spam-kallibrierung"),BayersSpamfilter.Type.SPAM, false);
		
		System.out.println("Ham test:");
		System.out.println("______________________");
		filter.checkFolder(new File("../ham-test"), false);
		System.out.println("Spam test:");
		System.out.println("______________________");
		filter.checkFolder(new File("../spam-test"), false);
		filter.printSpecificLists();
	}

}
