package sciCon.controller;

import sciCon.model.Conference;
import sciCon.model.Controller;
import sciCon.model.FileInfo;
import sciCon.model.NetworkConnection;
import sciCon.model.Paper;
import sciCon.model.ServerResponseTimeout;
import sciCon.model.SocketEvent;

import java.io.File;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FileManagerController implements Controller {
	@FXML Parent fileManagerWindow;

	@FXML private TextField fileSearchBar;
	@FXML private TableView<FileInfo> filesTableView;

	@FXML Button addNewFileButton;
	@FXML Button downloadFileButton;
	@FXML Button removeFileButton;

	private FileChooser fileChooser = new FileChooser();
	private File chosenFile = null;

	// Those FileInfo objects contain fileIDs, as opposed to those created from
	// local files
	private static ArrayList<FileInfo> filesForThisConference = new ArrayList<FileInfo>();

	@FXML public void initialize() {
		fetchCurrentlyStoredFiles(fc.getSelectedConference());

		filesTableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("filename"));
		filesTableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("authorsName"));
		filesTableView.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("description"));

		refreshList(filesForThisConference);

		fileSearchBar.textProperty().addListener(obs -> {
			filterTable(fileSearchBar.getText());
		});

		downloadFileButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(final ActionEvent e) {
				FileInfo chosenRow = filesTableView.getSelectionModel().getSelectedItem();
				if (chosenRow != null) {
					fileChooser.setInitialFileName(chosenRow.getFilename());

					chosenFile = fileChooser.showSaveDialog((Stage) fileManagerWindow.getScene().getWindow());

					if (chosenFile != null) {
						new Thread(() -> downloadFile(chosenRow.getFileID())).start();
					}

				} else {
					Platform.runLater(new Runnable() {
						@Override public void run() {
							openDialogBox(fileManagerWindow,
									"Musisz wybrać plik do pobrania. Żaden plik nie jest zaznaczony");
						}
					});
				}
			}
		});

		removeFileButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(final ActionEvent e) {
				FileInfo chosenRow = filesTableView.getSelectionModel().getSelectedItem();
				if (chosenRow != null) {
					new Thread(() -> sendRequestToRemoveFile(chosenRow.getFileID())).start();
				} else {
					Platform.runLater(new Runnable() {
						@Override public void run() {
							openDialogBox(fileManagerWindow,
									"Musisz wybrać plik do usunięcia. Żaden plik nie jest zaznaczony");
						}
					});
				}
			}
		});
	}

	private void filterTable(String filter) {
		ArrayList<FileInfo> filteredList = new ArrayList<FileInfo>();

		for (FileInfo fInfo : filesForThisConference) {
			if (fInfo.getAuthorsName().toLowerCase().contains(filter.toLowerCase())
					|| fInfo.getDescription().toLowerCase().contains(filter.toLowerCase())
					|| fInfo.getFilename().toLowerCase().contains(filter.toLowerCase())) {
				filteredList.add(fInfo);
			}
		}

		refreshList(filteredList);
	}

	private void sendRequestToRemoveFile(Integer givenFileID) {
		SocketEvent se = new SocketEvent("reqestRemovingChosenFile", givenFileID);
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent("fileRemoved", "fileRemovingError");

		String eventName = res.getName();
		final String message;

		if (eventName.equals("fileRemoved")) {
			message = "Usunięto wybrany plik z bazy danych";
		} else {
			message = "Wystąpił błąd. Nie można usunąć pliku";
		}
		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(fileManagerWindow, message);
			}
		});
	}

	private void downloadFile(Integer givenFileID) {
		SocketEvent se = new SocketEvent("reqestSendingChosenFile", givenFileID);
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent("fileSent", "fileSendingError");

		String eventName = res.getName();

		if (eventName.equals("fileSendingError")) {
			Platform.runLater(new Runnable() {
				@Override public void run() {
					openDialogBox(fileManagerWindow, "Wystąpił błąd. Nie można pobrać pliku");
				}
			});
		} else {
			Paper fileFetchedFromServer = new Paper();
			byte[] receivedBytes = res.getObject(byte[].class);
			fileFetchedFromServer.createFromReceivedBytes(receivedBytes);

			fileFetchedFromServer.saveAsFile(chosenFile.getAbsolutePath());
		}
	}

	private void fetchCurrentlyStoredFiles(Conference forConference) {
		SocketEvent se = new SocketEvent("reqestFileList", forConference);
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent("fileListFetched",
					"fileListFetchError");
		String eventName = res.getName();

		if (eventName.equals("fileListFetchError")) {
			Platform.runLater(new Runnable() {
				@Override public void run() {
					openDialogBox(fileManagerWindow, "Wystąpił błąd. Nie można pobrać listy plików");
				}
			});
		} else {
			filesForThisConference = (ArrayList<FileInfo>) res.getObject(ArrayList.class);
		}
	}

	public void refreshList(ArrayList<FileInfo> newContent) {
		filesTableView.setItems(FXCollections.observableArrayList(newContent));
	}

	@FXML private void goToUploadWindow() {
		openNewWindow(fileManagerWindow, "view/UploadFileLayout.fxml", 700, 500, false, "Wysyłanie pliku na serwer");
	}
}
