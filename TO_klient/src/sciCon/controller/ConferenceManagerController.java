package sciCon.controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import sciCon.model.Controller;

public class ConferenceManagerController implements Controller {
	
	@FXML Parent confManagerWindow;
	@FXML private TextField searchUserField;
	@FXML private TextField searchFileField;
	@FXML private ListView usersLV;
	@FXML private ListView filesLV;
	
	private String message;
	
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
