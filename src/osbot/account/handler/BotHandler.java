package osbot.account.handler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import osbot.account.AccountStage;
import osbot.account.AccountStatus;
import osbot.account.LoginStatus;
import osbot.account.global.Config;
import osbot.bot.BotController;
import osbot.database.DatabaseUtilities;
import osbot.settings.CliArgs;
import osbot.settings.OsbotController;
import osbot.tables.AccountTable;

public class BotHandler {

	/**
	 * Queries {@code tasklist} if the process ID {@code pid} is running.
	 * 
	 * @param pid
	 *            the PID to check
	 * @return {@code true} if the PID is running, {@code false} otherwise
	 */
	public static boolean isProcessIdRunningOnWindows(int pid) {
		try {
			Runtime runtime = Runtime.getRuntime();
			String cmds[] = { "cmd", "/c", "tasklist /FI \"PID eq " + pid + "\"" };
			Process proc = runtime.exec(cmds);

			InputStream inputstream = proc.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line;
			while ((line = bufferedreader.readLine()) != null) {
				// Search the PID matched lines single line for the sequence: " 1300 "
				// if you find it, then the PID is still running.
				// System.out.println("Current pids: "+line);
				if (line.contains(" " + pid + " ")) {
					return true;
				}
			}

			return false;
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Cannot query the tasklist for some reason.");
		}

		return false;

	}

	/**
	 * Checks the processes
	 */
	public static void checkProcesses() {
		Iterator<Integer> it = BotController.getJavaPIDsWindows().iterator();

		while (it.hasNext()) {
			int nextPid = it.next();

			if (!isProcessIdRunningOnWindows(nextPid)) {
				System.out.println("Removed pid: " + nextPid + " from the processes list, was no longer running");
				it.remove();
			}
		}

	}

	/**
	 * Runs a bot
	 * 
	 * @param bot
	 */
	public static void runBot(OsbotController bot) {
		if (bot == null) {
			System.out.println("Invalid bot");
			return;
		}
		AccountTable account = bot.getAccount();

		// bot.addArguments(CliArgs.DEBUG, false, 5005);
		bot.addArguments(CliArgs.LOGIN, true, Config.OSBOT_USERNAME, Config.OSBOT_PASSWORD);
		bot.addArguments(CliArgs.DATA, false, 0);
		bot.addArguments(CliArgs.WORLD, false, account.getWorld());

		// if (!account.getScript().equalsIgnoreCase(AccountStage.TUT_ISLAND.name())) {
		bot.addArguments(CliArgs.ALLOW, false, "norandoms");
		// }

		if (account.hasUsernameAndPasswordAndBankpin()) {
			bot.addArguments(CliArgs.BOT, true, account.getEmail(), account.getPassword(), account.getBankPin());
		} else if (account.hasUsernameAndPassword()) {
			bot.addArguments(CliArgs.BOT, true, account.getEmail(), account.getPassword(), "0000");
		}
		bot.addArguments(CliArgs.MEM, false, 2048);
		if (account.hasValidProxy()) {
			bot.addArguments(CliArgs.PROXY, true, account.getProxyIp(), account.getProxyPort(),
					account.getProxyUsername(), account.getProxyPassword());
		}
		if (account.hasScript()) {
			String accountStatus = bot.getAccount().getStatus().name().replaceAll("_", "-");
			bot.addArguments(CliArgs.SCRIPT, true, account.getScript(), account.getEmail() + "_" + account.getPassword()
					+ "_" + bot.getPidId() + "_" + accountStatus + "_" + account.getUsername());
		}
		bot.setStartTime(System.currentTimeMillis());
		DatabaseUtilities.updateLoginStatus(LoginStatus.INITIALIZING, bot.getId());
		bot.runBot(false);
	}

	public static void runMule(OsbotController bot, String toTradeWith, String emailOfUserTradingWith) {
		if (bot == null) {
			System.out.println("Invalid bot");
			return;
		}
		AccountTable account = bot.getAccount();

		// bot.addArguments(CliArgs.DEBUG, false, 5005);
		bot.addArguments(CliArgs.LOGIN, true, Config.OSBOT_USERNAME, Config.OSBOT_PASSWORD);
		bot.addArguments(CliArgs.DATA, false, 0);
		bot.addArguments(CliArgs.WORLD, false, 394);

		// if (!account.getScript().equalsIgnoreCase(AccountStage.TUT_ISLAND.name())) {
		bot.addArguments(CliArgs.ALLOW, false, "norandoms");
		// }

		if (account.hasUsernameAndPasswordAndBankpin()) {
			bot.addArguments(CliArgs.BOT, true, account.getEmail(), account.getPassword(), account.getBankPin());
		} else if (account.hasUsernameAndPassword()) {
			bot.addArguments(CliArgs.BOT, true, account.getEmail(), account.getPassword(), "0000");
		}
		bot.addArguments(CliArgs.MEM, false, 2048);
		if (account.hasValidProxy()) {
			bot.addArguments(CliArgs.PROXY, true, account.getProxyIp(), account.getProxyPort(),
					account.getProxyUsername(), account.getProxyPassword());
		}
		if (account.hasScript()) {
			String accountStatus = bot.getAccount().getStage().name().replaceAll("_", "-");
			bot.addArguments(CliArgs.SCRIPT, true, "MULE_TRADING", account.getEmail() + "_" + account.getPassword()
					+ "_" + bot.getPidId() + "_" + accountStatus + "_" + toTradeWith + "_" + emailOfUserTradingWith);
		}
		bot.setStartTime(System.currentTimeMillis());
		DatabaseUtilities.updateLoginStatus(LoginStatus.INITIALIZING, bot.getId());
		bot.runBot(true);
	}

	/**
	 * Returns the amount of bots that are currently active
	 * 
	 * @return
	 */
	public static int getAmountOfBotsActive() {
		return BotController.getJavaPIDsWindows().size();

	}

	/**
	 * Kill all bots
	 */
	public static void killAllBots() {
		for (int pid : BotController.getJavaPIDsWindows()) {
			BotController.killProcess(pid);
		}
	}

	/**
	 * Gets the mule partner
	 * 
	 * @param bot
	 * @return
	 */
	public static OsbotController getMulePartner(OsbotController bot) {
		for (int b = 0; b < BotController.getBots().size(); b++) {
			OsbotController osbot2 = BotController.getBots().get(b);

			if (osbot2.getAccount().getTradeWithOther() != null && bot.getAccount().getTradeWithOther() != null
					&& !osbot2.getAccount().getUsername().equalsIgnoreCase(bot.getAccount().getUsername())
					&& osbot2.getAccount().getTradeWithOther().equalsIgnoreCase(bot.getAccount().getTradeWithOther())) {
				return osbot2;
			}
		}
		return null;
	}

	public static OsbotController getOsbotByName(String name) {
		for (int b = 0; b < BotController.getBots().size(); b++) {
			OsbotController osbot2 = BotController.getBots().get(b);

			if (osbot2.getAccount().getUsername().equalsIgnoreCase(name)) {
				return osbot2;
			}
		}
		return null;
	}

	/**
	 * Handling with running the mules
	 * 
	 * Setting them up to yet
	 */
	public static void handleMules() {
		OsbotController availableMule = null;

		// If accounts are not loaded yet
		if (BotController.getBots().size() == 0) {
			System.out.println("[MULE TRADING] Couldn't trade yet, accounts weren't loaded yet");
		}
		for (int i = 0; i < BotController.getBots().size(); i++) {
			OsbotController osbot = BotController.getBots().get(i);
			String tradeWithOther = DatabaseUtilities.getTradeWithOther(osbot.getId());
			OsbotController partner = getOsbotByName(tradeWithOther);

			if (tradeWithOther != null && (osbot.getAccount().getStage() == AccountStage.MULE_TRADING
					|| osbot.getAccount().getStage() == AccountStage.UNKNOWN)) {

				// Cant find the partner
				if (partner == null) {
					System.out.println("Couldn't find the partner to trade: E01");
					DatabaseUtilities.setTradingWith(null, osbot.getAccount().getUsername());
					DatabaseUtilities.setTradingWith(null, osbot.getId());
				}

				if (partner != null) {
					String tradeWithOther2 = DatabaseUtilities.getTradeWithOther(partner.getId());
					if (!osbot.getAccount().getUsername().equalsIgnoreCase(tradeWithOther2)
							|| !partner.getAccount().getUsername().equalsIgnoreCase(tradeWithOther)) {
						System.out.println("Accounts didn't trade eachother! E04");
						DatabaseUtilities.setTradingWith(null, osbot.getAccount().getUsername());
						DatabaseUtilities.setTradingWith(null, tradeWithOther);
					}
				}

				if (osbot.getAccount().getStatus() != AccountStatus.MULE
						&& osbot.getAccount().getStatus() != AccountStatus.AVAILABLE) {
					System.out.println("One of the accounts got banned! E03");
					DatabaseUtilities.setTradingWith(null, osbot.getAccount().getUsername());
					DatabaseUtilities.setTradingWith(null, tradeWithOther);
				}

				// Don't match
				if (osbot != null && tradeWithOther.equalsIgnoreCase(osbot.getAccount().getUsername())) {
					DatabaseUtilities.setTradingWith(null, tradeWithOther);
					System.out.println("Don't correspond: E02");
				}

			}

		}

		for (int i = 0; i < BotController.getBots().size(); i++) {
			OsbotController osbot = BotController.getBots().get(i);

			// Looking for a mule account and that is currently available ('NULL')
			if (osbot.getAccount().getStatus() == AccountStatus.MULE && osbot.getAccount().getTradeWithOther() == null
					&& osbot.getAccount().getStage() == AccountStage.UNKNOWN
					&& DatabaseUtilities.getTradeWithOther(osbot.getId()) == null
					&& DatabaseUtilities.getAmountOfMuleTrades(osbot.getAccount().getUsername()) == 0) {
				availableMule = osbot;
				System.out.println("[MULE TRADING] Found an account! " + availableMule.getAccount().getUsername());
			}
		}

		// Must have a mule available to continue
		if (BotController.getBots().size() > 0 && availableMule == null) {
			System.out.println("[MULE TRADING] Couldn't find mule to trade with, is not available");
			return;
		}

		for (int i = 0; i < BotController.getBots().size(); i++) {
			OsbotController osbot = BotController.getBots().get(i);

			// If an account wants to mule
			// Not with himself and must be status MULE_TRADING
			if (DatabaseUtilities.getTradeWithOther(osbot.getId()) == null
					&& (osbot.getAccount().getStage() == AccountStage.MULE_TRADING
							&& osbot.getAccount().getStatus() == AccountStatus.AVAILABLE)
					&& !osbot.getAccount().getUsername().equalsIgnoreCase(availableMule.getAccount().getUsername())) {

				// Setting in database that the mule is trading with the workers name
				DatabaseUtilities.setTradingWith(osbot.getAccount().getUsername(), availableMule.getId());

				System.out.println("[MULE TRADING] Starting mule: " + availableMule.getAccount().getUsername()
						+ " to trade with: " + osbot.getAccount().getUsername());

				// Setting in database that worker is trading with mule
				DatabaseUtilities.setTradingWith(availableMule.getAccount().getUsername(), osbot.getId());

				System.out.println("[MULE TRADING] Starting worker: " + osbot.getAccount().getUsername()
						+ " to trade with mule: " + availableMule.getAccount().getUsername());

				break;
			}
		}

	}

	private static boolean wantsToMule() {
		for (int i = 0; i < BotController.getBots().size(); i++) {
			OsbotController osbot = BotController.getBots().get(i);

			if (osbot.getAccount().getTradeWithOther() != null && osbot.getAccount().getTradeWithOther().length() > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Handlig all the bots, deciding how many should be open etc.
	 */
	public static void handleBots() {
		// Will check the PID processes if they are still running or not, when not, they
		// get deleted in the PID list
		BotHandler.checkProcesses();

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(new Date());

		System.out.println("[BOT HANDLER MANAGEMENT] Bots currently active: " + getAmountOfBotsActive());

		for (int i = 0; i < BotController.getBots().size(); i++) {
			OsbotController osbot = BotController.getBots().get(i);

			// Running mules
			if (osbot != null && osbot.getAccount().getTradeWithOther() != null
					&& osbot.getAccount().getTradeWithOther().length() > 0
					&& !BotController.containsInPidList(osbot.getPidId())) {

				if (!DatabaseUtilities.getAccountStageInDatabase(osbot.getId())
						.equalsIgnoreCase(AccountStage.UNKNOWN.name())
						&& !DatabaseUtilities.getAccountStageInDatabase(osbot.getId())
								.equalsIgnoreCase(AccountStage.MULE_TRADING.name())) {

					System.out.println("Account stage wasn't to mule trade");
					DatabaseUtilities.setTradingWith(null, osbot.getId());
					break;
				}

				runMule(osbot, osbot.getAccount().getTradeWithOther(),
						DatabaseUtilities.getEmailFromUsername(osbot.getAccount().getTradeWithOther()));
				System.out.println("Running mule trading " + osbot.getAccount().getUsername());

				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if (getAmountOfBotsActive() < Config.MAX_BOTS_OPEN) {

			for (int i = 0; i < BotController.getBots().size(); i++) {
				OsbotController osbot = BotController.getBots().get(i);

				if (osbot != null
						&& (osbot.getAccount().getStatus() == AccountStatus.AVAILABLE
								|| osbot.getAccount().getStatus() == AccountStatus.WALKING_STUCK
								|| (osbot.getAccount().getStatus() == AccountStatus.TASK_TIMEOUT && osbot.getAccount()
										.getAmountTimeout() < Config.AMOUNT_OF_TIMEOUTS_BEFORE_GONE)
								|| (osbot.getAccount().getStatus() == AccountStatus.TIMEOUT && osbot.getAccount()
										.getAmountTimeout() < Config.AMOUNT_OF_TIMEOUTS_BEFORE_GONE))
						&& osbot.getAccount().getStage() != AccountStage.UNKNOWN
						&& osbot.getAccount().getStage() != AccountStage.MULE_TRADING
						&& !BotController.containsInPidList(osbot.getPidId())
						&& getAmountOfBotsActive() < Config.MAX_BOTS_OPEN) {

					if (!calendar2.after(osbot.getAccount().getDate())) {
						System.out.println(
								"Skipping " + osbot.getAccount().getUsername() + " because has currently a break");
						continue;
					}
					if (wantsToMule()) {
						System.out.println("A bot wants to mule, so giving them priority");
						continue;
					}

					runBot(osbot);
					System.out.println("[BOT HANDLER MANAGEMENT] Running bot name: " + osbot.getAccount().getStage()
							+ " " + osbot.getAccount().getUsername());
					try {
						Thread.sleep(15000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (osbot == null) {
					System.out.println(osbot + " got null");
				} else if (getAmountOfBotsActive() >= Config.MAX_BOTS_OPEN) {
					System.out.println(
							"[BOT HANDLER MANAGEMENT] Maximum amount of bots currently online for this machine reached");
				}
			}
		}
	}
}
