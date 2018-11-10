package osbot.controller.communicator;

import java.util.ArrayList;
import java.util.Iterator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import osbot.account.AccountStatus;
import osbot.account.global.Config;
import osbot.account.handler.BotHandler;
import osbot.account.webdriver.WebdriverFunctions;
import osbot.bot.BotController;
import osbot.controller.PopupController;
import osbot.database.DatabaseUtilities;
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
	TableColumn<AccountTable, String> breaktill;

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
	TableColumn<AccountTable, Integer> accValue;

	@FXML
	TableColumn<AccountTable, String> world;

	@FXML
	TableColumn<AccountTable, String> proxyPort;

	@FXML
	TableColumn<AccountTable, Boolean> lowCpuMode;

	@FXML
	TableColumn<AccountTable, AccountStatus> status;

	public static ObservableList<AccountTable> dataAccountTable = FXCollections.observableArrayList();

	@FXML
	private TextField numberOfBotsSetting;

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
	private void setAccounts() {
		try {
			Config.MAX_BOTS_OPEN = Integer.parseInt(numberOfBotsSetting.getText());
			System.out.println("Set max bots open to: "+Config.MAX_BOTS_OPEN);
		} catch (Exception e) {
			System.out.println("Must be a number");
		}
	}

	@FXML
	private void deleteAccounts() {

		for (AccountTable account : table.getSelectionModel().getSelectedItems()) {
			int id = account.getId();// table.getSelectionModel().getSelectedItem().getId();
			DatabaseUtilities.deleteFromTable(id);
		}
		table.getItems().removeAll(table.getSelectionModel().getSelectedItems());
	}

	/**
	 * Will kill all the currently running bots in an instant
	 */
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
		// Starting one specific bot
		BotHandler.runBot(bot);
	}

	@FXML
	private ToggleButton toggleButton;

	@FXML
	private void toggleBot() {
		// Starting the bots
		BotHandler.handleBots();
	}

	@FXML
	private void stopBot() {
		OsbotController bot = BotController.getBotById(table.getSelectionModel().getSelectedItem().getId());
		BotController.killProcess(bot.getPidId());
	}

	@FXML
	private void createAccounts() {
		// Starts another thread on creating accounts
		DatabaseUtilities.seleniumCreateAccountThread();
	}

	@FXML
	private void recover() {
		// Starts another thread on recovering accounts
		DatabaseUtilities.seleniumRecoverAccount();
	}

	@FXML
	public void initialize() {

		// Killing all webdrivers on start
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
		accValue.setCellValueFactory(new PropertyValueFactory<AccountTable, Integer>("accountValue"));
		breaktill.setCellValueFactory(new PropertyValueFactory<AccountTable, String>("dateString"));

		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		table.setItems(dataAccountTable);

		System.out.println("Initializing bot");

		// Running the main thread
		ThreadHandler.runThreads();

		// Running another thread in this class, because stuck on table
		new Thread(() -> {

			while (true) {

				// Gets all the account information from a MySQL connection
				ArrayList<AccountTable> account = DatabaseUtilities.getAccountsFromMysqlConnection();

				// Will get the username stored that's currently selected, this is so when the
				// table refreshes with new MySQL data, the selected item will stay
				String selectedItem = null;
				if (table.getSelectionModel().getSelectedItem() != null) {
					selectedItem = table.getSelectionModel().getSelectedItem().getUsername();
				}

				if (account.size() > 0) {
					// Clears the table
					table.getItems().clear();
					int botListSize = BotController.getBots().size();

					for (AccountTable acc : account) {
						AccountTable accTable = new AccountTable(acc.getId(), acc.getScript(), acc.getUsername(),
								acc.getWorld(), acc.getProxyIp(), acc.getProxyPort(), acc.isLowCpuMode(),
								acc.getStatus(), acc.getStage(), acc.getAccountStageProgress());
						accTable.setQuestPoints(acc.getQuestPoints());
						accTable.setPassword(acc.getPassword());
						accTable.setBankPin(acc.getBankPin());
						accTable.setDay(acc.getDay());
						accTable.setMonth(acc.getMonth());
						accTable.setYear(acc.getYear());
						accTable.setEmail(acc.getEmail());
						accTable.setAccountValue(acc.getAccountValue());
						accTable.setDate(acc.getDate());
						accTable.setDateString(acc.getDateString());
						accTable.setTradeWithOther(acc.getTradeWithOther());
						accTable.setProxyUsername(acc.getProxyUsername());
						accTable.setProxyPassword(acc.getProxyPassword());

						// Adds the account to the table
						if (table != null && table.getItems() != null && accTable != null) {
							table.getItems().add(accTable);
						}

						if (botListSize == 0) {
							BotController.addBot(new OsbotController(acc.getId(), acc));
						} else {
							OsbotController bot = BotController.getBotById(acc.getId());
							if (bot != null) {
								bot.setAccount(accTable);
							} else if (bot == null) {
								BotController.addBot(new OsbotController(acc.getId(), acc));
							}
						}
					}

					ArrayList<AccountTable> toBeDeleted = containsInBoth(BotController.getBots(), account);

					for (AccountTable del : toBeDeleted) {
						removeFromList(del.getUsername());
						System.out.println(del.getUsername()
								+ " deleted from the botcontroller, didn't exist as available anymore");
					}

				}

				// Will find the same index as before that was selected
				int index = -1;
				for (int i = 0; i < table.getItems().size(); i++) {
					AccountTable acc = table.getItems().get(i);
					if (acc.getUsername().equalsIgnoreCase(selectedItem)) {
						index = i;
					}
				}

				// Refreshes the table
				table.refresh();

				// Setting the selection model on the found index

				// try {
				// if (index > -1 && table != null && table.getSelectionModel() != null) {
				// table.getSelectionModel().select(index);
				// }
				// } catch (Exception e) {
				// //If goes wrong, then clear the selection
				// table.getSelectionModel().clearSelection();
				// e.printStackTrace();
				// }

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("[REFRESHING TABLE] still active [10 sec] per loop");
			}
		}).start();

		DatabaseUtilities.checkPidsProcessesEveryMinutes();

		// if (Config.CREATING_ACCOUNTS_THREAD_ACTIVE) {
		// DatabaseUtilities.seleniumCreateAccountThread();
		// }
		// if (Config.RECOVERING_ACCOUNTS_THREAD_ACTIVE) {
		// DatabaseUtilities.seleniumRecoverAccount();
		// }
	}

	/**
	 * Will look if two arraylists contains the same values. The one with the most
	 * values to check on must be first in the paramaters
	 * 
	 * @param one
	 * @param two
	 * @return
	 */
	private ArrayList<AccountTable> containsInBoth(ArrayList<OsbotController> one, ArrayList<AccountTable> two) {
		ArrayList<AccountTable> notFoundInBoth = new ArrayList<AccountTable>();

		for (OsbotController a : one) {
			boolean found = false;
			for (AccountTable b : two) {
				if (a.getAccount().getUsername().equalsIgnoreCase(b.getUsername())) {
					found = true;
				}
			}
			if (!found) {
				notFoundInBoth.add(a.getAccount());
			}
		}
		return notFoundInBoth;
	}

	/**
	 * Removes an account name from the BotControllers list
	 * 
	 * @param accountName
	 */
	private void removeFromList(String accountName) {
		Iterator<OsbotController> bot = BotController.getBots().iterator();

		while (bot.hasNext()) {
			OsbotController b = bot.next();

			if (b.getAccount().getUsername().equalsIgnoreCase(accountName)) {
				bot.remove();
			}
		}
	}

	/**
	 * The name that's in the list
	 * 
	 * @param accountName
	 * @return
	 */
	private boolean accountContainsInList(String accountName) {
		boolean found = false;
		for (AccountTable acc : DatabaseUtilities.getAccountsFromMysqlConnection()) {
			if (acc.getUsername().equalsIgnoreCase(accountName)) {
				found = true;
			}
		}
		return found;
	}

	/**
	 * This feature has been removed, now the table is automatically refreshing
	 * without clearing the BotController list to prevent deleting PID's
	 */
	@FXML
	private void refreshTable() {

		// Button not working, refreshes automatically

	}

}
