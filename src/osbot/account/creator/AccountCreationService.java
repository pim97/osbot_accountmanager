package osbot.account.creator;

import java.io.IOException;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.twocaptcha.api.ProxyType;
import com.twocaptcha.api.TwoCaptchaService;

import osbot.account.gmail.GmailPageObjects;
import osbot.account.gmail.protonmail.ProtonMain;
import osbot.account.runescape.website.RunescapeActions;
import osbot.account.webdriver.WebdriverFunctions;
import osbot.database.DatabaseProxy;
import osbot.settings.OsbotController;

public class AccountCreationService {

	/**
	 * Waiting for element to be visible
	 * 
	 * @param driver
	 * @param element
	 */
	public static void waitForVisible(WebDriver driver, WebElement element) {
		try {
			Thread.sleep(1000);
			System.out.println("Waiting for element visibility");
			WebDriverWait wait = new WebDriverWait(driver, 15);
			wait.until(ExpectedConditions.visibilityOf(element));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Launching the runescape website
	 * 
	 * @param proxy
	 * @param account
	 * @param test
	 */
	public static void launchRunescapeWebsite(DatabaseProxy proxy, OsbotController account, boolean test) {
		WebdriverFunctions.killAll();

		System.setProperty("webdriver.gecko.driver",
				System.getProperty("user.home") + "/toplistbot/driver/geckodriver.exe");
		System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
		System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");

		ProfilesIni profile2 = new ProfilesIni();
		FirefoxProfile profile = profile2.getProfile("bot");// new FirefoxProfile();

		FirefoxBinary firefoxBinary = new FirefoxBinary();
		DesiredCapabilities dc = new DesiredCapabilities();
		FirefoxOptions option = new FirefoxOptions();

		option.setBinary(firefoxBinary);
		option.setProfile(profile);
		WebDriver driver = new FirefoxDriver(option);
		Dimension n = new Dimension(1000, 700);
		driver.manage().window().setSize(n);

		driver.get("moz-extension://49aecb7d-8e81-4baf-8d90-d5e138cc07fd/add-edit-proxy.html");

		Select select = new Select(driver.findElement(By.id("newProxyType")));
		select.selectByIndex(1);

		driver.findElement(By.id("newProxyAddress")).sendKeys(proxy.getProxyIp());
		driver.findElement(By.id("newProxyPort")).sendKeys(proxy.getProxyPort());
		driver.findElement(By.id("newProxyUsername")).sendKeys("rvWt0S");
		driver.findElement(By.id("newProxyPassword")).sendKeys("AqwncH");
		driver.findElement(By.id("newProxySave")).click();

		RunescapeActions runescapeWebsite = new RunescapeActions(driver, account);
		runescapeWebsite.create();
		/**
		 * The proton e-mail service
		 */
		 ProtonMain proton = new ProtonMain(driver, account);
		 proton.verifyAccount();

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		driver.quit();
		System.out.println("Account successfully created");

	}

	public static void main(String args[]) {
		for (int i = 0; i < 300; i++) {
			RandomNameGenerator name = new RandomNameGenerator();
			System.out.println(name.generateRandomNameString());
		}
		// ArrayList<AccountTable> account =
		// DatabaseUtilities.getAccountsFromMysqlConnection();
		// for (AccountTable acc : account) {
		// BotController.addBot(new OsbotController(acc.getId(), acc));
		// }
		// launchRunescapeWebsite(BotController.getBotById(7));
	}

}
