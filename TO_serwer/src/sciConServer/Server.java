package sciConServer;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Server extends Application {
	
	private Stage primaryStage;
//	private Parent layout;
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("sciCon server");
        loadServerView();
//		loadScene(primaryStage, "view/serverLayout.fxml", 320, 200, false);
	}
	
	private void loadServerView() {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Server.class.getResource("serverLayout.fxml"));
		try {
			Parent layout;
			layout = (Parent) loader.load();
			Scene scene = new Scene(layout);
			primaryStage.setResizable(false);
			primaryStage.setScene(scene);
			primaryStage.setMinWidth(320);
			primaryStage.setMinHeight(200);
			primaryStage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
