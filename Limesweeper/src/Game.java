import java.util.concurrent.CountDownLatch;

import javax.swing.JOptionPane;

public class Game {
	int _retry;
	Painter _p;
	Board _board;
	BoardFactory _factory;
	
	public Game(){
		_retry = 0;
		_p = new Painter();
		_factory = new BoardFactory(_p);
	}
	
	public void gameStart() {
		while(_retry == 0) {
			
			// make board + countdown latch (the latter to wait for JFrame to close before opening retry dialogue)
	        CountDownLatch boardSignal = new CountDownLatch(1);
			int difficulty = promptDifficulty();
			_board = new Board(_p, difficulty, boardSignal);
	        _board.setVisible(true);
	        _board.setResizable(false);
	        try {
				boardSignal.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			_retry = promptRetry();
		}
	}
	
	private int promptDifficulty() {
		String [] options = {"Easy", "Medium", "Difficult"};
		String msg = "Select your difficulty";
		String title = "Difficulty Selection";
		return JOptionPane.showOptionDialog(null, msg, title, JOptionPane.DEFAULT_OPTION, 
				JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
	}
	
	private int promptRetry() {
		return JOptionPane.showConfirmDialog (null, "Would you like to play again?","Retry",JOptionPane.YES_NO_OPTION);

	}
}
