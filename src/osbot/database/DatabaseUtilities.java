package osbot.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import osbot.account.AccountStage;
import osbot.account.AccountStatus;
import osbot.account.creator.AccountCreationService;
import osbot.account.creator.RandomNameGenerator;
import osbot.account.creator.SeleniumType;
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
		String query = " insert into account (name, password, bank_pin, day, month, year, proxy_ip, proxy_port, world_number, low_cpu_mode, status, email)"
				+ " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

			// execute the preparedstatement
			preparedStmt.execute();
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

		ResultSet resultSet = DatabaseConnection.getDatabase().getResult(sql);
		ArrayList<DatabaseProxy> proxiesInDatabase = new ArrayList<DatabaseProxy>();

		try {
			while (resultSet.next()) {

				proxiesInDatabase.add(new DatabaseProxy(resultSet.getString("ip_addres"), resultSet.getString("port")));
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
	public static ArrayList<DatabaseProxy> getUsedProxies() {
		String sql = "SELECT * FROM account as a\r\n" + "WHERE a.`status`=\"available\"";

		ResultSet resultSet = DatabaseConnection.getDatabase().getResult(sql);
		ArrayList<DatabaseProxy> proxiesOutDatabase = new ArrayList<DatabaseProxy>();

		try {
			while (resultSet.next()) {

				proxiesOutDatabase
						.add(new DatabaseProxy(resultSet.getString("proxy_ip"), resultSet.getString("proxy_port")));

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return proxiesOutDatabase;
	}

	public static int getMaxInteger() {
		String sql = "SELECT MAX(id) as max FROM `account`";

		try {
			ResultSet resultSet = DatabaseConnection.getDatabase().getResult(sql);
			while (resultSet.next()) {
				int max = resultSet.getInt("max");

				return max + 1;
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

			System.out.println("Updated account in database with new password!");

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

		String sql = "SELECT ac.*, p.username as p_us, p.password as p_pass FROM account AS ac INNER JOIN proxies AS p ON p.ip_addres=ac.proxy_ip WHERE ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND ac.status <> \"LOCKED_INGAME\" AND ac.status <> \"BANNED\"";
		try {
			ResultSet resultSet = DatabaseConnection.getDatabase().getResult(sql);

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
				int accountValue = resultSet.getInt("account_value");
				AccountStatus status = AccountStatus.valueOf(resultSet.getString("status"));
				String date = resultSet.getString("break_till");

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

				AccountTable account = new AccountTable(id, null, name, world, proxyIp, proxyPort, lowCpuMode, status,
						stage, account_stage_progress);
				account.setProxyUsername(proxyUsername);
				account.setProxyPassword(proxyPassword);
				account.setPassword(password);
				account.setScript(scriptName);
				account.setEmail(email);
				account.setQuestPoints(qp);
				account.setAccountValue(accountValue);
				account.setDate(calendar);
				account.setDateString(date);
				account.setTradeWithOther(tradingWith);

				accounts.add(account);

			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// DatabaseConnection.getDatabase().disconnect();
		}
		return accounts;
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
		String sql = "SELECT * FROM account WHERE status = \"LOCKED\"";
		try {
			ResultSet resultSet = DatabaseConnection.getDatabase().getResult(sql);

			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				System.out.println("Account id: " + id + " has to get recovered");

				OsbotController bot = BotController.getBotById(id);
				if (bot != null) {
					bots.add(bot);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bots;
	}

	public static void main(String[] args) {
		// seleniumRecoverAccount();
		// seleniumCreateAccountThread();
	}

	/**
	 * Recovers an account
	 */
	public static void seleniumRecoverAccount() {
		new Thread(() -> {
			{

				while (true) {
					System.out.println("[ACCOUNT RECOVERING] " + getAccountsToBeRecovered().size()
							+ " accounts left to recover currently");
					// System.out.println(getAccountsToBeRecovered().size());
					for (OsbotController account : getAccountsToBeRecovered()) {
						// if (account.getAccount().getUsername().equalsIgnoreCase("solomid6lz")) {
						DatabaseProxy proxy = new DatabaseProxy(account.getAccount().getProxyIp(),
								account.getAccount().getProxyPort());
						AccountCreationService.launchRunescapeWebsite(proxy, account, SeleniumType.RECOVER_ACCOUNT);
						System.out.println("Recovering account: " + account.getAccount().getUsername());
						// }
					}
					//
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

		}).start();
	}

	/**
	 * 
	 */
	public static void seleniumCreateAccountThread() {
		new Thread(() -> {

			while (true) {

				HashMap<DatabaseProxy, Integer> hash = oneExistsInOther(getTotalProxies(), getUsedProxies());

				int count = 0;
				for (Entry<DatabaseProxy, Integer> entry : hash.entrySet()) {
					DatabaseProxy key = entry.getKey();
					Integer value = entry.getValue();

					if (value < 2) {
						count += value;
					}
				}

				System.out.println(
						"[RS AUTOMATIC ACCOUNT CREATION] " + count + " accounts left to create accounts with!");
				for (Entry<DatabaseProxy, Integer> entry : hash.entrySet()) {
					DatabaseProxy key = entry.getKey();
					Integer value = entry.getValue();

					if (value < 2) {
						/**
						 * public AccountTable(int id, String script, String username, int world, String
						 * proxyIp, String proxyPort, boolean lowCpuMode, AccountStatus status) {
						 */

						RandomNameGenerator name = new RandomNameGenerator();

						AccountTable table = new AccountTable(-1, "test", name.generateRandomNameString(), 318,
								key.getProxyIp(), key.getProxyPort(), true, AccountStatus.AVAILABLE,
								AccountStage.TUT_ISLAND, 0);
						table.setPassword(name.generateRandomNameString());
						table.setBankPin("0000");
						OsbotController bot = new OsbotController(-1, table);

						AccountCreationService.launchRunescapeWebsite(key, bot, SeleniumType.CREATE_VERIFY_ACCOUNT);
					}

				}

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}).start();
	}

}
