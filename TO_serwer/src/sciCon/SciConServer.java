package sciCon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import sciCon.model.Conference;
import sciCon.model.DbConnection;
import sciCon.model.SocketEvent;
import sciCon.model.User;
import sciCon.model.Validator;

public class SciConServer implements Runnable {
	private ServerSocket listener;
	private int port;

	public SciConServer(int p) {
		port = p;
	}

	@Override
	public void run() {
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
			SocketEvent e = null;
			
			int validationCode = isUserValid(u); // 0 - login is valid
			String socketEvtName = "registerFailed";
			String message = "";
			
			if (dbConn.doesUserExist(u.getLogin())) {
				message = "Login jest już w użyciu.";
			} else {
				
				message = interpretValidationCode(validationCode,
						"Zarejestrowano",
						"Login musi mieć co najmniej 3 znaki i składać się z liter, cyfr lub znaku \"_\".",
						"Hasło musi mieć co najmniej 6 znaków.",
						"Imię i nazwisko muszą mieć co najmniej po 2 znaki.");
				
				if (validationCode == 0) {
					if(!dbConn.registerUser(u)) {
						message = "Rejestracja nie powiodła się. Błąd serwera.";
					} else {
						socketEvtName = "registerSucceeded";
					}
				}
			}
			
			e = new SocketEvent(socketEvtName, message);
			try {
				objOut.writeObject(e);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

private void handleAddConference(Conference c) {
			
			int validationCode = isConferenceValid(c);
			String socketEvtName = "addConferenceFailed";
			String message = "";
			SocketEvent e = null;
		
			message = interpretValidationCode(validationCode,
					"Dodano konferencję.",
					"Należy wypełnić wszystkie pola z godziną.",
					"Podaj czas rozpoczęcia późniejszy niż obecny.",
					"Konferencja nie może kończyć się wcześniej niż się zaczyna.",
					"Niepoprawnie wprowadzone dane. Nazwa i miejsce: od 3 do 60 znaków. "
					+ "Temat: od 3 do 40 znaków. Plan nie może być pusty.");
		
			if (validationCode == 0) { // if conference data is valid
				if (!dbConn.addConference(c)) {
					message = "Nie udało się dodać konferencji. Błąd serwera.";
				} else {
					socketEvtName = "addConferenceSucceeded";
				}
			}
			
			e = new SocketEvent(socketEvtName, message);
			
			try {
				objOut.writeObject(e);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	
		private void handleLogin(User u) {
			SocketEvent e = null;
			int userId = dbConn.doLoginAndPasswordMatch(u.getLogin(), u.getPassword());
			if (userId > -1) {
				e = new SocketEvent("loginSucceeded", userId);
			} else {
				e = new SocketEvent("loginFailed");
			}
			try {
				objOut.writeObject(e);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		private void handleConferenceFeed(boolean past) {
			ArrayList<Conference> conferenceFeed = dbConn.fetchConferenceFeed(past);
			SocketEvent e = null;
			
			// create SocketEvent w ArrayList arg
			e = new SocketEvent("fetchConferenceFeed", conferenceFeed);
			try {
				objOut.writeObject(e);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				SocketEvent e = null;

				while (true) {
					e = (SocketEvent) objIn.readObject();
					// name tells server what to do
					String eventName = e.getName();
					switch (eventName) {
					// login request
					case "reqLogin": {
						User u = (User) e.getObject(User.class);
						handleLogin(u);
						break;
					}
					case "reqRegister": {
						User u = e.getObject(User.class);
						handleRegistration(u);
						break;
					}
					case "reqConferenceFeed": {
						Boolean past = false;
						past = Boolean.valueOf(e.getObject(Boolean.class));
						handleConferenceFeed(past);
						break;
					}
					case "reqAddConference": {
						Conference c = (Conference) e.getObject(Conference.class);
						handleAddConference(c);
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
