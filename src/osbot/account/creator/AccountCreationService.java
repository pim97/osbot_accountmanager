package osbot.account.creator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
	public static final List<PidDriver> ALL_DRIVERS = new CopyOnWriteArrayList<PidDriver>();

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

	public static void removeProcess(int pid) {
		Iterator<PidDriver> it = ALL_DRIVERS.iterator();

		while (it.hasNext()) {
			PidDriver nextPid = it.next();

			if (pid == nextPid.getPidId()) {
				System.out.println(
						"Removed pid: " + nextPid.getPidId() + " from the processes list, was no longer running");
				it.remove();
			}
		}
	}

	public static boolean checkPreviousProcessesAndDie(SeleniumType type) {
		for (PidDriver d : ALL_DRIVERS) {
			if (d.getType() == type) {
				// d.getDriver().quit();
				// BotController.killProcess(d.getPidId());
				// System.out.println("Killed pid: " + d.getPidId() + " with type: " +
				// type.name());
				return true;
			}
		}
		return false;
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

	public static boolean containsDriver(int pid) {
		for (PidDriver d : AccountCreationService.ALL_DRIVERS) {
			if (d.getPidId() == pid) {
				return true;
			}
		}
		return false;
	}

	private static boolean launching = false;

	public static synchronized boolean getLaunching() {
		return launching;
	}

	public static synchronized void setLaunching(boolean launch) {
		launching = launch;
	}

	private static ArrayList<AccountCreate> usedUsernames = new ArrayList<AccountCreate>();

	public static boolean containsUsername(String user) {
		for (AccountCreate acc : usedUsernames) {
			if (acc.getUsername().equalsIgnoreCase(user)) {
				return true;
			}
		}
		return false;
	}

	public static void addUsernameToUsernames(String username) {
		AccountCreate acc = new AccountCreate(System.currentTimeMillis(), username);
		usedUsernames.add(acc);
	}

	public static void checkUsedUsernames() {
		Thread t = new Thread(() -> {

			synchronized (usedUsernames) {
				Iterator<AccountCreate> it = usedUsernames.iterator();
				System.out.println("List checked usernames: " + usedUsernames.size());

				while (it.hasNext()) {
					AccountCreate user = it.next();
					System.out.println("Time to remove: " + (System.currentTimeMillis() - user.getTime()));
					if (((System.currentTimeMillis() - user.getTime()) > 600_000)) {
						it.remove();
						System.out.println("Removed username, may continue with recovering");
					}
				}
			}

		});
		t.start();
	}

	/**
	 * Launching the runescape website
	 * 
	 * @param proxy
	 * @param account
	 * @param test
	 */
	public static void launchRunescapeWebsite(DatabaseProxy proxy, OsbotController account, SeleniumType type) {
		// if (getLaunching()) {
		// return;
		// }
		// if (checkPreviousProcessesAndDie(type)) {
		// return;
		// }

		setLaunching(true);
		long begin = System.currentTimeMillis();

		System.setProperty("webdriver.gecko.driver", "geckodriver.exe");
//				System.getProperty("user.home") + "/toplistbot/driver/geckodriver.exe");
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
		// while (searching) {
		// if (tries > 5) {
		// driver.quit();
		// setLaunching(false);
		// searching = false;
		// System.out.println("Couldn't find the PID, restarting the browser");
		// return;
		// }
		// pidsAfter = GeckoHandler.getGeckodriverExeWindows();
		//
		// if (pids.size() != pidsAfter.size()) {
		// pidsAfter.removeAll(pids);
		//
		// searching = false;
		// System.out.println("Found pid!");
		//
		// pidsAfter.stream().forEach(pid -> {
		// System.out.println("pid found : " + pid);
		// });
		// } else {
		// System.out.println("Couldn't find Pid yet, " + pids.size() + " " +
		// pidsAfter.size());
		// }
		//
		// System.out.println("Trying to find the pid");
		// tries++;
		// }

		// if (pidsAfter.size() == 1) {
		// pidId = pidsAfter.get(0);
		// setLaunching(false);
		// System.out.println("Pid set to with geckodriver: " + pidsAfter.get(0));
		// } else if (pidsAfter.size() > 1) {
		// // AccountCreationService.checkPreviousProcessesAndDie(type);
		// // WebdriverFunctions.killAll();
		// setLaunching(false);
		// System.out.println("Quitting driver, couldn't specify the pid");
		// return;
		// }

		// if (pidId < 0) {
		// System.out.println("Pid couldn't be set");
		// driver.quit();
		// setLaunching(false);
		// return;
		// } else {
		// System.out.println("Pid set!");
		// }
		System.out.println("launched in " + ((System.currentTimeMillis() - begin) / 1000) + " seconds");
		PidDriver pidDriver = new PidDriver(driver, pidId);

		Dimension n = new Dimension(1000, 700);
		driver.manage().window().setSize(n);

		driver.get("moz-extension://49aecb7d-8e81-4baf-8d90-d5e138cc07fd/add-edit-proxy.html"); // old

		// Selecting socks 5
		Select select = new Select(driver.findElement(By.id("newProxyType")));
		select.selectByIndex(1);

		driver.findElement(By.id("newProxyAddress")).sendKeys(account.getAccount().getProxyIp());
		driver.findElement(By.id("newProxyPort")).sendKeys(account.getAccount().getProxyPort());
		driver.findElement(By.id("newProxyUsername")).sendKeys(account.getAccount().getProxyUsername());
		driver.findElement(By.id("newProxyPassword")).sendKeys(account.getAccount().getProxyPassword());
		driver.findElement(By.id("newProxySave")).click();

		System.out.println("Used proxy ip: " + account.getAccount().getProxyIp());
		System.out.println("Used proxy port: " + account.getAccount().getProxyPort());
		System.out.println("Used proxy username: " + account.getAccount().getProxyUsername());
		System.out.println("Used proxy password: " + account.getAccount().getProxyPassword());

		if (type == SeleniumType.CREATE_VERIFY_ACCOUNT) {
			pidDriver.setType(type);
			// ALL_DRIVERS.add(pidDriver);

			// Killing all pids that were active but not found in the system
			// for (int pid : pids) {
			// PidDriver driv = getPidDriver(pid);
			// if (driv == null) {
			// BotController.killProcess(pid);
			// System.out.println("Driver was null, quiting this");
			// }
			// }

			RunescapeActions runescapeWebsite = new RunescapeActions(driver, account, type, pidDriver);
			runescapeWebsite.create();
			ProtonMain proton = new ProtonMain(driver, account, pidDriver);
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

		else if (type == SeleniumType.RECOVER_ACCOUNT) {
			pidDriver.setType(type);
			// ALL_DRIVERS.add(pidDriver);

			// Killing all pids that were active but not found in the system

			// for (int pid : pids) {
			// PidDriver driv = getPidDriver(pid);
			// if (driv == null) {
			// BotController.killProcess(pid);
			// System.out.println("Driver was null, quiting this");
			// }
			// }

			RunescapeActions runescapeWebsite = new RunescapeActions(driver, account, type, pidDriver);
			runescapeWebsite.unlock();
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

	/**
	 * @return the usedUsernames
	 */
	public static synchronized List<AccountCreate> getUsedUsernames() {
		return usedUsernames;
	}

	/**
	 * @param usedUsernames
	 *            the usedUsernames to set
	 */
	public static synchronized void setUsedUsernames(ArrayList<AccountCreate> usedUsernames) {
		AccountCreationService.usedUsernames = usedUsernames;
	}

}
