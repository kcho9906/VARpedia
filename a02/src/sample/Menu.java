package sample;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

    public Menu(Stage stage) {
        window = stage;
        setup();
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
    }

    public VBox getMenuLayout(){
        return menuLayout;
    }

}