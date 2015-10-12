package ch.fhnw.bayersfilter.spamfilter;

/**
 * 
 * @author raphaelbrunner
 *
 */
public class Word {
	//the needed if there is a word never found in a specific mail type
	//0 is not allowed because it would give an error in the bayers formule
	//0 * value situation would happen and always give 0 as result, or in our case 1/1 = 1
	//everything would be spam if there is somewhere one character that is only used in spam or ham
	public final double ZERO_VALUE = 0.001;
	
	//saves the word as String
	public String word;
	
	//how much the word is found in spam mails
	public double spam;
	
	//how much the word is found in ham mails
	public double ham;
	
	//how much is the diffrence between ham and spam mails to find the most significant mails
	public double difference;
	
	/**
	 * constructor
	 * @param w The word as String
	 * @param s The value of how much this word is found in spam
	 * @param h The value of how much this word is found in ham
	 */
	Word(String w, double s, double h){
		if(s == 0){
			s = ZERO_VALUE;
		}
		if(h == 0){
			h = ZERO_VALUE;
		}
		word = w;
		spam = s;
		ham = h;
		calculateDifference();
	}
	/**
	 * increases the counter with one for Spam and recalculates the difference between ham and spam
	 */
	public void addSpam(){
		if(spam == ZERO_VALUE) {
			spam = 1;
		} else {
			spam += 1;
		}
		calculateDifference();
	}
	/**
	 * increases the counter with one for Ham and recalculates the difference between ham and spam
	 */
	public void addHam(){
		if(ham == ZERO_VALUE){
			ham = 1;
		} else {
			ham += 1;
		}
		calculateDifference();
	}
	/**
	 * calculates the difference between ham and spam (important to find the most specific words)
	 */
	private void calculateDifference(){
		if(ham < spam){
			difference = spam - ham;
		} else {
			difference = ham - spam;
		}
	}
	
	@Override
	public String toString(){
		return "Word: " + word + "\tHam: " + ham + "\tSpam: " + spam + "\tDifference: " + difference;
	}
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Word){
			Word w = (Word)obj;
			return word.equals(w.word);
		}
		return false;
	}
}
