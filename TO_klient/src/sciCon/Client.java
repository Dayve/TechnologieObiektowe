package sciCon;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.stage.Stage;
import sciCon.model.Controller;
import sciCon.model.NetworkConnection;

public class Client extends Application implements Controller {

    private Stage primaryStage;
    
    
    
    @Override
    public void start(Stage primaryStage) throws SQLException {
    	
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("sciCon");
        
        // initialize GUI
        
        initClientLayout();
    }

    private void initClientLayout() {
    	loadScene(primaryStage, "view/LoginLayout.fxml", 320, 250, false, 0, 0);
    }

    
    public static void main(String[] args) {
        launch(args);
    }
}