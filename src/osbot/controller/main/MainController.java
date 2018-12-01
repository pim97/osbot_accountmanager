package osbot.controller.main;

import java.util.Scanner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import osbot.account.global.Config;
import osbot.account.webdriver.WebdriverFunctions;
import osbot.bot.BotController;
import osbot.controller.communicator.ContentController;
import osbot.settings.OsbotController;

public class MainController extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			BorderPane root = (BorderPane) FXMLLoader.load(this.getClass().getResource("/view/main.fxml"));
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.setTitle("OSBot account handler");
			primaryStage.show();

			primaryStage.setOnCloseRequest(e -> {
				killAll();
			});

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
		// for (String s : args) {
		// System.out.println();
		// }
		// System.out.println("Enter the amount of bots: ");
		// Scanner scanner = new Scanner(System.in);
		// int amount = Integer.parseInt(scanner.nextLine());

		Config.DATABASE_NAME = args[2];
		Config.DATABASE_USER_NAME = args[3];

		if (args[4].equalsIgnoreCase("null")) {
			Config.DATABASE_PASSWORD = "";
		} else {
			Config.DATABASE_PASSWORD = args[4];
		}
		
		Config.DATABASE_IP = args[5];

		System.out.println("DATABASE SETTIGNS: ");
		System.out.println("Database username: " + Config.DATABASE_USER_NAME);
		System.out.println("Database name: " + Config.DATABASE_NAME);
		System.out.println("Database password: " + Config.DATABASE_PASSWORD);
		System.out.println("Database IP: " + Config.DATABASE_IP);

		if (!Config.GUI) {
			Config.MAX_BOTS_OPEN = Integer.parseInt(args[1]);
			// System.out.println("Entered: " + Config.MAX_BOTS_OPEN + " bots to run");
			// scanner.close();
			ContentController.run();
		} else {
			launch(args);
		}
		// launch(args);
	}

}
