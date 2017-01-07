package sciCon.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import sciCon.model.Conference;
import sciCon.model.Controller.ConferenceFilter;
import sciCon.model.NetworkConnection;
import sciCon.model.Post;
import sciCon.model.SocketEvent;
import sciCon.model.User;

public class FeedController {

	private Integer selectedConferenceId = null;
	private ArrayList<Conference> feed = new ArrayList<Conference>();
	private HashMap<Integer, Tab> openedTabsConferencesIds = new HashMap<Integer, Tab>();

	public void clear() {
		feed.clear();
		openedTabsConferencesIds.clear();
		selectedConferenceId = null;
	}

	public ArrayList<Conference> getFeed() {
		return feed;
	}

	public void setFeed(ArrayList<Conference> feed) {
		this.feed = feed;
	}

	public Conference getSelectedConference() {
		if (selectedConferenceId != null) {
			return feed.stream().filter(c -> c.getId() == selectedConferenceId).findFirst().get();
		} else {
			return null;
		}
	}

	public Conference getConference(int id) {
		return feed.stream().filter(c -> c.getId() == id).findFirst().get();
	}

	public void setSelectedConferenceId(Integer selectedConferenceId) {
		this.selectedConferenceId = selectedConferenceId;
	}

	public Integer getSelectedConferenceId() {
		return selectedConferenceId;
	}

	public static String addNLsIfTooLong(String givenString, int limit) {
		String[] separateWords = givenString.split("\\s+");
		String result = new String();
		int howMuchCharsSoFar = 0;

		for (int i = 0; i < separateWords.length; ++i) {
			howMuchCharsSoFar += separateWords[i].length() + 1; // +1 because
			// we assume that every word has a space at the end

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
		switch (cf) {
			case PAST: {
				filtered = (ArrayList<Conference>) feed.stream().filter(c -> c.getEndTime().isBefore(now))
						.collect(Collectors.toList());
				break;
			}
			case FUTURE: {
				filtered = (ArrayList<Conference>) feed.stream().filter(c -> c.getStartTime().isAfter(now))
						.collect(Collectors.toList());
				break;
			}
			case ONGOING: {
				filtered = (ArrayList<Conference>) feed.stream()
						.filter(c -> c.getStartTime().isAfter(now) && c.getEndTime().isBefore(now))
						.collect(Collectors.toList());
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

	public void resizeSelectedConferenceTab(TabPane tp, Integer newHeight) {
		Tab t = tp.getSelectionModel().getSelectedItem();
		if (t != null) {
			VBox vb = (VBox) t.getContent();
			ObservableList<Node> children = vb.getChildren();
			newHeight /= children.size();
			for (Node child : children) {
				((Region) child).setPrefHeight(newHeight);
			}
		}
	}

	@SuppressWarnings("unchecked") private ArrayList<Post> reqForumsFeed(Integer usersId, Integer conferencesId) {
		ArrayList<Post> forumsFeed = null;
		ArrayList<Integer> userIdConferenceId = new ArrayList<Integer>();
		userIdConferenceId.add(usersId);
		userIdConferenceId.add(conferencesId);
		SocketEvent se = new SocketEvent("reqConferencesPosts", userIdConferenceId);

		NetworkConnection.sendSocketEvent(se);
		SocketEvent res = NetworkConnection.rcvSocketEvent();
		String eventName = res.getName();
		if (eventName.equals("sendForumMessageSucceeded")) {
			forumsFeed = res.getObject(ArrayList.class);
		}
		return forumsFeed;
	}

	private ListView<TextFlow> createForumsListView(Conference c, double prefForumsHeight) {
		ArrayList<Post> posts = reqForumsFeed(ApplicationController.currentUser.getId(), c.getId());
		ListView<TextFlow> lv = null;
		if (posts != null) {
			lv = new ListView<TextFlow>();
			TextFlow flow = new TextFlow();
			flow.setPrefHeight(prefForumsHeight);
			ArrayList<User> selectedConfUsersList = new ArrayList<User>();
			selectedConfUsersList.addAll(c.getOrganizers());
			selectedConfUsersList.addAll(c.getParticipantsList());
			Map<Integer, User> usersById = new HashMap<Integer, User>();
			for (User u : selectedConfUsersList) {
				usersById.put(u.getId(), u);
			}

			// Styles:
			String boldStyle = new String("-fx-font-weight:bold;"), // for the
																	// author's
																	// name
					regularStyle = new String(); // For content and date

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
			for (Post p : posts) {
				// add date and \n (regular font)
				Text date = new Text(p.getTime().format(formatter) + "\n");
				date.setStyle(regularStyle);

				User u = usersById.get(p.getAuthorsId());
				// add author's text (bold)
				Text author = new Text(u.getLogin() + ": ");
				author.setStyle(boldStyle);

				// add post's content (regular font)
				Text content = new Text(p.getContent());
				content.setStyle(regularStyle);
				lv.getItems().add(new TextFlow(date, author, content));
			}
			lv.setStyle("-fx-padding: 10 10 10 10;");
			lv.setPrefHeight(prefForumsHeight);
			lv.scrollTo(lv.getItems().size());
		}
		return lv;
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

	public void refreshConferenceTab(TabPane tp, Integer tabsId, ArrayList<Conference> confPool) {
//		int tabsId = Integer.parseInt(t.getId());
		Conference c = null;
		// tabsId could be null if ApplicationController tried to refresh forum
		// but there weren't any tabs selected
		if(tabsId == null) {
			return;
		}
		for (Conference fromPool : confPool) {
			if (fromPool.getId() == tabsId) {
				c = fromPool;
				break;
			}
		}
		if (c == null) {
			// if there's no such conference found remove its tab
			openedTabsConferencesIds.remove(tabsId);
			tp.getTabs().remove(tabsId);
		}
		
		VBox vbox = new VBox();
		double paneSize = tp.getHeight() / 2;
		boolean showForum = true;
		ListView<TextFlow> forumsListView = createForumsListView(c, paneSize);
		if (forumsListView == null) {
			paneSize = tp.getHeight();
			showForum = false;
		}
		ScrollPane scPane = createConfDescriptionScrollPane(c, paneSize);
		vbox.getChildren().add(scPane);
		if (showForum) {
			vbox.getChildren().add(forumsListView);
		}
		openedTabsConferencesIds.get(tabsId).setContent(vbox);
	}
	
	public void refreshConferenceTabs(TabPane tp, ArrayList<Conference> confPool) {
		try {
			for (Iterator<Tab> iterator = tp.getTabs().iterator(); iterator.hasNext();) {
				Tab t = iterator.next();
				refreshConferenceTab(tp, Integer.parseInt(t.getId()), confPool);
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
					double paneSize = tp.getHeight() / 2;
					boolean showForum = true;
					ListView<TextFlow> forumPane = createForumsListView(c, paneSize);
					if (forumPane == null) {
						paneSize = tp.getHeight();
						showForum = false;
					}
					ScrollPane scPane = createConfDescriptionScrollPane(c, paneSize);

					// VBOx is redundant only theoretically, the full hierarchy
					// is:
					// Tab[ VBox[ ScrollPane[ TextFlow[ Text, Text, Text, ... ]
					// ] ] ]
					vbox.getChildren().add(scPane);
					if (showForum) {
						vbox.getChildren().add(forumPane);
					}

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
