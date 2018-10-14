package osbot.bot;

import java.util.ArrayList;

import osbot.settings.OsbotController;

public class BotController {

	/**
	 * 
	 */
	private static ArrayList<OsbotController> bots = new ArrayList<OsbotController>();

	/**
	 * 
	 * @return
	 */
	public static boolean addBot(OsbotController controller) {
		return bots.add(controller);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public static OsbotController getBotById(int id) {
		for (OsbotController bot : bots) {
			if (bot.getId() == id) {
				return bot;
			}
		}
		return null;
	}
	
	/**
	 * @return the bots
	 */
	public static ArrayList<OsbotController> getBots() {
		return bots;
	}

	/**
	 * @param bots the bots to set
	 */
	public static void setBots(ArrayList<OsbotController> bots) {
		BotController.bots = bots;
	}
	
}
