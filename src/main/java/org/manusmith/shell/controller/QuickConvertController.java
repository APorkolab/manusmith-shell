package org.manusmith.shell.controller;

import javafx.fxml.FXML;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import org.manusmith.shell.service.EngineBridge;
import org.manusmith.shell.service.StatusService;

import java.io.File;
import java.util.List;

public class QuickConvertController {

    @FXML
    private StackPane dropZone;

    private EngineBridge engineBridge;

    @FXML
    public void initialize() {
        this.engineBridge = new EngineBridge();
        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        dropZone.setOnDragOver(this::handleDragOver);
        dropZone.setOnDragDropped(this::handleDragDropped);
        dropZone.setOnDragEntered(event -> dropZone.setStyle("-fx-border-color: #009688; -fx-border-style: dashed; -fx-background-color: #e0f2f1;"));
        dropZone.setOnDragExited(event -> dropZone.setStyle("-fx-border-color: #a0a0a0; -fx-border-style: dashed; -fx-background-color: #f8f8f8;"));
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    @FXML
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
        StatusService.getInstance().updateStatus("Processing " + files.size() + " file(s)...");
        for (File file : files) {
            try {
                File outDir = new File(file.getParentFile(), "out");
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }
                String outputFileName = file.getName().replaceFirst("[.][^.]+$", "") + "_converted.docx";
                File outputFile = new File(outDir, outputFileName);

                engineBridge.quickConvert(file, outputFile);

            } catch (Exception e) {
                StatusService.getInstance().updateStatus("Failed to convert " + file.getName() + ": " + e.getMessage());
                // In a real app, show an error alert.
                System.err.println("Error converting file: " + e.getMessage());
            }
        }
        StatusService.getInstance().updateStatus("Quick Convert finished. Check the 'out' folder.");
    }
}
