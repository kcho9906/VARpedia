package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;


public class Creation {
    private String _creationName;
    private FileTime _timeCreated;

    public Creation(File directory) {
        _creationName = directory.getName();
        _timeCreated = getCreationDate(directory);
    }

    public String get_creationName() {
        return _creationName;
    }

    public void set_creationName(String _creationName) {
        this._creationName = _creationName;
    }

    public FileTime get_timeCreated() {
        return _timeCreated;
    }

    public void set_timeCreated(FileTime _timeCreated) {
        this._timeCreated = _timeCreated;
    }

    private FileTime getCreationDate(File directory) {
        Path p = Paths.get(directory.getAbsolutePath());
        BasicFileAttributes view = null;
        try {
            view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return view.creationTime();

    }

    @Override
    public String toString() {
        return _creationName;
    }
}

