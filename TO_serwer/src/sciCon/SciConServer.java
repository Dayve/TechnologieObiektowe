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
						"Login musi mieć co najmniej 3 znaki i składać się z liter, cyfr lub znaku \"_\".",
						"Hasło musi mieć co najmniej 6 znaków.", "Imię i nazwisko muszą mieć co najmniej po 2 znaki.");

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
		
		private void handleExpellUsers(ArrayList<Integer> usersIds, Integer conferenceId) {
			SocketEvent se = null;
			if (!dbConn.expellUsers(usersIds, conferenceId)) {
				se = new SocketEvent("expellUsersFailed");
			} else {
				se = new SocketEvent("expellUsersSucceeded");
			}
			try {
				objOut.writeObject(se);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void handleSetUsersRole(ArrayList<Integer> usersIds, UsersRole uR, Integer conferenceId) {
			SocketEvent se = null;
			if (!dbConn.updateUsersRoles(usersIds, uR, conferenceId)) {
				se = new SocketEvent("setRoleFailed");
			} else {
				Conference c = dbConn.fetchConference(conferenceId);
				se = new SocketEvent("setRoleSucceeded", c);
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

						case "reqSetRole": {
							@SuppressWarnings("unchecked")
							ArrayList<Integer> usersIds = se.getObject(ArrayList.class);
							User.UsersRole role = se.getObject(UsersRole.class);
							Integer conferenceId = se.getObject(Integer.class);
							handleSetUsersRole(usersIds, role, conferenceId);
							break;
						}

						case "reqExpellUsers": {
							@SuppressWarnings("unchecked")
							ArrayList<Integer> usersIds = se.getObject(ArrayList.class);
							Integer conferenceId = se.getObject(Integer.class);
							handleExpellUsers(usersIds, conferenceId);
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
