package sample;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
    private HBox chooseSynthLayout = new HBox();
    private VBox createAudioButtonsLayout = new VBox();
    private HBox highlightedAudioTextLayout = new HBox();
    private Label chooseInfo = new Label("Synthesiser ");
    private HBox flickrImagesLayout = new HBox();
    private TextField searchInput = new TextField();
    private TextField creationNameInput = new TextField();
    private TextArea searchResult = new TextArea();
    private Button createButton = new Button("Create new creation");
    private Button searchButton = new Button("Search");
    private Button highlightedTextButton = new Button("Preview Selected Text");
    private Button saveHighlightedTextButton = new Button("Save Selected Text");
    private ComboBox<String> chooseSynthesiser = new ComboBox<>();
    private Slider flickrImageSlider = new Slider(1,10,5);
    private ListView<String> audioFileList = new ListView<String>();
    private ProgressBar progressBar = new ProgressBar(0);
    private Label progressBarLabel = new Label("");
    private Label selectTextFilesLabel = new Label("Please select the text files above that you want to merge");
    private Label nameCreationLabel = new Label("What would you like to name your creation?");
    private Label flickrInfoLabel = new Label("Select number of images included in creation");
    private VBox createCreationsLayout;
    private String synthChoice = "";

    public CreateCreations() {
        defaultSettings();
        setUpLayout();
        setActions();
    }

    public void setUpLayout() {

        //----------------------------SET UP DISABLE BINDINGS------------------------------//

        searchButton.disableProperty().bind(searchInput.textProperty().isEmpty());
        chooseSynthesiser.disableProperty().bind(searchResult.textProperty().isEmpty());
        highlightedTextButton.disableProperty().bind(chooseSynthesiser.valueProperty().isNull());
        saveHighlightedTextButton.disableProperty().bind(chooseSynthesiser.valueProperty().isNull());
        creationNameInput.disableProperty().bind(Bindings.isEmpty(audioFileList.getSelectionModel().getSelectedItems()));

        //-----------------------------SEARCH INPUT LAYOUT---------------------------------//
        progressBar.prefWidthProperty().bind(searchResult.widthProperty());

        searchLayout.getChildren().addAll(progressBarLabel, searchInput, searchButton);
        searchLayout.setAlignment(Pos.CENTER);
        searchLayout.setSpacing(20);

        searchResult.setWrapText(true);
        searchResult.setMinHeight(250);

        //-----------------------------LIST VIEW AUDIO CLIPS-------------------------------//
        audioFileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //audioFileList.setPrefHeight(80); // temporary height, can change later
        audioFileList.setPlaceholder(new Label("No audio files created"));

        //---------------------------------COMBO BOX SETUP---------------------------------//
        chooseSynthLayout = new HBox();
        chooseSynthesiser.getItems().setAll("Festival", "ESpeak");
        chooseSynthLayout.getChildren().setAll(chooseInfo, chooseSynthesiser);
        chooseSynthLayout.setAlignment(Pos.CENTER);


        //--------------------------HIGHLIGHTED AUDIO TEXT LAYOUT--------------------------//
        createAudioButtonsLayout.getChildren().setAll(chooseSynthLayout, highlightedTextButton, saveHighlightedTextButton);
        highlightedTextButton.prefWidthProperty().bind(chooseSynthLayout.widthProperty());
        saveHighlightedTextButton.prefWidthProperty().bind(chooseSynthLayout.widthProperty());
        createAudioButtonsLayout.setSpacing(10);
        audioFileList.prefWidthProperty().bind(searchInput.widthProperty());

        highlightedAudioTextLayout.prefWidthProperty().bind(progressBar.widthProperty());
        highlightedAudioTextLayout.getChildren().addAll(audioFileList, createAudioButtonsLayout);
        highlightedAudioTextLayout.setAlignment(Pos.CENTER);
        highlightedAudioTextLayout.setSpacing(10);

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
        createCreationsLayout.getChildren().addAll(searchLayout, progressBar, searchResult, highlightedAudioTextLayout, flickrInfoLabel, flickrImageSlider, configureCreationsLayout);
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

        // preview audio button
        highlightedTextButton.setOnAction(event -> {
            String selectedText = searchResult.getSelectedText();
            boolean speak = countMaxWords(selectedText);
            speechWorker previewSpeechWorker = new speechWorker(selectedText, "ESpeak"); //default preview speech to ESpeak

            boolean exists = checkAudioExist(); //check if audio exists.
            if (speak) {
                synthChoice = chooseSynthesiser.getValue();
                if (synthChoice.equals("Festival")) {
                    previewSpeechWorker = new speechWorker(selectedText, "Festival");
                } else {
                    previewSpeechWorker = new speechWorker(selectedText, "ESpeak");
                }
                Thread th = new Thread(previewSpeechWorker);
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
                synthChoice = chooseSynthesiser.getValue();

                result.ifPresent(name -> {
                    String command = "";
                    try {
                        if (synthChoice.equals("Festival")) { //if user selected festival - need to put in background GUI
                            command = "echo \"" + selectedText + "\" | text2wave -o ./src/audioFiles/" + name + "-" + synthChoice;
                        } else if (synthChoice.equals("ESpeak")){
                            command = "espeak \"" + selectedText + "\" -w ./src/audioFiles/" + name + "-" + synthChoice + " -s 130";
                        } else {
                            System.out.println("nothing selected");
                        }
                    } catch (NullPointerException e) {
                        System.out.println("error");
                    }


                    // save the audio file based on choice of user's speech synthesis
                    Terminal.command(command);
                    audioFileList.getItems().add(name + "-" + synthChoice);
                    System.out.println("finished making audio file " + name);
                    // add a text file with the temporary text
                    String textCommand = "echo " + selectedText + " > ./src/textFiles/" + name + ".txt";
                    Terminal.command(textCommand);
                });
            }
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
                CreationWorker creationWorker = new CreationWorker(action, creationDir, numImages);
                //start the progress bar
                startProgressBar("Creating Creation...", creationWorker);
                creationWorker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                    @Override
                    public void handle(WorkerStateEvent event) {

                        // Display the sentences in the display area
                        String message = "Creation \"" + creationDir.getName() + "\" was made ";
                        Boolean result = creationWorker.getValue();
                        if (result) {
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

    private boolean checkAudioExist() {
        //
        return true;
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
            Main.createAlertBox("Chunk cannot be more than 30 words, try a smaller chunk");
            return false;
        }
        return true;
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
        getAudioList();
    }

    private void getAudioList() {
        String path = System.getProperty("user.dir") + "/src/audioFiles";
        System.out.println(path);

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                System.out.println(file.getName());
                audioFileList.getItems().add(file.getName());
            }
        }


    }
}