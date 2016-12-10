package sciCon.model;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sciCon.Client;
import sciCon.controller.AlertController;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;

public interface Controllers {

	default public void loadScene(Stage stage, String path, int minW, int minH, boolean resizable) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Client.class.getResource(path));
			Parent layout = (Parent) loader.load();
			stage.hide();
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
			sourceStage.hide();
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

	default public void loadScene(Event event, String path, int minW, int minH, boolean resizable) {
		try {
			Stage sourceStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			FXMLLoader loader = new FXMLLoader();
			Parent layout = (Parent) loader.load();
			sourceStage.hide();
			Scene scene = new Scene(layout);
			sourceStage.setMinWidth(minW);
			sourceStage.setMinHeight(minH);
			sourceStage.setResizable(resizable);
			loader.setLocation(Client.class.getResource(path));
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

	default public void openNewWindow(Event event, String path, int minW, int minH, boolean resizable,
			String title) {
		Stage SourceStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

		FXMLLoader loader = new FXMLLoader();

		loader.setLocation(Client.class.getResource(path));
		Stage newStage = new Stage();
		Scene newScene = null;
		try {
			newScene = new Scene(loader.load());
			newStage.setMaxHeight(minH);
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

	default public void openAlert(Event event, String message) {
		Stage SourceStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Client.class.getResource("view/AlertLayout.fxml"));
		Stage newStage = new Stage();
		Scene newScene = null;
		try {
			newScene = new Scene(loader.load());
			newStage.setMaxHeight(320);
			newStage.setMaxWidth(400);
			newStage.initOwner(SourceStage);
			newStage.setScene(newScene);
			newStage.setResizable(false);
			newStage.setTitle("Powiadomienie");
			AlertController controller = loader.<AlertController>getController();
			controller.setAlertText(message);
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
