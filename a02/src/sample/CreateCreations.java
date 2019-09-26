package sample;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.io.File;
import java.util.Observable;
import java.util.Optional;

public class CreateCreations {
    private Button returnToMenuButton2 = new Button("Return to menu");
    private HBox searchLayout = new HBox();
    private HBox configureCreationsLayout = new HBox();
    private TextField searchInput = new TextField();
    private TextField creationNameInput = new TextField();
    private TextArea searchResult = new TextArea();
    private Button createButton = new Button("Create new creation");
    private Button searchButton = new Button("Search");
    private Slider flickrImageSlider = new Slider(1,10,5);
    private ListView<String> audioFileList;
    private ProgressBar progressBar = new ProgressBar(0);
    private Label progressBarLabel = new Label("");
    private Label flickrInfoLabel = new Label("Select number of images included in creation");
    private VBox createCreationsLayout;
    private Audio audio;


    public CreateCreations() {

        setUpLayout();
        setActions();
        defaultSettings();
    }

    public void setUpLayout() {

        //----------------------------AUDIO LIST SETUP-------------------------------------//
        audio = new Audio(searchResult, progressBar, searchInput);
        audioFileList = audio.getAudioList();


        //----------------------------SET UP DISABLE BINDINGS------------------------------//
        searchButton.disableProperty().bind(searchInput.textProperty().isEmpty());
        creationNameInput.disableProperty().bind(Bindings.isEmpty(audioFileList.getSelectionModel().getSelectedItems()));

        //-----------------------------SEARCH INPUT LAYOUT---------------------------------//
        progressBar.prefWidthProperty().bind(searchResult.widthProperty());

        searchLayout.getChildren().addAll(progressBarLabel, searchInput, searchButton);
        searchLayout.setAlignment(Pos.CENTER);
        searchLayout.setSpacing(20);

        searchResult.setWrapText(true);
        searchResult.setMinHeight(250);


        //--------------------------CREATING CREATION INPUT LAYOUT--------------------------//
        creationNameInput.prefWidthProperty().bind(audioFileList.widthProperty());
        configureCreationsLayout.getChildren().addAll(creationNameInput, createButton, returnToMenuButton2);
        configureCreationsLayout.setSpacing(10);
        configureCreationsLayout.setAlignment(Pos.CENTER);

        //--------------------------GATHERING FLICKR IMAGES LAYOUT---------------------------//
        flickrImageSlider.prefWidthProperty().bind(progressBar.widthProperty());
        flickrImageSlider.setMajorTickUnit(1.0);
        flickrImageSlider.setMinorTickCount(0);
        flickrImageSlider.setShowTickLabels(true);
        flickrImageSlider.setShowTickMarks(true);
        flickrImageSlider.setSnapToTicks(true);

        //------------------------------CREATE CREATIONS LAYOUT------------------------------//
        createCreationsLayout = new VBox(10);
        createCreationsLayout.getChildren().addAll(searchLayout, progressBar, searchResult, audio.getLayout(), flickrInfoLabel, flickrImageSlider, configureCreationsLayout);
        createCreationsLayout.setAlignment(Pos.CENTER);
        createCreationsLayout.setPadding(new Insets(10, 50, 10, 50));
    }

    public void setActions() {

        // button to return to main menu
        returnToMenuButton2.setPrefWidth(150);
        returnToMenuButton2.setOnAction(e -> {

            e.consume();
            boolean confirm = Main.returnToMenu();
            if (confirm) {
                defaultSettings();
            }
        });

        // search for the term on Wikipedia
        searchButton.setOnAction(event -> {

            // use the terminal to wikit the term with a worker / task
            WikitWorker wikitWorker = new WikitWorker(searchInput.getText());

            // start the progress bar
            startProgressBar("Searching for search term...", wikitWorker);

            wikitWorker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    String result = wikitWorker.getValue().trim();
                    if (result.contains("not found :^(")) {
                        finishProgressBar("Term not found");
                        Main.createAlertBox("Did not find term \"" + searchInput.getText() + "\". Please try again");
                        defaultSettings();
                    } else {
                        // Display the sentences in the display area
                        searchResult.setText(wikitWorker.getValue().trim());
                        finishProgressBar("Search term found!");
                        searchResult.setDisable(false);
                    }
                }

            });

            Thread th = new Thread(wikitWorker);
            th.start();
        });

        createButton.setOnAction(event -> { //NEED TO FIX CREATE CREATION

            String input = creationNameInput.getText().trim();
            String action = "";
            if (!input.isEmpty() && input.matches("[a-zA-Z0-9_ -]+")) {
                File creationDir = new File("./src/creations/" + input);
                if (creationDir.exists()) {
                    Boolean overwrite = Main.addConfirmationAlert("ERROR", "\"" + input + "\" exists. \nRename or overwrite?", "Overwrite", "Rename");
                    if (overwrite){
                        action = "overwrite";
                    } else {
                        creationNameInput.clear(); //clears creation name input
                        return;
                    }
                } else {
                    action = "create";
                }


                // we want to take the audio files, concatenate them, make the wave file
                // then combine with the flickr images and the text
                int numImages = (int) flickrImageSlider.getValue();
                // merge selected audio files

                //create creation worker to create creation
                CreationWorker creationWorker = new CreationWorker(action, creationDir, numImages, audio);
                //start the progress bar
                startProgressBar("Creating Creation...", creationWorker);
                creationWorker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                    @Override
                    public void handle(WorkerStateEvent event) {

                        // Display the sentences in the display area
                        String message = "Creation \"" + creationDir.getName() + "\" was made ";
                        String result = creationWorker.getValue();
                        if (result.contains("Success")) {
                            message += "successfully!";
                            finishProgressBar(message);
                            boolean play = Main.addConfirmationAlert(message,  "Play creation?", "Yes", "No");
                            if (play) {
                                Main.playVideo(creationDir.getName());
                            }
                        } else {
                            message += "unsuccessfully...";
                            finishProgressBar(message);
                            Main.createAlertBox("Error creating creation");
                        }


                    }
                });

                Thread th = new Thread(creationWorker);
                th.start();

            } else{
                Main.createAlertBox("Invalid creation name input");
            }

            System.out.println(action);



        });


    }

    public VBox getCreateCreationsLayout() {
        return createCreationsLayout;
    }

    public void startProgressBar(String text, Worker worker) {
        progressBarLabel.setText(text);
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        progressBar.progressProperty().bind(worker.progressProperty());
    }

    public void finishProgressBar(String text) {
        progressBarLabel.setText(text);
        progressBar.progressProperty().unbind();
        progressBar.setProgress(1.0d);
    }

    public void defaultSettings() {
        searchInput.clear();
        searchResult.clear();
        creationNameInput.clear();
        searchResult.setDisable(true);
        progressBarLabel.setText("Please search something");
        progressBar.setProgress(0);
        audio.getAudioList();
    }


}