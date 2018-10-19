package osbot.database;

public class DatabaseProxy {

	public DatabaseProxy(String proxyIp, String proxyPort) {
		this.proxyIp = proxyIp;
		this.proxyPort = proxyPort;
	}

	private String proxyIp;
	
	private String proxyPort;

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
}
