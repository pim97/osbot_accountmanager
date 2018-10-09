
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import osbot.tables.AccountTable;

public class Main extends Application implements Initializable {

	/**
	 * Table start
	 */
	@FXML
	TableView<AccountTable> table;

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

	public ObservableList<AccountTable> dataAccountTable = FXCollections.observableArrayList();
	
	/**
	 * Table end
	 */

	@FXML
	private Button button;

	/**
	 * Opens the start window
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = (BorderPane) FXMLLoader.load(this.getClass().getResource("main.fxml"));
			Scene scene = new Scene(root);
			// scene.getStylesheets().add(this.getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.setTitle("OSBot account handler");
			primaryStage.show();

			// popup window
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@FXML
	private void openWindow() {
		OpenWindow window = new OpenWindow();
		window.openWindow("popup.fxml");
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		id.setCellValueFactory(new PropertyValueFactory<AccountTable, Integer>("id"));
		script.setCellValueFactory(new PropertyValueFactory<AccountTable, String>("script"));
		username.setCellValueFactory(new PropertyValueFactory<AccountTable, String>("username"));
		world.setCellValueFactory(new PropertyValueFactory<AccountTable, String>("world"));
		proxyIp.setCellValueFactory(new PropertyValueFactory<AccountTable, String>("proxyIp"));
		proxyPort.setCellValueFactory(new PropertyValueFactory<AccountTable, String>("proxyPort"));
		lowCpuMode.setCellValueFactory(new PropertyValueFactory<AccountTable, Boolean>("lowCpuMode"));

		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		table.setItems(dataAccountTable);
		
		for (int i = 0; i < 100; i++) {
			table.getItems().add(new AccountTable("1", "1", "1", "1", "1", false, 0));
		}

//		RunBot bot = new RunBot();
//
//		bot.addArguments(CliArgs.WORLD, "318");
//		bot.runBot();
		System.out.println("Initializing bot");
	}

}
