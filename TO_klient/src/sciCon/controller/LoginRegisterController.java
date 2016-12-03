package sciCon.controller;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sciCon.Controllers;
import sciCon.dbInterface;
import sciCon.model.NetworkConnection;
import sciCon.model.User;
import sciCon.model.SocketEvent;

public class LoginRegisterController implements Controllers, dbInterface {

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
	@FXML
	private Label controlLabel;
	Service<Void> backgroundThread = null;

	@FXML
	
	public void reqLogin() {
		String login = loginField.getText();
		String password = passwordField.getText();

		User u = new User(login, password);
		SocketEvent e = new SocketEvent("reqLogin", u);

		System.out.print("Wysy³am: ");
		System.out.println(u);

		NetworkConnection.sendSocketEvent(e);

		System.out.println("Wys³em");
		SocketEvent res = NetworkConnection.rcvSocketEvent();
		System.out.print("dostano: ");
		User resData = res.getObject(User.class);
		System.out.println(resData);
	}
	
	public void reqRegister() {
		
		String login = loginField.getText();
		String password = passwordField.getText();
		String rePassword = passwordRepeatField.getText();
		String name = nameField.getText();
		String surname = surnameField.getText();
		String message = "";

		User u = new User(login, password, name, surname);
		SocketEvent e = new SocketEvent("reqRegister", u);

		System.out.print("Wysy³am: ");
		System.out.println(u);
		
		NetworkConnection.sendSocketEvent(e);

		System.out.println("Wys³em");
		SocketEvent res = NetworkConnection.rcvSocketEvent();
		System.out.print("dostano: ");
		User resData = res.getObject(User.class);
		System.out.println(resData);
	}
	
	@FXML
	public void registerBtn() {

		java.lang.reflect.Method m = null;
		try {
			m = LoginRegisterController.class.getMethod("reqRegister");
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		runInAnotherThread(m, this);
	}
	// backgroundThread = new Service<Void>() {
	// @Override
	// protected Task<Void> createTask() {
	// return new Task<Void>() {
	// @Override
	// protected Void call() throws Exception {
	//// NetworkConnection.sendSocketEvent(u);
	// return null;
	// }
	// };
	// }
	// };
	//
	// backgroundThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
	// public void handle(WorkerStateEvent event) {
	// System.out.println("done!");
	// }
	// });
	// }
	//
	// controlLabel.setText(message);
	// backgroundThread.restart();
	// }

	@FXML
	private void loginBtn(ActionEvent event) { // handler

		// here check if login is valid
		java.lang.reflect.Method m = null;
		try {
			m = LoginRegisterController.class.getMethod("reqLogin");
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		runInAnotherThread(m, this);
	}

	@FXML
	private void goToApplication(ActionEvent event) {
		Stage sourceStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		loadScene(sourceStage, "view/LoginLayout.fxml", 320, 200, false);
	}

	@FXML
	private void goToLogin(ActionEvent event) {
		loadScene(event, "view/LoginLayout.fxml", 320, 200, false);
	}

	@FXML
	private void goToRegistration(ActionEvent event) {
		loadScene(event, "view/RegisterLayout.fxml", 320, 200, false);
	}
}
