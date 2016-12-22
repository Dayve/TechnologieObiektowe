package sciCon.controller;

import java.time.LocalDate;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import sciCon.model.Conference;
import sciCon.model.Controller;
import sciCon.model.NetworkConnection;
import sciCon.model.SocketEvent;
import sciCon.model.User;
import sciCon.model.Week;

public class ApplicationController implements Controller {

	@FXML private Button prevMonth;
	@FXML private Button nextMonth;
	@FXML private TableView<Week> calendarTable; // A TableView representing the calendar
	@FXML private Label currentlyChosenDateLabel;
	@FXML private VBox conferenceFeedBox;
	@FXML private AnchorPane feedAnchorPane;
	@FXML private ComboBox<String> conferenceFeedCB;
	@FXML private ComboBox<String> conferenceFeedNumberCB;
	@FXML private Label loginLabel;
	@FXML private VBox listOfSelectedDaysEvents;
	Event sharedEvent = null;
	
	public static final int CHAR_LIMIT_IN_TITLEPANE = 35;
	
	public static User currentUser;
	private ArrayList<Conference> feed;
	private static LocalDate calendarsDate; // It represents the currently selected (clicked) date
	public enum feedReqPeriod { PAST, FUTURE, ALL };
	
	private void fillVBoxWithPanes(VBox vb, ArrayList<Conference> cs){
		int index = 0;
		vb.getChildren().clear();
		TitledPane tpane = null;
		for(Conference c : cs) {
			TextArea feed = new TextArea(c.toString());
			feed.setWrapText(true);
			feed.setEditable(false);
			tpane = new TitledPane();
			tpane.setText(addNLsIfTooLong(c.getName(), CHAR_LIMIT_IN_TITLEPANE));
			tpane.setContent(feed);
			tpane.setExpanded(false);
			
			vb.getChildren().add(index, tpane);
			index++;
		}
		AnchorPane.setTopAnchor((Node)vb, 0.0);
		AnchorPane.setBottomAnchor((Node)vb, 0.0);
		AnchorPane.setLeftAnchor((Node)vb, 0.0);
		AnchorPane.setRightAnchor((Node)vb, 0.0);
	}
		
	@SuppressWarnings("unchecked")
	@FXML private void reqConferenceFeed() {
		String feedPeriodCB = conferenceFeedCB.getValue();
		Boolean feedPeriod = null;
		if(feedPeriodCB.equals("Zakończone konferencje")) {
			feedPeriod = true;
		} else if (feedPeriodCB.equals("Nadchodzące konferencje")) {
			feedPeriod = false;
		}
		
		SocketEvent e = new SocketEvent("reqConferenceFeed", feedPeriod);
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
	
	public void reqCurrentUser() {
		SocketEvent e = new SocketEvent("reqCurrentUser");
		NetworkConnection.sendSocketEvent(e);
		SocketEvent res = NetworkConnection.rcvSocketEvent();

		String eventName = res.getName();
		if(eventName.equals("currentUserSucceeded")) {
			currentUser = res.getObject(User.class);
		}
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				loginLabel.setText("Zalogowano: " + currentUser.getLogin() + ".");
			}
		});
	}
	
	public void reqLogout() {
		NetworkConnection.disconnect();
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				loadScene(sharedEvent, "view/LoginLayout.fxml", 320, 250, false, 0, 0);
			}
		});
	}
	
	@FXML private void reqLogoutButton(ActionEvent event) {
		sharedEvent = event;
		// here check if login is valid
		java.lang.reflect.Method m = null;
		try {
			m = ApplicationController.class.getMethod("reqLogout");
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		runInAnotherThread(m, this);
	}
	
	@FXML
	public void initialize() {
	
//		conferenceFeedBox.setFillWidth(true);
		ObservableList<String> feedOptions = 
			    FXCollections.observableArrayList(
			        "Nadchodzące konferencje",
			        "Wszystkie konferencje",
			        "Zakończone konferencje"
			    );
		
		// Dummy content:
		/*
		ObservableList<TitledPane> items = FXCollections.observableArrayList(
				new TitledPane("Tytuł pierwszego", new TextArea("Tekst do pierwszego\nwydarzenia na liście")),
				new TitledPane("Tytuł drugiego", new TextArea("Tekst do drugiego, lepszego\nwydarzenia na liście bocznej"))
		);
		listOfSelectedDaysEvents.setItems(items);
		*/
		// --------------
		
		conferenceFeedCB.getItems().addAll(feedOptions);
		conferenceFeedCB.setValue("Nadchodzące konferencje");
		
		ObservableList<String> feedNumberOptions = 
			    FXCollections.observableArrayList("20","50","100","...");
		
		conferenceFeedNumberCB.getItems().addAll(feedNumberOptions);
		conferenceFeedNumberCB.setValue("50");
		
		// Must be called before CalendarController.fillCalendarTable:
		reqConferenceFeed();
		
		calendarsDate = LocalDate.now();
		CalendarController.fillCalendarTable(calendarTable, 
				currentlyChosenDateLabel, calendarsDate, feed, listOfSelectedDaysEvents);
		
		calendarTable.getSelectionModel().setCellSelectionEnabled(true);
		
		java.lang.reflect.Method m = null;
		try {
			m = ApplicationController.class.getMethod("reqCurrentUser");
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		runInAnotherThread(m, this);
	}
	
	public static String addNLsIfTooLong(String givenString, int limit) {
		String[] separateWords = givenString.split("\\s+");
		String result = new String();
		int howMuchCharsSoFar = 0;
		
		for(int i=0 ; i<separateWords.length ; ++i) {			
			howMuchCharsSoFar += separateWords[i].length() + 1; // +1 because we assume that every word has a space at the end
			
			if(howMuchCharsSoFar > limit) {
				result += "\n";
				howMuchCharsSoFar = 0;
			}
			result += separateWords[i] + " ";
		}
		
		return result;
	}
	
	public void changeMonthToNext() {
		calendarsDate = calendarsDate.plusMonths(1);
		CalendarController.refreshCalendarTable(calendarTable, currentlyChosenDateLabel, calendarsDate, feed, listOfSelectedDaysEvents);
	}
	
	public void changeMonthToPrevious() {
		calendarsDate = calendarsDate.minusMonths(1);
		CalendarController.refreshCalendarTable(calendarTable, currentlyChosenDateLabel, calendarsDate, feed, listOfSelectedDaysEvents);
	}
	
	@FXML
	public void addConferenceBtn(ActionEvent event) {
		openNewWindow(event, "view/ConferenceCreatorLayout.fxml", 600, 650, false, "Dodaj konferencję");
	}
}