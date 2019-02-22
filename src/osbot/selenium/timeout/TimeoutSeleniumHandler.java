package osbot.selenium.timeout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import osbot.account.handler.GeckoHandler;
import osbot.database.PidCheck;

public class TimeoutSeleniumHandler {

	/**
	 * Gecko pids
	 */
	private ArrayList<PidCheck> geckoPids = new ArrayList<PidCheck>();

	/**
	 * Firefox pids
	 */
	private ArrayList<PidCheck> firefoxPids = new ArrayList<PidCheck>();

	/**
	 * Contains in the list or not?
	 * 
	 * @param list
	 * @param pid
	 * @return
	 */
	private boolean containsInGeckoPid(ArrayList<PidCheck> list, int pid) {
		for (PidCheck o : list) {
			if (o.getPid() == pid) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Contains in the live list or not?
	 * 
	 * @param liveList
	 * @param inNotLiveListPid
	 * @return
	 */
	private boolean containsInLiveList(List<Integer> liveList, int inNotLiveListPid) {
		for (int pid : liveList) {
			if (inNotLiveListPid == pid) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Kills a process
	 * 
	 * @param pidId
	 */
	private void killProcess(int pidId) {
		// removed /T
		String cmd = "taskkill /F /PID " + pidId;
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(cmd);
	}

	/**
	 * Returns a pid object from a pid id
	 * 
	 * @param list
	 * @param pid
	 * @return
	 */
	private PidCheck getPidCheckObjectFromPid(ArrayList<PidCheck> list, int pid) {
		for (PidCheck pid2 : list) {
			if (pid2.getPid() == pid) {
				return pid2;
			}
		}
		return null;
	}

	/**
	 * Removes from the list
	 * 
	 * @param list
	 * @param o
	 */
	private void removeGeckoPid(ArrayList<PidCheck> list, PidCheck o) {
		PidCheck pidObject = getPidCheckObjectFromPid(list, o.getPid());
		// int index = list.indexOf(o);
		if (pidObject != null) {
			System.out.println("Removed " + o.getPid() + " from pid list");
			list.remove(pidObject);
		} else {
			System.out.println("Couldn't kill, didnt find index in list");
		}
		killProcess(o.getPid());
	}

	/**
	 * Handle both pids
	 */
	public void handleGeckoPids() {
		handlePids(GeckoHandler.getGeckodriverExeWindows(), geckoPids);
		handlePids(GeckoHandler.getFirefoxExeWindows(), firefoxPids);
		printOutTimes(geckoPids);
		// printOutTimes(firefoxPids);
	}

	private void printOutTimes(ArrayList<PidCheck> list) {
		for (PidCheck l : list) {
			System.out.println(System.currentTimeMillis() - l.getStartTime() + " " + l.getPid());
		}
	}

	private void handlePids(List<Integer> pids, ArrayList<PidCheck> list) {
		// When there's currently not the pid in the list
		for (int pid : pids) {
			if (!containsInGeckoPid(list, pid)) {
				list.add(new PidCheck(pid, System.currentTimeMillis()));
				System.out.println("Registered new pid: " + pid);
			}
		}

		// Pids to remove
		ArrayList<PidCheck> removePidsFromList = new ArrayList<PidCheck>();

		for (PidCheck l : list) {
			// When pid isn't live anymore
			if (!containsInLiveList(pids, l.getPid())) {
				removePidsFromList.add(l);
			}

			// Too long open
			if ((System.currentTimeMillis() - l.getStartTime()) > 1_200_000) {
				removePidsFromList.add(l);
			}

		}

		// Remove the pids when it isn't live anymore
		for (PidCheck remove : removePidsFromList) {
			removeGeckoPid(list, remove);
		}
	}

}
