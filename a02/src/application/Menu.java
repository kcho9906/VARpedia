package application;

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

/**
 * This is the scene for the main menu. The layout of the menu and
 * the button actions are all facilitated in this class.
 */
public class Menu {

    private Button viewCreationsButton = new Button ("View all creations");
    private Button createCreationsButton = new Button ("Create new creations");
    private HBox buttonsLayout = new HBox(20);
    private Label titleLabel = new Label("SOFTENG 206 Assignment 3");
    private Label introductionLabel = new Label("Welcome to VARpedia");
    private Label briefLabel = new Label("");
    private static Scene viewCreationsScene, createCreationsScene;
    private static Stage window;
    private static ViewCreations viewCreations;
    private VBox menuLayout = new VBox(20);

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

        viewCreations = new ViewCreations(window);
        VBox viewCreationsLayout = viewCreations.getViewCreationsLayout();

        CreateCreations createCreations = new CreateCreations();
        VBox createCreationsLayout = createCreations.getCreateCreationsLayout();

        viewCreationsScene = new Scene(viewCreationsLayout, window.getWidth(), window.getHeight());
        createCreationsScene = new Scene(createCreationsLayout, window.getWidth(), window.getHeight());
    }

    /**
     * Sets all the button actions
     */
    public void setActions() {

        viewCreationsButton.setOnAction(e -> {

            viewCreations.updateTable();
            window.setScene(viewCreationsScene);
        });

        createCreationsButton.setOnAction(e -> {

            window.setScene(createCreationsScene);
        });
    }

    /**
     * Getter for the menu layout
     * @return
     */
    public VBox getMenuLayout() {

        return menuLayout;
    }

    /**
     * returns to the view creations scene
     */
    public static void returnToViewCreations() {

        viewCreations.updateTable();
        window.setScene(viewCreationsScene);
    }
}