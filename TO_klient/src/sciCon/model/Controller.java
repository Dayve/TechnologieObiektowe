package sciCon.model;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sciCon.Client;
import sciCon.controller.DialogController;

public interface Controller {
	public enum ConferenceFilter {
		PAST, FUTURE, ONGOING, ALL
	};

	default public String addNLsIfTooLong(String givenString, int limit) {
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

		return result;
	}

	default public ArrayList<Conference> filterFeed(ArrayList<Conference> feed, ConferenceFilter cf) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime now = LocalDateTime.now();
		now.format(formatter);
		ArrayList<Conference> filtered = new ArrayList<Conference>();
		filtered.addAll(feed);
		switch (cf) {
		case PAST: {
			filtered.removeIf(s -> s.getStartTime().isAfter(now));
			break;
		}
		case FUTURE: {
			filtered.removeIf(s -> s.getEndTime().isBefore(now));
			break;
		}
		case ONGOING: {
			filtered.removeIf(s -> s.getStartTime().isBefore(now) && s.getEndTime().isAfter(now));
			break;
		}
		case ALL: {
			break;
		}

		default:
			break;
		}
		return filtered;
	}

	default public void fillListWithLabels(ListView<Label> lv, ArrayList<Conference> cs, ConferenceFilter cf, int charLimit) {
		ArrayList<Conference> filtered = filterFeed(cs, cf);
		ObservableList<Label> ol = FXCollections.observableArrayList();
		lv.getItems().clear();
		Label label = null;
		for (Conference c : filtered) {
			TextArea feed = new TextArea(c.toString());
			feed.setWrapText(true);
			feed.setEditable(false);
			feed.setMouseTransparent(true);
			feed.setFocusTraversable(false);
			label = new Label(addNLsIfTooLong(c.getName(), charLimit));
			ol.add(label);
		}
		lv.setItems(ol);
		AnchorPane.setTopAnchor((Node) lv, 0.0);
		AnchorPane.setBottomAnchor((Node) lv, 0.0);
		AnchorPane.setLeftAnchor((Node) lv, 0.0);
		AnchorPane.setRightAnchor((Node) lv, 0.0);
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
			// scene.getStylesheets().add(Client.class.getResource("application.css").toExternalForm());
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

	default public void openNewWindow(Event event, String path, int minW, int minH, boolean resizable, String title) {
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
