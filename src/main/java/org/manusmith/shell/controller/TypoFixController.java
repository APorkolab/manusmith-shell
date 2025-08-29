package org.manusmith.shell.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.manusmith.shell.service.DocxReaderService;
import org.manusmith.shell.service.EngineBridge;
import org.manusmith.shell.service.FileDialogs;
import org.manusmith.shell.service.StatusService;
import org.manusmith.shell.util.Fx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class TypoFixController {

    @FXML private TextField tfFile;
    @FXML private ChoiceBox<String> cbProfile;
    @FXML private TextArea taOriginal;
    @FXML private TextArea taPreview;

    private FileDialogs fileDialogs;
    private EngineBridge engineBridge;
    private DocxReaderService docxReaderService;
    private File currentFile;

    @FXML
    public void initialize() {
        this.fileDialogs = new FileDialogs();
        this.engineBridge = new EngineBridge();
        this.docxReaderService = new DocxReaderService();

        cbProfile.setItems(FXCollections.observableArrayList("HU", "EN", "DE"));
        cbProfile.setValue("HU");

        taOriginal.textProperty().addListener((obs, old, aNew) -> updatePreview());
        cbProfile.valueProperty().addListener((obs, old, aNew) -> updatePreview());
    }

    @FXML
    private void onBrowse() {
        StatusService.getInstance().updateStatus("Opening file browser for TypoFix...");
        Optional<File> file = fileDialogs.showOpenTextDialog(tfFile.getScene().getWindow());
        file.ifPresent(this::loadFile);
    }

    private void loadFile(File file) {
        try {
            StatusService.getInstance().updateStatus("Loading file: " + file.getName());
            String content;
            if (file.getName().toLowerCase().endsWith(".docx")) {
                content = docxReaderService.readText(file);
            } else {
                content = Files.readString(file.toPath());
            }
            taOriginal.setText(content);
            tfFile.setText(file.getAbsolutePath());
            this.currentFile = file;
            StatusService.getInstance().updateStatus("File loaded: " + file.getName());
        } catch (IOException e) {
            StatusService.getInstance().updateStatus("Error loading file: " + e.getMessage());
            Fx.error("File Read Error", "Failed to read file: " + e.getMessage());
        }
    }

    private void updatePreview() {
        String originalText = taOriginal.getText();
        if (originalText == null || originalText.isEmpty()) {
            taPreview.clear();
            return;
        }
        String fixedText = engineBridge.cleanText(originalText);
        taPreview.setText(fixedText);
    }

    @FXML
    private void onApply() {
        if (currentFile == null) {
            Fx.error("Error", "No file loaded to save.");
            return;
        }
        String previewText = taPreview.getText();
        if (previewText == null || previewText.isEmpty()) {
            Fx.alert("Info", "There is no fixed text to save.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Fixed Text");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt"));
        fileChooser.setInitialFileName(currentFile.getName().replaceFirst("[.][^.]+$", "") + "_fixed.txt");

        File outputFile = fileChooser.showSaveDialog(tfFile.getScene().getWindow());
        if (outputFile != null) {
            StatusService.getInstance().updateStatus("Saving file: " + outputFile.getName());
            try {
                Files.writeString(outputFile.toPath(), previewText);
                StatusService.getInstance().updateStatus("File saved successfully.");
                Fx.alert("Success", "File saved to " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                StatusService.getInstance().updateStatus("Error saving file: " + e.getMessage());
                Fx.error("Save Error", "Failed to save file: " + e.getMessage());
            }
        }
    }
}
