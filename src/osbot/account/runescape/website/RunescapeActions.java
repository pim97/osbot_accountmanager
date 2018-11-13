package osbot.account.runescape.website;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import osbot.account.AccountStage;
import osbot.account.AccountStatus;
import osbot.account.creator.HttpRequests;
import osbot.account.creator.PidDriver;
import osbot.account.creator.RandomNameGenerator;
import osbot.account.creator.SeleniumType;
import osbot.account.creator.queue.Captcha;
import osbot.account.global.Config;
import osbot.account.webdriver.WebdriverFunctions;
import osbot.database.DatabaseUtilities;
import osbot.random.RandomUtil;
import osbot.settings.OsbotController;

public class RunescapeActions {

	/**
	 * 
	 * @param driver
	 * @param account
	 */
	public RunescapeActions(WebDriver driver, OsbotController account, SeleniumType type, PidDriver pidDriver,
			Captcha captcha) {
		setAccount(account);
		setDriver(driver);
		setType(type);
		setPidDriver(pidDriver);
		setCaptcha(captcha);
	}

	private PidDriver pidDriver;

	private WebDriver driver;

	private OsbotController account;

	private SeleniumType type;

	private Captcha captcha;

	/**
	 * Creates the account
	 */
	public boolean create() {
		if (createAccount()) {
			// Inserting into the database, because successfull
			DatabaseUtilities.insertIntoTable(account.getAccount());
			return true;
		}
		System.out.println("Failed to create the account");
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public boolean fillInInformationRecovering() {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 120);
			wait.until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver driver) {
					if (driver.getWindowHandles().size() > 1) {
						return true;
					}
					return false;
				}
			});

			for (String winHandle : driver.getWindowHandles()) {
				System.out.println("Switched to: " + winHandle);
				driver.switchTo().window(winHandle);
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			WebdriverFunctions.waitForLoad(driver);
			// WebdriverFunctions.waitForElementToBeVisible(driver,
			// driver.findElement(By.name("password")));

			if (fillInNewPassword()) {
				if (!clickButtonAndVerifyLink(By.name("submit"), "enter_security_code")) {// account_created
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			// driver.close();
			// driver.quit();
			return false;
		}
	}

	/**
	 * Unlocks a locked account
	 * 
	 * @return
	 */
	public boolean unlock() {
		try {

			while (!goToRunescapeRecoverAccount()) {
				if (WebdriverFunctions.hasQuit(driver)) {
					System.out.println("Breaking out of loop");
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Waiting on navigating to the Runescape recover page");
			}
			System.out.println("On the runescape account");

			if (driver.getPageSource().contains("You have been temporarily blocked from using this service.")
					&& driver.getCurrentUrl().contains("message.ws")) {// message.ws/?message=5
				if (captcha.getAccount() != null && captcha.getAccount().getAccount() != null
						&& captcha.getAccount().getAccount().getProxyIp() != null) {
					DatabaseUtilities.updateStatusOfAccountByIp(AccountStatus.LOCKED_TIMEOUT,
							captcha.getAccount().getAccount().getProxyIp());
				}
				// HttpRequests.updateAccountStatusInDatabase("LOCKED_TIMEOUT",
				// getAccount().getAccount().getEmail());
				System.out.println("Account couldn't be recovered this way");

				// True so it doesnt add it again
				captcha.setSuccessfullyUsed(true);
				driver.close();
				driver.quit();
				return false;
			}

			getResponseToken("https://secure.runescape.com/m=accountappeal/passwordrecovery");

			if (fillInRecoverInformation()) {
				System.out.println("Filled in all recover details");
			} else {
				unlock();
				// restarting
			}

			while (!hasCaptchaCompleted()) {
				System.out.println("Waiting on the completion of the captcha");

				if (WebdriverFunctions.hasQuit(driver)) {
					System.out.println("Breaking out of loop");
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			getCaptcha().setSuccessfullyUsed(true);
			Config.QUEUE.removeFromQueue(captcha);
			System.out.println("Captcha has completed!");

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			setFailedTries(0);
			while (!clickButtonAndVerifyLink(By.id("passwordRecovery"), "email-confirmation")) {// account-identified
				if (WebdriverFunctions.hasQuit(driver)) {
					System.out.println("Breaking out of loop");
					break;
				}
				
				System.out.println("Verifying the unlock button!");
				setFailedTries(getFailedTries() + 1);

				if (getFailedTries() > 1) {
					driver.close();
					driver.quit();
					System.out.println("Restarting..");
					return false;
				}

				System.out.println("Three");

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// while (!clickButtonAndVerifyLink(By.name("continueYes"),
			// "enter_security_code")) {// account_created
			// System.out.println("Verifying the final unlock button!");
			// setFailedTries(getFailedTries() + 1);
			// //
			// if (getFailedTries() > 1) {
			// driver.quit();
			// System.out.println("Restarting..");
			// return false;
			// }
			//
			// try {
			// Thread.sleep(1000);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }

			if (waitUnCaptchaFailed(getType())) {
				driver.close();
				driver.quit();
				System.out.println("Captcha failed, retrying with new driver");
				return false;
			}

			System.out.println("Successully verified account");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Got error - restarting now");
			// driver.close();
			// driver.quit();
			return false;
		}
	}

	private boolean accountUnkowninglyFailedRecover() {
		System.out.println(driver.getPageSource());
		if (driver.getPageSource().contains("Due to your account status, you must")) {
			HttpRequests.updateAccountStatusInDatabase("LOCKED_INGAME", getAccount().getAccount().getEmail());
			System.out.println("Account couldn't be recovered this way");

			// True so it doesnt add it again
			captcha.setSuccessfullyUsed(true);

//			driver.close();
//			driver.quit();
			return true;
		}
		return false;
	}

	/**
	 * 
	 */
	private int failedTries = 0;

	/**
	 * Creates an account on Runescape
	 * 
	 * @return
	 */
	private boolean createAccount() {
		try {
			while (!goToRunescapeCreateAccount()) {
				if (WebdriverFunctions.hasQuit(driver)) {
					System.out.println("Breaking out of loop");
					break;
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Waiting on navigating to the Runescape account");
			}
			System.out.println("On the runescape website!");

			getResponseToken("https://secure.runescape.com/m=account-creation/create_account");

			// Filling in all the information
			if (fillInInformation()) {
				System.out.println("Successfully filled in all information!");
			}

			// else {
			// createAccount();
			// // Going to the website again -- restarting
			// }

			while (!hasCaptchaCompleted()) {
				if (WebdriverFunctions.hasQuit(driver)) {
					System.out.println("Breaking out of loop");
					break;
				}
				
				System.out.println("Waiting on the completion of the captcha");

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			getCaptcha().setSuccessfullyUsed(true);
			Config.QUEUE.removeFromQueue(captcha);

			System.out.println("Captcha has completed!");

			System.out.println("Waiting 10 seconds to verify inputs");
			Thread.sleep(10000);

			while (!clickButtonAndVerifyLink(By.id("create-submit"), "account_created")) {// account_created
				if (WebdriverFunctions.hasQuit(driver)) {
					System.out.println("Breaking out of loop");
					break;
				}
				
				System.out.println("Verifying the clicking button!");
				setFailedTries(getFailedTries() + 1);

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (waitUnCaptchaFailed(getType())) {
				driver.close();
				driver.quit();
				System.out.println("Captcha failed, retrying with new driver");
				return false;
			}

			System.out.println("Successfully clicked & account created!");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error - restarting");
//			driver.close();
//			driver.quit();
			return false;
		}
	}

	/**
	 * 
	 * @return
	 */
	private boolean waitUnCaptchaFailed(SeleniumType type) {
		try {
			return (new WebDriverWait(driver, 15)).until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver d) {
					return captchaFailed(type);
				}
			});
		} catch (Exception e) {
			// Ignorning stacktrace

			// True so it doesnt add it again
			captcha.setSuccessfullyUsed(true);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * has the captcha successfully been solved or not?
	 * 
	 * @return
	 */
	private boolean captchaFailed(SeleniumType type) {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (type == SeleniumType.CREATE_VERIFY_ACCOUNT) {
			return (new WebDriverWait(driver, 20)).until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver d) {
					return driver.getPageSource().contains("You can now begin your adventure with your new account.")
							? false
							: true;
				}
			});
			// if (driver.getPageSource().contains("You can now begin your adventure with
			// your new account.")) {
			// return false;
			// }
		} else if (type == SeleniumType.RECOVER_ACCOUNT) {
			// WebdriverFunctions.waitForElementToBeVisible(driver,
			// driver.findElement(By.name("continueYes")));
			return (new WebDriverWait(driver, 20)).until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver d) {
					return driver.getPageSource().contains("A link to reset your password has been sent to") ? false
							: true;
				}
			});
			// if (driver.getPageSource().contains("A link to reset your password has been
			// sent to")) {
			// return false;
			// }
		}

		return true;
	}

	/**
	 * Getting the captcha response token
	 */
	private void getResponseToken(String link) {

		// new Thread(() -> {
		// String responseToken = null;
		// TwoCaptchaService service = new
		// TwoCaptchaService("8ff2e630e82351bdc3f0b00af2e026b9",
		// "6LccFA0TAAAAAHEwUJx_c1TfTBWMTAOIphwTtd1b", link, "" +
		// getAccount().getAccount().getProxyIp(),
		// "" + getAccount().getAccount().getProxyPort(),
		// getAccount().getAccount().getProxyUsername(),
		// getAccount().getAccount().getProxyPassword(), ProxyType.SOCKS5);
		//
		// try {
		// responseToken = service.solveCaptcha();
		// System.out.println("The response token is: " + responseToken);
		// } catch (InterruptedException | IOException e) {
		// // TODO Auto-generated catch block
		// driver.quit();
		// e.printStackTrace();
		// }
		//
		// RemoteWebDriver r = (RemoteWebDriver) driver;
		// String setResponseToken =
		// "document.getElementById('g-recaptcha-response').value='" + responseToken +
		// "'";
		// r.executeScript(setResponseToken);
		//
		// }).start();

		RemoteWebDriver r = (RemoteWebDriver) driver;
		String setResponseToken = "document.getElementById('g-recaptcha-response').value='"
				+ getCaptcha().getResultKey() + "'";
		System.out.println("Set key to: " + getCaptcha().getResultKey());
		r.executeScript(setResponseToken);
	}

	/**
	 * Clicks a button and verifies the link it redirects to
	 * 
	 * @param by
	 * @param link
	 * @return
	 */
	public boolean clickButtonAndVerifyLink(By by, String link) {
		try {

			if (WebdriverFunctions.hasQuit(driver)) {
				System.out.println("Breaking out of loop");
				return true;
			}
			
			WebDriverWait wait = new WebDriverWait(driver, 30);
			wait.until(ExpectedConditions.visibilityOfElementLocated(by));
			WebElement button = getDriver().findElement(by);

			if (button != null) {
				button.click();

				if (isAtLink(link)) {
					return true;
				}

				// Must recover ingame??
				if (isAtLinkNoWait("game-recovery")) {
					return accountUnkowninglyFailedRecover();
				}
				// Quiting driving when failing to click button
				if (isAtLinkNoWait("passwordrecovery")) {
//					driver.close();
//					driver.quit();
					return false;
				}

			}
		} catch (Exception e) {
			System.out.println("Couldn't verify clicking the button!");
			e.printStackTrace();
			// driver.close();
			// driver.quit();
		}
		return false;
	}

	/**
	 * 
	 * @param link
	 * @return
	 */
	public boolean isAtLink(String link) {
		try {
			return (new WebDriverWait(driver, 60)).until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver d) {
					return getCurrentURL().contains(link);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			// Ignorning stacktrace
			return false;
		}
	}

	public boolean isAtLinkNoWait(String link) {
		try {
			return getCurrentURL().contains(link);
		} catch (Exception e) {
			e.printStackTrace();
			// Ignorning stacktrace
			return false;
		}
	}

	/**
	 * 
	 * @return
	 */
	private boolean hasCaptchaCompleted() {
		RemoteWebDriver r = (RemoteWebDriver) driver;
		return (new WebDriverWait(driver, 300)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				String setResponseToken = "return document.getElementById('g-recaptcha-response').value";
				String a = r.executeScript(setResponseToken).toString();

				if (a.equalsIgnoreCase("ERROR_CAPTCHA_UNSOLVABLE")) {
//					driver.close();
//					driver.quit();
					System.out.println("Driver quit, captcha was unsolvable");
					return false;
				}
				return a.length() != 0 ? true : false;
			}
		});
	}

	/**
	 * All inputs exist on the page
	 * 
	 * @return
	 */
	private boolean allRecoverElementsExist() {
		if (elementsExist(new ArrayList<By>(Arrays.asList(By.id("email"))))) {
			return true;
		}
		;
		return false;
	}

	/**
	 * All inputs exist on the page
	 * 
	 * @return
	 */
	private boolean allInputElementsExist() {
		if (elementsExist(new ArrayList<By>(Arrays.asList(By.id("create-email"), By.id("create-password"),
				By.id("character-name"), By.className("m-date-entry__day-field"),
				By.className("m-date-entry__month-field"), By.className("m-date-entry__year-field"))))) {
			return true;
		}
		;
		return false;
	}

	/**
	 * Do all the elements have value?
	 * 
	 * @return
	 */
	private boolean allInputElementsHaveValue() {
		if (allElementsHaveValue(new ArrayList<By>(Arrays.asList(By.id("create-email"), By.id("create-password"),
				By.id("character-name"), By.className("m-date-entry__day-field"),
				By.className("m-date-entry__month-field"), By.className("m-date-entry__year-field"))))) {
			return true;
		}
		;
		return false;
	}

	/**
	 * Do all the elements have value?
	 * 
	 * @return
	 */
	private boolean allInputLockedAccountElementsHaveValue() {
		if (allElementsHaveValue(new ArrayList<By>(Arrays.asList(By.id("email"))))) {
			return true;
		}
		;
		return false;
	}

	/**
	 * 
	 * @return
	 */
	private int getRandomNumber() {
		Random rand = new Random();
		return rand.nextInt(1000000) + 1;
	}

	/**
	 * 
	 * @param upper
	 * @param lower
	 * @return
	 */
	public int getRandomValueBetweenUpperAndLower(int upper, int lower) {
		int r = (int) (Math.random() * (upper - lower)) + lower;
		return r;
	}

	/**
	 * Can fill in all the recover information
	 * 
	 * @return
	 */
	private boolean fillInRecoverInformation() {
		while (!allRecoverElementsExist()) {
			if (WebdriverFunctions.hasQuit(driver)) {
				System.out.println("Breaking out of loop");
				break;
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Waiting for all inputs exist on the page");
		}

		driver.findElement(By.id("email")).sendKeys("" + account.getAccount().getEmail());

		// Cookie consent dismissed
		// driver.findElement(By.className("c-cookie-consent__dismiss")).click();

		if (!allInputLockedAccountElementsHaveValue()) {
			goToRunescapeRecoverAccount();
			System.out.println("Not all inputs had value, restarting the website");
			return false;
		}
		System.out.println("All inputs have values!");
		return true;
	}

	/**
	 * 
	 * @return
	 */
	private boolean fillInNewPassword() {
		// while (!elementsExist(new ArrayList<By>(Arrays.asList(By.name("password"),
		// By.name("confirm"))))) {
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// System.out.println("Waiting for all inputs exist on the page");
		// }

		WebdriverFunctions.waitForLoad(driver);
		WebDriverWait wait = new WebDriverWait(driver, 20);
		WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));
		WebElement myDynamicElement = (new WebDriverWait(driver, 20))
				.until(ExpectedConditions.presenceOfElementLocated(By.name("password")));

		System.out.println("old password: " + getAccount().getAccount().getPassword());

		String password = new RandomNameGenerator().generateRandomNameString();
		driver.findElement(By.name("password")).sendKeys(password);
		driver.findElement(By.name("confirm")).sendKeys(password);
		DatabaseUtilities.updatePasswordInDatabase(password, getAccount().getId());

		System.out.println("new password: " + password);

		System.out.println("Filled in values!");
		return true;

	}

	/**
	 * Fills in all information required to make an account
	 * 
	 * @return
	 */
	private boolean fillInInformation() {
		// while (!allInputElementsExist()) {
		// try {
		// Thread.sleep(100);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// System.out.println("Waiting for all inputs exist on the page");
		// }
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("create-email")));

		int randomNumber = getRandomNumber();

		int day = getRandomValueBetweenUpperAndLower(1, 30);
		int month = getRandomValueBetweenUpperAndLower(0, 12);
		int year = getRandomValueBetweenUpperAndLower(1990, 2000);

		// New worlds selection of more F2P worlds
		List<Integer> worldsAvailable = new ArrayList<Integer>(
				Arrays.asList(474, 477, 470, 479, 472, 476, 473, 478, 475, 471, 469, 394, 453, 456, 452, 458, 460, 455,
						459, 451, 454, 457, 398, 397, 399, 383, 498, 497, 499, 504, 502, 503, 501, 500, 505, 506));

		int world = worldsAvailable.get(RandomUtil.getRandomNumberInRange(0, worldsAvailable.size() - 1));

		account.getAccount().setWorld(world);
		account.getAccount().setDay(day);
		account.getAccount().setMonth(month);
		account.getAccount().setYear(year);
		account.getAccount().setEmail("alphabearman+" + randomNumber + "@protonmail.com");
		account.getAccount().setUsername(new RandomNameGenerator().generateRandomNameString());
		account.getAccount().setPassword(new RandomNameGenerator().generateRandomNameString());

		int totalAccounts = DatabaseUtilities.getAvailableAccounts();
		int totalMules = DatabaseUtilities.getMuleAmount();

		/**
		 * Automatically creating mules
		 */
		if (totalMules == 0) {
			account.getAccount().setStage(AccountStage.UNKNOWN);
			account.getAccount().setStatus(AccountStatus.MULE);
		}

		driver.findElement(By.className("m-date-entry__day-field")).sendKeys("" + account.getAccount().getDay());
		driver.findElement(By.className("m-date-entry__month-field")).sendKeys("" + account.getAccount().getMonth());
		driver.findElement(By.className("m-date-entry__year-field")).sendKeys("" + account.getAccount().getYear());
		driver.findElement(By.id("create-email")).sendKeys("alphabearman+" + randomNumber + "@protonmail.com");
		driver.findElement(By.id("character-name")).sendKeys(account.getAccount().getUsername());
		driver.findElement(By.id("create-password")).sendKeys(account.getAccount().getPassword());
		// driver.findElement(By.className("c-cookie-consent__dismiss")).click();

		// if (!allInputElementsHaveValue()) {
		// goToRunescapeCreateAccount();
		// System.out.println("Not all inputs had value, restarting the website");
		// return false;
		// }

		System.out.println("All inputs have values!");
		return true;

	}

	/**
	 * Will determine if all elements on the page are visible
	 * 
	 * @param elements
	 * @return
	 */
	private boolean elementsExist(List<By> bys) {
		List<WebElement> elements = new ArrayList<WebElement>();
		for (By by : bys) {
			if (by != null) {
				elements.add(driver.findElement(by));
			}
		}
		int elementListSize = elements.size();
		int count = 0;
		for (WebElement element : elements) {
			if (element != null && element.isDisplayed()) {
				count++;
			}
		}
		return count == elementListSize ? true : false;
	}

	/**
	 * Determines if all the input fiels have values filled in or nit
	 * 
	 * @param bys
	 * @return
	 */
	private boolean allElementsHaveValue(List<By> bys) {
		List<WebElement> elements = new ArrayList<WebElement>();
		boolean allElementsHaveValue = true;
		for (By by : bys) {
			if (by != null) {
				elements.add(driver.findElement(by));
			}
		}
		for (WebElement element : elements) {
			if (element != null
					&& (element.getAttribute("value") == null || element.getAttribute("value").length() == 0)) {
				allElementsHaveValue = false;
			}
		}
		return allElementsHaveValue;
	}

	/**
	 * Goes to the recover account of runescape
	 * 
	 * @return
	 */
	private boolean goToRunescapeRecoverAccount() {
		// driver.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS);

		// try {
		getDriver().get(RunescapeWebsiteConfig.RUNESCAPE_RECOVER_ACCOUNT_URL);
		// } catch (TimeoutException e) {
		// System.out.println("Page did not load within 40 seconds!");
		// System.out.println("Restarting driver and trying again");
		// e.printStackTrace();
		// // treat the timeout as needed
		// driver.close();
		// driver.quit();
		// }

		System.out.println("Current URL: " + getCurrentURL());
		if (getCurrentURL().contains("runescape")) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	private boolean goToRunescapeCreateAccount() {
		// driver.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS);

		// try {
		getDriver().get(RunescapeWebsiteConfig.RUNESCAPE_CREATE_ACCOUNT_URL);
		// } catch (TimeoutException e) {
		// System.out.println("Page did not load within 40 seconds!");
		// System.out.println("Restarting driver and trying again");
		// e.printStackTrace();
		// // treat the timeout as needed
		// driver.close();
		// driver.quit();
		// }

		System.out.println("Current URL: " + getCurrentURL());
		if (getCurrentURL().contains("runescape")) {
			return true;
		}
		return false;
	}

	/**
	 * Waits for the page to load and returns the current url
	 * 
	 * @return
	 */
	private String getCurrentURL() {
		WebdriverFunctions.waitForLoad(driver);
		return getDriver().getCurrentUrl();
	}

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
	 * @return the failedTries
	 */
	public int getFailedTries() {
		return failedTries;
	}

	/**
	 * @param failedTries
	 *            the failedTries to set
	 */
	public void setFailedTries(int failedTries) {
		this.failedTries = failedTries;
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

	/**
	 * @return the captcha
	 */
	public Captcha getCaptcha() {
		return captcha;
	}

	/**
	 * @param captcha
	 *            the captcha to set
	 */
	public void setCaptcha(Captcha captcha) {
		this.captcha = captcha;
	}
}
