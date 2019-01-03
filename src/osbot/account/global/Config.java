package osbot.account.global;

import java.util.ArrayList;
import java.util.Arrays;

import osbot.account.creator.queue.CaptchaQueue;
import osbot.database.DatabaseUtilities;
import osbot.random.RandomUtil;

public class Config {

	public static final ArrayList<String> DATABASE_NAMES = new ArrayList<String>(
			Arrays.asList("107.150.38.50_002_elf", "173.208.203.146_001_dragon", "bear_test"));

	/**
	 * Osbot configs
	 */

	public static final String OSBOT_USERNAME = "dormic";

	public static final String OSBOT_PASSWORD = "DuTIbljNuXHDF4T0e7Bk";

	public static final int MAX_PROXIES_PER_MACHINE = 30;

	public static String DATABASE_NAME = "";

	public static String DATABASE_USER_NAME = "";

	public static String DATABASE_PASSWORD = "";

	public static String DATABASE_IP = "";

	public static String PREFIX_EMAIL = "";

	/**
	 * TODO: launch set the id
	 * 
	 * TEST SERVER: ID = 1
	 * 
	 * LIVE SERVERS 173.208.203.146 : 2 107.150.38.50 : 3
	 */
	public static int MACHINE_ID = -1;

	public static final CaptchaQueue QUEUE = new CaptchaQueue();

	public static final int AMOUNT_OF_TIMEOUTS_BEFORE_GONE = 3;

	public static final ArrayList<String> SERVER_MULES = new ArrayList<String>(
			Arrays.asList("185.194.15.2:8000:hGk5CB:s0jRMm"));

	public static boolean isServerMuleProxy(String ip, String port) {
		StringBuilder proxyString = new StringBuilder();
		proxyString.append(ip);
		proxyString.append(":");
		proxyString.append(port);
		for (String proxy : SERVER_MULES) {
			String[] split = proxy.split(":");
			StringBuilder p = new StringBuilder();
			p.append(split[0] + ":" + split[1]);
			if (proxyString.toString().equalsIgnoreCase(p.toString())) {
				return true;
			}
		}
		return false;
	}

	public static final ArrayList<String> STATIC_MULE_PROXIES = new ArrayList<String>(
			Arrays.asList("185.99.99.69:8000:mule", "185.194.13.35:8000:main", "45.4.196.149:8000:mule"));

	public static ArrayList<String> MULE_PROXY_IP = new ArrayList<String>();

	public static String getRandomStaticProxy() {
		return STATIC_MULE_PROXIES.get(RandomUtil.getRandomNumberInRange(0, STATIC_MULE_PROXIES.size() - 1));
	}

	public static ArrayList<String> getMuleProxiesAndInitialize() {
		if (Config.MULE_PROXY_IP.size() == 0) {
			Config.MULE_PROXY_IP = DatabaseUtilities.getMuleProxyAddresses();
		}
		return MULE_PROXY_IP;
	}

	public static ArrayList<String> getAllMuleProxiesWithoutSuperMule() {
		if (Config.MULE_PROXY_IP.size() == 0) {
			Config.MULE_PROXY_IP = DatabaseUtilities.getMuleProxyAddresses();
		}
		ArrayList<String> proxies = new ArrayList<String>();

		for (String proxy : MULE_PROXY_IP) {
			String[] p = proxy.split(":");
			String p3 = p[0] + ":" + p[1];
			for (String proxy2 : SUPER_MULE_PROXY_IP) {
				String[] p2 = proxy2.split(":");
				String p4 = p2[0] + ":" + p2[1];

				if (!p3.equalsIgnoreCase(p4)) {
					proxies.add(p[0] + ":" + p[1] + ":" + p[2] + ":" + p[3]);
				}
			}
		}
		return proxies;
	}

	public static String getRandomMuleProxyWithoutSuperMule() {
		if (Config.MULE_PROXY_IP.size() == 0) {
			Config.MULE_PROXY_IP = DatabaseUtilities.getMuleProxyAddresses();
		}

		ArrayList<String> proxies = new ArrayList<String>();

		for (String proxy : MULE_PROXY_IP) {
			String[] p = proxy.split(":");
			String p3 = p[0] + ":" + p[1];
			for (String proxy2 : SUPER_MULE_PROXY_IP) {
				String[] p2 = proxy2.split(":");
				String p4 = p2[0] + ":" + p2[1];

				if (!p3.equalsIgnoreCase(p4)) {
					proxies.add(p[0] + ":" + p[1] + ":" + p[2] + ":" + p[3]);
				}
			}
		}
		if (proxies.size() == 0) {
			return "";
		} else {
			return proxies.get(RandomUtil.getRandomNumberInRange(0, proxies.size() - 1));
		}
	}

	public static ArrayList<String> SUPER_MULE_PROXY_IP = new ArrayList<String>(
			Arrays.asList("181.177.86.9:9966:mpPo6U:MTA7c8"));

	public static String getRandomSuperMuleProxy() {
		return SUPER_MULE_PROXY_IP.get(RandomUtil.getRandomNumberInRange(0, SUPER_MULE_PROXY_IP.size() - 1));
	}

	public static boolean isStaticMuleProxy(String ip, String port) {
		StringBuilder proxyString = new StringBuilder();
		proxyString.append(ip);
		proxyString.append(":");
		proxyString.append(port);
		for (String proxy : STATIC_MULE_PROXIES) {
			String[] split = proxy.split(":");
			StringBuilder p = new StringBuilder();
			p.append(split[0] + ":" + split[1]);
			if (proxyString.toString().equalsIgnoreCase(p.toString()) && split[2].contains("mule")) {
				return true;
			}
		}
		return false;
	}

	public static boolean isSuperMuleProxy(String ip, String port) {
		StringBuilder proxyString = new StringBuilder();
		proxyString.append(ip);
		proxyString.append(":");
		proxyString.append(port);
		for (String proxy : SUPER_MULE_PROXY_IP) {
			String[] split = proxy.split(":");
			StringBuilder p = new StringBuilder();
			p.append(split[0] + ":" + split[1]);
			if (p.toString().equalsIgnoreCase(proxyString.toString())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isMuleProxy(String ip, String port) {
		if (Config.MULE_PROXY_IP.size() == 0) {
			Config.MULE_PROXY_IP = DatabaseUtilities.getMuleProxyAddresses();
		}

		StringBuilder proxyString = new StringBuilder();
		proxyString.append(ip);
		proxyString.append(":");
		proxyString.append(port);
		for (String proxy : MULE_PROXY_IP) {
			String[] split = proxy.split(":");
			StringBuilder p = new StringBuilder();
			p.append(split[0] + ":" + split[1]);
			if (p.toString().equalsIgnoreCase(proxyString.toString())) {
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

	public static final boolean BREAKING = false;

	/**
	 * End
	 */

	public static final boolean LOW_CPU = false;

	public static final boolean ERROR_IP = false;

	public static final boolean CREATE_BATCH_FILES_FOR_MULES = false;

	public static final boolean CREATING_ACCOUNTS_THREAD_ACTIVE = false;

	public static final boolean RECOVERING_ACCOUNTS_THREAD_ACTIVE = false;

	public static final boolean BOT_HANDLER_THREAD_ACTIVE = false;

	public static final boolean CLOSE_ON_INACTIVITY = false;

	public static final boolean MULES_TRADING = true;
}
