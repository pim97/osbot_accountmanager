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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.safety.Cleaner;

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

		if (Config.isServerMuleProxy(bot.getAccount().getProxyIp(), bot.getAccount().getProxyPort())) {
			new Thread(
					() -> DatabaseUtilities.updateLoginStatus("server_muling", LoginStatus.INITIALIZING, bot.getId()))
							.start();
		} else {
			new Thread(() -> DatabaseUtilities.updateLoginStatus(LoginStatus.INITIALIZING, bot.getId())).start();
		}

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
		bot.addArguments(CliArgs.MEM, false, 1200);
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
		// if (bot.isStartingUp()) {
		// bot.setStartingUp(false);
		// System.out.println("[ERROR] Bot is already running!");
		// return;
		// }

		AccountTable account = bot.getAccount();

		bot.setCliArgs(new StringBuilder());

		bot.setStartingUp(true);
		bot.setStartTime(System.currentTimeMillis());

		// System.out.println("1");
		if (Config.isServerMuleProxy(bot.getAccount().getProxyIp(), bot.getAccount().getProxyPort())) {
			new Thread(
					() -> DatabaseUtilities.updateLoginStatus("server_muling", LoginStatus.INITIALIZING, bot.getId()))
							.start();
		} else {
			new Thread(() -> DatabaseUtilities.updateLoginStatus(LoginStatus.INITIALIZING, bot.getId())).start();
		}
		// bot.addArguments(CliArgs.DEBUG, false, 5005);
		bot.addArguments(CliArgs.LOGIN, true, Config.OSBOT_USERNAME, Config.OSBOT_PASSWORD);
		bot.addArguments(CliArgs.DATA, false, 0);
		bot.addArguments(CliArgs.WORLD, false, 453);

		// System.out.println("2");
		if (Config.LOW_CPU) {
			// if (!account.getScript().equalsIgnoreCase(AccountStage.TUT_ISLAND.name())) {
			bot.addArguments(CliArgs.ALLOW, false, "norandoms,lowcpu");
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
		bot.addArguments(CliArgs.MEM, false, 1200);
		if (account.hasValidProxy()) {
			bot.addArguments(CliArgs.PROXY, true, account.getProxyIp(), account.getProxyPort(),
					account.getProxyUsername(), account.getProxyPassword());
		}
		if (account.hasScript()) {
			String osbotPartner = null;

			osbotPartner = DatabaseUtilities.getTradeWithOther(bot.getId());
			if (osbotPartner == null) {
				osbotPartner = DatabaseUtilities.getTradeWithOther("server_muling", bot.getId());
			}

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
			boolean serverMuleTrading = (hasPartner)
					&& (partner != null && partner.getAccount().getStatus() == AccountStatus.SERVER_MULE
							&& bot.getAccount().getStatus() == AccountStatus.SUPER_MULE)
					|| ((bot.getAccount().getStatus() == AccountStatus.SERVER_MULE && partner != null
							&& partner.getAccount().getStatus() == AccountStatus.SUPER_MULE));

			if (hasPartner && isRightAccountStatus) {
				script = "SUPERMULE_TRADING";
			} else if (hasPartner && serverMuleTrading) {
				script = "SERVERMULE_TRADING";
			}
			// else if (!hasPartner && (isRightAccountStatus || serverMuleTrading)) {
			// System.out.println("Couldn't find partner, so stopping!");
			// return;
			// }
			else {
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
				writer.println(bot.getCliArgs().toString().replaceAll("MULE_TRADING", "LOGIN_TEST")
						.replaceAll("SUPERMULE_TRADING", "LOGIN_TEST"));
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

	public static void resetAllTradingDatabaseForServerMule(OsbotController otherBot, OsbotController partner) {
		DatabaseUtilities.setTradingWith("server_muling", null, otherBot.getId());
		DatabaseUtilities.setTradingWith("server_muling", null, partner.getId());

		DatabaseUtilities.setTradingWith(null, otherBot.getId());
		DatabaseUtilities.setTradingWith(null, partner.getId());

		DatabaseUtilities.setTradingWith(null, partner.getAccount().getUsername());
		DatabaseUtilities.setTradingWith(null, otherBot.getAccount().getUsername());
		DatabaseUtilities.setTradingWith("server_muling", null, partner.getAccount().getUsername());
		DatabaseUtilities.setTradingWith("server_muling", null, otherBot.getAccount().getUsername());

		DatabaseUtilities.setServerMuleConnectedDatabase(null);
	}

	private static void isServerMuleTrading() {
		boolean isCorrectDatabase = DatabaseUtilities.getServerMuleUsedByDatabase() == null ? false
				: DatabaseUtilities.getServerMuleUsedByDatabase().equalsIgnoreCase(Config.DATABASE_NAME);

		for (int i = 0; i < BotController.getBots().size(); i++) {

			OsbotController otherBot = BotController.getBots().get(i);

			if ((otherBot.getAccount().getStatus() == AccountStatus.SUPER_MULE
					|| otherBot.getAccount().getStatus() == AccountStatus.SERVER_MULE) && (isCorrectDatabase)) {

				String tradeWithOfServerMule = DatabaseUtilities.getTradeWithOther("server_muling", otherBot.getId());
				if (tradeWithOfServerMule == null) {
					tradeWithOfServerMule = DatabaseUtilities.getTradeWithOther(otherBot.getId());
				}

				if (tradeWithOfServerMule != null) {

					OsbotController partner = getOsbotByName(tradeWithOfServerMule);

					if (otherBot.getAccount().getStatus() == AccountStatus.BANNED) {
						DatabaseUtilities.setTradingWith(null, otherBot.getId());
						DatabaseUtilities.setTradingWith("server_muling", null, otherBot.getId());

						DatabaseUtilities.setTradingWith("server_muling", null, otherBot.getAccount().getUsername());
						DatabaseUtilities.setTradingWith(null, otherBot.getAccount().getUsername());
						System.out.println("Account was banned S04");

						DatabaseUtilities.setServerMuleConnectedDatabase(null);
					}

					if (partner == null) {
						System.out.println("Couldn't find the partner to trade: S01");
						DatabaseUtilities.setTradingWith(null, otherBot.getAccount().getUsername());
						DatabaseUtilities.setTradingWith("server_muling", null, otherBot.getAccount().getUsername());

						DatabaseUtilities.setTradingWith(null, otherBot.getId());
						DatabaseUtilities.setTradingWith("server_muling", null, otherBot.getId());

						DatabaseUtilities.setServerMuleConnectedDatabase(null);
					}

					if (partner != null) {

						if ((otherBot.getAccount().getStatus() == AccountStatus.SUPER_MULE
								&& partner.getAccount().getStatus() == AccountStatus.SERVER_MULE)
								|| (otherBot.getAccount().getStatus() == AccountStatus.SERVER_MULE
										&& partner.getAccount().getStatus() == AccountStatus.SUPER_MULE)) {

							// Done for correct server in database config for server muling (multiple
							// servers)
							if (partner.getAccount().getStatus() == AccountStatus.SERVER_MULE
									&& otherBot.getAccount().getStatus() == AccountStatus.SERVER_MULE
									&& DatabaseUtilities.getServerMuleUsedByDatabase() != null) {

								OsbotController serverMule = otherBot.getAccount()
										.getStatus() == AccountStatus.SERVER_MULE ? otherBot : partner;

								String tradeWithOther3 = DatabaseUtilities.getTradeWithOther("server_muling",
										serverMule.getId());

								if (tradeWithOther3 == null) {
									DatabaseUtilities.setServerMuleConnectedDatabase(null);
									System.out.println(
											"Set database to NULL in server_muling, because something went wrong!");
								}
							}

							// Less than 5M coins on the acc -> reset it
							if ((otherBot.getAccount().getStatus() == AccountStatus.SUPER_MULE
									|| partner.getAccount().getStatus() == AccountStatus.SUPER_MULE)) {
								OsbotController superMule = otherBot.getAccount()
										.getStatus() == AccountStatus.SUPER_MULE ? otherBot : partner;

								if (superMule != null
										&& Integer.parseInt(superMule.getAccount().getAccountValue()) <= 2_500_000
										&& !superMule.getAccount().isUpdated()) {
									System.out.println("Account value was less than 2.5 million! S05");
									resetAllTradingDatabaseForServerMule(otherBot, partner);
									superMule.getAccount().setUpdated(true);
								}
							}

							String tradeWithOfSuperMule = DatabaseUtilities.getTradeWithOther(partner.getId());
							// System.out.println(
							// otherBot.getAccount().getUsername() + " " + otherBot.getAccount().getStatus()
							// + " "
							// + tradeWithOfSuperMule + " " + partner.getId());
							if (tradeWithOfSuperMule == null) {
								tradeWithOfSuperMule = DatabaseUtilities.getTradeWithOther("server_muling",
										partner.getId());
							}

							if ((tradeWithOfServerMule != null && tradeWithOfSuperMule == null)
									|| (tradeWithOfSuperMule != null && tradeWithOfServerMule == null)) {
								System.out.println("One account had a trade and the other not, restarting S03");
								resetAllTradingDatabaseForServerMule(otherBot, partner);
							}

							if (tradeWithOfSuperMule != null) {

								// System.out.println(
								// "1: " + partner.getAccount().getUsername() + " " + tradeWithOfSuperMule);
								//
								// System.out.println(
								// "2: " + otherBot.getAccount().getUsername() + " " + tradeWithOfServerMule);

								if ((!otherBot.getAccount().getUsername().equalsIgnoreCase(tradeWithOfSuperMule)
										|| !partner.getAccount().getUsername()
												.equalsIgnoreCase(tradeWithOfServerMule))) {
									System.out.println("Accounts didn't trade eachother: S02");
									resetAllTradingDatabaseForServerMule(otherBot, partner);
								}
							}
						}
					}
				}

				// OsbotController superMule = osbot.getAccount().getStatus() ==
				// AccountStatus.SUPER_MULE ? osbot
				// : otherBot;
				//
				// String tradeWithOfSuperMule =
				// DatabaseUtilities.getTradeWithOther(superMule.getId());
				// String tradeWithOfServerMule =
				// DatabaseUtilities.getTradeWithOther("server_muling", serverMule.getId());
				//
				// if (serverMule != null && superMule != null && tradeWithOfSuperMule != null
				// && tradeWithOfServerMule != null) {
				//
				// if
				// (!serverMule.getAccount().getUsername().equalsIgnoreCase(tradeWithOfSuperMule)
				// ||
				// !superMule.getAccount().getUsername().equalsIgnoreCase(tradeWithOfServerMule))
				// {
				// System.out.println("Accounts didn't match eachother E05");
				// DatabaseUtilities.setTradingWith("server_muling", null, serverMule.getId());
				// DatabaseUtilities.setTradingWith(null, superMule.getId());
				// DatabaseUtilities.setTradingWith(null,
				// serverMule.getAccount().getUsername());
				// DatabaseUtilities.setTradingWith("server_muling", null,
				// superMule.getAccount().getUsername());
				// }
				//
				// }

				// System.out.println("Server mule: " + serverMule.getAccount().getUsername() +
				// " Trade with: "
				// + tradeWithOfServerMule + " super mule: " +
				// superMule.getAccount().getUsername()
				// + " trade with: " + tradeWithOfSuperMule);
			}

		}
	}

	/**
	 * Handling with running the mules
	 * 
	 * Setting them up to yet
	 */
	public static void handleMules() {
		OsbotController availableMule = null;
		OsbotController availableSuperMule = null;
		OsbotController availableServerMule = null;

		DatabaseUtilities.setBannedAndTradingWithToNull();

		// If accounts are not loaded yet
		if (BotController.getBots().size() == 0) {
			System.out.println("[MULE TRADING] Couldn't trade yet, accounts weren't loaded yet");
		}
		for (int i = 0; i < BotController.getBots().size(); i++) {

			OsbotController osbot = BotController.getBots().get(i);

			if (osbot.getAccount().getStage() == AccountStage.MULE_TRADING
					|| osbot.getAccount().getStage() == AccountStage.UNKNOWN
					|| osbot.getAccount().getStatus() == AccountStatus.MULE
					|| osbot.getAccount().getStatus() == AccountStatus.SUPER_MULE
					|| osbot.getAccount().getStatus() == AccountStatus.SERVER_MULE) {

				isServerMuleTrading();

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

						// Skipping because other method deals with this
						if ((osbot.getAccount().getStatus() == AccountStatus.SUPER_MULE
								&& partner.getAccount().getStatus() == AccountStatus.SERVER_MULE)
								|| (osbot.getAccount().getStatus() == AccountStatus.SERVER_MULE
										&& partner.getAccount().getStatus() == AccountStatus.SUPER_MULE)) {
							continue;
						}

						String tradeWithOther2 = DatabaseUtilities.getTradeWithOther(partner.getId());

						if ((tradeWithOther != null && tradeWithOther2 == null)
								|| (tradeWithOther == null && tradeWithOther2 != null)) {
							System.out.println("One of the account had a null! E04");
							DatabaseUtilities.setTradingWith(null, osbot.getAccount().getUsername());
							DatabaseUtilities.setTradingWith(null, osbot.getId());
						}

						boolean moreThan1Trading = DatabaseUtilities
								.getAmountOfMuleTrades(osbot.getAccount().getUsername()) > 1
								|| DatabaseUtilities.getAmountOfMuleTrades(partner.getAccount().getUsername()) > 1;
						boolean notTradingEachOther = !osbot.getAccount().getUsername()
								.equalsIgnoreCase(tradeWithOther2)
								|| !partner.getAccount().getUsername().equalsIgnoreCase(tradeWithOther);

						if (notTradingEachOther || moreThan1Trading) {
							if (notTradingEachOther) {
								System.out.println("Accounts didn't trade eachother! E04");
							} else {
								System.out.println("More than 1 trading! E05");
							}

							// System.out.println("1: " + osbot.getAccount().getUsername() + " " +
							// tradeWithOther2);
							// System.out.println("2: " + partner.getAccount().getUsername() + " " +
							// tradeWithOther);

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

		// HashMap<OsbotController, Integer> availableMuleList = new
		// HashMap<OsbotController, Integer>();
		// HashMap<OsbotController, Integer> availableSuperMuleList = new
		// HashMap<OsbotController, Integer>();
		// HashMap<OsbotController, Integer> availableServerMuleList = new
		// HashMap<OsbotController, Integer>();

		/**
		 * Finding all the accounts and put them in a list to compare the values
		 */
		for (int i = 0; i < BotController.getBots().size(); i++) {
			OsbotController osbot = BotController.getBots().get(i);

			if (((osbot.getAccount().getStage() == AccountStage.MULE_TRADING
					|| (osbot.getAccount().getStage() == AccountStage.UNKNOWN))

					&& (osbot.getAccount().getStatus() == AccountStatus.MULE
							|| osbot.getAccount().getStatus() == AccountStatus.SUPER_MULE
							|| osbot.getAccount().getStatus() == AccountStatus.SERVER_MULE))) {

				boolean isServerMule = osbot.getAccount().getStatus() == AccountStatus.SERVER_MULE;
				boolean isSuperMule = osbot.getAccount().getStatus() == AccountStatus.SUPER_MULE;
				boolean isNormalMule = osbot.getAccount().getStatus() == AccountStatus.MULE;

				int value = 0;
				try {
					value = Integer.parseInt(osbot.getAccount().getAccountValue());
				} catch (Exception e) {
					System.out.println(osbot.getAccount().getUsername() + " " + osbot.getAccount().getAccountValue());
					value = 0;
					e.printStackTrace();
				}

				if (isServerMule) {
					if (osbot.getAccount().getTradeWithOther() == null
							&& DatabaseUtilities.getTradeWithOther("server_muling", osbot.getId()) == null) {
						availableServerMule = osbot;
						// System.out.println(
						// "[SERVER TRADING] Found an account! " +
						// availableServerMule.getAccount().getUsername());
					}
				}

				if (isSuperMule || isNormalMule) {
					if (osbot.getAccount().getTradeWithOther() == null
							&& DatabaseUtilities.getTradeWithOther(osbot.getId()) == null
							&& DatabaseUtilities.getAmountOfMuleTrades(osbot.getAccount().getUsername()) == 0) {
						if (isSuperMule) {
							availableSuperMule = osbot;
							// System.out.println("[SUPER MULE TRADING] Found an account! "
							// + availableSuperMule.getAccount().getUsername());
						} else if (isNormalMule) {
							availableMule = osbot;
							// System.out.println(
							// "[MULE TRADING] Found an account! " +
							// availableMule.getAccount().getUsername());
						}
					}
				}

				// if (isServerMule) {
				// if (osbot.getAccount().getTradeWithOther() == null
				// && DatabaseUtilities.getTradeWithOther("server_muling", osbot.getId()) ==
				// null) {
				// availableServerMuleList.put(osbot, value);
				// }
				// }
				//
				// // Looking for a mule account and that is currently available ('NULL')
				// if (osbot.getAccount().getTradeWithOther() == null
				// && DatabaseUtilities.getTradeWithOther(osbot.getId()) == null
				// && DatabaseUtilities.getAmountOfMuleTrades(osbot.getAccount().getUsername())
				// == 0) {
				// if (osbot.getAccount().getStatus() == AccountStatus.SUPER_MULE) {
				// availableSuperMuleList.put(osbot, value);
				// } else if (osbot.getAccount().getStatus() == AccountStatus.MULE) {
				// availableMuleList.put(osbot, value);
				// }
				// }

			}
		}

		/**
		 * Sorting the accounts by value they have
		 */
		// Entry<OsbotController, Integer> availableMuleFirst =
		// availableMuleList.entrySet().stream()
		// .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).findFirst().orElse(null);
		//
		// Entry<OsbotController, Integer> availableSuperMuleFirst =
		// availableSuperMuleList.entrySet().stream()
		// .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).findFirst().orElse(null);
		//
		// Entry<OsbotController, Integer> availableServerMuleFirst =
		// availableServerMuleList.entrySet().stream()
		// .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).findFirst().orElse(null);
		//
		// if (availableServerMuleFirst != null) {
		// availableServerMule = availableServerMuleFirst.getKey();
		// System.out
		// .println("[SERVER MULE TRADING] Found an account! " +
		// availableServerMule.getAccount().getUsername()
		// + " with value: " + availableServerMule.getAccount().getAccountValue());
		// }
		// if (availableSuperMuleFirst != null) {
		// availableSuperMule = availableSuperMuleFirst.getKey();
		// System.out.println("[SUPER MULE TRADING] Found an account! " +
		// availableSuperMule.getAccount().getUsername()
		// + " with value: " + availableSuperMule.getAccount().getAccountValue());
		// }
		// if (availableMuleFirst != null) {
		// availableMule = availableMuleFirst.getKey();
		// System.out.println("[NORMAL MULE TRADING] Found an account! " +
		// availableMule.getAccount().getUsername()
		// + " with value: " + availableMule.getAccount().getAccountValue());
		// }

		// Must have a mule available to continue
		if (BotController.getBots().size() > 0 && availableServerMule == null) {
			// System.out.println("[SERVER MULE TRADING] Couldn't find server-mule to trade
			// with, is not available");
		} else {

			boolean isCorrectDatabase = DatabaseUtilities.getServerMuleUsedByDatabase() == null ? false
					: DatabaseUtilities.getServerMuleUsedByDatabase().equalsIgnoreCase(Config.DATABASE_NAME);

			for (int i = 0; i < BotController.getBots().size(); i++) {
				OsbotController osbot = BotController.getBots().get(i);

				int value = 0;
				try {
					value = Integer.parseInt(osbot.getAccount().getAccountValue());
				} catch (Exception e) {
					System.out.println(osbot.getAccount().getUsername() + " " + osbot.getAccount().getAccountValue());
					value = 0;
					e.printStackTrace();
				}

				if (osbot.getAccount().getStage() == AccountStage.UNKNOWN
						&& osbot.getAccount().getStatus() == AccountStatus.SUPER_MULE && value > 2_500_000
						&& !isCorrectDatabase) {

					// If an account wants to mule
					// Not with himself and must be status MULE_TRADING
					if (DatabaseUtilities.getTradeWithOther(osbot.getId()) == null
							&& !osbot.getAccount().getUsername()
									.equalsIgnoreCase(availableServerMule.getAccount().getUsername())
							&& DatabaseUtilities.getTradeWithOther("server_muling", availableServerMule.getId()) == null
							&& DatabaseUtilities
									.getAmountOfMuleTrades(availableServerMule.getAccount().getUsername()) == 0
							&& DatabaseUtilities.getAmountOfMuleTrades("server_muling",
									osbot.getAccount().getUsername()) == 0) {

						// Setting in database that the mule is trading with the workers name
						DatabaseUtilities.setTradingWith("server_muling", osbot.getAccount().getUsername(),
								availableServerMule.getId());

						System.out.println(
								"[MULE TRADING] Starting super-mule: " + availableServerMule.getAccount().getUsername()
										+ " to trade with: " + osbot.getAccount().getUsername());

						// Setting in database that worker is trading with mule
						DatabaseUtilities.setTradingWith(availableServerMule.getAccount().getUsername(), osbot.getId());

						System.out.println("[MULE TRADING] Starting normal-mule: " + osbot.getAccount().getUsername()
								+ " to trade with mule: " + availableServerMule.getAccount().getUsername());

						DatabaseUtilities.setServerMuleConnectedDatabase(Config.DATABASE_NAME);

						break;
					}
				}
			}
		}

		// Must have a mule available to continue
		if (BotController.getBots().size() > 0 && availableSuperMule == null) {
			// System.out.println("[SUPER MULE TRADING] Couldn't find super-mule to trade
			// with, is not available");
		} else {
			for (int i = 0; i < BotController.getBots().size(); i++) {
				OsbotController osbot = BotController.getBots().get(i);

				int value = 0;
				try {
					value = Integer.parseInt(osbot.getAccount().getAccountValue());
				} catch (Exception e) {
					System.out.println(osbot.getAccount().getUsername() + " " + osbot.getAccount().getAccountValue());
					value = 0;
					e.printStackTrace();
				}

				if (osbot.getAccount().getStage() == AccountStage.UNKNOWN
						&& osbot.getAccount().getStatus() == AccountStatus.MULE && value > 500_000) {

					// If an account wants to mule
					// Not with himself and must be status MULE_TRADING
					if (DatabaseUtilities.getTradeWithOther(osbot.getId()) == null
							&& !osbot.getAccount().getUsername()
									.equalsIgnoreCase(availableSuperMule.getAccount().getUsername())
							&& DatabaseUtilities.getTradeWithOther(availableSuperMule.getId()) == null
							&& DatabaseUtilities
									.getAmountOfMuleTrades(availableSuperMule.getAccount().getUsername()) == 0
							&& DatabaseUtilities.getAmountOfMuleTrades(osbot.getAccount().getUsername()) == 0) {

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
			// System.out.println("[MULE TRADING] Couldn't find mule to trade with, is not
			// available");
		} else {
			for (int i = 0; i < BotController.getBots().size(); i++) {
				OsbotController osbot = BotController.getBots().get(i);

				if (osbot.getAccount().getStage() == AccountStage.MULE_TRADING
						&& osbot.getAccount().getStatus() == AccountStatus.AVAILABLE) {

					// If an account wants to mule
					// Not with himself and must be status MULE_TRADING
					if (DatabaseUtilities.getTradeWithOther(osbot.getId()) == null
							&& !osbot.getAccount().getUsername()
									.equalsIgnoreCase(availableMule.getAccount().getUsername())
							&& DatabaseUtilities.getTradeWithOther(availableMule.getId()) == null & DatabaseUtilities
									.getAmountOfMuleTrades(availableMule.getAccount().getUsername()) == 0
							&& DatabaseUtilities.getAmountOfMuleTrades(osbot.getAccount().getUsername()) == 0) {

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
					|| Config.isStaticMuleProxy(osbot.getAccount().getProxyIp(), osbot.getAccount().getProxyPort())
					|| Config.isServerMuleProxy(osbot.getAccount().getProxyIp(), osbot.getAccount().getProxyPort());
			boolean mayLoginBecauseOfStatus = osbot.getAccount().getLoginStatus() != LoginStatus.LOGGED_IN;

			// System.out.println(Config.isMuleProxy(osbot.getAccount().getProxyIp(),
			// osbot.getAccount().getProxyPort()));
			// System.out.println(osbot.getAccount().getStatus() + " " +
			// osbot.getAccount().getStage());
			// System.out.println("rigth acc: " + isRightAccount);
			// System.out.println("proxy add: " + isProxyAddress);
			// System.out.println("not started up: " + notStartedUpYet);
			if (isRightAccount && isProxyAddress && mayLoginBecauseOfStatus) {
				boolean notStartedUpYet = !BotController.containsInPidList(osbot.getPidId());
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
					// if (DatabaseUtilities.getLoginStatus(osbot2.getId()) == LoginStatus.DEFAULT)
					// {

					if (osbot2.getAccount().getStage() != AccountStage.UNKNOWN
							&& osbot2.getAccount().getStage() != AccountStage.MULE_TRADING) {

						System.out.println("Account stage wasn't to mule trade");
						DatabaseUtilities.setTradingWith(null, osbot2.getId());
						break;
					}

					System.out.println("Running mule trading " + osbot2.getAccount().getUsername());
					if (!Config.TESTING) {
						runMule(osbot2, osbot2.getAccount().getTradeWithOther(),
								DatabaseUtilities.getEmailFromUsername(osbot2.getAccount().getTradeWithOther()));
//						try {
//							Thread.sleep(5500);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
					}
					// }
				}
			}
		}

		// try {
		// Thread.sleep(2500);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
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

		if (wantsToMule()) {
			System.out.println("A bot wants to mule, so giving them priority");
			runMule();
		}

		if (getAmountOfBotsActive() < Config.MAX_BOTS_OPEN) {

			for (int i = 0; i < BotController.getBots().size(); i++) {
				OsbotController osbot = BotController.getBots().get(i);

				long startTime = System.currentTimeMillis();

				if (wantsToMule()) {
					System.out.println("A bot wants to mule, so giving them priority");
					runMule();
					continue;
				}

				OsbotController superMule = superMuleWantsToLaunchToFinishTutorialIsland();
				if (superMule != null) {
					System.out.println(
							"Skipping this one, because a tutorial island mule/super mule/server mule has to launch first, acc: "
									+ superMule.getAccount().getUsername() + " status: "
									+ superMule.getAccount().getStatus());
					runBot(superMule);
					continue;
				}

				// System.out.println((System.currentTimeMillis() - startTime) + " ms time part
				// one");

				boolean hasValidLoginStatus = osbot.getAccount().getLoginStatus() == LoginStatus.DEFAULT;

				if (!hasValidLoginStatus) {
					continue;
				}

				boolean correctStagetoLaunch = osbot.getAccount().getStage() != AccountStage.UNKNOWN
						&& osbot.getAccount().getStatus() != AccountStatus.OUT_OF_MONEY
						&& osbot.getAccount().getStage() != AccountStage.MULE_TRADING
						&& osbot.getAccount().getStage() != AccountStage.GE_SELL_BUY_MINING;

				if (!correctStagetoLaunch) {
					continue;
				}

				boolean correctStatusToLaunch = (osbot.getAccount().getStatus() == AccountStatus.AVAILABLE)
						|| (osbot.getAccount().getStatus() == AccountStatus.WALKING_STUCK)
						|| (osbot.getAccount().getStatus() == AccountStatus.TASK_TIMEOUT
								&& osbot.getAccount().getAmountTimeout() < Config.AMOUNT_OF_TIMEOUTS_BEFORE_GONE)
						|| (osbot.getAccount().getStatus() == AccountStatus.TIMEOUT
								&& osbot.getAccount().getAmountTimeout() < Config.AMOUNT_OF_TIMEOUTS_BEFORE_GONE);

				boolean isProxyOnline = osbot.getAccount().isProxyOnline();

				if (!isProxyOnline) {
					System.out.println("Skipping launching this account, because the proxy is offline");
					continue;
				}

				if (!correctStatusToLaunch) {
					continue;
				}

				boolean enoughSpaceToLaunch = javaPidsSize < Config.MAX_BOTS_OPEN;

				if (!enoughSpaceToLaunch) {
					continue;
				}

				boolean hasValidEmail = osbot.getAccount().getEmail() != null;

				if (!hasValidEmail) {
					continue;
				}

				// System.out.println((System.currentTimeMillis() - startTime) + " ms time part
				// two");

				if (osbot != null) {

					boolean containsInProgram = BotController.containsInPidList(osbot.getPidId());

					if (containsInProgram) {
						continue;
					}

					if (Config.BREAKING) {
						if (!calendar2.after(osbot.getAccount().getDate())) {
							System.out.println(
									"Skipping " + osbot.getAccount().getUsername() + " because has currently a break");
							continue;
						}
					}

					runBot(osbot);
					
//					try {
//						Thread.sleep(5500);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					
					System.out.println("[BOT HANDLER MANAGEMENT] Running bot name: " + osbot.getAccount().getStage()
							+ " " + osbot.getAccount().getUsername());

					System.out.println(System.currentTimeMillis() - startTime + " ms to start up");
				}
			}
		}
	}
}
