package osbot.account.creator;

import org.openqa.selenium.WebDriver;

public class PidDriver {

	public PidDriver(WebDriver driver, int pidId) {
		this.driver = driver;
		this.pidId = pidId;
	}

	private WebDriver driver;
	
	private int pidId;
	
	private SeleniumType type;

	/**
	 * @return the driver
	 */
	public WebDriver getDriver() {
		return driver;
	}

	/**
	 * @param driver the driver to set
	 */
	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}

	/**
	 * @return the pidId
	 */
	public int getPidId() {
		return pidId;
	}

	/**
	 * @param pidId the pidId to set
	 */
	public void setPidId(int pidId) {
		this.pidId = pidId;
	}

	/**
	 * @return the type
	 */
	public SeleniumType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(SeleniumType type) {
		this.type = type;
	}
}
