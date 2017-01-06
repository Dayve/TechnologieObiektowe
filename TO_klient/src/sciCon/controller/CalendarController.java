package sciCon.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import sciCon.model.Conference;
import sciCon.model.Controller;
import sciCon.model.Week;

public class CalendarController implements Controller {
	private FeedController feedController;
	private LocalDate selectedDate; // currently selected (clicked) date

	public CalendarController(FeedController fc) {
		this.feedController = fc;
	}

	public void setCalendarsDate(LocalDate calendarsDate) {
		this.selectedDate = calendarsDate;
	}

	public LocalDate getCalendarsDate() {
		return selectedDate;
	}

	public void refreshCalendarTable(TableView<Week> calendarTable,
			LocalDate calendarsDate, ArrayList<Conference> conferencesFeed, TabPane tp,
			ListView<Label> listOfSelectedDaysEvents) {
		calendarTable.getItems().clear();
		calendarTable.getColumns().clear();
		fillCalendarTable(calendarTable, conferencesFeed, tp, listOfSelectedDaysEvents);
	}

	public void fillCalendarTable(TableView<Week> calendarTable,
			ArrayList<Conference> conferencesFeed, TabPane tp, ListView<Label> listOfSelectedDaysEvents) {
		// ColumnTitle are used only while displaying the content,
		// PropertyValue however must be the same as variable names in Week
		// class.
		String[] daysOfTheWeekColumnTitles = { "Pn", "Wt", "Śr", "Czw", "Pt", "Sb", "Nd" };
		String[] daysOfTheWeekPropertyValues = { "pn", "wt", "sr", "cz", "pt", "sb", "nd" };

		// List of columns:
		List<TableColumn<Week, String>> dayOfTheWeekColumns = new ArrayList<TableColumn<Week, String>>();

		// Setting the attributes for every column (identical as in the previous
		// version of FXML file):
		for (int i = 0; i < daysOfTheWeekColumnTitles.length; ++i) {
			TableColumn<Week, String> col = new TableColumn<>(daysOfTheWeekColumnTitles[i]);

			col.setMinWidth(10);
			col.setPrefWidth(55);
			col.setMaxWidth(200);
			col.setResizable(false);
			col.setSortable(false);

			String defaultCellSettings = "-fx-alignment: CENTER; -fx-font-size: 14pt;",
					participantMarker = "-fx-background-color: blue;",
					organizerMarker = "-fx-background-color: yellow;", prelectorMarker = "-fx-background-color: gray;",
					sponsorMarker = "-fx-background-color: pink;", pendingMarker = "-fx-background-color: red;",
					noneYetMarker = "-fx-background-color: green;";

			// Column-wise cell factory:
			col.setCellFactory(tableColumn -> {
				TableCell<Week, String> cell = new TableCell<Week, String>() {
					@Override protected void updateItem(String item, boolean emptyCell) {
						super.updateItem(item, emptyCell);
						this.setText(emptyCell ? null : item);

						setStyle(defaultCellSettings);

						if (item != null || !emptyCell) {
							if (!item.isEmpty()) {
								ArrayList<Conference> thisDayConferences = getConferencesAtDate(
										selectedDate.withDayOfMonth(Integer.parseInt(item)), conferencesFeed);
								if (!thisDayConferences.isEmpty()) {
									for (Conference c : thisDayConferences) {
										// If you have some role in more than
										// one conference in that day, style
										// will be overwritten
										// (in thisDayConferences array order)

										switch (ApplicationController
												.usersRoleOnConference(ApplicationController.currentUser, c)) {
											// If you have two roles, style will
											// be overwritten in this order:
											case PARTICIPANT:
												setStyle(defaultCellSettings + " " + participantMarker);
												break;

											case ORGANIZER:
												setStyle(defaultCellSettings + " " + organizerMarker);
												break;

											case PRELECTOR:
												setStyle(defaultCellSettings + " " + prelectorMarker);
												break;

											case SPONSOR:
												setStyle(defaultCellSettings + " " + sponsorMarker);
												break;

											case PENDING:
												setStyle(defaultCellSettings + " " + pendingMarker);
												break;

											case NONE:
												setStyle(defaultCellSettings + " " + noneYetMarker);
												break;
										}
									}
								}
							}
						}
					}
				};

				cell.setPrefHeight(col.getWidth());

				// Handle action: left mouse button pressed:
				cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent t) {
						if (!cell.isEmpty()) {
							String cellsContent = cell.getItem();

							if (!cellsContent.isEmpty()) {
								selectedDate = selectedDate.withDayOfMonth(Integer.parseInt(cellsContent));

								if (isAnyConferenceAtDate(selectedDate, conferencesFeed)) {
									// Perform an action after a day with
									// assigned conference was clicked:
									feedController.fillListViewWithSelectedDaysConferences(selectedDate,
											conferencesFeed, tp, listOfSelectedDaysEvents, false);
								} else {
									listOfSelectedDaysEvents.getItems().clear();
								}
								ConferenceCreatorController.setChosenDay(selectedDate);
							}
						}
					}
				});

				return cell;
			});

			col.setCellValueFactory(new PropertyValueFactory<>(daysOfTheWeekPropertyValues[i]));
			dayOfTheWeekColumns.add(col);
		}

		// Filling the actual table:
		calendarTable.setItems(createThisMonthsWeeksRows(selectedDate));
		calendarTable.getColumns().addAll(dayOfTheWeekColumns);
	}

	// Returns String containing: "<polish name of a month> <year>" for a given
	// LocalDate:
	static String localDateToPolishDateString(LocalDate givenDate) {
		String result = new String();

		switch (givenDate.getMonth()) {
			case JANUARY:
				result += "Styczeń ";
				break;
			case FEBRUARY:
				result += "Luty ";
				break;
			case MARCH:
				result += "Marzec ";
				break;
			case APRIL:
				result += "Kwiecień ";
				break;
			case MAY:
				result += "Maj ";
				break;
			case JUNE:
				result += "Czerwiec ";
				break;
			case JULY:
				result += "Lipiec ";
				break;
			case AUGUST:
				result += "Sierpień ";
				break;
			case SEPTEMBER:
				result += "Wrzesień ";
				break;
			case OCTOBER:
				result += "Październik ";
				break;
			case NOVEMBER:
				result += "Listopad ";
				break;
			case DECEMBER:
				result += "Grudzień ";
				break;
			default:
				result += "[Invalid month] ";
				break;
		}

		result += givenDate.getYear();

		return result;
	}

	public String PolishDateStringToEngDateString(String givenDate) {
		String result = new String();

		switch (givenDate) {
			case "Styczeń":
				result += "Jan";
				break;
			case "Luty":
				result += "Feb";
				break;
			case "Marzec":
				result += "Mar";
				break;
			case "Kwiecień":
				result += "Apr";
				break;
			case "Maj":
				result += "May";
				break;
			case "Czerwiec":
				result += "Jun";
				break;
			case "Lipiec":
				result += "Jul";
				break;
			case "Sierpień":
				result += "Aug";
				break;
			case "Wrzesień":
				result += "Sep";
				break;
			case "Październik":
				result += "Oct";
				break;
			case "Listopad":
				result += "Nov";
				break;
			case "Grudzień":
				result += "Dec";
				break;
		}
		return result;
	}

	private static ArrayList<Conference> getConferencesAtDate(LocalDate givenDate,
			ArrayList<Conference> conferencesFeed) {
		ArrayList<Conference> results = new ArrayList<Conference>();

		for (Conference d : conferencesFeed) {
			if (d.getStartTime().toLocalDate().equals(givenDate)) {
				results.add(d);
			}
		}
		return results;
	}

	// Returns true if there is a conference (one or more) assigned to a
	// givenDate:
	private static boolean isAnyConferenceAtDate(LocalDate givenDate, ArrayList<Conference> conferencesFeed) {
		for (Conference d : conferencesFeed) {
			if (d.getStartTime().toLocalDate().equals(givenDate)) {
				return true;
			}
		}
		return false;
	}

	// Generates day numbers (calendarTable rows - weeks) for the year and month
	// currently stored in calendarsDate:
	private static ObservableList<Week> createThisMonthsWeeksRows(LocalDate calendarsDate) {
		ObservableList<Week> weeksInAMonth = FXCollections.observableArrayList();

		int nthDayOfWeekMonthStartsAt = calendarsDate.withDayOfMonth(1).getDayOfWeek().getValue();
		List<String> currentInitializers = new ArrayList<String>();
		Integer dayOfTheMonth = 1;
		boolean firstWeekIteration = true;

		while (dayOfTheMonth <= calendarsDate.lengthOfMonth()) {
			currentInitializers.clear();

			for (int d = 1; d <= 7; ++d) {
				if ((d < nthDayOfWeekMonthStartsAt && firstWeekIteration)
						|| dayOfTheMonth > calendarsDate.lengthOfMonth()) {
					// Empty labels in days of the week before current month's
					// first day and after the last one:
					currentInitializers.add("");
				} else {
					currentInitializers.add(dayOfTheMonth.toString());
					dayOfTheMonth++;
				}
			}

			weeksInAMonth.add(new Week(currentInitializers));
			if (firstWeekIteration)
				firstWeekIteration = false;
		}

		return weeksInAMonth;
	}
}