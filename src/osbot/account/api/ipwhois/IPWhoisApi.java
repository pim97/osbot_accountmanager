package osbot.account.api.ipwhois;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.ThreadUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import osbot.bot.BotController;
import osbot.database.DatabaseUtilities;
import osbot.random.RandomUtil;
import osbot.settings.OsbotController;

public class IPWhoisApi implements ProxyInformation {

	private static final int MAXIMUM_LOAD_TIME = 2000;

	/**
	 * The list that's being worked on all the time
	 */
	private Hashtable<String, WhoIsIp> listWithWorkingProxies = new Hashtable<String, WhoIsIp>();

	/**
	 * The cache of a list when it's looking up again and want the previous full
	 * list
	 */
	private Hashtable<String, WhoIsIp> listWithWorkingProxiesCache = new Hashtable<String, WhoIsIp>();

	/**
	 * 
	 * @return
	 */
	private Hashtable<String, WhoIsIp> getListOfWorkingProxies() {
		if (!finishedLookingUpAllProxies) {
			return listWithWorkingProxiesCache;
		}
		return listWithWorkingProxies;
	}

	/**
	 * Returns a random country object code
	 * 
	 * @param countryCode
	 * @return
	 */
	public WhoIsIp getRandomCountryObjectFromCountryCode(String countryCode) {
		ArrayList<WhoIsIp> random = new ArrayList<WhoIsIp>();
		Set<Entry<String, WhoIsIp>> entrySet = getListOfWorkingProxies().entrySet();

		for (Entry<String, WhoIsIp> entry1 : entrySet) {
			if (entry1.getValue().getCountry_code().equalsIgnoreCase(countryCode)) {
				random.add(entry1.getValue());
			}
		}
		if (random.size() > 0) {
			return random.get(RandomUtil.getRandomNumberInRange(0, random.size() - 1));
		}
		return null;
	}

	/**
	 * Returns a random country code after the list has been filled
	 * 
	 * @return
	 */
	public String getRandomCountryCodeFromList() {
		if (getListOfWorkingProxies().size() > 0) {
			Object[] crunchifyKeys = getListOfWorkingProxies().keySet().toArray();
			Object key = crunchifyKeys[new Random()
					.nextInt(crunchifyKeys.length - 1 > 0 ? crunchifyKeys.length - 1 : 0)];
			return getListOfWorkingProxies().get(key).getCountry_code();
		}
		return null;
	}

	/**
	 * Are all the proxies finished looking up?
	 */
	private boolean finishedLookingUpAllProxies = true;

	/**
	 * Checks if the proxy is still working, and if so, putting it to the hashtable
	 * 
	 * @param begin
	 * @param end
	 */
	private void checkValidityOfProxy(int begin, int end) {
		for (int i = begin; i < end; i++) {
			String ipAddres = getIpAddressFromRequest("http://api.ipify.org/",
					"" + i + ".megaproxy.rotating.proxyrack.net", Integer.toString(i));

			if (ipAddres != null) {
				long timeRequest = System.currentTimeMillis();
				ProxyWrapperIpWhosApi wrapper = new ProxyWrapperIpWhosApi();
				WhoIsIp whoIsIpObjectFromProxyWrapper = wrapper.getProxyInformation(ipAddres, Integer.toString(i));
				long responseTime = (System.currentTimeMillis() - timeRequest);

				whoIsIpObjectFromProxyWrapper.setOriginalProxy(i);

				System.out.println(i + " " + whoIsIpObjectFromProxyWrapper.getCountry_code() + " "
						+ whoIsIpObjectFromProxyWrapper.getCountry() + " " + whoIsIpObjectFromProxyWrapper.getCity()
						+ " took: " + responseTime + " ms");

				listWithWorkingProxies.put(Integer.toString(i), whoIsIpObjectFromProxyWrapper);

				whoIsIpObjectFromProxyWrapper.setResponseTime(responseTime);
			}
		}
	}

	/**
	 * Is it done checking validating the proxies?
	 * 
	 * @return
	 */
	private boolean allThreadsDead() {
		for (Thread t : ThreadUtils.getAllThreads()) {
			if (t.getName().contains("validity") && t.isAlive()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 */
	public void checkAllAccountsOnCountryProxyUsage() {
		for (OsbotController bot : BotController.getBots()) {
			if (!bot.getAccount().isUpdated() && bot.getAccount().getCountryProxyCode() == null) {
				DatabaseUtilities.updateProxyAddresCountryOfUserWhereNull(getRandomCountryCodeFromList(), bot.getId());
				bot.getAccount().setUpdated(true);
			}
		}
	}

	public void loop() {
		if (finishedLookingUpAllProxies) {
			int sharedWithThreads = 5;

			finishedLookingUpAllProxies = false;
			listWithWorkingProxies.clear();
			for (int i = 1500; i < 1750; i += sharedWithThreads) {
				int begin = i;
				int end = (begin + sharedWithThreads) - 1;

				Thread proxyValidityThread = new Thread(() -> {
					checkValidityOfProxy(begin, end);
				});
				proxyValidityThread.setName("validity_" + begin);
				proxyValidityThread.start();
			}

			while (!allThreadsDead()) {
				// System.out.println("Waiting to be done");

				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			listWithWorkingProxiesCache = listWithWorkingProxies;
			finishedLookingUpAllProxies = true;
			System.out.println("Successfully executed proxy loading!");
			// checkAllAccountsOnCountryProxyUsage();
		}
	}

	public static IPWhoisApi api = null;

	public static IPWhoisApi getSingleton() {
		if (api == null) {
			api = new IPWhoisApi();
			return api;
		}
		return api;
	}

	public static void main(String args[]) {
		getSingleton().loop();
		System.out.println("code " + getSingleton().getRandomCountryCodeFromList());
	}

	/**
	 * Sends a get request with a proxy addres & port Has a timeout of 5000 seconds
	 * 
	 * @param website
	 * @param proxy
	 * @param port
	 * @return
	 */
	public String getIpAddressFromRequest(String website, String proxy, String port) {
		Scanner scan = null;
		try {
			String url = website;
			URL server = new URL(url);
			Properties systemProperties = System.getProperties();
			systemProperties.setProperty("http.proxyHost", proxy);
			systemProperties.setProperty("http.proxyPort", port);
			HttpURLConnection connection = (HttpURLConnection) server.openConnection();
			connection.setConnectTimeout(MAXIMUM_LOAD_TIME);
			connection.setReadTimeout(MAXIMUM_LOAD_TIME);
			connection.connect();
			InputStream in = connection.getInputStream();

			scan = new Scanner(in);
			while (scan.hasNextLine()) {
				String line = scan.nextLine();

				// System.out.println(line);

				return line;
			}

		} catch (java.net.SocketTimeoutException e) {
			// System.out.println("Took too long to connect to this ip!");
		} catch (SocketException e) {
			// System.out.println("Socket went wrong");
		} catch (IOException e) {
			// System.out.println("Request error");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (scan != null) {
				scan.close();
			}
		}
		return null;
	}

	@Override
	public WhoIsIp getProxyInformation(String proxyIp, String port) {
		try {
			String response = getIpAddressFromRequest("http://pro.ipwhois.io/json/" + proxyIp + "?key=Y6EmzoTOVdXIDLbK",
					proxyIp, port);

			JSONParser pars = new JSONParser();
			JSONObject mainJson = (JSONObject) pars.parse(response);

			WhoIsIp ipInfo = new WhoIsIp();
			for (Object key : mainJson.keySet()) {
				String keyStr = (String) key;
				Object keyvalue = mainJson.get(keyStr);

				try {
					Field field = ipInfo.getClass().getDeclaredField(keyStr);
					field.setAccessible(true);

					if (String.class.isAssignableFrom(field.getType())) {
						field.set(ipInfo, keyvalue);
					} else if (Integer.class.isAssignableFrom(field.getType())) {
						field.set(ipInfo, keyvalue);
					} else if (Long.class.isAssignableFrom(field.getType())) {
						field.set(ipInfo, keyvalue);
					}

				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Cannot find linked field!");
				}

			}

			return ipInfo;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return the finishedLookingUpAllProxies
	 */
	public boolean isFinishedLookingUpAllProxies() {
		return finishedLookingUpAllProxies;
	}

	/**
	 * @param finishedLookingUpAllProxies
	 *            the finishedLookingUpAllProxies to set
	 */
	public void setFinishedLookingUpAllProxies(boolean finishedLookingUpAllProxies) {
		this.finishedLookingUpAllProxies = finishedLookingUpAllProxies;
	}

}
