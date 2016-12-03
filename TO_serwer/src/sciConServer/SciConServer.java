package sciConServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import sciCon.model.SocketEvent;
import sciCon.model.User;

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

	private class ServerImplementation implements Runnable {
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

			int validationCode = dbConn.isUserValid(u); // 0 - login is valid
			String message = "";

			if ((validationCode & 1) == 1) {
				if (message != "") {
					message += "\n";
				}
				message += "Login jest ju¿ w u¿yciu.";
			}

			if ((validationCode & 2) == 2) {
				if (message != "") {
					message += "\n";
				}
				message += "Login musi mieæ co najmniej 3 znaki i sk³adaæ siê z liter, cyfr lub znaku \"_\".";
			}

			if ((validationCode & 4) == 4) {
				if (message != "") {
					message += "\n";
				}
				message += "Has³o musi mieæ co najmniej 6 znaków.";
			}

			if ((validationCode & 8) == 8) {
				if (message != "") {
					message += "\n";
				}
				message += "Imiê i nazwisko musz¹ mieæ co najmniej po 2 znaki.";
			}

			if (validationCode == 0) { // if user data is valid
				if (dbConn.registerUser(u)) {
					message = "Zarejestrowano.";
					e = new SocketEvent("registerSucceeded", message);
				} else {
					message = "Rejestracja nie powiod³a siê. Spróbuj póŸniej.";
					e = new SocketEvent("registerFailed", message);
				}
			} else { // if user data is invalid
				e = new SocketEvent("registerSucceeded", message);
			}

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
