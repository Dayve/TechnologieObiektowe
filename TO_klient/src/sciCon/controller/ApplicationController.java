package sciCon.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sciCon.model.Conference;
import sciCon.model.Controllers;
import sciCon.model.NetworkConnection;
import sciCon.model.SocketEvent;
import sciCon.model.User;
import sciCon.model.Week;

public class ApplicationController implements Controllers {

	@FXML private Button prevMonth;
	@FXML private Button nextMonth;
	@FXML private TableView<Week> calendarTable; // A TableView representing the calendar
	@FXML private Label currentlyChosenDateLabel;
	@FXML private VBox conferenceFeedBox;
	@FXML private ComboBox<String> conferenceFeedCB;
	@FXML private ComboBox<String> conferenceFeedNumberCB;
	private ArrayList<Conference> feed;
	private LocalDate calendarsDate; // It represents the currently selected (clicked) date
	
	private void fillVBoxWithPanes(VBox vb, ArrayList<Conference> cs){
		int index = 0;
		for(Conference c : cs) {
			Label feed = new Label(c.toString());
			TitledPane tpane = new TitledPane(c.getName(), feed);
			tpane.setExpanded(false);
			
			vb.getChildren().add(index, tpane);
			index++;
		}
	}
	
	private void reqConferenceFeed() {
		
		Boolean past = false;
		SocketEvent e = new SocketEvent("reqConferenceFeed", past);
		NetworkConnection.sendSocketEvent(e);
		SocketEvent res = NetworkConnection.rcvSocketEvent();

		String eventName = res.getName();
	
		
		if (eventName.equals("fetchConferenceFeed")) {
			feed = res.getObject(ArrayList.class);
		}
		
//		 run in JavaFX after background thread finishes work
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if(feed != null) {
					fillVBoxWithPanes(conferenceFeedBox, feed);
				}
			}
		});

	}
	
	@FXML
	public void initialize() {
		ObservableList<String> feedOptions = 
			    FXCollections.observableArrayList(
			        "Nadchodz¹ce konferencje",
			        "Wszystkie konferencje",
			        "Zakoñczone konferencje"
			    );
		conferenceFeedCB.getItems().addAll(feedOptions);
		conferenceFeedCB.setValue("Nadchodz¹ce konferencje");
		
		ObservableList<String> feedNumberOptions = 
			    FXCollections.observableArrayList("20","50","100","...");
		
		conferenceFeedNumberCB.getItems().addAll(feedNumberOptions);
		conferenceFeedNumberCB.setValue("50");
		
		calendarsDate = LocalDate.now();
		CalendarController.fillCalendarTable(calendarTable, 
				currentlyChosenDateLabel, calendarsDate);
		
		reqConferenceFeed();
	}
	
	public void changeMonthToNext() {
		calendarsDate = calendarsDate.plusMonths(1);
		CalendarController.refreshCalendarTable(calendarTable, currentlyChosenDateLabel, calendarsDate);
	}
	
	public void changeMonthToPrevious() {
		calendarsDate = calendarsDate.minusMonths(1);
		CalendarController.refreshCalendarTable(calendarTable, currentlyChosenDateLabel, calendarsDate);
	}
	
	@FXML
	public void addConferenceBtn(ActionEvent event) {
		openNewWindow(event, "view/ConferenceCreatorLayout.fxml", 600, 650, false, "Dodaj konferencjê");
	}
}