import javax.swing.ImageIcon;
import javax.swing.JButton;

public class Painter {
	private ImageIcon [] icons;
	private int NUM_IMAGES = 15;
	
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
		return icons[Enums.IconsEnum.FLAG.getValue()];
	}
	
	public ImageIcon getHappyFace() {
		return icons[Enums.IconsEnum.HAPPY.getValue()];
	}
	
	public ImageIcon getWinningFace() {
		return icons[Enums.IconsEnum.WINNING.getValue()];
	}
	
	public ImageIcon getLosingFace() {
		return icons[Enums.IconsEnum.LOST.getValue()];
	}
}
