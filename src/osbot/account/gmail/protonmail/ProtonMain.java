package osbot.account.gmail.protonmail;

import java.util.ArrayList;

import org.openqa.selenium.WebDriver;

import osbot.account.creator.AccountCreationService;
import osbot.account.creator.PidDriver;
import osbot.account.creator.SeleniumType;
import osbot.account.runescape.website.RunescapeActions;
import osbot.account.webdriver.WebdriverFunctions;
import osbot.database.DatabaseUtilities;
import osbot.settings.OsbotController;

public class ProtonMain {

	private ProtonActions actions;

	private OsbotController account;

	private PidDriver pidDriver;

	/**
	 * 
	 * @param driver
	 */
	public ProtonMain(WebDriver driver, OsbotController account, PidDriver pidDriver) {
		this.setDriver(driver);
		this.setAccount(account);
		this.setActions(new ProtonActions(driver, account));
		this.setPidDriver(pidDriver);
	}

	public void unlockAccount() {
		if (getActions().login(ProtonConfig.PROTON_MAIL_USERNAME, ProtonConfig.PROTON_MAIL_PASSWORD)) {
			System.out.println("Successfully logged in");

			boolean found = false;
			int cantFindEmail = 0;
			// boolean friday = false;
			// int fridayTries = 0;

			while (!found) {

				// if (!friday && getActions().blackFridayDeals()) {
				// System.out.println("Solving this black fridays thing");
				// friday = true;
				// }
				// if (fridayTries > 10) {
				// System.out.println("Couldn't find friday, requesting to not click it again");
				// friday = true;
				// }
				// fridayTries++;

				if (getActions().clickMail("Reset your Jagex password")) {
					if (!getActions().clickedCorrectEmail()) {
						// getActions().deleteEmail();
						System.out.println("Email has not been found, clicking on the next one");
						cantFindEmail++;
					} else if (getActions().clickedCorrectEmail()) {
						found = true;
						System.out.println("Email has been found");
					}
				}

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (cantFindEmail > 20) {
					System.out.println("Had 20 tries of finding the e-mail, but couldn't find, restarting the driver");
					driver.quit();
					break;
				}

				if (WebdriverFunctions.hasQuit(driver)) {
					driver.quit();
					System.out.println("Breaking out of loop");
					break;
				}
			}
			System.out.println("Got the e-mail");

			while (!getActions().clickLink("https://secure.runescape.com/m=accountappeal/enter_security_code.ws")) {
				if (WebdriverFunctions.hasQuit(driver)) {
					driver.quit();
					System.out.println("Breaking out of loop");
					break;
				}

				System.out.println("Waiting to click on the verification link");

				// try {
				// Thread.sleep(50);
				// } catch (InterruptedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
			}
			System.out.println("Succesfully clicked on the verification link!");

			while (!fillInAllInformation()) {
				if (WebdriverFunctions.hasQuit(driver)) {
					driver.quit();
					System.out.println("Breaking out of loop");
					break;
				}

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Trying to fill in..");
			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// AccountCreationService.checkPreviousProcessesAndDie(getPidDriver().getType());
			driver.quit();
			System.out.println("Successfully recovered account!");
		}
	}

	/**
	 * Recovering password
	 * 
	 * @return
	 */
	public boolean fillInAllInformation() {
		RunescapeActions runescapeWebsite = new RunescapeActions(driver, account, null, null);

		return runescapeWebsite.fillInInformationRecovering();
	}

	/**
	 * Runs the main webbrowser
	 */
	public void verifyAccount() {
		if (getActions().login(ProtonConfig.PROTON_MAIL_USERNAME, ProtonConfig.PROTON_MAIL_PASSWORD)) {
			if (getAccount().getAccount().getEmail() == null) {
				System.out.println("E-mail was not set correctly, quiting!");
				driver.quit();
				return;
			}
			System.out.println("Successfully logged in");
			boolean found = false;
			int cantFindEmail = 0;
			// boolean friday = false;
			// int fridayTries = 0;

			while (!found) {

				// if (!friday && getActions().blackFridayDeals()) {
				// System.out.println("Solving this black fridays thing");
				// friday = true;
				// }
				// if (fridayTries > 10) {
				// System.out.println("Couldn't find friday, requesting to not click it again");
				// friday = true;
				// }
				// fridayTries++;

				if (getActions().clickMail("Welcome to RuneScape!")) {
					if (!getActions().clickedCorrectEmail()) {
						// getActions().deleteEmail();
						System.out.println("Email has not been found");
						cantFindEmail++;
					} else if (getActions().clickedCorrectEmail()) {
						found = true;
						System.out.println("Email has been found");
					}
				}

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (cantFindEmail > 20) {
					System.out.println("Had 20 tries of finding the e-mail, but couldn't find, restarting the driver");
					driver.quit();
					// AccountCreationService.checkPreviousProcessesAndDie(getPidDriver().getType());
					break;
				}

				if (WebdriverFunctions.hasQuit(driver)) {
					driver.quit();
					System.out.println("Breaking out of loop");
					break;
				}

			}
			System.out.println("Got the e-mail");

			while (!getActions().clickLink("https://secure.runescape.com/m=email-register/submit_code.ws")) {
				if (WebdriverFunctions.hasQuit(driver)) {
					// AccountCreationService.checkPreviousProcessesAndDie(getPidDriver().getType());
					driver.quit();
					System.out.println("Breaking out of loop");
					break;
				}

				System.out.println("Waiting to click on the verification link");

				// try {
				// Thread.sleep(50);
				// } catch (InterruptedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
			}

			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// AccountCreationService.checkPreviousProcessesAndDie(getPidDriver().getType());
			DatabaseUtilities.insertIntoTable(account.getAccount());
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

	/**
	 * @return the pidDriver
	 */
	public PidDriver getPidDriver() {
		return pidDriver;
	}

	/**
	 * @param pidDriver
	 *            the pidDriver to set
	 */
	public void setPidDriver(PidDriver pidDriver) {
		this.pidDriver = pidDriver;
	}

}
