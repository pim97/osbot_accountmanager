package osbot.bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
	
	public static void killProcess(final int pid) {
        try {
            if (System.getProperty("os.name").startsWith("Windows")) {
                Runtime.getRuntime().exec("Taskkill /PID " + pid + " /F");
            } else {
                Runtime.getRuntime().exec("kill -9 " + pid);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
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
