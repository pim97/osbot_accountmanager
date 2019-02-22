package osbot.database;

public class DatabaseProxy {

	public DatabaseProxy(String proxyIp, String proxyPort, String proxyUsername, String proxyPassword) {
		this.proxyIp = proxyIp;
		this.proxyPort = proxyPort;
		this.setProxyUsername(proxyUsername);
		this.setProxyPassword(proxyPassword);
	}
	
	private boolean isOnline;

	private int usedCount;
	
	private String proxyIp;
	
	private String proxyPort, proxyUsername, proxyPassword;

	/**
	 * @return the proxyIp
	 */
	public String getProxyIp() {
		return proxyIp;
	}

	/**
	 * @param proxyIp the proxyIp to set
	 */
	public void setProxyIp(String proxyIp) {
		this.proxyIp = proxyIp;
	}

	/**
	 * @return the proxyPort
	 */
	public String getProxyPort() {
		return proxyPort;
	}

	/**
	 * @param proxyPort the proxyPort to set
	 */
	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	/**
	 * @return the proxyPassword
	 */
	public String getProxyPassword() {
		return proxyPassword;
	}

	/**
	 * @param proxyPassword the proxyPassword to set
	 */
	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	/**
	 * @return the proxyUsername
	 */
	public String getProxyUsername() {
		return proxyUsername;
	}

	/**
	 * @param proxyUsername the proxyUsername to set
	 */
	public void setProxyUsername(String proxyUsername) {
		this.proxyUsername = proxyUsername;
	}

	/**
	 * @return the usedCount
	 */
	public int getUsedCount() {
		return usedCount;
	}

	/**
	 * @param usedCount the usedCount to set
	 */
	public void setUsedCount(int usedCount) {
		this.usedCount = usedCount;
	}

	/**
	 * @return the isOnline
	 */
	public boolean isOnline() {
		return isOnline;
	}

	/**
	 * @param isOnline the isOnline to set
	 */
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
}
