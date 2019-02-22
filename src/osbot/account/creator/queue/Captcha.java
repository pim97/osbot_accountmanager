package osbot.account.creator.queue;

import osbot.account.creator.SeleniumType;
import osbot.settings.OsbotController;

public class Captcha {

	public Captcha(String apiKey, String websiteKey, SeleniumType type) {
		this.apiKey = apiKey;
		this.websiteKey = websiteKey;
		this.type = type;
	}
	
	private int tries, triesCaptcha;

	private OsbotController account;
	
	private String apiKey, websiteKey, resultKey = null;

	private boolean working = false;

	private SeleniumType type;
	
	private boolean successfullyUsed = false;
	
	private boolean opened = false;
	
	private long solvedTime = -1;
	
	private int captchaAgainTry;

	public String getApiKey() {
		return apiKey;
	}

	public String getWebsiteKey() {
		return websiteKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setWebsiteKey(String websiteKey) {
		this.websiteKey = websiteKey;
	}

	/**
	 * @return the working
	 */
	public boolean isWorking() {
		return working;
	}

	/**
	 * @param working
	 *            the working to set
	 */
	public void setWorking(boolean working) {
		this.working = working;
	}

	/**
	 * @return the resultKey
	 */
	public String getResultKey() {
		return resultKey;
	}

	/**
	 * @param resultKey
	 *            the resultKey to set
	 */
	public void setResultKey(String resultKey) {
		this.resultKey = resultKey;
	}

	/**
	 * @return the type
	 */
	public SeleniumType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(SeleniumType type) {
		this.type = type;
	}

	/**
	 * @return the account
	 */
	public OsbotController getAccount() {
		return account;
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(OsbotController account) {
		this.account = account;
	}

	/**
	 * @return the solvedTime
	 */
	public long getSolvedTime() {
		return solvedTime;
	}

	/**
	 * @param solvedTime the solvedTime to set
	 */
	public void setSolvedTime(long solvedTime) {
		this.solvedTime = solvedTime;
	}

	/**
	 * @return the tries
	 */
	public int getTries() {
		return tries;
	}

	/**
	 * @param tries the tries to set
	 */
	public void setTries(int tries) {
		this.tries = tries;
	}

	/**
	 * @return the opened
	 */
	public boolean isOpened() {
		return opened;
	}

	/**
	 * @param opened the opened to set
	 */
	public void setOpened(boolean opened) {
		this.opened = opened;
	}

	/**
	 * @return the triesCaptcha
	 */
	public int getTriesCaptcha() {
		return triesCaptcha;
	}

	/**
	 * @param triesCaptcha the triesCaptcha to set
	 */
	public void setTriesCaptcha(int triesCaptcha) {
		this.triesCaptcha = triesCaptcha;
	}

	/**
	 * @return the successfullyUsed
	 */
	public boolean isSuccessfullyUsed() {
		return successfullyUsed;
	}

	/**
	 * @param successfullyUsed the successfullyUsed to set
	 */
	public void setSuccessfullyUsed(boolean successfullyUsed) {
		this.successfullyUsed = successfullyUsed;
	}

	/**
	 * @return the captchaAgainTry
	 */
	public int getCaptchaAgainTry() {
		return captchaAgainTry;
	}

	/**
	 * @param captchaAgainTry the captchaAgainTry to set
	 */
	public void setCaptchaAgainTry(int captchaAgainTry) {
		this.captchaAgainTry = captchaAgainTry;
	}

}
