package sciCon.model;

import java.io.Serializable;

public class User implements Serializable {
	private static final long serialVersionUID = -7433946303557607605L;
	public enum UsersRole {
		ORGANIZER, SPONSOR, PRELECTOR, PARTICIPANT, PENDING, NONE
	}
	private Integer id = null;
	String login = null, name = null, surname = null, password = null, email = null, organization = null;

	public User(String login, String password) {
		this.login = login;
		this.password = password;
	}

	public User(String login, String name, String surname) {
		this.login = login;
		this.name = name;
		this.surname = surname;
	}

	public User(Integer id, String login, String name, String surname, String email, String organization) {
		this.id = id;
		this.login = login;
		this.name = name;
		this.surname = surname;
		this.email = email;
		this.organization = organization;
	}

	public User(String login, String password, String name, String surname, String organization) {
		this.login = login;
		this.password = password;
		this.name = name;
		this.surname = surname;
		this.organization = organization;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getLogin() {
		return login;
	}

	public String getSurname() {
		return surname;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getOrganization() {
		return organization;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	@Override public String toString() {
		return "User [id = " + id + " login=" + login + ", name=" + name + ", surname=" + surname + ", email=" + email
				+ ", organization=" + organization + "]";
	}
}
