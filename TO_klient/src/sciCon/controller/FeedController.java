package sciCon.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import sciCon.model.Conference;
import sciCon.model.Controller;

public class FeedController implements Controller {

	private Integer selectedConferenceId = null;
	private ArrayList<Conference> feed = new ArrayList<Conference>();
	private HashMap<Integer, Tab> openedTabsConferencesIds = new HashMap<Integer, Tab>();

	public ArrayList<Conference> getFeed() {
		return feed;
	}

	public void setFeed(ArrayList<Conference> feed) {
		System.out.println("ustawiono feed");
		this.feed = feed;
	}

	public Conference getSelectedConference() {
		if (selectedConferenceId != null) {
			return feed.stream().filter(c -> c.getId() == selectedConferenceId).findFirst().get();
		} else {
			return null;
		}
	}

	public void setSelectedConferenceId(Integer selectedConferenceId) {
		this.selectedConferenceId = selectedConferenceId;
	}

	public Integer getSelectedConferenceId() {
		return selectedConferenceId;
	}

	public ArrayList<Conference> filterFeed(ArrayList<Conference> feed, ConferenceFilter cf) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime now = LocalDateTime.now();
		now.format(formatter);
		ArrayList<Conference> filtered = new ArrayList<Conference>();
		// filtered.addAll(feed);
		switch (cf) {
			case PAST: {
				filtered = (ArrayList<Conference>) feed.stream().filter(c -> c.getEndTime().isBefore(now))
						.collect(Collectors.toList());
				// filtered.removeIf(s -> s.getStartTime().isAfter(now));
				break;
			}
			case FUTURE: {
				filtered = (ArrayList<Conference>) feed.stream().filter(c -> c.getStartTime().isAfter(now))
						.collect(Collectors.toList());
				// filtered.removeIf(s -> s.getEndTime().isBefore(now));
				break;
			}
			case ONGOING: {
				filtered = (ArrayList<Conference>) feed.stream()
						.filter(c -> c.getStartTime().isAfter(now) && c.getEndTime().isBefore(now))
						.collect(Collectors.toList());
				// filtered.removeIf(s -> s.getStartTime().isBefore(now) &&
				// s.getEndTime().isAfter(now));
				break;
			}
			case ALL: {
				filtered.addAll(feed);
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
		listOfSelectedDaysEvents.getItems().clear();
		for (Conference c : feed) {
			if (c.getStartTime().toLocalDate().equals(selectedDate)) {
				selectedDayConferences.add(c);
			}
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
			Integer currId = c.getId();
			label = new Label(addNLsIfTooLong(title, charLimit));
			label.setFont(Font.font("Inconsolata", 13));

			label.setId(currId.toString());
			label.setPrefWidth(lv.getWidth());
			label.setOnMouseClicked(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent t) {
					setSelectedConferenceId(currId);
					openConferenceTab(tp, cs);
				}
			});
			ol.add(label);
		}
		lv.setItems(ol);
	}

	public void resizeConferenceTabs(TabPane tp, Integer size) {
		for (Tab t : tp.getTabs()) {
			VBox vb = (VBox) t.getContent();
			ScrollPane confInfo = (ScrollPane) vb.getChildren().get(0);
			confInfo.setPrefHeight(size);
		}
	}

	private ScrollPane createConfDescriptionScrollPane(Conference c, double prefTabHeight) {
		// TextFlow is built from many Text objects (which can have different
		// styles)
		TextFlow flow = new TextFlow();
		flow.setPrefHeight(prefTabHeight);

		ArrayList<Text> confDescriptionSections = new ArrayList<Text>(); // e.g.
																			// "Tytuł",
																			// "Organizatorzy"

		// Styles:
		String sectionNameStyle = new String("-fx-font-weight:bold;"), // For
																		// "Tytuł",
																		// "Organizatorzy"
																		// and
																		// the
																		// rest
				sectionContentStyle = new String(); // For content (text of
													// description etc.)

		String[] sectionNames = new String[] { "Temat:\n", "\n\nOrganizatorzy:\n", "\nCzas rozpoczęcia:\n",
				"\n\nCzas zakończenia:\n", "\n\nMiejsce:\n", "\n\nPlan:\n", "\n\nOpis:\n",
				"\n\nUczestnicy: (wg. roli)\n" };

		String[] sectionContents = new String[] { c.getSubject(), Conference.userListToStr(c.getOrganizers()),
				c.getStartTime().toString().replace("T", ", godz. "),
				c.getEndTime().toString().replace("T", ", godz. "), c.getPlace(), c.getAgenda(), c.getDescription(),
				c.getAllParticipantsListStr() };

		for (int i = 0; i < sectionContents.length; ++i) {
			// Label/section name:
			Text currentSectionTitle = new Text(sectionNames[i]);
			currentSectionTitle.setStyle(sectionNameStyle);
			confDescriptionSections.add(currentSectionTitle);

			// Content:
			Text currentSectionContent = new Text(sectionContents[i]);
			currentSectionContent.setStyle(sectionContentStyle);
			confDescriptionSections.add(currentSectionContent);
		}

		ScrollPane scPane = new ScrollPane(flow);
		scPane.setFitToWidth(true);

		flow.getChildren().addAll(confDescriptionSections);
		flow.setStyle("-fx-padding: 10 10 10 10;");
		return scPane;
	}
	
	public void refreshConferenceTabs(TabPane tp, ArrayList<Conference> confPool) {
		try {
			for (Iterator<Tab> iterator = tp.getTabs().iterator(); iterator.hasNext();) {
				Tab t = iterator.next();
				try {
					Conference c = confPool.stream().filter(conf -> conf.getId() == Integer.parseInt(t.getId()))
							.findFirst().get();
					VBox vbox = new VBox();
					ScrollPane scPane = createConfDescriptionScrollPane(c, tp.getHeight()/2);
					Platform.runLater(new Runnable() {
						@Override public void run() {
							vbox.getChildren().add(scPane);
							t.setContent(vbox);
						}
					});
				} catch (NoSuchElementException e) {
					// if there's no such conference found remove its tab
					openedTabsConferencesIds.remove(Integer.parseInt(t.getId()));
					iterator.remove();
				}
			}
		} catch (ConcurrentModificationException e) {
			// happens when there is only one tab opened
			// and organizer removes their conference
			// so the tab closes and leaves nothing opened
			setSelectedConferenceId(null);
		}
	}

	public void openConferenceTab(TabPane tp, ArrayList<Conference> confPool) {

		Integer currId = getSelectedConferenceId();

		if (!openedTabsConferencesIds.containsKey(currId)) {
			for (Conference c : confPool) {
				if (c.getId() == currId) {
					Tab tab = new Tab();
					tab.setOnClosed(new EventHandler<Event>() {
						@Override public void handle(Event event) {
							Integer id = Integer.parseInt(tab.getId());
							openedTabsConferencesIds.remove(id);
						}
					});
					tab.setText(c.getName());
					tab.setId(currId.toString());
					VBox vbox = new VBox();

					ScrollPane scPane = createConfDescriptionScrollPane(c, tp.getHeight()/2);

					// VBOx is redundant only theoretically, the full hierarchy
					// is:
					// Tab[ VBox[ ScrollPane[ TextFlow[ Text, Text, Text, ... ]
					// ] ] ]
					vbox.getChildren().add(scPane);

					tab.setContent(vbox);
					tp.getTabs().add(tab);
					openedTabsConferencesIds.put(currId, tab);
					tp.getSelectionModel().select(tab);
					break;
				}
			}
		} else {
			tp.getSelectionModel().select(openedTabsConferencesIds.get(currId));
		}
	}
}
