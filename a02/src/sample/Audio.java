package sample;

import javafx.beans.binding.Bindings;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Audio {
    private ListView<String> audioFileList = new ListView<String>();
    private VBox createAudioButtonsLayout = new VBox(10);
    private HBox chooseSynthLayout = new HBox();
    private HBox highlightedAudioTextLayout = new HBox(10);
    private Button highlightedTextButton = new Button("Preview Selected Text");
    private Button saveHighlightedTextButton = new Button("Save Selected Text");
    private Label chooseInfo = new Label("Synthesiser ");
    private ComboBox<String> chooseSynthesiser = new ComboBox<>();
    private String synthChoice = "";
    private TextArea _searchResult;
    private TextField _searchInput;
    private ProgressBar _progressBar;
    private Button delete = new Button("Delete");
    private Button deleteAll = new Button("Delete All");
    private HBox audioButtonLayout = new HBox(10);
    private Button playSelected = new Button("Play");
    private VBox audioListLayout = new VBox(10);
    private List<String> selectedAudio = audioFileList.getSelectionModel().getSelectedItems();;

    public Audio(TextArea searchResult, ProgressBar progressBar, TextField searchInput) {
        _searchResult = searchResult;
        _searchInput = searchInput;
        _progressBar = progressBar;
        setupLayout();
        setupButtons();
    }

    public void setupLayout() {


        //----------------------------SET UP DISABLE BINDINGS------------------------------//
        chooseSynthesiser.disableProperty().bind(_searchResult.textProperty().isEmpty());
        highlightedTextButton.disableProperty().bind(chooseSynthesiser.valueProperty().isNull());
        saveHighlightedTextButton.disableProperty().bind(chooseSynthesiser.valueProperty().isNull());
        playSelected.disableProperty().bind(audioFileList.getSelectionModel().selectedItemProperty().isNull());
        delete.disableProperty().bind(audioFileList.getSelectionModel().selectedItemProperty().isNull());
        deleteAll.disableProperty().bind(Bindings.size(audioFileList.getItems()).isEqualTo(0));
        //-------------------------------SET UP BUTTON SIZES-------------------------------//
        highlightedTextButton.setMaxWidth(180);
        saveHighlightedTextButton.setMaxWidth(180);
        //-----------------------------LIST VIEW AUDIO CLIPS-------------------------------//
        audioFileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //audioFileList.setPrefHeight(80); // temporary height, can change later
        audioFileList.setPlaceholder(new Label("No audio files created"));
        audioFileList.prefWidthProperty().bind(highlightedAudioTextLayout.widthProperty());

        //--------------------------HIGHLIGHTED AUDIO TEXT LAYOUT--------------------------//
        createAudioButtonsLayout.getChildren().setAll(chooseSynthLayout, highlightedTextButton, saveHighlightedTextButton, audioListLayout);
        createAudioButtonsLayout.prefWidthProperty().bind(highlightedAudioTextLayout.widthProperty());
        createAudioButtonsLayout.setAlignment(Pos.TOP_RIGHT);
        highlightedTextButton.prefWidthProperty().bind(chooseSynthLayout.widthProperty());
        saveHighlightedTextButton.prefWidthProperty().bind(chooseSynthLayout.widthProperty());

        highlightedAudioTextLayout.prefWidthProperty().bind(_searchResult.widthProperty());
        highlightedAudioTextLayout.getChildren().addAll(audioListLayout, createAudioButtonsLayout);
        highlightedAudioTextLayout.setAlignment(Pos.CENTER);

        audioButtonLayout.getChildren().setAll(playSelected, delete, deleteAll);
        audioButtonLayout.prefWidthProperty().bind(audioFileList.widthProperty());
        audioListLayout.getChildren().setAll(audioFileList, audioButtonLayout);
        //---------------------------------COMBO BOX SETUP---------------------------------//
        chooseSynthesiser.getItems().setAll("Festival", "ESpeak");
        chooseSynthLayout.getChildren().setAll(chooseInfo, chooseSynthesiser);
        chooseSynthLayout.setAlignment(Pos.CENTER_RIGHT);


    }

    private void setupButtons() {
// preview audio button
        highlightedTextButton.setOnAction(event -> {
            String selectedText = _searchResult.getSelectedText();
            boolean speak = countMaxWords(selectedText);

            TerminalWorker previewSpeechWorker;

            if (speak) {
                highlightedTextButton.disableProperty().unbind();
                highlightedTextButton.setDisable(true);
                highlightedTextButton.setText("Playing...");
                synthChoice = chooseSynthesiser.getValue();
                if (synthChoice.equals("Festival")) {
                    String command = "echo \"" + selectedText + "\" | festival --tts";
                    previewSpeechWorker = new TerminalWorker(command);
                } else {
                    String command =  "espeak \"" + selectedText + "\"";
                    previewSpeechWorker = new TerminalWorker(command);
                }
                Thread th = new Thread(previewSpeechWorker);
                th.start();
                previewSpeechWorker.setOnSucceeded(event1 -> {
                    highlightedTextButton.setDisable(false);
                    highlightedTextButton.disableProperty().bind(chooseSynthesiser.valueProperty().isNull());
                    highlightedTextButton.setText("Preview Selected Text");
                });
            }

        });

        // button which saves the highlighted text
        saveHighlightedTextButton.setOnAction(event -> {
            // if there is no temporary directory for audio files
            // create one
            createAudioFileDirectory("textFiles");

            // get the selected text and save it to an audio file
            String selectedText = _searchResult.getSelectedText();

            boolean validRange = countMaxWords(selectedText);

            if (validRange) {

                // have a pop up ask for a name for the audio file?
                TextInputDialog tempAudioFileName = new TextInputDialog();
                tempAudioFileName.setHeaderText("Enter a name for your audio file");
                tempAudioFileName.setContentText("Name:");
                Optional<String> result = tempAudioFileName.showAndWait();
                synthChoice = chooseSynthesiser.getValue();

                result.ifPresent(name -> {
                    if (name.isEmpty() || name == null) {
                        Main.createAlertBox("Please enter a name for audio file");
                    } else if (audioExists(name, synthChoice)){
                        Main.createAlertBox("Audio file already exists. Please rename.");
                    } else {
                        String command = "";
                        try {
                            if (synthChoice.equals("Festival")) { //if user selected festival - need to put in background GUI
                                command = "echo \"" + selectedText + "\" | text2wave -o ./src/audioFiles/" + name + "_" + synthChoice;
                            } else if (synthChoice.equals("ESpeak")) {
                                command = "espeak \"" + selectedText + "\" -w ./src/audioFiles/" + name + "_" + synthChoice + " -s 130";
                            } else {
                                System.out.println("nothing selected");
                            }
                        } catch (NullPointerException e) {
                            System.out.println("error");
                        }


                        // save the audio file based on choice of user's speech synthesis
                        Terminal.command(command);
                        getAudioList();
                        // add a text file with the temporary text
                        String textCommand = "echo " + selectedText + " > ./src/textFiles/" + name + ".txt";
                        Terminal.command(textCommand);
                    }
                });
            }
        });

        deleteAll.setOnAction(event -> {
            boolean clearAudio = Main.addConfirmationAlert("Delete all audio files", "Are you sure you want to delete all existing audio files?", "Yes", "No");
            if (clearAudio) {
                String command = "rm ./src/audioFiles/*";
                Terminal.command(command);
                getAudioList();
            }
        });

        delete.setOnAction(event -> {
            for (String audioName: selectedAudio) {
                String command = "rm ./src/audioFiles/" + audioName;
                Terminal.command(command);
            }
            getAudioList();
        });

        playSelected.setOnAction(event -> {
            if (selectedAudio.size() > 1) {
                Main.createAlertBox("Please only select one audio to play");
            } else {
                String command = "play ./src/audioFiles/" + selectedAudio.get(0);
                TerminalWorker playSelectedWorker = new TerminalWorker(command);

                Thread th = new Thread(playSelectedWorker);
                th.start();
                playSelected.disableProperty().unbind();
                playSelected.setDisable(true);

                playSelectedWorker.setOnSucceeded(event1 -> {
                    playSelected.setDisable(false);
                    playSelected.disableProperty().bind(audioFileList.getSelectionModel().selectedItemProperty().isNull());
                });
            }
        });
    }

    private boolean audioExists(String name, String synthChoice) {
        List<String> existingAudio = audioFileList.getItems();
        for (String audioName: existingAudio) {
            if (audioName.equals(name + "_" + synthChoice)) {
                return true;
            }
        }
        return false;
    }

    public ListView<String> getAudioList() {
        audioFileList.getItems().clear();
        String path = System.getProperty("user.dir") + "/src/audioFiles";




        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        Arrays.sort(listOfFiles, (f1, f2)->f1.compareTo(f2));
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

    public double mergeAudio(File creationName) {

        String command = "ffmpeg ";
        int count = 0;
        for (String fileName: selectedAudio) {
            command += "-i ./src/audioFiles/" + fileName + " ";
            count++;
        }

        command += "-filter_complex '";
        for (int i = 0; i < count; i++) {
            command += "[" + i + ":0]";
        }

        String outputPath = creationName.getPath() + "/output.wav";
        command += "concat=n=" + count + ":v=0:a=1[out]' -map '[out]' " + outputPath;
        Terminal.command(command);

        String getLengthCommand = "soxi -D " + outputPath;
        double duration = Double.parseDouble(Terminal.command(getLengthCommand));
        return duration;
    }
}

