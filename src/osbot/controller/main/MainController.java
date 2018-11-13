package osbot.controller.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import osbot.account.global.Config;
import osbot.account.webdriver.WebdriverFunctions;
import osbot.bot.BotController;
import osbot.settings.OsbotController;

public class MainController extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			BorderPane root = (BorderPane) FXMLLoader.load(this.getClass().getResource("/view/main.fxml"));
			Scene scene = new Scene(root);
			// scene.getStylesheets().add(this.getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.setTitle("OSBot account handler");
			// if (!Config.CREATE_AND_VERIFY) {
			primaryStage.show();
			// }

			primaryStage.setOnCloseRequest(e -> {
				killAll();
			});

			// popup window
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void killAll() {
		for (OsbotController bot : BotController.getBots()) {
			BotController.killProcess(bot.getPidId());
		}
		WebdriverFunctions.killAll();
		// Platform.exit();
		System.exit(1);
		System.out.println("Screen is closing, killing all left overs");
		// Save file
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
