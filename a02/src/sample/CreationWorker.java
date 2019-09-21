package sample;

import javafx.concurrent.Task;

import java.io.File;

public class CreationWorker extends Task<String> {
    private String _input;

    public CreationWorker(String input) {

        _input = input;
    }

    @Override
    protected String call() throws Exception {

        if (!_input.isEmpty() && _input.matches("[a-zA-Z0-9_ -]+")) {
            File creationDir = new File(_input);
            if (creationDir.exists()) {
                Boolean overwrite = ConfirmBox.display("ERROR", _input + "exists. \nRename or overwrite?", "Overwrite", "Rename");
                if (overwrite) {
                    String command = "rm " + creationDir.getName() + "/*";
                    Terminal.command(command);
                    creationDir.mkdir();
                    return "Success!";
                }
                return "Overwriting cancelled. Creation not made";
            } else {
                creationDir.mkdir();
            }
            return "Success!";
        } else {
            return "Invalid creation name. Please try again";
        }
    }
}
