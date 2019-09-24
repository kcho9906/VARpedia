package sample;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Menu {
    private Stage window;
    private VBox menuLayout = new VBox(20);

    private Button viewCreationsButton = new Button ("View all creations");
    private Button createCreationsButton = new Button ("Create new creations");
    private Label titleLabel = new Label("SOFTENG 206 Assignment 3");
    private Label introductionLabel = new Label("Welcome to VARpedia");
    private Label briefLabel = new Label("");
    private HBox buttonsLayout = new HBox(20);
    private Scene viewCreationsScene, createCreationsScene;


    public Menu(Stage stage) {
        window = stage;
        setup();
        setActions();
    }

    public void setup() {

        menuLayout.getChildren().addAll(titleLabel, introductionLabel, briefLabel, buttonsLayout);
        menuLayout.setAlignment(Pos.CENTER);
        buttonsLayout.getChildren().addAll(viewCreationsButton, createCreationsButton);
        buttonsLayout.setAlignment(Pos.BOTTOM_CENTER);
        buttonsLayout.setPadding(new Insets(10, 10, 10, 10));
        buttonsLayout.setSpacing(10);
        createCreationsButton.setPrefHeight(100);
        createCreationsButton.prefWidthProperty().bind(window.widthProperty());
        viewCreationsButton.setPrefHeight(100);
        viewCreationsButton.prefWidthProperty().bind(window.widthProperty());
        titleLabel.setFont(Font.font("Calibri", FontWeight.BOLD, 20));

        ViewCreations viewCreations = new ViewCreations(window);
        VBox viewCreationsLayout = viewCreations.getViewCreationsLayout();

        CreateCreations createCreations = new CreateCreations();
        VBox createCreationsLayout = createCreations.getCreateCreationsLayout();

        viewCreationsScene = new Scene(viewCreationsLayout, 600, 600);
        createCreationsScene = new Scene(createCreationsLayout, 600,600);
    }

    public void setActions() {
        viewCreationsButton.setOnAction(e -> {
            window.setScene(viewCreationsScene);
        });

        createCreationsButton.setOnAction(e -> {
            window.setScene(createCreationsScene);
        });

    }

    public VBox getMenuLayout(){
        return menuLayout;
    }

}