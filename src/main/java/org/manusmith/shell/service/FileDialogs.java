package org.manusmith.shell.service;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;

public class FileDialogs {

    private static final FileChooser.ExtensionFilter DOCX_FILTER =
            new FileChooser.ExtensionFilter("Word Document (*.docx)", "*.docx");
    
    private static final FileChooser.ExtensionFilter ALL_SUPPORTED_FILTER =
            new FileChooser.ExtensionFilter("All Supported Documents (*.docx, *.odt, *.md, *.txt)", "*.docx", "*.odt", "*.md", "*.txt");
    
    private static final FileChooser.ExtensionFilter ODT_FILTER =
            new FileChooser.ExtensionFilter("OpenDocument Text (*.odt)", "*.odt");
    
    private static final FileChooser.ExtensionFilter MARKDOWN_FILTER =
            new FileChooser.ExtensionFilter("Markdown (*.md)", "*.md");
    
    private static final FileChooser.ExtensionFilter TEXT_FILTER =
            new FileChooser.ExtensionFilter("Text File (*.txt)", "*.txt");
    
    private static final FileChooser.ExtensionFilter ALL_FILES_FILTER =
            new FileChooser.ExtensionFilter("All Files (*.*)", "*.*");

    public Optional<File> showOpenDocxDialog(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Manuscript");
        fileChooser.getExtensionFilters().addAll(
                ALL_SUPPORTED_FILTER,
                DOCX_FILTER,
                ODT_FILTER,
                MARKDOWN_FILTER,
                TEXT_FILTER,
                ALL_FILES_FILTER
        );
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
        fileChooser.setTitle("Open Document");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Readable Documents", "*.txt", "*.md", "*.docx"),
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
