package sciCon.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import com.sun.javafx.scene.control.skin.TableHeaderRow;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import sciCon.Client;
import sciCon.model.Conference;
import sciCon.model.Controller;
import sciCon.model.NetworkConnection;
import sciCon.model.SocketEvent;
import sciCon.model.User;
import sciCon.model.Week;

@SuppressWarnings("restriction")
public class ApplicationController implements Controller {

	@FXML Parent applicationWindow;
	@FXML ComboBox<String> monthsCB;
	@FXML private ComboBox<String> yearsCB;
	@FXML private Button prevMonth;
	@FXML private Button nextMonth;
	@FXML private TableView<Week> calendarTable; // A TableView representing the
	// calendar
	@FXML Button joinLeaveManageConfBtn;
	@FXML Button removeConfBtn;
	@FXML private ListView<Label> conferenceFeedList;
	@FXML private AnchorPane feedAnchorPane;
	@FXML private ComboBox<String> conferenceFeedCB;
	@FXML private ComboBox<String> conferenceFeedNumberCB;
	@FXML private Label loginLabel;
	@FXML private ListView<Label> listOfSelectedDaysEvents;
	@FXML private TabPane eventDetailsTP;
	@FXML private TextField searchField;
	@FXML private TextArea forumsMessage;

	private CalendarController calendar = new CalendarController(fc);

	public static User currentUser;
	private String message = null;
	private int checkedRequestsWithoutUpdate = 0;

	private ConferenceFilter filter;

	public enum feedReqPeriod {
		PAST, FUTURE, ALL
	};

	public static final int CHAR_LIMIT_IN_TITLEPANE = 35;

	private static LinkedBlockingQueue<RequestType> requestQueue = new LinkedBlockingQueue<RequestType>();

	@FXML public void initialize() {
		setupFeedFilterCBs();
		setupTabPane();
		reqConferenceFeed();
		setupTimer();
		setupForumTextArea();
		setupMonthsYearsCBs();
		setupCalendar();
		new Thread(() -> reqCurrentUser()).start();

		Platform.runLater(new Runnable() {
			@Override public void run() {
				setupTabResizeEvent();
			}
		});
	}

	// static method allowing other controllers to make requests
	// which will be fulfilled by ApplicationController with every timer's tick
	public static void makeRequest(RequestType newRequest) {
		requestQueue.add(newRequest);
	}

	private void setupForumTextArea() {
		forumsMessage.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER & 
						(event.isControlDown() | event.isShiftDown())) {
					forumsMessage.setText(forumsMessage.getText() + "\n");
					forumsMessage.end();
				} else if (event.getCode() == KeyCode.ENTER) {
					if (forumsMessage.getText().length() > 0) {
						reqSendForumMessage(forumsMessage.getText());
						forumsMessage.clear();
					}
					event.consume();
				}
			}
		});
	}

	private void setupTabResizeEvent() {
		Stage mainStage = (Stage) applicationWindow.getScene().getWindow();
		mainStage.heightProperty().addListener(new ChangeListener<Number>() {
			@Override public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				fc.resizeSelectedConferenceTab(eventDetailsTP, arg2.intValue());
			}
		});
	}

	// sets up the TabPane - makes it modify selectedConferenceId on tab
	// selection change
	private void setupTabPane() {
		eventDetailsTP.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override public void changed(ObservableValue<? extends Tab> ov, Tab from, Tab to) {
				if (to != null) {
					fc.resizeSelectedConferenceTab(eventDetailsTP,
							applicationWindow.getScene().getWindow().heightProperty().getValue().intValue());
					fc.setSelectedConferenceId(Integer.parseInt(to.getId()));
					checkUsersParticipation();

				} else {
					fc.setSelectedConferenceId(null);
				}
			}
		});
	}

	// sets up the ComboBoxes allowing user to filter conferences
	private void setupFeedFilterCBs() {
		ObservableList<String> feedOptions = FXCollections.observableArrayList("Nadchodzące konferencje",
				"Wszystkie konferencje", "Zakończone konferencje");

		conferenceFeedCB.getItems().addAll(feedOptions);
		conferenceFeedCB.setValue("Nadchodzące konferencje");

		ObservableList<String> feedNumberOptions = FXCollections.observableArrayList("20", "50", "...");

		conferenceFeedNumberCB.getItems().addAll(feedNumberOptions);
		conferenceFeedNumberCB.setValue("50");

		searchField.textProperty().addListener(obs -> {
			refreshConferencesListView(searchField.getText());
		});
	}

	private void setupMonthsYearsCBs() {
		ObservableList<String> monthsFeedOptions = FXCollections.observableArrayList("Styczeń", "Luty", "Marzec",
				"Kwiecień", "Maj", "Czerwiec", "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień");

		monthsCB.getItems().addAll(monthsFeedOptions);
		monthsCB.setValue("miesiąc");

		ObservableList<String> yearsFeedOptions = FXCollections.observableArrayList("2016", "2017", "2018", "2019",
				"2020");

		yearsCB.getItems().addAll(yearsFeedOptions);
		yearsCB.setValue("rok");
	}

	// sets the calendar up - fills it according to the current date and lets
	// user select its cells
	private void setupCalendar() {
		calendar.setCalendarsDate(LocalDate.now());
		calendar.fillCalendarTable(calendarTable, fc.getFeed(), eventDetailsTP, listOfSelectedDaysEvents);
		calendarTable.getSelectionModel().setCellSelectionEnabled(true);
		// Set initial ComboBox values
		String currentDateInPolish = CalendarController.localDateToPolishDateString(calendar.getCalendarsDate());
		monthsCB.setValue(currentDateInPolish.substring(0, currentDateInPolish.indexOf(" ")));
		yearsCB.setValue(currentDateInPolish.substring(currentDateInPolish.indexOf(" ") + 1));

		calendarTable.widthProperty().addListener(new ChangeListener<Number>() {
			@Override public void changed(ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) {
				TableHeaderRow header = (TableHeaderRow) calendarTable.lookup("TableHeaderRow");
				header.reorderingProperty().addListener(new ChangeListener<Boolean>() {
					@Override public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
							Boolean newValue) {
						header.setReordering(false);
					}
				});
			}
		});
	}

	// sets the timer up - every second timer checks requestsQueue, which
	// contains
	// tasks from other controllers for ApplicationController to perform
	private void setupTimer() {
		Client.timer = new Timer();
		Client.timer.scheduleAtFixedRate(new TimerTask() {
			@Override public void run() {
				Platform.runLater(new Runnable() {
					@Override public void run() {
						// TODO: requestQueue.contains() and remove() should be
						// changed to something
						// more appropriate once we extend requestType

						if (requestQueue.contains(RequestType.UPDATE_CONFERENCE_FEED)
								|| checkedRequestsWithoutUpdate > 10) {
							reqConferenceFeed();
							checkedRequestsWithoutUpdate = 0;
							requestQueue.remove(RequestType.UPDATE_CONFERENCE_FEED);
						} else
							checkedRequestsWithoutUpdate++;

						if (requestQueue.contains(RequestType.REQUEST_JOINING_CONFERENCE)) {
							reqJoinConference();
							requestQueue.remove(RequestType.REQUEST_JOINING_CONFERENCE);
						}

						if (requestQueue.contains(RequestType.REQUEST_LEAVING_CONFERENCE)) {
							reqLeaveConference();
							requestQueue.remove(RequestType.REQUEST_LEAVING_CONFERENCE);
						}

						if (requestQueue.contains(RequestType.REQUEST_REMOVING_CONFERENCE)) {
							reqRemoveConference();
							requestQueue.remove(RequestType.REQUEST_REMOVING_CONFERENCE);
						}
					}
				});
			}
		}, 0, 1000);
	}

	public enum ParticipantsRole {
		PARTICIPANT, ORGANIZER, PRELECTOR, SPONSOR, PENDING, NONE
	}

	public static ParticipantsRole usersRoleOnConference(User user, Conference conference) {
		for (User u : conference.getParticipants()) {
			if (u.getId().equals(user.getId()))
				return ParticipantsRole.PARTICIPANT;
		}
		for (User u : conference.getOrganizers()) {
			if (u.getId().equals(user.getId()))
				return ParticipantsRole.ORGANIZER;
		}
		for (User u : conference.getPrelectors()) {
			if (u.getId().equals(user.getId()))
				return ParticipantsRole.PRELECTOR;
		}
		for (User u : conference.getSponsors()) {
			if (u.getId().equals(user.getId()))
				return ParticipantsRole.SPONSOR;
		}
		for (User u : conference.getPending()) {
			if (u.getId().equals(user.getId()))
				return ParticipantsRole.PENDING;
		}
		return ParticipantsRole.NONE;
	}

	private void reqSendForumMessage(String message) {
		if (fc.getSelectedConferenceId() != null) {
			if (message.length() > 0) {
				ArrayList<Integer> userIdConferenceId = new ArrayList<Integer>();
				userIdConferenceId.add(currentUser.getId());
				userIdConferenceId.add(fc.getSelectedConferenceId());
				SocketEvent se = new SocketEvent("reqSendForumMessage", userIdConferenceId, message);

				NetworkConnection.sendSocketEvent(se);
				SocketEvent res = NetworkConnection.rcvSocketEvent();
				String eventName = res.getName();
				if (eventName.equals("sendForumMessageSucceeded")) {
					reqConferenceFeed();
				} else {
					Platform.runLater(new Runnable() {
						@Override public void run() {
							openDialogBox(applicationWindow, "Wiadomość nie została wysłana.");
						}
					});
				}
			}
		}
	}

	// filters feed depending on conferenceCB's value - future/all/past
	// conferences
	@FXML private void filterFeed() {
		String feedPeriodCB = conferenceFeedCB.getValue();
		filter = ConferenceFilter.ALL;
		if (feedPeriodCB.equals("Zakończone konferencje")) {
			filter = ConferenceFilter.PAST;
		} else if (feedPeriodCB.equals("Nadchodzące konferencje")) {
			filter = ConferenceFilter.FUTURE;
		}
		ArrayList<Conference> filtered = fc.filterFeed(fc.getFeed(), filter);
		Platform.runLater(new Runnable() {
			@Override public void run() {
				fc.fillListWithLabels(conferenceFeedList, filtered, eventDetailsTP, filter, CHAR_LIMIT_IN_TITLEPANE,
						true);
				refreshConferencesListView(searchField.getText());
			}
		});
	}

	// checks if currentUser participates in given (selected) conference
	// and modifies leave/join button text and behaviour accordingly
	private void checkUsersParticipation() {
		Integer selectedConfId = fc.getSelectedConferenceId();
		// look for conference thats id is clicked
		if (selectedConfId != null) {
			try {
				Conference selectedConf = fc.getSelectedConference();
				ArrayList<User> selectedConfParticipants = selectedConf.getParticipantsList();
				ArrayList<User> selectedConfOrganizers = selectedConf.getOrganizers();
				boolean currentUserTakesPart = false;
				boolean currentUserIsOrganizer = false;
				// check if current user takes part in selected conference
				for (User u : selectedConfParticipants) {
					if (u.getId() == currentUser.getId()) {
						currentUserTakesPart = true;
						break;
					}
				}
				// check if current user is organizer of selected conference
				for (User u : selectedConfOrganizers) {
					if (u.getId() == currentUser.getId()) {
						currentUserIsOrganizer = true;
						break;
					}
				}

				if (currentUserIsOrganizer) {
					removeConfBtn.setDisable(false);
					joinLeaveManageConfBtn.setOnAction((event) -> {
						manageConferenceBtn();
					});
					joinLeaveManageConfBtn.setText("Zarządzaj");
				} else {
					removeConfBtn.setDisable(true);
					if (currentUserTakesPart || currentUserIsOrganizer) {
						joinLeaveManageConfBtn.setOnAction((event) -> {
							new Thread(() -> leaveConferenceBtn()).start();
						});
						joinLeaveManageConfBtn.setText("Wycofaj się");
					} else {
						joinLeaveManageConfBtn.setOnAction((event) -> {
							new Thread(() -> joinConferenceBtn()).start();
						});
						joinLeaveManageConfBtn.setText("Weź udział");
					}
				}
			} catch (NoSuchElementException e) {
				fc.setSelectedConferenceId(null);
			}
		}
	}

	// requests data about conferences from the database through the server
	// compares it with current data and if there is difference, updates
	// information
	@SuppressWarnings("unchecked") @FXML public void reqConferenceFeed() {
		SocketEvent e = new SocketEvent("reqConferenceFeed");
		NetworkConnection.sendSocketEvent(e);
		SocketEvent res = NetworkConnection.rcvSocketEvent();

		String eventName = res.getName();
		ArrayList<Conference> tempFeed;

		if (eventName.equals("updateConferenceFeed")) {
			// get temp feed to compare it with current one
			tempFeed = res.getObject(ArrayList.class);
			// fc.setFeed(tempFeed);

			// run in JavaFX after background thread finishes work
			// compare if feeds match, if so, don't fill vbox with new content
			if (tempFeed != null && !tempFeed.toString().equals(fc.getFeed().toString())) {
				fc.setFeed(tempFeed);

				Platform.runLater(new Runnable() {
					@Override public void run() {
						ArrayList<Conference> feed = fc.getFeed();
						fc.refreshConferenceTabs(eventDetailsTP, feed);
						// fill FeedBox and Calendar in JavaFX UI Thread
						checkUsersParticipation();
						filterFeed();
						calendar.refreshCalendarTable(calendarTable, calendar.getCalendarsDate(), feed, eventDetailsTP,
								listOfSelectedDaysEvents);
						refreshConferencesListView(searchField.getText());
						fc.fillListViewWithSelectedDaysConferences(calendar.getCalendarsDate(), feed, eventDetailsTP,
								listOfSelectedDaysEvents, false);
					}
				});
			}
		}

	}

	// bound to searchField, this is reaction to typed text
	private void refreshConferencesListView(String searchBoxContent) {

		String periodFilterFromComboBox = conferenceFeedCB.getValue();
		filter = ConferenceFilter.ALL;
		if (periodFilterFromComboBox.equals("Zakończone konferencje")) {
			filter = ConferenceFilter.PAST;
		} else if (periodFilterFromComboBox.equals("Nadchodzące konferencje")) {
			filter = ConferenceFilter.FUTURE;
		}

		FilteredList<Conference> searchBarFilteredData = new FilteredList<>(
				FXCollections.observableArrayList(fc.filterFeed(fc.getFeed(), filter)),
				s -> s.getName().toLowerCase().contains(searchBoxContent.toLowerCase()));

		ArrayList<Conference> searchBarFilteredData_ArrayList = new ArrayList<Conference>();
		for (Conference con : searchBarFilteredData)
			searchBarFilteredData_ArrayList.add(con);

		Platform.runLater(new Runnable() {
			@Override public void run() {
				fc.fillListWithLabels(conferenceFeedList, searchBarFilteredData_ArrayList, eventDetailsTP, filter,
						CHAR_LIMIT_IN_TITLEPANE, true);
			}
		});
	}

	// sends request for the current user object and puts it in currentUser
	// static variable
	public void reqCurrentUser() {
		SocketEvent se = new SocketEvent("reqCurrentUser");
		NetworkConnection.sendSocketEvent(se);
		SocketEvent res = NetworkConnection.rcvSocketEvent();

		String eventName = res.getName();
		if (eventName.equals("currentUserSucceeded")) {
			currentUser = res.getObject(User.class);
		}

		Platform.runLater(new Runnable() {
			@Override public void run() {
				loginLabel.setText("Zalogowano: " + currentUser.getLogin() + ".");
			}
		});
	}

	@FXML public void manageConferenceBtn() {
		Integer selectedConfId = fc.getSelectedConferenceId();
		if (selectedConfId != null) {
			String selectedConfName = fc.getSelectedConference().getName();
			openNewWindow(applicationWindow, "view/ConferenceManagerLayout.fxml", 650, 600, false,
					"Zarządzaj konferencją \"" + selectedConfName + "\"");
		}
	}

	// sends request to join conference after user confirms it
	@FXML public void joinConferenceBtn() {
		Integer selectedConfId = fc.getSelectedConferenceId();

		if (selectedConfId != null) {
			String conferenceName = fc.getSelectedConference().getName();

			String message = "Czy na pewno chcesz wziąć udział w konferencji \"" + conferenceName + "\"?";
			Platform.runLater(new Runnable() {
				@Override public void run() {
					openConfirmationWindow(applicationWindow, message, RequestType.REQUEST_JOINING_CONFERENCE);
				}
			});
		}
	}

	// actual request for joining a conference
	private void reqJoinConference() {
		ArrayList<Integer> userIdConferenceId = new ArrayList<Integer>();
		userIdConferenceId.add(currentUser.getId());
		userIdConferenceId.add(fc.getSelectedConferenceId());

		SocketEvent se = new SocketEvent("reqJoinConference", userIdConferenceId);
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent();
		String eventName = res.getName();
		if (eventName.equals("joinConferenceSucceeded")) {
			reqConferenceFeed();
			message = "Wysłano prośbę o udział w konferencji do jej organizatora.";
		} else {
			message = "Nie udało się dołączyć do konferencji.";
		}

		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(applicationWindow, message);
			}
		});
	}

	// sends request to leave conference after user confirms it
	@FXML public void leaveConferenceBtn() {
		Integer selectedConfId = fc.getSelectedConferenceId();
		if (selectedConfId != null) {
			String conferenceName = fc.getSelectedConference().getName();
			String message = "Czy na pewno chcesz zrezygnować z udziału w konferencji \"" + conferenceName + "\"?";
			Platform.runLater(new Runnable() {
				@Override public void run() {
					openConfirmationWindow(applicationWindow, message, RequestType.REQUEST_LEAVING_CONFERENCE);
				}
			});
		}
	}

	// actual request for leaving a conference
	private void reqLeaveConference() {
		ArrayList<Integer> userIdConferenceId = new ArrayList<Integer>();
		userIdConferenceId.add(currentUser.getId());
		userIdConferenceId.add(fc.getSelectedConferenceId());

		SocketEvent se = new SocketEvent("reqLeaveConference", userIdConferenceId);
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent();
		String eventName = res.getName();
		if (eventName.equals("leaveConferenceSucceeded")) {
			reqConferenceFeed();
			message = "Zrezygnowałeś z udziału w konferencji.";
		} else {
			message = "Nie udało się zrezygnować z udziału w konferencji.";
		}

		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(applicationWindow, message);
			}
		});
	}

	@FXML public void removeConferenceBtn() {
		Integer selectedConfId = fc.getSelectedConferenceId();
		if (selectedConfId != null) {
			String conferenceName = fc.getSelectedConference().getName();
			String message = "Czy na pewno chcesz usunąć konferencję \"" + conferenceName + "\"?";
			Platform.runLater(new Runnable() {
				@Override public void run() {
					openConfirmationWindow(applicationWindow, message, RequestType.REQUEST_REMOVING_CONFERENCE);
				}
			});
		}
	}

	// actual request for leaving a conference
	private void reqRemoveConference() {
		SocketEvent se = new SocketEvent("reqRemoveConference", fc.getSelectedConferenceId());
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent();
		String eventName = res.getName();
		if (eventName.equals("removeConferenceSucceeded")) {
			reqConferenceFeed();
			message = "Udało się usunąć konferencję.";
		} else {
			message = "Nie udało się usunąć konferencji.";
		}

		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(applicationWindow, message);
			}
		});
	}

	@FXML private void logoutButton() {
		// here check if login is valid
		new Thread(() -> logout()).start();
	}

	public void logout() {
		NetworkConnection.disconnect();
		fc.clear();
		Client.timer.cancel();
		Platform.runLater(new Runnable() {
			@Override public void run() {
				loadScene(applicationWindow, "view/LoginLayout.fxml", 320, 250, false, 0, 0);
			}
		});
	}

	@FXML public void addConferenceBtn() {
		openNewWindow(applicationWindow, "view/ConferenceCreatorLayout.fxml", 600, 650, false, "Dodaj konferencję");
	}

	public void changeMonthToNext() {
		calendar.setCalendarsDate(calendar.getCalendarsDate().plusMonths(1));
		calendar.refreshCalendarTable(calendarTable, calendar.getCalendarsDate(), fc.getFeed(), eventDetailsTP,
				listOfSelectedDaysEvents);
		updateComboBoxesAccordingToDate(calendar.getCalendarsDate());
	}

	public void changeMonthToPrevious() {
		calendar.setCalendarsDate(calendar.getCalendarsDate().minusMonths(1));
		calendar.refreshCalendarTable(calendarTable, calendar.getCalendarsDate(), fc.getFeed(), eventDetailsTP,
				listOfSelectedDaysEvents);
		updateComboBoxesAccordingToDate(calendar.getCalendarsDate());
	}

	public void changeMonthToChosen() {
		String polishMonth = monthsCB.getValue();
		String engShortMonth = calendar.PolishDateStringToEngDateString(polishMonth);
		try {
			Date date = new SimpleDateFormat("MMM", Locale.ENGLISH).parse(engShortMonth);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int month = cal.get(Calendar.MONTH) + 1; // months begin with 0
			calendar.setCalendarsDate(calendar.getCalendarsDate().withMonth(month));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		calendar.refreshCalendarTable(calendarTable, calendar.getCalendarsDate(), fc.getFeed(), eventDetailsTP,
				listOfSelectedDaysEvents);
	}

	public void changeYearToChosen() {
		int year = Integer.parseInt(yearsCB.getValue());
		calendar.setCalendarsDate(calendar.getCalendarsDate().withYear(year));
		calendar.refreshCalendarTable(calendarTable, calendar.getCalendarsDate(), fc.getFeed(), eventDetailsTP,
				listOfSelectedDaysEvents);
	}

	private void updateComboBoxesAccordingToDate(LocalDate givenDate) {
		// Set new ComboBox values:
		String currentDateInPolish = CalendarController.localDateToPolishDateString(givenDate);
		monthsCB.setValue(currentDateInPolish.substring(0, currentDateInPolish.indexOf(" ")));
		yearsCB.setValue(currentDateInPolish.substring(currentDateInPolish.indexOf(" ") + 1));
	}
}