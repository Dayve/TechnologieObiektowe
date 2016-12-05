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

	default public void loadScene(Stage stage, String path, int minW, int minH, boolean resizable) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Client.class.getResource(path));
			Parent layout = (Parent) loader.load();
			stage.setMinWidth(minW);
			stage.setMinHeight(minH);
			stage.setResizable(resizable);
			Scene scene = new Scene(layout);
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	default public void loadScene(Parent parent, String path, int minW, int minH, boolean resizable) {
		try {
			Stage sourceStage = (Stage) parent.getScene().getWindow();
			sourceStage.setMinWidth(minW);
			sourceStage.setMinHeight(minH);
			sourceStage.setResizable(resizable);
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Client.class.getResource(path));
			Parent layout = (Parent) loader.load();
			Scene scene = new Scene(layout);

			sourceStage.setScene(scene);
			sourceStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	default public void loadScene(ActionEvent event, String path, int minW, int minH, boolean resizable) {
		try {
			Stage sourceStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			sourceStage.setMinWidth(minW);
			sourceStage.setMinHeight(minH);
			sourceStage.setResizable(resizable);
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Client.class.getResource(path));
			Parent layout = (Parent) loader.load();
			Scene scene = new Scene(layout);

			sourceStage.setScene(scene);
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
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				} catch (InvocationTargetException e) {
				}

				return null;
			}
		};

		new Thread((Runnable) worker).start();
	}

	default public void openNewWindow(ActionEvent event, String path, int minW, int minH, boolean resizable) {
		Stage SourceStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

		FXMLLoader loader = new FXMLLoader();
		
		loader.setLocation(Client.class.getResource(path));
		Stage newStage = new Stage();
		Scene newScene = null;
		try {
			System.out.println("before get resource");
			newScene = new Scene(loader.load());
			System.out.println("after get resource");
			newStage.setMaxHeight(minH);
			newStage.setMaxWidth(minW);
			newStage.initOwner(SourceStage);
			
			newStage.setScene(newScene);
		
			newStage.setResizable(resizable);
			newStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	default public void closeWindow(ActionEvent event) {
		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		stage.close();
	}
}
