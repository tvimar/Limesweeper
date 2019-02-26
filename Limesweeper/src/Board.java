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
import java.util.concurrent.CountDownLatch;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class Board extends JFrame {
	
	// Constants
	
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
	
	private CountDownLatch _latch;
	private JLabel _smiley;
	
	// Constructor
	
	public Board(Painter painter, int difficulty, CountDownLatch latch){

		_latch = latch;
		_painter = painter;
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
		topPanel.setMaximumSize(new Dimension(400, 60));
		topPanel.setLayout(new GridBagLayout());
		
		_minecount = makeDisplay(String.valueOf(_minesleft));
		_timedisplay = makeDisplay("0");
		_smiley = new JLabel(_painter.getHappyFace());
		_smiley.setPreferredSize(new Dimension(60, 60));
		
		topPanel.add(_minecount);
		topPanel.add(_smiley);
		topPanel.add(_timedisplay);
		return topPanel;
	}
	
	private JTextField makeDisplay(String start) {
		JTextField newdisplay = new JTextField(start, 3);
		newdisplay.setEditable(false);
		newdisplay.setMaximumSize(new Dimension(60, 55));
		Font bigFont = newdisplay.getFont().deriveFont(Font.PLAIN, 32f);
		newdisplay.setFont(bigFont);
		newdisplay.setHorizontalAlignment(JTextField.RIGHT);
		return newdisplay;
	}
	
	private void setUpTimer() {
		_time = 0;
		ActionListener updateTime = new ActionListener() {
	      public void actionPerformed(ActionEvent evt) {
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
		_painter.paintButton(_gamefield[x][y], Enums.IconsEnum.UNCLICKED.getValue());
	}
	
	private void addTileListener(Tile t, int x, int y) {
		t.addMouseListener(
			new MouseAdapter(){
				@Override
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						if(_gamefield[x][y].wasChecked() == false) {
							if(_gamefield[x][y].getIcon() == _painter.getFlag()) {
								_painter.paintButton(_gamefield[x][y], Enums.IconsEnum.UNCLICKED.getValue());
								updateMineCount(1);
							} else {
								_painter.paintButton(_gamefield[x][y], Enums.IconsEnum.FLAG.getValue());
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
	
	private void initStats(int difficulty) {
		if(difficulty == Enums.DifficultyEnum.EASY.ordinal()) {
			_boardWidth = 9;
			_boardHeight = 9;
			_numberOfMines = 10;
			_boardDimension = new Dimension(300, 400);
		} else if(difficulty == Enums.DifficultyEnum.MEDIUM.ordinal()) {
			_boardWidth = 16;
			_boardHeight = 16;
			_numberOfMines = 40;
			_boardDimension = new Dimension(510, 600);
		} else if(difficulty == Enums.DifficultyEnum.HARD.ordinal()) {
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
				if(_gamefield[x][y].hasMine()) _painter.paintButton(_gamefield[x][y], Enums.IconsEnum.MINE.getValue());
			}
		}
	}
	
	// End game functions
	
	private void gameOver() {
		_timer.stop();
		_smiley.setIcon(_painter.getLosingFace());
		exposeMines();
		JOptionPane.showMessageDialog(this, "You lost :(");
		endGame();
	}
	
	private void gameWon() {
		_timer.stop();
		_smiley.setIcon(_painter.getWinningFace());
		JOptionPane.showMessageDialog(this, "You win!");
		endGame();
	}
	
	private void endGame() {
		setVisible(false);
		_latch.countDown();
		dispose();
	}
}
