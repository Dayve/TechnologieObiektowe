package sciCon;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.stage.Stage;
import sciCon.model.NetworkConnection;

public class Client extends Application implements Controllers, dbInterface {

    private Stage primaryStage;
    
    public static void ConnectToServer() {
    	NetworkConnection.connect("localhost", 8080);;
    }
    
    @Override
    public void start(Stage primaryStage) throws SQLException {
    	
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("sciCon");

        // get a method to call it using reflection
        
        java.lang.reflect.Method m = null;
        
		try {
			m = Client.class.getMethod("ConnectToServer");
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		// connect to server in another thread
		
        runInAnotherThread(m, null);
        
        
        // initialize GUI
        
        initClientLayout();
    }

    private void initClientLayout() {
    	loadScene(primaryStage, "view/LoginLayout.fxml", 320, 200, false);
    }

    
    public static void main(String[] args) {
        launch(args);
    }
}