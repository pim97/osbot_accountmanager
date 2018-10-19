package osbot.account.gmail;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import osbot.account.webdriver.WebdriverFunctions;

public class GmailPageObjects {

	private WebDriver driver;
	@FindBy(how = How.XPATH, xpath = "//input[@id='identifierId']")
	WebElement emailField;

	@FindBy(how = How.XPATH, xpath = "//*[@id='password']/div[1]/div/div[1]/input")
	WebElement passwordField;
	//
	@FindBy(how = How.XPATH, xpath = "//span[@class='bog']")
	List<WebElement> emailThreads;

	@FindBy(how = How.XPATH, xpath = "//span[@class='azewN']")
	WebElement profileLogo;

	@FindBy(how = How.XPATH, xpath = "//div[@aria-label='Verwijderen']")
	WebElement deleteButton;

	public GmailPageObjects(WebDriver driver) {
		this.driver = driver;
	}

	public void enterEmail(String emailID) {
		waitForVisible(driver, emailField);
		Actions actions = new Actions(driver);
		actions.moveToElement(emailField);
		actions.click();
		actions.sendKeys(emailID + Keys.ENTER);
		actions.build().perform();
		System.out.println("Email entered");
	}

	public void enterPassword(String password) {
		waitForVisible(driver, passwordField);
		Actions actions = new Actions(driver);
		actions.moveToElement(passwordField);
		actions.click();
		actions.sendKeys(password + Keys.ENTER);
		actions.build().perform();
		System.out.println("Password entered");
	}

	public boolean clickLink(String link) {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		RemoteWebDriver r = (RemoteWebDriver) driver;
//		String setSelfLink = "var all = document.getElementsByTagName(\"a\"); for (var i=0; i < all.length; i++) { all[i].setAttribute('target','_self'); }";
//		r.executeScript(setSelfLink);
		
		driver.findElement(By.className("bBe")).click();

		List<WebElement> allLinks = driver.findElements(By.tagName("a"));
		WebElement lastLink = null;

		System.out.println("All links found on web page are: " + allLinks.size() + " links");

		WebdriverFunctions.waitForLoad(driver);

		for (WebElement link2 : allLinks) {
			if (link2 != null && link2.getAttribute("href") != null && link2.getAttribute("href").contains(link)) {
				lastLink = link2;
			}
		}
		System.out.println("clicking now if found");
		if (lastLink != null) {
			lastLink.click();
			return true;
		}
		return false;
	}

	// public void deleteEmail() {
	// WebElement deleteButton = driver.findElement(By.className("asa"));
	// System.out.println("delBut "+deleteButton);
	// waitForVisible(driver, deleteButton);
	// deleteButton.click();
	// }

	public void clickDeleteButton() {

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		driver.findElement(By.xpath("//div[@aria-label='Verwijderen']"))
			.click();
	}

	public void clickEmail(String emailSubject) {

		System.out.println("Looking for email subject");
		
//		WebdriverFunctions.waitForLoad(driver);
		waitForVisible(driver, profileLogo);

//		List<WebElement> emailThreads = driver.findElements(By.xpath("//span[@class='bog']"));

		System.out.println(emailThreads.size());
		for (int i = 0; i < emailThreads.size(); i++) {

			System.out.println(emailThreads.get(i).getText());
			if (emailThreads.get(i).getText().equalsIgnoreCase(emailSubject)) {
				emailThreads.get(i).click();
				System.out.println("email clicked");
				break;
			}
		}
	}

	public void waitForVisible(WebDriver driver, WebElement element) {
		try {
//			WebdriverFunctions.waitForLoad(driver);

			Thread.sleep(1000);
			System.out.println("Waiting for element visibility");
			WebDriverWait wait = new WebDriverWait(driver, 15);
			wait.until(ExpectedConditions.visibilityOf(element));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}