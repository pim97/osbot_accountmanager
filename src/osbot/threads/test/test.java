package osbot.threads.test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Select;

import osbot.account.creator.PidDriver;
import osbot.account.handler.GeckoHandler;
import osbot.account.runescape.website.RunescapeWebsiteConfig;

public class test {

	public static void main(String[] args) {

		System.setProperty("webdriver.gecko.driver", "geckodriver.exe");
		// System.getProperty("user.home") + "/toplistbot/driver/geckodriver.exe");
		System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
		System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");

		ProfilesIni profile2 = new ProfilesIni();
		FirefoxProfile profile = profile2.getProfile("bot");// new FirefoxProfile();

		FirefoxBinary firefoxBinary = new FirefoxBinary();
		// firefoxBinary.addCommandLineOptions("--headless");
		DesiredCapabilities dc = new DesiredCapabilities();
		FirefoxOptions option = new FirefoxOptions();

		option.setBinary(firefoxBinary);
		option.setProfile(profile);
		int pidId = -1;

		// PidDriver driver = new PidDriver();
		List<Integer> pids = GeckoHandler.getGeckodriverExeWindows();
		List<Integer> pidsAfter = null;

		WebDriver driver = new FirefoxDriver(option);

		int tries = 0;
		boolean searching = true;

		PidDriver pidDriver = new PidDriver(driver, pidId);

		Dimension n = new Dimension(1000, 700);
		driver.manage().window().setSize(n);

		driver.get("moz-extension://49aecb7d-8e81-4baf-8d90-d5e138cc07fd/add-edit-proxy.html"); // old

		// Selecting socks 5
		Select select = new Select(driver.findElement(By.id("newProxyType")));
		select.selectByIndex(1);

		driver.findElement(By.id("newProxyAddress")).sendKeys("1118.139.176.242");
		driver.findElement(By.id("newProxyPort")).sendKeys("14619");
		driver.findElement(By.id("newProxyUsername")).sendKeys("");
		driver.findElement(By.id("newProxyPassword")).sendKeys("");
		driver.findElement(By.id("newProxySave")).click();

		driver.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS);

		boolean onWebsite = false;

		while (!onWebsite) {
			try {
				driver.navigate().to(RunescapeWebsiteConfig.RUNESCAPE_CREATE_ACCOUNT_URL);
			} catch (Exception e) {
				System.out.println("Page did not load within 40 seconds!");
				System.out.println("Restarting driver and trying again");
				e.printStackTrace();
				driver.navigate().to(RunescapeWebsiteConfig.RUNESCAPE_CREATE_ACCOUNT_URL);
			}
			onWebsite = true;
		}

		System.out.println("Successfully navigateed!");
	}

}
