package org.manusmith.shell.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.manusmith.shell.dto.AuthorMeta;
import org.manusmith.shell.dto.ConvertRequest;
import org.manusmith.shell.dto.FormattingPrefs;
import org.manusmith.shell.service.EngineBridge;
import org.manusmith.shell.service.FileDialogs;
import org.manusmith.shell.service.SharedDataService;
import org.manusmith.shell.service.StatusService;
import org.manusmith.shell.service.ValidationService;
import org.manusmith.shell.util.Fx;
import org.manusmith.shell.util.Strings;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class ConvertController {

    // --- FXML Fields ---
    @FXML private TextField tfInput;
    @FXML private TextField tfOutput;
    @FXML private TextField tfAuthor;
    @FXML private TextField tfAddress;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private TextField tfTitle;
    @FXML private TextField tfWords;

    // --- Services ---
    private FileDialogs fileDialogs;
    private EngineBridge engineBridge;
    private ValidationService validationService;

    @FXML
    public void initialize() {
        this.fileDialogs = new FileDialogs();
        this.engineBridge = new EngineBridge();
        this.validationService = new ValidationService();
    }

    @FXML
    private void onBrowseInput() {
        StatusService.getInstance().updateStatus("Opening file browser for input...");
        Optional<File> file = fileDialogs.showOpenDocxDialog(tfInput.getScene().getWindow());
        file.ifPresent(f -> {
            tfInput.setText(f.getAbsolutePath());
            StatusService.getInstance().updateStatus("Input file selected: " + f.getName());
            // Suggest an output file name based on the input
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
        StatusService.getInstance().updateStatus("Generating...");

        // --- 1. Gather Data ---
        File inputFile = new File(tfInput.getText());
        File outputFile = new File(tfOutput.getText());

        AuthorMeta authorMeta = new AuthorMeta(
                tfAuthor.getText(),
                tfAddress.getText(),
                tfEmail.getText(),
                tfPhone.getText(),
                tfTitle.getText(),
                tfWords.getText()
        );
        SharedDataService.getInstance().setAuthorMeta(authorMeta);

        // This is a bit of a hack. A cleaner way would be to use a shared model or dependency injection.
        // For the MVP, this is acceptable. The ID "cbItalicToUnderline" is in main.fxml.
        CheckBox cbItalicToUnderline = (CheckBox) tfInput.getScene().lookup("#cbItalicToUnderline");
        boolean italicToUnderline = cbItalicToUnderline != null && cbItalicToUnderline.isSelected();
        FormattingPrefs formattingPrefs = new FormattingPrefs(italicToUnderline);


        // --- 2. Create Request (DTO) ---
        ConvertRequest request = new ConvertRequest(inputFile, outputFile, authorMeta, formattingPrefs);

        // --- 3. Validation ---
        List<String> errors = validationService.validate(request);
        if (!errors.isEmpty()) {
            Fx.error("Validation Error", String.join("\n", errors));
            StatusService.getInstance().updateStatus("Validation failed.");
            return;
        }

        // --- 4. Call Engine ---
        StatusService.getInstance().updateStatus("Processing file: " + inputFile.getName());
        try {
            engineBridge.process(request);
            Fx.alert("Success", "File saved to:\n" + outputFile.getAbsolutePath());
            StatusService.getInstance().updateStatus("Successfully generated: " + outputFile.getName());
        } catch (Exception e) {
            System.err.println("An error occurred during processing: " + e.getMessage());
            e.printStackTrace();
            Fx.error("Error", "An unexpected error occurred:\n" + e.getMessage());
            StatusService.getInstance().updateStatus("Error during generation.");
        }
    }
}
