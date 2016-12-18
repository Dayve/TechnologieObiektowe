package sciCon.model;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sciCon.Client;
import sciCon.controller.DialogController;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;

public interface Controller {

	default public void loadScene(Stage stage, String path, int w, int h, boolean resizable, int minW, int minH) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Client.class.getResource(path));
			Parent layout = (Parent) loader.load();
			stage.hide();
			stage.setMaximized(false);
			stage.setWidth(w);
			stage.setHeight(h + 25);
			stage.setMinWidth(minW);
			stage.setMinHeight(minH + 25);
			stage.setResizable(resizable);
			Scene scene = new Scene(layout);
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	default public void loadScene(Parent parent, String path, int w, int h, boolean resizable, int minW, int minH) {
			Stage sourceStage = (Stage) parent.getScene().getWindow();
			loadScene(sourceStage, path, w, h, resizable, minW, minH);
	}

	default public void loadScene(Event event, String path, int w, int h, boolean resizable, int minW, int minH) {
			Stage sourceStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			loadScene(sourceStage, path, w, h, resizable, minW, minH);
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

	default public void openNewWindow(Stage SourceStage, String path, int minW, int minH, boolean resizable,
			String title) {

		FXMLLoader loader = new FXMLLoader();

		loader.setLocation(Client.class.getResource(path));
		Stage newStage = new Stage();
		Scene newScene = null;
		try {
			newScene = new Scene(loader.load());
			newStage.setMaxHeight(minH + 25);
			newStage.setMaxWidth(minW);
			newStage.initOwner(SourceStage);

			newStage.setScene(newScene);

			newStage.setResizable(resizable);
			if (title != null) {
				newStage.setTitle(title);
			}
			newStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	default public void openNewWindow(Event event, String path, int minW, int minH, boolean resizable,
			String title) {
		Stage SourceStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

		openNewWindow(SourceStage, path, minW, minH, resizable, title);
	}
	
	

	default public void openDialogBox(Event event, String message) {
		Stage SourceStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Client.class.getResource("view/DialogBoxLayout.fxml"));
		Stage newStage = new Stage();
		Scene newScene = null;
		try {
			newScene = new Scene(loader.load());
			newStage.setMaxHeight(305);
			newStage.setMaxWidth(400);
			newStage.initOwner(SourceStage);
			newStage.setScene(newScene);
			newStage.setResizable(false);
			newStage.setTitle("Powiadomienie");
			DialogController controller = loader.<DialogController>getController();
			controller.setDialogMessage(message);
			newStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	default public void closeWindow(Event event) {
		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		stage.close();
	}
}
