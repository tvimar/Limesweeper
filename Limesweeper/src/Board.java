import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
	
	private JPanel _tilePanel;
	
	// Constructor
	
	public Board(){

		_painter = new Painter();
		int difficulty = promptDifficulty();
		initStats(difficulty);

		setPreferredSize(new Dimension(1000, 600));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		
		initBoard();

        setTitle("Limesweeper");
    }
	
	// Initialization functions
	
	private void initBoard() {
		//set up the board itself
		_tilePanel = setUpField();
		
		// Grab 10 random indexes to setMines;
		_rng = new Random();
		setBoardMines();
		
        add(_tilePanel);
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
							} else {
								_painter.paintButton(_gamefield[x][y], FLAG);
							}
						}
					}
					else {
						clickSquare(x, y);
					}	
				}
			}
		);
	}
	
	private int promptDifficulty() {
		//JOptionPane.showMessageDialog(this, "You win!");
		String [] options = {"Easy", "Medium", "Difficult"};
		String msg = "Select your difficulty";
		return JOptionPane.showOptionDialog(null, msg, "Difficulty Selection",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
	}
	
	private void initStats(int difficulty) {
		if(difficulty == EASY) {
			_boardWidth = 9;
			_boardHeight = 9;
			_numberOfMines = 10;
		} else if(difficulty == MEDIUM) {
			_boardWidth = 16;
			_boardHeight = 16;
			_numberOfMines = 40;
		} else if(difficulty == HARD) {
			_boardWidth = 30;
			_boardHeight = 16;
			_numberOfMines = 99;
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
		exposeMines();
		JOptionPane.showMessageDialog(this, "You lost :(");
		promptRetry();
	}
	
	private void gameWon() {
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
		remove(_tilePanel);
		initStats(difficulty);
		initBoard();
		setVisible(true);
	}
}
