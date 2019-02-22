package osbot.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import osbot.account.AccountStage;
import osbot.account.AccountStatus;
import osbot.account.LoginStatus;
import osbot.account.api.ipwhois.IPWhoisApi;
import osbot.account.api.proxy6.Proxy6Proxy;
import osbot.account.creator.AccountCreate;
import osbot.account.creator.AccountCreationService;
import osbot.account.creator.RandomNameGenerator;
import osbot.account.creator.SeleniumType;
import osbot.account.global.Config;
import osbot.account.handler.BotHandler;
import osbot.account.handler.GeckoHandler;
import osbot.account.webdriver.WebdriverFunctions;
import osbot.bot.BotController;
import osbot.settings.OsbotController;
import osbot.tables.AccountTable;

public class DatabaseUtilities {

	public static void insertIntoTable(String database, AccountTable account) {

		// the mysql insert statement
		String query = " insert into " + database
				+ ".account (name, password, bank_pin, day, month, year, proxy_ip, proxy_port, world_number, low_cpu_mode, status, email, account_stage)"
				+ " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		// create the mysql insert preparedstatement
		PreparedStatement preparedStmt;
		try {
			preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);

			// preparedStmt.setInt(1, account.getId());
			preparedStmt.setString(1, account.getUsername());
			preparedStmt.setString(2, account.getPassword());
			preparedStmt.setString(3, account.getBankPin());

			preparedStmt.setInt(4, account.getDay());
			preparedStmt.setInt(5, account.getMonth());
			preparedStmt.setInt(6, account.getYear());

			preparedStmt.setString(7, account.getProxyIp());
			preparedStmt.setString(8, account.getProxyPort());
			preparedStmt.setInt(9, account.getWorld());
			preparedStmt.setBoolean(10, account.isLowCpuMode());
			preparedStmt.setString(11, account.getStatus().name());
			preparedStmt.setString(12, account.getEmail());
			preparedStmt.setString(13, account.getStage().name());

			// execute the preparedstatement
			preparedStmt.execute();
			preparedStmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Insert an account into the database when it's being created with Selenium
	 * 
	 * @param account
	 */
	public static void insertIntoTable(AccountTable account) {

		// the mysql insert statement
		String query = " insert into account (name, password, bank_pin, day, month, year, proxy_ip, proxy_port, world_number, low_cpu_mode, status, email, account_stage, country_code_proxy_usage)"
				+ " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		// create the mysql insert preparedstatement
		PreparedStatement preparedStmt;
		try {
			preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);

			// preparedStmt.setInt(1, account.getId());
			preparedStmt.setString(1, account.getUsername());
			preparedStmt.setString(2, account.getPassword());
			preparedStmt.setString(3, account.getBankPin());

			preparedStmt.setInt(4, account.getDay());
			preparedStmt.setInt(5, account.getMonth());
			preparedStmt.setInt(6, account.getYear());

			preparedStmt.setString(7, account.getProxyIp());
			preparedStmt.setString(8, account.getProxyPort());
			preparedStmt.setInt(9, account.getWorld());
			preparedStmt.setBoolean(10, account.isLowCpuMode());
			preparedStmt.setString(11, account.getStatus().name());
			preparedStmt.setString(12, account.getEmail());
			preparedStmt.setString(13, account.getStage().name());
			preparedStmt.setString(14, account.getCountryProxyCode());

			// execute the preparedstatement
			preparedStmt.execute();
			preparedStmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Inserts a proxy into the database
	 * 
	 * @param proxy
	 * @param mule
	 */
	public static void insertProxy(Proxy6Proxy proxy, boolean mule) {
		if (DatabaseUtilities.containsInProxyList(proxy.getIp(), proxy.getPort(), proxy.getUser(), proxy.getPass())) {
			System.out.println("You're already using this proxy, may not use it again");
			return;
		}

		// the mysql insert statement
		String query = " insert into proxies (ip_addres,port,username,password,mule_proxy,is_alive)"
				+ " values (?, ?, ?, ?, ?, ?)";

		// create the mysql insert preparedstatement
		PreparedStatement preparedStmt;
		try {
			preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);

			preparedStmt.setString(1, proxy.getIp());
			preparedStmt.setString(2, proxy.getPort());
			preparedStmt.setString(3, proxy.getUser());

			preparedStmt.setString(4, proxy.getPass());
			preparedStmt.setInt(5, mule ? 1 : 0);
			preparedStmt.setInt(6, 1);

			// execute the preparedstatement
			preparedStmt.execute();
			preparedStmt.close();

			System.out.println("Inserted new proxy into the database!");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Returns the amount of super mules there is currently
	 * 
	 * @return
	 */
	public static int getAmountOfSuperMules() {
		int amountOfSuperMules = 0;
		for (OsbotController bot : BotController.getBots()) {
			if (bot.getAccount().getStatus() == AccountStatus.SUPER_MULE) {
				amountOfSuperMules++;
			}
		}
		return amountOfSuperMules;
	}

	/**
	 * Sometimes too many super mules accounts are created, this should cut them
	 * off, not the ideal solution but okay for now
	 */
	public static void checkIfAccountIsTooMany() {
		if (getAmountOfSuperMules() > 1) {
			OsbotController removedBot = null;

			for (OsbotController bot : BotController.getBots()) {
				if (getAmountOfSuperMules() == 1) {
					break;
				}

				if (bot.getAccount().getStatus() == AccountStatus.SUPER_MULE
						&& Integer.parseInt(bot.getAccount().getAccountValue()) == 0
						&& bot.getAccount().getStage() == AccountStage.UNKNOWN
						&& Config.isSuperMuleProxy(bot.getAccount().getProxyIp(), bot.getAccount().getProxyPort())) {
					removedBot = bot;
					deleteFromTable(bot.getId());
					System.out.println("Tried to delete: " + bot.getAccount().getEmail());
					break;
				}

			}
			if (removedBot != null) {
				BotController.getBots().remove(removedBot);
				System.out.println("Removed bot from the list");
			}

		}
	}

	/**
	 * Deletes an account in the database
	 * 
	 * @param id
	 */
	public static void deleteFromTable(int id) {

		String query = "delete from account where id = ?";
		PreparedStatement preparedStmt;
		try {
			preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);

			preparedStmt.setInt(1, id);

			// execute the preparedstatement
			preparedStmt.execute();
			preparedStmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Cointains in the proxy list where proxies are alive
	 * 
	 * @param address
	 * @param port
	 * @param username
	 * @param password
	 * @return
	 */
	public static boolean containsInProxyList(String address, String port, String username, String password) {
		for (DatabaseProxy proxy : getTotalProxiesWithMuleProxiesAndExceptAlive()) {
			if (proxy.getProxyIp().equalsIgnoreCase(address) && proxy.getProxyPort().equalsIgnoreCase(port)
					&& proxy.getProxyUsername().equalsIgnoreCase(username)
					&& proxy.getProxyPassword().equalsIgnoreCase(password)) {
				return true;
			}
		}
		return false;
	}

	public static final int SERVER_MULE_TRADE_THRESHOLD = 2_500_000;

	public static boolean isOverThresholdToTradeToServerMule() {
		String sql = "SELECT SUM(account_value) as amount FROM account WHERE account_stage=\"UNKNOWN\" AND (status=\"MULE\" OR status=\"SUPER_MULE\")";

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {
				while (resultSet.next()) {

					int amount = resultSet.getInt("amount");

					if (amount >= SERVER_MULE_TRADE_THRESHOLD) {
						return true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean shouldMuleTradeWithSuperMuleWhenNoOneIsTrading() {
		String sql = "SELECT COUNT(*) as count_null_trading, (SELECT COUNT(*) FROM account WHERE status=\"MULE\" AND account_stage=\"UNKNOWN\") as total FROM account WHERE status=\"MULE\" AND account_stage=\"UNKNOWN\" AND trade_with_other IS NULL";

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {
				while (resultSet.next()) {

					int notTrading = resultSet.getInt("count_null_trading");
					int totalMules = resultSet.getInt("total");

					if ((notTrading == totalMules) && (notTrading > 0 && totalMules > 0)) {
						return true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static ArrayList<DatabaseProxy> getTotalProxiesWithMuleProxiesAndExceptAlive() {
		String sql = "SELECT * FROM proxies as p";
		ArrayList<DatabaseProxy> proxiesInDatabase = new ArrayList<DatabaseProxy>();

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {
				while (resultSet.next()) {

					proxiesInDatabase
							.add(new DatabaseProxy(resultSet.getString("ip_addres"), resultSet.getString("port"),
									resultSet.getString("username"), resultSet.getString("password")));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return proxiesInDatabase;
	}

	public static ArrayList<DatabaseProxy> getTotalProxiesWithMuleProxies() {
		String sql = "SELECT * FROM proxies as p";
		ArrayList<DatabaseProxy> proxiesInDatabase = new ArrayList<DatabaseProxy>();

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {
				while (resultSet.next()) {

					proxiesInDatabase
							.add(new DatabaseProxy(resultSet.getString("ip_addres"), resultSet.getString("port"),
									resultSet.getString("username"), resultSet.getString("password")));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return proxiesInDatabase;
	}

	/**
	 * 
	 * @return
	 */
	public static ArrayList<DatabaseProxy> getTotalProxies() {
		String sql = "SELECT * FROM proxies as p WHERE p.mule_proxy=0";
		ArrayList<DatabaseProxy> proxiesInDatabase = new ArrayList<DatabaseProxy>();

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {
				while (resultSet.next()) {

					proxiesInDatabase
							.add(new DatabaseProxy(resultSet.getString("ip_addres"), resultSet.getString("port"),
									resultSet.getString("username"), resultSet.getString("password")));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return proxiesInDatabase;
	}

	public static ArrayList<String> getMuleProxyAddresses() {
		String sql = "SELECT * FROM proxies WHERE mule_proxy = 1";
		ArrayList<String> proxies = new ArrayList<String>();

		String address, port, user, password;

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {
				while (resultSet.next()) {
					StringBuilder sb = new StringBuilder();

					address = resultSet.getString("ip_addres");
					port = resultSet.getString("port");
					user = resultSet.getString("username");
					password = resultSet.getString("password");
					sb.append(address);
					sb.append(":");
					sb.append(port);
					sb.append(":");
					sb.append(user);
					sb.append(":");
					sb.append(password);

					proxies.add(sb.toString());
				}
				return proxies;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return proxies;
	}

	public static boolean isErrorProxy(String proxyIp, String proxyPort) {
		String sql = "SELECT error_ip FROM proxies WHERE ip_addres = '" + proxyIp + "' AND port = '" + proxyPort + "'";
		boolean error = false;

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {
				while (resultSet.next()) {
					error = Boolean.parseBoolean(resultSet.getString("error_ip"));
				}
				return error;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return error;
	}

	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static String getEmailFromUsername(String username) {
		String sql = "SELECT email FROM account WHERE email=\"" + username + "\"";
		String email = null;

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {
				while (resultSet.next()) {
					email = resultSet.getString("email");

				}
				return email;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return email;
	}

	public static boolean checkAlreadyLockedProxies(String proxy, String email) {

		String sql = "SELECT email,proxy_ip,updated_at FROM account WHERE status=\"LOCKED_TIMEOUT\" AND proxy_ip= '"
				+ proxy + "' AND email <> '" + email + "'";

		System.out.println("USING SQL: " + sql);
		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {

				boolean alreadyLocked = false;
				while (resultSet.next()) {
					alreadyLocked = true;
				}

				if (alreadyLocked) {
					try {
						String query = "UPDATE account SET status = ? WHERE email=?";
						PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection()
								.prepareStatement(query);
						preparedStmt.setString(1, AccountStatus.LOCKED_TIMEOUT.name());
						preparedStmt.setString(2, email);

						// execute the java preparedstatement
						preparedStmt.executeUpdate();
						preparedStmt.close();

						System.out.println("Updated account to locked_timeout because ip was timed out E10");

						return true;

					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * 
	 * @return
	 */
	public static ArrayList<DatabaseProxy> getUsedProxies() {
		String sql = "SELECT ac.*, p.username as p_us, p.password as p_pass FROM account AS ac INNER JOIN proxies AS p ON p.ip_addres=ac.proxy_ip WHERE ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND email IS NOT NULL AND ac.status <> \"OUT_OF_MONEY\" AND email IS NOT NULL AND ac.status <> \"LOCKED_INGAME\" AND ac.status <> \"BANNED\"  AND ac.status <> \"TIMEOUT\" AND ac.status <> \"TASK_TIMEOUT\" AND ac.status <> \"INVALID_PASSWORD\"";
		ArrayList<DatabaseProxy> proxiesOutDatabase = new ArrayList<DatabaseProxy>();

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {
				while (resultSet.next()) {

					proxiesOutDatabase
							.add(new DatabaseProxy(resultSet.getString("proxy_ip"), resultSet.getString("proxy_port"),
									resultSet.getString("p_us"), resultSet.getString("p_pass")));

				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return proxiesOutDatabase;
	}

	public static int accountsToCreate2() {
		ArrayList<DatabaseProxy> proxy = getUsedProxies2();
		int count = 0;

		for (DatabaseProxy pro : proxy) {
			int amount = (2 - pro.getUsedCount()) > 0 ? (2 - pro.getUsedCount()) : 0;

			count += amount;
		}
		return count;
	}

	public static int totalAccountsAvailable() {
		ArrayList<DatabaseProxy> proxy = getUsedProxies2();
		int count = 0;

		for (DatabaseProxy pro : proxy) {
			count += pro.getUsedCount();
		}
		return count;
	}

	public static void deleteFromProxyList(String ip, String port) {
		try {
			String query = "DELETE FROM proxies WHERE ip_addres = ? AND port = ?";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, ip);
			preparedStmt.setString(2, port);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<DatabaseProxy> getUsedProxies2() {
		String sql = "SELECT * FROM ( SELECT SUM(CASE WHEN ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND ac.status <> \"LOCKED_INGAME\"  AND ac.status <> \"OUT_OF_MONEY\" AND email IS NOT NULL AND ac.status <> \"BANNED\" AND ac.status <> \"TIMEOUT\" AND ac.status <> \"TASK_TIMEOUT\"  AND ac.status <> \"INVALID_PASSWORD\" THEN 1 ELSE 0 END) AS count, p.ip_addres AS proxy, p.port, p.username, p.password, p.mule_proxy, p.is_alive as alive FROM proxies AS p LEFT JOIN account AS ac ON ac.proxy_ip=p.ip_addres AND ac.proxy_port=p.port GROUP BY p.ip_addres, p.port, p.username, p.password) AS proxy WHERE mule_proxy <> 1";
		ArrayList<DatabaseProxy> proxiesOutDatabase = new ArrayList<DatabaseProxy>();

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {
				while (resultSet.next()) {

					int count = resultSet.getInt("count");
					String ip = resultSet.getString("proxy");
					String port = resultSet.getString("port");
					String username = resultSet.getString("username");
					String password = resultSet.getString("password");
					boolean isOnline = resultSet.getBoolean("alive");

					DatabaseProxy proxy = new DatabaseProxy(ip, port, username, password);
					proxy.setUsedCount(count);
					proxy.setOnline(isOnline);

					proxiesOutDatabase.add(proxy);

				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return proxiesOutDatabase;
	}

	/**
	 * 
	 * @return
	 */
	public static int getMaxInteger() {
		String sql = "SELECT MAX(id) as max FROM `account`";

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {
				while (resultSet.next()) {
					int max = resultSet.getInt("max");

					return max + 1;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static boolean updateAtASpecificTimeToMule() {
		try {
			String query = "UPDATE account SET account_stage = ? WHERE account_stage='MINING_LEVEL_TO_15' AND account_value > 3500 AND status='AVAILABLE'"
					+ " AND updated_at BETWEEN SUBDATE(NOW(),1) AND NOW()";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, AccountStage.MULE_TRADING.name());

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();

			System.out.println("Updated account to mule!");

			for (OsbotController bots : BotController.getBots()) {
				if ((bots.getAccount().getStage() == AccountStage.MINING_LEVEL_TO_15
						|| bots.getAccount().getStage() == AccountStage.MULE_TRADING)) {
					BotController.killProcess(bots.getPidId());
					System.out.println(
							"Killed the process: " + bots.getPidId() + " because is going to trade with a mule!");
				}
			}

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Updates the account in the database
	 * 
	 * @param newPassword
	 * @param accountId
	 * @return
	 */
	public static boolean updatePasswordInDatabase(String newPassword, int accountId) {
		try {
			String query = "UPDATE account SET password = ?, status = ? WHERE id=?";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, newPassword);
			preparedStmt.setString(2, "Available");
			preparedStmt.setInt(3, accountId);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();

			System.out.println("Updated account in database with new password!");

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Update proxyaddress where null
	 * 
	 * @param newPassword
	 * @param accountId
	 * @return
	 */
	public static boolean updateProxyAddresCountryOfUserWhereNull(String countryCode, int accountId) {
		try {
			String query = "UPDATE account SET country_code_proxy_usage = ? WHERE id=?";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, countryCode);
			preparedStmt.setInt(2, accountId);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean updateProxyAliveInDatabase(String ipAddress, boolean alive) {
		try {
			int bool = 0;
			if (alive) {
				bool = 1;
			} else {
				bool = 0;
			}

			String query = "UPDATE proxies SET is_alive = ? WHERE ip_addres=?";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setInt(1, bool);
			preparedStmt.setString(2, ipAddress);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();

			// System.out.println("Updated account in database with new alive status!");

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean isServerMule(AccountTable account) {
		return (Config.isServerMuleProxy(account.getProxyIp(), account.getProxyPort()));
	}

	/**
	 * 
	 */
	public static void closeBotsWhenNotActive() {
		long startTime = System.currentTimeMillis();

		for (OsbotController bot : BotController.getBots()) {

			// If starting it, then it may not harm that process to prevent any weird things
			if (bot.isStartingUp()) {
				System.out.println("May not harm this process, it is starting up right now.");
				continue;
			}

			if (bot.getAccount().isUpdated()) {
				// System.out.println("Waiting for the next account refresh to update the
				// account again");
				continue;
			}

			// Is not logged in but in database still logged in
			if ((bot.getAccount().getLoginStatus() == LoginStatus.INITIALIZING
					|| bot.getAccount().getLoginStatus() == LoginStatus.LOGGED_IN) && bot.getPidId() <= 0) {
				bot.getAccount().setUpdated(true);
				System.out.println("KILLING: 5");

				if (isServerMule(bot.getAccount())) {
					DatabaseUtilities.updateLoginStatus("server_muling", LoginStatus.DEFAULT, bot.getId());
				} else {
					DatabaseUtilities.updateLoginStatus(LoginStatus.DEFAULT, bot.getId());
				}
			}

			// If the pid ID is lower than 0, then it's not launched
			if (bot.getPidId() <= 0) {
				continue;
			}

			boolean isProcessRunningOnWindows = BotHandler.isProcessIdRunningOnWindows(bot.getPidId());

			boolean mustCloseBecauseMoreThan1CurrentlyOpenBasedOnUsername = BotHandler.doubleRunningOnProcess(bot) > 1;

			if (mustCloseBecauseMoreThan1CurrentlyOpenBasedOnUsername) {
				bot.getAccount().setUpdated(true);
				BotController.killProcess(bot.getPidId());
				bot.setPidId(-1);
				if (isServerMule(bot.getAccount())) {
					DatabaseUtilities.updateLoginStatus("server_muling", LoginStatus.DEFAULT, bot.getId());
				} else {
					DatabaseUtilities.updateLoginStatus(LoginStatus.DEFAULT, bot.getId());
				}
				System.out.println("KILLING: 6 because of duplicated windows open");
			}

			// When account has started, but not logged in between 80 seconds
			if (bot.getAccount().getLoginStatus() != null
					&& bot.getAccount().getLoginStatus() == LoginStatus.INITIALIZING && (System.currentTimeMillis()
							- bot.getStartTime() > (Config.NEW_PROXYRACK_CONFIGURATION ? 300_000 : 150_000))) {
				bot.getAccount().setUpdated(true);
				BotController.killProcess(bot.getPidId());
				bot.setPidId(-1);
				if (isServerMule(bot.getAccount())) {
					DatabaseUtilities.updateLoginStatus("server_muling", LoginStatus.DEFAULT, bot.getId());
				} else {
					DatabaseUtilities.updateLoginStatus(LoginStatus.DEFAULT, bot.getId());
				}
				System.out.println("KILLING: 2");
			}

			// When the pid is not on the machine active anymore
			if (!isProcessRunningOnWindows) {
				bot.getAccount().setUpdated(true);
				BotController.killProcess(bot.getPidId());
				bot.setPidId(-1);
				if (isServerMule(bot.getAccount())) {
					DatabaseUtilities.updateLoginStatus("server_muling", LoginStatus.DEFAULT, bot.getId());
				} else {
					DatabaseUtilities.updateLoginStatus(LoginStatus.DEFAULT, bot.getId());
				}
				System.out.println("KILLING: 1");
			}

			// When the process is running, but not logged in, then set status back to
			// default
			// if (isProcessRunningOnWindows && bot.getAccount().getLoginStatus() ==
			// LoginStatus.DEFAULT) {
			// bot.getAccount().setUpdated(true);
			// BotController.killProcess(bot.getPidId());
			// bot.setPidId(-1);
			// System.out.println("Killing 4");
			// }
		}

		// When a pid is active on the system, but not in the program
		List<Integer> runningPidsOnMachine = BotController.getJavaPIDsWindows();
		ArrayList<Integer> currentPidsInProgram = new ArrayList<Integer>();

		// Creating the left overs
		currentPidsInProgram.addAll(BotController.getJavaPIDsWindows());
		runningPidsOnMachine.removeAll(currentPidsInProgram);

		runningPidsOnMachine.forEach(b -> {
			if (b != BotHandler.MAIN_PID) {
				BotController.killProcess(b);
				System.out.println("KILLING: 3");
			}
		});

		System.out.println("Close bots when active took: " + (System.currentTimeMillis() - startTime) + " ms");
	}

	public static LoginStatus getLoginStatus(int accountId) {
		String sql = "SELECT login_status FROM account WHERE id=" + accountId + "";

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);
			try {
				LoginStatus status = null;

				while (resultSet.next()) {
					String loginStatus = resultSet.getString("login_status");
					status = LoginStatus.valueOf(loginStatus);
				}
				return status;

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean updateLoginStatus(String database, LoginStatus status, int accountId) {
		try {
			String query = "UPDATE " + database + ".account SET login_status = ? WHERE id=?";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, status.name());
			preparedStmt.setInt(2, accountId);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();

			System.out.println("Updated account in database with login status: " + status.name().toUpperCase());

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean updateLoginStatus(LoginStatus status, int accountId) {
		try {
			String query = "UPDATE account SET login_status = ? WHERE id=?";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, status.name());
			preparedStmt.setInt(2, accountId);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();

			System.out.println("Updated account in database with login status: " + status.name().toUpperCase());

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean updateProxyStatusByIp(int status, String ip) {
		try {
			String query = "UPDATE proxies SET error_ip= ? WHERE ip_addres = ?";
			// String query = "UPDATE account SET status = ? WHERE proxy_ip=? BETWEEN
			// SUBDATE(NOW(),1) AND NOW() AND status=\"LOCKED\"";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setInt(1, status);
			preparedStmt.setString(2, ip);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();

			System.out.println(preparedStmt.toString());
			System.out.println("Updated proxy status in database with ip! " + status + " ip: " + ip);
			preparedStmt.close();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean updateStatusOfAccountByIp(AccountStatus status, String ip) {
		try {
			String query = "UPDATE account SET status = ? WHERE proxy_ip=? AND status=\"LOCKED_TIMEOUT\"";
			// String query = "UPDATE account SET status = ? WHERE proxy_ip=? BETWEEN
			// SUBDATE(NOW(),1) AND NOW() AND status=\"LOCKED\"";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, status.name());
			preparedStmt.setString(2, ip);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();

			System.out.println(preparedStmt.toString());
			System.out.println("Updated account status in database with ip! " + status.name() + " ip: " + ip);
			preparedStmt.close();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean updateStatusOfAccountByIp(AccountStatus status, String ip, String email) {
		try {
			String query = "UPDATE account SET status = ? WHERE proxy_ip=? AND email = ? AND status=\"LOCKED_TIMEOUT\"";
			// String query = "UPDATE account SET status = ? WHERE proxy_ip=? BETWEEN
			// SUBDATE(NOW(),1) AND NOW() AND status=\"LOCKED\"";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, status.name());
			preparedStmt.setString(2, ip);
			preparedStmt.setString(3, email);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();

			System.out.println(preparedStmt.toString());
			System.out.println("Updated account status in database with ip! " + status.name() + " ip: " + ip);
			preparedStmt.close();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean updateProxyStatus(String proxyIp, String proxyPort, boolean error) {
		try {
			String query = "UPDATE proxies SET error_ip = ? WHERE ip_addres = ? AND port = ?";
			System.out.println("update query: " + query);
			// String query = "UPDATE account SET status = ? WHERE proxy_ip=? BETWEEN
			// SUBDATE(NOW(),1) AND NOW() AND status=\"LOCKED\"";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setInt(1, error ? 1 : 0);
			preparedStmt.setString(2, proxyIp);
			preparedStmt.setString(3, proxyPort);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();

			System.out.println(preparedStmt.toString());
			// System.out.println("Updated account status in database with ip! " +
			// status.name() + " ip: " + ip);
			preparedStmt.close();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean updateStatusOfAccountById(String database, AccountStatus status, int id) {
		try {
			String query = "UPDATE " + database + ".account SET status = ? WHERE id=?";
			// String query = "UPDATE account SET status = ? WHERE proxy_ip=? BETWEEN
			// SUBDATE(NOW(),1) AND NOW() AND status=\"LOCKED\"";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, status.name());
			preparedStmt.setInt(2, id);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();

			System.out.println(preparedStmt.toString());
			// System.out.println("Updated account status in database with ip! " +
			// status.name() + " ip: " + ip);
			preparedStmt.close();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean updateStatusOfAccountById(AccountStatus status, int id) {
		try {
			String query = "UPDATE account SET status = ? WHERE id=?";
			// String query = "UPDATE account SET status = ? WHERE proxy_ip=? BETWEEN
			// SUBDATE(NOW(),1) AND NOW() AND status=\"LOCKED\"";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, status.name());
			preparedStmt.setInt(2, id);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();

			System.out.println(preparedStmt.toString());
			// System.out.println("Updated account status in database with ip! " +
			// status.name() + " ip: " + ip);
			preparedStmt.close();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean updateStatusOfAccountByIpWithoutLockedTimeout(AccountStatus status, String ip) {
		try {
			String query = "UPDATE account SET status = ? WHERE proxy_ip=? AND status='LOCKED' AND updated_at BETWEEN SUBDATE(NOW(),2) AND NOW()";
			// String query = "UPDATE account SET status = ? WHERE proxy_ip=? BETWEEN
			// SUBDATE(NOW(),1) AND NOW() AND status=\"LOCKED\"";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, status.name());
			preparedStmt.setString(2, ip);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();

			System.out.println(preparedStmt.toString());
			System.out.println("Updated account status in database with ip! " + status.name() + " ip: " + ip);
			preparedStmt.close();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void changeErrorIpBackToNormal() {
		String sql = "SELECT * FROM proxies WHERE error_ip=1";

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {
				while (resultSet.next()) {
					String updatedAt = resultSet.getString("updated_at");
					String ip = resultSet.getString("ip_addres");

					Calendar calendar = Calendar.getInstance();

					java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date date2 = sdf.parse(updatedAt);
					calendar.setTime(date2);
					calendar.add(Calendar.MINUTE, 80);

					Calendar calendar2 = Calendar.getInstance();
					calendar2.setTime(new Date());

					if (calendar2.after(calendar)) {
						System.out.println("Updated ALL ERROR-IP's back to LOCKED for ip: " + ip
								+ ", due to 60 minutes have past");
						updateProxyStatusByIp(0, ip);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void changeTimeoutLockedToNormal() {
		String sql = "SELECT proxy_ip,email,updated_at FROM account WHERE status=\"LOCKED_TIMEOUT\"";

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {
				while (resultSet.next()) {
					String updatedAt = resultSet.getString("updated_at");
					String ip = resultSet.getString("proxy_ip");
					String email = resultSet.getString("email");

					Calendar calendar = Calendar.getInstance();

					java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date date2 = sdf.parse(updatedAt);
					calendar.setTime(date2);
					calendar.add(Calendar.MINUTE, 60);

					// try {

					Calendar calendar2 = Calendar.getInstance();
					calendar2.setTime(new Date());

					if (calendar2.after(calendar)) {
						System.out.println("Updated ALL LOCKED_TIMEOUT back to LOCKED for ip: " + ip
								+ ", due to 45 minutes have past");
						updateStatusOfAccountByIp(AccountStatus.LOCKED, ip);
					}

					// } catch (ParseException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }

					// return max + 1;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// return -1;

	}

	public static boolean updateAccountStage(String database, AccountStage stage, int accountId) {
		try {
			String query = "UPDATE " + database + ".account SET account_stage = ? WHERE id=?";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, stage.name());
			preparedStmt.setInt(2, accountId);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();

			System.out.println("Updated account in database with new stage!");

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean updateAccountStage(AccountStage stage, int accountId) {
		try {
			String query = "UPDATE account SET account_stage = ? WHERE id=?";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, stage.name());
			preparedStmt.setInt(2, accountId);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();

			System.out.println("Updated account in database with new stage!");

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static AccountStatus getAccountStatusInDatabase(String database, int accountId) {
		StringBuilder db = new StringBuilder();
		if (database != null) {
			db.append(database);
			db.append(".");
		}
		String sql = "SELECT status FROM " + db.toString() + "account WHERE id = " + accountId + "";
		// System.out.println(sql);

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);
			try {
				while (resultSet.next()) {
					AccountStatus stage = AccountStatus.valueOf(resultSet.getString("account_stage"));

					return stage;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getAccountStageInDatabase(String accountName) {
		String sql = "SELECT account_stage FROM account WHERE name = " + accountName + "";

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);
			try {
				while (resultSet.next()) {
					String stage = resultSet.getString("account_stage");

					return stage;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getAccountStageInDatabase(int accountId) {
		String sql = "SELECT account_stage FROM account WHERE id = " + accountId + "";

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);
			try {
				while (resultSet.next()) {
					String stage = resultSet.getString("account_stage");

					return stage;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int getAmountOfMuleTradesInAllDatabases(String name) {
		int count = 0;

		for (String database : Config.DATABASE_NAMES) {
			String sql = "SELECT COUNT(*) as cnt FROM `" + database + "`.account WHERE trade_with_other = '" + name
					+ "'";
			// System.out.println(sql);
			try {
				PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
						.prepareStatement(sql);

				ResultSet resultSet = preparedStatement.executeQuery(sql);
				try {
					// System.out.println(sql);
					while (resultSet.next()) {
						int cnt = resultSet.getInt("cnt");

						count += cnt;
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					resultSet.close();
					preparedStatement.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("found amount: " + count);
		return count;
	}

	public static int getAmountOfMuleTrades(String database, String name) {
		String sql = "SELECT COUNT(*) as cnt FROM " + database + ".account WHERE trade_with_other = '" + name + "'";

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);

			ResultSet resultSet = preparedStatement.executeQuery(sql);
			try {
				// System.out.println(sql);
				while (resultSet.next()) {
					int cnt = resultSet.getInt("cnt");

					return cnt;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static int getAmountOfMuleTrades(String name) {
		String sql = "SELECT COUNT(*) as cnt FROM account WHERE trade_with_other = '" + name + "'";

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);

			ResultSet resultSet = preparedStatement.executeQuery(sql);
			try {
				// System.out.println(sql);
				while (resultSet.next()) {
					int cnt = resultSet.getInt("cnt");

					return cnt;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static String getTradeWithOther(String database, int accountId) {
		String sql = "SELECT trade_with_other FROM " + database + ".account WHERE id = " + accountId + "";

		// System.out.println(sql);

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {
				while (resultSet.next()) {
					String stage = resultSet.getString("trade_with_other");

					return stage;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getTradeWithOther(int accountId) {
		String sql = "SELECT trade_with_other FROM account WHERE id = " + accountId + "";

		// System.out.println(sql);

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {
				while (resultSet.next()) {
					String stage = resultSet.getString("trade_with_other");

					return stage;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getServerMuleUsedByDatabase() {
		String sql = "SELECT used_by_database FROM server_muling.config WHERE id=0";

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);

			try {
				while (resultSet.next()) {
					String stage = resultSet.getString("used_by_database");

					return stage;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean setBannedAndTradingWithToNull() {
		try {
			String query = "UPDATE account SET trade_with_other = null WHERE status = \"BANNED\" AND trade_with_other IS NOT NULL";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean setTradingWith(String database, String tradeWith, int accountId) {
		try {
			String query = "UPDATE " + database + ".account SET trade_with_other = ? WHERE id=?";
			// System.out.println(query);
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, tradeWith);
			preparedStmt.setInt(2, accountId);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();

			System.out.println("Updated account in database trading with: " + tradeWith + " Z02");

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean setTradingWith(String tradeWith, int accountId) {
		try {
			String query = "UPDATE account SET trade_with_other = ? WHERE id=?";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, tradeWith);
			preparedStmt.setInt(2, accountId);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();

			System.out.println("Updated account in database trading with: " + tradeWith + " Z03");

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean setTradingWithByUsername(String database, String tradeWith, String username) {
		try {
			String query = "UPDATE " + database + ".account SET trade_with_other = ? WHERE name=?";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, tradeWith);
			preparedStmt.setString(2, username);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();

			System.out.println("Updated account in database trading with: " + tradeWith + " Z04");

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean setTradingWith(String database, String tradeWith, String username) {
		try {
			String query = "UPDATE " + database + ".account SET trade_with_other = ? WHERE trade_with_other=?";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, tradeWith);
			preparedStmt.setString(2, username);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();

			System.out.println("Updated account in database trading with: " + tradeWith + " Z05");

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean setServerMuleConnectedDatabase(String database) {
		try {
			String query = "UPDATE server_muling.config SET used_by_database = ? WHERE id=0";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, database);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void insertLoggingMessage(String type, String message) {
		// the mysql insert statement
		String query = "INSERT INTO logging.`log` (type, message, server) values (?,?,?)";

		// create the mysql insert preparedstatement
		try {
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);

			preparedStmt.setString(1, type);
			preparedStmt.setString(2, message);
			preparedStmt.setString(3, Config.DATABASE_NAME);

			// execute the preparedstatement
			preparedStmt.execute();
			preparedStmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// api.log(exceptionToString(e));
			e.printStackTrace();
		}
	}

	public static boolean setTradingWith(String tradeWith, String username) {
		try {
			String query = "UPDATE account SET trade_with_other = ? WHERE trade_with_other=?";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, tradeWith);
			preparedStmt.setString(2, username);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();

			System.out.println("Updated account in database trading with: " + tradeWith + " Z01");

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @return
	 */
	public static ArrayList<AccountTable> getAccountsFromMysqlConnection() {
		ArrayList<AccountTable> accounts = new ArrayList<AccountTable>();

		String oldSql = "SELECT * FROM (SELECT ac.*, p.is_alive, p.username as p_us, p.password as p_pass FROM account AS ac INNER JOIN proxies AS p ON p.ip_addres=ac.proxy_ip WHERE (ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND ac.status <> \"OUT_OF_MONEY\" AND email IS NOT NULL AND ac.status <> \"LOCKED_INGAME\" AND ac.status <> \"BANNED\"  AND ac.status <> \"TIMEOUT\" AND ac.status <> \"TASK_TIMEOUT\"  AND ac.status <> \"INVALID_PASSWORD\") OR (ac.status=\"TASK_TIMEOUT\" AND amount_timeout < "
				+ Config.AMOUNT_OF_TIMEOUTS_BEFORE_GONE + ") OR (ac.status=\"TIMEOUT\" AND amount_timeout < "
				+ Config.AMOUNT_OF_TIMEOUTS_BEFORE_GONE + ")) as z GROUP BY z.id";

		String sql = "SELECT * FROM (\r\n"
				+ "SELECT * FROM (SELECT ac.*, p.is_alive as alive, p.username as p_us, p.password as p_pass FROM account AS ac INNER JOIN proxies AS p ON p.ip_addres=ac.proxy_ip WHERE (ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND ac.status <> \"OUT_OF_MONEY\" AND email IS NOT NULL AND ac.status <> \"LOCKED_INGAME\" AND ac.status <> \"BANNED\"  AND ac.status <> \"TIMEOUT\" AND ac.status <> \"TASK_TIMEOUT\"  AND ac.status <> \"INVALID_PASSWORD\") OR (ac.status=\"TASK_TIMEOUT\" AND amount_timeout < "
				+ Config.AMOUNT_OF_TIMEOUTS_BEFORE_GONE + ") OR (ac.status=\"TIMEOUT\" AND amount_timeout < "
				+ Config.AMOUNT_OF_TIMEOUTS_BEFORE_GONE + ")) as z GROUP BY z.id\r\n" + ") as z\r\n" + "\r\n"
				+ "UNION\r\n" + "\r\n" + "SELECT * FROM (\r\n"
				+ "SELECT * FROM (SELECT ac.*, p.is_alive as alive, p.username as p_us, p.password as p_pass FROM server_muling.account AS ac INNER JOIN server_muling.proxies AS p ON p.ip_addres=ac.proxy_ip WHERE (ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND ac.status <> \"OUT_OF_MONEY\" AND email IS NOT NULL AND ac.status <> \"LOCKED_INGAME\" AND ac.status <> \"BANNED\"  AND ac.status <> \"TIMEOUT\" AND ac.status <> \"TASK_TIMEOUT\"  AND ac.status <> \"INVALID_PASSWORD\") OR (ac.status=\"TASK_TIMEOUT\" AND amount_timeout < "
				+ Config.AMOUNT_OF_TIMEOUTS_BEFORE_GONE + ") OR (ac.status=\"TIMEOUT\" AND amount_timeout < "
				+ Config.AMOUNT_OF_TIMEOUTS_BEFORE_GONE + ")) as z GROUP BY z.id\r\n" + ") as p";

		// System.out.println(sql);

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);
			try {

				while (resultSet.next()) {
					int id = resultSet.getInt("id");
					String email = resultSet.getString("email");
					String name = resultSet.getString("name");
					String password = resultSet.getString("password");
					int world = resultSet.getInt("world_number");
					int qp = resultSet.getInt("quest_points");
					String proxyIp = resultSet.getString("proxy_ip");
					String proxyPort = resultSet.getString("proxy_port");
					String scriptName = resultSet.getString("account_stage");
					String tradingWith = resultSet.getString("trade_with_other");
					String proxyUsername = resultSet.getString("p_us");
					String proxyPassword = resultSet.getString("p_pass");
					boolean lowCpuMode = resultSet.getBoolean("low_cpu_mode");
					int isAlive = resultSet.getInt("alive");
					String accountValue = resultSet.getString("account_value");
					AccountStatus status = AccountStatus.valueOf(resultSet.getString("status"));
					String date = resultSet.getString("break_till");
					int amountTimeout = resultSet.getInt("amount_timeout");
					LoginStatus loginStatus = LoginStatus.valueOf(resultSet.getString("login_status"));
					String countryCode = resultSet.getString("country_code_proxy_usage");

					Calendar calendar = Calendar.getInstance();
					// calendar.add(Calendar.MINUTE, 30);
					java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					try {
						Date date2 = sdf.parse(date);
						calendar.setTime(date2);

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					AccountStage stage = null;
					if (resultSet.getString("account_stage") != null) {
						stage = AccountStage.valueOf(resultSet.getString("account_stage"));
					}
					int account_stage_progress = resultSet.getInt("account_stage_progress");

					AccountTable account = new AccountTable(id, null, name, world, proxyIp, proxyPort, lowCpuMode,
							status, stage, account_stage_progress);
					account.setLoginStatus(loginStatus);
					account.setProxyUsername(proxyUsername);
					account.setAmountTimeout(amountTimeout);
					account.setProxyPassword(proxyPassword);
					account.setPassword(password);
					account.setScript(scriptName);
					account.setEmail(email);
					account.setQuestPoints(qp);
					account.setAccountValue(accountValue);
					account.setDate(calendar);
					account.setDateString(date);
					account.setTradeWithOther(tradingWith);
					account.setCountryProxyCode(countryCode);

					boolean alive = isAlive == 1 ? true : false;
					account.setProxyOnline(alive);

					accounts.add(account);

				}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return accounts;
	}

	public static String formatNumbers(String input) {
		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(input);
		NumberFormat nf = NumberFormat.getInstance();
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String g = m.group();
			m.appendReplacement(sb, nf.format(Double.parseDouble(g)));
		}
		return m.appendTail(sb).toString();
	}

	/**
	 * 
	 * @param arrayList
	 * @param arrayList2
	 * @return
	 */
	public static HashMap<DatabaseProxy, Integer> oneExistsInOther(ArrayList<DatabaseProxy> arrayList,
			ArrayList<DatabaseProxy> arrayList2) {
		HashMap<DatabaseProxy, Integer> hash = new HashMap<DatabaseProxy, Integer>();

		for (DatabaseProxy p : arrayList) {
			int count = 0;

			for (DatabaseProxy p2 : arrayList2) {
				if (p.getProxyIp().equalsIgnoreCase(p2.getProxyIp())
						&& p.getProxyPort().equalsIgnoreCase(p2.getProxyPort())) {

					count++;

				}
			}
			hash.put(p, count);
			// System.out.println(p+" "+count+" added to array");
		}
		return hash;
	}

	public static synchronized ArrayList<OsbotController> getAccountsToBeRecovered() {
		ArrayList<OsbotController> bots = new ArrayList<OsbotController>();
		try {
			String sql = "SELECT * FROM account WHERE status=\"LOCKED\" AND updated_at BETWEEN SUBDATE(NOW(),1) AND NOW()";
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);
			try {

				while (resultSet.next()) {
					int id = resultSet.getInt("id");
					// System.out.println("Account id: " + id + " has to get recovered");

					OsbotController bot = BotController.getBotById(id);
					if (bot != null) {
						bots.add(bot);
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				preparedStatement.close();
				resultSet.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return bots;
	}

	public static void main(String[] args) {

		// AccountCreationService.addUsernameToUsernames("abc");
		//
		// AccountCreationService.getUsedUsernames().forEach(a -> {
		// System.out.println("test:" + a.getUsername() + " " + a.getTime());
		// });

		// DatabaseUtilities.changeTimeoutLockedToNormal();

		// seleniumRecoverAccount();
		// seleniumCreateAccountThread();
	}

	private static List<PidCheck> FIREFOX_PIDS = new ArrayList<PidCheck>();

	private static boolean aPid(int pid) {
		for (PidCheck localPid : FIREFOX_PIDS) {
			if (localPid.getPid() == pid) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsInRealTimePid(int pid) {
		for (Integer localPid : GeckoHandler.getGeckodriverExeWindows()) {
			if (localPid == pid) {
				return true;
			}
		}
		return false;
	}

	private static List<PidCheck> GECKO_PIDS = new ArrayList<PidCheck>();

	private static boolean containsInPid2(int pid) {
		for (PidCheck localPid : GECKO_PIDS) {
			if (localPid.getPid() == pid) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsInRealTimePid2(int pid) {
		for (Integer localPid : GeckoHandler.getFirefoxExeWindows()) {
			if (localPid == pid) {
				return true;
			}
		}
		return false;
	}

	public static void checkPidsProcessesEveryMinutes2() {
		// if (GeckoHandler.getFirefoxExeWindows().size() > 0 &&
		// GeckoHandler.getGeckodriverExeWindows().size() <= 0) {
		// WebdriverFunctions.killAll();
		// }
		//
		// for (Integer pid : GeckoHandler.getFirefoxExeWindows()) {
		// if (!containsInPid2(pid)) {
		// PidCheck c = new PidCheck(pid);
		// GECKO_PIDS.add(c);
		// System.out.println("Added new firefox/2 pid: " + c.getPid());
		// }
		// }
		//
		// Iterator<PidCheck> i = GECKO_PIDS.iterator();
		//
		// while (i.hasNext()) {
		// PidCheck pid = i.next();
		//
		// if (!containsInRealTimePid2(pid.getPid())) {
		// BotController.killProcess(pid.getPid());
		// i.remove();
		// // System.out.println("Pid /2 " + pid + " was removed, was already open for 5
		// // minutes");
		// continue;
		// }
		//
		// if (pid.getMatches() > 1200) {
		// BotController.killProcess(pid.getPid());
		// i.remove();
		// System.out.println("Pid /2 " + pid + " was killed, was already open for 5
		// minutes");
		// continue;
		// } else {
		// // System.out.println("Firefox /2 pid: " + pid.getPid() + " closing in " +
		// // pid.getMatches() + "/300");
		// }
		// pid.setMatches(pid.getMatches() + 1);
		// }
		//
		// for (Integer pid : GeckoHandler.getGeckodriverExeWindows()) {
		// if (!containsInPid(pid)) {
		// FIREFOX_PIDS.add(new PidCheck(pid));
		// System.out.println("Added new firefox pid: " + pid);
		// }
		// }
		//
		// Iterator<PidCheck> b = FIREFOX_PIDS.iterator();
		//
		// while (b.hasNext()) {
		// PidCheck pid = b.next();
		//
		// if (!containsInRealTimePid(pid.getPid())) {
		// BotController.killProcess(pid.getPid());
		// b.remove();
		// // System.out.println("Pid " + pid + " was removed, was already open for 5
		// // minutes");
		// continue;
		// }
		//
		// if (pid.getMatches() > 1200) {
		// BotController.killProcess(pid.getPid());
		// b.remove();
		// System.out.println("Pid " + pid + " was killed, was already open for 5
		// minutes");
		// continue;
		// } else {
		// // System.out.println("Firefox pid: " + pid.getPid() + " closing in " +
		// // pid.getMatches() + "/300");
		// }
		// pid.setMatches(pid.getMatches() + 1);
		// }
		//
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	private static boolean containsInPid(int pid) {
		for (PidCheck localPid : FIREFOX_PIDS) {
			if (localPid.getPid() == pid) {
				return true;
			}
		}
		return false;
	}

	// public static void checkPidsProcessesEveryMinutes() {
	// new Thread(() -> {
	//
	// while (true) {
	//
	// List<PidCheck> pids = new ArrayList<PidCheck>();
	// // boolean launch = AccountCreationService.getLaunching();
	//
	// try {
	// Thread.sleep(200_000);
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// // boolean launch2 = AccountCreationService.getLaunching();
	// List<Integer> pidsAfter5Minutes = GeckoHandler.getGeckodriverExeWindows();
	//
	// // if (launch && launch2 && pids.size() == pidsAfter5Minutes.size()) {
	// // AccountCreationService.setLaunching(false);
	// // System.out.println("Set launching to false because was lauching too
	// long");
	// // }
	//
	// pids.removeAll(pidsAfter5Minutes);
	//
	// for (Integer pid : pids) {
	// BotController.killProcess(pid);
	// System.out.println("Pid " + pid + " was removed, was already open for 5
	// minutes");
	// }
	//
	// }
	//
	// }).start();
	// }

	public static int getAvailableAccounts() {
		String sql = "SELECT COUNT(*) as available_accounts FROM account AS ac INNER JOIN proxies AS p ON p.ip_addres=ac.proxy_ip WHERE ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND email IS NOT NULL  AND ac.status <> \"OUT_OF_MONEY\" AND ac.status <> \"LOCKED_INGAME\" AND ac.status <> \"BANNED\" AND ac.status <> \"LOCKED_TIMEOUT\" AND ac.status <> \"INVALID_PASSWORD\"";

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);
			try {
				int availableAccounts = 0;

				while (resultSet.next()) {
					availableAccounts = resultSet.getInt("available_accounts");
					return availableAccounts;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static int getServerMuleAmount() {
		String sql = "SELECT COUNT(*) as mule_count FROM server_muling.account WHERE account_stage = \"UNKNOWN\" AND status = \"SERVER_MULE\"";

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);
			try {
				int muleCount = -1;

				while (resultSet.next()) {
					muleCount = resultSet.getInt("mule_count");
					return muleCount;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 10;
	}

	public static int getMuleAmount() {
		String sql = "SELECT COUNT(*) as mule_count FROM account WHERE account_stage = \"UNKNOWN\" AND status = \"MULE\"";

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);
			try {
				int muleCount = 0;

				while (resultSet.next()) {
					muleCount = resultSet.getInt("mule_count");
					return muleCount;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 10;
	}

	public static int getMuleAccountsInTheMaking() {
		StringBuilder totalProxies = new StringBuilder();
		for (String randomProxy : Config.getAllMuleProxiesWithoutSuperMule()) {
			String[] randomProxySplit = randomProxy.split(":");
			String proxy = randomProxySplit[0];
			String port = randomProxySplit[1];

			if (totalProxies.length() == 0) {
				totalProxies.append("(proxy_ip= '" + proxy + "' AND proxy_port = '" + port + "')");
			} else {
				totalProxies.append(" OR (proxy_ip= '" + proxy + "' AND proxy_port = '" + port + "')");
			}
		}

		String sql = "SELECT COUNT(*) as mule_count FROM account WHERE (" + totalProxies.toString()
				+ ") AND (status = \"AVAILABLE\" OR status=\"LOCKED\" OR status=\"LOCKED_TIMEOUT\")";
		// System.out.println(sql);

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);
			try {
				int muleCount = -1;

				while (resultSet.next()) {
					muleCount = resultSet.getInt("mule_count");
					return muleCount;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 10;
	}

	public static int getServerMulesAccountsInTheMaking() {
		StringBuilder totalProxies = new StringBuilder();
		for (String randomProxy : Config.SERVER_MULES) {
			String[] randomProxySplit = randomProxy.split(":");
			String proxy = randomProxySplit[0];
			String port = randomProxySplit[1];

			if (totalProxies.length() == 0) {
				totalProxies.append("(proxy_ip= '" + proxy + "' AND proxy_port = '" + port + "')");
			} else {
				totalProxies.append(" OR (proxy_ip= '" + proxy + "' AND proxy_port = '" + port + "')");
			}
		}

		String sql = "SELECT COUNT(*) as mule_count FROM server_muling.account WHERE (" + totalProxies.toString()
				+ ") AND (status = \"AVAILABLE\" OR status=\"LOCKED\" OR status=\"LOCKED_TIMEOUT\")";
		// System.out.println(sql);

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);
			try {
				int muleCount = -1;

				while (resultSet.next()) {
					muleCount = resultSet.getInt("mule_count");
					return muleCount;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 10;
	}

	public static int getSuperAccountsInTheMaking() {
		StringBuilder totalProxies = new StringBuilder();
		for (String randomProxy : Config.SUPER_MULE_PROXY_IP) {
			String[] randomProxySplit = randomProxy.split(":");
			String proxy = randomProxySplit[0];
			String port = randomProxySplit[1];

			if (totalProxies.length() == 0) {
				totalProxies.append("(proxy_ip= '" + proxy + "' AND proxy_port = '" + port + "')");
			} else {
				totalProxies.append(" OR (proxy_ip= '" + proxy + "' AND proxy_port = '" + port + "')");
			}
		}

		String sql = "SELECT COUNT(*) as mule_count FROM account WHERE (" + totalProxies.toString()
				+ ") AND (status = \"AVAILABLE\" OR status=\"LOCKED\" OR status=\"LOCKED_TIMEOUT\")";
		// System.out.println(sql);

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);
			try {
				int muleCount = -1;

				while (resultSet.next()) {
					muleCount = resultSet.getInt("mule_count");
					return muleCount;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static int getSuperMuleAmount() {
		String sql = "SELECT COUNT(*) as mule_count FROM account WHERE account_stage = \"UNKNOWN\" AND status = \"SUPER_MULE\"";

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);
			try {
				int muleCount = -1;

				while (resultSet.next()) {
					muleCount = resultSet.getInt("mule_count");
				}
				return muleCount;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				resultSet.close();
				preparedStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Recovers an account
	 */
	public static void seleniumRecoverAccount() {

		// synchronized (getAccountsToBeRecovered()) {
		// AccountCreationService.checkPreviousProcessesAndDie(SeleniumType.RECOVER_ACCOUNT);

		// if (!AccountCreationService.getLaunching()) {
		// AccountCreationService.checkProcesses();
		// }

		// if (AccountCreationService.getLaunching()) {
		// return;
		// }

		if (!GeckoHandler.mayStartFirefoxBrowser(5)) {
			return;
		}

		System.out.println(
				"[ACCOUNT RECOVERING] " + getAccountsToBeRecovered().size() + " accounts left to recover currently");

		System.out.println(
				"[ACCOUNT RECOVERING] " + AccountCreationService.getUsedUsernames().size() + " accounts timed out");
		ArrayList<OsbotController> accs = new ArrayList<OsbotController>();

		synchronized (getAccountsToBeRecovered()) {
			accs = getAccountsToBeRecovered();
			Collections.shuffle(accs);
		}

		for (OsbotController account : accs) {
			if (!account.getAccount().isProxyOnline()) {
				System.out.println("Skipping, because proxy is offline");
				continue;
			}
			if (!GeckoHandler.mayStartFirefoxBrowser(5)) {
				break;
			}
			// if (Config.ERROR_IP &&
			// DatabaseUtilities.isErrorProxy(account.getAccount().getProxyIp(),
			// account.getAccount().getProxyPort())) {
			// System.out.println("Skipping this IP, because it's currently giving an
			// error");
			// continue;
			// }

			if (!AccountCreationService.containsUsername(account.getAccount().getUsername())) {
				if (DatabaseUtilities.checkAlreadyLockedProxies(account.getAccount().getProxyIp(),
						account.getAccount().getEmail())) {
					System.out.println("Didn't recover, because IP is flagged for some time");
					continue;
				}
				System.out.println("Recovering account: " + account.getAccount().getUsername());
				AccountCreationService.addUsernameToUsernames(account.getAccount().getUsername());

				DatabaseProxy proxy = new DatabaseProxy(account.getAccount().getProxyIp(),
						account.getAccount().getProxyPort(), account.getAccount().getProxyUsername(),
						account.getAccount().getProxyPassword());

				AccountCreationService.launchRunescapeWebsite(proxy, account, SeleniumType.RECOVER_ACCOUNT, false);
				break;
				// System.out.println("Recovering account: " +
				// account.getAccount().getUsername());
			}
		}
		// }

	}

	// public static int getSizeToCreateAccounts() {
	// HashMap<DatabaseProxy, Integer> hash = oneExistsInOther(getTotalProxies(),
	// getUsedProxies());
	//
	// int count = 0;
	// for (Entry<DatabaseProxy, Integer> entry : hash.entrySet()) {
	// DatabaseProxy key = entry.getKey();
	// Integer value = entry.getValue();
	//
	// if (value < 2) {
	// count += value;
	// }
	// }
	// return count;
	// }

	private static long lastAttempt = 0;

	public static void transformIntoMuleAccount() {

		/**
		 * Making mules with that specific IP-adress
		 */

		if (DatabaseUtilities.getServerMulesAccountsInTheMaking() >= 0) {

			for (OsbotController mule : BotController.getBots()) {
				if (mule.getAccount().getStage() != AccountStage.TUT_ISLAND
						&& mule.getAccount().getStatus() == AccountStatus.AVAILABLE
						&& Config.isServerMuleProxy(mule.getAccount().getProxyIp(), mule.getAccount().getProxyPort())) {

					System.out.println("Server mule completed tutorial island, setting to official mule now!");
					mule.getAccount().setStage(AccountStage.UNKNOWN);
					mule.getAccount().setStatus(AccountStatus.SERVER_MULE);

					updateAccountStage("server_muling", mule.getAccount().getStage(), mule.getId());
					updateStatusOfAccountById("server_muling", mule.getAccount().getStatus(), mule.getId());

					if (mule.getPidId() > 0) {
						BotController.killProcess(mule.getPidId());
					}
				}
			}

		}

		if (DatabaseUtilities.getMuleAccountsInTheMaking() >= 0) {
			// Make an account once every 20 minutes
			for (OsbotController mule : BotController.getBots()) {
				if (mule.getAccount().getStage() != AccountStage.TUT_ISLAND
						&& mule.getAccount().getStatus() == AccountStatus.AVAILABLE
						&& Config.isStaticMuleProxy(mule.getAccount().getProxyIp(), mule.getAccount().getProxyPort())) {

					System.out.println("Mule completed tutorial island, setting to official mule now!");
					mule.getAccount().setStage(AccountStage.UNKNOWN);
					mule.getAccount().setStatus(AccountStatus.MULE);

					updateAccountStage(mule.getAccount().getStage(), mule.getId());
					updateStatusOfAccountById(mule.getAccount().getStatus(), mule.getId());

					if (mule.getPidId() > 0) {
						BotController.killProcess(mule.getPidId());
					}
				}
			}
		}

		if (DatabaseUtilities.getSuperAccountsInTheMaking() >= 0) {
			// Make an account once every 20 minutes
			for (OsbotController mule : BotController.getBots()) {
				if (mule.getAccount().getStage() != AccountStage.TUT_ISLAND
						&& mule.getAccount().getStatus() == AccountStatus.AVAILABLE
						&& Config.isSuperMuleProxy(mule.getAccount().getProxyIp(), mule.getAccount().getProxyPort())) {

					System.out.println("Mule completed tutorial island, setting to official super mule now!");
					mule.getAccount().setStage(AccountStage.UNKNOWN);
					mule.getAccount().setStatus(AccountStatus.SUPER_MULE);

					updateAccountStage(mule.getAccount().getStage(), mule.getId());
					updateStatusOfAccountById(mule.getAccount().getStatus(), mule.getId());

					if (mule.getPidId() > 0) {
						BotController.killProcess(mule.getPidId());
					}
				}
			}
		}
	}

	public static void transformIntoMuleHandler() {
		System.out.println("[MULE CREATION] Current amount of mules: " + DatabaseUtilities.getMuleAmount() + " time: "
				+ (System.currentTimeMillis() - lastAttempt));

		if ((DatabaseUtilities.getSuperMuleAmount()) == 0 && (DatabaseUtilities.getSuperAccountsInTheMaking() == 0)
				&& ((System.currentTimeMillis() - lastAttempt) > 1_200_000)) {
			lastAttempt = System.currentTimeMillis();

			RandomNameGenerator name = new RandomNameGenerator();

			String[] proxyString = Config.getRandomSuperMuleProxy().split(":");

			if (Config.ERROR_IP && DatabaseUtilities.isErrorProxy(proxyString[0], proxyString[1])) {
				System.out.println("Skipping this IP, because it's currently giving an error");
				return;
			}

			AccountTable table = new AccountTable(-1, "test", name.generateRandomNameString(), 394, proxyString[0],
					proxyString[1], true, AccountStatus.AVAILABLE, AccountStage.TUT_ISLAND, 0);

			table.setPassword(name.generateRandomNameString());
			table.setProxyUsername(proxyString[2]);// hjeg53
			table.setProxyPassword(proxyString[3]);// L9MbdJ
			table.setBankPin("0000");

			DatabaseProxy proxy = new DatabaseProxy(table.getProxyUsername(), table.getProxyPort(),
					table.getProxyUsername(), table.getProxyPassword());

			OsbotController bot = new OsbotController(-1, table);
			System.out.println(
					"Creating account: " + table.getUsername() + " stage: " + " status: " + AccountStatus.SUPER_MULE);

			AccountCreationService.launchRunescapeWebsite(proxy, bot, SeleniumType.CREATE_VERIFY_ACCOUNT, false);

		}

		if ((DatabaseUtilities.getServerMuleAmount() == 0)
				&& (DatabaseUtilities.getServerMulesAccountsInTheMaking() == 0)
				&& ((System.currentTimeMillis() - lastAttempt) > 800_000)) {
			lastAttempt = System.currentTimeMillis();

			RandomNameGenerator name = new RandomNameGenerator();

			String[] proxyString = Config.SERVER_MULES.get(0).split(":");

			if (Config.ERROR_IP && DatabaseUtilities.isErrorProxy(proxyString[0], proxyString[1])) {
				System.out.println("Skipping this IP, because it's currently giving an error");
				return;
			}

			AccountTable table = new AccountTable(-1, "test", name.generateRandomNameString(), 394, proxyString[0],
					proxyString[1], true, AccountStatus.AVAILABLE, AccountStage.TUT_ISLAND, 0);

			table.setPassword(name.generateRandomNameString());
			table.setProxyUsername(proxyString[2]);// hjeg53
			table.setProxyPassword(proxyString[3]);// L9MbdJ
			table.setBankPin("0000");

			DatabaseProxy proxy = new DatabaseProxy(table.getProxyUsername(), table.getProxyPort(),
					table.getProxyUsername(), table.getProxyPassword());

			OsbotController bot = new OsbotController(-1, table);
			System.out.println("Creating account: " + table.getUsername() + " status: " + AccountStatus.SERVER_MULE);

			AccountCreationService.launchRunescapeWebsite(proxy, bot, SeleniumType.CREATE_VERIFY_ACCOUNT, true);

		}

		if ((DatabaseUtilities.getMuleAmount() <= 4) && (DatabaseUtilities.getMuleAccountsInTheMaking() <= 2)
				&& ((System.currentTimeMillis() - lastAttempt) > 600_000)) {
			lastAttempt = System.currentTimeMillis();

			RandomNameGenerator name = new RandomNameGenerator();

			String[] proxyString = Config.getRandomMuleProxyWithoutSuperMule().split(":");

			if (Config.ERROR_IP && DatabaseUtilities.isErrorProxy(proxyString[0], proxyString[1])) {
				System.out.println("Skipping this IP, because it's currently giving an error");
				return;
			}

			AccountTable table = new AccountTable(-1, "test", name.generateRandomNameString(), 394, proxyString[0],
					proxyString[1], true, AccountStatus.AVAILABLE, AccountStage.TUT_ISLAND, 0);

			table.setPassword(name.generateRandomNameString());
			table.setProxyUsername(proxyString[2]);// hjeg53
			table.setProxyPassword(proxyString[3]);// L9MbdJ
			table.setBankPin("0000");

			DatabaseProxy proxy = new DatabaseProxy(table.getProxyUsername(), table.getProxyPort(),
					table.getProxyUsername(), table.getProxyPassword());

			OsbotController bot = new OsbotController(-1, table);
			System.out.println("Creating account: " + table.getUsername() + " status: " + AccountStatus.MULE);

			AccountCreationService.launchRunescapeWebsite(proxy, bot, SeleniumType.CREATE_VERIFY_ACCOUNT, false);

		}

	}

	public static List<AccountCreate> USED_IPS_TO_CREATE_WITH = new CopyOnWriteArrayList<AccountCreate>();

	public static boolean ipExists(String ip) {
		boolean exists = false;
		for (AccountCreate create : USED_IPS_TO_CREATE_WITH) {
			if (create.getUsername().equalsIgnoreCase(ip)) {
				exists = true;
			}
		}
		return exists;
	}

	public static void addToUsedIpsCreateAccount(String ip) {
		boolean exists = false;
		for (AccountCreate create : USED_IPS_TO_CREATE_WITH) {
			if (create.getUsername().equalsIgnoreCase(ip)) {
				exists = true;
			}
		}
		if (exists) {
			System.out.println("SKIPPING BECAUSE IP TO CREATE ALREADY EXISTS FOR NOW");
			return;
		}

		USED_IPS_TO_CREATE_WITH.add(new AccountCreate(System.currentTimeMillis(), ip));

		System.out.println("Added to CREATE LIST: " + ip);
	}

	public static void checkUsedIPs() {
		ArrayList<AccountCreate> toRemove = new ArrayList<AccountCreate>();

		for (AccountCreate create : USED_IPS_TO_CREATE_WITH) {

			if (System.currentTimeMillis() - create.getTime() > 350_000) {
				toRemove.add(create);
			}
		}

		for (AccountCreate create : toRemove) {
			USED_IPS_TO_CREATE_WITH.remove(create);
			System.out.println("Removed: " + create + " from the create list, because was longer than 350_000 secs");
		}
	}

	/**
	 * 
	 */
	public static void seleniumCreateAccountThread() {

		// // if (!AccountCreationService.getLaunching()) {
		// // AccountCreationService.checkProcesses();
		// // }
		//
		// // if (AccountCreationService.getLaunching()) {
		// // return;
		// // }
		//
		// //
		// AccountCreationService.checkPreviousProcessesAndDie(SeleniumType.CREATE_VERIFY_ACCOUNT);

		// HashMap<DatabaseProxy, Integer> hash = oneExistsInOther(getTotalProxies(),
		// getUsedProxies());

		// int count = 0;
		// for (Entry<DatabaseProxy, Integer> entry : hash.entrySet()) {
		// DatabaseProxy key = entry.getKey();
		// Integer value = entry.getValue();
		//
		// if (value < 2) {
		// count += value;
		// }
		// }

		checkUsedIPs();

		if (!GeckoHandler.mayStartFirefoxBrowser(5)) {
			return;
		}

		System.out.println("[RS AUTOMATIC ACCOUNT CREATION] " + accountsToCreate2()
				+ " accounts left to create accounts with! Total accounts available: "
				+ BotController.getBots().size());

		ArrayList<DatabaseProxy> proxies = new ArrayList<DatabaseProxy>();
		synchronized (getUsedProxies2()) {
			proxies = getUsedProxies2();
			Collections.shuffle(proxies);
		}

		for (DatabaseProxy proxy : proxies) {
			if (!proxy.isOnline()) {
				System.out.println("Skipping this IP, because the proxy is offline E01");
				continue;
			}
			if (!GeckoHandler.mayStartFirefoxBrowser(5)) {
				return;
			}
			if (Config.ERROR_IP && DatabaseUtilities.isErrorProxy(proxy.getProxyIp(), proxy.getProxyPort())) {
				System.out.println("Skipping this IP, because it's currently giving an error");
				continue;
			}
			if (ipExists(proxy.getProxyIp() + ":" + proxy.getProxyPort())) {
				System.out.println("Skipping IP: " + proxy.getProxyIp() + ":" + proxy.getProxyPort()
						+ " because already exists in creating for this IP-addres");
				continue;
			}

			if (proxy.getProxyIp().contains("185.232")) {
				System.out.println("Not using this flagged ip-address to create accounts");
				continue;
			}

			boolean usedProxyAmount = proxy.getUsedCount() < 3 || Config.NEW_PROXYRACK_CONFIGURATION;

			// DatabaseProxy key = entry.getKey();
			// Integer value = entry.getValue();
			int extraBots = 9;
			if ((usedProxyAmount) && (BotController.getBots().size()) < (Config.MAX_BOTS_OPEN + extraBots)) {

				/**
				 * public AccountTable(int id, String script, String username, int world, String
				 * proxyIp, String proxyPort, boolean lowCpuMode, AccountStatus status) {
				 */
				RandomNameGenerator name = new RandomNameGenerator();

				AccountTable table = new AccountTable(-1, "test", name.generateRandomNameString(), 318,
						proxy.getProxyIp(), proxy.getProxyPort(), true, AccountStatus.AVAILABLE,
						AccountStage.TUT_ISLAND, 0);
				table.setPassword(name.generateRandomNameString());
				table.setProxyUsername(proxy.getProxyUsername());
				table.setProxyPassword(proxy.getProxyPassword());
				table.setBankPin("0000");
				table.setCountryProxyCode(IPWhoisApi.getSingleton().getRandomCountryCodeFromList());

				if (Config.isMuleProxy(proxy.getProxyIp(), proxy.getProxyPort())
						|| Config.isSuperMuleProxy(proxy.getProxyIp(), proxy.getProxyPort())
						|| Config.isStaticMuleProxy(proxy.getProxyIp(), proxy.getProxyPort())) {
					System.out.println("IS MULE PROXY, SKIPPING!");
					break;
				}

				addToUsedIpsCreateAccount(proxy.getProxyIp() + ":" + proxy.getProxyPort());

				OsbotController bot = new OsbotController(-1, table);
				System.out.println("Creating account: " + table.getUsername());

				AccountCreationService.launchRunescapeWebsite(proxy, bot, SeleniumType.CREATE_VERIFY_ACCOUNT, false);
				break;
			}

		}

	}

}
