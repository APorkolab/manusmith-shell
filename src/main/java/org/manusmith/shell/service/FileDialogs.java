package org.manusmith.shell.service;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;

public class FileDialogs {

    private static final FileChooser.ExtensionFilter DOCX_FILTER =
            new FileChooser.ExtensionFilter("Word Document (*.docx)", "*.docx");

    public Optional<File> showOpenDocxDialog(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Manuscript");
        fileChooser.getExtensionFilters().add(DOCX_FILTER);
        File file = fileChooser.showOpenDialog(owner);
        return Optional.ofNullable(file);
    }

    public Optional<File> showSaveDocxDialog(Window owner, String initialFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        fileChooser.getExtensionFilters().add(DOCX_FILTER);
        fileChooser.setInitialFileName(initialFileName);
        File file = fileChooser.showSaveDialog(owner);
        return Optional.ofNullable(file);
    }

    public Optional<File> showOpenTextDialog(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Text File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.md"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = fileChooser.showOpenDialog(owner);
        return Optional.ofNullable(file);
    }

    public Optional<File> showSaveTextDialog(Window owner, String initialFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Cover Letter");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt"));
        fileChooser.setInitialFileName(initialFileName);
        File file = fileChooser.showSaveDialog(owner);
        return Optional.ofNullable(file);
    }
}
