package osbot.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import osbot.account.AccountStatus;
import osbot.tables.AccountTable;

public class DatabaseUtilities {

	public static void insertIntoTable(AccountTable account) {

		// the mysql insert statement
		String query = " insert into account (id, name, password, bank_pin, proxy_ip, proxy_port, world_number, low_cpu_mode, status)"
				+ " values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		// create the mysql insert preparedstatement
		PreparedStatement preparedStmt;
		try {
			preparedStmt = DatabaseConnection.getDatabase().getConnection().prepareStatement(query);

			preparedStmt.setInt(1, account.getId());
			preparedStmt.setString(2, account.getUsername());
			preparedStmt.setString(3, account.getPassword());
			preparedStmt.setString(4, account.getBankPin());
			preparedStmt.setString(5, account.getProxyIp());
			preparedStmt.setString(6, account.getProxyPort());
			preparedStmt.setInt(7, account.getWorld());
			preparedStmt.setBoolean(8, account.isLowCpuMode());
			preparedStmt.setString(9, account.getStatus().name());

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

	public static ArrayList<AccountTable> getAccountsFromMysqlConnection() {
		ArrayList<AccountTable> accounts = new ArrayList<AccountTable>();

		String sql = "SELECT * FROM `account`";
		try {
			ResultSet resultSet = DatabaseConnection.getDatabase().getResult(sql);

			while (resultSet.next()) {
				int id = resultSet.getInt("id");
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

				accounts.add(account);

			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// DatabaseConnection.getDatabase().disconnect();
		}
		return accounts;
	}

	public static void main(String args[]) {
		// deleteFromTable();
		 getAccountsFromMysqlConnection().forEach(a -> {
			 insertIntoTable(a);
		 });
	}

}
