package osbot.database;

public class PidCheck {

	public PidCheck(int pid, long startTime) {
		this.pid = pid;
		this.startTime = startTime;
	}

	public int getPid() {
		return pid;
	}

	public int getMatches() {
		return matches;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public void setMatches(int matches) {
		this.matches = matches;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	private int pid, matches;

	private long startTime;
}
