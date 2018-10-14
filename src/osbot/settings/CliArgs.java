package osbot.settings;
public enum CliArgs {
	DEBUG("-debug"), MEM("-mem"), DATA("-data"), ALLOW("-allow"), PROXY("-proxy"), LOGIN("-login"), BOT("-bot"), SCRIPT(
			"-script"), WORLD("-world");

	private String key;

	private CliArgs(String key) {
		this.setKey(key);
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
}