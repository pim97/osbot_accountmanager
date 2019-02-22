package osbot.account.creator.queue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import com.twocaptcha.api.ProxyType;
import com.twocaptcha.api.TwoCaptchaService;

import osbot.account.AccountStage;
import osbot.account.AccountStatus;
import osbot.account.creator.AccountCreationService;
import osbot.account.creator.RandomNameGenerator;
import osbot.account.creator.SeleniumType;
import osbot.account.runescape.website.RunescapeWebsiteConfig;
import osbot.database.DatabaseProxy;
import osbot.database.DatabaseUtilities;
import osbot.settings.OsbotController;
import osbot.tables.AccountTable;

public class CaptchaQueue implements Runnable {

	private final Queue<Captcha> captchaQueue = new LinkedList<Captcha>();

	private final ArrayList<Captcha> hadCaptchasAccounts = new ArrayList<Captcha>();

	/**
	 * RunescapeWebsiteConfig
	 */
	@Override
	public void run() {

		while (true) {

			System.out.println("Queue size: " + captchaQueue.size() + " to recover: " + accountsToRecover()
					+ " to create: " + accountsToCreate());

			// System.out.println(DatabaseUtilities.getAccountsFromMysqlConnection().size());
			if (accountsToRecover() > 0) {
				addToQueue(SeleniumType.RECOVER_ACCOUNT);
			} else if (accountsToCreate() > 0) {
				addToQueue(SeleniumType.CREATE_VERIFY_ACCOUNT);
			}

			Iterator<Captcha> captchasAlreadyDoneIterator = hadCaptchasAccounts.iterator();
			captchasAlreadyDoneIterator = hadCaptchasAccounts.iterator();

			while (captchasAlreadyDoneIterator.hasNext()) {
				Captcha queue = captchasAlreadyDoneIterator.next();

				if (queue.getSolvedTime() > 0 && System.currentTimeMillis() - queue.getSolvedTime() > 300_000) {
					System.out.println(
							"Removed account from the queue backlog: " + queue.getAccount().getAccount().getUsername());
					captchasAlreadyDoneIterator.remove();
				}
			}

			Iterator<Captcha> waitingQueueIterator = captchaQueue.iterator();

			while (waitingQueueIterator.hasNext()) {
				Captcha queue = waitingQueueIterator.next();

				// Working for the response token
				if (!queue.isWorking()) {
					getResponseToken(queue);
					queue.setWorking(true);
				}

				// When the queue is getting a captcha code
				if (queue.isWorking()) {
					queue.setTriesCaptcha(queue.getTriesCaptcha() + 1);
					System.out.println("Current captcha tries at: " + queue.getTriesCaptcha() + "/200");
					if ((queue.getTriesCaptcha() > 200 && queue.getSolvedTime() == -1)
							|| (queue.getTriesCaptcha() > 1000)) {
						System.out.println("Removed account due to timing out");
						hadCaptchasAccounts.add(queue);

						waitingQueueIterator.remove();
					}
				}

				// Adding it to the queue again when it didnt solve it
				System.out.println("Use the captcha again: "
						+ (!queue.isSuccessfullyUsed() && queue.isOpened() && queue.getCaptchaAgainTry() < 2
								&& queue.getResultKey() != null && queue.getTries() > 80 && queue.isWorking()));

				if (!queue.isSuccessfullyUsed() && queue.isOpened() && queue.getCaptchaAgainTry() < 2
						&& queue.getResultKey() != null && queue.getTries() > 70) {
					addCaptchaToQueueAgain(queue);
				}

				// Clearnig the stuck ones to continue with the process
				if (queue.getResultKey() != null) {
					queue.setTries(queue.getTries() + 1);
					System.out.println("Current tries at: " + queue.getTries() + "/120");
					if ((queue.getTries() > 120 && queue.getSolvedTime() == -1) || (queue.getTries() > 500)) {
						System.out.println("Removed account due to timing out");
						hadCaptchasAccounts.add(queue);
						waitingQueueIterator.remove();

					}
				}

				// Got the response token? Then open website etc..
				if (queue.getResultKey() != null && !queue.isOpened()) {
					System.out.println("Searching for type to solve: " + queue.getType());
					if (queue.getType() == SeleniumType.CREATE_VERIFY_ACCOUNT) {
						openCreation(queue);
					} else if (queue.getType() == SeleniumType.RECOVER_ACCOUNT) {
						openRecovery(queue);
					}
					queue.setOpened(true);

				}
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void removeFromQueue(Captcha queue) {
		System.out.println("Key found, response token is: " + queue.getResultKey());
		queue.setSolvedTime(System.currentTimeMillis());
		hadCaptchasAccounts.add(queue);

		Iterator<Captcha> waitingQueueIterator = captchaQueue.iterator();
		waitingQueueIterator = captchaQueue.iterator();

		while (waitingQueueIterator.hasNext()) {
			Captcha cap = waitingQueueIterator.next();
			// Working for the response token

			if (cap.equals(queue)) {
				System.out.println("Removed " + queue + "from the queue");
				waitingQueueIterator.remove();

				addCaptchaToQueueAgain(queue);
				continue;
			}

			if (cap == null || cap.getAccount() == null || cap.getAccount().getAccount() == null) {
				break;
			}

			if (cap.getAccount().getAccount().getUsername()
					.equalsIgnoreCase(queue.getAccount().getAccount().getUsername())) {
				System.out.println("Removed " + queue.getAccount().getAccount().getUsername() + " from the queue");
				waitingQueueIterator.remove();

				addCaptchaToQueueAgain(queue);
				continue;

			}
		}
	}

	private void addCaptchaToQueueAgain(Captcha queue) {
		if (!queue.isSuccessfullyUsed() && queue.getCaptchaAgainTry() < 2) {

			System.out.println("Captcha code wasn't used successfully, using again! CODE:01");
			queue.setOpened(false);
			queue.setCaptchaAgainTry(queue.getCaptchaAgainTry() + 1);
			captchaQueue.add(queue);
		}
	}

	private int accountsToRecover() {
		return DatabaseUtilities.getAccountsToBeRecovered().size();
	}

	private int accountsToCreate() {
		int amount = 0;

		HashMap<DatabaseProxy, Integer> hash = DatabaseUtilities.oneExistsInOther(DatabaseUtilities.getTotalProxies(),
				DatabaseUtilities.getUsedProxies());

		for (Entry<DatabaseProxy, Integer> entry : hash.entrySet()) {
			DatabaseProxy key = entry.getKey();
			Integer value = entry.getValue();

			if (value < 3) {
				amount += value;
			}
		}
		return amount;
	}

	private void openCreation(Captcha captcha) {
//		Thread t1 = new Thread(() -> {
//			DatabaseProxy proxy = new DatabaseProxy(captcha.getAccount().getAccount().getProxyIp(),
//					captcha.getAccount().getAccount().getProxyPort(),
//					captcha.getAccount().getAccount().getProxyUsername(),
//					captcha.getAccount().getAccount().getProxyPassword());
//
//			AccountCreationService.launchRunescapeWebsite(proxy, captcha.getAccount(),
//					SeleniumType.CREATE_VERIFY_ACCOUNT, captcha);
//			System.out.println("Opening website for: " + captcha.getType());
//		});
//		t1.start();

	}

	private void openRecovery(Captcha captcha) {

//		Thread t2 = new Thread(() -> {
//			DatabaseProxy proxy = new DatabaseProxy(captcha.getAccount().getAccount().getProxyIp(),
//					captcha.getAccount().getAccount().getProxyPort(),
//					captcha.getAccount().getAccount().getProxyUsername(),
//					captcha.getAccount().getAccount().getProxyPassword());
//
//			AccountCreationService.launchRunescapeWebsite(proxy, captcha.getAccount(), SeleniumType.RECOVER_ACCOUNT,
//					captcha);
//			System.out.println("Opening website for: " + captcha.getType());
//		});
//		t2.start();
	}

	private void getResponseToken(Captcha captcha) {

		new Thread(() -> {
			String responseToken = null;

			if (captcha == null || captcha.getAccount() == null || captcha.getAccount().getAccount() == null
					|| captcha.getAccount().getAccount().getProxyIp() == null
					|| captcha.getAccount().getAccount().getProxyPort() == null
					|| captcha.getAccount().getAccount().getProxyUsername() == null
					|| captcha.getAccount().getAccount().getProxyPassword() == null) {
				return;
			}

			String proxyIp = captcha.getAccount().getAccount().getProxyIp(),
					port = captcha.getAccount().getAccount().getProxyPort(),
					username = captcha.getAccount().getAccount().getProxyUsername(),
					password = captcha.getAccount().getAccount().getProxyPassword();
			String websiteLink = captcha.getType() == SeleniumType.CREATE_VERIFY_ACCOUNT
					? RunescapeWebsiteConfig.RUNESCAPE_CREATE_ACCOUNT_URL
					: RunescapeWebsiteConfig.RUNESCAPE_RECOVER_ACCOUNT_URL;

			TwoCaptchaService service = new TwoCaptchaService("8ff2e630e82351bdc3f0b00af2e026b9",
					"6LccFA0TAAAAAHEwUJx_c1TfTBWMTAOIphwTtd1b", websiteLink, proxyIp, port, username, password,
					ProxyType.SOCKS5);

			try {
				responseToken = service.solveCaptcha();
				System.out.println("The response token is: " + responseToken);
				captcha.setResultKey(responseToken);
			} catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				removeFromQueue(captcha);
			}

		}).start();
	}

	private boolean containsInQueue(OsbotController account) {
		Iterator<Captcha> waitingQueueIterator = captchaQueue.iterator();
		waitingQueueIterator = captchaQueue.iterator();

		for (Captcha captcha : hadCaptchasAccounts) {
			// Check if null
			if (captcha == null || captcha.getAccount() == null
					|| captcha.getAccount().getAccount().getUsername() == null || account == null
					|| account.getAccount() == null || account.getAccount().getUsername() == null) {
				return false;
			}
			// Already did the account?
			if (captcha.getAccount().getAccount().getUsername().equalsIgnoreCase(account.getAccount().getUsername())) {
				return true;
			}
		}

		while (waitingQueueIterator.hasNext()) {
			Captcha queue = waitingQueueIterator.next();

			if (queue == null || queue.getAccount() == null || queue.getAccount().getAccount().getUsername() == null
					|| account == null || account.getAccount() == null || account.getAccount().getUsername() == null) {
				return false;
			}
			if (queue.getAccount().getAccount().getUsername().equalsIgnoreCase(account.getAccount().getUsername())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds an element to the queue
	 * 
	 * @param websiteLinkOnCaptcha
	 */
	public void addToQueue(SeleniumType type) {
		if (captchaQueue.size() > 1) {
			// System.out.println("Queue is full!");
			return;
		}

		System.out.println("Queue size is now: " + captchaQueue.size());
		Captcha captcha = new Captcha("8ff2e630e82351bdc3f0b00af2e026b9", "6LccFA0TAAAAAHEwUJx_c1TfTBWMTAOIphwTtd1b",
				type);
		captcha.setType(type);

		if (type == SeleniumType.CREATE_VERIFY_ACCOUNT) {
			HashMap<DatabaseProxy, Integer> hash = DatabaseUtilities
					.oneExistsInOther(DatabaseUtilities.getTotalProxies(), DatabaseUtilities.getUsedProxies());

			for (Entry<DatabaseProxy, Integer> entry : hash.entrySet()) {
				DatabaseProxy key = entry.getKey();
				Integer value = entry.getValue();

				if (value < 3) {

					RandomNameGenerator name = new RandomNameGenerator();

					AccountTable table = new AccountTable(-1, "test", name.generateRandomNameString(), 318,
							key.getProxyIp(), key.getProxyPort(), true, AccountStatus.AVAILABLE,
							AccountStage.TUT_ISLAND, 0);
					table.setPassword(name.generateRandomNameString());
					table.setProxyUsername(key.getProxyUsername());
					table.setProxyPassword(key.getProxyPassword());
					table.setBankPin("0000");
					OsbotController bot = new OsbotController(-1, table);

					captcha.setAccount(bot);
					System.out.println("Set account for " + type.name());
					break;
				}
			}

		} else if (type == SeleniumType.RECOVER_ACCOUNT) {
			for (OsbotController bot : DatabaseUtilities.getAccountsToBeRecovered()) {
				if (!containsInQueue(bot)) {
					captcha.setAccount(bot);
					System.out.println(
							"Set account: " + captcha.getAccount().getAccount().getUsername() + " for " + type.name());
					break;
				}
			}
		}
		captchaQueue.add(captcha);
		System.out.println("Added type " + type.name() + " to the queue, size: " + captchaQueue.size());
	}

	/**
	 * @return the captchaQueue
	 */
	public Queue<Captcha> getCaptchaQueue() {
		return captchaQueue;
	}

	/**
	 * @return the hadCaptchasAccounts
	 */
	public ArrayList<Captcha> getHadCaptchasAccounts() {
		return hadCaptchasAccounts;
	}

}
