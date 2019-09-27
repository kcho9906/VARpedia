package sample;

import javafx.concurrent.Task;

import java.io.File;

public class CreationWorker extends Task<String> {
    private final int _numImages;
    private String _input;
    private String command, result;
    private File _creationDir;
    private String _name, _path;
    private Audio _audio;

    public CreationWorker(String input, File creationDir, int numImages, Audio audio) {

        _input = input;
        _creationDir = creationDir;
        _numImages = numImages;
        _name = _creationDir.getName();
        _path = _creationDir.getPath();
        _audio = audio;
    }

    @Override
    protected String call() throws Exception {

        boolean create = false;
        switch (_input) {
            case "overwrite":
                command = "rm " + _path + "/*";
                create = true;
                break;
            case "create":
                command = "mkdir " + _path;
                create = true;
                break;
            default:
                command = "";
                create = false;
        }
        if (create) {
            Terminal.command(command);
            int imagesFound = FlickrImageExtractor.downloadImages(_creationDir, _numImages);
            String message;
            if (imagesFound < 0) {
                message = "Error";
            } else if (imagesFound == 0) {
                message = "No images found";
            } else { // create here?
                double duration = _audio.mergeAudio(_creationDir); // merges audio
                message = "Success";

                // set the creation name with creationName_duration
                String creationName = _creationDir.getName();

                // resize all the images to the same size 1920x1080
                String command = "mogrify -resize 1920x1080 ./src/creations/" + creationName + "/*.jpg"; // may need to specify this is used in brief
                Terminal.command(command);

                // merge the images
                command = "cat ./src/creations/" + creationName + "/*.jpg | ffmpeg -f image2pipe -r " + ((1.0 / duration) * imagesFound) + " -vcodec mjpeg -i - -vcodec libx264 -vf scale=1920:-2 -vf scale=-2:1080 ./src/creations/" + creationName + "/" + creationName + "Temp.mp4";
                Terminal.command(command);

                // add the name onto the video
                command = "ffmpeg -i ./src/creations/" + creationName + "/" + creationName + "Temp.mp4 -vf drawtext=\"fontfile=/Library/Fonts/Verdana.ttf: text='" + creationName + "': fontcolor=white: fontsize=100: x=(w-text_w)/2: y=(h-text_h)/2\" -codec:a copy ./src/creations/" + creationName + "/" + creationName + "Text.mp4";
                Terminal.command(command);

                // merge the video and images
                command = "ffmpeg -i ./src/creations/" + creationName + "/" + creationName + "Text.mp4 -i ./src/creations/" + creationName + "/output.wav -c:v copy -c:a aac -strict experimental ./src/creations/" + creationName + "/" + creationName + ".mp4";
                Terminal.command(command);
            }
            return message;
        }

        return "";
    }
}
