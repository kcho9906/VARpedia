package sample;

import com.sun.javaws.progress.Progress;
import javafx.beans.binding.Bindings;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.attribute.FileTime;
import java.util.Optional;

public class CreateCreations {
    private Stage window;
    private Button returnToMenuButton2 = new Button("Return to menu");
    private HBox searchLayout = new HBox();
    private HBox configureCreationsLayout = new HBox();
    private HBox highlightedAudioTextLayout = new HBox();
    private TextField searchInput = new TextField();
    private TextField lineInput = new TextField();
    private TextField creationNameInput = new TextField();
    private TextArea searchResult = new TextArea();
    private Button createButton = new Button("Create new creation");
    private Button searchButton = new Button("Search");
    private Button highlightedTextButton = new Button("Preview Selected Text");
    private Button saveHighlightedTextButton = new Button("Save Selected Text");
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
        searchLayout.setPadding(new Insets(10, 10, 10, 10));
        searchLayout.getChildren().addAll(searchInput, searchButton);
        searchLayout.setAlignment(Pos.CENTER);
        searchLayout.setSpacing(10);

        //-----------------------------LIST VIEW AUDIO CLIPS-------------------------------//
        audioFileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        audioFileList.setPrefHeight(80); // temporary height, can change later
        audioFileList.setPlaceholder(new Label("No audio files created"));

        //--------------------------HIGHLIGHTED AUDIO TEXT LAYOUT--------------------------//
        highlightedAudioTextLayout.prefWidthProperty().bind(progressBar.widthProperty());
        highlightedAudioTextLayout.setPadding(new Insets(10, 10, 10, 10));
        highlightedAudioTextLayout.getChildren().addAll(highlightedTextButton, saveHighlightedTextButton, audioFileList);
        highlightedAudioTextLayout.setAlignment(Pos.CENTER);
        highlightedAudioTextLayout.setSpacing(10);

        //--------------------------CREATING CREATION INPUT LAYOUT--------------------------//
        configureCreationsLayout.setPadding(new Insets(10, 10, 10, 10));
        configureCreationsLayout.getChildren().addAll(lineInput, creationNameInput);
        configureCreationsLayout.setAlignment(Pos.CENTER);
        configureCreationsLayout.setSpacing(10);

        //------------------------------CREATE CREATIONS LAYOUT------------------------------//
        createCreationsLayout = new VBox(20);
        createCreationsLayout.getChildren().addAll(searchLayout, progressBarLabel, progressBar, searchResult, highlightedAudioTextLayout, configureCreationsLayout, createButton, returnToMenuButton2);
        createCreationsLayout.setAlignment(Pos.CENTER);
    }

    public void setActions() {

        // button to return to main menu
        returnToMenuButton2.setPrefWidth(200);
        returnToMenuButton2.setOnAction(e -> {
            e.consume();
            Main.returnToMenu();
        });

        // search for the term on Wikipedia
        searchButton.setOnAction(event -> {
            // use the terminal to wikit the term with a worker / task
            WikitWorker wikitWorker = new WikitWorker(searchInput.getText());

            // start the progress bar
            progressBar.progressProperty().bind(wikitWorker.progressProperty());

            wikitWorker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    // Display the sentences in the display area
                    searchResult.setText(wikitWorker.getValue().trim());

                    progressBar.progressProperty().unbind();
                    progressBar.setProgress(0);
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
            createAudioFileDirectory("textFiles");

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
                    // add a text file with the temporary text
                    String command = "echo " + selectedText + " > ./src/textFiles/" + name + ".txt";
                    Terminal.command(command);
                    audioFileList.getItems().add(name);

                });
            }
        });
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
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Chunk cannot be more than 30 words, try a smaller chunk");
            alert.show();
            return false;
        }

        return true;
    }
}