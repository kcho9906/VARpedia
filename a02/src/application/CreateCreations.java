package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.File;

/**
 * This class is responsible for creating all the creations. It also
 * contains all the components for the layout. All button events involved
 * in creating creations is in this class
 */
public class CreateCreations {

    private Audio audio;
    private Button returnToMenuButton2 = new Button("Return to menu");
    private Button createButton = new Button("Create new creation");
    private Button searchButton = new Button("Search");
    private Button reset;
    private HBox searchLayout = new HBox();
    private HBox configureCreationsLayout = new HBox();
    private Label createLabel = new Label("5) Name Creation");
    private Label progressBarLabel = new Label("");
    private Label flickrInfoLabel = new Label("4) Select number of images included in creation");
    private Label currentKeyWord;
    private ProgressBar progressBar = new ProgressBar(0);
    private Slider flickrImageSlider = new Slider(1,10,5);
    private static String keyword = "";
    private StringProperty searchTerm = new SimpleStringProperty(keyword);
    private TextField searchInput = new TextField();
    private TextField creationNameInput = new TextField();
    private TextArea searchResult = new TextArea();
    private VBox createCreationsLayout;

    public CreateCreations() {
        setUpLayout();
        setActions();
        defaultSettings();
    }

    public void setUpLayout() {

        //----------------------------AUDIO LIST SETUP-------------------------------------//
        audio = new Audio(searchResult);
        reset = audio.getResetButton();
        currentKeyWord = audio.getCurrentKeywordLabel();


        //----------------------------SET UP DISABLE BINDINGS------------------------------//
        searchButton.disableProperty().bind(searchInput.textProperty().isEmpty());

        //-----------------------------SEARCH INPUT LAYOUT---------------------------------//
        searchLayout.getChildren().addAll(progressBarLabel, searchInput, searchButton);
        searchLayout.setAlignment(Pos.CENTER);
        searchLayout.setSpacing(20);

        progressBar.prefWidthProperty().bind(searchResult.widthProperty());
        progressBarLabel.setAlignment(Pos.CENTER_RIGHT);
        searchResult.setWrapText(true);
        searchResult.setMinHeight(250);
        searchButton.setMinWidth(80);

        //--------------------------CREATING CREATION INPUT LAYOUT--------------------------//
        creationNameInput.prefWidthProperty().bind(searchInput.widthProperty());
        configureCreationsLayout.getChildren().addAll(createLabel, creationNameInput, createButton, returnToMenuButton2);
        configureCreationsLayout.setSpacing(10);
        configureCreationsLayout.setAlignment(Pos.CENTER);
        returnToMenuButton2.setPrefWidth(150);

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
        returnToMenuButton2.setOnAction(e -> {
            e.consume();
            Main.returnToMenu();
        });

        // search for the term on Wikipedia
        searchButton.setOnAction(event -> {

            keyword = (searchInput.getText().trim());
            // use the terminal to wikit the term with a worker / task
            currentKeyWord.setText("Current Keyword: " + keyword);
            TerminalWorker wikitWorker = new TerminalWorker("wikit " + keyword);

            // start the progress bar
            startProgressBar("Searching for ...", wikitWorker);

            wikitWorker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    String result = "\"" + wikitWorker.getValue().trim() + "\"";
                    if (result.contains("not found :^(")) {
                        finishProgressBar("Term not found");
                        Main.createAlertBox("Did not find term \"" + searchInput.getText() + "\". Please try again");
                        defaultSettings();
                    } else {
                        // Display the sentences in the display area
                        searchResult.setText(wikitWorker.getValue().trim());
                        finishProgressBar("1b) Highlight (editable) text below for ");
                        searchResult.setDisable(false);
                        audio.refreshAudioInfo();
                    }
                }
            });

            Thread th = new Thread(wikitWorker);
            th.start();
        });

        // code executed once the user is happy with the creation settings
        createButton.setOnAction(event -> {

            // checks if keyword is enmpty
            if (keyword.equals("")) {

                Main.createAlertBox("No keyword entered.\nPlease search something at step 1a)");
            } else { // gets all settings and creates the creation

                String input = creationNameInput.getText().trim();
                String action = "";
                if (!input.isEmpty() && input.matches("[a-zA-Z0-9_-]+")) {

                    File creationDir = new File("src/creations/" + input);
                    if (creationDir.exists()) { // checks if the file exists

                        Boolean overwrite = Main.addConfirmationAlert("ERROR", "\"" + input + "\" exists. \nRename or overwrite?", "Overwrite", "Rename");
                        if (overwrite) {

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

                    //create creation worker to create creation
                    CreationWorker creationWorker = new CreationWorker(action, creationDir, numImages, audio, keyword);

                    //start the progress bar
                    startProgressBar("Creating Creation...", creationWorker);
                    creationWorker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                        @Override
                        public void handle(WorkerStateEvent event) {

                            // Display the sentences in the display area
                            String result = creationWorker.getValue();
                            if (result.contains("Success")) {

                                finishProgressBar(result);
                                boolean play = Main.addConfirmationAlert("\"" + creationDir.getName() + "\" was created successfully!", "Play creation?", "Yes", "No");

                                if (play) {

                                    File file = new File (creationDir.getPath() + "/" + creationDir.getName() + ".mp4");
                                    Main.playVideo(file);
                                }
                            } else {

                                finishProgressBar(result);
                                Main.createAlertBox("Error creating creation \"" + creationDir.getName() + "\"\n" + result);
                            }
                        }
                    });

                    Thread th = new Thread(creationWorker);
                    th.start();
                } else {

                    Main.createAlertBox("Available characters: a-z  A-Z  0-9  _- \n(spaces are NOT allowed)");
                    creationNameInput.clear();
                }
            }
        });

        reset.setOnAction(event -> {

            boolean clear = Main.addConfirmationAlert("Clearing application page", "Are you sure you want to do this?\n All search info will be cleared", "Yes", "No");
            if (clear) {

                defaultSettings();
            }
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

    // resets the application/scene to the default settings
    public void defaultSettings() {

        searchInput.clear();
        searchResult.clear();
        creationNameInput.clear();
        searchResult.setDisable(true);
        progressBarLabel.setText("1a) Please search something");
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        keyword = "";
        audio.refreshAudioInfo();
        searchTerm.setValue(keyword);
        currentKeyWord.setText("Current keyword: N/A\nEnter one at step 1a)");
    }

    // gets the searched term
    public static String getKeyword(){

        return keyword;
    }
}