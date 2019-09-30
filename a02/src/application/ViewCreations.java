package application;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;

/**
 * This class is responsible for the view creations scene and button actions.
 * You will have the options to:
 *      Play - Select a creation to play
 *      Delete - Select a creation to delete
 *      Delete All - Deletes all creations
 *      Return to Menu - Returns to the main menu
 */
public class ViewCreations {

    private Stage window;
    private Button playCreationButton = new Button("Play");
    private Button deleteCreationButton = new Button("Delete");
    private Button returnToMenuButton1 = new Button("Return to menu");
    private Button deleteAllButton = new Button("Delete All");
    private TableView creationsList;
    private VBox viewCreationsLayout;

    public ViewCreations(Stage stage) {
        window = stage;
        setUpList();
        setUpLayout();
        setActions();
    }

    /**
     * Sets up the list for the creations with the titles:
     *      Creations Names
     *      Keyword
     *      Time Created
     *      Length of Video
     */
    public void setUpList() {

        creationsList = new TableView<>();
        TableColumn<Creation, String> nameColumn = new TableColumn<>("Creation Names");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("_creationName"));
        nameColumn.prefWidthProperty().bind(creationsList.widthProperty().divide(4));

        TableColumn<Creation, String> searchTermColumn = new TableColumn<>("Keyword");
        searchTermColumn.setCellValueFactory(new PropertyValueFactory<>("_searchTerm"));
        searchTermColumn.prefWidthProperty().bind(creationsList.widthProperty().divide(4));

        TableColumn<Creation, String> timeColumn = new TableColumn<>("Time Created");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("_timeCreated"));
        timeColumn.prefWidthProperty().bind(creationsList.widthProperty().divide(4));

        TableColumn<Creation, String> durationColumn = new TableColumn<>("Length of Video");
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("_duration"));
        durationColumn.prefWidthProperty().bind(creationsList.widthProperty().divide(4));

        creationsList.getColumns().addAll(nameColumn, searchTermColumn, timeColumn, durationColumn);
        updateTable();
    }

    public void setUpLayout() {

        HBox viewCreationsOptions = new HBox();
        viewCreationsOptions.setPadding(new Insets(10, 10, 10, 10));
        viewCreationsOptions.setSpacing(10);
        viewCreationsOptions.setAlignment(Pos.CENTER);
        viewCreationsOptions.getChildren().addAll(playCreationButton, deleteCreationButton, deleteAllButton, returnToMenuButton1);
        viewCreationsLayout = new VBox(20);
        viewCreationsLayout.getChildren().addAll(creationsList, viewCreationsOptions);
        viewCreationsLayout.setAlignment(Pos.CENTER);

        viewCreationsOptions.prefWidthProperty().bind(window.widthProperty());
        playCreationButton.setPrefHeight(100);
        deleteCreationButton.setPrefHeight(100);
        deleteAllButton.setPrefHeight(100);
        returnToMenuButton1.setPrefHeight(100);

        playCreationButton.setPrefWidth(150);
        deleteAllButton.setPrefWidth(150);
        deleteCreationButton.setPrefWidth(150);

        //disable play and delete buttons until selection is made
        playCreationButton.disableProperty().bind(Bindings.isEmpty(creationsList.getSelectionModel().getSelectedItems()));
        deleteCreationButton.disableProperty().bind(Bindings.isEmpty(creationsList.getSelectionModel().getSelectedItems()));
    }

    public void setActions() {

        returnToMenuButton1.setPrefWidth(150);
        returnToMenuButton1.setOnAction(e -> {

            e.consume();
            Main.returnToMenu();
        });

        //play the creation back to user
        playCreationButton.setOnAction(playButtonClicked -> {

            String creationName = creationsList.getSelectionModel().getSelectedItem().toString();
            File creationFile = new File("src/creations/" + creationName + "/" + creationName + ".mp4");
            Main.playVideo(creationFile);
        });

        //set up an confirmation box to confirm with user the selected creation is to be deleted
        deleteCreationButton.setOnAction(deleteButtonClicked -> {

            ObservableList<Creation> allCreations = creationsList.getItems();
            Object creationSelected = creationsList.getSelectionModel().getSelectedItem();
            String creationName = ((Creation) creationSelected).get_creationName();
            Boolean answer = Main.addConfirmationAlert("Deleting Creation", "Are you sure you want to delete \"" + creationName + "\"?", "Yes", "No");
            if (answer) {

                allCreations.remove(creationSelected);
                String command = "rm -r -f src/creations/" + creationName;
                Terminal.command(command);
            }
        });

        deleteAllButton.setOnAction(event -> {

            boolean clearCreations = Main.addConfirmationAlert("Delete all audio files", "Are you sure you want to delete all creations?", "Yes", "No");
            if (clearCreations) {

                String command = "rm -Rf src/creations/*";
                Terminal.command(command);
                updateTable();
            }
        });
    }

    public VBox getViewCreationsLayout() {

        return viewCreationsLayout;
    }

    /**
     * This method is for getting the creations from the list.
     * @return
     */
    private ObservableList<Creation> getCreations() {

        ObservableList<Creation> creations = FXCollections.observableArrayList();
        String path = System.getProperty("user.dir") + "/src/creations";
        File[] directories = new File(path).listFiles(File::isDirectory);
        for (File directory: directories) {

            creations.add(new Creation(directory));
        }
        return creations;
    }

    /**
     * updates the table
     */
    public void updateTable() {

        creationsList.setItems(getCreations());
    }

    /**
     * This is a class for all the creation properties
     */
    public class Creation {

        private String _creationName;
        private String _searchTerm;
        private String _timeCreated;
        private String _duration;

        public Creation(File directory) {
            _creationName = directory.getName();
            _timeCreated = getCreationDate(directory);
            _duration = calculateDuration(directory);
            _searchTerm = findSearchTerm(directory);
        }

        public String get_creationName() {

            return _creationName;
        }

        public void set_creationName(String _creationName) {

            this._creationName = _creationName;
        }

        public String get_timeCreated() {

            return _timeCreated;
        }

        public void set_timeCreated(String _timeCreated) {

            this._timeCreated = _timeCreated;
        }

        public void set_duration(String duration) {

            this._duration = duration;
        }

        public String get_duration() {

            return _duration;
        }

        public void set_searchTerm(String searchTerm) {

            this._searchTerm = searchTerm;
        }

        public String get_searchTerm() {

            return _searchTerm;
        }

        private String getCreationDate(File directory) {

            Path p = Paths.get(directory.getAbsolutePath());
            BasicFileAttributes view = null;
            String dateCreated = null;
            try {

                view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
                SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy-hh:mm a");
                FileTime date = view.creationTime();
                dateCreated = df.format(date.toMillis());
            } catch (IOException e) {

                e.printStackTrace();
            }
            return dateCreated.trim();
        }

        @Override
        public String toString() {

            return _creationName;
        }

        public String calculateDuration(File directory) {

            //get file name for video
            String command = "ls " + directory.getPath() + " | grep .mp4$";
            String fileName = Terminal.command(command).trim();
            String lengthCommand = "ffmpeg -i " + directory.getPath() + "/" + fileName + " 2>&1 | grep Duration | cut -d ' ' -f 4 | sed s/,//";
            String duration = Terminal.command(lengthCommand).trim();
            return duration;
        }

        public String findSearchTerm(File directory) {

            //get file name for video
            String command = "ls -a " + directory.getPath() + " | grep .wav$ | sed 's/^.\\(.*\\)....$/\\1/'";
            String name = Terminal.command(command).trim();
            return name;
        }
    }
}