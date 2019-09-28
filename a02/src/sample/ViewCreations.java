package sample;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.FileTime;

public class ViewCreations {
    private Stage window;
    private Button playCreationButton = new Button("Play");
    private Button deleteCreationButton = new Button("Delete");
    private Button returnToMenuButton1 = new Button("Return to menu");
    private TableView creationsList;
    private VBox viewCreationsLayout;

    public ViewCreations(Stage stage) {
        window = stage;
        setUpList();
        setUpLayout();
        setActions();
    }

    public void setUpList() {
        creationsList = new TableView<>();
        TableColumn<Creation, String> nameColumn = new TableColumn<>("Creation Names");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("_creationName"));
        nameColumn.prefWidthProperty().bind(creationsList.widthProperty().divide(2));

        TableColumn<Creation, FileTime> timeColumn = new TableColumn<>("Time Created");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("_timeCreated"));
        timeColumn.prefWidthProperty().bind(creationsList.widthProperty().divide(2));


        creationsList.getColumns().addAll(nameColumn, timeColumn);
        updateTable();
    }

    public void setUpLayout() {
        HBox viewCreationsOptions = new HBox();
        viewCreationsOptions.setPadding(new Insets(10, 10, 10, 10));
        viewCreationsOptions.setSpacing(10);
        viewCreationsOptions.setAlignment(Pos.CENTER);
        viewCreationsOptions.getChildren().addAll(playCreationButton, deleteCreationButton, returnToMenuButton1);
        viewCreationsLayout = new VBox(20);
        viewCreationsLayout.getChildren().addAll(creationsList, viewCreationsOptions);
        viewCreationsLayout.setAlignment(Pos.CENTER);

        viewCreationsOptions.prefWidthProperty().bind(window.widthProperty());
        playCreationButton.setPrefHeight(100);
        deleteCreationButton.setPrefHeight(100);
        returnToMenuButton1.setPrefHeight(100);

        //disable play and delete buttons until selection is made
        playCreationButton.disableProperty().bind(Bindings.isEmpty(creationsList.getSelectionModel().getSelectedItems()));
        deleteCreationButton.disableProperty().bind(Bindings.isEmpty(creationsList.getSelectionModel().getSelectedItems()));
    }

    public void setActions() {

        returnToMenuButton1.setPrefWidth(200);
        returnToMenuButton1.setOnAction(e -> {
            e.consume();
            Main.returnToMenu();
        });

        //play the creation back to user
        playCreationButton.setOnAction(playButtonClicked -> {
            Object creationSelected = creationsList.getSelectionModel().getSelectedItem();
            String creationName = ((Creation) creationSelected).toString();
            Main.playVideo(creationName);
        });

        //set up an confirmation box to confirm with user the selected creation is to be deleted
        deleteCreationButton.setOnAction(deleteButtonClicked -> {
            ObservableList<Creation> allCreations = creationsList.getItems();
            Object creationSelected = creationsList.getSelectionModel().getSelectedItem();
            String creationName = ((Creation) creationSelected).toString();
            Boolean answer = Main.addConfirmationAlert("Deleting Creation", "Are you sure you want to delete \"" + creationName + "\"?", "Yes", "No");
            if (answer) {
                allCreations.remove(creationSelected);
                String command = "rm -rf ./src/creations/" + creationName;

                Terminal.command(command);
            }
        });
    }

    public VBox getViewCreationsLayout() {
        return viewCreationsLayout;
    }

    //------------------------------------VIEW CREATIONS METHODS-----------------------------//


    private ObservableList<Creation> getCreations() {
        ObservableList<Creation> creations = FXCollections.observableArrayList();
        String path = System.getProperty("user.dir") + "/src/creations";
        File[] directories = new File(path).listFiles(File::isDirectory);
        for (File directory: directories) {
            creations.add(new Creation(directory));
        }
        return creations;
    }

    public void updateTable() {
        creationsList.setItems(getCreations());
    }

}