package sample;

import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public class CreateCreations {
    private Stage window;
    private Button returnToMenuButton2 = new Button("Return to menu");
    private HBox searchLayout = new HBox();
    private HBox configureCreationsLayout = new HBox();
    private HBox chooseSynthLayout = new HBox();
    private VBox createAudioButtonsLayout = new VBox();
    private HBox highlightedAudioTextLayout = new HBox();
    private Label chooseInfo = new Label("Synthesiser");
    private TextField searchInput = new TextField();
    private TextField lineInput = new TextField();
    private TextField creationNameInput = new TextField();
    private TextArea searchResult = new TextArea();
    private Button createButton = new Button("Create new creation");
    private Button searchButton = new Button("Search");
    private Button highlightedTextButton = new Button("Preview Selected Text");
    private Button saveHighlightedTextButton = new Button("Save Selected Text");
    private ComboBox<String> chooseSynthesiser = new ComboBox<>();
    private ListView<String> audioFileList = new ListView<String>();
    private File file, creationDir;
    private ProgressBar progressBar = new ProgressBar(0);
    private int _totalLines;
    private Label progressBarLabel = new Label("");
    private VBox createCreationsLayout;

    public CreateCreations(Stage stage) {
        window = stage;
        setUpLayout();
        setActions();
    }

    public void setUpLayout() {
        //-----------------------------------SEARCH LAYOUT---------------------------------//
        progressBar.prefWidthProperty().bind(searchResult.widthProperty());
        searchResult.setWrapText(true);
        searchResult.setMinHeight(300);
        searchLayout.setPadding(new Insets(10, 10, 10, 10));
        searchLayout.getChildren().addAll(searchInput, searchButton);
        searchLayout.setAlignment(Pos.CENTER);
        searchLayout.setSpacing(10);

        //-----------------------------LIST VIEW AUDIO CLIPS-------------------------------//
        audioFileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //audioFileList.setPrefHeight(80); // temporary height, can change later
        audioFileList.setPlaceholder(new Label("No audio files created"));

        //---------------------------------COMBO BOX SETUP---------------------------------//
        chooseSynthLayout = new HBox();
        chooseSynthesiser.getItems().setAll("Festival", "ESpeak");
        chooseSynthLayout.getChildren().setAll(chooseInfo, chooseSynthesiser);

        //--------------------------HIGHLIGHTED AUDIO TEXT LAYOUT--------------------------//
        highlightedAudioTextLayout.prefWidthProperty().bind(progressBar.widthProperty());
        createAudioButtonsLayout.getChildren().setAll(chooseSynthLayout, highlightedTextButton, saveHighlightedTextButton);
        createAudioButtonsLayout.setAlignment(Pos.CENTER_LEFT);
        createAudioButtonsLayout.setPadding(new Insets(10, 10, 10, 10));
        highlightedAudioTextLayout.setPadding(new Insets(10, 10, 10, 10));
        highlightedAudioTextLayout.getChildren().addAll(createAudioButtonsLayout, audioFileList);
        highlightedAudioTextLayout.setAlignment(Pos.CENTER);
        highlightedAudioTextLayout.setSpacing(10);
        //default buttons to be disabled
        chooseSynthesiser.disableProperty().bind(searchResult.textProperty().isEmpty());
        highlightedTextButton.disableProperty().bind(chooseSynthesiser.valueProperty().isNull());
        saveHighlightedTextButton.disableProperty().bind(chooseSynthesiser.valueProperty().isNull());

        //--------------------------CREATING CREATION INPUT LAYOUT--------------------------//
        configureCreationsLayout.setPadding(new Insets(10, 10, 10, 10));
        configureCreationsLayout.getChildren().addAll(creationNameInput, createButton);
        configureCreationsLayout.setAlignment(Pos.CENTER);
        configureCreationsLayout.setSpacing(10);

        //------------------------------CREATE CREATIONS LAYOUT------------------------------//
        createCreationsLayout = new VBox(20);
        createCreationsLayout.getChildren().addAll(searchLayout, progressBarLabel, progressBar, searchResult, chooseSynthLayout, highlightedAudioTextLayout, configureCreationsLayout, returnToMenuButton2);
        createCreationsLayout.setAlignment(Pos.CENTER);
    }

    public void setActions() {

        // button to return to main menu
        returnToMenuButton2.setPrefWidth(150);
        returnToMenuButton2.setOnAction(e -> {
            e.consume();
            Main.returnToMenu();
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
                    // Display the sentences in the display area
                    searchResult.setText(wikitWorker.getValue().trim());
                    finishProgressBar("Search term found!");
                }

            });

            Thread th = new Thread(wikitWorker);
            th.start();
        });

        // preview audio button
        highlightedTextButton.setOnAction(event -> {
            String selectedText = searchResult.getSelectedText();
            boolean speak = countMaxWords(selectedText);

            if (speak) {
                // run the espeak command through a worker so the GUI doesn't freeze
                eSpeakWorker espeakWorker = new eSpeakWorker(selectedText);

                Thread th = new Thread(espeakWorker);
                th.start();
            }
        });

        // button which saves the highlighted text
        saveHighlightedTextButton.setOnAction(event -> {
            // if there is no temporary directory for audio files
            // create one
            createAudioFileDirectory("audioFiles");

            // get the selected text and save it to an audio file
            String selectedText = searchResult.getSelectedText();

            boolean create = countMaxWords(selectedText);

            if (create) {

                // have a pop up ask for a name for the audio file?
                TextInputDialog tempAudioFileName = new TextInputDialog();
                tempAudioFileName.setHeaderText("Enter a name for your audio file");
                tempAudioFileName.setContentText("Name:");
                Optional<String> result = tempAudioFileName.showAndWait();

                result.ifPresent(name -> {
                    String command = "";
                    String synthChoice = "";
                    try {
                        synthChoice = chooseSynthesiser.getValue();
                        if (synthChoice.equals("Festival")) { //if user selected festival - need to put in background GUI
                            command = "echo " + selectedText + " | text2wave -o " + name + "; lame " + name + " " + "./src/audioFiles/" + name + ".mp3";
                        } else if (synthChoice.equals("ESpeak")){
                            command = "espeak \"" + selectedText + "\" -w ./src/audioFiles/" + name + " -s 130";
                        } else {
                            System.out.println("nothing selected");
                        }
                    } catch (NullPointerException e) {
                        System.out.println("error");
                    }


                    // save the audio file based on choice of user's speech synthesis
                    Terminal.command(command);
                    audioFileList.getItems().add(name);
                });
            }
        });

        createButton.setOnAction(event -> {

            String input = creationNameInput.getText().trim();

            //create creation worker to create creation
            CreationWorker creationWorker = new CreationWorker(input);

            //start the progress bar
            startProgressBar("Creating Creation...", creationWorker);

            creationWorker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {

                    // Display the sentences in the display area
                    String result = creationWorker.getValue().trim();
                    if (result.equals("Success")) {
                        createAlertBox("Creation made successfully! Play creation?");
                    }

                   finishProgressBar(result);

                }
            });

            Thread th = new Thread(creationWorker);
            th.start();
        });
    }

    public void makeCreationDir(File dir) {
        dir.mkdir();
    }

    public VBox getCreateCreationsLayout() {
        return createCreationsLayout;
    }

    public void createAudioFileDirectory(String directory) {
        // create the temporary audio file
        try {
            boolean success = new File("./src/" + directory).mkdir();

        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
    }

    public boolean countMaxWords(String selectedText) {
        String[] words = selectedText.split("\\s+");
        if (words.length > 30) {
            createAlertBox("Chunk cannot be more than 30 words, try a smaller chunk");
            return false;
        }
        return true;
    }

    public void createAlertBox(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        Alert alert1 = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText(message);
        alert.show();
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


}