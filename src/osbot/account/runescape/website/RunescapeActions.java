package osbot.account.runescape.website;

import java.io.IOException;
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

import com.twocaptcha.api.ProxyType;

import osbot.account.AccountStatus;
import osbot.account.creator.PidDriver;
import osbot.account.creator.RandomNameGenerator;
import osbot.account.creator.SeleniumType;
import osbot.account.global.Config;
import osbot.account.webdriver.WebdriverFunctions;
import osbot.account.worlds.World;
import osbot.account.worlds.WorldType;
import osbot.database.DatabaseUtilities;
import osbot.settings.OsbotController;

public class RunescapeActions {

	/**
	 * 
	 * @param driver
	 * @param account
	 */
	public RunescapeActions(WebDriver driver, OsbotController account, SeleniumType type, PidDriver pidDriver) {
		setAccount(account);
		setDriver(driver);
		setType(type);
		setPidDriver(pidDriver);
	}

	private PidDriver pidDriver;

	private WebDriver driver;

	private OsbotController account;

	private SeleniumType type;

	/**
	 * Creates the account
	 */
	public boolean create() {
		if (createAccount()) {
			// Inserting into the database, because successfull
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

			Thread.sleep(10000);

			System.out.println(driver.getPageSource());
			if (driver.getPageSource().contains("You have been temporarily blocked from using this service.")) {// message.ws/?message=5
				DatabaseUtilities.updateStatusOfAccountByIpWithoutLockedTimeout(AccountStatus.LOCKED_TIMEOUT,
						getAccount().getAccount().getProxyIp());
				// HttpRequests.updateAccountStatusInDatabase(AccountStatus.LOCKED_TIMEOUT.name(),
				// getAccount().getAccount().getEmail());
				System.out.println("Account locked timeout fraudulent");
				driver.quit();
				return false;
			}

			// WebdriverFunctions.waitForElementToBeVisible(driver,
			// driver.findElement(By.name("password")));

			if (fillInNewPassword()) {
				if (!clickButtonAndVerifyLink(By.name("submit"), "enter_security_code", false, -1)) {// account_created
					if (WebdriverFunctions.hasQuit(driver)) {
						System.out.println("Breaking out of loop");
						return true;
					}
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
			driver.quit();
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
			System.out.println("On the runescappe account");

			if (driver.getPageSource().contains("You have been temporarily blocked from using this service.")
					&& driver.getCurrentUrl().contains("message.ws")) {// message.ws/?message=5
				DatabaseUtilities.updateStatusOfAccountByIpWithoutLockedTimeout(AccountStatus.LOCKED_TIMEOUT,
						getAccount().getAccount().getProxyIp());
				// HttpRequests.updateAccountStatusInDatabase(AccountStatus.LOCKED_TIMEOUT.name(),
				// getAccount().getAccount().getEmail());
				System.out.println("Account couldn't be recovered this way");
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
			System.out.println("Captcha has completed!");

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			setFailedTries(0);
			while (!clickButtonAndVerifyLink(By.id("passwordRecovery"), "email-confirmation", true, 1)) {// account-identified
				if (WebdriverFunctions.hasQuit(driver)) {
					System.out.println("Breaking out of loop");
					break;
				}
				System.out.println("Verifying the unlock button!");
				setFailedTries(getFailedTries() + 1);

				if (getFailedTries() > 1) {
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
				driver.quit();
				System.out.println("Captcha failed, retrying with new driver");
				return false;
			}

			System.out.println("Successully verified account");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Got error - restarting now");
			// driver.quit();
			return false;
		}
	}

	private boolean accountUnkowninglyFailedRecover() {
		System.out.println(driver.getPageSource());
		if (driver.getPageSource().contains("Due to your account status, you must")) {
			DatabaseUtilities.updateStatusOfAccountById(AccountStatus.LOCKED_INGAME, getAccount().getAccount().getId());
			// HttpRequests.updateAccountStatusInDatabase("LOCKED_INGAME",
			// getAccount().getAccount().getEmail());
			System.out.println("Account couldn't be recovered this way");
			driver.quit();
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
			// Thread for watching over the current page
			new Thread(() -> {
				while (!WebdriverFunctions.hasQuit(driver)) {
					// System.out.println("Current page url: " + getDriver().getCurrentUrl() + " at
					// link: "
					// + isAtLinkNoWait("error?error=1"));
					if (isAtLinkNoWait("error?error=1")) {
						System.out.println("LINK WAIT ONE 3");
						DatabaseUtilities.updateProxyStatus(getAccount().getAccount().getProxyIp(),
								getAccount().getAccount().getProxyPort(), true);
						System.out.println("Proxy set to blocked");
						driver.quit();
					}

					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();

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
			} else {
				createAccount();
				// Going to the website again -- restarting
			}

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
			System.out.println("Captcha has completed!");

			while (!clickButtonAndVerifyLink(By.id("create-submit"),
					"https://secure.runescape.com/m=account-creation/account_created", true, 0)) {// account_created

				WebElement el = driver
						.findElement(By.xpath("//p[contains(text(), 'Please complete the reCAPTCHA box.')]"));
				if (el.isDisplayed()) {
					System.out.println("Captcha failed");
					driver.quit();
					break;
				}

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
					// e.printStackTrace();
					System.out.println("Error in sleep");
				}
			}

			WebElement el = driver.findElement(By.xpath("//p[contains(text(), 'Please complete the reCAPTCHA box.')]"));
			if (el.isDisplayed()) {
				System.out.println("Captcha failed");
				driver.quit();
				return false;
			}

			if (waitUnCaptchaFailed(getType())) {
				System.out.println("Captcha failed, retrying with new driver");
				Thread.sleep(8000);
				driver.quit();
				return false;
			}

			System.out.println("Successfully clicked & account created!");
			return true;
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("Error - restarting");
			// driver.quit();
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
			// return (new WebDriverWait(driver, 20)).until(new ExpectedCondition<Boolean>()
			// {
			// public Boolean apply(WebDriver d) {
			// return driver.getPageSource().contains("You can now begin your adventure with
			// your new account.");
			// }
			// });
			if (driver.getPageSource().contains("You can now begin your adventure with your new account.")) {
				return false;
			}
		} else if (type == SeleniumType.RECOVER_ACCOUNT)

		{
			// WebdriverFunctions.waitForElementToBeVisible(driver,
			// driver.findElement(By.name("continueYes")));
			// return (new WebDriverWait(driver, 20)).until(new ExpectedCondition<Boolean>()
			// {
			// public Boolean apply(WebDriver d) {
			// return driver.getPageSource().contains("A link to reset your password has
			// been sent to");
			// }
			// });
			if (driver.getPageSource().contains("A link to reset your password has been sent to")) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Getting the captcha response token
	 */
	private void getResponseToken(String link) {

		new Thread(() -> {
			String responseToken = null;
			osbot.account.TwoCaptchaService service = new osbot.account.TwoCaptchaService(
					"8ff2e630e82351bdc3f0b00af2e026b9", "6Lcsv3oUAAAAAGFhlKrkRb029OHio098bbeyi_Hv", link,
					"" + getAccount().getAccount().getProxyIp(), "" + getAccount().getAccount().getProxyPort(),
					getAccount().getAccount().getProxyUsername(), getAccount().getAccount().getProxyPassword(),
					ProxyType.SOCKS5, true);

			try {
				responseToken = service.solveCaptcha();
				System.out.println("The response token is: " + responseToken);
			} catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				driver.quit();
				e.printStackTrace();
			}

			RemoteWebDriver r = (RemoteWebDriver) driver;
			String setResponseToken = "document.getElementById('g-recaptcha-response').value='" + responseToken + "'";

			// "document.getElementById('g-recaptcha-response').innerHTML='" + responseToken
			// + "'";

			r.executeScript(setResponseToken);

			String setResponseToken2 = "return document.getElementById('g-recaptcha-response').value";
			String a = r.executeScript(setResponseToken2).toString();

			System.out.println("Set resonse token on element to: " + a);

		}).start();
	}

	/**
	 * Clicks a button and verifies the link it redirects to
	 * 
	 * @param by
	 * @param link
	 * @return type = 0 - acc creation type = 1 - acc recover
	 */
	public boolean clickButtonAndVerifyLink(By by, String link, boolean javascriptExecutorInsteadOfFormRequest,
			int type) {
		try {
			if (WebdriverFunctions.hasQuit(driver)) {
				System.out.println("Breaking out of loop");
				return true;
			}
			WebDriverWait wait = new WebDriverWait(driver, 30);
			wait.until(ExpectedConditions.visibilityOfElementLocated(by));
			WebElement button = getDriver().findElement(by);

			if (button != null) {

				if (!javascriptExecutorInsteadOfFormRequest) {
					button.click();
				} else {
					// Execute javascript to click the button
					RemoteWebDriver r = (RemoteWebDriver) driver;
					String toExecute = "";
					// if (type == 0) {
					// toExecute = "$('#create-email-form').submit();";
					//
					// // "document.getElementById('create-email-form').submit();";
					//
					// } else if (type == 1) {
					// toExecute = "$('#password-recovery-form').submit();";
					// // toExecute = "document.getElementById('password-recovery-form').submit();";
					// }
					String setResponseToken = "onSubmit()";
					r.executeScript(setResponseToken);
				}

				// String text1 = "An error has occurred and it has not been possible to create
				// your account.";
				// WebElement el1 = driver.findElement(By.xpath("//*[contains(text(),'" + text1
				// + "')]"));
				// System.out.println("IS DISPLAYED ERROR: " + (el1 != null &&
				// el1.isDisplayed()));
				// if ((el1 != null && el1.isDisplayed()) ||

				System.out.println("LINK WAIT ONE 1");
				if (isAtLink(link)) {
					return true;
				}
				System.out.println("LINK WAIT ONE 2");

				if (isAtLinkNoWait("error?error=1")) {
					System.out.println("LINK WAIT ONE 3");
					DatabaseUtilities.updateProxyStatus(getAccount().getAccount().getProxyIp(),
							getAccount().getAccount().getProxyPort(), true);
					// HttpRequests.updateAccountStatusInDatabase(AccountStatus.LOCKED_TIMEOUT.name(),
					// getAccount().getAccount().getEmail());
					System.out.println("Proxy set to blocked");
					driver.quit();
					return false;
				}
				System.out.println("LINK WAIT ONE 4");

				// Must recover ingame??
				if (isAtLinkNoWait("game-recovery")) {
					return accountUnkowninglyFailedRecover();
				}

				String text = "Please enter a valid email address or username.";
				WebElement el = driver.findElement(By.xpath("//*[contains(text(),'" + text + "')]"));
				System.out.println("IS DISPLAYED: " + (el != null && el.isDisplayed()));
				if (el != null && el.isDisplayed()) {// message.ws/?message=5
					DatabaseUtilities.updateStatusOfAccountById(AccountStatus.BANNED, getAccount().getId());
					// HttpRequests.updateAccountStatusInDatabase(AccountStatus.LOCKED_TIMEOUT.name(),
					// getAccount().getAccount().getEmail());
					System.out.println("Account set to banned");
					driver.quit();
					return false;
				}

				// Quiting driving when failing to click button
				if (isAtLinkNoWait("passwordrecovery")) {
					driver.quit();
					return false;
				}

			}
		} catch (Exception e) {
			if (WebdriverFunctions.hasQuit(driver)) {
				System.out.println("Breaking out of loop");
				return true;
			}
			System.out.println("Couldn't verify clicking the button!");
			System.out.println("WARNING! BLOCKED STACKTRACE");
			// e.printStackTrace();
			driver.close();
			driver.quit();
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
			return (new WebDriverWait(driver, 45)).until(new ExpectedCondition<Boolean>() {
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
					driver.quit();
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
		if (allElementsHaveValue(new ArrayList<By>(
				Arrays.asList(By.id("create-email"), By.id("create-password"), By.className("m-date-entry__day-field"),
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
		return rand.nextInt(2146000000) + 1;
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
		// while (!allRecoverElementsExist()) {
		// try {
		// Thread.sleep(100);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// System.out.println("Waiting for all inputs exist on the page");
		// }

		WebDriverWait wait = new WebDriverWait(driver, 20);
		WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));

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

		WebDriverWait wait = new WebDriverWait(driver, 120);
		WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));
		WebElement myDynamicElement = (new WebDriverWait(driver, 120))
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
		WebDriverWait wait = new WebDriverWait(driver, 20);
		WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("create-email")));

		int randomNumber = getRandomNumber();

		int day = getRandomValueBetweenUpperAndLower(1, 30);
		int month = getRandomValueBetweenUpperAndLower(0, 12);
		int year = getRandomValueBetweenUpperAndLower(1990, 2000);

		// New worlds selection of more F2P worlds
		World worldsAvailable = World.getRandomWorldWithLessPopulation(WorldType.F2P, 20);
		// new ArrayList<Integer>(
		// Arrays.asList(474, 477, 470, 479, 472, 476, 473, 478, 475, 471, 469, 394,
		// 453, 456, 452, 458, 460, 455,
		// 459, 451, 454, 457, 398, 397, 399, 383, 498, 497, 499, 504, 502, 503, 501,
		// 500, 505, 506));

		int world = worldsAvailable.getNumber();

		account.getAccount().setWorld(world);
		account.getAccount().setDay(day);
		account.getAccount().setMonth(month);
		account.getAccount().setYear(year);
		StringBuilder email = new StringBuilder();

		// Email stringbuilder
		email.append(Config.PREFIX_EMAIL + "+");
		email.append(randomNumber);
		email.append("@protonmail.com");

		account.getAccount().setEmail(email.toString());

		// Dates
		driver.findElement(By.className("m-date-entry__day-field"))
				.sendKeys(new StringBuilder().append(account.getAccount().getDay()).toString());

		driver.findElement(By.className("m-date-entry__month-field"))
				.sendKeys(new StringBuilder().append(account.getAccount().getMonth()).toString());

		driver.findElement(By.className("m-date-entry__year-field"))
				.sendKeys(new StringBuilder().append(account.getAccount().getYear()).toString());
		driver.findElement(By.id("create-email")).sendKeys(account.getAccount().getEmail());

		driver.findElement(By.id("create-password")).sendKeys(account.getAccount().getPassword());
		// driver.findElement(By.id("character-name")).sendKeys(account.getAccount().getUsername());
		// driver.findElement(By.className("c-cookie-consent__dismiss")).click();

		if (!allInputElementsHaveValue()) {
			goToRunescapeCreateAccount();
			System.out.println("Not all inputs had value, restarting the website");
			return false;
		}

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
		driver.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS);

		try {
			getDriver().navigate().to(RunescapeWebsiteConfig.RUNESCAPE_RECOVER_ACCOUNT_URL);
		} catch (TimeoutException e) {
			System.out.println("Page did not load within 40 seconds!");
			System.out.println("Restarting driver and trying again");
			e.printStackTrace();
			// treat the timeout as needed
			driver.quit();
		}

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
		driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);

		try {
			getDriver().navigate().to(RunescapeWebsiteConfig.RUNESCAPE_CREATE_ACCOUNT_URL);
		} catch (TimeoutException e) {
			System.out.println("Page did not load within 120 seconds!");
			System.out.println("Restarting driver and trying again");
			e.printStackTrace();
			// treat the timeout as needed
			driver.quit();
		}

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
}
