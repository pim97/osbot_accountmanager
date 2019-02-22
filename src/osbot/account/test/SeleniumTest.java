package osbot.account.test;

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

import osbot.account.runescape.website.RunescapeWebsiteConfig;

public class SeleniumTest {

	public static void main(String args[]) {
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

		driver.get(RunescapeWebsiteConfig.RUNESCAPE_CREATE_ACCOUNT_URL);
		
		driver.findElement(By.id("create-submit")).click();
		
		//span[contains(text(), 'Assign Rate')]
//		WebElement el = driver.findElement(By.xpath("//p[contains='Please complete the reCAPTCHA box.']"));
		WebElement el = driver.findElement(By.xpath("//p[contains(text(), 'Please complete the reCAPTCHA box.')]"));
		
		System.out.println("Element visible: "+el.isDisplayed());
		
		
	}
}
