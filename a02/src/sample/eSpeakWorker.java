package sample;

import javafx.concurrent.Task;

public class eSpeakWorker extends Task<Void> {
    private String _selectedText;

    public eSpeakWorker(String selectedText) {
        this._selectedText = selectedText;
    }

    @Override
    protected Void call() throws Exception {

        // run the selected text through espeak
        String command = "espeak \"" + _selectedText + "\"";
        Terminal.command(command);

        return null;
    }
}
