package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

/**
 * JavaFX GUI application which lets the user search for a term on Wikipedia.
 * They will be given text from the search, the user can select chunks of text
 * and merge them into one audio file. Alongside this, the user can select
 * images to be made into a video with the superimposed word. The video can
 * then be played back to the user.
 *
 * @Author Charles Paterson
 * @Author Steven Ho
 */
public class Main extends Application {

    public static Stage window;
    public static Scene menuScene;

    @Override
    public void start(Stage primaryStage) throws Exception{

        // sets the dimensions of the application
        window = primaryStage;
        window.setHeight(700);
        window.setWidth(850);
        window.setMinHeight(700);
        window.setMinWidth(850);

        // creates the files to be used
        createFileDirectory("audioFiles");
        createFileDirectory("creations");

        Menu menu = new Menu(window);
        VBox menuLayout = menu.getMenuLayout();
        menuScene = new Scene(menuLayout, window.getWidth(), window.getHeight());

        window.setOnCloseRequest(event -> {

            event.consume();
            Boolean answer = addConfirmationAlert("Exiting Program", "Are you sure you want to quit?", "Quit", "Cancel");
            if (answer) {

                window.close();
                Platform.exit();
                System.exit(0);
            }
        });

        // sets the title and shows the window
        window.setTitle("VARpedia");
        window.setResizable(true);
        window.setScene(menuScene);
        window.show();
    }

    public static void returnToMenu() {

        window.setScene(menuScene);
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

    /**
     * Creates a warning alert when required to warn user
     * @param message
     */
    public static void createAlertBox(String message) {

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.show();
    }

    /**
     * plays the video through the creation player
     * @param creationName
     */
    public static void playVideo(File creationName) {

        CreationPlayer creationPlayer = new CreationPlayer(creationName);
        Scene videoScene = new Scene(creationPlayer.getCreationPlayerLayout(), window.getWidth(), window.getHeight());
        window.setScene(videoScene);
    }

    /**
     * Creates a file directory of a given name
     * @param directory
     */
    public static void createFileDirectory(String directory) {

        try {

            new File("./src/" + directory).mkdir();
        } catch (Exception e) {

            createAlertBox("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        launch(args);
    }
}