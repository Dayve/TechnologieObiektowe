package sciCon.controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import sciCon.MainApp;

public class StartLayoutController {
	
	// Reference to the main application.
	private GridPane rootLayout;
	
    @FXML
    private void login(ActionEvent event) { // handler
  
        try {
        	FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/ApplicationLayout.fxml"));
			rootLayout = (GridPane) loader.load();
			Scene scene = new Scene(rootLayout);
	    	Stage sourceStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
	    	
	    	sourceStage.setScene(scene);
	    	sourceStage.setResizable(true);
	    	sourceStage.setMinWidth(900);
	    	sourceStage.setMinHeight(600);
	    	
	    	
	    	sourceStage.show();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Show the scene containing the root layout.
        
    	
    	
    }
 
}
