package sciCon;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import sciCon.model.Conference;
import sciCon.model.DbConnection;
import sciCon.model.Paper;
import sciCon.model.Post;
import sciCon.model.SocketEvent;
import sciCon.model.User;
import sciCon.model.User.UsersRole;
import sciCon.model.Validator;

public class SciConServer implements Runnable {

	private ServerSocket listener;
	public HashMap<Integer, User> loggedUsers = new HashMap<Integer, User>();
	private int port;

	public SciConServer(int p) {
		port = p;
	}

	@Override public void run() {
		try {
			startServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startServer() throws IOException {
		listener = new ServerSocket(port);
		try {
			while (true) {
				System.out.println("Starting server...");
				ServerImplementation ssi = new ServerImplementation(listener.accept());
				Thread thread = new Thread(ssi);
				// run server implementation in a new thread
				thread.start();
				System.out.println("Server started.");
			}
		} finally {
			listener.close();
		}
	}

	private class ServerImplementation implements Runnable, Validator {
		private Socket s;
		private ObjectInputStream objIn;
		private ObjectOutputStream objOut;
		private DbConnection dbConn;
		private User loggedUser = null;

		public ServerImplementation(Socket socket) {
			// connect to database
			dbConn = new DbConnection("todb", "todb", "todb");

			// define socket
			s = socket;
			try {
				// get input and output streams from socket
				objIn = new ObjectInputStream(s.getInputStream());
				objOut = new ObjectOutputStream(s.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void handleRegistration(User u) {
			SocketEvent se = null;

			int validationCode = isUserValid(u); // 0 - login is valid
			String socketEvtName = "registerFailed";
			String message = "";

			if (dbConn.doesUserExist(u.getLogin())) {
				message = "Login jest już w użyciu.";
			} else {

				message = interpretValidationCode(validationCode, "Zarejestrowano",
						"Login musi mieć od 3 do 30 znaków i składać się z liter, cyfr lub znaku \"_\".",
						"Hasło musi mieć od 6 do 40 znaków.", "Imię i nazwisko muszą mieć od 2 do 30 znaków.");

				if (validationCode == 0) {
					if (!dbConn.registerUser(u)) {
						message = "Rejestracja nie powiodła się. Błąd serwera.";
					} else {
						socketEvtName = "registerSucceeded";
					}
				}
			}

			se = new SocketEvent(socketEvtName, message);
			try {
				objOut.writeObject(se);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		private void handleUpdateProfile(User u, String password) {
			String socketEvtName = "updateProfileFailed";
			String message = "Edycja profilu nie powiodła się. Podane obecne hasło jest niepoprawne.";
			User fetchedUser = null;
			User resultingUser = null;
			fetchedUser = dbConn.getUser(u.getLogin(), password);
			if (fetchedUser != null) {
				if (u.getName() == null) {
					u.setName(fetchedUser.getName());
				}
				if (u.getSurname() == null) {
					u.setName(fetchedUser.getSurname());
				}
				if (u.getPassword() == null) {
					u.setPassword(password);
				}

				int validationCode = isUserValid(u);

				message = interpretValidationCode(validationCode, "Zaaktualizowano profil.",
						"Login musi mieć od 3 do 30 znaków i składać się z liter, cyfr lub znaku \"_\".",
						"Hasło musi mieć od 6 do 40 znaków.", "Imię i nazwisko muszą mieć od 2 do 30 znaków.",
						"Adres e-mail jest w niepoprawnym formacie. Musi składać się z maksymalnie 40 znaków - "
								+ "liter, cyfr lub \"_\", \".\", \"%\", \"+\", \"-\".",
						"Nazwa organizacji nie może zawierać więcej niż 100 znaków.");

				if (validationCode == 0) {
					resultingUser = dbConn.editUser(u);
					if (resultingUser == null) {
						message = "Edycja profilu nie powiodła się. Błąd serwera.";
					} else {
						message = "Edycja profilu powiodła się.";
						socketEvtName = "updateProfileSucceeded";
					}
				}
			}
			
			SocketEvent se = new SocketEvent(socketEvtName, message, resultingUser);
			try {
				objOut.writeObject(se);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

		private void handleAddConference(Conference c) {

			int validationCode = isConferenceValid(c);
			String socketEvtName = "addConferenceFailed";
			String message = "";
			SocketEvent se = null;

			message = interpretValidationCode(validationCode, "Dodano konferencję.",
					"Podaj czas rozpoczęcia późniejszy niż obecny o co najmniej godzinę.",
					"Konferencja nie może kończyć się wcześniej niż się zaczyna.",
					"Nazwa nie może być krótsza niż 3 i dłuższa niż 200 znaków.",
					"Temat nie może być krótszy niż 3 i dłuższy niż 200 znaków.",
					"Zawartość pola \"Miejsce\" nie może być krótsza niż 3 i dłuższy niż 250 znaków.",
					"Pole \"Plan\" nie może być puste.");

			if (validationCode == 0) { // if conference data is valid
				if (!dbConn.addConference(c)) {
					message = "Nie udało się dodać konferencji. Błąd serwera.";
				} else {
					socketEvtName = "addConferenceSucceeded";
				}
			}

			se = new SocketEvent(socketEvtName, message);

			try {
				objOut.writeObject(se);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void handleJoinConference(int userId, int conferenceId) {
			SocketEvent se = null;
			if (!dbConn.addParticipant(userId, conferenceId)) {
				se = new SocketEvent("joinConferenceFailed");
			} else {
				se = new SocketEvent("joinConferenceSucceeded");
			}

			try {
				objOut.writeObject(se);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void handleLeaveConference(int userId, int conferenceId) {
			SocketEvent se = null;
			if (!dbConn.removeParticipant(userId, conferenceId)) {
				se = new SocketEvent("leaveConferenceFailed");
			} else {
				se = new SocketEvent("leaveConferenceSucceeded");
			}

			try {
				objOut.writeObject(se);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void handleSetUsersRole(ArrayList<Integer> usersIds, UsersRole uR, Integer conferenceId) {
			SocketEvent se = null;
			if (uR == UsersRole.NONE) {
				if (dbConn.expellUsers(usersIds, conferenceId)) {
					Conference c = dbConn.fetchConference(conferenceId);
					se = new SocketEvent("expellSucceeded", c);
				} else {
					se = new SocketEvent("expellFailed");
				}

			} else {
				if (dbConn.updateUsersRoles(usersIds, uR, conferenceId)) {
					Conference c = dbConn.fetchConference(conferenceId);
					se = new SocketEvent("setRoleSucceeded", c);
				} else {
					se = new SocketEvent("setRoleFailed");
				}
			}
			try {
				objOut.writeObject(se);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void handleRemoveConference(int conferenceId) {
			SocketEvent se = null;
			if (!dbConn.removeConference(conferenceId)) {
				se = new SocketEvent("removeConferenceFailed");
			} else {
				se = new SocketEvent("removeConferenceSucceeded");
			}

			try {
				objOut.writeObject(se);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void handleLogin(User u) {
			SocketEvent se = null;
			// check if user with received login is in the DB
			User fetchedUser = dbConn.getUser(u.getLogin(), u.getPassword());
			if (fetchedUser != null) {
				// send back user data (without password),
				se = new SocketEvent("loginSucceeded");

				// register user in server's memory (hashmap) and client-server
				// process memory
				loggedUser = fetchedUser;
				loggedUsers.put(fetchedUser.getId(), fetchedUser);
			} else {
				se = new SocketEvent("loginFailed");
			}
			try {
				objOut.writeObject(se);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		private void handleConferenceFeed() {
			ArrayList<Conference> conferenceFeed = dbConn.fetchConferenceFeed();
			SocketEvent se = null;

			// create SocketEvent w ArrayList arg
			se = new SocketEvent("updateConferenceFeed", conferenceFeed);
			try {
				objOut.writeObject(se);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		private void handleSendForumMessage(int userId, int conferenceId, String message) {
			SocketEvent se = null;
			if (!dbConn.addPost(userId, conferenceId, message)) {
				se = new SocketEvent("sendForumMessageFailed");
			} else {
				se = new SocketEvent("sendForumMessageSucceeded");
			}

			try {
				objOut.writeObject(se);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void handleRequestConferencesPosts(int userId, int conferenceId) {
			SocketEvent se = null;
			UsersRole fetchedRole = dbConn.checkUsersRole(userId, conferenceId);
			if (fetchedRole == UsersRole.NONE || fetchedRole == UsersRole.PENDING) {
				se = new SocketEvent("sendForumMessageFailed");
			} else {
				ArrayList<Post> posts = dbConn.fetchConferencesPosts(conferenceId);
				se = new SocketEvent("sendForumMessageSucceeded", posts);
			}

			try {
				objOut.writeObject(se);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void handleSendCurrentUser() {
			SocketEvent se = null;

			se = new SocketEvent("currentUserSucceeded", loggedUser);

			try {
				objOut.writeObject(se);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		@Override public void run() {
			try {
				SocketEvent se = null;

				while (true) {
					try {
						se = (SocketEvent) objIn.readObject();
					} catch (EOFException eof) {
						System.out.println("Somebody lost connection.");
						break;
					}

					// name tells server what to do
					String eventName = se.getName();
					switch (eventName) {
						case "fileSentToServer": {
							byte[] receivedPaper_rawData = se.getObject(byte[].class);

							Paper receivedPaper = new Paper();
							receivedPaper.createFromReceivedBytes(receivedPaper_rawData);
							// receivedPaper.saveAsFile("/home/dayve/Pulpit/TO_TEST_DESTINATION");

							dbConn.addFile(receivedPaper);
							break;
						}
						// login request
						case "reqLogin": {
							User u = (User) se.getObject(User.class);
							handleLogin(u);
							break;
						}
						case "reqRegister": {
							User u = se.getObject(User.class);
							handleRegistration(u);
							break;
						}

						case "reqUpdateProfile": {
							User u = se.getObject(User.class);
							String password = se.getObject(String.class);
							handleUpdateProfile(u, password);
							break;
						}
						case "reqConferenceFeed": {
							handleConferenceFeed();
							break;
						}
						case "reqAddConference": {
							Conference c = (Conference) se.getObject(Conference.class);
							handleAddConference(c);
							break;
						}
						case "reqJoinConference": {
							@SuppressWarnings("unchecked")
							ArrayList<Integer> userIdConferenceId = se.getObject(ArrayList.class);
							int userId = userIdConferenceId.get(0);
							int conferenceId = userIdConferenceId.get(1);
							handleJoinConference(userId, conferenceId);
							break;
						}

						case "reqLeaveConference": {
							@SuppressWarnings("unchecked")
							ArrayList<Integer> userIdConferenceId = se.getObject(ArrayList.class);
							int userId = userIdConferenceId.get(0);
							int conferenceId = userIdConferenceId.get(1);
							handleLeaveConference(userId, conferenceId);
							break;
						}

						case "reqRemoveConference": {
							Integer conferenceId = se.getObject(Integer.class);
							handleRemoveConference(conferenceId);
							break;
						}

						case "reqSendForumMessage": {
							@SuppressWarnings("unchecked")
							ArrayList<Integer> userIdConferenceId = se.getObject(ArrayList.class);
							String message = se.getObject(String.class);
							int userId = userIdConferenceId.get(0);
							int conferenceId = userIdConferenceId.get(1);
							handleSendForumMessage(userId, conferenceId, message);
							break;
						}

						case "reqConferencesPosts": {
							@SuppressWarnings("unchecked")
							ArrayList<Integer> userIdConferenceId = se.getObject(ArrayList.class);
							int userId = userIdConferenceId.get(0);
							int conferenceId = userIdConferenceId.get(1);
							handleRequestConferencesPosts(userId, conferenceId);
							break;
						}

						case "reqSetRole": {
							@SuppressWarnings("unchecked")
							ArrayList<Integer> usersIds = se.getObject(ArrayList.class);
							User.UsersRole role = se.getObject(UsersRole.class);
							Integer conferenceId = se.getObject(Integer.class);
							handleSetUsersRole(usersIds, role, conferenceId);
							break;
						}

						case "reqCurrentUser": {
							handleSendCurrentUser();
							break;
						}
						default:
							break;
					}
				}
			} catch (SocketException e) {
				System.out.println("Somebody just disconnected.");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					// remove user who was logged in from loggedUsers hashmap
					if (loggedUser != null) {
						loggedUsers.remove(loggedUser.getId());
					}
					s.close();
					objIn.close();
					objOut.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}