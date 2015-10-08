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
		filter.calibrate(new File("../ham-kallibrierung"), BayersSpamfilter.Type.HAM);
		System.out.println("Spam kalibrierung:");
		System.out.println("______________________");
		filter.calibrate(new File("../spam-kallibrierung"),BayersSpamfilter.Type.SPAM);
		
		System.out.println("Ham test:");
		System.out.println("______________________");
		filter.checkFolder(new File("../ham-test"));
		System.out.println("Spam test:");
		System.out.println("______________________");
		filter.checkFolder(new File("../spam-test"));
	}

}
