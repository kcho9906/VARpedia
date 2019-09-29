package application;

import javafx.concurrent.Task;
import java.io.File;

public class CreationWorker extends Task<String> {

    private Audio _audio;
    private double duration;
    private File _creationDir;
    private int _numImages, imagesFound;
    private String _input;
    private String command;
    private String _path;


    public CreationWorker(String input, File creationDir, int numImages, Audio audio) {

        _input = input;
        _creationDir = creationDir;
        _numImages = numImages;
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
            imagesFound = FlickrImageExtractor.downloadImages(_creationDir, _numImages);
            String message;
            if (imagesFound < 0) {
                message = "Error";
            } else if (imagesFound == 0) {
                message = "No images found";
                _creationDir.delete();
            } else { // create here?
                message = "Success!";
                duration = _audio.mergeAudio(_creationDir); // merges audio
                if (duration != -1) {
                    //create the video
                    String creationName = _creationDir.getName();
                    String path = _creationDir.getPath();
                    createVideo(creationName, path);

                } else {
                    String command = "rm -r -f " + _creationDir.getPath();
                    Terminal.command(command);
                    System.out.println(command);
                    return "No audio files selected for creation";
                }
            }
            return message;
        } else {
            return "Could not make directory for creation";
        }
    }

    private void createVideo(String creationName, String path) {
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
}
