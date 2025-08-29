package org.manusmith.shell.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.manusmith.shell.service.DocxReaderService;
import org.manusmith.shell.service.EngineBridge;
import org.manusmith.shell.service.FileDialogs;
import org.manusmith.shell.service.PreferencesService;
import org.manusmith.shell.service.StatusService;
import org.manusmith.shell.util.Fx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class TypoFixController {

    @FXML private VBox contentBox;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private TextField tfFile;
    @FXML private ChoiceBox<String> cbProfile;
    @FXML private TextArea taOriginal;
    @FXML private TextArea taPreview;

    private FileDialogs fileDialogs;
    private EngineBridge engineBridge;
    private DocxReaderService docxReaderService;
    private PreferencesService preferencesService;
    private File currentFile;

    @FXML
    public void initialize() {
        this.fileDialogs = new FileDialogs();
        this.engineBridge = new EngineBridge();
        this.docxReaderService = new DocxReaderService();
        this.preferencesService = new PreferencesService();

        cbProfile.setItems(FXCollections.observableArrayList("HU", "EN", "DE", "Shunn"));
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
        // This could also be a task if files are very large
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

        String profile;
        if (preferencesService.getAlwaysNormalize()) {
            profile = "Shunn";
            // Optionally, disable the choice box to make it clear why it's not being used
            cbProfile.setDisable(true);
            cbProfile.setValue(profile); // Visually reflect the profile being used
        } else {
            cbProfile.setDisable(false);
            profile = cbProfile.getValue();
        }

        String fixedText = engineBridge.cleanText(originalText, profile);
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
        if (outputFile == null) {
            return; // User cancelled
        }

        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                StatusService.getInstance().updateStatus("Saving file: " + outputFile.getName());
                Files.writeString(outputFile.toPath(), previewText);
                return null;
            }
        };

        saveTask.setOnSucceeded(e -> {
            StatusService.getInstance().updateStatus("File saved successfully.");
            Fx.alert("Success", "File saved to " + outputFile.getAbsolutePath());
        });

        saveTask.setOnFailed(e -> {
            Throwable ex = saveTask.getException();
            StatusService.getInstance().updateStatus("Error saving file: " + ex.getMessage());
            Fx.error("Save Error", "Failed to save file: " + ex.getMessage());
        });

        progressIndicator.visibleProperty().bind(saveTask.runningProperty());
        contentBox.disableProperty().bind(saveTask.runningProperty());

        new Thread(saveTask).start();
    }
}
