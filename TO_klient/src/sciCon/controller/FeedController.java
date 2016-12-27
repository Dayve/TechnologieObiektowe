package sciCon.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import sciCon.model.Conference;
import sciCon.model.Controller.ConferenceFilter;

public class FeedController {
	
	private Integer selectedConferenceId = null;
	private HashMap<Integer, Tab> openedTabsConferencesIds = new HashMap<Integer, Tab>();

	public void setSelectedConferenceId(Integer selectedConferenceId) {
		this.selectedConferenceId = selectedConferenceId;
	}
	
	public Integer getSelectedConferenceId() {
		return selectedConferenceId;
	}

	public String addNLsIfTooLong(String givenString, int limit) {
		String[] separateWords = givenString.split("\\s+");
		String result = new String();
		int howMuchCharsSoFar = 0;

		for (int i = 0; i < separateWords.length; ++i) {
			howMuchCharsSoFar += separateWords[i].length() + 1; // +1 because we
																// assume that
																// every word
																// has a space
																// at the end

			if (howMuchCharsSoFar > limit) {
				result += "\n";
				howMuchCharsSoFar = 0;
			}
			result += separateWords[i] + " ";
		}

		return result.substring(0, result.length() - 1);
	}

	public ArrayList<Conference> filterFeed(ArrayList<Conference> feed, ConferenceFilter cf) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime now = LocalDateTime.now();
		now.format(formatter);
		ArrayList<Conference> filtered = new ArrayList<Conference>();
		filtered.addAll(feed);
		switch (cf) {
		case PAST: {
			filtered.removeIf(s -> s.getStartTime().isAfter(now));
			break;
		}
		case FUTURE: {
			filtered.removeIf(s -> s.getEndTime().isBefore(now));
			break;
		}
		case ONGOING: {
			filtered.removeIf(s -> s.getStartTime().isBefore(now) && s.getEndTime().isAfter(now));
			break;
		}
		case ALL: {
			break;
		}

		default:
			break;
		}
		return filtered;
	}

	public void fillListViewWithSelectedDaysConferences(LocalDate selectedDate, ArrayList<Conference> feed, TabPane tp,
			ListView<Label> listOfSelectedDaysEvents, boolean showDate) {
		ArrayList<Conference> selectedDayConferences = new ArrayList<Conference>();

		for (Conference c : feed) {
			if (c.getStartTime().toLocalDate().equals(selectedDate))
				selectedDayConferences.add(c);
		}
		if (selectedDayConferences != null) {
			fillListWithLabels(listOfSelectedDaysEvents, selectedDayConferences, tp, ConferenceFilter.ALL,
					ApplicationController.CHAR_LIMIT_IN_TITLEPANE, showDate);
		}
	}

	public void fillListWithLabels(ListView<Label> lv, ArrayList<Conference> cs, TabPane tp, ConferenceFilter cf,
			int charLimit, boolean showDate) {
		ArrayList<Conference> filtered = filterFeed(cs, cf);
		Collections.sort(filtered, Conference.confDateComparator);
		ObservableList<Label> ol = FXCollections.observableArrayList();
		lv.getItems().clear();
		Label label = null;
		for (Conference c : filtered) {
			String title = c.getName();
			if (showDate) {
				title += " (" + c.getDate() + ")";
			}
			label = new Label(addNLsIfTooLong(title, charLimit));
			label.setFont(Font.font("Inconsolata", 13));
			label.setOnMouseClicked(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent t) {
					setSelectedConferenceId(c.getId());
					openConferenceTab(tp, cs);
				}
			});
			label.setPrefWidth(lv.getWidth());
			ol.add(label);
		}
		lv.setItems(ol);
	}
	
	public void resizeConferenceTabs(TabPane tp, Integer size) {
		for(Tab t: tp.getTabs()) {
			VBox vb = (VBox) t.getContent();
			TextArea confInfo = (TextArea) vb.getChildren().get(0);
			confInfo.setPrefHeight(size);
		}
	}
	
	public void refreshConferenceTabs(TabPane tp, ArrayList<Conference> confPool) {
		for(Tab t: tp.getTabs()) {
			Conference conf = confPool.stream().filter(c -> c.getId() == Integer.parseInt(t.getId())).findFirst().get();
			
			VBox vbox = new VBox();
			TextArea confInfo = new TextArea(conf.toString());
			confInfo.setPrefHeight(tp.getHeight()/2);
			confInfo.setWrapText(true);
			confInfo.setEditable(false);
			
			vbox.getChildren().add(confInfo);
			t.setContent(vbox);
		}
		
	}
	public void openConferenceTab(TabPane tp, ArrayList<Conference> confPool) {
		Tab tab = new Tab();
		Integer currId = getSelectedConferenceId();
		tab.setOnClosed(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				Integer id = Integer.parseInt(tab.getId());
				openedTabsConferencesIds.remove(id);
			}
		});
		for (Conference c : confPool) {
			if (c.getId() == currId) {
				tab.setText(c.getName());
				tab.setId(currId.toString());
				VBox vbox = new VBox();
				TextArea confInfo = new TextArea(c.toString());
				confInfo.setPrefHeight(tp.getHeight()/2);
				confInfo.setWrapText(true);
				confInfo.setEditable(false);
				
				vbox.getChildren().add(confInfo);
				tab.setContent(vbox);
				if (!openedTabsConferencesIds.containsKey(currId)) {
					tp.getTabs().add(tab);
					tp.getSelectionModel().select(tab);
					openedTabsConferencesIds.put(currId, tab);
				} else {
					tp.getSelectionModel().select(openedTabsConferencesIds.get(currId));
				}
			}
		}
	}
}
