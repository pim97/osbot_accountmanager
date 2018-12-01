package osbot.account.global;

import java.util.ArrayList;
import java.util.Arrays;

import osbot.account.creator.queue.CaptchaQueue;
import osbot.random.RandomUtil;

public class Config {

	/**
	 * Osbot configs
	 */

	public static final String OSBOT_USERNAME = "dormic";

	public static final String OSBOT_PASSWORD = "DuTIbljNuXHDF4T0e7Bk";

	public static String DATABASE_NAME = "";

	public static String DATABASE_USER_NAME = "";

	public static String DATABASE_PASSWORD = "";

	public static String DATABASE_IP = "";

	public static final CaptchaQueue QUEUE = new CaptchaQueue();

	public static final int AMOUNT_OF_TIMEOUTS_BEFORE_GONE = 10;

	public static final ArrayList<String> MULE_PROXY_IP = new ArrayList<String>(Arrays.asList("196.16.115.30:8000"));
	
	public static String getRandomMuleProxy() {
		return MULE_PROXY_IP.get(RandomUtil.getRandomNumberInRange(0, MULE_PROXY_IP.size() - 1));
	}
	
	public static boolean isMuleProxy(String ip, String port) {
		StringBuilder proxyString = new StringBuilder();
		proxyString.append(ip);
		proxyString.append(":");
		proxyString.append(port);
		for (String proxy : MULE_PROXY_IP) {
			if (proxyString.toString().equalsIgnoreCase(proxy)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Bots settings
	 */
	public static int MAX_BOTS_OPEN = 14;

	/**
	 * Configs for threads
	 */

	public static final boolean GUI = false;

	public static final boolean CAPTCHA = false;

	/**
	 * End
	 */

	public static final boolean LOW_CPU = true;

	public static final boolean CREATE_BATCH_FILES_FOR_MULES = true;

	public static final boolean CREATING_ACCOUNTS_THREAD_ACTIVE = true;

	public static final boolean RECOVERING_ACCOUNTS_THREAD_ACTIVE = true;

	public static final boolean BOT_HANDLER_THREAD_ACTIVE = true;

	public static final boolean CLOSE_ON_INACTIVITY = true;

	public static final boolean MULES_TRADING = true;
}
