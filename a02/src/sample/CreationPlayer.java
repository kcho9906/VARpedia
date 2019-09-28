package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;

public class CreationPlayer {

    private VBox mediaLayout = new VBox(10);

    private Button returnToMenuButton3 = new Button("Return to menu");
    private HBox videoButtonLayout = new HBox();
    private HBox timeLayout = new HBox(10);
    private Button btnMute = new Button("Mute");
    private Button btnPlayPause = new Button("Pause");
    private Label timeLabel = new Label();
    private Slider volumeBar = new Slider(0, 100, 50);
    private Slider timeBar;
    final double[] volumeBeforeMute = {0};
    Duration duration;

    Media video;
    MediaPlayer player;
    MediaView mediaView;

    public CreationPlayer(String creationName) {
        createMediaPlayer(creationName); //create new components for new video player
        setUpProperties();
        setupButtons();
        setupLayout();
    }

    private void setupLayout() {
        videoButtonLayout.setStyle("-fx-background-color: ECD8D9;");
        videoButtonLayout.getChildren().addAll(btnPlayPause, returnToMenuButton3, btnMute, volumeBar);
        videoButtonLayout.setPadding(new Insets(10, 10, 10, 10));
        videoButtonLayout.setSpacing(10);
        videoButtonLayout.setAlignment(Pos.BASELINE_CENTER);

        timeLayout.getChildren().addAll(timeLabel, timeBar);
        timeLayout.setPadding(new Insets(0, 10, 0, 10));
        timeBar.prefWidthProperty().bind(mediaLayout.widthProperty());
        timeLabel.setMinWidth(100);

        mediaView.fitWidthProperty().bind(mediaLayout.widthProperty());
        mediaView.fitHeightProperty().bind(mediaLayout.heightProperty());
        mediaLayout.getChildren().addAll(mediaView, timeLayout, videoButtonLayout);
        mediaLayout.setAlignment(Pos.CENTER);

    }

    private void setUpProperties() {
        volumeBar.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (oldValue != newValue) {
                    player.setVolume(volumeBar.getValue()/100);
                }
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
                timeLabel.setText(time + "/" + String.format("%02d", (int) duration.toMinutes()) + ":" + String.format("%02d", (int) duration.toSeconds()));
                timeBar.setValue(newValue.toMinutes());
            }
        });
}

    private void createMediaPlayer(String creationName) {
        File fileUrl = new File("src/creations/" + creationName + "/" + creationName + ".mp4");
        video = new Media(fileUrl.toURI().toString());
        player = new MediaPlayer(video);
        mediaView = new MediaView(player);
        player.setAutoPlay(true);
        String command = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 " + fileUrl;
        String getDuration = Terminal.command(command);
        double milliseconds = Double.parseDouble(getDuration) * 1000;
//        String[] tokens = duration.split(":");
//        int hours = (int) Double.parseDouble(tokens[0]);
//        int mins = (int) Double.parseDouble(tokens[1]);
//        int secs = (int) Double.parseDouble((tokens[2].substring(0, 2)));
//        int millisecs = (int) Double.parseDouble(tokens[2].substring(3));

        duration = new Duration(milliseconds);

        timeBar = new Slider(0, duration.toMinutes(), 0);
//        System.out.println(hours);
//        System.out.println(mins);
//        System.out.println(secs);
//        System.out.println(millisecs);
    }

    private void setupButtons() {
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
    }

    public VBox getCreationPlayerLayout() {
        return mediaLayout;
    }


}
