package sample;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;

public class CreationPlayer {

    private VBox mediaLayout = new VBox(10);
    public CreationPlayer(String creationName) {
        //setupButtons();
        playVideo(creationName);
    }

    //public void setupButtons(){};


    public void playVideo(String creationName) {
        //create new components for new video player
        File fileUrl = new File("src/creations/" + creationName + "/" + creationName + ".mp4");
        Media video = new Media(fileUrl.toURI().toString());
        MediaPlayer player = new MediaPlayer(video);
        player.setAutoPlay(true);
        MediaView mediaView = new MediaView(player);
        Button returnToMenuButton3 = new Button("Return to menu");
        HBox videoButtonLayout = new HBox();
        Button btnMute = new Button("Mute");
        Button btnPlayPause = new Button("Pause");
        double durationMins = video.getDuration().toMinutes();
        Label timeLabel = new Label();
        Slider volumeBar = new Slider(0, 100, 50);
        Slider timeBar = new Slider ();
        
        final double[] volumeBeforeMute = {0};


        volumeBar.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (oldValue != newValue) {
                    player.setVolume(volumeBar.getValue()/100);
                }
            }
        });

        videoButtonLayout.setStyle("-fx-background-color: ECD8D9;");
        videoButtonLayout.getChildren().addAll(timeLabel, btnPlayPause, returnToMenuButton3, btnMute, volumeBar);
        videoButtonLayout.setPadding(new Insets(10, 10, 10, 10));
        videoButtonLayout.setSpacing(10);

        videoButtonLayout.setAlignment(Pos.BASELINE_CENTER);
        mediaView.fitWidthProperty().bind(mediaLayout.widthProperty());
        mediaView.fitHeightProperty().bind(mediaLayout.heightProperty());
        mediaLayout.getChildren().addAll(mediaView, timeBar, videoButtonLayout);
        mediaLayout.setAlignment(Pos.CENTER);
        btnMute.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (volumeBar.getValue()!=0) {
                    volumeBeforeMute[0] = volumeBar.getValue();
                    volumeBar.setValue(0);
                    btnMute.setText("Unmute");
                } else {
                    btnMute.setText("Mute");
                    volumeBar.setValue(volumeBeforeMute[0]);
                }
            }
        });

        btnPlayPause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                    player.pause();
                    btnPlayPause.setText("Play");
                } else {
                    player.play();
                    btnPlayPause.setText("Pause");
                }
            }
        });

        returnToMenuButton3.setOnAction(e -> {
            if (Main.returnToMenu()) {
                player.stop();
            }
        });

        player.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue,
                                Duration newValue) {
                String time = "";
                time += String.format("%02d", (int) newValue.toMinutes());
                time += ":";
                time += String.format("%02d", (int) newValue.toSeconds());
                timeLabel.setText(time);
                timeBar.setValue(newValue.toMinutes());
            }
        });

    }

    public VBox getCreationPlayerLayout() {
        return mediaLayout;
    }
}
