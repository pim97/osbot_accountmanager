package osbot.account.runescape.website;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.twocaptcha.api.ProxyType;
import com.twocaptcha.api.TwoCaptchaService;

import osbot.account.webdriver.WebdriverFunctions;
import osbot.database.DatabaseUtilities;
import osbot.settings.OsbotController;

public class RunescapeActions {

	/**
	 * 
	 * @param driver
	 * @param account
	 */
	public RunescapeActions(WebDriver driver, OsbotController account) {
		setAccount(account);
		setDriver(driver);
	}

	private WebDriver driver;

	private OsbotController account;

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
	 * Creates an account on Runescape
	 * 
	 * @return
	 */
	private boolean createAccount() {
		while (!goToRunescapeCreateAccount()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Waiting on navigating to the Runescape account");
		}
		System.out.println("On the runescape website!");
		
		getResponseToken();

		// Filling in all the information
		if (fillInInformation()) {
			System.out.println("Successfully filled in all information!");
		} else {
			createAccount();
			// Going to the website again -- restarting
		}

		while (!hasCaptchaCompleted()) {
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
				"https://secure.runescape.com/m=account-creation/account_created")) {
			System.out.println("Verifying the clicking button!");

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		System.out.println("Successfully clicked & account created!");
		return true;
	}

	/**
	 * Getting the captcha response token
	 */
	private void getResponseToken() {
		new Thread(() -> {
			String responseToken = null;
			TwoCaptchaService service = new TwoCaptchaService("8ff2e630e82351bdc3f0b00af2e026b9",
					"6LccFA0TAAAAAHEwUJx_c1TfTBWMTAOIphwTtd1b",
					"https://secure.runescape.com/m=account-creation/create_account",
					"" + getAccount().getAccount().getProxyIp(), "" + getAccount().getAccount().getProxyPort(),
					"rvWt0S", "AqwncH", ProxyType.SOCKS5);

			try {
				responseToken = service.solveCaptcha();
				System.out.println("The response token is: " + responseToken);
			} catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			RemoteWebDriver r = (RemoteWebDriver) driver;
			String setResponseToken = "document.getElementById('g-recaptcha-response').value='" + responseToken + "'";
			r.executeScript(setResponseToken);
			
		}).start();
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
			WebElement button = getDriver().findElement(by);

			WebdriverFunctions.waitForElementToBeVisible(driver, button);
			if (button != null) {
				button.click();

				WebdriverFunctions.waitForUrl(driver, link);
				System.out.println(getCurrentURL() + " " + link);
				if (getCurrentURL().contains(link)) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
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
	 * Fills in all information required to make an account
	 * 
	 * @return
	 */
	private boolean fillInInformation() {
		while (!allInputElementsExist()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Waiting for all inputs exist on the page");
		}
		int randomNumber = getRandomNumber();

		int day = getRandomValueBetweenUpperAndLower(1, 30);
		int month = getRandomValueBetweenUpperAndLower(0, 12);
		int year = getRandomValueBetweenUpperAndLower(1990, 2000);

		account.getAccount().setWorld(394);
		account.getAccount().setDay(day);
		account.getAccount().setMonth(month);
		account.getAccount().setYear(year);
		account.getAccount().setEmail("alphabearman+" + randomNumber + "@protonmail.com");

		driver.findElement(By.className("m-date-entry__day-field")).sendKeys("" + account.getAccount().getDay());
		driver.findElement(By.className("m-date-entry__month-field")).sendKeys("" + account.getAccount().getMonth());
		driver.findElement(By.className("m-date-entry__year-field")).sendKeys("" + account.getAccount().getYear());
		driver.findElement(By.id("create-email")).sendKeys("alphabearman+" + randomNumber + "@protonmail.com");
		driver.findElement(By.id("create-password")).sendKeys(account.getAccount().getPassword());
		driver.findElement(By.id("character-name")).sendKeys(account.getAccount().getUsername());
		driver.findElement(By.className("c-cookie-consent__dismiss")).click();

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
	 * 
	 * @return
	 */
	private boolean goToRunescapeCreateAccount() {
		getDriver().navigate().to(RunescapeWebsiteConfig.RUNESCAPE_CREATE_ACCOUNT_URL);

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
}
