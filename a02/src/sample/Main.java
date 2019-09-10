package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage window;
    public static Scene menuScene;

    @Override
    public void start(Stage primaryStage) throws Exception{

        window = primaryStage;
        Menu menu = new Menu(window);
        VBox menuLayout = menu.getMenuLayout();
        menuScene = new Scene(menuLayout, 600, 600);


        //alert box to confirm quitting of application
        window.setOnCloseRequest(e -> {
            e.consume();
            closeProgram();
        });
        
        window.setTitle("VARpedia");
        window.setScene(menuScene);
        window.show();
    }

    public static void returnToMenu() {
        Boolean answer = ConfirmBox.display("Confirm action", "Are you sure you want to return to menu?", "Yes", "No");
        if(answer) {
            window.setScene(menuScene);
            }
    }

    private static void closeProgram(){
        Boolean answer = ConfirmBox.display("Quit", "Are you sure you want to quit?", "Yes", "No");
        if(answer){
            window.close();
            Platform.exit();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}