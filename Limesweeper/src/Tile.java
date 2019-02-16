import java.awt.Dimension;

import javax.swing.JButton;

public class Tile extends JButton {
	private boolean _containsMine;
	private boolean _checked;
	
	public Tile() {
		_containsMine = false;
		_checked = false;
	}
	
	@Override
	public Dimension getPreferredSize() {
        return new Dimension(30, 30);
    }
	
	public boolean hasMine() {
		return _containsMine;
	}
	
	public boolean wasChecked() {
		return _checked;
	}
	
	public void setMine(boolean mine) {
		_containsMine = mine;
	}
	
	public void setChecked(boolean check) {
		_checked = check;
	}
}
