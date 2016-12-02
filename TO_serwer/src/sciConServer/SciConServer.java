package sciConServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import oracle.jdbc.pool.OracleDataSource;
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

		private OracleDataSource ods = null;

		private void ConnectToDb(String database, String dbUser, String dbPassword) {
			try {
				ods = new OracleDataSource();
				ods.setURL("jdbc:oracle:oci:@" + database);
				ods.setUser(dbUser);
				ods.setPassword(dbPassword);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		private boolean isLoginValid(String login) {

			String login_resolve_query = "select login_uzytkownika from uzytkownik where login = (?)";
			try {
				ConnectToDb("todb", "todb", "todb");
				Connection conn = ods.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(login_resolve_query);
				pstmt.setString(1, login);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next()) {
					return true;
				}
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return false;
		}

		private int doLoginAndPasswordMatch(String login, String password) { // returns
																				// user
																				// id

			String loginQuery = "select id_uzytkownika from uzytkownik where login = (?) and haslo = (?)";
			int resolved_id = 0;
			try {

				ConnectToDb("todb", "todb", "todb");
				Connection conn = ods.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(loginQuery);
				pstmt.setString(1, login);
				pstmt.setString(2, password);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next()) {
					resolved_id = rs.getInt(0);
				}
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return resolved_id;
		}

		private void ExecuteUpdate(String query, int id, String login, String password, String name, String surname) {
			try {
				ConnectToDb("todb", "todb", "todb");
				Connection conn = ods.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, id);
				pstmt.setString(2, login);
				pstmt.setString(3, password);
				pstmt.setString(4, name);
				pstmt.setString(5, surname);
				pstmt.executeUpdate();
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		private int ExecuteQuery(String query, String parameterName) {
			int resultId = 0;
			try {
				ConnectToDb("todb", "todb", "todb");
				Connection conn = ods.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query);
				pstmt.setString(1, parameterName);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					resultId = rs.getInt(1);
				}
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return resultId;
		}

		public ServerImplementation(Socket socket) {
			s = socket;
		}

		@Override
		public void run() {
			try {

//				String countUsersQuery = "select count(?) from uzytkownik";
//				String registerQuery = "insert into uzytkownik(id_uzytkownika, login, haslo, imie, nazwisko) values(?,?,?,?,?)";

				objIn = new ObjectInputStream(s.getInputStream());
				objOut = new ObjectOutputStream(s.getOutputStream());

				SocketEvent e = null;
				User user = null;
				// java.lang.reflect.Method method = null;
				// MethodData methodData = null;

				while (true) {

					e = (SocketEvent) objIn.readObject();
					String eventName = e.getName();
					System.out.println(eventName);
					//
					switch (eventName) {
					case "loginReq": {
						System.out.println("zaraz wyœlê obiekt...");
						User u = (User) e.getObject(User.class);
						System.out.println(u);
						objOut.writeObject(u);
						System.out.println("poszed³ obiekt");
						break;
					}
					default: break;
					}

					// if(user != null) {
					// // print user if it's not null
					// System.out.println(user);
					//
					// }
					// if(user != null && user.getName() != null &&
					// user.getSurname() != null) {
					// // if name and surname is not null, register user to the
					// database
					//// int id = ExecuteQuery(countUsersQuery,
					// "id_uzytkownika");
					//// ExecuteUpdate(registerQuery, id, user.getLogin(),
					// user.getPassword(), user.getName(), user.getSurname());
					// objOut.writeObject(user);
					// }

					//
					// try {
					// methodData = (MethodData) objIn.readObject();
					// method =
					// ClientInvokedMethods.getMethod(methodData.getName(),
					// methodData.getParamsTypes());
					// Object[] arguments = methodData.getParams();
					// } catch (SecurityException e) {}
					// catch (NoSuchMethodException e) {}
					//
					// try {
					// method.invoke();
					// } catch (IllegalArgumentException e) {}
					// catch (IllegalAccessException e) {}
					// catch (InvocationTargetException e) {}
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
