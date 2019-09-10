package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception{

        Menu menu = new Menu(primaryStage);
        VBox menuLayout = menu.getMenuLayout();

        ViewCreations viewCreations = new ViewCreations(primaryStage);
        VBox viewCreationsLayout = viewCreations.getViewCreationsLayout();

        CreateCreations createCreations = new CreateCreations(primaryStage);
        VBox createCreationsLayout = createCreations.getCreateCreationsLayout();


        Scene root = new Scene(menuLayout, 600, 600);
        Scene viewCreationsScene = new Scene(viewCreationsLayout, 600, 600);
    //    Scene createCreations = new Scene(createCreationsLayout, 600,600);
   //     videoScene = new Scene(mediaLayout, 600, 600);

        primaryStage.setTitle("VARpedia");
        primaryStage.setScene(viewCreationsScene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
