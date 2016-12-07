package sciCon.controller;

import java.time.LocalDate;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import sciCon.Controllers;
import sciCon.model.Conference;
import sciCon.model.NetworkConnection;
import sciCon.model.SocketEvent;

public class ConferenceCreatorController implements Controllers {
	
	@FXML private TextField nameField;
	@FXML private TextField subjectField;
	@FXML private DatePicker dateField;
	@FXML private TextField startTimeField;
	@FXML private TextField endTimeField;
	@FXML private TextArea placeField;
	@FXML private TextArea descriptionField;
	@FXML private TextArea agendaField;
	@FXML private Label controlLabel;
	
	private String message;
	
	@FXML
	public void initialize() {
		dateField.setValue(LocalDate.now());
	}
	
	@FXML
	public void reqAddConference() {
		String name = nameField.getText();
		String subject = subjectField.getText();
		LocalDate date = dateField.getValue();
		String startTime = startTimeField.getText();
		String endTime = endTimeField.getText();
		String place = placeField.getText();
		String description = descriptionField.getText();
		String agenda = agendaField.getText();
		
		Conference conf = new Conference(name, date, subject, startTime,
				endTime, place, description, agenda);
		
		SocketEvent e = new SocketEvent("reqAddConference", conf);
		NetworkConnection.sendSocketEvent(e);
		
		SocketEvent res = NetworkConnection.rcvSocketEvent();
		String eventName = res.getName();
		
		if(eventName.equals("addConferenceSucceeded")) {
			message = "Dodano konferencjê.";
		} else if (eventName.equals("addConferenceFailed")){
			message = res.getObject(String.class);
		} else {
			message = "Nie uda³o siê dodaæ konferencji. Serwer nie odpowiada.";
		}
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				controlLabel.setText(message);
			}
		});
		
	}
	
	@FXML
	public void addConferenceBtn() {
		java.lang.reflect.Method m = null;
		try {
			m = ConferenceCreatorController.class.getMethod("reqAddConference");
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		runInAnotherThread(m, this);
	}
	
	@FXML
	public void closeWindowBtn(ActionEvent event) {
		closeWindow(event);
	}
}
