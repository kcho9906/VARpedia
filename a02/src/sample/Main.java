package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class Main extends Application {

    public static Stage window;
    public static Scene menuScene;

    @Override
    public void start(Stage primaryStage) throws Exception{

        window = primaryStage;
        window.setHeight(700);
        window.setWidth(800);
        Menu menu = new Menu(window);
        VBox menuLayout = menu.getMenuLayout();
        menuScene = new Scene(menuLayout, window.getWidth(), window.getHeight());
        menuScene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                window.setWidth((double)newValue);
            }
        });

        window.setOnCloseRequest( event -> {
            event.consume();
            Boolean answer = addConfirmationAlert("Exiting Program", "Are you sure you want to quit?", "Quit", "Cancel");
            if(answer){
                window.close();
                Platform.exit();
                System.exit(0);
            }
        });

        window.setTitle("VARpedia");
        window.setResizable(true);
        window.setScene(menuScene);
        window.show();

        // delete audioFies if closed
        String command = "rm -fr ./src/textFiles";
        Terminal.command(command);

    }


    public static boolean returnToMenu() {
        Boolean answer = addConfirmationAlert("Returning to Menu", "Are you sure you want to return to menu?", "Yes", "No");
        if (answer) {
            window.setScene(menuScene);
            return true;
        }
        return false;
    }

    /**
     * An alert class which will alert the user with the input message
     * @param confirmationMessage
     */
    public static boolean addConfirmationAlert(String title, String confirmationMessage, String yes, String no) {

        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setContentText(confirmationMessage);

        Optional<ButtonType> result = a.showAndWait();
        if (result.get() == ButtonType.OK) {
            return true;
        } else {
            return false;
        }
    }

    public static void createAlertBox(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.show();
    }

    public static void playVideo(String creationName) {
        CreationPlayer creationPlayer = new CreationPlayer(creationName);
        Scene videoScene = new Scene(creationPlayer.getCreationPlayerLayout(), window.getWidth(), window.getHeight());
        window.setScene(videoScene);

    }

    public static void main(String[] args) {
        launch(args);
    }
}