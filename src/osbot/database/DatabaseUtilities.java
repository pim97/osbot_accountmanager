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

	/**
	 * 
	 * @param account
	 */
	public static void insertIntoTable(AccountTable account) {

		// the mysql insert statement
		String query = " insert into account (name, password, bank_pin, day, month, year, proxy_ip, proxy_port, world_number, low_cpu_mode, status, email, account_stage)"
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
		String sql = "SELECT ac.*, p.username as p_us, p.password as p_pass FROM account AS ac INNER JOIN proxies AS p ON p.ip_addres=ac.proxy_ip WHERE ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND email IS NOT NULL AND ac.account_stage <> \"OUT_OF_MONEY\" AND email IS NOT NULL AND ac.status <> \"LOCKED_INGAME\" AND ac.status <> \"BANNED\" AND ac.status <> \"INVALID_PASSWORD\" AND ac.status <> \"TIMEOUT\" AND ac.status <> \"TASK_TIMEOUT\" AND ac.status <> \"LOCKED_TIMEOUT\"";
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

	public static ArrayList<DatabaseProxy> getUsedProxies2() {
		String sql = "SELECT * FROM ( SELECT SUM(CASE WHEN ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND ac.status <> \"LOCKED_INGAME\"  AND ac.account_stage <> \"OUT_OF_MONEY\" AND email IS NOT NULL AND ac.status <> \"BANNED\" AND ac.status <> \"INVALID_PASSWORD\" AND ac.status <> \"TIMEOUT\" AND ac.status <> \"TASK_TIMEOUT\" AND ac.status <> \"LOCKED_TIMEOUT\" THEN 1 ELSE 0 END) AS count, p.ip_addres AS proxy, p.port, p.username, p.password, p.mule_proxy FROM proxies AS p LEFT JOIN account AS ac ON ac.proxy_ip=p.ip_addres AND ac.proxy_port=p.port GROUP BY p.ip_addres, p.port, p.username, p.password) AS proxy WHERE mule_proxy <> 1";
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

					DatabaseProxy proxy = new DatabaseProxy(ip, port, username, password);
					proxy.setUsedCount(count);

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
	 * 
	 */
	public static void closeBotsWhenNotActive() {
		for (OsbotController bot : BotController.getBots()) {

			// If starting it, then it may not harm that process to prevent any weird things
			if (bot.isStartingUp()) {
				System.out.println("May not harm this process, it is starting up right now.");
				continue;
			}

			// Is not logged in but in database still logged in
			if ((getLoginStatus(bot.getId()) == LoginStatus.INITIALIZING
					|| getLoginStatus(bot.getId()) == LoginStatus.LOGGED_IN) && bot.getPidId() <= 0) {
				System.out.println("KILLING: 5");
				DatabaseUtilities.updateLoginStatus(LoginStatus.DEFAULT, bot.getId());
			}

			// If the pid ID is lower than 0, then it's not launched
			if (bot.getPidId() <= 0) {
				continue;
			}

			// When account has started, but not logged in between 80 seconds
			if (getLoginStatus(bot.getId()) != null && getLoginStatus(bot.getId()) == LoginStatus.INITIALIZING
					&& (System.currentTimeMillis() - bot.getStartTime() > 58_000)) {
				BotController.killProcess(bot.getPidId());
				bot.setPidId(-1);
				DatabaseUtilities.updateLoginStatus(LoginStatus.DEFAULT, bot.getId());
				System.out.println("KILLING: 2");
			}

			// When the pid is not on the machine active anymore
			if (!BotHandler.isProcessIdRunningOnWindows(bot.getPidId())) {
				BotController.killProcess(bot.getPidId());
				bot.setPidId(-1);
				DatabaseUtilities.updateLoginStatus(LoginStatus.DEFAULT, bot.getId());
				System.out.println("KILLING: 1");
			}

			// When the process is running, but not logged in, then set status back to
			// default
			if (BotHandler.isProcessIdRunningOnWindows(bot.getPidId())
					&& DatabaseUtilities.getLoginStatus(bot.getId()) == LoginStatus.DEFAULT) {
				BotController.killProcess(bot.getPidId());
				bot.setPidId(-1);
				System.out.println("Killing 4");
			}
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
					calendar.add(Calendar.MINUTE, 45);

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

	public static String getTradeWithOther(int accountId) {
		String sql = "SELECT trade_with_other FROM account WHERE id = " + accountId + "";

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

	public static boolean setTradingWith(String tradeWith, int accountId) {
		try {
			String query = "UPDATE account SET trade_with_other = ? WHERE id=?";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, tradeWith);
			preparedStmt.setInt(2, accountId);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();
			preparedStmt.close();

			System.out.println("Updated account in database trading with: " + tradeWith);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
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

			System.out.println("Updated account in database trading with: " + tradeWith);

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

		String sql = "SELECT ac.*, p.username as p_us, p.password as p_pass FROM account AS ac INNER JOIN proxies AS p ON p.ip_addres=ac.proxy_ip WHERE (ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND ac.account_stage <> \"OUT_OF_MONEY\" AND email IS NOT NULL AND ac.status <> \"LOCKED_INGAME\" AND ac.status <> \"BANNED\" AND ac.status <> \"INVALID_PASSWORD\" AND ac.status <> \"TIMEOUT\" AND ac.status <> \"TASK_TIMEOUT\") OR (ac.status=\"TASK_TIMEOUT\" AND amount_timeout < 10) OR (ac.status=\"TIMEOUT\" AND amount_timeout < 10)";

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
					String accountValue = resultSet.getString("account_value");
					AccountStatus status = AccountStatus.valueOf(resultSet.getString("status"));
					String date = resultSet.getString("break_till");
					int amountTimeout = resultSet.getInt("amount_timeout");

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
					account.setProxyUsername(proxyUsername);
					account.setAmountTimeout(amountTimeout);
					account.setProxyPassword(proxyPassword);
					account.setPassword(password);
					account.setScript(scriptName);
					account.setEmail(email);
					account.setQuestPoints(qp);
					account.setAccountValue(formatNumbers(accountValue));
					account.setDate(calendar);
					account.setDateString(date);
					account.setTradeWithOther(tradingWith);

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

	public static ArrayList<OsbotController> getAccountsToBeRecovered() {
		ArrayList<OsbotController> bots = new ArrayList<OsbotController>();
		try {
			String sql = "SELECT * FROM account WHERE status = \"LOCKED\"";
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

	private static boolean containsInPid(int pid) {
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
		if (GeckoHandler.getFirefoxExeWindows().size() > 0 && GeckoHandler.getGeckodriverExeWindows().size() <= 0) {
			WebdriverFunctions.killAll();
		}

		for (Integer pid : GeckoHandler.getFirefoxExeWindows()) {
			if (!containsInPid2(pid)) {
				PidCheck c = new PidCheck(pid);
				GECKO_PIDS.add(c);
				System.out.println("Added new firefox/2 pid: " + c.getPid());
			}
		}

		Iterator<PidCheck> i = GECKO_PIDS.iterator();

		while (i.hasNext()) {
			PidCheck pid = i.next();

			if (!containsInRealTimePid2(pid.getPid())) {
				BotController.killProcess(pid.getPid());
				i.remove();
				System.out.println("Pid /2 " + pid + " was removed, was already open for 5 minutes");
				continue;
			}

			if (pid.getMatches() > 300) {
				BotController.killProcess(pid.getPid());
				i.remove();
				System.out.println("Pid /2 " + pid + " was removed, was already open for 5 minutes");
				continue;
			} else {
				// System.out.println("Firefox /2 pid: " + pid.getPid() + " closing in " +
				// pid.getMatches() + "/300");
			}
			pid.setMatches(pid.getMatches() + 1);
		}

		for (Integer pid : GeckoHandler.getGeckodriverExeWindows()) {
			if (!containsInPid(pid)) {
				FIREFOX_PIDS.add(new PidCheck(pid));
				System.out.println("Added new firefox pid: " + pid);
			}
		}

		Iterator<PidCheck> b = FIREFOX_PIDS.iterator();

		while (b.hasNext()) {
			PidCheck pid = b.next();

			if (!containsInRealTimePid(pid.getPid())) {
				BotController.killProcess(pid.getPid());
				b.remove();
				// System.out.println("Pid " + pid + " was removed, was already open for 5
				// minutes");
				continue;
			}

			if (pid.getMatches() > 300) {
				BotController.killProcess(pid.getPid());
				b.remove();
				// System.out.println("Pid " + pid + " was removed, was already open for 5
				// minutes");
				continue;
			} else {
				// System.out.println("Firefox pid: " + pid.getPid() + " closing in " +
				// pid.getMatches() + "/300");
			}
			pid.setMatches(pid.getMatches() + 1);
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		String sql = "SELECT COUNT(*) as available_accounts FROM account AS ac INNER JOIN proxies AS p ON p.ip_addres=ac.proxy_ip WHERE ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND email IS NOT NULL  AND ac.account_stage <> \"OUT_OF_MONEY\" AND ac.status <> \"LOCKED_INGAME\" AND ac.status <> \"BANNED\" AND ac.status <> \"LOCKED_TIMEOUT\"";

		try {
			PreparedStatement preparedStatement = DatabaseConnection.getDatabase().getConnection()
					.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery(sql);
			try {
				int availableAccounts = 0;

				while (resultSet.next()) {
					availableAccounts = resultSet.getInt("available_accounts");
				}
				return availableAccounts;
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
				}
				return muleCount;
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

	/**
	 * Recovers an account
	 */
	public static void seleniumRecoverAccount() {

		synchronized (getAccountsToBeRecovered()) {
			// AccountCreationService.checkPreviousProcessesAndDie(SeleniumType.RECOVER_ACCOUNT);

			// if (!AccountCreationService.getLaunching()) {
			// AccountCreationService.checkProcesses();
			// }

			// if (AccountCreationService.getLaunching()) {
			// return;
			// }

			System.out.println("[ACCOUNT RECOVERING] " + getAccountsToBeRecovered().size()
					+ " accounts left to recover currently");

			System.out.println(
					"[ACCOUNT RECOVERING] " + AccountCreationService.getUsedUsernames().size() + " accounts timed out");

			ArrayList<OsbotController> accs = getAccountsToBeRecovered();
			Collections.shuffle(accs);

			for (OsbotController account : accs) {
				if (GeckoHandler.getGeckodriverExeWindows().size() > 5) {
					System.out.println("Breaking because too many geckodrivers active!");
					break;
				}
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

					AccountCreationService.launchRunescapeWebsite(proxy, account, SeleniumType.RECOVER_ACCOUNT);
					break;
					// System.out.println("Recovering account: " +
					// account.getAccount().getUsername());
				}

			}
		}

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

	public static void transformIntoMuleHandler() {
		System.out.println("[MULE CREATION] Current amount of mules: " + DatabaseUtilities.getMuleAmount() + " time: "
				+ (System.currentTimeMillis() - lastAttempt));

		// if (DatabaseUtilities.getMuleAmount() < 2) {
		// System.out.println("Not enough mules -- creating/setting a new mule");
		//
		// OsbotController mule = null;
		// for (OsbotController newMule : BotController.getBots()) {
		// if (newMule.getAccount().getStatus() == AccountStatus.AVAILABLE
		// && Integer.parseInt(newMule.getAccount().getAccountValue()) <= 1000
		// && newMule.getAccount().getStage() != AccountStage.TUT_ISLAND
		// && (newMule.getAccount().getStage() == AccountStage.QUEST_COOK_ASSISTANT
		// || getMuleAmount() <= 0)) {
		// mule = newMule;
		// System.out.println("Found a new mule!");
		// break;
		// }
		// }
		// if (mule != null) {
		// mule.getAccount().setStage(AccountStage.UNKNOWN);
		// mule.getAccount().setStatus(AccountStatus.MULE);
		// System.out.println("Set account: " + mule.getAccount().getUsername() + " to a
		// mule!");
		// DatabaseUtilities.updateStatusOfAccountById(AccountStatus.MULE,
		// mule.getId());
		// DatabaseUtilities.updateAccountStage(AccountStage.UNKNOWN, mule.getId());
		// }
		// }

		/**
		 * Making mules with that specific IP-adress
		 */

		if (DatabaseUtilities.getMuleAmount() < 1) {
			// Make an account once every 20 minutes
			for (OsbotController mule : BotController.getBots()) {
				if (mule.getAccount().getStage() != AccountStage.TUT_ISLAND
						&& mule.getAccount().getStatus() == AccountStatus.AVAILABLE
						&& Config.isMuleProxy(mule.getAccount().getProxyIp(), mule.getAccount().getProxyPort())) {

					System.out.println("Mule completed tutorial island, setting to official mule now!");
					mule.getAccount().setStage(AccountStage.UNKNOWN);
					mule.getAccount().setStatus(AccountStatus.MULE);

					updateAccountStage(mule.getAccount().getStage(), mule.getId());
					updateStatusOfAccountById(mule.getAccount().getStatus(), mule.getId());
				}
			}
		}

		if (DatabaseUtilities.getMuleAmount() < 1 && (System.currentTimeMillis() - lastAttempt) > 1_200_000) {
			lastAttempt = System.currentTimeMillis();

			RandomNameGenerator name = new RandomNameGenerator();

			String[] proxyString = Config.getRandomMuleProxy().split(":");

			AccountTable table = new AccountTable(-1, "test", name.generateRandomNameString(), 394, proxyString[0],
					proxyString[1], true, AccountStatus.AVAILABLE, AccountStage.TUT_ISLAND, 0);

			table.setPassword(name.generateRandomNameString());
			table.setProxyUsername("hjeg53");
			table.setProxyPassword("L9MbdJ");
			table.setBankPin("0000");

			DatabaseProxy proxy = new DatabaseProxy(table.getProxyUsername(), table.getProxyPort(),
					table.getProxyUsername(), table.getProxyPassword());

			OsbotController bot = new OsbotController(-1, table);
			System.out.println("Creating account: " + table.getUsername() + " stage: " + bot.getAccount().getStage());

			AccountCreationService.launchRunescapeWebsite(proxy, bot, SeleniumType.CREATE_VERIFY_ACCOUNT);

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

		System.out.println("[RS AUTOMATIC ACCOUNT CREATION] " + accountsToCreate2()
				+ " accounts left to create accounts with! Total accounts available: " + totalAccountsAvailable());

		ArrayList<DatabaseProxy> proxies = getUsedProxies2();
		Collections.shuffle(proxies);

		for (DatabaseProxy proxy : proxies) {
			if (GeckoHandler.getGeckodriverExeWindows().size() > 5) {
				System.out.println("Breaking because too many geckodrivers active!");
				break;
			}
			if (ipExists(proxy.getProxyIp() + ":" + proxy.getProxyPort())) {
				System.out.println("Skipping IP: " + proxy.getProxyIp() + ":" + proxy.getProxyPort()
						+ " because already exists in creating for this IP-addres");
				continue;
			}
			// DatabaseProxy key = entry.getKey();
			// Integer value = entry.getValue();
			if (proxy.getUsedCount() < 2
					&& totalAccountsAvailable() < (Config.MAX_BOTS_OPEN + (Config.MAX_BOTS_OPEN / 5))) {
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

				if (Config.isMuleProxy(proxy.getProxyIp(), proxy.getProxyPort())) {
					System.out.println("IS MULE PROXY, SKIPPING!");
					break;
				}

				addToUsedIpsCreateAccount(proxy.getProxyIp() + ":" + proxy.getProxyPort());

				OsbotController bot = new OsbotController(-1, table);
				System.out.println("Creating account: " + table.getUsername());

				AccountCreationService.launchRunescapeWebsite(proxy, bot, SeleniumType.CREATE_VERIFY_ACCOUNT);
				break;
			}

		}

	}

}
