package osbot.account.creator;

import java.io.File;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Select;

public class AccountCreationService {

	public static void launchRunescapeWebsite() {
		System.setProperty("webdriver.gecko.driver",
				System.getProperty("user.home") + "/toplistbot/driver/geckodriver.exe");
//		System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
//		System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");

		ProfilesIni profile2 = new ProfilesIni();
		FirefoxProfile profile = profile2.getProfile("bot");//new FirefoxProfile();

	    FirefoxBinary firefoxBinary = new FirefoxBinary();
		DesiredCapabilities dc = new DesiredCapabilities();
		FirefoxOptions option = new FirefoxOptions();
		option.setBinary(firefoxBinary);
		option.setProfile(profile);
		WebDriver driver = new FirefoxDriver(option);
		driver.get("moz-extension://49aecb7d-8e81-4baf-8d90-d5e138cc07fd/add-edit-proxy.html");
	
		Select select = new Select(driver.findElement(By.id("newProxyType")));
		select.selectByIndex(1);
		
		driver.findElement(By.id("newProxyAddress")).sendKeys("185.36.190.252");
		driver.findElement(By.id("newProxyPort")).sendKeys("8000");
		driver.findElement(By.id("newProxyUsername")).sendKeys("rvWt0S");
		driver.findElement(By.id("newProxyPassword")).sendKeys("AqwncH");
		driver.findElement(By.id("newProxySave")).click();
		
		driver.get("https://secure.runescape.com/m=account-creation/create_account");
		
		driver.findElement(By.id("create-email")).sendKeys("toplistbot+1@gmail.com");
		driver.findElement(By.id("create-password")).sendKeys("passw");
		driver.findElement(By.id("character-name")).sendKeys("name");
		driver.findElement(By.name("name")).sendKeys("22");
		driver.findElement(By.name("month")).sendKeys("05");
		driver.findElement(By.name("year")).sendKeys("1995");
		
		
		
	}

	public static void main(String args[]) {
		launchRunescapeWebsite();
	}

}
