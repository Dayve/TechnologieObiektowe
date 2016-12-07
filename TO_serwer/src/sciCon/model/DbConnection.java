package sciCon.model;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

import oracle.jdbc.pool.OracleDataSource;

public class DbConnection {

	static OracleDataSource ods = null;
	static Connection conn = null;

	public DbConnection(String database, String dbUser, String dbPassword) {
		try {
			ods = new OracleDataSource();
			ods.setURL("jdbc:oracle:oci:@" + database);
			ods.setUser(dbUser);
			ods.setPassword(dbPassword);
			conn = ods.getConnection();
		} catch (SQLException e) {
			System.out.println("Failed to connect to database.");
		}
	}

	public boolean doesUserExist(String login) {
		String loginQuery = "select login from uzytkownik where login = (?)";
		try {
			PreparedStatement pstmt = conn.prepareStatement(loginQuery);
			pstmt.setString(1, login);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				return true;
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * @return user's id if matching pair is found, if not: -1
	 */
	public int doLoginAndPasswordMatch(String login, String password) {

		String loginQuery = "select id_uzytkownika from uzytkownik where login = (?) and haslo = (?)";
		int userId = -1;
		try {
			PreparedStatement pstmt = conn.prepareStatement(loginQuery);
			pstmt.setString(1, login);
			pstmt.setString(2, password);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				userId = rs.getInt(1);
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userId;
	}

	private int countEntries(String column, String table) {
		int count = 0;
		try {
			String countQuery = "select count(?) from " + table;
			PreparedStatement pstmt = conn.prepareStatement(countQuery);
			pstmt.setString(1, column);

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				count = rs.getInt(1);
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	private int maxEntry(String column, String table) {
		int count = 0;
		try {
			String countQuery = "select max(" + column + ") from " + table;
			PreparedStatement pstmt = conn.prepareStatement(countQuery);

			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	public boolean addConference(Conference c) {
		boolean succeeded = true;

		String name = c.getName(), subject = c.getSubject(), startTime = c.getStartTime(), endTime = c.getEndTime(),
				place = c.getPlace(), description = c.getDescription(), agenda = c.getAgenda();
		LocalDate date = c.getDate();

		int id = 0;
		id = this.maxEntry("id_wydarzenia", "wydarzenie") + 1;

		String addConferenceQuery = "insert into wydarzenie values(?, to_date(?,'YYYY-MM-DD'), ?, ?, ?, ?, ?, ?, ?)";

		try {
			PreparedStatement pstmt = conn.prepareStatement(addConferenceQuery);
			pstmt.setInt(1, id);
			pstmt.setString(2, date.toString());
			pstmt.setString(3, name);
			pstmt.setString(4, subject);
			pstmt.setString(5, place);
			pstmt.setString(6, description);
			pstmt.setString(7, agenda);
			pstmt.setString(8, startTime);
			pstmt.setString(9, endTime);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			succeeded = false;
			System.out.println("Adding a conference to database has failed.");
		}
		return succeeded;
	}

	public int isUserValid(User u) {

		int retCode = 0;

		String login = u.getLogin();
		String password = u.getPassword();
		String name = u.getName();
		String surname = u.getSurname();

		if (doesUserExist(login)) {
			retCode |= 1;
		}

		if (!(login.matches("[a-zA-Z0-9_]*")) || login.length() < 3) {
			retCode |= 2;
		}

		if (password.length() < 6) {
			retCode |= 4;
		}

		if (name.length() < 2 || surname.length() < 2) {
			retCode |= 8;
		}

		return retCode;
	}

	public boolean registerUser(User u) {
		boolean succeeded = true;
		int id = 0;
		id = this.maxEntry("id_uzytkownika", "uzytkownik") + 1;

		String login = u.getLogin();
		String name = u.getName();
		String password = u.getPassword();
		String surname = u.getSurname();

		String registerQuery = "insert into uzytkownik(id_uzytkownika, login, haslo, imie, nazwisko) values(?,?,?,?,?)";

		try {
			PreparedStatement pstmt = conn.prepareStatement(registerQuery);
			pstmt.setInt(1, id);
			pstmt.setString(2, login);
			pstmt.setString(3, password);
			pstmt.setString(4, name);
			pstmt.setString(5, surname);
			pstmt.executeUpdate();
			pstmt.close();
			succeeded = true;
		} catch (SQLException e) {
			succeeded = false;
			e.printStackTrace();
		}

		return succeeded;
	}

	public ArrayList<Conference> showConferenceFeed() {

		Date fetchedDate;
		String name;
		ArrayList<Conference> conferenceFeed = new ArrayList<Conference>();
		String conferenceFeedQuery = "select data, nazwa from wydarzenie where data >= current_date";
		try {
			PreparedStatement pstmt = conn.prepareStatement(conferenceFeedQuery);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				fetchedDate = rs.getDate(1);
				name = rs.getString(2);
				LocalDate date = fetchedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				conferenceFeed.add(new Conference(name, date));
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conferenceFeed;
	}

	public int isConferenceValid(Conference c) {
		int retCode = 0;

		// String login = u.getLogin();
		// String password = u.getPassword();
		// String name = u.getName();
		// String surname = u.getSurname();

		// check some conditions
		//
		// if (doesUserExist(login)) {
		// retCode |= 1;
		// }
		//
		// if (!(login.matches("[a-zA-Z0-9_]*")) || login.length() < 3) {
		// retCode |= 2;
		// }
		//
		// if (password.length() < 6) {
		// retCode |= 4;
		// }
		//
		// if (name.length() < 2 || surname.length() < 2) {
		// retCode |= 8;
		// }

		return retCode;
	}
}