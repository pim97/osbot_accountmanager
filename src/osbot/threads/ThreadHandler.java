package osbot.threads;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import osbot.account.AccountStage;
import osbot.account.api.ipwhois.IPWhoisApi;
import osbot.account.api.proxy6.Proxy6;
import osbot.account.creator.AccountCreationService;
import osbot.account.global.Config;
import osbot.account.handler.BotHandler;
import osbot.account.mules.TradeBeforeBanWaves;
import osbot.bot.BotController;
import osbot.controller.communicator.ContentController;
import osbot.database.DatabaseUtilities;
import osbot.random.RandomUtil;
import osbot.selenium.timeout.TimeoutSeleniumHandler;
import osbot.settings.OsbotController;

public class ThreadHandler {

	/**
	 * As long as this is true, the main thread will be running
	 */
	private static boolean programIsRunning = true;

	/**
	 * A list of all the threads in the program
	 */
	private static List<Thread> threadList = new ArrayList<Thread>();

	/**
	 * The queue of captchas which turned out to be useless, so not using anymore
	 */
	private static void runQueueThread() {
		Thread queueThread = new Thread(() -> {
			Config.QUEUE.run();
		});
		queueThread.setName("queueThread");
		queueThread.start();

		threadList.add(queueThread);
	}

	private static void checkProxiesAndInsertIntoDatabaseAndOnTheWebsite() {
		Thread checkProxiesAndInsertIntoDatabaseAndOnTheWebsite = new Thread(() -> {

			while (programIsRunning) {

				Proxy6.getSingleton().loop();
				// BotHandler.checkJavaPidsTimeout();

				// try {
				// Thread.sleep(120_000);
				// } catch (Exception e) {
				// e.printStackTrace();
				// }

			}

		});
		checkProxiesAndInsertIntoDatabaseAndOnTheWebsite.setName("checkProxiesAndInsertIntoDatabaseAndOnTheWebsite");
		checkProxiesAndInsertIntoDatabaseAndOnTheWebsite.start();

		threadList.add(checkProxiesAndInsertIntoDatabaseAndOnTheWebsite);
	}

	private static void checkProxiesProxyRackToUse() {
		Thread checkProxiesProxyRackToUse = new Thread(() -> {

			while (programIsRunning) {
				IPWhoisApi.getSingleton().loop();

				try {
					Thread.sleep(120_000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
		checkProxiesProxyRackToUse.setName("checkProxiesProxyRackToUse");
		checkProxiesProxyRackToUse.start();

		threadList.add(checkProxiesProxyRackToUse);
	}

	private static void muleTradingBeforeBanWaveTrade2() {
		Thread muleTradingBeforeBanWaveTrade2 = new Thread(() -> {

			while (programIsRunning) {
				TradeBeforeBanWaves.trade2();
			}

		});
		muleTradingBeforeBanWaveTrade2.setName("muleTradingBeforeBanWaveTrade2");
		muleTradingBeforeBanWaveTrade2.start();

		threadList.add(muleTradingBeforeBanWaveTrade2);
	}

	private static void muleTradingBeforeBanWave() {
		Thread muleTradingBeforeBanWave = new Thread(() -> {

			while (programIsRunning) {
				TradeBeforeBanWaves.trade1();
			}

		});
		muleTradingBeforeBanWave.setName("muleTradingBeforeBanWave");
		muleTradingBeforeBanWave.start();

		threadList.add(muleTradingBeforeBanWave);
	}

	private static void isUnkownAndNotAMule() {

		Thread isUnkownAndNotAMule = new Thread(() -> {

			while (programIsRunning) {

				for (OsbotController bots : BotController.getBots()) {
					if ((!Config.isMuleProxy(bots.getAccount().getProxyIp(), bots.getAccount().getProxyPort())
							&& !Config.isServerMuleProxy(bots.getAccount().getProxyIp(),
									bots.getAccount().getProxyPort())
							&& !Config.isStaticMuleProxy(bots.getAccount().getProxyIp(),
									bots.getAccount().getProxyPort())
							&& !Config.isSuperMuleProxy(bots.getAccount().getProxyIp(),
									bots.getAccount().getProxyPort()))
							&& (DatabaseUtilities.getAccountStageInDatabase(bots.getId())
									.equalsIgnoreCase("UNKNOWN"))) {
						DatabaseUtilities.updateAccountStage(AccountStage.TUT_ISLAND, bots.getId());
						System.out.println("Was an 'unknown' but was not a mule, setting back to tut island");
					}
				}

				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		isUnkownAndNotAMule.setName("isUnkownAndNotAMule");
		isUnkownAndNotAMule.start();

		threadList.add(isUnkownAndNotAMule);

	}

	private static void transformIntoMuleAccount() {
		Thread transformIntoMuleAccount = new Thread(() -> {

			while (programIsRunning) {

				DatabaseUtilities.transformIntoMuleAccount();
				// BotHandler.checkJavaPidsTimeout();

				try {
					Thread.sleep(RandomUtil.getRandomNumberInRange(1000, 10000));
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		});
		transformIntoMuleAccount.setName("transformIntoMuleAccount");
		transformIntoMuleAccount.start();

		threadList.add(transformIntoMuleAccount);
	}

	private static void transformIntoMuleHandler() {
		Thread transformIntoMuleHandler = new Thread(() -> {

			while (programIsRunning) {

				DatabaseUtilities.transformIntoMuleHandler();
				// BotHandler.checkJavaPidsTimeout();

				try {
					Thread.sleep(RandomUtil.getRandomNumberInRange(10000, 20000));
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		});
		transformIntoMuleHandler.setName("transformIntoMuleHandler");
		transformIntoMuleHandler.start();

		threadList.add(transformIntoMuleHandler);
	}

	// Setting a delay so it doesn't double launch and double recover an account
	private static int createDelay = 5000;

	/**
	 * The thread for selenium trying to create accounts
	 */
	private static void createAccountsThread(int index) {
		Thread createAccounts = new Thread(() -> {

			try {
				Thread.sleep(RandomUtil.getRandomNumberInRange(0, 100_000));
			} catch (Exception e) {
				e.printStackTrace();
			}
			DatabaseUtilities.seleniumCreateAccountThread();

		});
		createAccounts.setName("createAccounts" + index);
		createAccounts.start();

		threadList.add(createAccounts);
	}

	// Setting a delay so it doesn't double launch and double recover an account
	private static int recoverDelay = 5000;

	/**
	 * The thread for selenium trying to recover account
	 */
	private static void recoverAccountsThread(int index) {
		Thread recoverAccounts = new Thread(() -> {

			try {
				Thread.sleep(RandomUtil.getRandomNumberInRange(0, 100_000));
			} catch (Exception e) {
				e.printStackTrace();
			}

			DatabaseUtilities.seleniumRecoverAccount();

		});
		recoverAccounts.setName("recoverAccounts" + index);
		recoverAccounts.start();

		threadList.add(recoverAccounts);

	}

	/**
	 * Handles the thread for muling on the accounts
	 */
	private static void checkMulesCorrectTrading() {
		Thread checkMulesCorrectTrading = new Thread(() -> {
			while (programIsRunning) {

				BotHandler.checkMulesCorrectTrading();
			}
		});
		checkMulesCorrectTrading.setName("checkMulesCorrectTrading");
		checkMulesCorrectTrading.start();

		threadList.add(checkMulesCorrectTrading);
	}

	/**
	 * Handles the thread for muling on the accounts
	 */
	private static void handleNormalMulesTrading() {
		Thread handleNormalMulesTrading = new Thread(() -> {
			while (programIsRunning) {

				BotHandler.handleNormalMules();
			}
		});
		handleNormalMulesTrading.setName("handleNormalMulesTrading");
		handleNormalMulesTrading.start();

		threadList.add(handleNormalMulesTrading);
	}

	private static void handleServerMules() {
		Thread handleServerMules = new Thread(() -> {
			while (programIsRunning) {

				BotHandler.handleServerMules();
			}
		});
		handleServerMules.setName("handleServerMules");
		handleServerMules.start();

		threadList.add(handleServerMules);
	}

	private static void handleSuperMules() {
		Thread handleSuperMules = new Thread(() -> {
			while (programIsRunning) {

				BotHandler.handleSuperMules();
			}
		});
		handleSuperMules.setName("handleSuperMules");
		handleSuperMules.start();

		threadList.add(handleSuperMules);
	}

	/**
	 * Handles all the bots running
	 */
	private static void handleBotsRunning() {
		Thread handleBotsRunning = new Thread(() -> {

			while (programIsRunning) {

				BotHandler.handleBots();

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		});
		handleBotsRunning.setName("handleBotsRunning");
		handleBotsRunning.start();

		threadList.add(handleBotsRunning);
	}

	private static TimeoutSeleniumHandler timeoutHandler = new TimeoutSeleniumHandler();

	private static void checkPids() {
		Thread checkPidsProcessesEveryMinutes2 = new Thread(() -> {
			while (programIsRunning) {

				timeoutHandler.handleGeckoPids();
				// DatabaseUtilities.checkPidsProcessesEveryMinutes2();

				// Checking every 5 seconds if bot is still running
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		checkPidsProcessesEveryMinutes2.setName("checkPidsProcessesEveryMinutes2");
		checkPidsProcessesEveryMinutes2.start();

		threadList.add(checkPidsProcessesEveryMinutes2);
	}

	private static void checkTimeoutLockedBackToNormal() {
		Thread checkTimeoutLockedBackToNormal = new Thread(() -> {
			while (programIsRunning) {

				DatabaseUtilities.changeTimeoutLockedToNormal();
				DatabaseUtilities.changeErrorIpBackToNormal();

				// Checking every 5 seconds if bot is still running
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		checkTimeoutLockedBackToNormal.setName("checkTimeoutLockedBackToNormal");
		checkTimeoutLockedBackToNormal.start();

		threadList.add(checkTimeoutLockedBackToNormal);
	}

	private static void checkUsedUsernames() {
		Thread checkUsedUsernames = new Thread(() -> {
			while (programIsRunning) {

				AccountCreationService.checkUsedUsernames();

				// Checking every 5 seconds if bot is still running
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		checkUsedUsernames.setName("checkUsedUsernames");
		checkUsedUsernames.start();

		threadList.add(checkUsedUsernames);
	}

	private static void checkBotsWhenNotActive() {
		Thread checkBotsWhenNotActive = new Thread(() -> {
			while (programIsRunning) {

				DatabaseUtilities.closeBotsWhenNotActive();

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		checkBotsWhenNotActive.setName("checkBotsWhenNotActive");
		checkBotsWhenNotActive.start();

		threadList.add(checkBotsWhenNotActive);
	}

	private static void checkRunningErrors() {
		Thread checkRunningErrors = new Thread(() -> {
			while (programIsRunning) {

				DatabaseUtilities.checkIfAccountIsTooMany();
				// DatabaseUtilities.checkRunningErrors();
				// try {
				// Thread.sleep(30_000);
				// } catch (InterruptedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
			}
		});
		checkRunningErrors.setName("checkRunningErrors");
		checkRunningErrors.start();

		threadList.add(checkRunningErrors);
	}

	public static void mainThread() {
		Thread recoverAndCreateThread = new Thread(() -> {
			while (programIsRunning) {

				int recoverAmount = DatabaseUtilities.getAccountsToBeRecovered().size() != 1
						? DatabaseUtilities.getAccountsToBeRecovered().size() / 2
						: DatabaseUtilities.getAccountsToBeRecovered().size();

				int createAmount = DatabaseUtilities.accountsToCreate2() != 1
						? DatabaseUtilities.accountsToCreate2() / 2
						: DatabaseUtilities.accountsToCreate2();
				if (createAmount > 5) {
					createAmount = 5;
				}
				if (recoverAmount > 5) {
					recoverAmount = 5;
				}

				System.out.println("Recover accounts thread to be: " + recoverAmount);

				for (int i = 0; i < recoverAmount; i++) {
					if (getThread("recoverAccounts" + i) == null && Config.RECOVERING_ACCOUNTS_THREAD_ACTIVE) {

						recoverAccountsThread(i);
						System.out.println("Started new thread: recoverAccounts" + i);

					}
				}

				System.out.println("Create accounts thread to be: " + createAmount);
				for (int i = 0; i < createAmount; i++) {
					if (getThread("createAccounts" + i) == null && Config.CREATING_ACCOUNTS_THREAD_ACTIVE) {

						createAccountsThread(i);
						System.out.println("Started new thread: createAccounts" + i);
					}
				}

				try {
					Thread.sleep(35_000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		recoverAndCreateThread.setName("recoverAndCreateThread");
		recoverAndCreateThread.start();

		threadList.add(recoverAndCreateThread);

		System.out.println("Waiting 10 seconds for everything to load...");

		Thread mainThread = new Thread(() -> {
			while (programIsRunning) {

				// Sleeping 20 seconds for all the accounts to load
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if (BotHandler.MAIN_PID == -1) {
					String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
					long pid = Long.parseLong(processName.split("@")[0]);
					BotHandler.MAIN_PID = pid;

					System.out.println("Set program pid to: " + BotHandler.MAIN_PID);
				}

				checkForAlive();

				checkTheadsRunningAndIfNotStartNewOne();

				// Thread sleeping & checking every 30 seconds
				try {
					Thread.sleep(10_000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// System.out.println("Checking every 2 seconds for threads to be alive or
				// not");
			}
		});
		mainThread.setName("mainThread");
		mainThread.start();

		threadList.add(mainThread);
	}

	public static void checkTheadsRunningAndIfNotStartNewOne() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (getThread("checkTimeoutLockedBackToNormal") == null) {
			checkTimeoutLockedBackToNormal();
			System.out.println("Started new thread checkTimeoutLockedBackToNormal");
		}

		if (getThread("refreshTablesThread") == null) {
			ContentController.refreshTablesThread();
			System.out.println("Started new thread refreshTablesThread");
		}

		if (getThread("checkBotsWhenNotActive") == null && Config.CLOSE_BOTS_WHEN_ACTIVE) {
			checkBotsWhenNotActive();
			System.out.println("Started new thread checkBotsWhenNotActive");
		}

		if (getThread("transformIntoMuleAccount") == null) {
			transformIntoMuleAccount();
			System.out.println("Started new thread transformIntoMuleAccount");
		}

		if (getThread("checkMulesCorrectTrading") == null) {
			checkMulesCorrectTrading();
			System.out.println("Started new thread checkMulesCorrectTrading");
		}

		if (getThread("checkProxiesAndInsertIntoDatabaseAndOnTheWebsite") == null) {
			checkProxiesAndInsertIntoDatabaseAndOnTheWebsite();
			System.out.println("Started new thread checkProxiesAndInsertIntoDatabaseAndOnTheWebsite");
		}

		if (getThread("isUnkownAndNotAMule") == null) {
			isUnkownAndNotAMule();
			System.out.println("Started new thread isUnkownAndNotAMule");
		}

		if (getThread("checkRunningErrors") == null) {
			checkRunningErrors();
			System.out.println("Started new thread checkRunningErrors");
		}

		if (getThread("checkPidsProcessesEveryMinutes2") == null) {
			checkPids();
			System.out.println("Started new thread checkPidsProcessesEveryMinutes2");

		}

		if (getThread("checkUsedUsernames") == null) {
			checkUsedUsernames();
			System.out.println("Started new thread checkUsedUsernames");
		}

		// if (getThread("muleTradingBeforeBanWave") == null) {
		// muleTradingBeforeBanWave();
		// System.out.println("Started new thread muleTradingBeforeBanWave");
		//
		// }
		//
		// if (getThread("muleTradingBeforeBanWaveTrade2") == null) {
		// muleTradingBeforeBanWaveTrade2();
		// System.out.println("Started new thread muleTradingBeforeBanWaveTrade2");
		//
		// }

		if (getThread("handleBotsRunning") == null && Config.BOT_HANDLER_THREAD_ACTIVE) {
			handleBotsRunning();
			System.out.println("Started new thread: handleBotsRunning");
		}

		if (getThread("transformIntoMuleHandler") == null && Config.CREATING_ACCOUNTS_THREAD_ACTIVE) {
			transformIntoMuleHandler();
			System.out.println("Started new thread: transformIntoMuleHandler");
		}

		if (getThread("handleNormalMulesTrading") == null && Config.MULES_TRADING) {
			handleNormalMulesTrading();
			System.out.println("Started new thread: handleNormalMulesTrading");
		}
		if (getThread("handleServerMules") == null && Config.MULES_TRADING) {
			handleServerMules();
			System.out.println("Started new thread: handleServerMules");
		}
		if (getThread("handleSuperMules") == null && Config.MULES_TRADING) {
			handleSuperMules();
			System.out.println("Started new thread: handleSuperMules");
		}

		if (getThread("checkProxiesProxyRackToUse") == null && Config.NEW_PROXYRACK_CONFIGURATION) {
			checkProxiesProxyRackToUse();
			System.out.println("Started new thread: checkProxiesProxyRackToUse");
		}

		if (getThread("queueThread") == null && Config.CAPTCHA) {
			runQueueThread();
			System.out.println("Started new thread: queueThread");
		}
	}

	/**
	 * Manages all the threads currently running
	 */
	public static void runThreads() {

		mainThread();

		Thread backupThread = new Thread(() -> {

			while (programIsRunning) {

				// checkForAlive();

				System.out.println("[BACKUP] Thread management: " + isThreadAlive("backupThread") + " "
						+ getThread("backupThread"));

				if (getThread("mainThread") == null) {
					mainThread();
					System.out.println("Started new thread: mainThread");
				}

				checkTheadsRunningAndIfNotStartNewOne();

				try {
					Thread.sleep(20_000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		backupThread.setName("backupThread");
		backupThread.start();

		threadList.add(backupThread);

	}

	/**
	 * 
	 * @param threadName
	 * @return
	 */
	private static Thread getThread(String threadName) {
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Thread t : threadSet) {
			if (t != null && t.getName().equalsIgnoreCase(threadName) && t.isAlive()) {
				// System.out.println("Thread :" + t + ":" + "state:" + t.getState());
				return t;
			}
		}
		return null;
	}

	/**
	 * Is the thread still alive or not?
	 * 
	 * @param threadName
	 * @return
	 */
	public static boolean isThreadAlive(String threadName) {
		// for (Thread thread : threadList) {
		// if (thread.getName().equalsIgnoreCase(threadName) && thread.isAlive()) {
		// return true;
		// }
		// }
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Thread t : threadSet) {
			if (t != null && t.getName().equalsIgnoreCase(threadName) && t.isAlive()) {
				// System.out.println("Thread :" + t + ":" + "state:" + t.getState());
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 */
	private static void checkForAlive() {
		Iterator<Thread> threads = threadList.iterator();

		while (threads.hasNext()) {
			Thread thread = threads.next();

			if (!thread.isAlive()) {
				// Thread wasn't alive anymore, deleted it
				System.out.println("Removed thread:" + thread.getName() + " because it wasn't active anymore");
				threads.remove();
			}
		}
	}

	/**
	 * @return the threadList
	 */
	public static List<Thread> getThreadList() {
		return threadList;
	}

	/**
	 * @param threadList
	 *            the threadList to set
	 */
	public static void setThreadList(ArrayList<Thread> threadList) {
		ThreadHandler.threadList = threadList;
	}

	/**
	 * @return the programIsRunning
	 */
	public static boolean isProgramIsRunning() {
		return programIsRunning;
	}

	/**
	 * @param programIsRunning
	 *            the programIsRunning to set
	 */
	public static void setProgramIsRunning(boolean programIsRunning) {
		ThreadHandler.programIsRunning = programIsRunning;
	}
}
