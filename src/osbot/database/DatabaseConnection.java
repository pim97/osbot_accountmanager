package osbot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

	public static DatabaseConnection con = new DatabaseConnection();

	public static DatabaseConnection getDatabase() {
		return con;
	}

	public Connection conn;

	public Connection getConnection() throws SQLException {
		if (conn == null || conn.isClosed()) {
			DatabaseConnection con = new DatabaseConnection();
			return conn = con.connect();
		}
		return conn;
	}

	public ResultSet getResult(String query) {
		try {
			PreparedStatement statement = getConnection().prepareStatement(query);

			ResultSet resultSet = statement.executeQuery(query);
			return resultSet;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// init database constants
	private static final String DATABASE_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/osbot";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "";
	private static final String MAX_POOL = "250";

	// init connection object
	private Connection connection;
	// init properties object
	private Properties properties;

	// create properties
	private Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
			properties.setProperty("user", USERNAME);
			properties.setProperty("password", PASSWORD);
			properties.setProperty("MaxPooledStatements", MAX_POOL);
		}
		return properties;
	}

	// connect database
	public Connection connect() {
		if (connection == null) {
			try {
				Class.forName(DATABASE_DRIVER);
				connection = DriverManager.getConnection(DATABASE_URL, getProperties());
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
		}
		return connection;
	}

	// disconnect database
	public void disconnect() {
		if (connection != null) {
			try {
				connection.close();
				connection = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String args[]) {

	}

}
