package bfroehlich.boggle;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Dictionary {


	private ArrayList<String> dictionary;

	public Dictionary() {
		loadDictionary();
	}
	
	public void loadDictionary() {
		dictionary = new ArrayList<String>();
		Scanner s = null;
		try {
			InputStream stream = getClass().getResourceAsStream("dictionary.txt");
			s = new Scanner(stream);
			while(s.hasNextLine()) {
				String next = s.nextLine().toUpperCase();
				dictionary.add(next);
			}
		}
		catch(Exception e) {
			System.out.println(e.getClass().getSimpleName());
		}
		finally {
			s.close();
		}
	}
	
	public int size() {
		return dictionary.size();
	}
	
	public int search(String word) {
		return Collections.binarySearch(dictionary, word);
	}
	
	public String letterLottery() {
		double[] frequencies = {.08167, .01492, .02782, .04253, .12702, .02228, .02015, .06094, .06966, .00153, .00772, .04025, .02406, .06749, .07507, .01929, .00095, .05987, .06327, .09056, .02758, .00978, .02360, .00150, .01974, .00074};
		ArrayList<Double> thresholds = new ArrayList<Double>();
		for(int i = 0; i < frequencies.length; i++) {
			if(i == 0) {
				thresholds.add(frequencies[i]);
			}
			else {
				thresholds.add(thresholds.get(i-1) + frequencies[i]);
			}
		}
		double rand = Math.random();
		int chosenLetterIndex = 0;
		for(int i = 0; i < thresholds.size(); i++) {
			if(rand < thresholds.get(i)) {
				chosenLetterIndex = i;
				break;
			}
		}
		char chosenLetter = (char) ('A' + chosenLetterIndex);
		return "" + chosenLetter;
	}
}