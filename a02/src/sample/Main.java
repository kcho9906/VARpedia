package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

public class Main extends Application {

    public static Stage window;
    public static Scene menuScene;

    @Override
    public void start(Stage primaryStage) throws Exception{

        window = primaryStage;
        Menu menu = new Menu(window);
        VBox menuLayout = menu.getMenuLayout();
        menuScene = new Scene(menuLayout, 600, 600);

        window.setTitle("VARpedia");
        window.setScene(menuScene);
        window.show();
    }

    public static void returnToMenu() {
        addConfirmationAlert("Are you sure you want to return to menu?");
    }

    /**
     * An alert class which will alert the user with the input message
     * @param confirmationMessage
     */
    public static void addConfirmationAlert(String confirmationMessage) {

        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setContentText(confirmationMessage);

        // if they want to overwrite, then they will be prompted
        Optional<ButtonType> result = a.showAndWait();
        if (result.get() == ButtonType.OK) {
            window.setScene(menuScene);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}