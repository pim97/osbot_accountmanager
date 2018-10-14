package osbot.controller.main;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import osbot.controller.communicator.ContentController;

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
			primaryStage.show();

			// popup window
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}
	
}
