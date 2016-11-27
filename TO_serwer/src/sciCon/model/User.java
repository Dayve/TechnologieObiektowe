package sciCon.model;

import java.io.Serializable;

public class User implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7433946303557607605L;
	String login = null;
	String name = null;
	String surname = null;
	String password = null;
	String email = null;
	
	public User(String login, String password) {
		this.login = login;
		this.password = password;
	}

	public User(String login, String password, String name, String surname) {
		this.login = login;
		this.password = password;
		this.name = name;
		this.surname = surname;
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

	@Override
	public String toString() {
		return "User [login=" + login + ", name=" + name + ", surname=" + surname + ", password=" + password
				+ ", email=" + email + "]";
	}
}
