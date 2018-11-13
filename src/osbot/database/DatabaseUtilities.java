package osbot.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import osbot.account.AccountStage;
import osbot.account.AccountStatus;
import osbot.account.creator.AccountCreationService;
import osbot.account.creator.RandomNameGenerator;
import osbot.account.handler.GeckoHandler;
import osbot.account.webdriver.WebdriverFunctions;
import osbot.bot.BotController;
import osbot.random.RandomUtil;
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

				proxiesInDatabase.add(new DatabaseProxy(resultSet.getString("ip_addres"), resultSet.getString("port"),
						resultSet.getString("username"), resultSet.getString("password")));
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
	public static String getEmailFromUsername(String username) {
		String sql = "SELECT email FROM account WHERE email=\"" + username + "\"";

		ResultSet resultSet = DatabaseConnection.getDatabase().getResult(sql);

		String email = null;

		try {
			while (resultSet.next()) {
				email = resultSet.getString("email");

			}
			return email;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return email;
	}

	/**
	 * 
	 * @return
	 */
	public static ArrayList<DatabaseProxy> getUsedProxies() {
		String sql = "SELECT ac.*, p.username as p_us, p.password as p_pass FROM account AS ac INNER JOIN proxies AS p ON p.ip_addres=ac.proxy_ip WHERE ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND ac.status <> \"LOCKED_INGAME\" AND ac.status <> \"BANNED\" AND ac.status <> \"INVALID_PASSWORD\" AND ac.status <> \"TIMEOUT\" AND ac.status <> \"TASK_TIMEOUT\"";

		ResultSet resultSet = DatabaseConnection.getDatabase().getResult(sql);
		ArrayList<DatabaseProxy> proxiesOutDatabase = new ArrayList<DatabaseProxy>();

		try {
			while (resultSet.next()) {

				proxiesOutDatabase.add(new DatabaseProxy(resultSet.getString("proxy_ip"),
						resultSet.getString("proxy_port"), resultSet.getString("p_us"), resultSet.getString("p_pass")));

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

	public static boolean updateStatusOfAccountByIp(AccountStatus status, String ip) {
		try {
			String query = "UPDATE account SET status = ? WHERE proxy_ip=? AND status=\"LOCKED\" AND updated_at BETWEEN SUBDATE(NOW(),1) AND NOW()";
			// String query = "UPDATE account SET status = ? WHERE proxy_ip=? BETWEEN
			// SUBDATE(NOW(),1) AND NOW() AND status=\"LOCKED\"";
			PreparedStatement preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);
			preparedStmt.setString(1, status.name());
			preparedStmt.setString(2, ip);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();

			System.out.println(preparedStmt.toString());
			System.out.println("Updated account status in database with ip! " + status.name() + " ip: " + ip);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void changeTimeoutLockedToNormal() {
		String sql = "SELECT proxy_ip,updated_at FROM account WHERE status=\"LOCKED_TIMEOUT\"";

		try {
			ResultSet resultSet = DatabaseConnection.getDatabase().getResult(sql);
			while (resultSet.next()) {
				String updatedAt = resultSet.getString("updated_at");
				String ip = resultSet.getString("proxy_ip");

				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MINUTE, 30);
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

				try {
					Date date2 = sdf.parse(updatedAt);
					calendar.setTime(date2);

					Calendar calendar2 = Calendar.getInstance();
					calendar2.setTime(new Date());

					if (calendar2.after(date2)) {
						System.out.println("Updated the LOCKED_TIMEOUT back to LOCKED, due to 30 minutes have past");
						updateStatusOfAccountByIp(AccountStatus.LOCKED, ip);
					}

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// return max + 1;
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
			ResultSet resultSet = DatabaseConnection.getDatabase().getResult(sql);
			while (resultSet.next()) {
				String stage = resultSet.getString("account_stage");

				return stage;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getTradeWithOther(int accountId) {
		String sql = "SELECT trade_with_other FROM account WHERE id = " + accountId + "";

		try {
			ResultSet resultSet = DatabaseConnection.getDatabase().getResult(sql);
			while (resultSet.next()) {
				String stage = resultSet.getString("trade_with_other");

				return stage;
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

		String sql = "SELECT ac.*, p.username as p_us, p.password as p_pass FROM account AS ac INNER JOIN proxies AS p ON p.ip_addres=ac.proxy_ip WHERE ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND ac.status <> \"LOCKED_INGAME\" AND ac.status <> \"BANNED\" AND ac.status <> \"INVALID_PASSWORD\"";
		// String sql = "SELECT ac.*, p.username as p_us, p.password as p_pass FROM
		// account AS ac INNER JOIN proxies AS p ON p.ip_addres=ac.proxy_ip WHERE
		// ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND ac.status <>
		// \"BANNED\"";

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
				String accountValue = resultSet.getString("account_value");
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
				account.setAccountValue(formatNumbers(accountValue));
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
		String sql = "SELECT * FROM account WHERE status = \"LOCKED\"";
		try {
			ResultSet resultSet = DatabaseConnection.getDatabase().getResult(sql);

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
		}
		return bots;
	}

	public static void main(String[] args) {
		seleniumRecoverAccount();
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

	private static List<PidCheck> FIREFOX_PIDS2 = new ArrayList<PidCheck>();

	private static boolean containsInPid2(int pid) {
		for (PidCheck localPid : FIREFOX_PIDS2) {
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
		new Thread(() -> {

			while (true) {

				if (GeckoHandler.getFirefoxExeWindows().size() > 0
						&& GeckoHandler.getGeckodriverExeWindows().size() <= 0) {
					WebdriverFunctions.killAll();
				}

				for (Integer pid : GeckoHandler.getFirefoxExeWindows()) {
					if (!containsInPid2(pid)) {
						PidCheck c = new PidCheck(pid);
						FIREFOX_PIDS2.add(c);
						System.out.println("Added new firefox/2 pid: " + c.getPid());
					}
				}

				Iterator<PidCheck> i = FIREFOX_PIDS2.iterator();

				while (i.hasNext()) {
					PidCheck pid = i.next();

					if (!containsInRealTimePid2(pid.getPid())) {
						BotController.killProcess(pid.getPid());
						i.remove();
						// System.out.println("Pid /2 " + pid + " was removed, was already open for 5
						// minutes");
						continue;
					}

					if (pid.getMatches() > 300) {
						BotController.killProcess(pid.getPid());
						i.remove();
						// System.out.println("Pid /2 " + pid + " was removed, was already open for 5
						// minutes");
						continue;
					} else {
						// System.out.println("Firefox /2 pid: " + pid.getPid() + " closing in " +
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

		}).start();

		new Thread(() -> {

			while (true) {

				for (Integer pid : GeckoHandler.getGeckodriverExeWindows()) {
					if (!containsInPid(pid)) {
						FIREFOX_PIDS.add(new PidCheck(pid));
						System.out.println("Added new firefox pid: " + pid);
					}
				}

				Iterator<PidCheck> i = FIREFOX_PIDS.iterator();

				while (i.hasNext()) {
					PidCheck pid = i.next();

					if (!containsInRealTimePid(pid.getPid())) {
						BotController.killProcess(pid.getPid());
						i.remove();
						// System.out.println("Pid " + pid + " was removed, was already open for 5
						// minutes");
						continue;
					}

					if (pid.getMatches() > 300) {
						BotController.killProcess(pid.getPid());
						i.remove();
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

		}).start();
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
		try {
			String sql = "SELECT COUNT(*) as available_accounts FROM account AS ac INNER JOIN proxies AS p ON p.ip_addres=ac.proxy_ip WHERE ac.visible = \"true\" AND ac.status <> \"MANUAL_REVIEW\" AND ac.status <> \"LOCKED_INGAME\" AND ac.status <> \"BANNED\"";
			int availableAccounts = 0;

			ResultSet resultSet = DatabaseConnection.getDatabase().getResult(sql);

			while (resultSet.next()) {
				availableAccounts = resultSet.getInt("available_accounts");
			}
			return availableAccounts;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static int getMuleAmount() {
		try {
			String sql = "SELECT COUNT(*) as mule_count FROM account WHERE account_stage = \"UNKNOWN\" AND status = \"MULE\"";
			int muleCount = 0;

			ResultSet resultSet = DatabaseConnection.getDatabase().getResult(sql);

			while (resultSet.next()) {
				muleCount = resultSet.getInt("mule_count");
			}
			return muleCount;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Recovers an account
	 */
	public static void seleniumRecoverAccount() {

		// AccountCreationService.checkPreviousProcessesAndDie(SeleniumType.RECOVER_ACCOUNT);

		if (!AccountCreationService.getLaunching()) {
			AccountCreationService.checkProcesses();
		}

		if (AccountCreationService.getLaunching()) {
			return;
		}

		System.out.println(
				"[ACCOUNT RECOVERING] " + getAccountsToBeRecovered().size() + " accounts left to recover currently");

		if (getAccountsToBeRecovered().size() == 0) {
			System.out.println("[ACCOUNT RECOVERING] No accounts to be recovered");
			return;
		}
		OsbotController account = getAccountsToBeRecovered().get(RandomUtil.getRandomNumberInRange(0,
				(getAccountsToBeRecovered().size() - 1 < 0 ? 0 : getAccountsToBeRecovered().size() - 1)));

		if (account != null) {
			// for (OsbotController account : getAccountsToBeRecovered()) {
			DatabaseProxy proxy = new DatabaseProxy(account.getAccount().getProxyIp(),
					account.getAccount().getProxyPort(), account.getAccount().getProxyUsername(),
					account.getAccount().getProxyPassword());

			// AccountCreationService.launchRunescapeWebsite(proxy, account,
			// SeleniumType.RECOVER_ACCOUNT);
		}
		// System.out.println("Recovering account: " +
		// account.getAccount().getUsername());

		// }

	}

	/**
	 * 
	 */
	public static void seleniumCreateAccountThread() {

		if (!AccountCreationService.getLaunching()) {
			AccountCreationService.checkProcesses();
		}

		if (AccountCreationService.getLaunching()) {
			return;
		}

		// AccountCreationService.checkPreviousProcessesAndDie(SeleniumType.CREATE_VERIFY_ACCOUNT);

		HashMap<DatabaseProxy, Integer> hash = oneExistsInOther(getTotalProxies(), getUsedProxies());

		int count = 0;
		for (Entry<DatabaseProxy, Integer> entry : hash.entrySet()) {
			DatabaseProxy key = entry.getKey();
			Integer value = entry.getValue();

			if (value < 3) {
				count += value;
			}
		}

		System.out.println("[RS AUTOMATIC ACCOUNT CREATION] " + count + " accounts left to create accounts with!");
		for (Entry<DatabaseProxy, Integer> entry : hash.entrySet()) {
			DatabaseProxy key = entry.getKey();
			Integer value = entry.getValue();

			if (value < 3) {
				/**
				 * public AccountTable(int id, String script, String username, int world, String
				 * proxyIp, String proxyPort, boolean lowCpuMode, AccountStatus status) {
				 */
				RandomNameGenerator name = new RandomNameGenerator();

				AccountTable table = new AccountTable(-1, "test", name.generateRandomNameString(), 318,
						key.getProxyIp(), key.getProxyPort(), true, AccountStatus.AVAILABLE, AccountStage.TUT_ISLAND,
						0);
				table.setPassword(name.generateRandomNameString());
				table.setProxyUsername(key.getProxyUsername());
				table.setProxyPassword(key.getProxyPassword());
				table.setBankPin("0000");
				OsbotController bot = new OsbotController(-1, table);

				// AccountCreationService.launchRunescapeWebsite(key, bot,
				// SeleniumType.CREATE_VERIFY_ACCOUNT);
			}

		}

	}

}
