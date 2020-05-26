package bfroehlich.boggle;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Scramble {

	private JFrame frame;
	private Dictionary dictionary;
	private ArrayList<JLabel> letters;
	private JTextField currentWord;
	private JTextField letterEntry;
	private WordHider[][] wordHiders;
	private JPanel board;
	
	public Scramble() {
		createFrame();
		dictionary = new Dictionary();
		newGame("REMDUR");
	}
	
	private void createFrame() {
		frame = new JFrame("It's so easy");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		JPanel panel = new JPanel();
		frame.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		board = new JPanel();
		panel.add(board);
		
		createWordHiders(7);
		
		JPanel midPanel = new JPanel();
		midPanel.setLayout(new FlowLayout());
		panel.add(midPanel);
		letters = new ArrayList<JLabel>();
		JPanel letterPanel = new JPanel();
		midPanel.add(letterPanel);
		letterPanel.setLayout(new GridLayout(3, 4));
		for(int i = 0; i < 6; i ++) {
			JLabel a = new JLabel("-");
			a.setFont(new Font("Arial", Font.PLAIN, 100));
			letters.add(a);
		}
		letterPanel.add(new JLabel(""));
		letterPanel.add(letters.get(0));
		letterPanel.add(letters.get(1));
		letterPanel.add(new JLabel(""));
		letterPanel.add(letters.get(2));
		letterPanel.add(new JLabel(""));
		letterPanel.add(new JLabel(""));
		letterPanel.add(letters.get(3));
		letterPanel.add(new JLabel(""));
		letterPanel.add(letters.get(4));
		letterPanel.add(letters.get(5));
		letterPanel.add(new JLabel(""));
		
		
		currentWord = new JTextField("", 20);
		currentWord.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				System.out.println("keyTyped " + e.getKeyCode());
				e.consume();
			}
			
			public void keyReleased(KeyEvent e) {
				System.out.println("keyReleased " + e.getKeyCode());
				textFieldKeyReleased(e.getKeyCode());
				e.consume();
			}
			
			public void keyPressed(KeyEvent e) {
				System.out.println("keyPressed " + e.getKeyCode());
				e.consume();
			}
		});
		midPanel.add(currentWord);
		
		JPanel south = new JPanel();
		panel.add(south);
		
		letterEntry = new JTextField("", 10);
		south.add(letterEntry);
		
		JButton newGame = new JButton("New Game");
		newGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String entry = letterEntry.getText();
				boolean isValidEntry = true;
				for(char c : entry.toCharArray()) {
					if(!Character.isAlphabetic(c)) {
						isValidEntry = false;
					}
				}
				if(!(entry.length() == 6)) {
					isValidEntry = false;
				}
				if(isValidEntry) {
					letterEntry.setText("");
					newGame(entry.toUpperCase());
				}
				else {
					newGame();
				}
			}
		});
		south.add(newGame);
		
		JButton giveUp = new JButton("Give Up");
		giveUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				giveUp();
				frame.pack();
			}
		});
		south.add(giveUp);
		
		frame.pack();
		

//		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
//		manager.addKeyEventDispatcher(new KeyEventDispatcher() {
//			public boolean dispatchKeyEvent(KeyEvent e) {
//				if (e.getID() == KeyEvent.KEY_PRESSED) {
//				} else if (e.getID() == KeyEvent.KEY_RELEASED) {
//					int code = e.getKeyCode();
//					keyReleased(code);
//				} else if (e.getID() == KeyEvent.KEY_TYPED) {
//				}
//				return false;
//			}
//		});
	}
	
	private void textFieldKeyReleased(int code) {
		char c = (char) (code - 65 + (int) 'a');
		if(Character.isAlphabetic(c)) {
			String letter = ("" + c).toUpperCase();
			letterPushed(letter);
		}
		else if(code == 8) {
			delete();
		}
		else if(code == 10) {
			clear();
		}
	}
	
	public class WordFoundException extends Exception {
		
	}
	
	private void createWordHiders(int size) {
		if(size < 6) {
			size = 6;
		}
		board.removeAll();
		board.setLayout(new GridLayout(size, size));
		wordHiders = new WordHider[size][size];
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				WordHider hider = new WordHider("");
				hider.setEditable(false);
				hider.setFont(new Font("Arial", Font.PLAIN, 30));
				wordHiders[i][j] = hider;
				board.add(hider);
			}
		}
	}
	
	private void letterPushed(String letter) {
		try {
			for(JLabel label : letters) {
				if(!label.getForeground().equals(Color.BLUE) && label.getText().equals(letter)) {
					label.setForeground(Color.BLUE);
					currentWord.setText(currentWord.getText() + letter);
					String word = currentWord.getText();
					for(int i = 0; i < wordHiders.length; i++) {
						WordHider[] row = wordHiders[i];
						for(int j = 0; j < row.length; j++) {
							WordHider hider = row[j];
							if(hider.getWord().equals(word) && !hider.isRevealed()) {
								hider.setRevealed(true);
								currentWord.setText("");
								throw new WordFoundException();
							}
						}
					}
					break;
				}
			}
		}
		catch(WordFoundException e) {
			for(JLabel label : letters) {
				label.setForeground(Color.BLACK);
			}
		}
		frame.pack();
	}
	
	private void delete() {
		String text = currentWord.getText();
		if(text.length() > 0) {
			String deletedLetter = text.substring(text.length()-1);
			for(JLabel label : letters) {
				if(label.getText().equals(deletedLetter) && label.getForeground().equals(Color.BLUE)) {
					label.setForeground(Color.BLACK);
					break;
				}
			}
			currentWord.setText(text.substring(0, text.length()-1));
		}
	}
	
	private void clear() {
		currentWord.setText("");
		for(JLabel label : letters) {
			label.setForeground(Color.BLACK);
		}
	}
	
	private void newGame() {
		currentWord.setText("");
		String someLetters = "";
		for(int i = 0; i < 6; i++) {
			someLetters += dictionary.letterLottery();
		}
		newGame(someLetters);
	}
	
	private void newGame(String someLetters) {		
		for(int i = 0; i < letters.size(); i++) {
			JLabel label = letters.get(i);
			String aChar = ".";
			if(i < someLetters.length()) {
				aChar = "" + someLetters.charAt(i);
			}
			label.setText(aChar);
			label.setForeground(Color.BLACK);
		}
		ArrayList<String> words = findAllPermutations();
		Collections.sort(words);
		Iterator<String> it = words.iterator();
		while(it.hasNext()) {
			String next = it.next();
			if(!isLegalWord(next)) {
				it.remove();
			}
			else if(words.get(words.indexOf(next) + 1).equals(next))  {
				it.remove();
			}
		}
		Collections.shuffle(words);
		Collections.sort(words, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.length() - o2.length();
			}
		});

		double size = Math.sqrt((double) words.size());
		if(size > (int) size) {
			size = (int) size + 1;
		}
		createWordHiders((int) size);
		
		int row = -1;
		int column = 0;
		for(int i = 0; i < words.size(); i++) {
			String word = words.get(i);
			row++;
			if(row >= wordHiders.length) {
				row = 0;
				column++;
			}
			wordHiders[row][column].setWord(word);
		}
		
		frame.pack();
	}
	
	private void giveUp() {
		for(int i = 0; i < wordHiders.length; i++) {
			WordHider[] row = wordHiders[i];
			for(int j = 0; j < row.length; j++) {
				WordHider hider = row[j];
				if(!hider.isRevealed()) {
					hider.setForeground(Color.RED);
				}
				hider.setRevealed(true);
			}
		}
	}
	
	private ArrayList<String> findAllPermutations() {
		ArrayList<String> words = new ArrayList<String>();
		for(int i = 0; i < letters.size(); i++) {
			boolean[] used = new boolean[letters.size()];
			extendWord("", i, used, words);
		}
		return words;
	}
	
	private void extendWord(String word, int lastPosition, boolean[] used, ArrayList<String> words) {
		word += letters.get(lastPosition).getText();
		used[lastPosition] = true;
		words.add(word);
		if(word.length() >= 100) {
			used[lastPosition] = false;
			return;
		}
		for(int x = 0; x < letters.size(); x++) {
			int nextPosition = x;
			if(!used[nextPosition]) {
				extendWord(word, nextPosition, used, words);
			}
		}
		word = word.substring(0, word.length()-1);
		used[lastPosition] = false;
	}
	
	private boolean isLegalWord(String word) {
		return (word.length() >= 3) && dictionary.search(word) > 0;
	}
}