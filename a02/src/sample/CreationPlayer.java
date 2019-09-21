package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;

public class CreationPlayer {

    private BorderPane mediaLayout = new BorderPane();
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
        Button btnPlayPause = new Button("Pause/Play");
        Label timeLabel = new Label();

        videoButtonLayout.setStyle("-fx-background-color: ECD8D9;");
        videoButtonLayout.getChildren().addAll(btnMute, timeLabel, btnPlayPause, returnToMenuButton3);
        videoButtonLayout.setPadding(new Insets(10, 10, 10, 10));
        videoButtonLayout.setSpacing(10);

        videoButtonLayout.setAlignment(Pos.BASELINE_CENTER);
        mediaView.fitWidthProperty().bind(mediaLayout.widthProperty());
        mediaView.fitHeightProperty().bind(mediaLayout.heightProperty());
        mediaLayout.setCenter(mediaView);
        mediaLayout.setBottom(videoButtonLayout);

        btnMute.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                player.setMute(!player.isMute());
            }
        });

        btnPlayPause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                    player.pause();
                } else {
                    player.play();
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
            }
        });

    }

    public BorderPane getCreationPlayerLayout() {
        return mediaLayout;
    }
}
