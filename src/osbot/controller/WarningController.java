package osbot.controller;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import osbot.controller.communicator.ContentController;

public class WarningController {
	
	private ContentController contentController;
	
	@FXML public Text warningText;
	
	@FXML private Button closeButton;
	
	public void start(Stage primaryStage) throws Exception {
		try {
			VBox root = (VBox) FXMLLoader.load(this.getClass().getResource("/view/warning/warning.fxml"));
			Scene scene = new Scene(root);
			// scene.getStylesheets().add(this.getClass().getResource("application.css").toExternalForm());
			// Create a controller instance
	        
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.setTitle("Warning");
			primaryStage.initModality(Modality.WINDOW_MODAL);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	private void initialize() {
		
	}
	
	@FXML
	private void closeWindow() {
		Stage stage = (Stage)closeButton.getScene().getWindow();
		stage.close();
	}

	public void init(ContentController contentController) {
		this.contentController = contentController;
	}
	
}
