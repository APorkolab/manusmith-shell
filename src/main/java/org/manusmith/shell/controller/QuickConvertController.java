package org.manusmith.shell.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import org.manusmith.shell.service.EngineBridge;
import org.manusmith.shell.service.StatusService;

import java.io.File;
import java.util.List;

public class QuickConvertController {

    @FXML private StackPane dropZone;
    @FXML private Label dropLabel;
    @FXML private ProgressIndicator progressIndicator;

    private EngineBridge engineBridge;
    private static final String IDLE_STYLE = "-fx-border-color: #a0a0a0; -fx-border-style: dashed; -fx-background-color: #f8f8f8;";
    private static final String HOVER_STYLE = "-fx-border-color: #009688; -fx-border-style: dashed; -fx-background-color: #e0f2f1;";

    @FXML
    public void initialize() {
        this.engineBridge = new EngineBridge();
        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        dropZone.setOnDragOver(this::handleDragOver);
        dropZone.setOnDragDropped(this::handleDragDropped);
        dropZone.setOnDragEntered(event -> dropZone.setStyle(HOVER_STYLE));
        dropZone.setOnDragExited(event -> dropZone.setStyle(IDLE_STYLE));
    }

    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            if (files != null && !files.isEmpty()) {
                processFiles(files);
                success = true;
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void processFiles(List<File> files) {
        Task<Integer> conversionTask = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                int processedCount = 0;
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    updateMessage("Processing " + file.getName() + " (" + (i + 1) + "/" + files.size() + ")");

                    File outDir = new File(file.getParentFile(), "out");
                    if (!outDir.exists()) {
                        if (!outDir.mkdirs()) {
                            throw new IOException("Failed to create output directory: " + outDir.getAbsolutePath());
                        }
                    }
                    String inputName = file.getName().toLowerCase(java.util.Locale.ROOT);
                    String baseName = inputName.replaceFirst("[.][^.]+$", "");
                    String outputFileName;

                    if (inputName.endsWith(".txt")) {
                        outputFileName = baseName + "_converted.docx";
                    } else if (inputName.endsWith(".docx")) {
                        outputFileName = baseName + "_converted.txt";
                    } else if (inputName.endsWith(".md")) {
                        outputFileName = baseName + "_converted.txt";
                    } else if (inputName.endsWith(".odt")) {
                        outputFileName = baseName + "_converted.txt";
                    } else {
                        System.err.println("Skipping unsupported file type: " + file.getName());
                        continue; // Skip this file
                    }
                    File outputFile = new File(outDir, outputFileName);

                    engineBridge.quickConvert(file, outputFile);
                    processedCount++;
                }
                return processedCount;
            }
        };

        conversionTask.setOnSucceeded(e -> {
            int count = conversionTask.getValue();
            StatusService.getInstance().updateStatus("Quick Convert finished. " + count + " file(s) processed.");
        });

        conversionTask.setOnFailed(e -> {
            Throwable ex = conversionTask.getException();
            StatusService.getInstance().updateStatus("Quick Convert failed: " + ex.getMessage());
            System.err.println("Error during Quick Convert: " + ex.getMessage());
            ex.printStackTrace();
        });

        StatusService.getInstance().statusProperty().bind(conversionTask.messageProperty());
        progressIndicator.visibleProperty().bind(conversionTask.runningProperty());
        dropLabel.visibleProperty().bind(conversionTask.runningProperty().not());
        dropZone.disableProperty().bind(conversionTask.runningProperty());

        new Thread(conversionTask).start();
    }
}
