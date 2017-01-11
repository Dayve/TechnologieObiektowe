package sciCon.controller;

import sciCon.model.Controller;
import sciCon.model.FileInfo;
import sciCon.model.NetworkConnection;
import sciCon.model.Paper;
import sciCon.model.SocketEvent;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class FileManagerController implements Controller {
	@FXML Parent fileManagerWindow;
	
	@FXML private TextField fileSearchBar;
	@FXML private TableView<FileInfo> filesTableView;
	
	@FXML Button addNewFileButton;
	@FXML Button downloadFileButton;
	@FXML Button removeFileButton;


	@FXML public void initialize() {
	}
	
	@FXML private void goToUploadWindow() {
		openNewWindow(fileManagerWindow, "view/UploadFileLayout.fxml", 700, 500, false,
			"Wysyłanie pliku na serwer");
	}
}
