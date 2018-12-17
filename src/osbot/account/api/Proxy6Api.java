package osbot.account.api;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import osbot.account.creator.HttpRequests;
import osbot.account.global.Config;

public class Proxy6Api {

	public Proxy6Api(String apiKey) {
		setApiKey(apiKey);
	}

	private String apiKey;

	private static final String BASE_URL = "https://proxy6.net/en/api/";

	/**
	 * Returns the Api URL
	 * 
	 * @return
	 */
	private String getApiUrl() {
		return appendString("", new String[] { BASE_URL, apiKey }, false);
	}

	/**
	 * Makes from 2 string 1 string
	 * 
	 * @param strings
	 * @return
	 */
	private String appendString(String between, String[] strings, boolean destroyLast) {
		StringBuilder sb = new StringBuilder();
		for (String str : strings) {
			sb.append(str);
			if (between != null) {
				sb.append(between);
			}
		}
		if (destroyLast) {
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * Sets a proxy for a specific Machine ID
	 * 
	 * @param id
	 * @return
	 */
	public boolean setDescription(String id, int machineId) {
		try {
			String urlParameters = "setdescr?new="
					+ URLEncoder.encode(appendString("", new String[] { Integer.toString(machineId) }, false),
							"UTF-8")
					+ "&ids=" + URLEncoder.encode(appendString(",", new String[] { id }, true), "UTF-8");

			String response = HttpRequests
					.sendGet(appendString("/", new String[] { getApiUrl(), urlParameters }, true));

			JSONParser pars = new JSONParser();
			JSONObject mainJson = (JSONObject) pars.parse(response);

			for (Object key : mainJson.keySet()) {
				String keyStr = (String) key;
				Object keyvalue = mainJson.get(keyStr);

				if (keyStr.equalsIgnoreCase("status")) {
					String keyvalue2 = (String) keyvalue;
					if (keyvalue2.equalsIgnoreCase("yes")) {
						return true;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Returns the balance of the current account
	 * 
	 * @param id
	 * @return
	 */
	public double getBalance() {
		try {

			String response = HttpRequests.sendGet(appendString("/", new String[] { getApiUrl() }, true));

			JSONParser pars = new JSONParser();
			JSONObject mainJson = (JSONObject) pars.parse(response);

			for (Object key : mainJson.keySet()) {
				String keyStr = (String) key;
				Object keyvalue = mainJson.get(keyStr);

				if (keyStr.equalsIgnoreCase("balance")) {
					double value = Double.parseDouble((String) keyvalue);
					return value;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Checks if the proxy is alive or not
	 * 
	 * @param id
	 * @return
	 */
	public boolean isAlive(String id) {
		try {
			String urlParameters = "check?ids="
					+ URLEncoder.encode(appendString(",", new String[] { id }, true), "UTF-8");

			String response = HttpRequests
					.sendGet(appendString("/", new String[] { getApiUrl(), urlParameters }, true));

			JSONParser pars = new JSONParser();
			JSONObject mainJson = (JSONObject) pars.parse(response);

			for (Object key : mainJson.keySet()) {
				String keyStr = (String) key;
				Object keyvalue = mainJson.get(keyStr);

				if (keyStr.equalsIgnoreCase("proxy_status")) {
					return Boolean.valueOf((boolean) keyvalue);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Lists all the proxies it returns from the API
	 */
	public ArrayList<Proxy6Proxy> getProxies() {
		ArrayList<Proxy6Proxy> proxies = new ArrayList<Proxy6Proxy>();
		String urlParameters = "getproxy";
		// "?accountStage=" + URLEncoder.encode(accountStatus, "UTF-8") + "&email="
		// + URLEncoder.encode(email, "UTF-8") + "&number=" + URLEncoder.encode("" +
		// number, "UTF-8");

		try {
			String response = HttpRequests
					.sendGet(appendString("/", new String[] { getApiUrl(), urlParameters }, true));

			JSONParser pars = new JSONParser();
			JSONObject mainJson = (JSONObject) pars.parse(response);
			JSONObject proxyJson = (JSONObject) mainJson.get("list");

			for (Object key : proxyJson.keySet()) {
				// based on you key types
				String keyStr = (String) key;
				Object keyvalue = proxyJson.get(keyStr);

				Proxy6Proxy prox = new Proxy6Proxy(Integer.parseInt(keyStr));

				// Print key and value
				// System.out.println("key: " + keyStr + " value: " + keyvalue);

				JSONObject arrKeyValue = (JSONObject) proxyJson.get(keyStr);

				for (Object z : arrKeyValue.keySet()) {
					String keyStr2 = (String) z;
					Object keyvalue2 = arrKeyValue.get(keyStr2);

					try {
						Field field = prox.getClass().getDeclaredField(keyStr2);
						field.setAccessible(true);

						if (String.class.isAssignableFrom(field.getType())) {
							field.set(prox, keyvalue2);
						} else if (Integer.class.isAssignableFrom(field.getType())) {
							field.set(prox, keyvalue2);
						} else if (Long.class.isAssignableFrom(field.getType())) {
							field.set(prox, keyvalue2);
						}

						// System.out.println("key: " + keyStr2 + " value: " + keyvalue2);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Cannot find linked field!");
					}

				}
				proxies.add(prox);
			}

			return proxies;
		} catch (

		Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param apiKey
	 *            the apiKey to set
	 */
	private void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

}
