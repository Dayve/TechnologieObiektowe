package sciCon.controller;

import java.time.LocalDate;

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
		LocalDate date = dateField.getValue();
		String startTime = startHr.getSelectionModel().getSelectedItem() + 
				"." + startMin.getSelectionModel().getSelectedItem();
		String endTime = endHr.getSelectionModel().getSelectedItem() + 
				"." + endMin.getSelectionModel().getSelectedItem();
		String place = placeField.getText();
		String description = descriptionField.getText();
		String agenda = agendaField.getText();

		Conference conf = new Conference(name, date, subject, startTime, endTime, place, description, agenda);

		SocketEvent e = new SocketEvent("reqAddConference", conf);
		NetworkConnection.sendSocketEvent(e);

		SocketEvent res = NetworkConnection.rcvSocketEvent();
		String eventName = res.getName();

		if (eventName.equals("addConferenceSucceeded")) {
			message = "Dodano konferencję.";
		} else if (eventName.equals("addConferenceFailed")) {
			message = res.getObject(String.class);
		} else {
			message = "Nie udało się dodać konferencji. Serwer nie odpowiada.";
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
