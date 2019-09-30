package application;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * This class contains all the components for the audio layout in create creations scene.
 * It also contains all the necessary methods regarding audio manipulation and merging.
 */
public class Audio {



    private Button moveUp = new Button("Move Up");
    private Button moveDown = new Button("Move Down");
    private Button previewTextButton = new Button("Preview Selected Text");
    private Button saveAudioButton = new Button("Save Selected Text");
    private Button addAudio = new Button (">>");
    private Button removeAudio = new Button ("<<");
    private Button resetButton = new Button("Reset to Default");
    private Button delete = new Button("Delete");
    private Button deleteAll = new Button("Delete All");
    private Button playSelected = new Button("Play");
    private ComboBox<String> chooseSynthesiser = new ComboBox<>();
    private HBox changeOrderLayout = new HBox(10);
    private HBox chooseSynthLayout = new HBox();
    private HBox audioLayout = new HBox(10);
    private HBox audioButtonLayout = new HBox(10);
    private Label chooseInfo = new Label("Synthesiser ");
    private Label audioFileListLabel = new Label("Existing audio files:");
    private Label audioCreationListLabel = new Label("3) Select audio for creation:");
    private Label audioOptionsLabel = new Label("2) Select audio options");
    private Label currentKeyword = new Label("Current keyword: N/A\nPlease search something");
    private ListView<String> audioFileList = new ListView<String>();
    private ListView<String> audioCreationList = new ListView<String>();
    private String synthChoice = "";
    private String searchTerm = "";
    private TextArea _searchResult;
    private ObservableList<String> selectedAudio = FXCollections.observableArrayList();
    private ObservableList<String> listForCreation = FXCollections.observableArrayList();
    private VBox editCreationAudioLayout = new VBox(20);
    private VBox audioFileListLayout = new VBox(10);
    private VBox audioCreationListLayout = new VBox(10);
    private VBox createAudioButtonsLayout = new VBox(10);

    public Audio(TextArea searchResult) {
        _searchResult = searchResult;
        Main.createFileDirectory("audioFiles");
        setupLayout();
        setupButtons();
    }

    public void setupLayout() {

        //----------------------------SET UP DISABLE BINDINGS------------------------------//
        chooseSynthesiser.disableProperty().bind(_searchResult.textProperty().isEmpty());
        previewTextButton.disableProperty().bind(_searchResult.selectedTextProperty().isEmpty());
        saveAudioButton.disableProperty().bind(_searchResult.selectedTextProperty().isEmpty());
        playSelected.disableProperty().bind(audioFileList.getSelectionModel().selectedItemProperty().isNull());
        delete.disableProperty().bind(audioFileList.getSelectionModel().selectedItemProperty().isNull());
        deleteAll.disableProperty().bind(Bindings.size(audioFileList.getItems()).isEqualTo(0));
        moveDown.disableProperty().bind(audioCreationList.getSelectionModel().selectedItemProperty().isNull());
        moveUp.disableProperty().bind(audioCreationList.getSelectionModel().selectedItemProperty().isNull());
        addAudio.disableProperty().bind(audioFileList.getSelectionModel().selectedItemProperty().isNull());
        removeAudio.disableProperty().bind(audioCreationList.getSelectionModel().selectedItemProperty().isNull());

        //---------------------------AUDIO CREATION LIST LAYOUT----------------------------//
        audioCreationList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        audioCreationList.setPlaceholder(new Label("No audio files for creation"));
        audioCreationList.prefWidthProperty().bind(audioLayout.widthProperty());
        audioCreationList.prefHeightProperty().bind(audioFileList.heightProperty());

        audioCreationListLayout.setAlignment(Pos.CENTER);
        audioCreationListLayout.getChildren().addAll(audioCreationListLabel, audioCreationList, changeOrderLayout);

        //--------------------------------MOVE AUDIO LAYOUT--------------------------------//
        changeOrderLayout.getChildren().addAll(moveUp, moveDown);
        changeOrderLayout.setAlignment(Pos.CENTER);

        editCreationAudioLayout.setMinWidth(50);
        editCreationAudioLayout.setAlignment(Pos.CENTER);
        editCreationAudioLayout.getChildren().addAll(addAudio, removeAudio);

        //-------------------------------AUDIO LAYOUT SETUP--------------------------------//
        audioLayout.prefWidthProperty().bind(_searchResult.widthProperty());
        audioLayout.getChildren().addAll(createAudioButtonsLayout, audioFileListLayout, editCreationAudioLayout, audioCreationListLayout);
        audioLayout.setAlignment(Pos.CENTER);

        //------------------------------AUDIO BUTTONS SETUP--------------------------------//
        previewTextButton.setMaxWidth(180);
        saveAudioButton.setMaxWidth(180);
        currentKeyword.setMinHeight(40);
        createAudioButtonsLayout.getChildren().setAll(audioOptionsLabel, currentKeyword, chooseSynthLayout, previewTextButton, saveAudioButton, resetButton);
        createAudioButtonsLayout.prefWidthProperty().bind(audioLayout.widthProperty());
        createAudioButtonsLayout.setAlignment(Pos.TOP_LEFT);
        previewTextButton.prefWidthProperty().bind(chooseSynthLayout.widthProperty());
        saveAudioButton.prefWidthProperty().bind(chooseSynthLayout.widthProperty());
        resetButton.prefWidthProperty().bind(saveAudioButton.widthProperty());

        //---------------------------------COMBO BOX SETUP---------------------------------//
        chooseSynthesiser.getItems().setAll("Festival", "ESpeak");
        chooseSynthLayout.getChildren().setAll(chooseInfo, chooseSynthesiser);
        chooseSynthLayout.setAlignment(Pos.CENTER_LEFT);

        //-----------------------------AUDIO FILE LIST LAYOUT------------------------------//
        audioFileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        audioFileList.setPlaceholder(new Label("No audio files"));
        audioFileList.prefWidthProperty().bind(audioLayout.widthProperty());
        audioFileList.setMinWidth(audioButtonLayout.getWidth());
        audioButtonLayout.getChildren().setAll(playSelected, delete, deleteAll);
        audioButtonLayout.prefWidthProperty().bind(audioFileList.widthProperty());
        audioButtonLayout.setAlignment(Pos.CENTER);

        audioFileListLayout.getChildren().addAll(audioFileListLabel, audioFileList, audioButtonLayout);
        audioFileListLayout.setAlignment(Pos.CENTER);
    }

    private void setupButtons() {

        //Plays audio of selected text based on speech synthesiser chosen.
        previewTextButton.setOnAction(event -> {

            if (chooseSynthesiser.getValue() == null) {
                Main.createAlertBox("Please select a speech synthesiser");
            } else {

                String selectedText = _searchResult.getSelectedText();
                boolean speak = countMaxWords(selectedText);
                TerminalWorker previewSpeechWorker; //send work to background thread

                if (speak) {

                    previewTextButton.disableProperty().unbind();
                    previewTextButton.setDisable(true);
                    previewTextButton.setText("Playing...");
                    synthChoice = chooseSynthesiser.getValue();

                    if (synthChoice.equals("Festival")) {

                        String command = "echo \"" + selectedText + "\" | festival --tts";
                        previewSpeechWorker = new TerminalWorker(command);
                    } else {

                        String command = "espeak \"" + selectedText + "\"";
                        previewSpeechWorker = new TerminalWorker(command);
                    }

                    Thread th = new Thread(previewSpeechWorker);
                    th.start();

                    previewSpeechWorker.setOnSucceeded(event1 -> {

                        previewTextButton.setDisable(false);
                        previewTextButton.disableProperty().bind(_searchResult.selectedTextProperty().isEmpty());
                        previewTextButton.setText("Preview Selected Text");
                    });
                }
            }
        });

        // Saves the audio file for selected text based on speech synthesiser chosen
        saveAudioButton.setOnAction(event -> {
            if (chooseSynthesiser.getValue() == null) {
                Main.createAlertBox("Please select a speech synthesiser");
            } else {

                searchTerm = CreateCreations.getKeyword();

                Main.createFileDirectory("audioFiles/" + searchTerm); // create directories for audio files if it does not exist
                String selectedText = _searchResult.getSelectedText();
                boolean validRange = countMaxWords(selectedText);

                if (validRange) {

                    // pop up window asking for user to name the audio file
                    TextInputDialog tempAudioFileName = new TextInputDialog();
                    tempAudioFileName.setHeaderText("Enter a name for your audio file");
                    tempAudioFileName.setContentText("Name:");
                    Optional<String> result = tempAudioFileName.showAndWait();
                    synthChoice = chooseSynthesiser.getValue();

                    result.ifPresent(name -> {

                        if (!result.get().isEmpty() && result.get().matches("[a-zA-Z0-9_-]+")) {

                            String command = "";
                            try {

                                String path = "src/audioFiles/" + searchTerm + "/";
                                if (synthChoice.equals("Festival")) {

                                    command = "echo \"" + selectedText + "\" | text2wave -o " + path + name + "_" + synthChoice;
                                } else if (synthChoice.equals("ESpeak")) {

                                    command = "espeak \"" + selectedText + "\" -w " + path + name + "_" + synthChoice + " -s 130";
                                } else {

                                    Main.createAlertBox("Please enter a name for the audio");
                                }
                            } catch (NullPointerException e) {

                                Main.createAlertBox(e.getMessage());
                            }

                            TerminalWorker audioWorker = new TerminalWorker(command);
                            Thread th = new Thread(audioWorker);
                            th.start();

                            audioWorker.setOnSucceeded(event1 -> {

                                getAudioFileList();
                            });
                        } else if (audioExists(name, synthChoice)) {

                            Main.createAlertBox("Audio file already exists. Please rename.");
                        } else {

                            Main.createAlertBox("Available characters: a-z  A-Z  0-9  _- \n(spaces are NOT allowed)");
                        }
                    });
                }
            }
        });

        //delete all audio files for keyword on confirmation by user
        deleteAll.setOnAction(event -> {

            searchTerm = CreateCreations.getKeyword();
            boolean clearAudio = Main.addConfirmationAlert("Delete all audio files", "Are you sure you want to delete all existing audio files?", "Yes", "No");

            if (clearAudio) {

                String command = "rm -r -f src/audioFiles/" + searchTerm;
                Terminal.command(command);
                getAudioFileList();
                listForCreation.clear();
                audioCreationList.setItems(listForCreation);
            }
        });


        //delete selected audio files for keyword
        delete.setOnAction(event -> {

            searchTerm = CreateCreations.getKeyword();
            List<String> deleteAudioList = new ArrayList<String>();
            selectedAudio = audioFileList.getSelectionModel().getSelectedItems();

            for (String audioName: selectedAudio) { //get list of selected audios to be deleted

                String command = "rm -f src/audioFiles/" + searchTerm + "/" + audioName;
                Terminal.command(command);
                deleteAudioList.add(audioName);
            }
            for (String audioName: deleteAudioList) {

                while (listForCreation.contains(audioName)) {

                    listForCreation.remove(audioName);
                }
                if (listForCreation.isEmpty()) {

                    String command = "rm -r -f src/audioFiles/" + searchTerm + "/" + audioName;
                    Terminal.command(command);
                }
            }

            //update lists
            getAudioFileList();
            audioCreationList.setItems(listForCreation);
        });

        //play selected audio file
        playSelected.setOnAction(event -> {

            selectedAudio = audioFileList.getSelectionModel().getSelectedItems();
            if (selectedAudio.size() > 1) {

                Main.createAlertBox("Please only select one audio to play");
            } else {

                searchTerm = CreateCreations.getKeyword();
                String command = "play src/audioFiles/" + searchTerm + "/" + selectedAudio.get(0);
                TerminalWorker playSelectedWorker = new TerminalWorker(command); //send to background thread
                Thread th = new Thread(playSelectedWorker);
                th.start();
                playSelected.disableProperty().unbind();
                playSelected.setDisable(true);


                playSelectedWorker.setOnSucceeded(event1 -> {

                    playSelected.setDisable(false);
                    playSelected.disableProperty().bind(audioFileList.getSelectionModel().selectedItemProperty().isNull());
                    playSelected.setText("Play");
                });
            }
        });

        //add audio to include in creation
        addAudio.setOnAction(event -> {

            try {

                addToCreationButton(event);
            } catch (IOException e) {

                e.printStackTrace();
            }
        });

        //remove audio from creation
        removeAudio.setOnAction(event -> {

            try {

                RemoveAudioFromCreationButton(event);
            } catch (IOException e) {

                e.printStackTrace();
            }
        });

        //shift order of audio recording in creation
        moveUp.setOnAction(event -> {

            if (audioCreationList.getSelectionModel().getSelectedItems().size()>1) {

                Main.createAlertBox("Please select only one to change order");
            } else {

                String audioName = audioCreationList.getSelectionModel().getSelectedItem();
                int originalPos = listForCreation.indexOf(audioName);
                if (originalPos != 0) {

                    Collections.swap(listForCreation, originalPos, originalPos - 1);
                }
            }
            audioCreationList.setItems(listForCreation); //update list
        });

        //shift order of audio recording in creation
        moveDown.setOnAction(event -> {

            if (audioCreationList.getSelectionModel().getSelectedItems().size()>1) {

                Main.createAlertBox("Please select only one to change order");
            } else {

                String audioName = audioCreationList.getSelectionModel().getSelectedItem();
                int originalPos = listForCreation.indexOf(audioName);
                if (originalPos != listForCreation.size()-1) {

                    Collections.swap(listForCreation, originalPos, originalPos + 1);
                }
            }
            audioCreationList.setItems(listForCreation); //update list
        });
    }

    //checks if audio file exists
    private boolean audioExists(String name, String synthChoice) {

        List<String> existingAudio = audioFileList.getItems();
        for (String audioName: existingAudio) {

            if (audioName.equals(name + "_" + synthChoice)) {
                return true;
            }
        }
        return false;
    }

    //gets list of audio files related to the keyword
    public ListView<String> getAudioFileList() {

        audioFileList.getItems().clear();
        searchTerm = CreateCreations.getKeyword();
        String path = System.getProperty("user.dir") + "/src/audioFiles/" + searchTerm;
        File folder = new File(path);
        if (folder.exists()) {

            File[] listOfFiles = folder.listFiles();
            Arrays.sort(listOfFiles, (f1, f2) -> f1.compareTo(f2));
            for (File file : listOfFiles) {

                if (file.isFile()) {

                    audioFileList.getItems().add(file.getName());
                }
            }
        }
        return audioFileList;
    }

    //checks that selected text in within 30 words
    public boolean countMaxWords(String selectedText) {

        String[] words = selectedText.split("\\s+");
        if (words.length > 30) {

            Main.createAlertBox("Chunk cannot be more than 30 words, try a smaller chunk");
            return false;
        }
        return true;
    }

    public HBox getLayout() {
        return audioLayout;
    }

    //merges all audio files chosen to be included in the creation
    public double mergeAudio(File creationName) {

        if (audioCreationList.getItems().size() > 0) {

            searchTerm = CreateCreations.getKeyword();
            String path = "src/audioFiles/" + searchTerm + "/";
            selectedAudio = audioCreationList.getItems();

            //start building bash command line to merge files
            String command = "ffmpeg ";
            int count = 0;
            for (String fileName : selectedAudio) {

                command += "-i " + path + fileName + " ";
                count++;
            }

            command += "-filter_complex '";
            for (int i = 0; i < count; i++) {

                command += "[" + i + ":0]";
            }
            String outputPath = creationName.getPath() + "/." + searchTerm + ".wav";
            command += "concat=n=" + count + ":v=0:a=1[out]' -map '[out]' " + outputPath;
            Terminal.command(command); //run bash command to merge audio files
            String getLengthCommand = "soxi -D " + outputPath;
            double duration = Double.parseDouble(Terminal.command(getLengthCommand));
            return duration;
        } else {
            return -1;
        }
    }

    //implementation to add audio into creation
    public void addToCreationButton(ActionEvent actionEvent) throws IOException {

        for (String word : audioFileList.getSelectionModel().getSelectedItems()){

            listForCreation.add(word);
        }
        audioCreationList.setItems(listForCreation);
    }

    //implementation to remove audio from creation
    public void RemoveAudioFromCreationButton(ActionEvent actionEvent) throws IOException {

        List<String> deleteAudioList = new ArrayList<String>();
        for (String word : audioCreationList.getSelectionModel().getSelectedItems()){

            deleteAudioList.add(word);
        }
        for (String word: deleteAudioList) {

            listForCreation.remove(word);
        }
        audioCreationList.setItems(listForCreation);
    }

    //resets audio lists
    public void refreshAudioInfo() {


        chooseSynthesiser.setValue(null);
        getAudioFileList();
        listForCreation.clear();
        audioCreationList.setItems(listForCreation);
    }

    public Button getResetButton() {
        return resetButton;
    }

    public Label getCurrentKeywordLabel() {
        return currentKeyword;
    }
}

