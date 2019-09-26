package sample;

import javafx.concurrent.Task;

public class speechWorker extends Task<Void> {
    private String _selectedText, _synthType;

    public speechWorker(String text, String synthType) {
        _selectedText = text;
        _synthType = synthType;
    }

    @Override
    protected Void call() throws Exception {
        String command = "espeak \"" + _selectedText + "\""; //default as Espeak speech synthesiser
        if (_synthType.equals("Festival")) {
            command = "echo \"" + _selectedText + "\" | festival --tts"; //change to Festival speech synthesiser
        }
        // run the selected text through specified speech synthesiser
        Terminal.command(command);
        System.out.println(command);
        return null;
    }
}
