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
        double duration = _audio.mergeAudio(_creationDir); // merges audio

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
        if (create && duration != -1) {
            Terminal.command(command);
            int imagesFound = FlickrImageExtractor.downloadImages(_creationDir, _numImages);
            String message;
            if (imagesFound < 0) {
                message = "Error";
            } else if (imagesFound == 0) {
                message = "No images found";
            } else { // create here?
                    message = "Success!";

                    // set the creation name with creationName_duration
                    String creationName = _creationDir.getName();
                    String path = _creationDir.getPath();
                    // resize all the images to the same size 1920x1080
//                String command = "mogrify -resize 1920x1080 ./src/creations/" + creationName + "/*.jpg"; // may need to specify this is used in brief
//                Terminal.command(command);

                    // merge the images
                    command = "cat " + path + "/*.jpg | ffmpeg -f image2pipe -framerate $((" + imagesFound + "))/" + duration + " -i - -vcodec libx264 -pix_fmt yuv420p -vf \"scale=w=1920:h=1080:force_original_aspect_ratio=1,pad=1920:1080:(ow-iw)/2:(oh-ih)/2\" " + path + "/" + creationName + "Temp.mp4";
                    Terminal.command(command);

                    // add the name onto the video
                    command = "ffmpeg -i " + path + "/" + creationName + "Temp.mp4 -vf drawtext=\"fontfile=/Library/Fonts/Verdana.ttf: text='" + creationName + "': fontcolor=white: fontsize=100: box=1: boxcolor=black@0.5: boxborderw=5: x=(w-text_w)/2: y=(h-text_h)/2\" -r 25 -codec:a copy " + path + "/" + creationName + "Text.mp4";
                    Terminal.command(command);

                    // merge the video and images
                    command = "ffmpeg -i " + path + "/" + creationName + "Text.mp4 -i " + path + "/output.wav -c:v copy -c:a aac -strict experimental " + path + "/" + creationName + ".mp4";
                    Terminal.command(command);

                    //remove unnecessary files
                    command = "rm " + path + "/*.jpg; rm " + path + "/*.wav; rm " + path + "/" + creationName + "Temp.mp4; rm " + path + "/" + creationName + "Text.mp4";
                    Terminal.command(command);
                }

            return message;
        } else if (!create) {
            return "Could not make directory for creation";
        } else {
            return "No audio files selected for creation";
        }
    }
}
