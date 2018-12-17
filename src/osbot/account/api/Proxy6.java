package osbot.account.api;

import java.util.ArrayList;

import osbot.account.global.Config;
import osbot.account.mail.MailWarning;
import osbot.database.DatabaseUtilities;

public class Proxy6 {

	/**
	 * Singleton for the Proxy6 class
	 * 
	 * @return
	 */
	public static Proxy6 getSingleton() {
		if (singleton == null) {
			singleton = new Proxy6();
		}
		return singleton;
	}

	public static Proxy6 singleton = null;

	private Proxy6Api api = new Proxy6Api("ec9fc4a5ca-97a29dd340-0fc4be673a");

	public ArrayList<Proxy6Proxy> proxyList = new ArrayList<Proxy6Proxy>();

	/**
	 * Looping through all the proxies
	 */
	public void loop() {
		//For test server
		if (Config.MACHINE_ID != -1) {
			update();

			// resetDescription();
			setDescriptionForComputer();
			updateValidityCheck();

			// TODO should be its own instance with other mail handlers
			double availableBalance = api.getBalance();
			if (availableBalance < 2) {
				MailWarning.initializeMail("Balance on Proxy6 is low! " + availableBalance + " is left!",
						"Please increase your balance on proxy6 to not have any proxies have removed!");
			}
		}
	}

	/**
	 * Adds all the current proxies in a proxy list
	 */
	private void update() {
		// if (proxyList.size() == 0) {
		proxyList.clear();
		proxyList = api.getProxies();
		// }
	}

	private void resetDescription() {
		for (Proxy6Proxy proxy : proxyList) {
			String proxyId = Integer.toString(proxy.getId());
			System.out.println(api.setDescription(proxyId, -1));
		}
	}

	/**
	 * Setting a description for a proxy
	 */
	private void setDescriptionForComputer() {

		// This should have his own loop
		for (Proxy6Proxy proxy : proxyList) {
			try {

				// If the proxy is a static one, then set it to MULE (-2) description
				if ((Config.isStaticMuleProxy(proxy.getIp(), proxy.getPort()))
						|| (Config.isSuperMuleProxy(proxy.getIp(), proxy.getPort()))
								&& Integer.parseInt(proxy.getDescr()) != -2) {
					System.out.println(api.setDescription(Integer.toString(proxy.getId()), -2));
					System.out.println("Set this proxy to a mule proxy!");
				}

				// Adding the muling proxies that all servers use to create their mulers
				if ((Config.isStaticMuleProxy(proxy.getIp(), proxy.getPort())
						|| (Config.isSuperMuleProxy(proxy.getIp(), proxy.getPort())))
						&& !DatabaseUtilities.containsInProxyList(proxy.getIp(), proxy.getPort(), proxy.getUser(),
								proxy.getPass())) {
					DatabaseUtilities.insertProxy(proxy, true);
					System.out.println("Added a muling proxy to the database, all servers will use this");
				}

				// If its not used by our database, but is still logged to use, reset it so
				// other machines may use it
				if (!DatabaseUtilities.containsInProxyList(proxy.getIp(), proxy.getPort(), proxy.getUser(),
						proxy.getPass()) && Integer.parseInt(proxy.getDescr()) == Config.MACHINE_ID
						&& !isAMulingProxy(proxy)) {
					System.out.println("Set this proxy to not used, so other machines may use it");
					System.out.println(api.setDescription(Integer.toString(proxy.getId()), -1));
				}

				// If already contains, but not set a description, then set a description
				if (DatabaseUtilities.containsInProxyList(proxy.getIp(), proxy.getPort(), proxy.getUser(),
						proxy.getPass()) && Integer.parseInt(proxy.getDescr()) != Config.MACHINE_ID
						&& !isAMulingProxy(proxy)) {
					System.out.println("Set a proxy to ours, because it's already used by us");
					System.out.println(api.setDescription(Integer.toString(proxy.getId()), Config.MACHINE_ID));
				}
			} catch (Exception e) {
				System.out.println("Could not set description");
				e.printStackTrace();
			}
		}

		for (Proxy6Proxy proxy : proxyList) {
			try {

				// A server may have a max. amount proxies used up in total
				if (DatabaseUtilities.getTotalProxies().size() >= Config.MAX_PROXIES_PER_MACHINE) {
					System.out.println("Returning because database already has " + Config.MAX_PROXIES_PER_MACHINE
							+ " proxies served");
					break;
				}

				// A server may not use a proxy again when it's already used
				if (Integer.parseInt(proxy.getDescr()) > 0) {
					System.out.println("Returning because this proxy is already used");
					continue;
				}

				// May not use it when it's a muling proxy
				if (isAMulingProxy(proxy)) {
					System.out.println("May not use this proxy, because it is a muling proxy");
					continue;
				}

				DatabaseUtilities.insertProxy(proxy, false);
				String proxyId = Integer.toString(proxy.getId());
				System.out.println(api.setDescription(proxyId, Config.MACHINE_ID));
			} catch (Exception e) {
				System.out.println("Could not set description");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Is the proxy a mule proxy?
	 * 
	 * @param proxy
	 * @return
	 */
	private boolean isAMulingProxy(Proxy6Proxy proxy) {
		return Integer.parseInt(proxy.getDescr()) == -2 || Config.isStaticMuleProxy(proxy.getIp(), proxy.getPort())
				|| Config.isMuleProxy(proxy.getIp(), proxy.getPort())
				|| Config.isSuperMuleProxy(proxy.getIp(), proxy.getPort());
	}

	/**
	 * Checks if the proxy is currently alive, and puts this into the database
	 */
	private void updateValidityCheck() {
		proxyList.forEach(proxy -> {
			try {

				// If contains in proxy list, then update to the database
				if (DatabaseUtilities.containsInProxyList(proxy.getIp(), proxy.getPort(), proxy.getUser(),
						proxy.getPass())) {
					boolean isAlive = api.isAlive(Integer.toString(proxy.getId()));
					// System.out.println("alive: " + isAlive);
					DatabaseUtilities.updateProxyAliveInDatabase(proxy.getIp(), isAlive);
				}

			} catch (Exception e) {
				System.out.println("Could not transform into an integer");
			}
		});

	}

}
