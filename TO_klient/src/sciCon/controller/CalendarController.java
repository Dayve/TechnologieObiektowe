package sciCon.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import sciCon.model.Conference;
import sciCon.model.Week;

public class CalendarController{
	
	public static void refreshCalendarTable(TableView<Week> calendarTable, Label currentlyChosenDateLabel, LocalDate calendarsDate, ArrayList<Conference> conferencesFeed, VBox listOfSelectedDaysEvents) {
		calendarTable.getItems().clear();
		calendarTable.getColumns().clear();
		fillCalendarTable(calendarTable, currentlyChosenDateLabel, calendarsDate, conferencesFeed, listOfSelectedDaysEvents);
	}
	

	public static void fillCalendarTable(TableView<Week> calendarTable, Label currentlyChosenDateLabel, LocalDate calendarsDate, ArrayList<Conference> conferencesFeed, VBox listOfSelectedDaysEvents) {     	
		// ColumnTitle are used only while displaying the content, 
		// PropertyValue however must be the same as variable names in Week class.
        String[] daysOfTheWeekColumnTitles = {"Pn", "Wt", "Śr", "Czw", "Pt", "Sb", "Nd"};
        String[] daysOfTheWeekPropertyValues = {"pn", "wt", "sr", "cz", "pt", "sb", "nd"};
        
        // List of columns:
        List<TableColumn<Week, String>> dayOfTheWeekColumns = new ArrayList<TableColumn<Week, String>>();
        
        // Setting the attributes for every column (identical as in the previous version of FXML file):
        for(int i=0; i<daysOfTheWeekColumnTitles.length; ++i) {
        	TableColumn<Week, String> col = new TableColumn<>(daysOfTheWeekColumnTitles[i]);
        	
        	// Column-wise cell factory:
        	col.setCellFactory( tableColumn -> {
        		TableCell<Week, String> cell = new TableCell<Week, String>() {
                    @Override
                    protected void updateItem(String item, boolean emptyCell) {
                        super.updateItem(item, emptyCell) ;
                        this.setText(emptyCell ? null : item);
    					
    					if (item == null || emptyCell) { // Skip checking cells, which are below the last week (row):
    						setStyle("");
    					} else {
    						// Mark not empty cell (skip those before 1 and after 28-31) if a conference is assigned to a given day:
		                	if(!item.isEmpty()) {
		                		if( isAnyConferenceAtDate(calendarsDate.withDayOfMonth(Integer.parseInt(item)), conferencesFeed) ) {
		                			setStyle("-fx-background-color: #b8b8b8");
		                		}
		                	}
    					}
                    }
                };
                
                // Handle action: left mouse button pressed:
                cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    public void handle(MouseEvent t) {
	                    if (!cell.isEmpty()) {
	                    	String cellsContent = cell.getItem();
	                    	
	                    	if(! cellsContent.isEmpty()) {                    		
	                    		LocalDate clickedDate = calendarsDate.withDayOfMonth(Integer.parseInt(cellsContent));
	                    		
	                    		if(isAnyConferenceAtDate(clickedDate, conferencesFeed)) {
	                    			// Perform an action after a day with assigned conference was clicked:
	                    			System.out.println("There is a conference on: " + clickedDate.toString());
	                    			fillVBoxWithSelectedDaysConferences(clickedDate, conferencesFeed, listOfSelectedDaysEvents);
	                    		}
	                    		else listOfSelectedDaysEvents.getChildren().clear();
	                    		
	                    		ConferenceCreatorController.setChosenDay(clickedDate);
	                    	}
	                    } 
                    }
                });
                
                return cell ;
        	});
        	
        	col.setMinWidth(10);
        	col.setPrefWidth(56);
        	col.setMaxWidth(200);
        	col.setResizable(false);
        	col.setSortable(false);
        	
        	col.setCellValueFactory(new PropertyValueFactory<>(daysOfTheWeekPropertyValues[i]));
        	dayOfTheWeekColumns.add(col);
        }
        
        // Filling the actual table:
        calendarTable.setItems(createThisMonthsWeeksRows(calendarsDate));
        calendarTable.getColumns().addAll(dayOfTheWeekColumns);
        
        // Change label: (this function is called whenever the month is changed, so should be the label)
        currentlyChosenDateLabel.setText(localDateToPolishDateString(calendarsDate));
	}
	
	
	private static void fillVBoxWithSelectedDaysConferences(LocalDate selectedDate, ArrayList<Conference> feed, VBox listOfSelectedDaysEvents) {
		ArrayList<Conference> selectedDayConferences = new ArrayList<Conference>();
		
		for(Conference c : feed) {
			if(c.getStartTime().equals(selectedDate)) selectedDayConferences.add(c);
		}

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if(selectedDayConferences != null) {
					int index = 0;
					listOfSelectedDaysEvents.getChildren().clear();
					//System.out.println("conferences count: " + cs.size());
					TitledPane tpane = null;
					for(Conference c : selectedDayConferences) {
						TextArea feed = new TextArea(c.toString());
						feed.setWrapText(true);
						feed.setEditable(false);
						tpane = new TitledPane();
						tpane.setText(ApplicationController.addNLsIfTooLong(c.getName(), ApplicationController.CHAR_LIMIT_IN_TITLEPANE));
						tpane.setContent(feed);
						tpane.setExpanded(false);
						
						listOfSelectedDaysEvents.getChildren().add(index, tpane);
						index++;
					}
					AnchorPane.setTopAnchor((Node)listOfSelectedDaysEvents, 0.0);
					AnchorPane.setBottomAnchor((Node)listOfSelectedDaysEvents, 0.0);
					AnchorPane.setLeftAnchor((Node)listOfSelectedDaysEvents, 0.0);
					AnchorPane.setRightAnchor((Node)listOfSelectedDaysEvents, 0.0);
				}
			}
		});
	}

	
	// Returns String containing: "<polish name of a month> <year>" for a given LocalDate:
	private static String localDateToPolishDateString(LocalDate givenDate) {
		String result = new String();
		
		switch(givenDate.getMonth()) {
			case JANUARY: result += "Styczeń "; break;
			case FEBRUARY: result += "Luty "; break;
			case MARCH: result += "Marzec "; break;
			case APRIL: result += "Kwiecień "; break;
			case MAY: result += "Maj "; break;
			case JUNE: result += "Czerwiec "; break;
			case JULY: result += "Lipiec "; break;
			case AUGUST: result += "Sierpień "; break;
			case SEPTEMBER: result += "Wrzesień "; break;
			case OCTOBER: result += "Październik "; break;
			case NOVEMBER: result += "Listopad "; break;
			case DECEMBER: result += "Grudzień "; break;
			default: result += "[Invalid month] "; break;
		}
		
		result += givenDate.getYear();
		
		return result;
	}
	
	
	// Returns true if there is a conference (one or more) assigned to a givenDate:
	private static boolean isAnyConferenceAtDate(LocalDate givenDate, ArrayList<Conference> conferencesFeed) {
		for(Conference d : conferencesFeed) {
			if(d.getStartTime().equals(givenDate)) {
				return true;
			}
		}
		return false;
	}
	
	
	// Generates day numbers (calendarTable rows - weeks) for the year and month currently stored in calendarsDate:
    private static ObservableList<Week> createThisMonthsWeeksRows(LocalDate calendarsDate){
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
