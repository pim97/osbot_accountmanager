import java.util.ArrayList;

public class Bots {

	/**
	 * 
	 */
	private static ArrayList<RunBot> bots = new ArrayList<RunBot>();

	/**
	 * 
	 * @param id
	 * @return
	 */
	public static RunBot getBotById(int id) {
		for (RunBot bot : bots) {
			if (bot.getId() == id) {
				return bot;
			}
		}
		return null;
	}
	
	/**
	 * @return the bots
	 */
	public static ArrayList<RunBot> getBots() {
		return bots;
	}

	/**
	 * @param bots the bots to set
	 */
	public static void setBots(ArrayList<RunBot> bots) {
		Bots.bots = bots;
	}
	
}
