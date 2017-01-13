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
import sciCon.model.User.UsersRole;

public class FeedController {

	private Integer selectedConferenceId = null;
	private ArrayList<Conference> feed = new ArrayList<Conference>();
	private HashMap<Integer, Post> selectedConferencesPosts = new HashMap<Integer, Post>();
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
			
			String myConferencesStyle = "-fx-font-weight: bold;";
			switch (ApplicationController
					.usersRoleOnConference(ApplicationController.currentUser, c.getId())) {
				case PARTICIPANT:
					label.setStyle(myConferencesStyle);
					break;

				case ORGANIZER:
					label.setStyle(myConferencesStyle + "-fx-text-fill: #13366C;");
					break;

				case PRELECTOR:
					label.setStyle(myConferencesStyle + "-fx-text-fill: #A5BEE9;");
					break;

				case SPONSOR:
					label.setStyle(myConferencesStyle + "-fx-text-fill: #485E7F;");
					break;

				case PENDING:
					label.setStyle(myConferencesStyle + "-fx-text-fill: #A7A7A7;");
					break;

				case NONE:
//					label.setStyle("-fx-background-color: ;");
					break;
			}
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
		ArrayList<Post> postDifferentFromCurrent = new ArrayList<Post>();
		ArrayList<Integer> userIdConferenceId = new ArrayList<Integer>();
		userIdConferenceId.add(usersId);
		userIdConferenceId.add(conferencesId);
		SocketEvent se = new SocketEvent("reqConferencesPosts", userIdConferenceId);

		NetworkConnection.sendSocketEvent(se);
		SocketEvent res = NetworkConnection.rcvSocketEvent();
		String eventName = res.getName();
		if (eventName.equals("sendForumMessageSucceeded")) {
			forumsFeed = res.getObject(ArrayList.class);
			if (forumsFeed == null) {
				return null;
			}
			for (int j = forumsFeed.size() - 1; j >= 0; j--) {
				Post p = forumsFeed.get(j);
				if (selectedConferencesPosts.containsKey(p.getPostsId())) {
					if (!p.getContent().equals(selectedConferencesPosts.get(p.getPostsId()).getContent())) {
						postDifferentFromCurrent.add(p);
					} else {
						break;
					}
				} else {
					postDifferentFromCurrent.add(p);
				}
			}
		}
		for (Post p : postDifferentFromCurrent) {
			System.out.println(p);
		}
		return postDifferentFromCurrent;
	}

	private boolean updateForumsListViewWithPosts(ListView<TextFlow> lv, Conference c) {
		ArrayList<Post> newPosts = reqForumsFeed(ApplicationController.currentUser.getId(), c.getId());
		if (newPosts.size() > 0) {
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
			for (int j = newPosts.size() - 1; j >= 0; j--) {

				Post p = newPosts.get(j);
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
				TextFlow flow = new TextFlow(date, author, content);
				flow.setId(p.getPostsId().toString());
				selectedConferencesPosts.put(p.getPostsId(), p);
				flow.setPrefWidth(lv.getWidth());
				lv.getItems().add(flow);
			}
			lv.setStyle("-fx-padding: 10 10 10 10;");
			return true;
		} else {
			return false;
		}
	}

	private void updateConfDescriptionScrollPane(ScrollPane scPane, Conference c) {
		// TextFlow is built from many Text objects (which can have different
		// styles)
		TextFlow flow = new TextFlow();

		// e.g. "Tytuł", "Organizatorzy"
		ArrayList<Text> confDescriptionSections = new ArrayList<Text>();

		// Styles:

		// For "Tytuł", "Organizatorzy" and the rest
		String sectionNameStyle = new String("-fx-font-weight:bold;"),

				// For content (text of description etc.)
				sectionContentStyle = new String();

		String[] sectionNames = new String[] { "Temat:\n", "\n\nOrganizatorzy:\n", "\nCzas rozpoczęcia:\n",
				"\n\nCzas zakończenia:\n", "\n\nMiejsce:\n", "\n\nPlan:\n", "\n\nOpis:\n",
				"\n\nUczestnicy: (wg roli)\n" };

		String[] sectionContents = new String[] { c.getSubject(), c.getOrganizersDescription(),
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
		
		flow.getChildren().addAll(confDescriptionSections);
		flow.setPrefWidth(scPane.getWidth());
		flow.setStyle("-fx-padding: 10 10 10 10;");
		scPane.setContent(flow);
	}

	public void refreshConferenceTab(TabPane tp, Integer tabsId, ArrayList<Conference> confPool) {
//		int tabsId = Integer.parseInt(t.getId());
		Conference c = null;
		// tabsId could be null if ApplicationController tried to refresh forum
		// but there weren't any tabs selected
		if (tabsId == null) {
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
			tp.getTabs().remove(openedTabsConferencesIds.get(tabsId));
			openedTabsConferencesIds.remove(tabsId);
			
		} else {

			VBox vb = (VBox) openedTabsConferencesIds.get(tabsId).getContent();
			if (vb.getChildren().size() >= 1) {
				ScrollPane confInfoPane = (ScrollPane) vb.getChildren().get(0);
				updateConfDescriptionScrollPane(confInfoPane, c);
			}
			if (vb.getChildren().size() == 2) {
				ListView<TextFlow> forumsListView = (ListView<TextFlow>) vb.getChildren().get(1);
				if(updateForumsListViewWithPosts(forumsListView, c)) { // update and check if it succeeded
					forumsListView.scrollTo(forumsListView.getItems().size()); // scroll to the last msg
				};
			}
		}
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
					ScrollPane descriptionPane = new ScrollPane();
					updateConfDescriptionScrollPane(descriptionPane, c);
					ListView<TextFlow> forumPane;
					double paneSize = tp.getHeight();
					UsersRole currUsersRole = ApplicationController
							.usersRoleOnConference(ApplicationController.currentUser, c.getId());
					if(currUsersRole != UsersRole.NONE && currUsersRole != UsersRole.PENDING) {
						paneSize /= 2;
						forumPane = new ListView<TextFlow>();
						updateForumsListViewWithPosts(forumPane, c);
						forumPane.setPrefHeight(paneSize);
						forumPane.scrollTo(forumPane.getItems().size());
						vbox.getChildren().add(forumPane);
					}
					
					descriptionPane.setPrefHeight(paneSize);
					descriptionPane.setFitToWidth(true);
					vbox.getChildren().add(0, descriptionPane);
					
					
					// VBOx is redundant only theoretically, the full hierarchy
					// is:
					// Tab[ VBox[ ScrollPane[ TextFlow[ Text, Text, Text, ... ]]]]
					
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
