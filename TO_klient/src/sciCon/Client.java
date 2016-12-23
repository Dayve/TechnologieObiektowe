package sciCon;
import java.sql.SQLException;
import java.util.Timer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import sciCon.model.Controller;

public class Client extends Application implements Controller {

    private Stage primaryStage;
    // timer is initialized in ApplicationController
    // and is used to send feedUpdateRequest every few seconds
    public static Timer timer;
    
    @Override
    public void start(Stage primaryStage) throws SQLException {
    	
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("sciCon");
        
        // initialize GUI
        
        initClientLayout();
    }

    @Override
    public void stop()
    {
    	if(timer != null) {
    		timer.cancel();
    	}
        Platform.exit();
    }
    
    private void initClientLayout() {
    	loadScene(primaryStage, "view/LoginLayout.fxml", 320, 250, false, 0, 0);
    }

    
    public static void main(String[] args) {
        launch(args);
    }
}