package osbot.threads;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Select;

import osbot.account.creator.PidDriver;
import osbot.account.global.Config;
import osbot.account.gmail.protonmail.ProtonMain;
import osbot.account.handler.GeckoHandler;
import osbot.bot.BotController;
import osbot.database.DatabaseUtilities;
import osbot.settings.OsbotController;
import osbot.tables.AccountTable;

public class Test {

	public static void load() {
		ArrayList<AccountTable> account = DatabaseUtilities.getAccountsFromMysqlConnection();

		if (account.size() > 0) {
			int botListSize = BotController.getBots().size();

			for (AccountTable acc : account) {
				AccountTable accTable = new AccountTable(acc.getId(), acc.getScript(), acc.getUsername(),
						acc.getWorld(), acc.getProxyIp(), acc.getProxyPort(), acc.isLowCpuMode(), acc.getStatus(),
						acc.getStage(), acc.getAccountStageProgress());
				accTable.setQuestPoints(acc.getQuestPoints());
				accTable.setPassword(acc.getPassword());
				accTable.setBankPin(acc.getBankPin());
				accTable.setDay(acc.getDay());
				accTable.setMonth(acc.getMonth());
				accTable.setYear(acc.getYear());
				accTable.setEmail(acc.getEmail());
				accTable.setAccountValue(acc.getAccountValue());
				accTable.setDate(acc.getDate());
				accTable.setDateString(acc.getDateString());
				accTable.setTradeWithOther(acc.getTradeWithOther());
				accTable.setProxyUsername(acc.getProxyUsername());
				accTable.setProxyPassword(acc.getProxyPassword());
				accTable.setAmountTimeout(acc.getAmountTimeout());

				if (botListSize == 0) {
					BotController.addBot(new OsbotController(acc.getId(), acc));
				} else {
					OsbotController bot = BotController.getBotById(acc.getId());
					if (bot != null) {
						bot.setAccount(accTable);
					} else if (bot == null) {
						BotController.addBot(new OsbotController(acc.getId(), acc));
					}
				}
			}

		}

	}

	public static void test() {
		long begin = System.currentTimeMillis();

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
		// List<Integer> pids = GeckoHandler.getGeckodriverExeWindows();
		// List<Integer> pidsAfter = null;

		WebDriver driver = new FirefoxDriver(option);

		// int tries = 0;
		// boolean searching = true;
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
		OsbotController account = BotController.getBots().get(0);
		account.getAccount().setEmail("alphabearman+796108@protonmail.com");
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

		System.out.println("launched in " + ((System.currentTimeMillis() - begin) / 1000) + " seconds");
		PidDriver pidDriver = new PidDriver(driver, pidId);

		Dimension n = new Dimension(1000, 700);
		driver.manage().window().setSize(n);

		ProtonMain proton = new ProtonMain(driver, BotController.getBots().get(0), pidDriver);
		proton.unlockAccount();
	}

	public static void main(String args[]) {
		Config.DATABASE_NAME = args[2];
		Config.DATABASE_USER_NAME = args[3];

		if (args[4].equalsIgnoreCase("null")) {
			Config.DATABASE_PASSWORD = "";
		} else {
			Config.DATABASE_PASSWORD = args[4];
		}

		Config.DATABASE_IP = args[5];

		System.out.println("DATABASE SETTIGNS: ");
		System.out.println("Database username: " + Config.DATABASE_USER_NAME);
		System.out.println("Database name: " + Config.DATABASE_NAME);
		System.out.println("Database password: " + Config.DATABASE_PASSWORD);
		System.out.println("Database IP: " + Config.DATABASE_IP);

		load();

		System.out.println(BotController.getBots().size());

		String sql = "SELECT * FROM (\r\n"
				+ "SELECT * FROM (SELECT ac.*, p.is_alive as alive, p.username as p_us, p.password as p_pass FROM account AS ac INNER JOIN proxies AS p ON p.ip_addres=ac.proxy_ip WHERE (ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND ac.status <> \"OUT_OF_MONEY\" AND email IS NOT NULL AND ac.status <> \"LOCKED_INGAME\" AND ac.status <> \"BANNED\"  AND ac.status <> \"TIMEOUT\" AND ac.status <> \"TASK_TIMEOUT\"  AND ac.status <> \"INVALID_PASSWORD\") OR (ac.status=\"TASK_TIMEOUT\" AND amount_timeout < "
				+ Config.AMOUNT_OF_TIMEOUTS_BEFORE_GONE + ") OR (ac.status=\"TIMEOUT\" AND amount_timeout < "
				+ Config.AMOUNT_OF_TIMEOUTS_BEFORE_GONE + ")) as z GROUP BY z.id\r\n" + ") as z\r\n" + "\r\n"
				+ "UNION\r\n" + "\r\n" + "SELECT * FROM (\r\n"
				+ "SELECT * FROM (SELECT ac.*, p.is_alive as alive, p.username as p_us, p.password as p_pass FROM server_muling.account AS ac INNER JOIN server_muling.proxies AS p ON p.ip_addres=ac.proxy_ip WHERE (ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND ac.status <> \"OUT_OF_MONEY\" AND email IS NOT NULL AND ac.status <> \"LOCKED_INGAME\" AND ac.status <> \"BANNED\"  AND ac.status <> \"TIMEOUT\" AND ac.status <> \"TASK_TIMEOUT\"  AND ac.status <> \"INVALID_PASSWORD\") OR (ac.status=\"TASK_TIMEOUT\" AND amount_timeout < "
				+ Config.AMOUNT_OF_TIMEOUTS_BEFORE_GONE + ") OR (ac.status=\"TIMEOUT\" AND amount_timeout < "
				+ Config.AMOUNT_OF_TIMEOUTS_BEFORE_GONE + ")) as z GROUP BY z.id\r\n" + ") as p";

		System.out.println(sql);

		// load();
		//
		// BotHandler.sortByStage();

		// for (OsbotController bot : BotController.getBots()) {
		// System.out.println(bot.getAccount().getStage());
		// }

		// System.out.println(Config.getRandomMuleProxyWithoutSuperMule());

		// World.getWorldsWithoutTotalRequirements(WorldType.F2P).stream().forEach(
		// world -> System.out.println(world.getType() + " " + world.getNumber() + " " +
		// world.getPlayerAmount()));

		// BotHandler.createBatFile(BotController.getBots().get(0));
		// BotHandler.runMule(BotController.getBots().get(0), "", "");

		// test();
	}
}
