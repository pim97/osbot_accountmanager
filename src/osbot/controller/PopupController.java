package osbot.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import osbot.account.AccountStatus;
import osbot.bot.BotController;
import osbot.controller.communicator.ContentController;
import osbot.database.DatabaseUtilities;
import osbot.settings.OsbotController;
import osbot.tables.AccountTable;

public class PopupController {

	@FXML
	private TextField username;

	@FXML
	private TextField password;

	@FXML
	private TextField bankpin;

	@FXML
	private TextField proxyIp;

	@FXML
	private TextField proxyPort;

	@FXML
	private TextField proxyUsername;

	@FXML
	private TextField proxyPassword;

	@FXML
	private TextField worldNumber;

	@FXML
	private Button addButton;

	public void start(Stage primaryStage) throws Exception {
		try {
			VBox root = (VBox) FXMLLoader.load(this.getClass().getResource("/view/popup/popup.fxml"));
			Scene scene = new Scene(root);
			// Stage stage = new Stage();
			// scene.getStylesheets().add(this.getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.setTitle("Popup");
			primaryStage.initModality(Modality.WINDOW_MODAL);
			primaryStage.showAndWait();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void addToTable() {
		String user = username.getText();
		String pass = password.getText();
		String bank = bankpin.getText();
		String proxIp = proxyIp.getText();
		String proxPort = proxyPort.getText();
		String worldNum = worldNumber.getText();
		boolean lowCpuMode = false;

		if (user != null && pass != null && worldNum != null) {
			try {
				int id = ContentController.dataAccountTable.size() + 1;
				AccountTable acc = new AccountTable(id, null, user, Integer.parseInt(worldNumber.getText()), proxIp,
						proxPort, lowCpuMode, AccountStatus.AVAILABLE);
				acc.setBankPin(bank);
				acc.setPassword(pass);
				OsbotController botController = new OsbotController();
				botController.setId(acc.getId());
				BotController.addBot(botController);

				DatabaseUtilities.insertIntoTable(acc);
				ContentController.dataAccountTable.add(acc);
			} catch (Exception e) {
				WarningController error = new WarningController();
				try {
					error.start(new Stage());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}

	}

}
