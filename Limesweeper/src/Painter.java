import javax.swing.ImageIcon;
import javax.swing.JButton;

public class Painter {
	private ImageIcon [] icons;
	private int NUM_IMAGES = 15;
	
	private int FLAG = 11;
	private int HAPPY = 12;
	private int WINNING = 13;
	private int LOST = 14;
	
	public Painter() {
		icons = new ImageIcon [NUM_IMAGES];
		
		for (int i = 0; i < NUM_IMAGES; i++) {
            String path = "resources/" + i + ".png";
            ImageIcon original = new ImageIcon(this.getClass().getResource(path));
            ImageIcon resized = new ImageIcon(original.getImage().getScaledInstance( 30, 30,  java.awt.Image.SCALE_SMOOTH ));
            icons[i] = resized;
        }
	}
	
	public void paintButton(JButton button, int image_num) {
		button.setIcon(icons[image_num]);
	}
	
	public ImageIcon getFlag() {
		return icons[FLAG];
	}
	
	public void paintHappyFace(JButton button) {
		paintButton(button, HAPPY);
	}
	
	public void paintWinningFace(JButton button) {
		paintButton(button, WINNING);
	}
	
	public void paintLosingFace(JButton button) {
		paintButton(button, LOST);
	}
}
