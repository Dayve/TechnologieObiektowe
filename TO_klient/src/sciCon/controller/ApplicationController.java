package sciCon.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

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
	@FXML
	private TabPane eventDetailsTP;

	private ArrayList<Conference> feed = new ArrayList<Conference>();
	private FeedController feedController = new FeedController();
	private CalendarController calendar = new CalendarController(feedController);

	public static User currentUser;
	private Event sharedEvent = null;
	private String message = null;
	private int checkedRequestsWithoutUpdate = 0;

	private ConferenceFilter filter;

	public enum feedReqPeriod {
		PAST, FUTURE, ALL
	};

	public static final int CHAR_LIMIT_IN_TITLEPANE = 35;

	private static LinkedBlockingQueue<requestType> requestQueue = new LinkedBlockingQueue<requestType>();

	public static void makeRequest(requestType newRequest) {
		requestQueue.add(newRequest);
	}

	public enum requestType {
		UPDATE_CONFERENCE_FEED
	};

	@FXML
	private void filterFeed() {
		String feedPeriodCB = conferenceFeedCB.getValue();
		filter = ConferenceFilter.ALL;
		if (feedPeriodCB.equals("Zakończone konferencje")) {
			filter = ConferenceFilter.PAST;
		} else if (feedPeriodCB.equals("Nadchodzące konferencje")) {
			filter = ConferenceFilter.FUTURE;
		}
		ArrayList<Conference> filtered = feedController.filterFeed(feed, filter);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				feedController.fillListWithLabels(conferenceFeedList, filtered, eventDetailsTP, filter,
						CHAR_LIMIT_IN_TITLEPANE, true);
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
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						// fill FeedBox and Calendar in JavaFX UI Thread
						filterFeed();
						calendar.refreshCalendarTable(calendarTable, currentlyChosenDateLabel,
								calendar.getCalendarsDate(), feed, eventDetailsTP, listOfSelectedDaysEvents);
					}
				});
			}
		}

	}

	// sends request for the current user object
	public void reqCurrentUser() {
		SocketEvent se = new SocketEvent("reqCurrentUser");
		NetworkConnection.sendSocketEvent(se);
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

	@FXML
	public void joinConferenceBtn(ActionEvent evt) {
		sharedEvent = evt;
		new Thread(() -> reqJoinConference()).start();
	}

	private void reqJoinConference() {
		ArrayList<Integer> userIdConferenceId = new ArrayList<Integer>();
		userIdConferenceId.add(currentUser.getId());
		userIdConferenceId.add(feedController.getSelectedConferenceId());

		SocketEvent se = new SocketEvent("reqJoinConference", userIdConferenceId);
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent();
		String eventName = res.getName();
		message = eventName.equals("joinConferenceSucceeded")
				? "Wysłano prośbę o udział w konferencji do jej organizatora."
				: "Nie udało się dołączyć do konferencji.";

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				openDialogBox(sharedEvent, message);
			}
		});
	}

	public void logout() {
		NetworkConnection.disconnect();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				loadScene(sharedEvent, "view/LoginLayout.fxml", 320, 250, false, 0, 0);
			}
		});
	}

	@FXML
	private void logoutButton(ActionEvent event) {
		sharedEvent = event;
		// here check if login is valid
		new Thread(() -> logout()).start();
	}

	@FXML
	public void initialize() {
		calendar.setCalendarsDate(LocalDate.now());
		ObservableList<String> feedOptions = FXCollections.observableArrayList("Nadchodzące konferencje",
				"Wszystkie konferencje", "Zakończone konferencje");

		conferenceFeedCB.getItems().addAll(feedOptions);
		conferenceFeedCB.setValue("Nadchodzące konferencje");

		ObservableList<String> feedNumberOptions = FXCollections.observableArrayList("20", "50", "100", "...");

		conferenceFeedNumberCB.getItems().addAll(feedNumberOptions);
		conferenceFeedNumberCB.setValue("50");

		makeRequest(requestType.UPDATE_CONFERENCE_FEED);

		Client.timer = new Timer();
		Client.timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						// TODO: requestQueue.contains() and remove() should be
						// changed to something
						// more appropriate once we extend requestType
						if (requestQueue.contains(requestType.UPDATE_CONFERENCE_FEED)
								|| checkedRequestsWithoutUpdate > 10) {
							reqConferenceFeed();
							checkedRequestsWithoutUpdate = 0;
							requestQueue.remove(requestType.UPDATE_CONFERENCE_FEED);
						} else
							checkedRequestsWithoutUpdate++;
					}
				});
			}
		}, 0, 1000);

	
		calendarTable.getSelectionModel().setCellSelectionEnabled(true);

		new Thread(() -> reqCurrentUser()).start();
	}

	public void changeMonthToNext() {
		calendar.setCalendarsDate(calendar.getCalendarsDate().plusMonths(1));
		calendar.refreshCalendarTable(calendarTable, currentlyChosenDateLabel, calendar.getCalendarsDate(), feed,
				eventDetailsTP, listOfSelectedDaysEvents);
	}

	public void changeMonthToPrevious() {
		calendar.setCalendarsDate(calendar.getCalendarsDate().minusMonths(1));
		calendar.refreshCalendarTable(calendarTable, currentlyChosenDateLabel, calendar.getCalendarsDate(), feed,
				eventDetailsTP, listOfSelectedDaysEvents);
	}

	@FXML
	public void addConferenceBtn(ActionEvent event) {
		openNewWindow(event, "view/ConferenceCreatorLayout.fxml", 600, 650, false, "Dodaj konferencję");
	}
}