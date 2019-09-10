package application;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
    }

    public void setUpList() {
        TableColumn<Creation, String> nameColumn = new TableColumn<>("Creation Names");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("_creationName"));
        nameColumn.setMinWidth(300);

        TableColumn<Creation, FileTime> timeColumn = new TableColumn<>("Time Created");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("_timeCreated"));
        timeColumn.setMinWidth(300);

        creationsList = new TableView<>();
        creationsList.getColumns().addAll(nameColumn, timeColumn);
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
/*
        returnToMenuButton1.setPrefWidth(200);
        returnToMenuButton1.setOnAction(e -> {
            e.consume();
            returnToMenu();
        });*/
    }

    public VBox getViewCreationsLayout() {
        return viewCreationsLayout;
    }
}
