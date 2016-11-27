package sciCon;
import java.io.IOException;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import sciCon.model.NetworkConnection;
//import oracle.jdbc.pool.OracleDataSource;


public class Client extends Application implements Controllers, dbInterface {

    private Stage primaryStage;
    private NetworkConnection network = null;
    
    @Override
    public void start(Stage primaryStage) throws SQLException {
    	network = new NetworkConnection();
    	
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("sciCon");

        initClientLayout();
    }

    private void initClientLayout() {
    	loadScene(primaryStage, "view/LoginLayout.fxml", 320, 200, false);
    }

    
    public static void main(String[] args) {
        launch(args);
    }
}