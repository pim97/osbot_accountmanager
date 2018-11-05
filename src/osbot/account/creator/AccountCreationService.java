package osbot.account.creator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import osbot.account.gmail.protonmail.ProtonMain;
import osbot.account.handler.BotHandler;
import osbot.account.handler.GeckoHandler;
import osbot.account.runescape.website.RunescapeActions;
import osbot.bot.BotController;
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
	 * Drivers
	 */
	public static final ArrayList<PidDriver> ALL_DRIVERS = new ArrayList<PidDriver>();

	/**
	 * Checks the processes
	 */
	public static void checkProcesses() {
		Iterator<PidDriver> it = ALL_DRIVERS.iterator();

		while (it.hasNext()) {
			PidDriver nextPid = it.next();

			if (!BotHandler.isProcessIdRunningOnWindows(nextPid.getPidId())) {
				System.out.println(
						"Removed pid: " + nextPid.getPidId() + " from the processes list, was no longer running");
				it.remove();
			}
		}
	}

	public static void checkPreviousProcessesAndDie(PidType type) {
		for (PidDriver d : ALL_DRIVERS) {
			if (d.getType() == type) {
				BotController.killProcess(d.getPidId());
				System.out.println("Killed pid: " + d.getPidId() + " with type: " + type.name());
			}
		}
	}

	/**
	 * Returns the pid driver
	 * 
	 * @param pid
	 * @return
	 */
	public static PidDriver getPidDriver(int pid) {
		for (PidDriver p : ALL_DRIVERS) {
			if (p.getPidId() == pid) {
				return p;
			}
		}
		return null;
	}

	private static boolean oneDriverLaunchingAtATime = false;

	/**
	 * Launching the runescape website
	 * 
	 * @param proxy
	 * @param account
	 * @param test
	 */
	public static void launchRunescapeWebsite(DatabaseProxy proxy, OsbotController account, SeleniumType type) {
		// WebdriverFunctions.killAll();
		checkProcesses();

		if (oneDriverLaunchingAtATime) {
			return;
		}

		oneDriverLaunchingAtATime = true;
		System.setProperty("webdriver.gecko.driver",
				System.getProperty("user.home") + "/toplistbot/driver/geckodriver.exe");
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

		boolean searching = true;
		while (searching) {
			pidsAfter = GeckoHandler.getGeckodriverExeWindows();

			if (pids.size() != pidsAfter.size()) {
				pidsAfter.removeAll(pids);

				searching = false;
				System.out.println("Found pid!");

				pidsAfter.stream().forEach(pid -> {
					System.out.println("pid found : " + pid);
				});
			} else {
				System.out.println("Couldn't find Pid yet, " + pids.size() + " " + pidsAfter.size());
			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Trying to find the pid");
		}

		if (pidsAfter.size() == 1) {
			pidId = pidsAfter.get(0);
			System.out.println("Pid set to with geckodriver: " + pidsAfter.get(0));
		} else {
			driver.quit();
			System.out.println("Quitting driver, couldn't specify the pid");
			return;
		}

		if (pidId < 0) {
			System.out.println("Pid couldn't be set");
			return;
		} else {
			System.out.println("Pid set!");
		}
		PidDriver pidDriver = new PidDriver(driver, pidId);

		oneDriverLaunchingAtATime = false;

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

		if (type == SeleniumType.CREATE_VERIFY_ACCOUNT) {
			pidDriver.setType(PidType.CREATE);
			checkPreviousProcessesAndDie(pidDriver.getType());
			ALL_DRIVERS.add(pidDriver);
			RunescapeActions runescapeWebsite = new RunescapeActions(driver, account, type, pidDriver);
			if (runescapeWebsite.create()) {
				/**
				 * The proton e-mail service
				 */
				ProtonMain proton = new ProtonMain(driver, account, pidDriver);
				proton.verifyAccount();
			}

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			driver.quit();
			System.out.println("Account successfully created");
		}

		else if (type == SeleniumType.RECOVER_ACCOUNT) {
			pidDriver.setType(PidType.RECOVER);
			checkPreviousProcessesAndDie(pidDriver.getType());
			ALL_DRIVERS.add(pidDriver);
			RunescapeActions runescapeWebsite = new RunescapeActions(driver, account, type, pidDriver);
			if (runescapeWebsite.unlock()) {
				/**
				 * Proton e-mail
				 */
				ProtonMain proton = new ProtonMain(driver, account, pidDriver);
				proton.unlockAccount();
			}

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// driver.quit();
			System.out.println("Account recovered successfully");
		}

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
