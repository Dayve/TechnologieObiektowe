package sciCon.model;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
	public User getUser(String _login, String _password) {

		String loginQuery = "select id_uzytkownika, login, haslo, imie, nazwisko, email, organizacja"
				+ " from uzytkownik where login = (?) and haslo = (?)";
		Integer id = null; 
		User u = null;
		String login = null, name = null, surname = null, email = null, organization = null;
		try {
			PreparedStatement pstmt = conn.prepareStatement(loginQuery);
			pstmt.setString(1, _login);
			pstmt.setString(2, _password);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				id = rs.getInt(1);
				login = rs.getString(2);
				name = rs.getString(3);
				surname = rs.getString(4);
				email = rs.getString(5);
				organization = rs.getString(6);
				u = new User(id, login, name, surname, email, organization);
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return u;
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
		
		int id = this.maxEntry("id_wydarzenia", "wydarzenie") + 1;

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

	public ArrayList<Conference> fetchConferenceFeed(Boolean past) {

		// !past - show present and future conferences
		
		int id = 0;
		String name, date, subject, startTime, endTime,
				place, description, agenda, conferenceFeedQuery;
		ArrayList<Conference> conferenceFeed = new ArrayList<Conference>();
		if(past == null) {
			conferenceFeedQuery = "select * from wydarzenie";
		} else {
			conferenceFeedQuery = past ? "select * from wydarzenie where data < current_date" :
				"select * from wydarzenie where data >= current_date";
		}
		

		try {
			PreparedStatement pstmt;
			
			pstmt = conn.prepareStatement(conferenceFeedQuery);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				id = rs.getInt(1);
				date = rs.getString(2).substring(0, 10);
				name = rs.getString(3);
				subject = rs.getString(4);
				place = rs.getString(5);
				description = rs.getString(6);
				agenda = rs.getString(7);
				startTime = rs.getString(8);
				endTime = rs.getString(9);
				
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate lDate = LocalDate.parse(date, formatter);
				
//				conferenceFeed.add(new Conference(id, name, lDate, subject, place, 
//						description, agenda, startTime, endTime));
				conferenceFeed.add(new Conference(id, name, lDate, subject, startTime, 
						endTime, place, description, agenda));
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conferenceFeed;
	}
}