package sciCon.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;

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
	 * @return user if matching pair is found, if not: -1
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

		String name = c.getName(), subject = c.getSubject(), place = c.getPlace(), 
				description = c.getDescription(), agenda = c.getAgenda();
		LocalDateTime startTime = c.getStartTime(), endTime = c.getEndTime();
		
		User organizer = c.getFirstOrganizer();
		
		int eventId = this.maxEntry("id_wydarzenia", "wydarzenie") + 1;
		int participantId = this.maxEntry("id_uczestnika", "uczestnik") + 1;

		String addConferenceQuery = "insert into wydarzenie values(?, ?, ?, ?, ?, ?, "
				+ "to_date(?,'YYYY-MM-DD HH24:MI'), to_date(?,'YYYY-MM-DD HH24:MI'))";
		String addOrganizerQuery = "insert into uczestnik values(?, ?, ?)";
		String addParticipantRoleQuery = "insert into rola_uczestnika values(?, 0)";

		String insertStartTime = startTime.toString().replace('T', ' ');
		String insertEndTime = endTime.toString().replace('T', ' ');
		
		System.out.println("dodanie konferencji - proba");
		System.out.println(c);
		System.out.println(organizer);
		try {
			PreparedStatement pstmt = conn.prepareStatement(addConferenceQuery);
			pstmt.setInt(1, eventId);
			pstmt.setString(2, name);
			pstmt.setString(3, subject);
			pstmt.setString(4, place);
			pstmt.setString(5, description);
			pstmt.setString(6, agenda);
			pstmt.setString(7, insertStartTime);
			pstmt.setString(8, insertEndTime);
			pstmt.executeUpdate();
			pstmt.close();
			
			pstmt = conn.prepareStatement(addOrganizerQuery);
			pstmt.setInt(1, participantId);
			pstmt.setInt(2, eventId);
			pstmt.setInt(3, organizer.getId());
			pstmt.executeUpdate();
			pstmt.close();
			
			pstmt = conn.prepareStatement(addParticipantRoleQuery);
			pstmt.setInt(1, participantId);
			pstmt.executeUpdate();
			
			pstmt.close();
		} catch (SQLException e) {
			succeeded = false;
			System.out.println("Adding a conference to database has failed.");
			e.printStackTrace();
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

	private ArrayList<User> fetchConferenceOrganizers(int conferenceId) {
		ArrayList<User> organizers = new ArrayList<User>();
		String login = null, name = null, surname = null, fetchOrganizerQuery =
				"SELECT login, imie, nazwisko FROM uzytkownik WHERE id_uzytkownika = "
				+ "(SELECT id_uzytkownika FROM uczestnik WHERE id_wydarzenia = (?) "
				+ "AND id_uczestnika IN (SELECT id_uczestnika FROM rola_uczestnika WHERE id_statusu = 0));";
		
		try {
			PreparedStatement pstmt = conn.prepareStatement(fetchOrganizerQuery);
			pstmt.setInt(1, conferenceId);
			
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				login = rs.getString(1);
				name = rs.getString(2);
				surname = rs.getString(3);
				organizers.add(new User(login, name, surname));
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return organizers;
	}
	
	public ArrayList<Conference> fetchConferenceFeed(Boolean past) {

		// !past - show present and future conferences
		
		int id = 0;
		String name = null, subject = null, place = null, description = null, agenda = null, 
			conferenceFeedQuery = "select id_wydarzenia, nazwa, temat, miejsce, opis,"
				+ "plan, to_char(czas_rozpoczecia,'yyyy-mm-dd hh24:mi'), "
				+ "to_char(czas_zakonczenia,'yyyy-mm-dd hh24:mi') from wydarzenie";
		LocalDateTime startTime, endTime;
		ArrayList<Conference> conferenceFeed = new ArrayList<Conference>();
	
		if(past != null) {
			conferenceFeedQuery += past ? " where czas_zakonczenia <= current_date" :
				" where czas_rozpoczecia >= current_date";
		}
		
		try {
			PreparedStatement pstmt;
			String startTimeStr, endTimeStr;
			
			pstmt = conn.prepareStatement(conferenceFeedQuery);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				id = rs.getInt(1);
				name = rs.getString(2);
				subject = rs.getString(3);
				place = rs.getString(4);
				description = rs.getString(5);
				agenda = rs.getString(6);
				startTimeStr = rs.getString(7);
				endTimeStr = rs.getString(8);
			
				System.out.println("dla id: " + id);
				ArrayList<User> organizers = fetchConferenceOrganizers(id);
				
				System.out.println("organizatorzy:!");
				System.out.println(organizers);
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				startTime = LocalDateTime.parse(startTimeStr, formatter);
				endTime = LocalDateTime.parse(endTimeStr, formatter);
				
				conferenceFeed.add(new Conference(id, name, subject, startTime, endTime, 
						place, description, agenda, organizers));
				
			}
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println(conferenceFeed.get(0));
		return conferenceFeed;
	}
}