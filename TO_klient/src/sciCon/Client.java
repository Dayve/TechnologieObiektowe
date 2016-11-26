package sciCon;
import java.io.IOException;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.stage.Stage;
import sciCon.model.NetworkConnection;
//import oracle.jdbc.pool.OracleDataSource;


public class Client extends Application implements Controllers, dbInterface {

    private Stage primaryStage;
    private NetworkConnection network = null;
    
    @Override
    public void start(Stage primaryStage) throws SQLException {
    	
//    	Thread net = new Thread(network);
//		net.start();
    	
		network = new NetworkConnection();
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("sciCon");

        initStartLayout();
    }

    
    public void initStartLayout() {
    	loadScene(primaryStage, "view/LoginLayout.fxml", 320, 200, false);
    }


    public Stage getPrimaryStage() {
        return primaryStage;
    }

    
    public static void main(String[] args) {
        launch(args);
    }
}