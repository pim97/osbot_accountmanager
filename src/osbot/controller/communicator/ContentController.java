package osbot.controller.communicator;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import osbot.account.AccountStage;
import osbot.account.AccountStatus;
import osbot.account.global.Config;
import osbot.account.handler.BotHandler;
import osbot.account.webdriver.WebdriverFunctions;
import osbot.bot.BotController;
import osbot.controller.PopupController;
import osbot.database.DatabaseUtilities;
import osbot.settings.CliArgs;
import osbot.settings.OsbotController;
import osbot.tables.AccountTable;
import osbot.threads.ThreadHandler;

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
	TableColumn<AccountTable, String> scriptProgress;

	@FXML
	TableColumn<AccountTable, String> email;

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
	private Button createAccsButton;

	@FXML
	private Button stopButton;

	@FXML
	private Button instantKillButton;

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
	private void instantKillAllBots() {
		BotHandler.killAllBots();
		System.out.println("Killed all bots!");
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
//		AccountTable account = bot.getAccount();

		BotHandler.runBot(bot);
		
		// bot.addArguments(CliArgs.DEBUG, false, 5005);
//		bot.addArguments(CliArgs.LOGIN, true, Config.OSBOT_USERNAME, Config.OSBOT_PASSWORD);
//		bot.addArguments(CliArgs.DATA, false, 0);
//		bot.addArguments(CliArgs.WORLD, false, account.getWorld());
//		bot.addArguments(CliArgs.MEM, false, "1024");
//
//		if (!account.getScript().equalsIgnoreCase(AccountStage.TUT_ISLAND.name())) {
//			bot.addArguments(CliArgs.ALLOW, false, "norandoms");
//		}
//
//		if (account.hasUsernameAndPasswordAndBankpin()) {
//			bot.addArguments(CliArgs.BOT, true, account.getEmail(), account.getPassword(), account.getBankPin());
//		} else if (account.hasUsernameAndPassword()) {
//			bot.addArguments(CliArgs.BOT, true, account.getEmail(), account.getPassword(), "0000");
//		}
//		if (account.hasValidProxy()) {
//			bot.addArguments(CliArgs.PROXY, true, account.getProxyIp(), account.getProxyPort(), Config.PROXY_USERNAME,
//					Config.PROXY_PASSWORD);
//		}
//		if (account.hasScript()) {
//			String accountStatus = bot.getAccount().getStatus().name().replaceAll("_", "-");
//			bot.addArguments(CliArgs.SCRIPT, true, account.getScript(),	
//					account.getEmail() + "_" + account.getPassword() + "_" + bot.getPidId()+"_"+accountStatus);
//		}
//		bot.runBot();

	}

	@FXML
	private ToggleButton toggleButton;

	@FXML
	private void toggleBot() {

		BotHandler.handleBots();

		// while (BotController.getJavaPIDsWindows().size() < 5) {
		// for (OsbotController bot : BotController.getBots()) {
		// AccountTable account = bot.getAccount();
		//
		// if (account.getStatus() == AccountStatus.AVAILABLE) {
		// // bot.addArguments(CliArgs.DEBUG, false, 5005);
		// bot.addArguments(CliArgs.LOGIN, true, Config.OSBOT_USERNAME,
		// Config.OSBOT_PASSWORD);
		// bot.addArguments(CliArgs.DATA, false, 0);
		// bot.addArguments(CliArgs.WORLD, false, account.getWorld());
		// bot.addArguments(CliArgs.MEM, false, "2048");
		// bot.addArguments(CliArgs.ALLOW, false, "norandoms");
		//
		// if (account.hasUsernameAndPasswordAndBankpin()) {
		// bot.addArguments(CliArgs.BOT, true, account.getEmail(),
		// account.getPassword(),
		// account.getBankPin());
		// } else if (account.hasUsernameAndPassword()) {
		// bot.addArguments(CliArgs.BOT, true, account.getEmail(),
		// account.getPassword(), "0000");
		// }
		// if (account.hasValidProxy()) {
		// bot.addArguments(CliArgs.PROXY, true, account.getProxyIp(),
		// account.getProxyPort(),
		// Config.PROXY_USERNAME, Config.PROXY_PASSWORD);
		// }
		// if (account.hasScript()) {
		// bot.addArguments(CliArgs.SCRIPT, true, account.getScript(),
		// account.getEmail() + "_" + account.getPassword());
		// }
		// bot.runBot();
		// }
		// try {
		// Thread.sleep(5000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// }

	}

	@FXML
	private void stopBot() {
		OsbotController bot = BotController.getBotById(table.getSelectionModel().getSelectedItem().getId());
		BotController.killProcess(bot.getPidId());
	}

	@FXML
	private void createAccounts() {
		DatabaseUtilities.seleniumCreateAccountThread();
	}

	@FXML
	private void recover() {
		DatabaseUtilities.seleniumRecoverAccount();
	}

	@FXML
	public void initialize() {
		WebdriverFunctions.killAll();

		id.setCellValueFactory(new PropertyValueFactory<AccountTable, Integer>("id"));
		script.setCellValueFactory(new PropertyValueFactory<AccountTable, String>("script"));
		username.setCellValueFactory(new PropertyValueFactory<AccountTable, String>("username"));
		scriptProgress.setCellValueFactory(new PropertyValueFactory<AccountTable, String>("accountStageProgress"));
		email.setCellValueFactory(new PropertyValueFactory<AccountTable, String>("questPoints"));
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

		// ArrayList<AccountTable> account =
		// DatabaseUtilities.getAccountsFromMysqlConnection();
		// for (AccountTable acc : account) {
		// table.getItems()
		// .add(new AccountTable(acc.getId(), acc.getScript(), acc.getUsername(),
		// acc.getWorld(),
		// acc.getProxyIp(), acc.getProxyPort(), acc.isLowCpuMode(), acc.getStatus(),
		// acc.getStage(),
		// acc.getAccountStageProgress()));
		// BotController.addBot(new OsbotController(acc.getId(), acc));
		// }

		// DatabaseUtilities.seleniumCreateAccountThread();

		// RunBot bot = new RunBot();
		//
		// bot.addArguments(CliArgs.WORLD, "318");
		// bot.runBot();
		System.out.println("Initializing bot");
		
		ThreadHandler.runThreads();

		new Thread(() -> {

			while (true) {

				ArrayList<AccountTable> account = DatabaseUtilities.getAccountsFromMysqlConnection();
				if (account.size() > 0) {
					table.getItems().clear();
					BotController.getBots().clear();

					for (AccountTable acc : account) {
						AccountTable accTable = new AccountTable(acc.getId(), acc.getScript(), acc.getUsername(), acc.getWorld(),
								acc.getProxyIp(), acc.getProxyPort(), acc.isLowCpuMode(), acc.getStatus(),
								acc.getStage(), acc.getAccountStageProgress());
						accTable.setQuestPoints(acc.getQuestPoints());
						
						table.getItems().add(accTable);
						
						BotController.addBot(new OsbotController(acc.getId(), acc));
					}
				}

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Refreshing table every 10 seconds");
			}
		}).start();

	}

	@FXML
	private void refreshTable() {

		table.getItems().clear();

		BotController.getBots().clear();
		ArrayList<AccountTable> account = DatabaseUtilities.getAccountsFromMysqlConnection();
		for (AccountTable acc : account) {
			table.getItems()
					.add(new AccountTable(acc.getId(), acc.getScript(), acc.getUsername(), acc.getWorld(),
							acc.getProxyIp(), acc.getProxyPort(), acc.isLowCpuMode(), acc.getStatus(), acc.getStage(),
							acc.getAccountStageProgress()));
			BotController.addBot(new OsbotController(acc.getId(), acc));
		}

	}

}
