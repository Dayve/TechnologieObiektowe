package sciCon.controller;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import oracle.jdbc.pool.OracleDataSource;
import sciCon.Controllers;
import sciCon.dbInterface;

public class LoginRegisterController implements Controllers, dbInterface {
	
	@FXML private TextField loginField;
	@FXML private TextField passwordField;
	@FXML private TextField passwordRepeatField;
	@FXML private Label controlLabel;
	
	private OracleDataSource ods = null;
	
	private void ConnectToDb(String database, String dbUser, String dbPassword){
		try {
				ods = new OracleDataSource();
				ods.setURL("jdbc:oracle:oci:@" + database);
				ods.setUser(dbUser);
				ods.setPassword(dbPassword);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	//considering moving this to dbinterface
	
	private void ExecuteUpdate(String query, int id, String login, String password) {
		try {
			Connection conn = ods.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, id);
			pstmt.setString(2, login);
			pstmt.setString(3, password);
			pstmt.executeUpdate();
	    	pstmt.close();
	    	conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private int ExecuteQuery(String query, String resultName, String parameterName){
		int resolved_id = 0;
		try {			
			Connection conn = ods.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setString(1, parameterName);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()){
				System.out.println(resolved_id);
		        resolved_id = rs.getInt(resultName);
			}
	    	pstmt.close();
	    	conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return resolved_id;
	}
	
	@FXML
	private void register() throws SQLException {
		
		String login = loginField.getText();
		String password = passwordField.getText();
		String rePassword = passwordRepeatField.getText();
		String id_resolve_query = "select count(?) as x from uzytkownik";
		String register_query = "insert into uzytkownik(ID_UZYT, LOGIN_UZYT, HASLO) values(?,?,?)";
		String message = "";

		boolean showMessage = false;
		
		if(!isLoginValid(login)) {
			message += "Login jest ju¿ zajêty. ";
			showMessage = true;
		}
		
		if(!isPasswordValid(password, rePassword)) {
			message += "\nHas³a do siebie nie pasuj¹.";
			showMessage = true;
		}
		if(showMessage == false) {
			message = "";
			int id = 0;
			ConnectToDb("todb", "todb", "todb");
			id = ExecuteQuery(id_resolve_query, "x", "ID_UZYT");
			id++;
			ExecuteUpdate(register_query, id, login, password);
			System.out.println("Zarejestrowano");
		} 
		
		controlLabel.setText(message);
	}

	@FXML
	private void goToLogin(ActionEvent event) {
		Stage sourceStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
		loadScene(sourceStage, "view/LoginLayout.fxml", 320, 200, false);
	}
	
	@FXML
	private void goToRegistration(ActionEvent event) {
		Stage sourceStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
		loadScene(sourceStage, "view/RegisterLayout.fxml", 320, 200, false);
	}

    @FXML
    private void login(ActionEvent event) { // handler
    	String login = loginField.getText();
		String password = passwordField.getText();
		
		// here check if login is valid
		
    	Stage sourceStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
    	loadScene(sourceStage, "view/ApplicationLayout.fxml", 900, 600, true);
    }
}
