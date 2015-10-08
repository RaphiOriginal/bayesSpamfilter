package ch.fhnw.bayersfilter.spamfilter;

public class Word {
	public String word;
	public double probabilitySpam;
	public double probabilityHam;
	public double difference;
	Word(String w, double s, double h){
		word = w;
		probabilitySpam = s;
		probabilityHam = h;
		if(h < s){
			difference = s -h;
		} else {
			difference = h - s;
		}
	}
}
