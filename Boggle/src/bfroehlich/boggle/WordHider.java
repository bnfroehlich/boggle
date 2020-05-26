package bfroehlich.boggle;

import javax.swing.JTextArea;

public class WordHider extends JTextArea {

	private String word;
	private boolean isRevealed;
	
	public WordHider(String word) {
		this.word = word;
		setRevealed(false);
	}
	
	public String getWord() {
		return word;
	}
	
	public void setWord(String word) {
		this.word = word;
		setRevealed(false);
	}
	
	public void setRevealed(boolean revealed) {
		this.isRevealed = revealed;
		if(revealed) {
			setText(word);
		}
		else {
			setText(new String(new char[word.length()]).replace("\0", "-"));
		}
	}
	
	public boolean isRevealed() {
		return isRevealed;
	}
}