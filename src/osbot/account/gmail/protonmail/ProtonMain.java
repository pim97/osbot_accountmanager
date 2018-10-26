package osbot.account.gmail.protonmail;

import org.openqa.selenium.WebDriver;

import osbot.account.runescape.website.RunescapeActions;
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

	public void unlockAccount() {
		if (getActions().login(ProtonConfig.PROTON_MAIL_USERNAME, ProtonConfig.PROTON_MAIL_PASSWORD)) {
			System.out.println("Successfully logged in");

			boolean found = false;

			while (!found) {
				if (getActions().clickMail("Reset your Jagex password")) {
					if (!getActions().clickedCorrectEmail()) {
						getActions().deleteEmail();
						System.out.println("Email has not been found");
					} else if (getActions().clickedCorrectEmail()) {
						found = true;
						System.out.println("Email has been found");
					}
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("Got the e-mail");

			while (!getActions().clickLink("https://secure.runescape.com/m=accountappeal/enter_security_code.ws")) {
				System.out.println("Waiting to click on the verification link");

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("Succesfully clicked on the verification link!");
			
			while (!fillInAllInformation()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Trying to fill in..");
			}
			driver.quit();
			System.out.println("Successfully recovered account!");
		}
	}
	
	/**
	 * Recovering password
	 * @return
	 */
	public boolean fillInAllInformation() {
		RunescapeActions runescapeWebsite = new RunescapeActions(driver, account, null);
		
		return runescapeWebsite.fillInInformationRecovering();
	}

	/**
	 * Runs the main webbrowser
	 */
	public void verifyAccount() {
		if (getActions().login(ProtonConfig.PROTON_MAIL_USERNAME, ProtonConfig.PROTON_MAIL_PASSWORD)) {
			System.out.println("Successfully logged in");
			boolean found = false;

			while (!found) {
				if (getActions().clickMail("Welcome to RuneScape!")) {
					if (!getActions().clickedCorrectEmail()) {
						getActions().deleteEmail();
						System.out.println("Email has not been found");
					} else if (getActions().clickedCorrectEmail()) {
						found = true;
						System.out.println("Email has been found");
					}
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("Got the e-mail");

			while (!getActions().clickLink("https://secure.runescape.com/m=email-register/submit_code.ws")) {
				System.out.println("Waiting to click on the verification link");

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			try {
				Thread.sleep(12500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			driver.quit();
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
	 * @param driver
	 *            the driver to set
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
	 * @param actions
	 *            the actions to set
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
	 * @param account
	 *            the account to set
	 */
	public void setAccount(OsbotController account) {
		this.account = account;
	}

}
