package org.manusmith.shell.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import org.manusmith.shell.dto.AuthorMeta;
import org.manusmith.shell.dto.ConvertRequest;
import org.manusmith.shell.dto.FormattingPrefs;
import org.manusmith.shell.service.EngineBridge;
import org.manusmith.shell.service.FileDialogs;
import org.manusmith.shell.service.PreferencesService;
import org.manusmith.shell.service.SharedDataService;
import org.manusmith.shell.service.StatusService;
import org.manusmith.shell.service.ValidationService;
import org.manusmith.shell.util.Fx;
import org.manusmith.shell.util.Strings;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class ConvertController {

    @FXML private VBox contentBox;
    @FXML private VBox dragDropArea;
    @FXML private ProgressIndicator progressIndicator;

    @FXML private TextField tfInput;
    @FXML private TextField tfOutput;
    @FXML private TextField tfAuthor;
    @FXML private TextField tfAddress;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private TextField tfTitle;
    @FXML private TextField tfWords;
    @FXML private CheckBox cbItalicToUnderline;

    private FileDialogs fileDialogs;
    private EngineBridge engineBridge;
    private ValidationService validationService;
    private PreferencesService preferencesService;
    
    private static final String DRAG_IDLE_STYLE = "-fx-border-color: #d0d0d0; -fx-border-style: dashed; -fx-border-width: 2; -fx-background-color: #fafafa; -fx-border-radius: 8; -fx-background-radius: 8;";
    private static final String DRAG_HOVER_STYLE = "-fx-border-color: #2196F3; -fx-border-style: dashed; -fx-border-width: 2; -fx-background-color: #e3f2fd; -fx-border-radius: 8; -fx-background-radius: 8;";

    @FXML
    public void initialize() {
        this.fileDialogs = new FileDialogs();
        this.engineBridge = new EngineBridge();
        this.validationService = new ValidationService();
        this.preferencesService = new PreferencesService();
        loadPreferences();
        setupDragAndDrop();
    }

    private void loadPreferences() {
        AuthorMeta savedMeta = preferencesService.loadAuthorMeta();
        if (savedMeta != null) {
            tfAuthor.setText(savedMeta.author());
            tfAddress.setText(savedMeta.address());
            tfEmail.setText(savedMeta.email());
            tfPhone.setText(savedMeta.phone());
            StatusService.getInstance().updateStatus("Author data loaded from preferences.");
        }
    }

    @FXML
    private void onBrowseInput() {
        StatusService.getInstance().updateStatus("Opening file browser for input...");
        Optional<File> file = fileDialogs.showOpenDocxDialog(tfInput.getScene().getWindow());
        file.ifPresent(f -> {
            tfInput.setText(f.getAbsolutePath());
            StatusService.getInstance().updateStatus("Input file selected: " + f.getName());
            if (Strings.isBlank(tfOutput.getText())) {
                String outputName = f.getName().replaceFirst("[.][^.]+$", "") + " (Shunn).docx";
                tfOutput.setText(new File(f.getParent(), outputName).getAbsolutePath());
            }
        });
    }

    @FXML
    private void onBrowseOutput() {
        StatusService.getInstance().updateStatus("Opening file browser for output...");
        String initialName = "output.docx";
        if (!Strings.isBlank(tfTitle.getText()) && !Strings.isBlank(tfAuthor.getText())) {
            initialName = tfAuthor.getText() + " - " + tfTitle.getText() + ".docx";
        }
        Optional<File> file = fileDialogs.showSaveDocxDialog(tfOutput.getScene().getWindow(), initialName);
        file.ifPresent(f -> {
            tfOutput.setText(f.getAbsolutePath());
            StatusService.getInstance().updateStatus("Output file location selected: " + f.getName());
        });
    }

    @FXML
    private void onGenerate() {
        AuthorMeta authorMeta = new AuthorMeta(
                tfAuthor.getText(), tfAddress.getText(), tfEmail.getText(),
                tfPhone.getText(), tfTitle.getText(), tfWords.getText()
        );
        SharedDataService.getInstance().setAuthorMeta(authorMeta);

        FormattingPrefs formattingPrefs = new FormattingPrefs(cbItalicToUnderline.isSelected());

        ConvertRequest request = new ConvertRequest(
                new File(tfInput.getText()), new File(tfOutput.getText()), authorMeta, formattingPrefs
        );

        List<String> errors = validationService.validate(request);
        if (!errors.isEmpty()) {
            Fx.error("Validation Error", String.join("\n", errors));
            StatusService.getInstance().updateStatus("Validation failed.");
            return;
        }

        Task<Void> generationTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                StatusService.getInstance().updateStatus("Processing file: " + request.inputFile().getName());
                engineBridge.process(request);
                return null;
            }
        };

        generationTask.setOnSucceeded(e -> {
            preferencesService.saveAuthorMeta(authorMeta); // Save on success
            Fx.alert("Success", "File saved to:\n" + request.outputFile().getAbsolutePath());
            StatusService.getInstance().updateStatus("Successfully generated: " + request.outputFile().getName());
        });

        generationTask.setOnFailed(e -> {
            Throwable ex = generationTask.getException();
            System.err.println("An error occurred during processing: " + ex.getMessage());
            ex.printStackTrace();
            Fx.error("Error", "An unexpected error occurred:\n" + ex.getMessage());
            StatusService.getInstance().updateStatus("Error during generation.");
        });

        progressIndicator.visibleProperty().bind(generationTask.runningProperty());
        contentBox.disableProperty().bind(generationTask.runningProperty());

        new Thread(generationTask).start();
    }
    
    private void setupDragAndDrop() {
        // Set initial style
        dragDropArea.setStyle(DRAG_IDLE_STYLE);
        
        // Setup drag and drop event handlers
        dragDropArea.setOnDragOver(this::handleDragOver);
        dragDropArea.setOnDragDropped(this::handleDragDropped);
        dragDropArea.setOnDragEntered(event -> {
            dragDropArea.setStyle(DRAG_HOVER_STYLE);
            event.consume();
        });
        dragDropArea.setOnDragExited(event -> {
            dragDropArea.setStyle(DRAG_IDLE_STYLE);
            event.consume();
        });
    }
    
    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != dragDropArea && event.getDragboard().hasFiles()) {
            // Check if the dragged files contain supported formats
            Dragboard db = event.getDragboard();
            List<File> files = db.getFiles();
            boolean hasSupportedFile = files.stream()
                    .anyMatch(file -> {
                        String fileName = file.getName().toLowerCase();
                        return fileName.endsWith(".docx") || fileName.endsWith(".odt") || 
                               fileName.endsWith(".md") || fileName.endsWith(".txt");
                    });
            
            if (hasSupportedFile) {
                event.acceptTransferModes(TransferMode.COPY);
            }
        }
        event.consume();
    }
    
    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            // Find the first supported file
            Optional<File> supportedFile = files.stream()
                    .filter(file -> {
                        String fileName = file.getName().toLowerCase();
                        return fileName.endsWith(".docx") || fileName.endsWith(".odt") || 
                               fileName.endsWith(".md") || fileName.endsWith(".txt");
                    })
                    .findFirst();
                    
            if (supportedFile.isPresent()) {
                File file = supportedFile.get();
                tfInput.setText(file.getAbsolutePath());
                StatusService.getInstance().updateStatus("Input file selected via drag & drop: " + file.getName());
                
                // Auto-generate output filename if not set
                if (Strings.isBlank(tfOutput.getText())) {
                    String outputName = file.getName().replaceFirst("[.][^.]+$", "") + " (Shunn).docx";
                    tfOutput.setText(new File(file.getParent(), outputName).getAbsolutePath());
                }
                success = true;
            } else {
                StatusService.getInstance().updateStatus("Please drop a supported file (.docx, .odt, .md, .txt).");
            }
        }
        
        // Reset style
        dragDropArea.setStyle(DRAG_IDLE_STYLE);
        
        event.setDropCompleted(success);
        event.consume();
    }
    
    @FXML
    private void onPreview() {
        // Preview functionality - currently placeholder
        StatusService.getInstance().updateStatus("Preview feature coming soon.");
        Fx.alert("Preview", "Preview functionality will be available in future versions.");
    }
}
