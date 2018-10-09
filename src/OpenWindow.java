import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class OpenWindow {

	/**
	 * Opens a new popup window
	 * @param text
	 */
	public void openWindow(String text) {
		try {
			VBox root = (VBox) FXMLLoader.load(this.getClass().getResource(text));
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			// scene.getStylesheets().add(this.getClass().getResource("application.css").toExternalForm());
			stage.setScene(scene);
			stage.setResizable(false);
			stage.setTitle("Popup");
			stage.initModality(Modality.WINDOW_MODAL);
			stage.showAndWait();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
