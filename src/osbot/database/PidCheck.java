package osbot.database;

public class PidCheck {

	public PidCheck(int pid) {
		this.pid = pid;
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

	private int pid, matches;
}
