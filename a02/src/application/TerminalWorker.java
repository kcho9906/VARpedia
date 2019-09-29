package application;

import javafx.concurrent.Task;

public class TerminalWorker extends Task<String> {

    private String _command;

    public TerminalWorker(String command) {

        this._command = command;
    }

    @Override
    protected String call() throws Exception {

        String output = Terminal.command(_command);
        output = " " + output;

        return output;
    }
}
