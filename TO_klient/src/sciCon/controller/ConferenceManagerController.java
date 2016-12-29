package sciCon.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import sciCon.model.Conference;
import sciCon.model.Controller;
import sciCon.model.NetworkConnection;
import sciCon.model.SocketEvent;

public class ConferenceManagerController implements Controller {
	
	@FXML Parent confManagerWindow;
	@FXML private TextField searchUserField;
	@FXML private TextField searchFileField;
	@FXML private ListView usersLV;
	@FXML private ListView filesLV;
	
	private String message;
	
//	@FXML
//	public void initialize() {
//		
//	}
	
	@FXML
	public void reqModifyUsersRole() {
		
	}
	
	@FXML
	public void addFileBtn() {
		
//		} else {
//			message = "Wypełnij wszystkie pola z godziną i minutą.";
//		}
//		Platform.runLater(new Runnable() {
//			@Override
//			public void run() {
//				openDialogBox(confManagerWindow, message);
//			}
//		});

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
