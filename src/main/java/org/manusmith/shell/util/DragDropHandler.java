package org.manusmith.shell.util;

import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Utility class for handling drag-and-drop functionality
 */
public class DragDropHandler {
    private static final Logger logger = LoggerFactory.getLogger(DragDropHandler.class);
    
    private final Consumer<List<File>> onFilesDropped;
    private final Consumer<Boolean> onDragStateChanged;
    private final String[] allowedExtensions;
    
    public DragDropHandler(Consumer<List<File>> onFilesDropped) {
        this(onFilesDropped, null);
    }
    
    public DragDropHandler(Consumer<List<File>> onFilesDropped, String... allowedExtensions) {
        this(onFilesDropped, null, allowedExtensions);
    }
    
    public DragDropHandler(Consumer<List<File>> onFilesDropped, 
                          Consumer<Boolean> onDragStateChanged, 
                          String... allowedExtensions) {
        this.onFilesDropped = onFilesDropped;
        this.onDragStateChanged = onDragStateChanged;
        this.allowedExtensions = allowedExtensions;
    }
    
    /**
     * Setup drag-and-drop for a node
     */
    public void setupDragDrop(Node node) {
        node.setOnDragOver(this::handleDragOver);
        node.setOnDragEntered(this::handleDragEntered);
        node.setOnDragExited(this::handleDragExited);
        node.setOnDragDropped(this::handleDragDropped);
        
        logger.debug("Drag-and-drop setup completed for node: {}", node.getClass().getSimpleName());
    }
    
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            List<File> files = event.getDragboard().getFiles();
            
            // Check if all files have allowed extensions
            if (allowedExtensions == null || areFilesAllowed(files)) {
                event.acceptTransferModes(TransferMode.COPY);
            }
        }
        event.consume();
    }
    
    private void handleDragEntered(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            List<File> files = event.getDragboard().getFiles();
            
            if (allowedExtensions == null || areFilesAllowed(files)) {
                // Add visual feedback for valid drag
                Node target = (Node) event.getTarget();
                target.getStyleClass().add("drag-over");
                
                if (onDragStateChanged != null) {
                    onDragStateChanged.accept(true);
                }
                
                logger.debug("Valid drag entered with {} files", files.size());
            }
        }
        event.consume();
    }
    
    private void handleDragExited(DragEvent event) {
        // Remove visual feedback
        Node target = (Node) event.getTarget();
        target.getStyleClass().remove("drag-over");
        
        if (onDragStateChanged != null) {
            onDragStateChanged.accept(false);
        }
        
        event.consume();
    }
    
    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            
            if (allowedExtensions == null || areFilesAllowed(files)) {
                try {
                    onFilesDropped.accept(files);
                    success = true;
                    logger.info("Successfully dropped {} files", files.size());
                } catch (Exception e) {
                    logger.error("Error handling dropped files", e);
                }
            } else {
                logger.warn("Dropped files contain unsupported extensions");
            }
        }
        
        // Remove visual feedback
        Node target = (Node) event.getTarget();
        target.getStyleClass().remove("drag-over");
        
        if (onDragStateChanged != null) {
            onDragStateChanged.accept(false);
        }
        
        event.setDropCompleted(success);
        event.consume();
    }
    
    private boolean areFilesAllowed(List<File> files) {
        if (allowedExtensions == null || allowedExtensions.length == 0) {
            return true;
        }
        
        for (File file : files) {
            if (!isFileAllowed(file)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isFileAllowed(File file) {
        if (allowedExtensions == null || allowedExtensions.length == 0) {
            return true;
        }
        
        String fileName = file.getName().toLowerCase();
        
        for (String extension : allowedExtensions) {
            if (fileName.endsWith("." + extension.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Create a drag-drop handler for supported document formats
     */
    public static DragDropHandler forDocuments(Consumer<List<File>> onFilesDropped) {
        return new DragDropHandler(onFilesDropped, (Consumer<Boolean>) null, 
                "docx", "odt", "txt", "md", "rtf", "doc");
    }
    
    /**
     * Create a drag-drop handler for any file type
     */
    public static DragDropHandler forAnyFile(Consumer<List<File>> onFilesDropped) {
        return new DragDropHandler(onFilesDropped, (Consumer<Boolean>) null);
    }
    
    /**
     * Create a drag-drop handler with visual feedback callback
     */
    public static DragDropHandler withFeedback(Consumer<List<File>> onFilesDropped,
                                              Consumer<Boolean> onDragStateChanged,
                                              String... allowedExtensions) {
        return new DragDropHandler(onFilesDropped, onDragStateChanged, allowedExtensions);
    }
}
