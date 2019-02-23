import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class Board extends JFrame {
	
	// Constants
	
	private int MINE = 9;
	private int UNCLICKED = 10;
	private int FLAG = 11;
	
	private int EASY = 0;
	private int MEDIUM = 1;
	private int HARD = 2;
	
	// Board members
	
	private Tile[][] _gamefield;
	private int _minesleft;
	private int _squaresleft;

	private int _boardWidth;
	private int _boardHeight;
	private int _numberOfMines;
	private int _safeSquares;
	
	private Random _rng;
	private Painter _painter;
	
	private JPanel _tilePanel; // For tiles
	private JPanel _topPanel; // For mine counter/timer/smiley face
	private JPanel _boardPanel; // Put former two panels on this
	
	private JTextField _minecount;
	private Dimension _boardDimension;
	
	private Timer _timer;
	private int _time;
	private JTextField _timedisplay;
	
	// Constructor
	
	public Board(){

		_painter = new Painter();
		int difficulty = promptDifficulty();
		initStats(difficulty);
		
		initBoard();

        setTitle("Limesweeper");
    }
	
	// Initialization functions
	
	private void initBoard() {
		
		setPreferredSize(_boardDimension);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		_boardPanel = new JPanel();
		_boardPanel.setLayout(new BorderLayout());
		
		//set up the board itself
		_tilePanel = setUpField();
		
		//set up counter
		_topPanel = setUpTop();
		
		//set up timer
		setUpTimer();
		
		// Grab 10 random indexes to setMines;
		_rng = new Random();
		setBoardMines();
		
		_boardPanel.add(_tilePanel, BorderLayout.SOUTH);
		_boardPanel.add(_topPanel, BorderLayout.CENTER);
        add(_boardPanel);
	}
	
	private JPanel setUpField() {
		//set up the board itself
		JPanel tilePanel = new JPanel();
		tilePanel.setSize(new Dimension(30 * _boardWidth, 30 * _boardHeight));
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraint = new GridBagConstraints();
		
		tilePanel.setLayout(layout);		
		
        // Create the board of tiles and square values
		
		_gamefield = new Tile[_boardWidth][_boardHeight];
		for(int y = 0; y < _boardHeight; y++) {
			for(int x = 0; x < _boardWidth; x++) {
				addTile(tilePanel, constraint, x, y);
			}
		}
		return tilePanel;
	}
	
	private JPanel setUpTop() {
		JPanel topPanel = new JPanel();
		topPanel.setSize(new Dimension(400, 60));
		topPanel.setLayout(new BorderLayout());
		
		_minecount = new JTextField(String.valueOf(_minesleft), 3);
		_minecount.setEditable(false);
		_minecount.setSize(new Dimension(60, 60));
		Font bigFont = _minecount.getFont().deriveFont(Font.PLAIN, 32f);
		_minecount.setFont(bigFont);
		_minecount.setHorizontalAlignment(JTextField.RIGHT);
		
		_timedisplay = new JTextField("0", 3);
		_timedisplay.setEditable(false);
		_timedisplay.setSize(new Dimension(60, 60));
		Font bigFont2 = _timedisplay.getFont().deriveFont(Font.PLAIN, 32f);
		_timedisplay.setFont(bigFont2);
		_timedisplay.setHorizontalAlignment(JTextField.RIGHT);
		
		topPanel.add(_minecount, BorderLayout.WEST);
		topPanel.add(_timedisplay, BorderLayout.EAST);
		return topPanel;
	}
	
	private void setUpTimer() {
		_time = 0;
		ActionListener updateTime = new ActionListener() {
	      public void actionPerformed(ActionEvent evt) {
	          //...Perform a task...
	    	  _time++;
	    	  _timedisplay.setText(String.valueOf(_time));
	      }
		};
		_timer = new Timer(1000, updateTime);
	}
	
	private void addTile(JPanel tilePanel, GridBagConstraints constraint, int x, int y) {
		_gamefield[x][y] = new Tile();
		addTileListener(_gamefield[x][y], x, y);
		constraint.gridx = x;
		constraint.gridy = y;
		tilePanel.add(_gamefield[x][y], constraint);
		_painter.paintButton(_gamefield[x][y], UNCLICKED);
	}
	
	private void addTileListener(Tile t, int x, int y) {
		t.addMouseListener(
			new MouseAdapter(){
				@Override
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						if(_gamefield[x][y].wasChecked() == false) {
							if(_gamefield[x][y].getIcon() == _painter.getFlag()) {
								_painter.paintButton(_gamefield[x][y], UNCLICKED);
								updateMineCount(1);
							} else {
								_painter.paintButton(_gamefield[x][y], FLAG);
								updateMineCount(-1);
							}
						}
					}
					else {
						if(_gamefield[x][y].getIcon() != _painter.getFlag()) clickSquare(x, y);
					}	
				}
			}
		);
	}
	
	private int promptDifficulty() {
		String [] options = {"Easy", "Medium", "Difficult"};
		String msg = "Select your difficulty";
		String title = "Difficulty Selection";
		return JOptionPane.showOptionDialog(null, msg, title, JOptionPane.DEFAULT_OPTION, 
				JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
	}
	
	private void initStats(int difficulty) {
		if(difficulty == EASY) {
			_boardWidth = 9;
			_boardHeight = 9;
			_numberOfMines = 10;
			_boardDimension = new Dimension(300, 400);
		} else if(difficulty == MEDIUM) {
			_boardWidth = 16;
			_boardHeight = 16;
			_numberOfMines = 40;
			_boardDimension = new Dimension(510, 600);
		} else if(difficulty == HARD) {
			_boardWidth = 30;
			_boardHeight = 16;
			_numberOfMines = 99;
			_boardDimension = new Dimension(950, 600);
		}
		
		_safeSquares = (_boardWidth * _boardHeight) - _numberOfMines;
		_minesleft = _numberOfMines;
		_squaresleft = _safeSquares;
	}
	
	// Square clicking functions
	
	private void clickSquare(int x, int y) {
		// if first move
		if(_squaresleft == _safeSquares) {
			repositionMine(_gamefield[x][y]);
			_timer.start();
		} 
		if (_gamefield[x][y].wasChecked() == false) revealSquare(x, y);
	}
	
	private void revealSquare(int x, int y) {
		if(_gamefield[x][y].hasMine()) {
			gameOver();
		} else {
			int neighbourMines = checkForNeighbourMines(x, y);
			_gamefield[x][y].setChecked(true);
			_painter.paintButton(_gamefield[x][y], neighbourMines);
			if(neighbourMines == 0) {
				checkNeighbours(x, y);
			}
			_squaresleft--;
			if(_squaresleft == 0) {
				gameWon();
			}
		}
	}
	
	private void checkNeighbours(int x, int y) {
		for(int i = Math.max(0, x - 1); i < Math.min (x + 2, _boardWidth); i++) {
			for(int j = Math.max(0, y - 1); j < Math.min (y + 2, _boardHeight); j++) {
				if(!(i == x && j == y)) {
					clickSquare(i, j);
				}
			}
		}
	}
	
	private int checkForNeighbourMines(int x, int y) {
		int minecount = 0;
		
		for(int i = Math.max(0, x - 1); i < Math.min (x + 2, _boardWidth); i++) {
			for(int j = Math.max(0, y - 1); j < Math.min (y + 2, _boardHeight); j++) {
				if(!(i == x && j == y)) {
					if(_gamefield[i][j].hasMine() == true) {
						minecount++;
					}
				}
			}
		}
		return minecount;
	}
	
	private void updateMineCount(int x) {
		_minesleft += x;
		_minecount.setText(String.valueOf(_minesleft));
	}
	
	// Mine setting functions
	
	// Protection from first square being a mine
	private void repositionMine(Tile square) {
		if(square.hasMine() == true) {
			setRandomMine();
			square.setMine(false);
		}
	}
	
	private void setBoardMines() {
		assert _gamefield != null;
		
		for(int i = 0; i < _numberOfMines; i++) {
			setRandomMine();
		}
		//setPresetMines();
	}
	
	private void setRandomMine() {
		while(true) {
			int x = _rng.nextInt(_boardWidth - 1);
			int y = _rng.nextInt(_boardHeight - 1);
			if(_gamefield[x][y].hasMine() == false) {
				_gamefield[x][y].setMine(true);
				break;
			}
		}
	}
	
	private void setPresetMines() { // TEST FUNCTION
		for(int i = 0; i < 9; i++) {
			_gamefield[i][i].setMine(true);
		}
	}
	
	private void exposeMines() {
		for(int y = 0; y < _boardHeight; y++) {
			for(int x = 0; x < _boardWidth; x++) {
				if(_gamefield[x][y].hasMine()) _painter.paintButton(_gamefield[x][y], MINE);
			}
		}
	}
	
	// End game functions
	
	private void gameOver() {
		_timer.stop();
		exposeMines();
		JOptionPane.showMessageDialog(this, "You lost :(");
		promptRetry();
	}
	
	private void gameWon() {
		_timer.stop();
		JOptionPane.showMessageDialog(this, "You win!");
		promptRetry();
	}
	
	private void promptRetry() {
		int result = JOptionPane.showConfirmDialog (null, "Would you like to play again?","Retry",JOptionPane.YES_NO_OPTION);
		if (result == 0) {
			int newdifficulty = promptDifficulty();
			resetGame(newdifficulty);
		} else {
			System.exit(0);
		}
	}
	
	private void resetGame(int difficulty) {
		remove(_boardPanel);
		initStats(difficulty);
		initBoard();
		setVisible(true);
	}
}
