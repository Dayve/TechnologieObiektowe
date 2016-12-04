package sciCon.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import sciCon.Controllers;
import sciCon.model.Week;

public class ApplicationController implements Controllers {
	@FXML private TableView<Week> calendarTable; // A TableView representing the calendar
	@FXML private Button prevMonth;
	@FXML private Button nextMonth;
	
	private LocalDate calendarsDate; // It represents the currently selected (clicked) date
	
	@FXML
	public void initialize() {
		calendarsDate = LocalDate.now();
		fillCalendarTable();
	}
	
	public void refreshCalendarTable() {
		calendarTable.getItems().clear();
		calendarTable.getColumns().clear();
		fillCalendarTable();
	}
	
	public void changeMonthToNext() {
		calendarsDate = calendarsDate.plusMonths(1);
		refreshCalendarTable();
	}
	
	public void changeMonthToPrevious() {
		calendarsDate = calendarsDate.minusMonths(1);
		refreshCalendarTable();
	}
	
	public void fillCalendarTable() {     	
    	// ColumnTitle are used only while displaying the content, 
		// PropertyValue however must be the same as variable names in Week class.
        String[] daysOfTheWeekColumnTitles = {"Pn", "Wt", "Œr", "Czw", "Pt", "Sb", "Nd"};
        String[] daysOfTheWeekPropertyValues = {"pn", "wt", "sr", "cz", "pt", "sb", "nd"};
        
        // List of columns:
        List<TableColumn<Week, String>> dayOfTheWeekColumns = new ArrayList<TableColumn<Week, String>>();
        
        // Setting the attributes for every column (identical as in the previous version of FXML file):
        for(int i=0; i<daysOfTheWeekColumnTitles.length; ++i) {
        	TableColumn<Week, String> col = new TableColumn<>(daysOfTheWeekColumnTitles[i]);
        	
        	col.setMinWidth(10);
        	col.setPrefWidth(56);
        	col.setMaxWidth(5000);
        	col.setResizable(false);
        	col.setSortable(false);
        	
        	col.setCellValueFactory(new PropertyValueFactory<>(daysOfTheWeekPropertyValues[i]));
        	dayOfTheWeekColumns.add(col);
        }
        
        // Filling the actual table:
        calendarTable.setItems(createThisMonthsWeeksRows());
        calendarTable.getColumns().addAll(dayOfTheWeekColumns);
	}
	
	// Generates day numbers (calendarTable rows) for the year and month currently stored in calendarsDate:
    public ObservableList<Week> createThisMonthsWeeksRows(){
        ObservableList<Week> weeksInAMonth = FXCollections.observableArrayList();
        
        int nthDayOfWeekMonthStartsAt = calendarsDate.withDayOfMonth(1).getDayOfWeek().getValue();
        List<String> currentInitializers = new ArrayList<String>();
        Integer dayOfTheMonth = 1;
        boolean firstWeekIteration = true;
        
        while(dayOfTheMonth <= calendarsDate.lengthOfMonth()) {
        	currentInitializers.clear();
        	
        	for(int d=1 ; d <= 7 ; ++d) {
	    		if((d < nthDayOfWeekMonthStartsAt && firstWeekIteration) || dayOfTheMonth > calendarsDate.lengthOfMonth()) {
	    			 // Empty labels in days of the week before current month's first day and after the last one:
	    			currentInitializers.add("");
	    		}
	    		else {
	    			currentInitializers.add(dayOfTheMonth.toString());
	    			dayOfTheMonth++;
	    		}
	    	}

    		weeksInAMonth.add(new Week(currentInitializers));
    		if(firstWeekIteration) firstWeekIteration = false;
        }
        
        return weeksInAMonth;
    }
}
