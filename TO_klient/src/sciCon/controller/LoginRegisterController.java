package sciCon.controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sciCon.Controllers;
import sciCon.dbInterface;

public class LoginRegisterController implements Controllers, dbInterface {
	
	@FXML private TextField loginField;
	@FXML private TextField passwordField;
	@FXML private TextField passwordRepeatField;
	@FXML private Label controlLabel;
	
	@FXML
	private void register() {
		String login = loginField.getText();
		String password = passwordField.getText();
		String rePassword = passwordRepeatField.getText();
		String message = "";
		boolean showMessage = false;
		
		if(!isLoginValid(login)) {
			message += "Login jest ju¿ zajêty. ";
			showMessage = true;
		}
		
		if(!isPasswordValid(password, rePassword)) {
			message += "\nHas³a do siebie nie pasuj¹.";
			showMessage = true;
		}
		if(showMessage == false) {
			message = "";
			System.out.println("Zarejestrowano: ");
		} 
		
		controlLabel.setText(message);
	
	}

	@FXML
	private void goToLogin(ActionEvent event) {
		Stage sourceStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
		loadScene(sourceStage, "view/LoginLayout.fxml", 320, 200, false);
	}
	
	@FXML
	private void goToRegistration(ActionEvent event) {
		Stage sourceStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
		loadScene(sourceStage, "view/RegisterLayout.fxml", 320, 200, false);
	}

    @FXML
    private void login(ActionEvent event) { // handler
    	String login = loginField.getText();
		String password = passwordField.getText();
		
		// here check if login is valid
		
    	Stage sourceStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
    	loadScene(sourceStage, "view/ApplicationLayout.fxml", 900, 600, true);
    }
}
