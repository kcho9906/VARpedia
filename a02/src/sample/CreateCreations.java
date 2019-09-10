package sample;

import com.sun.javaws.progress.Progress;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.attribute.FileTime;

public class CreateCreations {
    private Stage window;
    private Button returnToMenuButton2 = new Button("Return to menu");
    private HBox searchLayout = new HBox();
    private HBox configureCreationsLayout = new HBox();
    private TextField searchInput = new TextField();
    private TextField lineInput = new TextField();
    private TextField creationNameInput = new TextField();
    private TextArea searchResult = new TextArea();
    private Button createButton = new Button("Create new creation");
    private Button searchButton = new Button("Search");
    private File file, creationDir;
    private ProgressBar progressBar = new ProgressBar(0);
    private int _totalLines;
    private Label progressBarLabel = new Label("");
    private VBox createCreationsLayout;


    public CreateCreations(Stage stage) {
        window = stage;
        setUpLayout();
        setActions();
    }

    public void setUpLayout() {
        //-----------------------------------SEARCH LAYOUT---------------------------------//
        progressBar.prefWidthProperty().bind(searchResult.widthProperty());
        searchLayout.setPadding(new Insets(10, 10, 10, 10));
        searchLayout.getChildren().addAll(searchInput, searchButton);
        searchLayout.setAlignment(Pos.CENTER);
        searchLayout.setSpacing(10);

        //--------------------------CREATING CREATION INPUT LAYOUT-------------------------//
        configureCreationsLayout.setPadding(new Insets(10, 10, 10, 10));
        configureCreationsLayout.getChildren().addAll(lineInput, creationNameInput);
        configureCreationsLayout.setAlignment(Pos.CENTER);
        configureCreationsLayout.setSpacing(10);

        //------------------------------CREATE CREATIONS LAYOUT------------------------------//
        createCreationsLayout = new VBox(20);
        createCreationsLayout.getChildren().addAll(searchLayout, progressBarLabel, progressBar, searchResult, configureCreationsLayout, createButton, returnToMenuButton2);
        createCreationsLayout.setAlignment(Pos.CENTER);
    }

    public void setActions() {

        returnToMenuButton2.setPrefWidth(200);
        returnToMenuButton2.setOnAction(e -> {
            e.consume();
            Main.returnToMenu();
        });
    }

    public VBox getCreateCreationsLayout() {
        return createCreationsLayout;
    }
}