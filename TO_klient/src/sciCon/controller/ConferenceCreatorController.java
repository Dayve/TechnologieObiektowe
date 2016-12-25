package sciCon.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import sciCon.controller.ApplicationController.requestType;
import sciCon.model.Conference;
import sciCon.model.Controller;
import sciCon.model.NetworkConnection;
import sciCon.model.SocketEvent;

public class ConferenceCreatorController implements Controller {
	
	@FXML private TextField nameField;
	@FXML private TextField subjectField;
	@FXML private DatePicker dateField;
	@FXML private ComboBox<String> startHr;
	@FXML private ComboBox<String> startMin;
	@FXML private ComboBox<String> endHr;
	@FXML private ComboBox<String> endMin;
	@FXML private TextArea placeField;
	@FXML private TextArea descriptionField;
	@FXML private TextArea agendaField;
	
	// Date which will be used to initialize the DatePicker:
	private static LocalDate conferenceDestinedDay = LocalDate.now();
	
	private Event sharedEvent;
	private String message;
	
	@FXML
	public void initialize() {
		ObservableList<String> hours = 
			    FXCollections.observableArrayList(
			        "00", "01", "02", "03", "04", "05", "06",
			        "07", "08", "09", "10", "11", "12", "13",
			        "14", "15", "16", "17", "18", "19", "20",
			        "21", "22", "23"
			    );
		ObservableList<String> minutes = 
			    FXCollections.observableArrayList(
			        "00", "05", "10", "15", "20", "25", "30",
			        "35", "40", "45", "50", "55"
			    );
		
		startHr.getItems().addAll(hours);
		endHr.getItems().addAll(hours);
		startMin.getItems().addAll(minutes);
		endMin.getItems().addAll(minutes);
		dateField.setValue(conferenceDestinedDay);
	}
	
	// This function is called when a day is clicked (from CalendarController):
	public static void setChosenDay(LocalDate when) {
		conferenceDestinedDay = when;
	}

	@FXML
	public void reqAddConference() {
		String name = nameField.getText();
		String subject = subjectField.getText();
		// get LocalDateTime from LocalDate
		LocalDateTime date = dateField.getValue().atStartOfDay(); 
		String startHrCB = startHr.getSelectionModel().getSelectedItem();
		String startMinCB = startMin.getSelectionModel().getSelectedItem();
		String endHrCB = endHr.getSelectionModel().getSelectedItem();
		String endMinCB = endMin.getSelectionModel().getSelectedItem();

		//check if all hour and min combo boxes are filled
		
		if (startHrCB != null && startMinCB != null && endHrCB != null && endMinCB != null) {

			LocalDateTime startTime = date.plusHours(Long.parseLong(startHrCB)).plusMinutes(Long.parseLong(startMinCB));
			LocalDateTime endTime = date.plusHours(Long.parseLong(endHrCB)).plusMinutes(Long.parseLong(endMinCB));

			String place = placeField.getText();
			String description = descriptionField.getText();
			String agenda = agendaField.getText();	

			Conference conf = new Conference(name, subject, startTime, endTime, 
					place, description, agenda, ApplicationController.currentUser);
			
			SocketEvent e = new SocketEvent("reqAddConference", conf);
			NetworkConnection.sendSocketEvent(e);

			
			SocketEvent res = NetworkConnection.rcvSocketEvent();
			String eventName = res.getName();

			if (eventName.equals("addConferenceSucceeded")) {
				message = "Dodano konferencję.";
				ApplicationController.makeRequest(requestType.UPDATE_CONFERENCE_FEED);
			} else if (eventName.equals("addConferenceFailed")) {
				message = res.getObject(String.class);
			} else {
				message = "Nie udało się dodać konferencji. Serwer nie odpowiada.";
			}
		} else {
			message = "Wypełnij wszystkie pola z godziną i minutą.";
		}
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				openDialogBox(sharedEvent, message);
			}
		});

	}

	@FXML
	public void addConferenceBtn(Event evt) {
		sharedEvent = evt;
		new Thread(() ->reqAddConference()).start();
	}

	@FXML
	public void closeWindowBtn(ActionEvent event) {
		closeWindow(event);
	}
}
