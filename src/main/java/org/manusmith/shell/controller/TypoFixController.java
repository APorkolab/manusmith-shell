package org.manusmith.shell.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.manusmith.shell.service.EngineBridge;
import org.manusmith.shell.service.FileDialogs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javafx.stage.FileChooser;

public class TypoFixController {

    @FXML private TextField tfFile;
    @FXML private ChoiceBox<String> cbProfile;
    @FXML private TextArea taOriginal;
    @FXML private TextArea taPreview;

    private FileDialogs fileDialogs;
    private EngineBridge engineBridge;
    private File currentFile;

    @FXML
    public void initialize() {
        this.fileDialogs = new FileDialogs();
        this.engineBridge = new EngineBridge();

        cbProfile.setItems(FXCollections.observableArrayList("HU", "EN", "DE"));
        cbProfile.setValue("HU");

        // Add listeners to auto-update the preview
        taOriginal.textProperty().addListener((obs, old, aNew) -> updatePreview());
        cbProfile.valueProperty().addListener((obs, old, aNew) -> updatePreview());
    }

    @FXML
    private void onBrowse() {
        Optional<File> file = fileDialogs.showOpenTextDialog(tfFile.getScene().getWindow());
        file.ifPresent(this::loadFile);
    }

    private void loadFile(File file) {
        try {
            String content = Files.readString(file.toPath());
            taOriginal.setText(content);
            tfFile.setText(file.getAbsolutePath());
            this.currentFile = file;
        } catch (IOException e) {
            System.err.println("Failed to read file: " + e.getMessage());
            // In a real app, show an alert to the user.
        }
    }

    private void updatePreview() {
        String originalText = taOriginal.getText();
        if (originalText == null || originalText.isEmpty()) {
            taPreview.clear();
            return;
        }
        // In a real app, we'd pass the profile to the engine.
        // String profile = cbProfile.getValue();
        String fixedText = engineBridge.cleanText(originalText);
        taPreview.setText(fixedText);
    }

    @FXML
    private void onApply() {
        if (currentFile == null) {
            // In a real app, show an alert.
            System.out.println("No file loaded to save.");
            return;
        }
        String previewText = taPreview.getText();
        if (previewText == null || previewText.isEmpty()) {
            System.out.println("Nothing to save.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Fixed Text");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt"));
        fileChooser.setInitialFileName(currentFile.getName().replaceFirst("[.][^.]+$", "") + "_fixed.txt");

        File outputFile = fileChooser.showSaveDialog(tfFile.getScene().getWindow());
        if (outputFile != null) {
            try {
                Files.writeString(outputFile.toPath(), previewText);
                System.out.println("File saved successfully to " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Failed to save file: " + e.getMessage());
            }
        }
    }
}
