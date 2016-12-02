package sciCon;


import java.sql.*;

//import oracle.jdbc.pool.OracleDataSource;

public interface dbInterface {
	
	public default boolean isLoginValid(String login) {
		if(login.equals("uganda")) {
			return false;
		} else {
			return true;
		}
	}
	
	public default boolean isPasswordValid(String password, String rePassword) {
		if(password.equals(rePassword)) {
			return true;
		} else {
			return false;
		}
	}
	
}
