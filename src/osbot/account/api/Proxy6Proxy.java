package osbot.account.api;

public class Proxy6Proxy {

	private int id, version, active;

	private String ip, port, user, host, pass, type, country, date, date_end, descr;
	
	private long unixtime, unixtime_end;

	public int getId() {
		return id;
	}

	public int getVersion() {
		return version;
	}

	public Proxy6Proxy(int id) {
		this.id = id;
	}

	public int getActive() {
		return active;
	}

	public String getIp() {
		return ip;
	}

	public String getPort() {
		return port;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}

	public String getType() {
		return type;
	}

	public String getCountry() {
		return country;
	}

	public String getDate() {
		return date;
	}

	public String getDate_end() {
		return date_end;
	}

	public String getDescr() {
		return descr;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setDate_end(String date_end) {
		this.date_end = date_end;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the unixtime
	 */
	public long getUnixtime() {
		return unixtime;
	}

	/**
	 * @param unixtime the unixtime to set
	 */
	public void setUnixtime(long unixtime) {
		this.unixtime = unixtime;
	}

	/**
	 * @return the unixtime_end
	 */
	public long getUnixtime_end() {
		return unixtime_end;
	}

	/**
	 * @param unixtime_end the unixtime_end to set
	 */
	public void setUnixtime_end(long unixtime_end) {
		this.unixtime_end = unixtime_end;
	}
}
