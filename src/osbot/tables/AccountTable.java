package osbot.tables;

import osbot.account.AccountStage;
import osbot.account.AccountStatus;

public class AccountTable {

	/**
	 * Does the account have a username and password?
	 * 
	 * @return
	 */
	public boolean hasUsernameAndPassword() {
		return getUsername() != null && getPassword() != null && getUsername().length() > 0
				&& getPassword().length() > 0;
	}

	/**
	 * Does the account have a username, password & bank pin?
	 * 
	 * @return
	 */
	public boolean hasUsernameAndPasswordAndBankpin() {
		return getUsername() != null && getPassword() != null && getUsername().length() > 0
				&& getPassword().length() > 0 && getBankPin() != null && getBankPin().length() > 0;
	}

	/**
	 * Does the account have a valid proxy?
	 * 
	 * @return
	 */
	public boolean hasValidProxy() {
		return getProxyIp() != null && getProxyPort() != null && getProxyIp().length() > 0
				&& getProxyPort().length() > 0;
	}

	/**
	 * Does the account have a current script?
	 * 
	 * @return
	 */
	public boolean hasScript() {
		return getScript() != null && getScript().length() > 0;
	}

	public AccountTable(int id, String script, String username, int world, String proxyIp, String proxyPort,
			boolean lowCpuMode, AccountStatus status, AccountStage stage, int accountStageProgress) {
		this.id = id;
		this.script = script;
		this.username = username;
		this.world = world;
		this.proxyIp = proxyIp;
		this.proxyPort = proxyPort;
		this.lowCpuMode = lowCpuMode;
		this.status = status;
		this.stage = stage;
		this.accountStageProgress = accountStageProgress;
	}
	
	private AccountStage stage;

	private String script, username, password, proxyIp, proxyPort, bankPin, email;

	private int world, day, month, year, accountStageProgress;

	private AccountStatus status;

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

	public int getWorld() {
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

	public void setWorld(int world) {
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

	/**
	 * @return the status
	 */
	public AccountStatus getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(AccountStatus status) {
		this.status = status;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the bankPin
	 */
	public String getBankPin() {
		return bankPin;
	}

	/**
	 * @param bank
	 *            the bankPin to set
	 */
	public void setBankPin(String bank) {
		this.bankPin = bank;
	}

	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * @return the month
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * @param month the month to set
	 */
	public void setMonth(int month) {
		this.month = month;
	}

	/**
	 * @return the day
	 */
	public int getDay() {
		return day;
	}

	/**
	 * @param day the day to set
	 */
	public void setDay(int day) {
		this.day = day;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the stage
	 */
	public AccountStage getStage() {
		return stage;
	}

	/**
	 * @param stage the stage to set
	 */
	public void setStage(AccountStage stage) {
		this.stage = stage;
	}

	/**
	 * @return the accountStageProgress
	 */
	public int getAccountStageProgress() {
		return accountStageProgress;
	}

	/**
	 * @param accountStageProgress the accountStageProgress to set
	 */
	public void setAccountStageProgress(int accountStageProgress) {
		this.accountStageProgress = accountStageProgress;
	}

}
