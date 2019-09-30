package application;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
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

/**
 * This class is responsible for the layout and buttons actions
 * of the media player. The player has the following options
 *   Mute            - Removes all volume from the creation
 *   Change Volume   - Slider to alter the volume
 *   Play/Pause      - Allows us to play and pause the video
 *   Return to list  - Returns to the creation list
 *   Return to menu  - Returns to the main menu
 */
public class CreationPlayer {

    private Button returnToMenuButton3 = new Button("Return to menu");
    private Button btnMute = new Button("Mute");
    private Button btnPlayPause = new Button("Pause");
    private Button returnToViewCreations = new Button("Return to list");
    private double[] volumeBeforeMute = {0};
    private Duration duration;
    private static File fileUrl;
    private HBox videoButtonLayout = new HBox();
    private HBox timeLayout = new HBox(10);
    private Label timeLabel = new Label();
    private Slider volumeBar = new Slider(0, 100, 50);
    private Slider timeBar = new Slider();
    private String _creationName;
    private Media video;
    private static MediaPlayer player;
    private MediaView mediaView;
    private VBox mediaLayout = new VBox(10);

    public CreationPlayer(File creation) {

        fileUrl = creation;
        createMediaPlayer(); //create new components for new video player
        setUpProperties();
        setupButtons();
    }

    private void setupLayout() {

        videoButtonLayout.setStyle("-fx-background-color: ECD8D9;");
        videoButtonLayout.getChildren().addAll(btnMute, volumeBar, btnPlayPause, returnToViewCreations, returnToMenuButton3);
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
        mediaLayout.setAlignment(Pos.BOTTOM_CENTER);
        mediaLayout.setPadding(new Insets(0, 0, 50, 0));

        btnPlayPause.setMinWidth(70);
        btnMute.setMinWidth(btnPlayPause.getMinWidth());

        returnToViewCreations.setMinWidth(100);
    }

    /**
     * Sets up the properties of the javafx components in the media player
     */
    private void setUpProperties() {

        volumeBar.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                if (oldValue != newValue) {

                    player.setVolume(volumeBar.getValue()/100);
                }
            }
        });

        timeBar.valueProperty().addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable observable) {

                if (timeBar.isPressed()) {

                    player.seek(player.getMedia().getDuration().multiply(timeBar.getValue() / 100.00));
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
                String totalMins = String.format("%02d", (int) duration.toMinutes());
                String totalSecs = String.format("%02d", (int) (duration.toSeconds() - Integer.parseInt(totalMins)*60));
                timeLabel.setText(time + "/" + totalMins + ":" + totalSecs);
                timeBar.setValue(player.getCurrentTime().toMinutes()/player.getTotalDuration().toMinutes()*100.00);
            }
        });

        player.setOnReady(new Runnable() {
            @Override
            public void run() {
                mediaView = new MediaView(player);
                setupLayout();
            }
        });

        player.setOnEndOfMedia(new Runnable() {

            @Override
            public void run() {

                player.stop();
                Menu.returnToViewCreations();
            }
        });
    }

    /**
     * This creates the media player with name of the creation being played
     */
    private void createMediaPlayer() {

        video = new Media(fileUrl.toURI().toString());
        player = new MediaPlayer(video);
        player.setAutoPlay(true);

        String command = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 " + fileUrl;
        String getDuration = Terminal.command(command);

        double milliseconds = Double.parseDouble(getDuration) * 1000;
        duration = new Duration(milliseconds);
        timeBar.setValue(0);
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

                    btnPlayPause.setText("Pause");
                    player.play();
                }
            }
        });

        returnToMenuButton3.setOnAction(e -> {

            player.stop();
            Main.returnToMenu();

        });

        returnToViewCreations.setOnAction(event -> {

                player.stop();
                Menu.returnToViewCreations();

        });
    }

    public VBox getCreationPlayerLayout() {

        return mediaLayout;
    }

    public static void stopPlayer() {

        fileUrl.delete();
        player.dispose();
    }

}
