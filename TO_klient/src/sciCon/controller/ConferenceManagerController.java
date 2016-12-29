package sciCon.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import sciCon.model.Conference;
import sciCon.model.Controller;
import sciCon.model.User;

public class ConferenceManagerController implements Controller {

	@FXML
	Parent confManagerWindow;
	@FXML
	private TextField searchUserField;
	@FXML
	private TextField searchFileField;
	@FXML
	private ListView<Label> usersLV;
	@FXML
	private ListView filesLV;
	private ArrayList<Conference> feed;
	private Integer selectedConferenceId;
	private Conference selectedConference;
	private HashMap<Integer, User> selectedUsers = new HashMap<Integer, User>();

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
	
	private void fillUserListWithRoles(ObservableList<Label> ol, ArrayList<User> group, String role) {
		
		Label label = null;

		for (User u : group) {
			String title = u.getLogin() + " (" + u.getName() + " " + u.getSurname() + "), " + role;
			label = new Label(addNLsIfTooLong(title, 35));
			label.setFont(Font.font("Inconsolata", 13));

			label.setId(u.getId().toString());
			label.setPrefWidth(usersLV.getPrefWidth());
			label.setOnMouseClicked(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent t) {
					Label selectedLabel = ((Label) usersLV.getSelectionModel().getSelectedItem());
					Integer selectedUsersId = Integer.parseInt(selectedLabel.getId());
					if (selectedUsers.containsKey(selectedUsersId)) {
						selectedUsers.remove(selectedUsersId);
						selectedLabel.setStyle("-fx-font-weight: normal;");
					} else {
						selectedUsers.put(selectedUsersId, u);
						selectedLabel.setStyle("-fx-font-weight: bold;");
					}
				}
			});
			ol.add(label);
		}
	}
	
	public void fillUsersList() {
		if(selectedConferenceId != null) {
			selectedConference = feed.stream().filter(c -> c.getId() == selectedConferenceId).findFirst().get();
			ObservableList<Label> ol = FXCollections.observableArrayList();
			usersLV.getItems().clear();
			ArrayList<User> selectedConferencesUsersGroup = selectedConference.getOrganizers();
			fillUserListWithRoles(ol, selectedConferencesUsersGroup, "organizator");
			selectedConferencesUsersGroup = selectedConference.getSponsors();
			fillUserListWithRoles(ol, selectedConferencesUsersGroup, "sponsor");
			selectedConferencesUsersGroup = selectedConference.getPrelectors();
			fillUserListWithRoles(ol, selectedConferencesUsersGroup, "prelegent");
			selectedConferencesUsersGroup = selectedConference.getParticipants();
			fillUserListWithRoles(ol, selectedConferencesUsersGroup, "uczestnik");
			selectedConferencesUsersGroup = selectedConference.getPending();
			fillUserListWithRoles(ol, selectedConferencesUsersGroup, "niepotwierdzony");
			usersLV.setItems(ol);
		}
		
	}

	@FXML
	public void reqModifyUsersRole() {

	}

	@FXML
	public void addFileBtn() {

	}

	@FXML
	public void closeWindowBtn(ActionEvent event) {
		closeWindow(confManagerWindow);
	}

	@FXML
	private void closeBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			closeWindow(confManagerWindow);
		}
	}
}
