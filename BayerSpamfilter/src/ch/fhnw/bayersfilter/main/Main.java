package ch.fhnw.bayersfilter.main;

import java.io.File;

import ch.fhnw.bayersfilter.spamfilter.BayersSpamfilter;

public class Main {

	public static void main(String[] args) {
		BayersSpamfilter filter = new BayersSpamfilter();
		filter.learnMails(new File("../spam-anlern"), BayersSpamfilter.Type.SPAM);
		filter.learnMails(new File("../ham-anlern"), BayersSpamfilter.Type.HAM);
		
		System.out.println("Ham kalibrierung:");
		filter.checkFolder(new File("../ham-kallibrierung"));
		System.out.println("Spam kalibrierung:");
		filter.checkFolder(new File("../spam-kallibrierung"));
		
		System.out.println("Ham test:");
		filter.checkFolder(new File("../ham-test"));
		System.out.println("Spam test:");
		filter.checkFolder(new File("../spam-test"));
	}

}
