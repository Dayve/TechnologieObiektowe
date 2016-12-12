package sciCon.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import sciCon.model.Controller;

public class DialogController implements Controller{
	
	@FXML private TextArea dialogMessage;
	
	public void setDialogMessage(String text) {
		dialogMessage.setText(text);
	}
	
	
	@FXML public void closeWindowBtn(ActionEvent event) {
		closeWindow(event);
	}
}
