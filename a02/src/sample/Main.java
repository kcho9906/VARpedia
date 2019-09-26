package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage window;
    public static Scene menuScene;

    @Override
    public void start(Stage primaryStage) throws Exception{

        // create a downloadedImages folder
        String command = "mkdir ./downloadedImages";
        Terminal.command(command);

        window = primaryStage;
        Menu menu = new Menu(window);
        VBox menuLayout = menu.getMenuLayout();
        menuScene = new Scene(menuLayout, 600, 1000);

        FlickrImageExtractor.downloadImages("apple", 10);

        window.setTitle("VARpedia");
        window.setResizable(true);
        window.setScene(menuScene);
        window.show();

        // delete audioFies if closed
        command = "rm -fr ./src/textFiles";
        Terminal.command(command);

        // remove downloadedImages folder if closed
        command = "rm -fr ./downloadedImages";
        Terminal.command(command);

    }

    public static void returnToMenu() {
        Boolean answer = ConfirmBox.display("Confirm action", "Are you sure you want to return to menu?", "Yes", "No");
        if(answer) {
            window.setScene(menuScene);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}