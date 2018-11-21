package osbot.threads;

import java.util.ArrayList;
import java.util.Iterator;

import osbot.account.creator.AccountCreationService;
import osbot.account.global.Config;
import osbot.account.handler.BotHandler;
import osbot.database.DatabaseUtilities;
import osbot.random.RandomUtil;

public class ThreadHandler {

	/**
	 * As long as this is true, the main thread will be running
	 */
	private static boolean programIsRunning = true;

	/**
	 * A list of all the threads in the program
	 */
	private static ArrayList<Thread> threadList = new ArrayList<Thread>();

	/**
	 * 
	 */
	private static void runQueueThread() {
		Thread queueThread = new Thread(() -> {
			Config.QUEUE.run();
		});
		queueThread.setName("queueThread");
		queueThread.start();

		threadList.add(queueThread);
	}

	/**
	 * The thread for selenium trying to create accounts
	 */
	private static void createAccountsThread(int index) {
		Thread createAccounts = new Thread(() -> {

			while (programIsRunning) {

				try {
					Thread.sleep(RandomUtil.getRandomNumberInRange(0, 20000));
				} catch (Exception e) {
					e.printStackTrace();
				}

				DatabaseUtilities.seleniumCreateAccountThread();
			}

		});
		createAccounts.setName("createAccounts_" + index);
		createAccounts.start();

		threadList.add(createAccounts);
	}

	/**
	 * The thread for selenium trying to recover account
	 */
	private static void recoverAccountsThread(int index) {
		Thread recoverAccounts = new Thread(() -> {

			while (programIsRunning) {

				try {
					Thread.sleep(RandomUtil.getRandomNumberInRange(0, 20000));
				} catch (Exception e) {
					e.printStackTrace();
				}

				DatabaseUtilities.seleniumRecoverAccount();

			}

		});
		recoverAccounts.setName("recoverAccounts_" + index);
		recoverAccounts.start();

		threadList.add(recoverAccounts);
	}

	/**
	 * Handles the thread for muling on the accounts
	 */
	private static void handleMulesTrading() {
		Thread handleMulesTrading = new Thread(() -> {
			while (programIsRunning) {

				try {
					Thread.sleep(5000);
				} catch (Exception e) {
					e.printStackTrace();
				}

				BotHandler.handleMules();

				// Checking every 15 seconds for a mule
				try {
					Thread.sleep(10000);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		handleMulesTrading.setName("handleMulesTrading");
		handleMulesTrading.start();

		threadList.add(handleMulesTrading);
	}

	/**
	 * Handles all the bots running
	 */
	private static void handleBotsRunning() {
		Thread handleBotsRunning = new Thread(() -> {
			while (programIsRunning) {

				BotHandler.handleBots();

				// Checking every 5 seconds if bot is still running
				try {
					Thread.sleep(5000);
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

	private static void checkPids() {
		Thread checkPidsProcessesEveryMinutes2 = new Thread(() -> {
			while (programIsRunning) {

				DatabaseUtilities.checkPidsProcessesEveryMinutes2();

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

	private static void checkRunningErrors() {
		Thread checkRunningErrors = new Thread(() -> {
			while (programIsRunning) {

				DatabaseUtilities.checkRunningErrors();

				// Checking every 5 seconds if bot is still running
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		checkRunningErrors.setName("checkRunningErrors");
		checkRunningErrors.start();

		threadList.add(checkRunningErrors);
	}

	public static void mainThread() {
		Thread mainThread = new Thread(() -> {
			while (programIsRunning) {

				System.out.println("Waiting 10 seconds for everything to load...");
				// Sleeping 20 seconds for all the accounts to load
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				int recoverAmount = DatabaseUtilities.getAccountsToBeRecovered().size() != 1
						? DatabaseUtilities.getAccountsToBeRecovered().size() / 2
						: DatabaseUtilities.getAccountsToBeRecovered().size();

				int createAmount = DatabaseUtilities.getSizeToCreateAccounts() != 1
						? DatabaseUtilities.getSizeToCreateAccounts() / 2
						: DatabaseUtilities.getSizeToCreateAccounts();

				for (int i = 0; i < recoverAmount; i++) {
					System.out.println("Thread management: " + isThreadAlive("recoverAccounts_" + recoverAmount) + " "
							+ getThread("recoverAccounts_" + recoverAmount));
				}
				for (int i = 0; i < createAmount; i++) {
					System.out.println("Thread management: " + isThreadAlive("createAccounts_" + createAmount) + " "
							+ getThread("createAccounts_" + createAmount));
				}
				System.out.println("Thread management: " + isThreadAlive("handleBotsRunning") + " "
						+ getThread("handleBotsRunning"));
				System.out.println("Thread management: " + isThreadAlive("handleMulesTrading") + " "
						+ getThread("handleMulesTrading"));
				System.out
						.println("Thread management: " + isThreadAlive("queueThread") + " " + getThread("queueThread"));
				System.out.println("Thread management: " + isThreadAlive("checkUsedUsernames") + " "
						+ getThread("checkUsedUsernames"));
				System.out.println("Thread management: " + isThreadAlive("checkRunningErrors") + " "
						+ getThread("checkRunningErrors"));

				checkForAlive();

				System.out.println("Recover accounts thread to be: " + recoverAmount);
				for (int i = 0; i < recoverAmount; i++) {
					if ((!isThreadAlive("recoverAccounts_" + i) && getThread("recoverAccounts_" + i) == null)
							&& Config.RECOVERING_ACCOUNTS_THREAD_ACTIVE) {

						recoverAccountsThread(i);
						System.out.println("Started new thread: recoverAccounts_" + i);

					}
				}

				if ((!isThreadAlive("checkTimeoutLockedBackToNormal")
						&& getThread("checkTimeoutLockedBackToNormal") == null)) {
					checkTimeoutLockedBackToNormal();
					System.out.println("Started new thread checkTimeoutLockedBackToNormal");

				}

				if ((!isThreadAlive("checkRunningErrors") && getThread("checkRunningErrors") == null)) {
					checkRunningErrors();
					System.out.println("Started new thread checkRunningErrors");

				}

				if ((!isThreadAlive("checkPidsProcessesEveryMinutes2")
						&& getThread("checkPidsProcessesEveryMinutes2") == null)) {
					checkPids();
					System.out.println("Started new thread checkPidsProcessesEveryMinutes2");

				}

				if ((!isThreadAlive("checkUsedUsernames") && getThread("checkUsedUsernames") == null)) {
					checkUsedUsernames();
					System.out.println("Started new thread checkUsedUsernames");

				}

				System.out.println("Create accounts thread to be: " + createAmount);
				for (int i = 0; i < createAmount; i++) {
					if ((!isThreadAlive("createAccounts_" + i) && getThread("createAccounts_" + i) == null)
							&& Config.CREATING_ACCOUNTS_THREAD_ACTIVE) {

						createAccountsThread(i);
						System.out.println("Started new thread: createAccounts_" + i);
					}
				}

				if ((!isThreadAlive("handleBotsRunning") && getThread("handleBotsRunning") == null)
						&& Config.BOT_HANDLER_THREAD_ACTIVE) {
					handleBotsRunning();
					System.out.println("Started new thread: handleBotsRunning");
				}

				if ((!isThreadAlive("handleMulesTrading") && getThread("handleMulesTrading") == null)
						&& Config.MULES_TRADING) {
					handleMulesTrading();
					System.out.println("Started new thread: handleMulesTrading");
				}

				if ((!isThreadAlive("queueThread") && getThread("queueThread") == null) && Config.CAPTCHA) {
					runQueueThread();
					System.out.println("Started new thread: queueThread");
				}

				// Thread sleeping & checking every 30 seconds
				try {
					Thread.sleep(120_000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Checking every 120 seconds for threads to be alive or not");
			}
		});
		mainThread.setName("mainThread");
		mainThread.start();

		threadList.add(mainThread);
	}

	/**
	 * Manages all the threads currently running
	 */
	public static void runThreads() {

		mainThread();

		Thread backupThread = new Thread(() -> {

			while (programIsRunning) {

				checkForAlive();

				System.out.println("[BACKUP] Thread management: " + isThreadAlive("backupThread") + " "
						+ getThread("backupThread"));

				if ((!isThreadAlive("backupThread") && getThread("backupThread") == null)) {
					mainThread();
					System.out.println("Started new thread: backupThread");
				}

				try {
					Thread.sleep(1000);
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
		for (Thread thread : threadList) {
			if (thread.getName().equalsIgnoreCase(threadName)) {
				return thread;
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
		for (Thread thread : threadList) {
			if (thread.getName().equalsIgnoreCase(threadName) && thread.isAlive()) {
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
	public static ArrayList<Thread> getThreadList() {
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
