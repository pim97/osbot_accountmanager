package osbot.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import osbot.account.AccountStatus;
import osbot.account.creator.AccountCreationService;
import osbot.account.creator.RandomNameGenerator;
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

//			preparedStmt.setInt(1, account.getId());
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
		String sql = "SELECT * FROM `proxies`";

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
		String sql = "SELECT * FROM `account`";

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
	 * 
	 * @return
	 */
	public static ArrayList<AccountTable> getAccountsFromMysqlConnection() {
		ArrayList<AccountTable> accounts = new ArrayList<AccountTable>();

		String sql = "SELECT * FROM `account`";
		try {
			ResultSet resultSet = DatabaseConnection.getDatabase().getResult(sql);

			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String email = resultSet.getString("email");
				String name = resultSet.getString("name");
				String password = resultSet.getString("password");
				int world = resultSet.getInt("world_number");
				String proxyIp = resultSet.getString("proxy_ip");
				String proxyPort = resultSet.getString("proxy_port");
				String scriptName = resultSet.getString("scriptname");
				boolean lowCpuMode = resultSet.getBoolean("low_cpu_mode");
				AccountStatus status = AccountStatus.valueOf(resultSet.getString("status"));

				AccountTable account = new AccountTable(id, null, name, world, proxyIp, proxyPort, lowCpuMode, status);
				account.setPassword(password);
				account.setScript(scriptName);
				account.setEmail(email);

				accounts.add(account);

			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// DatabaseConnection.getDatabase().disconnect();
		}
		return accounts;
	}

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
		}
		return hash;
	}
	

	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		// deleteFromTable();
		new Thread(() -> {

			HashMap<DatabaseProxy, Integer> hash = oneExistsInOther(getTotalProxies(), getUsedProxies());

			for (Entry<DatabaseProxy, Integer> entry : hash.entrySet()) {
				DatabaseProxy key = entry.getKey();
				Integer value = entry.getValue();

				if (value < 3) {
					/**
					 * public AccountTable(int id, String script, String username, int world, String
					 * proxyIp, String proxyPort, boolean lowCpuMode, AccountStatus status) {
					 */

					RandomNameGenerator name = new RandomNameGenerator();

					AccountTable table = new AccountTable(-1, "test", name.generateRandomNameString(),
							318, key.getProxyIp(), key.getProxyPort(), true,
							AccountStatus.AVAILABLE);
					table.setPassword(name.generateRandomNameString());
					table.setBankPin("0000");
					OsbotController bot = new OsbotController(-1, table);
					
					AccountCreationService.launchRunescapeWebsite(key, bot, true);
				}

			}

		}).start();

		// getUsedProxies();
		// getAccountsFromMysqlConnection().forEach(a -> {
		// insertIntoTable(a);
		// });
	}

}
