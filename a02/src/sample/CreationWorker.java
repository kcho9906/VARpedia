package sample;

import javafx.concurrent.Task;

import java.io.File;

public class CreationWorker extends Task<Boolean> {
    private final int _numImages;
    private String _input;
    private String command, result;
    private File _creationDir;

    public CreationWorker(String input, File creationDir, int numImages) {

        _input = input;
        _creationDir = creationDir;
        _numImages = numImages;
    }

    @Override
    protected Boolean call() throws Exception {
        System.out.println(_creationDir.getPath());
        boolean create = false;
        switch (_input) {
            case "overwrite":
                command = "rm " + _creationDir.getPath() + "/*";

                break;
            case "create":
                command = "mkdir " + _creationDir.getPath();
                break;
        }
        Terminal.command(command);
        System.out.println(_creationDir.getName());
        FlickrImageExtractor.downloadImages(_creationDir.getName(), _numImages);
        return true;
    }
}
