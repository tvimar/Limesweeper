import java.util.concurrent.CountDownLatch;

public class BoardFactory {
	Painter _p;
	
	public BoardFactory(Painter p) {
		_p = p;
	}
	
	public Board makeBoard(int difficulty, CountDownLatch latch) {
		Board b = new Board(_p, difficulty, latch);
		return b;
	}
}
