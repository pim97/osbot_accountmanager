package osbot.account.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import osbot.bot.BotController;

public class GeckoHandler {

	/**
	 * May open a new one?
	 * 
	 * @param maxSize
	 * @return
	 */
	public static boolean mayStartFirefoxBrowser(int maxSize) {
		if (getGeckodriverExeWindows().size() > 5 && getFirefoxExeWindows().size() > 25) {
			System.out.println("Already 5 browsers open, must wait before starting a new one");
			return false;
		}
		return true;
	}

	public static List<Integer> getGeckodriverExeWindows() {
		List<Integer> pids = new ArrayList<>();
		try {
			Process process = Runtime.getRuntime().exec("tasklist /FI \"IMAGENAME eq geckodriver.exe\" /NH");
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

	public static List<Integer> getFirefoxExeWindows() {
		List<Integer> pids = new ArrayList<>();
		try {
			Process process = Runtime.getRuntime().exec("tasklist /FI \"IMAGENAME eq firefox.exe\" /NH");
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

	// public static void runDriver() {
	// new Thread(() -> {
	// try {
	// List<Integer> pids = BotController.getJavaPIDsWindows();
	// Process p = Runtime.getRuntime().exec(getCliArgs().toString());
	// System.out.println("Waiting for the geckodriver to launch..");
	// p.waitFor();
	// System.out.println(getCliArgs().toString());
	// List<Integer> pidsAfter = BotController.getJavaPIDsWindows();
	// pidsAfter.removeAll(pids);
	//
	// if (pidsAfter.size() == 1) {
	// setPidId(pidsAfter.get(0));
	// System.out.println("Pid set to: " + pidsAfter.get(0));
	// }
	// setCliArgs(new StringBuilder());
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }).start();
	// }
	//
	public static void killAllGeckodrivers() {
		for (int pid : getGeckodriverExeWindows()) {
			BotController.killProcess(pid);
		}
	}

}
