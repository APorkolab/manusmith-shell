package org.manusmith.shell.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.springframework.context.MessageSource; // Using simple message handling

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.util.Locale;
import java.util.Optional;

/**
 * Enhanced error handling utility for user-friendly error dialogs and recovery suggestions.
 */
public class ErrorHandlerUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlerUtil.class);
    private static Object messageSource; // Placeholder for message source
    
    public static void setMessageSource(Object source) {
        messageSource = source;
    }
    
    /**
     * Handle and display error with appropriate user message and recovery suggestions
     */
    public static void handleError(Exception exception, String context, Stage ownerStage) {
        logger.error("Error in context '{}': {}", context, exception.getMessage(), exception);
        
        Platform.runLater(() -> {
            ErrorInfo errorInfo = categorizeError(exception);
            showErrorDialog(errorInfo, context, ownerStage, exception);
        });
    }
    
    /**
     * Handle error with custom user message
     */
    public static void handleError(Exception exception, String userMessage, String context, Stage ownerStage) {
        logger.error("Error in context '{}': {}", context, exception.getMessage(), exception);
        
        Platform.runLater(() -> {
            ErrorInfo errorInfo = new ErrorInfo(
                "Error", 
                userMessage, 
                getSuggestion(exception),
                Alert.AlertType.ERROR
            );
            showErrorDialog(errorInfo, context, ownerStage, exception);
        });
    }
    
    /**
     * Show warning dialog
     */
    public static void showWarning(String message, String context, Stage ownerStage) {
        logger.warn("Warning in context '{}': {}", context, message);
        
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(ownerStage);
            alert.setTitle(getMessage("error.warning.title", "Warning"));
            alert.setHeaderText(context);
            alert.setContentText(message);
            alert.getDialogPane().getStylesheets().add(
                ErrorHandlerUtil.class.getResource("/styles/material-light.css").toExternalForm()
            );
            alert.showAndWait();
        });
    }
    
    /**
     * Show confirmation dialog for potentially risky operations
     */
    public static boolean showConfirmation(String message, String title, Stage ownerStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(ownerStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
            ErrorHandlerUtil.class.getResource("/styles/material-light.css").toExternalForm()
        );
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Categorize error and provide appropriate user-friendly information
     */
    private static ErrorInfo categorizeError(Exception exception) {
        if (exception instanceof NoSuchFileException) {
            return new ErrorInfo(
                getMessage("error.file.notfound.title", "File Not Found"),
                getMessage("error.file.notfound.message", "The selected file could not be found."),
                getMessage("error.file.notfound.suggestion", "Please check if the file exists and try again."),
                Alert.AlertType.WARNING
            );
        } else if (exception instanceof AccessDeniedException) {
            return new ErrorInfo(
                getMessage("error.file.access.title", "Access Denied"),
                getMessage("error.file.access.message", "You don't have permission to access this file."),
                getMessage("error.file.access.suggestion", "Please check file permissions or try running as administrator."),
                Alert.AlertType.ERROR
            );
        } else if (exception instanceof IOException) {
            return new ErrorInfo(
                getMessage("error.io.title", "File Operation Error"),
                getMessage("error.io.message", "An error occurred while processing the file."),
                getMessage("error.io.suggestion", "Please ensure the file is not corrupted and try again."),
                Alert.AlertType.ERROR
            );
        } else if (exception.getMessage() != null && exception.getMessage().toLowerCase().contains("memory")) {
            return new ErrorInfo(
                getMessage("error.memory.title", "Insufficient Memory"),
                getMessage("error.memory.message", "Not enough memory to process this file."),
                getMessage("error.memory.suggestion", "Please close other applications or try with a smaller file."),
                Alert.AlertType.ERROR
            );
        } else if (exception.getMessage() != null && exception.getMessage().toLowerCase().contains("corrupted")) {
            return new ErrorInfo(
                getMessage("error.file.corrupted.title", "File Corrupted"),
                getMessage("error.file.corrupted.message", "The file appears to be corrupted or in an unsupported format."),
                getMessage("error.file.corrupted.suggestion", "Please try with a different file or check the file integrity."),
                Alert.AlertType.WARNING
            );
        } else if (exception.getMessage() != null && exception.getMessage().toLowerCase().contains("network")) {
            return new ErrorInfo(
                getMessage("error.network.title", "Network Error"),
                getMessage("error.network.message", "A network-related error occurred."),
                getMessage("error.network.suggestion", "Please check your internet connection and try again."),
                Alert.AlertType.WARNING
            );
        } else {
            return new ErrorInfo(
                getMessage("error.general.title", "Unexpected Error"),
                getMessage("error.general.message", "An unexpected error occurred."),
                getMessage("error.general.suggestion", "Please try again or contact support if the problem persists."),
                Alert.AlertType.ERROR
            );
        }
    }
    
    /**
     * Get recovery suggestion for specific exception types
     */
    private static String getSuggestion(Exception exception) {
        if (exception instanceof NoSuchFileException) {
            return "Check if the file exists and is accessible.";
        } else if (exception instanceof AccessDeniedException) {
            return "Verify file permissions or run as administrator.";
        } else if (exception instanceof IOException) {
            return "Ensure the file is not corrupted or in use by another application.";
        } else if (exception.getMessage() != null && exception.getMessage().toLowerCase().contains("memory")) {
            return "Close other applications or increase available memory.";
        } else {
            return "Please try again or restart the application.";
        }
    }
    
    /**
     * Show detailed error dialog with expandable details
     */
    private static void showErrorDialog(ErrorInfo errorInfo, String context, Stage ownerStage, Exception exception) {
        Alert alert = new Alert(errorInfo.alertType);
        alert.initOwner(ownerStage);
        alert.setTitle(errorInfo.title);
        alert.setHeaderText(context);
        alert.setContentText(errorInfo.message + "\n\n" + errorInfo.suggestion);
        
        // Add expandable details
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String exceptionText = sw.toString();
        
        Label label = new Label("Technical details:");
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        
        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().getStylesheets().add(
            ErrorHandlerUtil.class.getResource("/styles/material-light.css").toExternalForm()
        );
        
        alert.showAndWait();
    }
    
    /**
     * Get localized message with fallback
     */
    private static String getMessage(String key, String defaultValue) {
        // Simple fallback to default value for now
        // In a full implementation, this would use ResourceBundle or similar
        return defaultValue;
    }
    
    /**
     * Error information holder
     */
    private static class ErrorInfo {
        final String title;
        final String message;
        final String suggestion;
        final Alert.AlertType alertType;
        
        ErrorInfo(String title, String message, String suggestion, Alert.AlertType alertType) {
            this.title = title;
            this.message = message;
            this.suggestion = suggestion;
            this.alertType = alertType;
        }
    }
}
