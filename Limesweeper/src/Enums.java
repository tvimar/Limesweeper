public class Enums {
	public enum IconsEnum{
		MINE(9),
		UNCLICKED(10),
		FLAG(11),
		HAPPY(12),
		WINNING(13),
		LOST(14);
		
		private final int icon;
		
		private IconsEnum(int icon) {
			this.icon = icon;
		}
		
		public int getValue() {
			return icon;
		}
	}
	
	public enum DifficultyEnum{
		EASY, MEDIUM, HARD;
	}
}
