package sciCon.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import sciCon.model.Conference;
import sciCon.model.Controller;
import sciCon.model.NetworkConnection;
import sciCon.model.SocketEvent;
import sciCon.model.User;
import sciCon.model.User.UsersRole;


public class ConferenceManagerController implements Controller {

	@FXML Parent confManagerWindow;
	@FXML private TextField searchUserField;
	@FXML private TextField searchFileField;
	@FXML private ListView<Label> usersLV;
	@FXML private ListView filesLV;

	@FXML private ComboBox<String> userOperationCB;
	@FXML private ComboBox<String> fileOperationCB;

	private ArrayList<Conference> feed;
	private Integer selectedConferenceId;
	private Conference selectedConference;
	private HashMap<Integer, User> selectedUsers = new HashMap<Integer, User>();
	private HashMap<Integer, User> deselectedUsers = new HashMap<Integer, User>();

	private String message;

	public void updateFeed(ArrayList<Conference> feed) {
		this.feed = feed;
	}

	public void setSelectedConference(Integer confId) {
		selectedConferenceId = confId;
	}

	public void refresh(ArrayList<Conference> feed) {
		updateFeed(feed);
		fillUsersList();
	}

	private void setupFilterCBs() {
		ObservableList<String> userActions = FXCollections.observableArrayList("Ustaw status: organizator",
				"Ustaw status: sponsor", "Ustaw status: prelegent", "Ustaw status: uczestnik", "Wyproś");

		userOperationCB.getItems().addAll(userActions);

		ObservableList<String> fileActions = FXCollections.observableArrayList("Usuń pliki");

		fileOperationCB.getItems().addAll(fileActions);
	}

	private void filterListView(ListView<Label> lv, String text) {

	}

	private void addUserLabelsWithRoles(ObservableList<Label> ol, ArrayList<User> group, String role) {

		Label label = null;

		for (User u : group) {
			String title = u.getLogin() + " (" + u.getName() + " " + u.getSurname() + "), " + role;
			label = new Label(addNLsIfTooLong(title, 35));

			label.setId(u.getId().toString());
			label.setPrefWidth(usersLV.getPrefWidth());
			deselectedUsers.put(u.getId(), u);
			label.setOnMouseClicked(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent t) {
					Label selectedLabel = ((Label) usersLV.getSelectionModel().getSelectedItem());
					Integer selectedUsersId = Integer.parseInt(selectedLabel.getId());
					if (selectedUsers.containsKey(selectedUsersId)) {
						selectedUsers.remove(selectedUsersId);
						deselectedUsers.put(selectedUsersId, u);
						selectedLabel.setStyle("-fx-font-weight: normal;");
					} else {
						deselectedUsers.remove(selectedUsersId);
						selectedUsers.put(selectedUsersId, u);
						selectedLabel.setStyle("-fx-font-weight: bold;");
					}
				}
			});
			ol.add(label);
		}
	}

	public void fillUsersList() {
		System.out.println("fillList poszedł");
		System.out.println(selectedConferenceId);
		if (selectedConferenceId != null) {
			selectedConference = feed.stream().filter(c -> c.getId() == selectedConferenceId).findFirst().get();
			System.out.println(selectedConference);
			ObservableList<Label> ol = FXCollections.observableArrayList();
			usersLV.getItems().clear();
			ArrayList<ArrayList<User>> selectedConferencesUsersGroups = new ArrayList<ArrayList<User>>();
			selectedConferencesUsersGroups.add(selectedConference.getOrganizers());
			selectedConferencesUsersGroups.add(selectedConference.getSponsors());
			selectedConferencesUsersGroups.add(selectedConference.getPrelectors());
			selectedConferencesUsersGroups.add(selectedConference.getParticipants());
			selectedConferencesUsersGroups.add(selectedConference.getPending());
			String[] roles = { "organizator", "sponsor", "prelegent", "uczestnik", "oczekujący" };
			for (int i = 0; i < selectedConferencesUsersGroups.size(); i++) {
				addUserLabelsWithRoles(ol, selectedConferencesUsersGroups.get(i), roles[i]);
			}
			usersLV.setItems(ol);
		}
	}

	@FXML public void initialize() {
		searchUserField.textProperty().addListener(obs -> {
			filterListView(usersLV, searchUserField.getText());
		});

		searchFileField.textProperty().addListener(obs -> {
			filterListView(filesLV, searchFileField.getText());
		});

		setupFilterCBs();
	}

	@FXML public void deselectSelectAllUsers() {
		if (selectedUsers.isEmpty()) {
			for (Label l : usersLV.getItems()) {
				l.setStyle("-fx-font-weight: bold;");
			}
			selectedUsers.putAll(deselectedUsers);
			deselectedUsers.clear();
		} else {
			for (Label l : usersLV.getItems()) {
				l.setStyle("-fx-font-weight: normal;");
			}
			deselectedUsers.putAll(selectedUsers);
			selectedUsers.clear();
		}
	}

	@FXML public void deselectSelectAllFiles() {

	}

	@FXML public void confirmUserOperation() {
		String operation = userOperationCB.getValue();
		String action = "reqSetRole";
		ArrayList<Integer> usersIds = new ArrayList<Integer>(selectedUsers.keySet());
		
		User.UsersRole role = null;
		
		switch (operation) {
			case "Ustaw status: organizator": {
				role = UsersRole.ORGANIZER;
				break;
			}
			case "Ustaw status: sponsor": {
				role = UsersRole.SPONSOR;
				break;
			}
			case "Ustaw status: prelegent": {
				role = UsersRole.PRELECTOR;
				break;
			}
			case "Ustaw status: uczestnik": {
				role = UsersRole.PARTICIPANT;
				break;
			}
			case "Wyproś": {
				action = "reqExpellUsers";
				break;
			}
			default:
				break;
		}
		
		if(role != null && usersIds.size() > 0) {
			SocketEvent se = new SocketEvent(action, role, selectedConferenceId, usersIds);
			NetworkConnection.sendSocketEvent(se);

			SocketEvent res = NetworkConnection.rcvSocketEvent();
			String eventName = res.getName();

			if (eventName.equals("setRoleSucceeded")) {
				message = "Pomyślnie wprowadzono zmiany.";
				ApplicationController.makeRequest(RequestType.UPDATE_CONFERENCE_FEED);
			} else if (eventName.equals("setRoleFailed")) {
				message = "Nie udało się wprowadzić zmian.";
			} else {
				message = "Nie udało się wprowadzić zmian. Serwer nie odpowiada.";
			}
		} else {
			message = "Zaznacz conajmniej jednego użytkownika i akcję do wykonania.";
		}
		
		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(confManagerWindow, message);
			}
		});
	}

	@FXML public void confirmFileOperation() {

	}

	@FXML public void reqModifyUsersRole() {

	}

	@FXML public void addFileBtn() {

	}

	@FXML public void closeWindowBtn(ActionEvent event) {
		closeWindow(confManagerWindow);
	}

	@FXML private void closeBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			closeWindow(confManagerWindow);
		}
	}
}
