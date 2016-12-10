
package sciCon.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import sciCon.model.Controllers;
import sciCon.model.NetworkConnection;
import sciCon.model.User;
import sciCon.model.SocketEvent;

public class LoginRegisterController implements Controllers {

	@FXML
	private AnchorPane loginWindow;
	@FXML
	private AnchorPane registrationWindow;
	@FXML
	private TextField loginField;
	@FXML
	private TextField nameField;
	@FXML
	private TextField surnameField;
	@FXML
	private TextField passwordField;
	@FXML
	private TextField passwordRepeatField;

	private int uid = -1; // user ID, assigned after signing in
	private String message;
	Event sharedEvent = null; // loginBtn action event gets here after it's pressed
	// so runLater can see it and scene can be changed.

	private boolean doPasswordsMatch(String password, String rePassword) {
		if (password.equals(rePassword)) {
			return true;
		} else {
			return false;
		}
	}

	@FXML
	public void reqLogin() {
		
		String login = loginField.getText();
		String password = passwordField.getText();

		User u = new User(login, password);
		SocketEvent e = new SocketEvent("reqLogin", u);

		NetworkConnection.sendSocketEvent(e);
		SocketEvent res = NetworkConnection.rcvSocketEvent();

		String eventName = res.getName();
	
		
		if (eventName.equals("loginFailed")) {
			message = "Niepoprawny login lub has³o.";
		} else if (eventName.equals("loginSucceeded")) {
			uid = res.getObject(Integer.class);
		}

		// run in JavaFX after background thread finishes work
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (uid > -1) {
					goToApplication(sharedEvent);
				} else {
					openAlert(sharedEvent, message);
				}
			}
		});

	}

	public void reqRegister() {

		String login = loginField.getText();
		String password = passwordField.getText();
		String rePassword = passwordRepeatField.getText();
		String name = nameField.getText();
		String surname = surnameField.getText();

		if (doPasswordsMatch(password, rePassword)) {
			User u = new User(login, password, name, surname);
			SocketEvent e = new SocketEvent("reqRegister", u);

			NetworkConnection.sendSocketEvent(e);
			SocketEvent res = NetworkConnection.rcvSocketEvent();

			message = res.getObject(String.class);
		} else {
			message = "Podane has³a nie s¹ identyczne.";
		}

		// run in JavaFX after background thread finishes work
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				openAlert(sharedEvent, message);
			}
		});
	}

	@FXML
	public void registerBtn(Event event) {
		sharedEvent = event;
		java.lang.reflect.Method m = null;
		try {
			m = LoginRegisterController.class.getMethod("reqRegister");
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		runInAnotherThread(m, this);
	}

	@FXML
	private void loginBtn(Event event) { // handler
		sharedEvent = event;
		// here check if login is valid
		java.lang.reflect.Method m = null;
		try {
			m = LoginRegisterController.class.getMethod("reqLogin");
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		runInAnotherThread(m, this);
	}

	@FXML
	public void loginBtnEnterKey(KeyEvent event) {
	    if (event.getCode() == KeyCode.ENTER) {
	        loginBtn(new ActionEvent());
	    }
	}

	@FXML
	public void registerBtnEnterKey(KeyEvent event) {
	    if (event.getCode() == KeyCode.ENTER) {
	    	registerBtn(event);
	    }
	}
	
	@FXML
	private void goToRegistrationKey(KeyEvent event) {
	    if (event.getCode() == KeyCode.ENTER) {
	    	loadScene(loginWindow, "view/RegisterLayout.fxml", 320, 200, false);
	    }
	}
	
	@FXML
	public void cancelBtnEnterKey(KeyEvent event) {
	    if (event.getCode() == KeyCode.ENTER) {
	    	loadScene(registrationWindow, "view/LoginLayout.fxml", 320, 200, false);
	    }
	}
	
	@FXML
	private void goToApplication(Event event) {
		loadScene(loginWindow, "view/ApplicationLayout.fxml", 900, 600, true);
	}

	@FXML
	private void goToLogin(Event event) {
		loadScene(registrationWindow, "view/LoginLayout.fxml", 320, 200, false);
	}

	@FXML
	private void goToRegistration(Event event) {
		loadScene(loginWindow, "view/RegisterLayout.fxml", 320, 200, false);
	}
}
