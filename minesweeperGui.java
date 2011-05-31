package Minesweeper;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class minesweeperGui extends JFrame{
	//GUI stuff.
	private static JTextArea myTextArea;//Text is displayed here.
	private JScrollPane scrollPane;//This houses the textArea to make it scrollable.
	private static JFrame mainFrame;//This is the window.
	static Font font = new Font("Monospaced", Font.PLAIN, 13);//The font.

	//App stuff.
	static char grid[][];//"The Answers," what the computer knows.
	static char interfaceGrid[][];//What the user sees.
	static boolean lost;//Winning status.
	static int flags;//Unplaced flags. 
	static boolean again = true;//If the user wants to play again.
	static boolean first = true;

	/*
	 * Constructor for the GUI frame.
	 */
	public minesweeperGui(){
		mainFrame = new JFrame();//The window
		mainFrame.setBounds(new Rectangle(new Dimension(680, 410)));
		mainFrame.setBackground(Color.BLACK);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);

		myTextArea = new JTextArea(5,20);//The text area.
		myTextArea.setBackground(Color.BLACK);
		myTextArea.setForeground(Color.WHITE);
		myTextArea.setEditable(false);
		myTextArea.setMargin(new Insets(10, 10, 10, 10));
		myTextArea.setFont(font);

		scrollPane = new JScrollPane(myTextArea);//Makes the text area scrollable, just in case.
		scrollPane.setBackground(Color.BLACK); 

		mainFrame.add(scrollPane);
		mainFrame.setVisible(true);
		mainFrame.setTitle("Minesweeper");

	}
	/*
	 * This is my main method. It gets grid settings from the user and loops, getting the user's turn 
	 * moves and displaying the grid each time. 
	 */
	public static void main(String args[]) throws IOException{
		@SuppressWarnings("unused")
		minesweeperGui g = new minesweeperGui();
		intro();
		while(again){
			if(!first)
				myTextArea.setText(null);
			printOut("\n                            Right. Let's begin.\n");
			doPause(1);
			int height = getInt("What do you want the height of the grid to be? (Max: 26)", 26);
			int width = getInt("What do you want the width of the grid to be? (Max: 26)", 26);
			int m = getInt("How many mines do you want in your grid?\nI recommend you pick " + (int)(Math.ceil(height*width*.1 + 1)) + ".", (height*width));
			myTextArea.setText(null);
			printOut("Generating the grid...\n"); gridGenerator(height,width,m); doPause(.8);
			printOut("Numbering the grid...\n"); gridNumberer(); doPause(.5);
			printOut("Bordering the grid...\n"); doPause(.3); //HAHA NOT ACTUALLY BORDERING THE GRID RIGHT THERE.
			printOut("Resizing window...\n"); doPause(.4); resizeWindow();
			runGame();
			doPause(4);
			int quit = JOptionPane.showConfirmDialog(null,
					"Would you like to play again?", "Again?", JOptionPane.YES_NO_OPTION);
			if(quit == 1){
				again = false;
				myTextArea.setText(null);
				printOut("Thanks for playing!");
				doPause(3);
				System.exit(0);
			}
			first = false;
			mainFrame.setBounds(new Rectangle(new Dimension(680, 410)));
			mainFrame.setLocationRelativeTo(null);
		}
	}
	/*
	 * This is the loop in which the game "runs."
	 */
	public static void runGame() throws IOException{
		do{
			lost=false;
			myTextArea.setText(null);
			printGrid(interfaceGrid);
			printOut("\nFlags Left: " + flags);
			String[] possibleValues = { "Open a space", "Place a flag"};
			String s = null;
			int i=0;
			while(s == null){
				s = (String) JOptionPane.showInputDialog(null, "What do you want to do?", "Input", JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
				i++;
				if(i>6){
					int quit = JOptionPane.showConfirmDialog(null,
							"Would you like to quit the game?", "Quit?", JOptionPane.YES_NO_OPTION);
					if(quit == 0){
						myTextArea.setText(null);
						printOut("Thanks for playing!");
						doPause(3);
						System.exit(0);
					}
				}
			}
			coordConverter(getChar("Gimme a row.", true),getChar("Good. Now gimme a column.", false), s);
			if(testIfWon()&&lost==false){
				lost=false;
				gameEnd();
			}
		}
		while(!lost);
	}
	/*
	 * This is the intro sequence for the app.
	 */
	public static void intro(){
		myTextArea.setText(null);
		title();
		doPause(4);
		myTextArea.setText(null);
		printOut("\n             Welcome to Salem Hilal's fantastic Minesweeper app.\n\n"); 
		doPause(1.5);
		printOut("\n  The rules are simple. The game generates a grid of covered spaces containing" +
				"\n  a certain number of mines. You, the user, pick a coordinate to be \"opened\"." +
				"\n If it's blank, all surrounding blank spaces are opened, up and until a number" +
				"\n is reached. A number signifies the number of mines in the surrounding spaces." +
				"\n You can put flags on spaces you think are mines. If all the mines are flagged," +
		"\n      YOU WIN. If you open a space with a mine, YOU LOSE. Simple, right?\n");
		doPause(6);
	}
	/*
	 * Generates a grid of @rows height and @columns width, filling it with @mines mines.
	 */
	public static void gridGenerator(int rows, int columns, int mines){
		flags = mines;//Create as many flags as there are mines. 
		grid = new char[rows][columns];
		interfaceGrid= new char[rows][columns];
		for(int a=0; a<interfaceGrid.length; a++){
			for(int b=0; b<interfaceGrid[0].length; b++){
				interfaceGrid[a][b]='?';
			}
		}
		Random rgen = new Random();
		int count = 0;
		for(int i=0; i<mines; i++){
			int r=rgen.nextInt(rows);
			int c=rgen.nextInt(columns);
			count++;
			count++;
			//If, by some odd chance, there's a mine there already, step back one and do it again.
			if(grid[r][c]=='*'){
				i--;
				count-=2;
			}
			//Otherwise, write a mine to that grid coordinate.
			else
				grid[r][c]='*';
		}
	}
	/*
	 * Adds an alphabetized set of row/column markers in a border around the grid.
	 */
	public static char[][] addBorderToGrid(char[][] oldGrid){
		char[][] newGrid = new char[oldGrid.length+4][oldGrid[0].length+4];//Can hold old + 2 space padding.
		for(int i=0; i<newGrid.length-2; i++){//For the inside of the grid rows
			newGrid[i][0]="  ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(i);
			newGrid[i][1]=' ';
			newGrid[i][newGrid[0].length-1]="  ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(i);
			newGrid[i][newGrid[0].length-2]=' ';
			for(int j = 0; j<newGrid[0].length-2; j++){//For the inside of the grid columns
				if(i==2){
					newGrid[0][j]="  ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(j);
					newGrid[1][j]=' ';
					newGrid[newGrid.length-1][j]="  ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(j);
					newGrid[newGrid.length-2][j]=' ';
				}
				if(i>=2 && j>=2)
					newGrid[i][j]=oldGrid[i-2][j-2];//Copy over the old grid.
			}
		}

		for(int i=0; i<newGrid.length; i++){

		}
		return newGrid;
	}
	/*
	 * Chews through the grid, assigning numbers to  every non-mine coordinate.
	 */
	public static void gridNumberer(){
		for(int i=0; i<grid.length; i++){//Rows
			for(int j=0; j< grid[i].length; j++){//Columns
				if(grid[i][j]!='*'){
					grid[i][j]=(""+adjacentMines(i,j)).charAt(0);
				}
			}
		}
	}
	/*
	 * Returns the number of adjacent mines for a given location, ignoring out of bounds errors.
	 */
	public static int adjacentMines(int row, int col){
		int count = 0;
		for(int i=row-1; i<=row+1; i++){
			for(int j=col-1; j<=col+1; j++){
				try{
					if(grid[i][j]=='*'){
						count++;
					}
				}
				catch(IndexOutOfBoundsException ioe){
					//Do nothing.
				};
			}
		}
		return count;
	}
	/*
	 * Prints out the grid, adding a border around the edges to indicate rows/columns.
	 */
	public static void printGrid(char[][] printGrid){
		printGrid = addBorderToGrid(printGrid);
		for(int i=0; i<printGrid.length; i++){
			for(int j=0; j<printGrid[i].length; j++){
				if(printGrid[i][j]!='0')
					printOut(printGrid[i][j] + " ");
				else
					printOut("  ");
			}
			printOut("\n");
		}
	}
	/*
	 * Prints grid as-is, without a border. It still spaces out the columns, though. 
	 */
	public static void printGridWithoutBorders(char[][] printGrid){
		for(int i=0; i<printGrid.length; i++){
			for(int j=0; j<printGrid[i].length; j++){
				System.out.print(printGrid[i][j] + " ");
			}
			System.out.println();
		}
	}
	/*
	 * The default "open" function. Reveals the selected coordinates and recursively reveals any surrounding squares,
	 * continuing if the spaces are blank. 
	 */
	public static void gridTraverser(int row, int column){
		if(grid[row][column]=='*'){
			myTextArea.setText(null);
			printOut("Awh man, you just stepped on a mine. Game over.");
			lost=true;
			gameEnd();
			return;
		}
		else if(grid[row][column]=='0'){
			//Remove the 0 from the grid and display a blank space in
			//the interface grid.
			grid[row][column]=' ';
			interfaceGrid[row][column]=' ';
			//Recurse in all 8 directions.
			for(int a=row-1; a<=row+1;a++){
				for(int b=column-1; b<=column+1; b++){
					try{
						gridTraverser(a,b);
					}
					catch(IndexOutOfBoundsException ioe){

					}
				}
			}
			return;
		}
		else if(grid[row][column]==' '){
			return;
		}
		else{//Must be a number. Display it, return. 	
			interfaceGrid[row][column]=grid[row][column];
			return;
		}
	}
	/*
	 * Checks to see if given coordinates are within the boundaries of grid.
	 */
	public static boolean inBounds(int row, int column){
		if(row>=0 && row<grid.length){
			if(column>=0 && column<grid[0].length){
				return(grid[row][column]!=' ');
			}
			else
				return false;
		}
		return false;
	}
	/*
	 * Prints out a HR the length of the grid. 
	 */
	public static void printHR(){
		System.out.println();
		for(int i=0; i<2*(grid.length+2)+1; i++){
			System.out.print("=");
		}
		System.out.println();
	}
	/*
	 * This method toggles a flag at a given row and column. It adds or subtracts from the 
	 * minesPlaced variable.
	 */
	public static void placeFlag(int row, int column){
		if(interfaceGrid[row][column]=='F'){
			interfaceGrid[row][column]='?';
			flags++;
		}
		else if(interfaceGrid[row][column]=='?'){
			interfaceGrid[row][column]='F';
			flags--;
		}
		else{
			JOptionPane.showMessageDialog(null, "You've already opened that square. \nIf there were a mine there, you'd be dead." +
					"\nNo point in putting a flag there, right?", "Try again", JOptionPane.WARNING_MESSAGE);
		}
	}
	/*
	 * This runs through the grid and tests if 1. Only mines are covered, or 2. all mines are flagged.
	 */
	public static boolean testIfWon(){
		if(flags<0){
			JOptionPane.showMessageDialog(null,"You have more flags than there are mines. " +
			"\nYou can't win unless each flag has a /nmine under it, and every mine has a flag over it.", "Watch your flags", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		else{
			boolean win1 = true;
			boolean win2 = true;
			for(int i=0; i<grid.length; i++){//Rows
				for(int j=0; j< grid[i].length; j++){//Columns
					if(grid[i][j]!='*'){//It's not a mine.
						if(interfaceGrid[i][j]=='?'){//If it's still covered,
							win1 =  false;//You haven't won yet.
						}
					}
					else if(grid[i][j]=='*'){
						if(interfaceGrid[i][j]!='F'){//And the mine isn't flagged,
							win2 =  false;//The user hasn't won yet.
							break;
						}
					}
				}
			}
			return (win1 || win2);//Every mine has been flagged, the user has won.
		}
	}
	/*
	 * End of game method. If lost is false, it congratulates you. Either way, it prints the uncovered grid.
	 */
	public static void gameEnd(){
		if(!lost){
			myTextArea.setText(null);
			printOut("\nYou found all the mines. Good job, bro.\n");
		}
		else{
			printOut("\n");
		}
		printGrid(grid);
		lost=true;
	}
	/*
	 * Honestly, the only reason this is a method is to keep things organized. That's all. I mean, look at that title.
	 */
	public static void title(){
		printOut(
				"                         ___  ___   _   __   _   _____  " +
				"\n                        /   |/   | | | |  \\ | | | ____| " +
				"\n                       / /|   /| | | | |   \\| | | |__   " +
				"\n                      / / |__/ | | | | | |\\   | |  __|  " +
				"\n                     / /       | | | | | | \\  | | |___  " +
				"\n                    /_/        |_| |_| |_|  \\_| |_____|" +
				"\n         _____   _          __  _____   _____   _____   _____   _____  " +
				"\n        /  ___/ | |        / / | ____| | ____| |  _  \\ | ____| |  _  \\   " +
				"\n        | |___  | |  __   / /  | |__   | |__   | |_| | | |__   | |_| |  " +
				"\n        \\___  \\ | | /  | / /   |  __|  |  __|  |  ___/ |  __|  |  _  /  " +
				"\n         ___| | | |/   |/ /    | |___  | |___  | |     | |___  | | \\ \\ " +
				"\n        /_____/ |___/|___/     |_____| |_____| |_|     |_____| |_|  \\_\\  " +
				"\n\n" +
				"\n================================================================================" +
				"\n===========================[ CRAPPY GUI EDITION ]===============================" +
				"\n===========================[    BETA VERSION    ]===============================" +
				"\n===========================[     SALEM HILAL    ]===============================" +
				"\n================================================================================");
	}
	public static void printOut(String s){
		myTextArea.append(s);
	}
	/*
	 * This converts a given row/column letter into numbers and then, if pick=1, traverses at that coordinate. Otherwise,
	 * it places/removes a flag at that location. 
	 */
	public static void coordConverter(char r, char c, String pick) throws IOException{
		int row = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(Character.toUpperCase(r));
		int column = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(Character.toUpperCase(c));
		if(pick.equals("Open a space"))
			gridTraverser(row, column);
		else if(pick.equals("Place a flag"))
			placeFlag(row, column);
	}
	/*
	 * This method pauses the output for @timeInSeconds seconds.
	 */
	public static void doPause(double timeInSeconds){
		long t0, t1;
		t0=System.currentTimeMillis();
		t1=(long) (System.currentTimeMillis()+(timeInSeconds*1000));
		do{
			t0=System.currentTimeMillis();
		} 
		while (t0 < t1);
	}
	/*
	 * Somewhat resizes the window depending on the grid size.
	 */
	public static void resizeWindow(){
		int height = grid.length * 18;
		if(height<=410){
			height=410;
		}
		mainFrame.setBounds(new Rectangle(new Dimension(680, height+150)));		
		mainFrame.setLocationRelativeTo(null);
	}
	/*
	 * This prints out @prompt and gets a character, looping until a character has been entered.
	 */
	public static char getChar(String prompt, boolean row) throws IOException{
		String input;
		char c = 0;
		boolean stop;
		do{
			input = JOptionPane.showInputDialog(prompt);
			stop=true;
			try{
				if(input.length()>1){
					JOptionPane.showMessageDialog(null, "Only enter one letter, please.", "Try again", JOptionPane.WARNING_MESSAGE);
					stop = false;
				}
				else{
					c=input.charAt(0);
					if("ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(Character.toUpperCase(c))<0){
						JOptionPane.showMessageDialog(null, "That's not a valid character. Try again.", "Try again", JOptionPane.WARNING_MESSAGE);
						stop = false;
					}
					else if(row){
						if("ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(Character.toUpperCase(c))>=grid.length){
							JOptionPane.showMessageDialog(null, "That's out of range. Try again.", "Try again", JOptionPane.WARNING_MESSAGE);
							stop = false;
						}
					}
					else if(!row){
						if("ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(Character.toUpperCase(c))>=grid[0].length){
							JOptionPane.showMessageDialog(null, "That's out of range. Try again.", "Try again", JOptionPane.WARNING_MESSAGE);
							stop = false;
						}
					}
				}
			}
			catch(Throwable t){
				JOptionPane.showMessageDialog(null, "That's not a valid character. Try again.", "Try again", JOptionPane.WARNING_MESSAGE);
				stop = false;
			}
		}
		while(!stop);
		return c;
	}
	/*
	 * This gets an integer, catching wrong integers and looping until an acceptable integer is found. 
	 */
	public static int getInt(String prompt, int max) throws IOException{
		String input;
		int i=0;
		boolean stop;
		do{
			input = JOptionPane.showInputDialog(prompt);
			stop=true;
			try{
				i=Integer.parseInt(input);
				if(i<=1){
					JOptionPane.showMessageDialog(null, "That number is too small. Try again.", "Try again", JOptionPane.WARNING_MESSAGE);
					stop = false;
				}
				else if(i>max){
					JOptionPane.showMessageDialog(null, "That number is too large (Max = " + max + ").", "Try again", JOptionPane.WARNING_MESSAGE);
					stop = false;
				}
			}
			catch(Throwable t){
				JOptionPane.showMessageDialog(null, "That's not a valid number. Try again.", "Try again", JOptionPane.WARNING_MESSAGE);
				stop = false;
			}
		}
		while(!stop);
		return i;
	}
}


