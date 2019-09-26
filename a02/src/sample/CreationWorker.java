package sample;

import javafx.concurrent.Task;

import java.io.File;

public class CreationWorker extends Task<String> {
    private final int _numImages;
    private String _input;
    private String command, result;
    private File _creationDir;
    private String _name, _path;

    public CreationWorker(String input, File creationDir, int numImages) {

        _input = input;
        _creationDir = creationDir;
        _numImages = numImages;
        _name = _creationDir.getName();
        _path = _creationDir.getPath();
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
            } else {
                Audio.
                message = "Success";
            }
            return message;
        }

        return "";
    }
}
