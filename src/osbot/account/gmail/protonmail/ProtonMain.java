package osbot.account.gmail.protonmail;

import org.openqa.selenium.WebDriver;

import osbot.settings.OsbotController;

public class ProtonMain {

	private ProtonActions actions;
	
	private OsbotController account;
	
	/**
	 * 
	 * @param driver
	 */
	public ProtonMain(WebDriver driver, OsbotController account) {
		this.setDriver(driver);
		this.setAccount(account);
		this.setActions(new ProtonActions(driver, account));
	}
	
	/**
	 * Runs the main webbrowser
	 */
	public void verifyAccount() {
		if (getActions().login(ProtonConfig.PROTON_MAIL_USERNAME, ProtonConfig.PROTON_MAIL_PASSWORD)) {
			System.out.println("Successfully logged in");
			
			while (!getActions().clickMail("Welcome to RuneScape!") && getActions().clickedCorrectEmail()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("Waiting for verification email...");
			}
			System.out.println("Found e-mail & clicked link in email and was verified correctly!");
			
			while (!getActions().clickLink("https://secure.runescape.com/m=email-register/submit_code.ws")) {
				System.out.println("Waiting to click on the verification link");
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("Succesfully clicked on the verification link!");
		}
	}
	
	
	/**
	 * The webdriver
	 */
	private WebDriver driver;


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
	 * @return the actions
	 */
	public ProtonActions getActions() {
		return actions;
	}

	/**
	 * @param actions the actions to set
	 */
	public void setActions(ProtonActions actions) {
		this.actions = actions;
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
	
}
