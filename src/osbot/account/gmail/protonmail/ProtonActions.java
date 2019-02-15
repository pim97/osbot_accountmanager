package osbot.account.gmail.protonmail;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import osbot.account.global.Config;
import osbot.account.webdriver.WebdriverFunctions;
import osbot.settings.OsbotController;

public class ProtonActions {

	/**
	 * 
	 * @param driver
	 */
	public ProtonActions(WebDriver driver, OsbotController account) {
		this.setDriver(driver);
		this.setAccount(account);
	}

	/**
	 * The webdriver
	 */
	private WebDriver driver;

	/**
	 * The account to verify
	 */
	private OsbotController account;

	/**
	 * Opens the proton mail and checks if the url is correct
	 * 
	 * @return
	 */
	private boolean openMail() {
		driver.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS);

		boolean onWebsite = false;

		while (!onWebsite) {
			if (WebdriverFunctions.hasQuit(driver)) {
				System.out.println("Breaking out of loop");
				onWebsite = true;
			}
			try {
				driver.navigate().to(ProtonConfig.LINK_TO_PROTON);
			} catch (Exception e) {
				System.out.println("Page did not load within 40 seconds!");
				System.out.println("Restarting driver and trying again");
				e.printStackTrace();
				driver.navigate().to(ProtonConfig.LINK_TO_PROTON);
			}
			onWebsite = true;

			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("Current URL: " + getCurrentURL());
		if (getCurrentURL().contains("protonmail")) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public boolean sendKeysAndVerifyValue(By by, String sendKeys) {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WebElement element = getDriver().findElement(by);

		WebdriverFunctions.waitForElementToBeVisible(driver, element);
		if (element != null) {
			element.sendKeys(sendKeys);
			System.out.println("Current input in element: " + element.getAttribute("value"));
			if (element.getAttribute("value").equalsIgnoreCase(sendKeys)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * is the user currently logged in or not?
	 * 
	 * @return
	 */
	private boolean isLoggedIn() {
		try {
			WebElement element = getDriver().findElement(By.id("ptSidebar"));

			if (element != null && element.isDisplayed()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Not logged in yet, not catching exception
			return false;
		}
		return false;
	}

	/**
	 * Click a specific link in the email
	 * 
	 * @param contentEquals
	 * @return
	 */
	public boolean clickLink(String contentEquals) {
		try {
			List<WebElement> allLinksByTagA = driver
					.findElements(By.xpath("//a[contains(text(),'https://secure.runescape.com')]"));

			for (WebElement link2 : allLinksByTagA) {
				System.out.println(
						"link found: " + link2 + " " + link2.getAttribute("href") != null ? link2.getAttribute("href")
								: "null");
				if (link2 != null && link2.getAttribute("href") != null
						&& link2.getAttribute("href").contains(contentEquals)) {
					link2.click();
					return true;
				}
			}
		} catch (SessionNotCreatedException e1) {
			e1.printStackTrace();
			driver.quit();
			if (WebdriverFunctions.hasQuit(driver)) {
				System.out.println("Breaking out of loop");
				return true;
			}
		} catch (Exception e) {
			if (WebdriverFunctions.hasQuit(driver)) {
				System.out.println("Breaking out of loop");
				return true;
			}
			e.printStackTrace();
			// Ignoring excetion, might be dangerous
			return false;
		}
		return false;
	}

	public boolean blackFridayDeals() {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 2);

			WebElement element = wait.until(
					ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@class='fa fa-times close']")));
			System.out.println("element found: " + element);
			if (element != null && element.isDisplayed()) {
				element.click();
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Couldnt find the friday deals!");
			return false;
		}
	}

	/**
	 * 
	 * @return
	 */
	public boolean clickedCorrectEmail() {
		try {
			WebdriverFunctions.waitForLoad(driver);
			Thread.sleep(2000);

			WebDriverWait wait = new WebDriverWait(driver, 30);

			WebElement element = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.className("recipients-summary-label")));

			WebElement toEmail = getDriver().findElement(By.className("recipients-summary-label"));

			WebdriverFunctions.waitForElementToBeVisible(driver, toEmail);
			if (toEmail != null) {
				System.out.println("Label: " + getAccount().getAccount().getEmail() + " " + toEmail.getText());
				if (getAccount().getAccount().getEmail().equalsIgnoreCase(toEmail.getText())) {
					return true;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public boolean deleteEmail() {
		try {
			WebElement deleteButton = getDriver().findElement(By.className("moveElement-btn-trash"));

			WebdriverFunctions.waitForElementToBeVisible(driver, deleteButton);
			if (deleteButton != null) {

				WebDriverWait wait = new WebDriverWait(driver, 30);

				WebElement element = wait
						.until(ExpectedConditions.visibilityOfElementLocated(By.className("recipients-summary-label")));

				WebElement toEmail = getDriver().findElement(By.className("recipients-summary-label"));
				WebdriverFunctions.waitForElementToBeVisible(driver, toEmail);
				deleteButton.click();
				System.out.println("Clicked delete button");

				if (toEmail == null) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	private int clickedIndex = 0;

	private int loop;

	/**
	 * 
	 * @return
	 */
	public boolean clickMail(String subjectName) {
		try {

			if (WebdriverFunctions.hasQuit(driver)) {
				System.out.println("Breaking out of loop");
				return true;
			}

			WebDriverWait wait = new WebDriverWait(driver, 30);

			WebElement element = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.className("subject-text")));

			List<WebElement> name = getDriver().findElements(By.className("subject-text"));

			if ((clickedIndex > 25) || (clickedIndex > name.size())) {// name.size()) {
				loop++;
				clickedIndex = 0;
			}

			if (loop > 10) {
				driver.close();
				driver.quit();
				System.out.println("Couldn't find e-mail 11 times, restarting & retrying");
				return false;
			}

			// for (WebElement name : email) {

			if (name.get(clickedIndex) != null && clickedIndex < name.size()
					&& name.get(clickedIndex).getText().equalsIgnoreCase(subjectName)) {
				name.get(clickedIndex).click();
				clickedIndex++;
				// WebElement parent = (WebElement) ((JavascriptExecutor) driver).executeScript(
				// "return arguments[0].parentNode;", email);
				// System.out.println(parent+" found "+parent.getText());
				return true;
			}
			// }

		} catch (SessionNotCreatedException e1) {
			e1.printStackTrace();
			driver.quit();
			if (WebdriverFunctions.hasQuit(driver)) {
				System.out.println("Breaking out of loop");
				return true;
			}
		} catch (org.openqa.selenium.WebDriverException e1) {
			e1.printStackTrace();
			driver.quit();
			if (WebdriverFunctions.hasQuit(driver)) {
				System.out.println("Breaking out of loop");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			clickedIndex++;
			return false;
		}
		clickedIndex++;
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public boolean login(String username, String password) {
		while (!isLoggedIn()) {
			if (WebdriverFunctions.hasQuit(driver)) {
				System.out.println("Breaking out of loop");
				break;
			}
			if (logInToMail(username, password)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Logging into mail
	 * 
	 * @return
	 */
	private boolean logInToMail(String username, String password) {
		if (openMail()) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (sendKeysAndVerifyValue(By.id("username"), Config.PREFIX_EMAIL + "@protonmail.com")) {
				if (sendKeysAndVerifyValue(By.id("password"), password)) {
					if (clickButtonAndVerifyLink(By.id("login_btn"), ProtonConfig.LINK_TO_PROTON)) {
						return true;
					}
				}
			}
		}
		return false;
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
				if (getCurrentURL().equalsIgnoreCase(link)) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	 * Setters and getters
	 * 
	 * @return
	 */
	public WebDriver getDriver() {
		return driver;
	}

	/**
	 * 
	 * @param driver
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
