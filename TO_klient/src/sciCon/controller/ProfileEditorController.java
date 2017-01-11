package sciCon.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import sciCon.model.Controller;
import sciCon.model.NetworkConnection;
import sciCon.model.SocketEvent;
import sciCon.model.User;

public class ProfileEditorController implements Controller {
	@FXML private Parent profileEditorWindow;
	@FXML private Label titleField;
	@FXML private TextField nameField;
	@FXML private TextField surnameField;
	@FXML private TextField currentPasswordField;
	@FXML private TextField newPasswordField;
	@FXML private TextField emailField;
	@FXML private TextField newPasswordRepeatField;
	@FXML private TextField organizationField;
	private String login;
	private String message;
	
	private void setTextField(TextInputControl textDestination, String text){
		if(text == null) {
			System.out.println("");
			text = "";
		}
		textDestination.setText(text);
	}
	
	@FXML public void initialize() {
		ApplicationController.reqCurrentUser();
		login = ApplicationController.currentUser.getLogin();
		titleField.setText(login + ": edycja profilu");
//		setTextField(titleField, ApplicationController.currentUser.getName());
		setTextField(nameField, ApplicationController.currentUser.getName());
		setTextField(surnameField, ApplicationController.currentUser.getSurname());
		setTextField(emailField, ApplicationController.currentUser.getEmail());
		setTextField(organizationField, ApplicationController.currentUser.getOrganization());
		
		
//		nameField.setText(ApplicationController.currentUser.getName());
//		surnameField.setText(ApplicationController.currentUser.getSurname());
//		emailField.setText(ApplicationController.currentUser.getEmail());
//		organizationField.setText(ApplicationController.currentUser.getOrganization());
	}
	
	@FXML public void reqUpdateProfile() {
		String currentPassword = doHash(currentPasswordField.getText());
		String newPassword = doHash(newPasswordField.getText());
		String reNewPassword = doHash(newPasswordRepeatField.getText());
		String name = nameField.getText();
		String surname = surnameField.getText();
		String email = emailField.getText();
		String organization = organizationField.getText();

		if (newPassword.equals(reNewPassword)) {
			User u = new User(ApplicationController.currentUser.getId(), 
					login, newPassword, name, surname, email, organization);
			SocketEvent e = new SocketEvent("reqUpdateProfile", u, currentPassword);

			NetworkConnection.sendSocketEvent(e);
			SocketEvent res = NetworkConnection.rcvSocketEvent();

			message = res.getObject(String.class);
		} else {
			message = "Podane hasła nie są identyczne.";
		}

		// run in JavaFX after background thread finishes work
		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(profileEditorWindow, message);
			}
		});
	}
	
	@FXML private void confirmBtn() { // handler
		new Thread(() -> reqUpdateProfile()).start();
	}

	@FXML private void confirmBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			new Thread(() -> reqUpdateProfile()).start();
		}
	}
	
	@FXML private void closeBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			closeWindow(profileEditorWindow);
		}
	}

	@FXML public void closeWindowBtn(ActionEvent event) {
		closeWindow(profileEditorWindow);
	}
}
