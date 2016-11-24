package sciCon;
import javafx.application.Application;
import javafx.stage.Stage;


public class MainApp extends Application implements Controllers {

    private Stage primaryStage;
    
    @Override
    public void start(Stage primaryStage) {
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