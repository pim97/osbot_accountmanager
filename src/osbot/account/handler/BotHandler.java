package osbot.account.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import osbot.account.AccountStage;
import osbot.account.AccountStatus;
import osbot.account.LoginStatus;
import osbot.account.global.Config;
import osbot.account.worlds.World;
import osbot.account.worlds.WorldType;
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

	public static long MAIN_PID = -1;

	public static void sortByStage() {
		Collections.sort(BotController.getBots(), new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				OsbotController bot1 = (OsbotController) o1;
				OsbotController bot2 = (OsbotController) o2;
				return bot1.getAccount().getStage().ordinal() - bot2.getAccount().getStage().ordinal();
			}
		});
	}

	public static void resetVariables(OsbotController bot) {
		// if (!bot.isStartingUp()) {
		// BotController.killProcess(bot.getPidId());
		// bot.setPidId(-1);
		// bot.setStartTime(-1);
		// DatabaseUtilities.updateLoginStatus(LoginStatus.DEFAULT, bot.getId());
		// System.out.println("Kiellingz2");
		// }
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

		if (bot.isStartingUp()) {
			System.out.println("[ERROR] Bot is already running!");
			return;
		}

		AccountTable account = bot.getAccount();

		bot.setCliArgs(new StringBuilder());

		World randomWorld = World.getRandomWorldWithLessPopulation(WorldType.F2P, 20);

		new Thread(() -> DatabaseUtilities.updateLoginStatus(LoginStatus.INITIALIZING, bot.getId())).start();
		bot.setStartingUp(true);
		bot.setStartTime(System.currentTimeMillis());

		// bot.addArguments(CliArgs.DEBUG, false, 5005);
		bot.addArguments(CliArgs.LOGIN, true, Config.OSBOT_USERNAME, Config.OSBOT_PASSWORD);
		bot.addArguments(CliArgs.DATA, false, 0);
		bot.addArguments(CliArgs.WORLD, false, randomWorld.getNumber());

		// if (!account.getScript().equalsIgnoreCase(AccountStage.TUT_ISLAND.name())) {
		if (Config.LOW_CPU) {
			// if (bot.getAccount().getStage() == AccountStage.GE_SELL_BUY_MINING) {
			// bot.addArguments(CliArgs.ALLOW, false, "norandoms");
			// } else {
			bot.addArguments(CliArgs.ALLOW, false, "norandoms,lowcpu");
			// }
		} else {
			bot.addArguments(CliArgs.ALLOW, false, "norandoms");
		}
		// }

		if (account.hasUsernameAndPasswordAndBankpin()) {
			bot.addArguments(CliArgs.BOT, true, account.getEmail(), account.getPassword(), account.getBankPin());
		} else if (account.hasUsernameAndPassword()) {
			bot.addArguments(CliArgs.BOT, true, account.getEmail(), account.getPassword(), "0000");
		}
		bot.addArguments(CliArgs.MEM, false, 1250);
		if (account.hasValidProxy()) {
			bot.addArguments(CliArgs.PROXY, true, account.getProxyIp(), account.getProxyPort(),
					account.getProxyUsername(), account.getProxyPassword());
		}
		if (account.hasScript()) {
			String accountStatus = bot.getAccount().getStatus().name().replaceAll("_", "-");
			bot.addArguments(CliArgs.SCRIPT, true, account.getScript(),
					account.getEmail() + "_" + account.getPassword() + "_" + bot.getPidId() + "_" + accountStatus + "_"
							+ account.getUsername() + "_" + Config.DATABASE_USER_NAME.replaceAll("_", "%") + "_"
							+ Config.DATABASE_NAME.replaceAll("_", "%") + "_"
							+ Config.DATABASE_PASSWORD.replaceAll("_", "%"));
		}
		bot.runBot(false);
	}

	public static void runMule(OsbotController bot, String toTradeWith, String emailOfUserTradingWith) {
		if (bot == null) {
			System.out.println("Invalid bot");
			return;
		}
		if (bot.isStartingUp()) {
			System.out.println("[ERROR] Bot is already running!");
			return;
		}

		AccountTable account = bot.getAccount();

		bot.setCliArgs(new StringBuilder());

		bot.setStartingUp(true);
		bot.setStartTime(System.currentTimeMillis());

		// System.out.println("1");
		new Thread(() -> DatabaseUtilities.updateLoginStatus(LoginStatus.INITIALIZING, bot.getId())).start();

		// bot.addArguments(CliArgs.DEBUG, false, 5005);
		bot.addArguments(CliArgs.LOGIN, true, Config.OSBOT_USERNAME, Config.OSBOT_PASSWORD);
		bot.addArguments(CliArgs.DATA, false, 0);
		bot.addArguments(CliArgs.WORLD, false, 453);

		// System.out.println("2");
		if (Config.LOW_CPU) {
			// if (!account.getScript().equalsIgnoreCase(AccountStage.TUT_ISLAND.name())) {
			bot.addArguments(CliArgs.ALLOW, false, "norandoms");
		} else {
			bot.addArguments(CliArgs.ALLOW, false, "norandoms");
		}
		// }

		if (account.hasUsernameAndPasswordAndBankpin()) {
			bot.addArguments(CliArgs.BOT, true, account.getEmail(), account.getPassword(), account.getBankPin());
		} else if (account.hasUsernameAndPassword()) {
			bot.addArguments(CliArgs.BOT, true, account.getEmail(), account.getPassword(), "0000");
		}
		// System.out.println("3");
		bot.addArguments(CliArgs.MEM, false, 1250);
		if (account.hasValidProxy()) {
			bot.addArguments(CliArgs.PROXY, true, account.getProxyIp(), account.getProxyPort(),
					account.getProxyUsername(), account.getProxyPassword());
		}
		if (account.hasScript()) {
			String osbotPartner = DatabaseUtilities.getTradeWithOther(bot.getId());
			OsbotController partner = BotController.getBotByAccountName(osbotPartner);
			System.out.println("The partner found is: " + partner);
			String script = "MULE_TRADING";

			boolean hasPartner = partner != null && partner.getAccount() != null
					&& partner.getAccount().getStatus() != null;
			boolean isRightAccountStatus = (hasPartner)
					&& (partner.getAccount().getStatus() == AccountStatus.SUPER_MULE
							&& bot.getAccount().getStatus() == AccountStatus.MULE)
					|| ((bot.getAccount().getStatus() == AccountStatus.SUPER_MULE
							&& partner.getAccount().getStatus() == AccountStatus.MULE));

			if (hasPartner && isRightAccountStatus) {
				script = "SUPERMULE_TRADING";
			} else {
				System.out.println("has partner: " + hasPartner);
				System.out.println("is right acc status: " + isRightAccountStatus);
			}

			System.out.println("Excecuting script with: " + script);

			// System.out.println("4");
			String accountStatus = bot.getAccount().getStage().name().replaceAll("_", "-");
			bot.addArguments(CliArgs.SCRIPT, true, script, account.getEmail() + "_" + account.getPassword() + "_"
					+ bot.getPidId() + "_" + accountStatus + "_" + Config.DATABASE_USER_NAME.replaceAll("_", "%") + "_"
					+ Config.DATABASE_NAME.replaceAll("_", "%") + "_" + Config.DATABASE_PASSWORD.replaceAll("_", "%")
					+ "_" + toTradeWith + "_" + emailOfUserTradingWith);
		}

		// System.out.println("5");
		if (bot.getAccount().getStatus() == AccountStatus.MULE
				|| bot.getAccount().getStatus() == AccountStatus.SUPER_MULE) {
			createBatFile(bot);
		}
		bot.runBot(true);
	}

	public static void createBatFile(OsbotController bot) {
		if (!Config.CREATE_BATCH_FILES_FOR_MULES) {
			System.out.println("Not creating a batch file");
			return;
		}
		String fileName = "mule/" + bot.getAccount().getEmail() + "_" + bot.getAccount().getUsername() + ".bat";
		File file = new File(fileName);
		if (!file.exists()) {
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(fileName, "UTF-8");
			} catch (FileNotFoundException | UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (writer != null) {
				writer.println(bot.getCliArgs().toString());
				writer.close();
			}
		}
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
		OsbotController availableSuperMule = null;

		// If accounts are not loaded yet
		if (BotController.getBots().size() == 0) {
			System.out.println("[MULE TRADING] Couldn't trade yet, accounts weren't loaded yet");
		}
		for (int i = 0; i < BotController.getBots().size(); i++) {

			OsbotController osbot = BotController.getBots().get(i);

			if ((osbot.getAccount().getStage() == AccountStage.MULE_TRADING
					|| (osbot.getAccount().getStage() == AccountStage.UNKNOWN)
							&& (osbot.getAccount().getStatus() == AccountStatus.MULE
									|| osbot.getAccount().getStatus() == AccountStatus.SUPER_MULE))) {

				String tradeWithOther = DatabaseUtilities.getTradeWithOther(osbot.getId());
				OsbotController partner = getOsbotByName(tradeWithOther);

				if (tradeWithOther != null) {

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
							&& osbot.getAccount().getStatus() != AccountStatus.AVAILABLE
							&& osbot.getAccount().getStatus() != AccountStatus.SUPER_MULE) {
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

		}

		for (int i = 0; i < BotController.getBots().size(); i++) {
			OsbotController osbot = BotController.getBots().get(i);

			if ((osbot.getAccount().getStatus() == AccountStatus.MULE
					|| osbot.getAccount().getStatus() == AccountStatus.SUPER_MULE)
					&& osbot.getAccount().getStage() == AccountStage.UNKNOWN) {

				// Looking for a mule account and that is currently available ('NULL')
				if (osbot.getAccount().getTradeWithOther() == null
						&& DatabaseUtilities.getTradeWithOther(osbot.getId()) == null
						&& DatabaseUtilities.getAmountOfMuleTrades(osbot.getAccount().getUsername()) == 0) {

					if (osbot.getAccount().getStatus() == AccountStatus.SUPER_MULE) {
						availableSuperMule = osbot;
						System.out.println(
								"[MULE TRADING] Found an account! " + availableSuperMule.getAccount().getUsername());
					} else if (osbot.getAccount().getStatus() == AccountStatus.MULE) {
						availableMule = osbot;
						System.out.println(
								"[MULE TRADING] Found an account! " + availableMule.getAccount().getUsername());
					}
				}
			}
		}

		// Must have a mule available to continue
		if (BotController.getBots().size() > 0 && availableSuperMule == null) {
			System.out.println("[MULE TRADING] Couldn't find super-mule to trade with, is not available");
		} else {
			for (int i = 0; i < BotController.getBots().size(); i++) {
				OsbotController osbot = BotController.getBots().get(i);

				if (osbot.getAccount().getStage() == AccountStage.UNKNOWN
						&& osbot.getAccount().getStatus() == AccountStatus.MULE
						&& Integer.parseInt(osbot.getAccount().getAccountValue().replaceAll(",", "")) > 1_000_000) {

					// If an account wants to mule
					// Not with himself and must be status MULE_TRADING
					if (DatabaseUtilities.getTradeWithOther(osbot.getId()) == null && !osbot.getAccount().getUsername()
							.equalsIgnoreCase(availableSuperMule.getAccount().getUsername())) {

						// Setting in database that the mule is trading with the workers name
						DatabaseUtilities.setTradingWith(osbot.getAccount().getUsername(), availableSuperMule.getId());

						System.out.println(
								"[MULE TRADING] Starting super-mule: " + availableSuperMule.getAccount().getUsername()
										+ " to trade with: " + osbot.getAccount().getUsername());

						// Setting in database that worker is trading with mule
						DatabaseUtilities.setTradingWith(availableSuperMule.getAccount().getUsername(), osbot.getId());

						System.out.println("[MULE TRADING] Starting normal-mule: " + osbot.getAccount().getUsername()
								+ " to trade with mule: " + availableSuperMule.getAccount().getUsername());

						break;
					}
				}
			}
		}

		// Must have a mule available to continue
		if (BotController.getBots().size() > 0 && availableMule == null) {
			System.out.println("[MULE TRADING] Couldn't find mule to trade with, is not available");
		} else {
			for (int i = 0; i < BotController.getBots().size(); i++) {
				OsbotController osbot = BotController.getBots().get(i);

				if (osbot.getAccount().getStage() == AccountStage.MULE_TRADING
						&& osbot.getAccount().getStatus() == AccountStatus.AVAILABLE) {

					// If an account wants to mule
					// Not with himself and must be status MULE_TRADING
					if (DatabaseUtilities.getTradeWithOther(osbot.getId()) == null && !osbot.getAccount().getUsername()
							.equalsIgnoreCase(availableMule.getAccount().getUsername())) {

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
		}

	}

	private static int wantsToGe() {
		int amount = 0;
		for (int i = 0; i < BotController.getBots().size(); i++) {
			OsbotController osbot = BotController.getBots().get(i);

			if (osbot.getAccount().getStage() == AccountStage.GE_SELL_BUY_MINING
					&& osbot.getAccount().getStatus() == AccountStatus.AVAILABLE && osbot.getPidId() <= 0) {
				amount++;
			}
		}
		return amount;
	}

	private static boolean wantsToMule() {
		for (int i = 0; i < BotController.getBots().size(); i++) {
			OsbotController osbot = BotController.getBots().get(i);

			if (osbot.getAccount().getTradeWithOther() != null && osbot.getAccount().getTradeWithOther().length() > 0) {
				if (!BotController.containsInPidList(osbot.getPidId())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Determines whether a muling is currently on tutorial island and wants to
	 * launch, thus giving it priority
	 * 
	 * @return
	 */
	private static OsbotController superMuleWantsToLaunchToFinishTutorialIsland() {
		for (int i = 0; i < BotController.getBots().size(); i++) {
			OsbotController osbot = BotController.getBots().get(i);

			boolean isRightAccount = osbot.getAccount().getStatus() == AccountStatus.AVAILABLE
					&& osbot.getAccount().getStage() == AccountStage.TUT_ISLAND;
			boolean isProxyAddress = Config.isSuperMuleProxy(osbot.getAccount().getProxyIp(),
					osbot.getAccount().getProxyPort())
					|| Config.isStaticMuleProxy(osbot.getAccount().getProxyIp(), osbot.getAccount().getProxyPort());
			boolean notStartedUpYet = !BotController.containsInPidList(osbot.getPidId());
			boolean mayLoginBecauseOfStatus = osbot.getAccount().getLoginStatus() != LoginStatus.LOGGED_IN;

			// System.out.println(Config.isMuleProxy(osbot.getAccount().getProxyIp(),
			// osbot.getAccount().getProxyPort()));
			// System.out.println(osbot.getAccount().getStatus() + " " +
			// osbot.getAccount().getStage());
			// System.out.println("rigth acc: " + isRightAccount);
			// System.out.println("proxy add: " + isProxyAddress);
			// System.out.println("not started up: " + notStartedUpYet);
			if (isRightAccount && isProxyAddress && mayLoginBecauseOfStatus) {
				if (notStartedUpYet) {
					return osbot;
				}
			}
		}
		return null;
	}

	public static boolean containsMiningAndNotRunning() {
		for (int i = 0; i < BotController.getBots().size(); i++) {
			OsbotController osbot = BotController.getBots().get(i);

			if (osbot != null
					&& ((osbot.getAccount().getStage() == AccountStage.MINING_IRON_ORE)
							|| (osbot.getAccount().getStage() == AccountStage.MINING_LEVEL_TO_15)
							|| (osbot.getAccount().getStage() == AccountStage.RIMMINGTON_IRON_ORE))
					&& osbot.getPidId() <= 0) {
				return true;
			}
		}
		return false;
	}

	public static boolean isMining(OsbotController osbot) {
		if (osbot.getAccount().getStage() == AccountStage.MINING_IRON_ORE
				|| osbot.getAccount().getStage() == AccountStage.MINING_LEVEL_TO_15
				|| osbot.getAccount().getStage() == AccountStage.RIMMINGTON_IRON_ORE) {
			return true;
		}
		return false;
	}

	public static void runMule() {
		List<OsbotController> botz = new ArrayList<OsbotController>();
		botz.addAll(BotController.getBots());

		// Reversering so the mule isn't the first one to launch (at most of the times),
		// but the worker
		Collections.reverse(botz);

		for (int i2 = 0; i2 < botz.size(); i2++) {
			OsbotController osbot2 = botz.get(i2);

			// if (osbot2 != null) {
			//
			// // When the account value is too low, then stop trading it
			// if (Integer.parseInt(osbot2.getAccount().getAccountValue()) <= 0
			// && osbot2.getAccount().getStage() == AccountStage.MULE_TRADING) {
			//
			// DatabaseUtilities.setTradingWith(null, osbot2.getId());
			// System.out.println("Account cash was too low to trade!");
			// break;
			// }
			// }

			if (osbot2 != null && osbot2.getAccount().getTradeWithOther() != null
					&& osbot2.getAccount().getTradeWithOther().length() > 0) {
				if (!BotController.containsInPidList(osbot2.getPidId())) {

					// Seperated because its checking in DB, taking long
					if (DatabaseUtilities.getLoginStatus(osbot2.getId()) == LoginStatus.DEFAULT) {

						if (osbot2.getAccount().getStage() != AccountStage.UNKNOWN
								&& osbot2.getAccount().getStage() != AccountStage.MULE_TRADING) {

							System.out.println("Account stage wasn't to mule trade");
							DatabaseUtilities.setTradingWith(null, osbot2.getId());
							break;
						}

						System.out.println("Running mule trading " + osbot2.getAccount().getUsername());
						runMule(osbot2, osbot2.getAccount().getTradeWithOther(),
								DatabaseUtilities.getEmailFromUsername(osbot2.getAccount().getTradeWithOther()));
					}
				}
			}
		}

		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Handlig all the bots, deciding how many should be open etc.
	 */
	public static void handleBots() {
		// long startTime = System.currentTimeMillis();

		// Will check the PID processes if they are still running or not, when not, they
		// get deleted in the PID list

		// System.out.println("HB: 1");
		int javaPidsSize = BotController.getJavaPIDsWindows().size();

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(new Date());

		sortByStage();

		System.out.println("[BOT HANDLER MANAGEMENT] Bots currently active: " + getAmountOfBotsActive());

		if (getAmountOfBotsActive() < Config.MAX_BOTS_OPEN) {

			for (int i = 0; i < BotController.getBots().size(); i++) {
				OsbotController osbot = BotController.getBots().get(i);

				if (wantsToMule()) {
					System.out.println("A bot wants to mule, so giving them priority");
					runMule();
					continue;
				}

				OsbotController superMule = superMuleWantsToLaunchToFinishTutorialIsland();
				if (superMule != null) {
					System.out.println(
							"Skipping this one, because a tutorial island mule/super mule has to launch first");
					runBot(superMule);
					continue;
				}

				System.out.println("HB: 9 " + osbot.getAccount().getStatus() + " " + osbot.getAccount().getStage());

				System.out.println("login  status:" + DatabaseUtilities.getLoginStatus(osbot.getId()));

				boolean correctStatusToLaunch = (osbot.getAccount().getStatus() == AccountStatus.AVAILABLE)
						|| (osbot.getAccount().getStatus() == AccountStatus.WALKING_STUCK)
						|| (osbot.getAccount().getStatus() == AccountStatus.TASK_TIMEOUT
								&& osbot.getAccount().getAmountTimeout() < Config.AMOUNT_OF_TIMEOUTS_BEFORE_GONE)
						|| (osbot.getAccount().getStatus() == AccountStatus.TIMEOUT
								&& osbot.getAccount().getAmountTimeout() < Config.AMOUNT_OF_TIMEOUTS_BEFORE_GONE);
				boolean correctStagetoLaunch = osbot.getAccount().getStage() != AccountStage.UNKNOWN
						&& osbot.getAccount().getStage() != AccountStage.OUT_OF_MONEY
						&& osbot.getAccount().getStage() != AccountStage.MULE_TRADING
						&& osbot.getAccount().getStage() != AccountStage.GE_SELL_BUY_MINING;
				boolean enoughSpaceToLaunch = javaPidsSize < Config.MAX_BOTS_OPEN;
				boolean hasValidEmail = osbot.getAccount().getEmail() != null;
				boolean hasValidLoginStatus = osbot.getAccount().getLoginStatus() == LoginStatus.DEFAULT;

				if (osbot != null && correctStatusToLaunch && correctStagetoLaunch && enoughSpaceToLaunch
						&& hasValidEmail && hasValidLoginStatus) {
					boolean containsInProgram = BotController.containsInPidList(osbot.getPidId());

					if (containsInProgram) {
						continue;
					}
					
					if (!calendar2.after(osbot.getAccount().getDate())) {
						System.out.println(
								"Skipping " + osbot.getAccount().getUsername() + " because has currently a break");
						continue;
					}

					runBot(osbot);
					System.out.println("[BOT HANDLER MANAGEMENT] Running bot name: " + osbot.getAccount().getStage()
							+ " " + osbot.getAccount().getUsername());

					// System.out.println("HB: 12");
				} else if (osbot == null) {
					System.out.println(osbot + " got null");
				} else if (getAmountOfBotsActive() >= Config.MAX_BOTS_OPEN) {
					System.out.println(
							"[BOT HANDLER MANAGEMENT] Maximum amount of bots currently online for this machine reached");
				}

				// System.out.println("HB: 13");
			}
			// System.out.println("HB: 14");
		}
		// System.out.println("HB: 15");
	}
}
