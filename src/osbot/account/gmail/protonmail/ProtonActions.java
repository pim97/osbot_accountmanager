package osbot.account.gmail.protonmail;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
		getDriver().navigate().to(ProtonConfig.LINK_TO_PROTON);

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
			List<WebElement> allLinksByTagA = driver.findElements(By.tagName("a"));

			for (WebElement link2 : allLinksByTagA) {
				if (link2 != null && link2.getAttribute("href") != null
						&& link2.getAttribute("href").contains(contentEquals)) {
					link2.click();
					return true;
				}
			}
		} catch (Exception e) {
			// Ignoring excetion, might be dangerous
			return false;
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public boolean clickedCorrectEmail() {
		try {
			WebdriverFunctions.waitForLoad(driver);
			Thread.sleep(2000);
			
			WebElement toEmail = getDriver().findElement(By.className("recipients-summary-label"));
			WebdriverFunctions.waitForElementToBeVisible(driver, toEmail);
			if (toEmail != null) {
				System.out.println("Label: "+ getAccount().getAccount().getEmail()+" "+toEmail.getText());
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

	/**
	 * 
	 * @return
	 */
	public boolean clickMail(String subjectName) {
		try {
			List<WebElement> email = getDriver().findElements(By.className("subject-text"));

			for (WebElement name : email) {
				if (name != null && name.getText().equalsIgnoreCase(subjectName)) {
					name.click();
//					WebElement parent = (WebElement) ((JavascriptExecutor) driver).executeScript(
//                            "return arguments[0].parentNode;", email);
//					System.out.println(parent+" found "+parent.getText());
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
	public boolean login(String username, String password) {
		while (!isLoggedIn()) {
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
			if (sendKeysAndVerifyValue(By.id("username"), username)) {
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
