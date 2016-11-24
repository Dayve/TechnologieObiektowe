package sciCon;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.stage.Stage;
import oracle.jdbc.pool.OracleDataSource;


public class MainApp extends Application implements Controllers, dbInterface {

    private Stage primaryStage;
    
    @Override
    public void start(Stage primaryStage) throws SQLException {
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