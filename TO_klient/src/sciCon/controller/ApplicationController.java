package sciCon.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import sciCon.Controllers;
import sciCon.model.Week;

public class ApplicationController implements Controllers {

	@FXML
	private Button prevMonth;
	@FXML
	private Button nextMonth;

	@FXML private TableView<Week> calendarTable; // A TableView representing the calendar
	@FXML private Label currentlyChosenDateLabel;
	private LocalDate calendarsDate; // It represents the currently selected (clicked) date
	
	@FXML
	public void initialize() {
		calendarsDate = LocalDate.now();
		CalendarController.fillCalendarTable(calendarTable, currentlyChosenDateLabel, calendarsDate);
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
		openNewWindow(event, "view/ConferenceCreatorLayout.fxml", 450, 650, false);
	}
}