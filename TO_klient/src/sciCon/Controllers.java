package sciCon;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public interface Controllers {
	default public void loadScene(Stage stage, String path, int w, int h, boolean resizable) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Client.class.getResource(path));
			Parent layout = (Parent) loader.load();
			Scene scene = new Scene(layout);
			stage.setResizable(resizable);
			stage.setScene(scene);
			stage.setMinWidth(w);
			stage.setMinHeight(h);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
