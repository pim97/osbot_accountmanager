package osbot.account.creator;

public class AccountCreate {

	
	private long time;
	
	public long getTime() {
		return time;
	}

	public AccountCreate(long time, String username) {
		this.time = time;
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	private String username;
}
