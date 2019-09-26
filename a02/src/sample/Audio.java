package sample;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class Audio {
    private ListView<String> audioFileList = new ListView<String>();
    private VBox createAudioButtonsLayout = new VBox();
    private HBox chooseSynthLayout = new HBox();
    private HBox highlightedAudioTextLayout = new HBox();
    private Button highlightedTextButton = new Button("Preview Selected Text");
    private Button saveHighlightedTextButton = new Button("Save Selected Text");
    private Label chooseInfo = new Label("Synthesiser ");
    private ComboBox<String> chooseSynthesiser = new ComboBox<>();
    private String synthChoice = "";
    private TextArea _searchResult;
    private TextField _searchInput;
    private ProgressBar _progressBar;

    public Audio(TextArea searchResult, ProgressBar progressBar, TextField searchInput) {
        _searchResult = searchResult;
        _searchInput = searchInput;
        _progressBar = progressBar;
        setupLayout();
        setupButtons();
    }

    public void setupLayout() {

        audioFileList.prefWidthProperty().bind(_searchInput.widthProperty());

        //----------------------------SET UP DISABLE BINDINGS------------------------------//


        chooseSynthesiser.disableProperty().bind(_searchResult.textProperty().isEmpty());
        highlightedTextButton.disableProperty().bind(chooseSynthesiser.valueProperty().isNull());
        saveHighlightedTextButton.disableProperty().bind(chooseSynthesiser.valueProperty().isNull());


        //-----------------------------LIST VIEW AUDIO CLIPS-------------------------------//
        audioFileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //audioFileList.setPrefHeight(80); // temporary height, can change later
        audioFileList.setPlaceholder(new Label("No audio files created"));

        //--------------------------HIGHLIGHTED AUDIO TEXT LAYOUT--------------------------//
        createAudioButtonsLayout.getChildren().setAll(chooseSynthLayout, highlightedTextButton, saveHighlightedTextButton);
        highlightedTextButton.prefWidthProperty().bind(chooseSynthLayout.widthProperty());
        saveHighlightedTextButton.prefWidthProperty().bind(chooseSynthLayout.widthProperty());
        createAudioButtonsLayout.setSpacing(10);
        audioFileList.prefWidthProperty().bind(_searchInput.widthProperty());

        highlightedAudioTextLayout.prefWidthProperty().bind(_progressBar.widthProperty());
        highlightedAudioTextLayout.getChildren().addAll(audioFileList, createAudioButtonsLayout);
        highlightedAudioTextLayout.setAlignment(Pos.CENTER);
        highlightedAudioTextLayout.setSpacing(10);

        //---------------------------------COMBO BOX SETUP---------------------------------//
        chooseSynthesiser.getItems().setAll("Festival", "ESpeak");
        chooseSynthLayout.getChildren().setAll(chooseInfo, chooseSynthesiser);
        chooseSynthLayout.setAlignment(Pos.CENTER);


    }

    private void setupButtons() {
// preview audio button
        highlightedTextButton.setOnAction(event -> {
            mergeAudio("apple");
            String selectedText = _searchResult.getSelectedText();
            boolean speak = countMaxWords(selectedText);
            speechWorker previewSpeechWorker = new speechWorker(selectedText, "ESpeak"); //default preview speech to ESpeak

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
            String selectedText = _searchResult.getSelectedText();

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
                        } else if (synthChoice.equals("ESpeak")) {
                            command = "espeak \"" + selectedText + "\" -w ./src/audioFiles/" + name + "-" + synthChoice + " -s 130";
                        } else {
                            System.out.println("nothing selected");
                        }
                    } catch (NullPointerException e) {
                        System.out.println("error");
                    }


                    // save the audio file based on choice of user's speech synthesis
                    System.out.println(command);
                    Terminal.command(command);
                    getAudioList();
                    // add a text file with the temporary text
                    String textCommand = "echo " + selectedText + " > ./src/textFiles/" + name + ".txt";
                    Terminal.command(textCommand);
                });
            }
        });
    }

    public ListView<String> getAudioList() {
        audioFileList.getItems().clear();

        String path = System.getProperty("user.dir") + "/src/audioFiles";

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                audioFileList.getItems().add(file.getName());
            }
        }
        return audioFileList;
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

    public HBox getLayout() {
        return highlightedAudioTextLayout;
    }

    public void mergeAudio(String creationName) {

        List<String> selectedAudio = audioFileList.getSelectionModel().getSelectedItems();
        String command = "ffmpeg ";
        int count = 0;
        for (String fileName: selectedAudio) {
            command += "-i ./src/audioFiles/" + fileName + " ";
            count++;
        }

        System.out.println(count);
        command += "-filter_complex '";
        for (int i = 0; i < count; i++) {
            command += "[" + i + ":0]";
        }

        command += "concat=n=" + count + ":v=0:a=1[out]' -map '[out]' ./src/creations/" + creationName + "/output.wav";
        System.out.println(command);
        Terminal.command(command);


    }
}

