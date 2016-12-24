package sciCon.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import sciCon.Client;
import sciCon.model.Conference;
import sciCon.model.Controller;
import sciCon.model.NetworkConnection;
import sciCon.model.SocketEvent;
import sciCon.model.User;
import sciCon.model.Week;

public class ApplicationController implements Controller {

	@FXML
	private Button prevMonth;
	@FXML
	private Button nextMonth;
	@FXML
	private TableView<Week> calendarTable; // A TableView representing the
											// calendar
	@FXML
	private Label currentlyChosenDateLabel;
	@FXML
	private ListView<Label> conferenceFeedList;
	@FXML
	private AnchorPane feedAnchorPane;
	@FXML
	private ComboBox<String> conferenceFeedCB;
	@FXML
	private ComboBox<String> conferenceFeedNumberCB;
	@FXML
	private Label loginLabel;
	@FXML
	private ListView<Label> listOfSelectedDaysEvents;
	private TabPane eventDetailsTP;
	private ConferenceFilter filter;
	Event sharedEvent = null;

	public static final int CHAR_LIMIT_IN_TITLEPANE = 35;

	public static User currentUser;
	private ArrayList<Conference> feed = new ArrayList<Conference>();

	private CalendarController calendar = new CalendarController();
	private static LocalDate calendarsDate; // It represents the currently
											// selected (clicked) date

	public enum feedReqPeriod {
		PAST, FUTURE, ALL
	};

	@FXML private void reqFilterFeed() {
		String feedPeriodCB = conferenceFeedCB.getValue();
		filter = ConferenceFilter.ALL;
		if (feedPeriodCB.equals("Zakończone konferencje")) {
			filter = ConferenceFilter.PAST;
		} else if (feedPeriodCB.equals("Nadchodzące konferencje")) {
			filter = ConferenceFilter.FUTURE;
		}
		ArrayList<Conference> filtered = filterFeed(feed, filter);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				fillListWithLabels(conferenceFeedList, filtered, filter, CHAR_LIMIT_IN_TITLEPANE, true);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	@FXML
	public void reqConferenceFeed() {
		SocketEvent e = new SocketEvent("reqConferenceFeed");
		NetworkConnection.sendSocketEvent(e);
		SocketEvent res = NetworkConnection.rcvSocketEvent();

		String eventName = res.getName();
		ArrayList<Conference> tempFeed;
		
		if (eventName.equals("updateConferenceFeed")) {
			// get temp feed to compare it with current one
			tempFeed = res.getObject(ArrayList.class);

			// run in JavaFX after background thread finishes work
			// compare if feeds match, if so, don't fill vbox with new content
			if (tempFeed != null && !tempFeed.toString().equals(feed.toString())) {
				feed = tempFeed;
				System.out.println();
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						// fill FeedBox and Calendar in JavaFX UI Thread
						reqFilterFeed();
						calendar.refreshCalendarTable(calendarTable, currentlyChosenDateLabel, calendarsDate, feed,
								listOfSelectedDaysEvents);
					}
				});
			}
		}

	}

	// sends request for the current user object
	public void reqCurrentUser() {
		SocketEvent e = new SocketEvent("reqCurrentUser");
		NetworkConnection.sendSocketEvent(e);
		SocketEvent res = NetworkConnection.rcvSocketEvent();

		String eventName = res.getName();
		if (eventName.equals("currentUserSucceeded")) {
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

	@FXML
	private void reqLogoutButton(ActionEvent event) {
		sharedEvent = event;
		// here check if login is valid
		new Thread(() -> reqLogout()).start();
	}

	@FXML
	public void initialize() {
		ObservableList<String> feedOptions = FXCollections.observableArrayList("Nadchodzące konferencje",
				"Wszystkie konferencje", "Zakończone konferencje");

		conferenceFeedCB.getItems().addAll(feedOptions);
		conferenceFeedCB.setValue("Nadchodzące konferencje");

		ObservableList<String> feedNumberOptions = FXCollections.observableArrayList("20", "50", "100", "...");

		conferenceFeedNumberCB.getItems().addAll(feedNumberOptions);
		conferenceFeedNumberCB.setValue("50");
		
		Client.timer = new Timer();
		Client.timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						reqConferenceFeed();
					}
				});
			}
		}, 0, 10000);
		
		
		calendarsDate = LocalDate.now();
		calendarTable.getSelectionModel().setCellSelectionEnabled(true);

		new Thread(() -> reqCurrentUser()).start();
		
		
	}

	public void changeMonthToNext() {
		calendarsDate = calendarsDate.plusMonths(1);
		calendar.refreshCalendarTable(calendarTable, currentlyChosenDateLabel, calendarsDate, feed,
				listOfSelectedDaysEvents);
	}

	public void changeMonthToPrevious() {
		calendarsDate = calendarsDate.minusMonths(1);
		calendar.refreshCalendarTable(calendarTable, currentlyChosenDateLabel, calendarsDate, feed,
				listOfSelectedDaysEvents);
	}

	@FXML
	public void addConferenceBtn(ActionEvent event) {
		openNewWindow(event, "view/ConferenceCreatorLayout.fxml", 600, 650, false, "Dodaj konferencję");
	}
}