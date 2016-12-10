package sciCon.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import sciCon.model.Controllers;

public class AlertController implements Controllers{
	
	@FXML private TextArea alertText;
	
	public void setAlertText(String text) {
		alertText.setText(text);
	}
	
	
	@FXML public void closeWindowBtn(ActionEvent event) {
		closeWindow(event);
	}
}
