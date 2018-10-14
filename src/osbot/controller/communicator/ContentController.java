package osbot.controller.communicator;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import osbot.account.AccountStatus;
import osbot.account.global.Config;
import osbot.bot.BotController;
import osbot.controller.PopupController;
import osbot.database.DatabaseUtilities;
import osbot.settings.CliArgs;
import osbot.settings.OsbotController;
import osbot.tables.AccountTable;

public class ContentController {

	/**
	 * Table start
	 */
	@FXML
	public TableView<AccountTable> table;

	@FXML
	TableColumn<AccountTable, Integer> id;

	@FXML
	TableColumn<AccountTable, String> script;

	@FXML
	TableColumn<AccountTable, String> proxyIp;

	@FXML
	TableColumn<AccountTable, String> username;

	@FXML
	TableColumn<AccountTable, String> world;

	@FXML
	TableColumn<AccountTable, String> proxyPort;

	@FXML
	TableColumn<AccountTable, Boolean> lowCpuMode;

	@FXML
	TableColumn<AccountTable, AccountStatus> status;

	public static ObservableList<AccountTable> dataAccountTable = FXCollections.observableArrayList();

	/**
	 * Table end
	 */

	@FXML
	private Button button;

	@FXML
	private Button startButton;

	@FXML
	private Button stopButton;

	@FXML
	private Button buttonDeleteAccount;

	@FXML
	private Button editValueInTableButton;

	@FXML
	public void show() {

	}

	@FXML
	private void deleteAccounts() {

		for (AccountTable account : table.getSelectionModel().getSelectedItems()) {
			int id = account.getId();// table.getSelectionModel().getSelectedItem().getId();
			DatabaseUtilities.deleteFromTable(id);
		}
		table.getItems().removeAll(table.getSelectionModel().getSelectedItems());
	}

	@FXML
	private void openWindow() {
		try {
			PopupController popup = new PopupController();
			popup.start(new Stage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	private void startBot() {
		OsbotController bot = BotController.getBotById(table.getSelectionModel().getSelectedItem().getId());
		AccountTable account = bot.getAccount();

		// bot.addArguments(CliArgs.DEBUG, false, 5005);
		bot.addArguments(CliArgs.LOGIN, true, Config.OSBOT_USERNAME, Config.OSBOT_PASSWORD);
		bot.addArguments(CliArgs.DATA, false, 0);

		if (account.hasUsernameAndPasswordAndBankpin()) {
			bot.addArguments(CliArgs.BOT, true, account.getUsername(), account.getPassword(), account.getBankPin());
		} else if (account.hasUsernameAndPassword()) {
			bot.addArguments(CliArgs.BOT, true, account.getUsername(), account.getPassword(), "0000");
		}
		if (account.hasValidProxy()) {
			bot.addArguments(CliArgs.PROXY, true, account.getProxyIp(), account.getProxyPort(), Config.PROXY_USERNAME,
					Config.PROXY_PASSWORD);
		}
		if (account.hasScript()) {
			bot.addArguments(CliArgs.SCRIPT, true, account.getScript(), account.getScript());
		}
		bot.runBot();

	}

	@FXML
	private void stopBot() {
		OsbotController bot = BotController.getBotById(table.getSelectionModel().getSelectedItem().getId());
		BotController.killProcess(bot.getPidId());
	}

	@FXML
	public void initialize() {

		id.setCellValueFactory(new PropertyValueFactory<AccountTable, Integer>("id"));
		script.setCellValueFactory(new PropertyValueFactory<AccountTable, String>("script"));
		username.setCellValueFactory(new PropertyValueFactory<AccountTable, String>("username"));
		world.setCellValueFactory(new PropertyValueFactory<AccountTable, String>("world"));
		proxyIp.setCellValueFactory(new PropertyValueFactory<AccountTable, String>("proxyIp"));
		proxyPort.setCellValueFactory(new PropertyValueFactory<AccountTable, String>("proxyPort"));
		lowCpuMode.setCellValueFactory(new PropertyValueFactory<AccountTable, Boolean>("lowCpuMode"));
		status.setCellValueFactory(new PropertyValueFactory<AccountTable, AccountStatus>("status"));

		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		table.setItems(dataAccountTable);

		/**
		 * public AccountTable(String script, String username, int world, String
		 * proxyIp, String proxyPort, boolean lowCpuMode, AccountStatus status) {
		 */
		ArrayList<AccountTable> account = DatabaseUtilities.getAccountsFromMysqlConnection();
		for (AccountTable acc : account) {
			table.getItems().add(new AccountTable(acc.getId(), acc.getScript(), acc.getUsername(), acc.getWorld(),
					acc.getProxyIp(), acc.getProxyPort(), acc.isLowCpuMode(), acc.getStatus()));
			BotController.addBot(new OsbotController(acc.getId(), acc));
		}

		// RunBot bot = new RunBot();
		//
		// bot.addArguments(CliArgs.WORLD, "318");
		// bot.runBot();
		System.out.println("Initializing bot");

	}

}
