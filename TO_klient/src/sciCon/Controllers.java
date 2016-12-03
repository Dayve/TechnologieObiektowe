package sciCon;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;

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
	
	default public void loadScene(ActionEvent event, String path, int w, int h, boolean resizable) {
		try {
			Stage sourceStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Client.class.getResource(path));
			Parent layout = (Parent) loader.load();
			Scene scene = new Scene(layout);
			sourceStage.setResizable(resizable);
			sourceStage.setScene(scene);
			sourceStage.setMinWidth(w);
			sourceStage.setMinHeight(h);
			sourceStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	default public void runInAnotherThread(java.lang.reflect.Method method, Object destinationObject, Object... args) {
		Worker<Void> worker = null;
		
		worker = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				try {
					method.invoke(destinationObject, args);
				} catch (IllegalArgumentException e) {}
				  catch (IllegalAccessException e) {}
				  catch (InvocationTargetException e) {}
				
				return null;
			}
		};
		
		new Thread((Runnable) worker).start();
	}
}
