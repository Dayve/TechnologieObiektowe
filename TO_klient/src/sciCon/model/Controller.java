package sciCon.model;

import java.io.IOException;
import java.util.ArrayList;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sciCon.Client;
import sciCon.controller.ConferenceManagerController;
import sciCon.controller.ConfirmationWindowController;
import sciCon.controller.DialogController;

public interface Controller {
	public enum ConferenceFilter {
		PAST, FUTURE, ONGOING, ALL
	};
	
	public enum RequestType {
		UPDATE_CONFERENCE_FEED,
		REQUEST_JOINING_CONFERENCE,
		REQUEST_LEAVING_CONFERENCE,
		REQUEST_REMOVING_CONFERENCE
	};
	
	public default String addNLsIfTooLong(String givenString, int limit) {
		String[] separateWords = givenString.split("\\s+");
		String result = new String();
		int howMuchCharsSoFar = 0;

		for (int i = 0; i < separateWords.length; ++i) {
			howMuchCharsSoFar += separateWords[i].length() + 1; // +1 because we
																// assume that
																// every word
																// has a space
																// at the end

			if (howMuchCharsSoFar > limit) {
				result += "\n";
				howMuchCharsSoFar = 0;
			}
			result += separateWords[i] + " ";
		}

		return result.substring(0, result.length() - 1);
	}
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
			scene.getStylesheets().add(Client.class.getResource("application.css").toExternalForm());
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

//	default public void openNewWindow(Stage sourceStage, String path, int minW, int minH, boolean resizable,
//			String title) {
//
//		FXMLLoader loader = new FXMLLoader();
//
//		loader.setLocation(Client.class.getResource(path));
//		Stage newStage = new Stage();
//		Scene newScene = null;
//		try {
//			newScene = new Scene(loader.load());
//			newStage.setMaxHeight(minH + 25);
//			newStage.setMaxWidth(minW);
//			newStage.initOwner(sourceStage);
//
//			newStage.setScene(newScene);
//
//			newStage.setResizable(resizable);
//			if (title != null) {
//				newStage.setTitle(title);
//			}
//			newStage.showAndWait();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	default public void openNewWindow(Parent sourceWindow, String path, int minW, int minH, boolean resizable,
			String title) {
		openNewWindow(new FXMLLoader(), sourceWindow, path, minW, minH, resizable, title);
	}
	
	default public void openNewWindow(FXMLLoader loader, Parent sourceWindow, String path, int minW, int minH, boolean resizable,
			String title) {
		Stage sourceStage = (Stage) sourceWindow.getScene().getWindow();
		loader.setLocation(Client.class.getResource(path));
		Stage newStage = new Stage();
		Scene newScene = null;
		try {
			newScene = new Scene(loader.load());
			newStage.setMaxHeight(minH + 25);
			newStage.setMaxWidth(minW);
			newStage.initOwner(sourceStage);

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
	

	default public void openNewConfManager(Parent sourceWindow, ArrayList<Conference> feed, Integer cId, String cName) {
		Stage sourceStage = (Stage) sourceWindow.getScene().getWindow();
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Client.class.getResource("view/ConferenceManagerLayout.fxml"));
		Stage newStage = new Stage();
		Scene newScene = null;
		try {
			newScene = new Scene(loader.load());
			newScene.getStylesheets().add(Client.class.getResource("application.css").toExternalForm());
			newStage.setMaxHeight(600);
			newStage.setMaxWidth(650);
			newStage.initOwner(sourceStage);
			newStage.setScene(newScene);
			newStage.setResizable(false);
			newStage.setTitle("Zarządzaj konferencją \"" + cName + "\"");
			ConferenceManagerController controller = loader.<ConferenceManagerController>getController();
			controller.setSelectedConference(cId);
			controller.updateFeed(feed);
			controller.fillUsersList();
			
			newStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	default public void openNewWindow(Parent window, String path, int minW, int minH, boolean resizable, String title) {
//		Stage sourceStage = (Stage) window.getScene().getWindow();
//
//		openNewWindow(sourceStage, path, minW, minH, resizable, title);
//	}
	
	default public void openConfirmationWindow(Parent window, String message, RequestType requestType) {
		Stage sourceStage = (Stage) window.getScene().getWindow();
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Client.class.getResource("view/ConfirmationLayout.fxml"));
		Stage newStage = new Stage();
		Scene newScene = null;
		try {
			newScene = new Scene(loader.load());
			newStage.setMaxHeight(250);
			newStage.setMaxWidth(300);
			newStage.initOwner(sourceStage);
			newStage.setScene(newScene);
			newStage.setResizable(false);
			newStage.setTitle("Powiadomienie");
			ConfirmationWindowController controller = loader.<ConfirmationWindowController>getController();
			controller.setConfirmationMessage(message);
			controller.setRequestType(requestType);
			newStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	default public void openDialogBox(Parent window, String message) {
		Stage sourceStage = (Stage) window.getScene().getWindow();
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Client.class.getResource("view/DialogBoxLayout.fxml"));
		Stage newStage = new Stage();
		Scene newScene = null;
		try {
			newScene = new Scene(loader.load());
			newStage.setMaxHeight(305);
			newStage.setMaxWidth(400);
			newStage.initOwner(sourceStage);
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

	default public void closeWindow(Parent window) {
		Stage stage = (Stage) window.getScene().getWindow();
		stage.close();
	}
}
