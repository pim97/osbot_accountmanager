package osbot.tables;

public class AccountTable {

	public AccountTable(String script, String username, String world, String proxyIp, String proxyPort,
			boolean lowCpuMode, int id) {
		this.script = script;
		this.username = username;
		this.world = world;
		this.proxyIp = proxyIp;
		this.proxyPort = proxyPort;
		this.lowCpuMode = lowCpuMode;
		this.id = id;
	}

	private String script, username, world, proxyIp, proxyPort;
	
	private boolean lowCpuMode;
	
	private int id;
	
	public int getId() {
		return id;
	}

	public String getScript() {
		return script;
	}

	public String getUsername() {
		return username;
	}

	public String getWorld() {
		return world;
	}

	public String getProxyIp() {
		return proxyIp;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public boolean isLowCpuMode() {
		return lowCpuMode;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	public void setProxyIp(String proxyIp) {
		this.proxyIp = proxyIp;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	public void setLowCpuMode(boolean lowCpuMode) {
		this.lowCpuMode = lowCpuMode;
	}
	
}
