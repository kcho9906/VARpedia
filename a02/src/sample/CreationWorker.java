package sample;

import javafx.concurrent.Task;

import java.io.File;

public class CreationWorker extends Task<Boolean> {
    private String _input;
    private String command, result;
    private File _creationDir;

    public CreationWorker(String input, File creationDir) {

        _input = input;
        _creationDir = creationDir;
    }

    @Override
    protected Boolean call() throws Exception {
        System.out.println(_creationDir.getPath());
        switch (_input) {
            case "overwrite":
                command = "rm " + _creationDir.getPath() + "/*";
                break;
            case "create":
                command = "mkdir " + _creationDir.getPath();
                break;
        }
        String success = Terminal.command(command);
        if (success.equals("No output") || success.equals("Error")) {
            return false;
        } else {
            return true;
        }
    }
}
