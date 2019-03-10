package osbot.bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import osbot.settings.OsbotController;

public class BotController {

	public static List<Integer> getJavaPIDsWindows() {
		List<Integer> pids = new ArrayList<>();
		try {
			Process process = Runtime.getRuntime().exec("tasklist /FI \"IMAGENAME eq java.exe\" /NH");
			try (final InputStream stdout = process.getInputStream();
					final InputStreamReader inputStreamReader = new InputStreamReader(stdout);
					final BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
				String processInfo;
				while ((processInfo = bufferedReader.readLine()) != null) {
					processInfo = processInfo.trim();
					String[] values = processInfo.split("\\s+");
					if (!processInfo.contains("No") && values.length >= 2) {
						pids.add(Integer.parseInt(values[1]));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pids;
	}

	/**
	 * Does the pid contains in the list or not?
	 * 
	 * @param id
	 * @return
	 */
	public static boolean containsInPidList(int id) {
		if (id <= 0) {
			return false;
		}
		for (int pid : getJavaPIDsWindows()) {
			if (pid == id) {
				return true;
			}
		}
		return false;
	}

	public static OsbotController getBotByAccountName(String accountName) {
		for (OsbotController bot : getBots()) {
			if (bot.getAccount().getUsername().equalsIgnoreCase(accountName)) {
				return bot;
			}
		}
		return null;
	}

	public static OsbotController getBotByPid(int id) {
		for (OsbotController bot : getBots()) {
			if (bot.getPidId() == id) {
				return bot;
			}
		}
		return null;
	}

	public static void killProcess(final int pid) {
		new Thread(() -> {
			try {
				Runtime.getRuntime().exec("Taskkill /PID " + pid + " /F");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
		// return (abc.exitValue() == 0);
	}

	/**
	 * 
	 */
	private static List<OsbotController> bots = new CopyOnWriteArrayList<OsbotController>();

	/**
	 * 
	 * @return
	 */
	public static boolean addBot(OsbotController controller) {
		return getBots().add(controller);
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
	public static synchronized List<OsbotController> getBots() {
		return bots;
	}

	/**
	 * @param bots
	 *            the bots to set
	 */
	public static void setBots(ArrayList<OsbotController> bots) {
		BotController.bots = bots;
	}

}
