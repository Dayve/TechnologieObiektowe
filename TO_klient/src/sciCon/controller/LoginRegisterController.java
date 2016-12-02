package sciCon.controller;

import java.util.ArrayList;

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
import sciCon.model.Post;
import sciCon.model.User;
import sciCon.model.SocketEvent;

public class LoginRegisterController implements Controllers, dbInterface {
	
	@FXML private TextField loginField;
	@FXML private TextField nameField;
	@FXML private TextField surnameField;
	@FXML private TextField passwordField;
	@FXML private TextField passwordRepeatField;
	@FXML private Label controlLabel;
	Service<Void> backgroundThread = null;
	
	@FXML
	private void register() {
		
		String login = loginField.getText();
		String password = passwordField.getText();
		String rePassword = passwordRepeatField.getText();
		String name = nameField.getText();
		String surname = surnameField.getText();
		String message = "";

		User u = new User(login, password, name, surname);
		
		boolean showMessage = false;
		
		if(login != null && login != "" && password != null && password != "" && name != null && name != "" &&
				surname != null && surname != "") {
			
			backgroundThread = new Service<Void>() {
				@Override
				protected Task<Void> createTask() {
					return new Task<Void>() {
						@Override
						protected Void call() throws Exception {
							NetworkConnection.sendObject(u);
							return null;
						}
					};
				}
			};
			
			backgroundThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				public void handle(WorkerStateEvent event) {
					System.out.println("done!");
				}
			});
		}
		
		controlLabel.setText(message);
		backgroundThread.restart();
	}
	
	
    @FXML
    private void login(ActionEvent event) { // handler
    	String login = loginField.getText();
		String password = passwordField.getText();
		
		User u = new User(login, password);
		SocketEvent e = new SocketEvent("loginReq", u);
		
		
		// here check if login is valid
		if(login != null && login != "" && password != null && password != "") {
			
		backgroundThread = new Service<Void>() {
			@Override
			protected Task<Void> createTask() {
				return new Task<Void>() {
					@Override
					protected Void call() throws Exception {
//						controlLabel.setText(q.toString());
						System.out.print("Wysy³am: ");
						System.out.println(u);
						NetworkConnection.sendObject(e);
						
//						SocketEvent res = (SocketEvent) NetworkConnection.getObject();
						User resData = (User) NetworkConnection.getObject();
						System.out.print("Odebrano: ");
						System.out.println(resData);
						
//						loadScene((Stage) ((Node)event.getSource()).getScene().getWindow(), 
//						"view/ApplicationLayout.fxml", 900, 600, true);
						return null;
					}
				};
			}
		};
		
		backgroundThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			public void handle(WorkerStateEvent event) {
				System.out.println("done!");
			}
		});
		
		backgroundThread.restart();
		}
    }
    
    @FXML
    private void goToApplication(ActionEvent event) {
		Stage sourceStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
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
