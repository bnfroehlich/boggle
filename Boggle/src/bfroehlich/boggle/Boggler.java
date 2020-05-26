package bfroehlich.boggle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

public class Boggler {

	private JFrame frame;
	private JLabel currentWordDisp;
	private JTextArea wordsFoundDisp;
	private JTextArea wordsFoundScore;
	private JTextArea wordsNotFoundDisp;
	private JTextArea wordsNotFoundScore;
	private ArrayList<String> wordsFound;
	
	private String currentWord;
	private JButton[][] buttons;
	private Point lastClicked;
	
	private JLabel time;
	private long gameStartTime;
	private Timer timer;
	
	private JRadioButton useLetterDice;
	private JRadioButton useLetterLottery;
	
	private Dictionary dictionary;
	
	public Boggler() {
		createFrame();
		dictionary = new Dictionary();
	}
	
	private void createFrame() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		JPanel grid = new JPanel();
		grid.setBorder(new LineBorder(Color.WHITE, 20));
		frame.add(grid, BorderLayout.WEST);
		grid.setLayout(new GridLayout(4, 4));
		buttons = new JButton[4][4];
		for(int i = 0; i < 4; i++) {
			final int x = i;
			for(int j = 0; j < 4; j++) {
				final int y = j;
				JButton button = new JButton();
				button.setFont(new Font("Comic Sans", Font.PLAIN, 50));
				button.setPreferredSize(new Dimension(120, 120));
				button.setBackground(Color.WHITE);
				button.setEnabled(false);
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(button.getBackground() == Color.WHITE
								&& (lastClicked == null || (x >= (lastClicked.x-1) && x <= (lastClicked.x+1) && (y >= (lastClicked.y-1) && y <= (lastClicked.y+1))))) {
							//if we are neighbor to lastclicked or lastclicked is null (indicating no letter are selected)
							//and the button is gray, meaning it has not been used yet in current word, then proceed
							button.setBackground(Color.RED);
							lastClicked = new Point(x, y);
							letterClicked(button.getText());
						}
						else {
							//clicking a non-neighbor or an already-used letter will clear the current word
							clearCurrentWord();
						}
					}
				});
				buttons[i][j] = button;
				grid.add(button);
			}
		}
		
		
		JPanel south = new JPanel();
		south.setLayout(new BorderLayout());
		frame.add(south, BorderLayout.SOUTH);
		currentWordDisp = new JLabel(" ");
		currentWordDisp.setFont(new Font("Comic Sans", Font.PLAIN, 30));
		south.add(currentWordDisp, BorderLayout.NORTH);
		
		JPanel status = new JPanel();
		south.add(status, BorderLayout.SOUTH);
		time = new JLabel("0");
		status.add(time);
		JButton newGame = new JButton("New Game");
		newGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(useLetterDice.isSelected()) {
					newGame(rollLetterDice());
				}
				else if(useLetterLottery.isSelected()) {
					ArrayList<String> letters = new ArrayList<String>();
					for(int i = 0; i < buttons.length; i++) {
						for(int j = 0; j < buttons[i].length; j++) {
							letters.add(dictionary.letterLottery());
						}
					}
					newGame(letters);
				}
			}
		});
		status.add(newGame);
		
		JButton endGame = new JButton("End Game");
		endGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				endGame();
			}
		});
		status.add(endGame);
		
		JButton letterEntry = new JButton("Letter Entry");
		letterEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				letterEntry();
			}
		});
		status.add(letterEntry);
		
		ButtonGroup group = new ButtonGroup();
		useLetterDice = new JRadioButton("Letter Dice", true);
		group.add(useLetterDice);
		status.add(useLetterDice);
		useLetterLottery = new JRadioButton("Letter Lottery", false);
		group.add(useLetterLottery);
		status.add(useLetterLottery);
		
		//center: words found
		JPanel center = new JPanel();
		center.setLayout(new BorderLayout());
		frame.add(center, BorderLayout.CENTER);
		
		wordsFoundDisp = new JTextArea("", 10, 10);
		wordsFoundDisp.setFont(new Font("Comic Sans", Font.PLAIN, 20));
		wordsFoundDisp.setEditable(false);

		JScrollPane wfScroll = new JScrollPane(wordsFoundDisp);
		center.add(wfScroll, BorderLayout.CENTER);
		
		wordsFoundScore = new JTextArea(2, 10);
		wordsFoundScore.setEditable(false);
		center.add(wordsFoundScore, BorderLayout.NORTH);

		//east: words not found
		JPanel east = new JPanel();
		east.setLayout(new BorderLayout());
		frame.add(east, BorderLayout.EAST);
		
		wordsNotFoundDisp = new JTextArea("", 10, 10);
		wordsNotFoundDisp.setFont(new Font("Comic Sans", Font.PLAIN, 20));
		wordsNotFoundDisp.setEditable(false);

		JScrollPane wnfScroll = new JScrollPane(wordsNotFoundDisp);
		east.add(wnfScroll, BorderLayout.CENTER);
		
		wordsNotFoundScore = new JTextArea(2, 10);
		wordsNotFoundScore.setEditable(false);
		east.add(wordsNotFoundScore, BorderLayout.NORTH);
		
		currentWord = "";
		wordsFound = new ArrayList<String>();
		
		frame.pack();
		frame.setVisible(true);
	}
	
	private void newGame(ArrayList<String> letters) {
		Iterator<String> it = letters.iterator();
		for(int i = 0; i < buttons.length; i++) {
			for(int j = 0; j < buttons[i].length; j++) {
				buttons[i][j].setText(it.next());
				buttons[i][j].setBackground(Color.WHITE);
				buttons[i][j].setEnabled(true);
			}
		}
		currentWord = "";
		wordsFound = new ArrayList<String>();
		wordsFoundDisp.setText("");
		wordsFoundScore.setText("");
		wordsNotFoundDisp.setText("");
		wordsNotFoundScore.setText("");
		currentWord = "";
		currentWordDisp.setText(" ");
		lastClicked = null;
		gameStartTime = System.currentTimeMillis();
		if(timer != null) {
			timer.stop();
		}
		timer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int secondsElapsed = (int) ((System.currentTimeMillis() - gameStartTime)/1000);
				if(secondsElapsed >= 180) {
					endGame();
				}
				time.setText("" + secondsElapsed);
			}
		});
		timer.start();
	}
	
	private void letterEntry() {
		ArrayList<String> letters = new ArrayList<String>();
		while(letters.isEmpty()) {
			String letterEntry = JOptionPane.showInputDialog("Letters");
			if(letterEntry == null) {
				return;
			}
			if(!(letterEntry.length() == 16)) {
				continue;
			}
			for(char c : letterEntry.toCharArray()) {
				if(Character.isAlphabetic(c)) {
					String s = ("" + c).toUpperCase();
					if(s.equals("Q")) {
						s = "QU";
					}
					letters.add(s);
				}
				else {
					letters.clear();
					break;
				}
			}
		}
		newGame(letters);
	}
	
	private void endGame() {
		for(int i = 0; i < buttons.length; i++) {
			for(int j = 0; j < buttons[i].length; j++) {
				buttons[i][j].setBackground(Color.GRAY);
				buttons[i][j].setEnabled(false);
			}
		}
		currentWord = "";
		currentWord = "";
		currentWordDisp.setText("GAME OVER");
		frame.repaint();
		frame.pack();
		lastClicked = null;
		if(timer != null) {
			timer.stop();
		}
		
		//find all possible words, remove words not in the dictionary and duplicates, then sort
		ArrayList<String> allWords = findAllWords();
		Collections.sort(allWords);
		Iterator<String> it = allWords.iterator();
		while(it.hasNext()) {
			String s = it.next();
			if(!isLegalWord(s)) {
				it.remove();
			}
			else if(allWords.get(allWords.indexOf(s) + 1).equals(s))  {
				it.remove();
			}
		}
		Collections.sort(allWords, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.length() - o2.length();
			}
		});
		
		//sort and score human words, removing human words from the computer's list
		int score = 0;
		int maxLength = 0;
		Collections.sort(wordsFound);
		Collections.sort(wordsFound, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.length() - o2.length();
			}
		});
		wordsFoundDisp.setText("");
		for(String s : wordsFound) {
			allWords.remove(s);
			maxLength = Math.max(maxLength, s.length());
			switch(s.length()) {
				case (3) : score += 1; break;
				case (4) : score += 1; break;
				case (5) : score += 2; break;
				case (6) : score += 3; break;
				case (7) : score += 5; break;
				case (8) : score += 11; break;
			}
			wordsFoundDisp.setText(wordsFoundDisp.getText() + s + "\n");
		}
		wordsFoundScore.setText("Words: " + wordsFound.size() + "\n" + "Score: " + score + "\nLongest: " + maxLength);
		
		//score computer words
		int compScore = 0;
		int maxCompLength = 0;
		for(String word : allWords) {
			maxCompLength = Math.max(maxCompLength, word.length());
			switch(word.length()) {
				case (3) : compScore += 1; break;
				case (4) : compScore += 1; break;
				case (5) : compScore += 2; break;
				case (6) : compScore += 3; break;
				case (7) : compScore += 5; break;
				case (8) : compScore += 11; break;
			}
			wordsNotFoundDisp.setText(wordsNotFoundDisp.getText() + word + "\n");
		}
		wordsNotFoundScore.setText("Computer words: " + allWords.size() + "\n" + "Score: " + compScore + "\nLongest: " + maxCompLength);
	}
	
	private ArrayList<String> findAllWords() {
		ArrayList<String> words = new ArrayList<String>();
		for(int i = 0; i < buttons.length; i++) {
			for(int j = 0; j < buttons[i].length; j++) {
				boolean[][] visited = new boolean[4][4];
				extendWord("", new Point(i, j), visited, words);
			}
		}
		return words;
	}
	
	private void extendWord(String word, Point lastLoc, boolean[][] visited, ArrayList<String> words) {
		word += buttons[lastLoc.x][lastLoc.y].getText();
		visited[lastLoc.x][lastLoc.y] = true;
		words.add(word);
		if(word.length() >= 8) {
			visited[lastLoc.x][lastLoc.y] = false;
			return;
		}
		for(int x = -1; x <= 1; x++) {
			for(int y = -1; y <= 1; y++) {
				Point nextLoc = new Point(lastLoc.x + x, lastLoc.y + y);
				if(nextLoc.x >= 0 && nextLoc.x <= 3 && nextLoc.y >= 0 && nextLoc.y <= 3) {
					if(!visited[nextLoc.x][nextLoc.y]) {
						extendWord(word, nextLoc, visited, words);
					}
				}
			}
		}
		word = word.substring(0, word.length()-1);
		visited[lastLoc.x][lastLoc.y] = false;
	}
	
	private void letterClicked(String letter) {
		currentWord += letter;
		currentWordDisp.setText(currentWord);
		if(isNewWord(currentWord)) {
			wordsFound.add(currentWord);
			wordsFoundDisp.setText(wordsFoundDisp.getText() + currentWord + "\n");
			clearCurrentWord();
		}
	}
	
	private void clearCurrentWord() {
		currentWord = "";
		currentWordDisp.setText(" ");
		lastClicked = null;
		for(int i = 0; i < buttons.length; i++) {
			for(int j = 0; j < buttons[i].length; j++) {
				buttons[i][j].setBackground(Color.WHITE);
			}
		}
	}
	
	private boolean isLegalWord(String word) {
		return word.length() >= 3 && dictionary.search(word) >= 0;
	}
	
	private boolean isNewWord(String word) {
		return isLegalWord(word) && !wordsFound.contains(word);
	}
	
	private ArrayList<String> rollLetterDice() {
		String[] diceArr = {"RIFOBX", "IFEHEY", "DENOWS", "UTOKND", "HMSRAO", "LUPETS", "ACITOA", "YLGKUE", "QBMJOA", "EHISPN", "VETIGN", "BALIYT", "EZAVND", "RALESC", "UWILRG", "PACEMD"};
		ArrayList<String> dice = new ArrayList<String>(Arrays.asList(diceArr));
		Collections.shuffle(dice);
		ArrayList<String> board = new ArrayList<String>();
		Random rand = new Random();
		for(int i = 0; i < dice.size(); i++) {
			int r = rand.nextInt(dice.get(i).length());
			String letter = "" + dice.get(i).charAt(r);
			if(letter.equals("Q")) letter = "QU";
			board.add(letter);
		}
		return board;
	}
}