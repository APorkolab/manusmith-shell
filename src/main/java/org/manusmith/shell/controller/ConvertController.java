package org.manusmith.shell.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
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

    @FXML private TextField tfInput;
    @FXML private TextField tfOutput;
    @FXML private TextField tfAuthor;
    @FXML private TextField tfAddress;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private TextField tfTitle;
    @FXML private TextField tfWords;

    private FileDialogs fileDialogs;
    private EngineBridge engineBridge;
    private ValidationService validationService;
    private PreferencesService preferencesService;

    @FXML
    public void initialize() {
        this.fileDialogs = new FileDialogs();
        this.engineBridge = new EngineBridge();
        this.validationService = new ValidationService();
        this.preferencesService = new PreferencesService();
        loadPreferences();
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
        StatusService.getInstance().updateStatus("Generating...");

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

        CheckBox cbItalicToUnderline = (CheckBox) tfInput.getScene().lookup("#cbItalicToUnderline");
        boolean italicToUnderline = cbItalicToUnderline != null && cbItalicToUnderline.isSelected();
        FormattingPrefs formattingPrefs = new FormattingPrefs(italicToUnderline);

        ConvertRequest request = new ConvertRequest(inputFile, outputFile, authorMeta, formattingPrefs);

        List<String> errors = validationService.validate(request);
        if (!errors.isEmpty()) {
            Fx.error("Validation Error", String.join("\n", errors));
            StatusService.getInstance().updateStatus("Validation failed.");
            return;
        }

        StatusService.getInstance().updateStatus("Processing file: " + inputFile.getName());
        try {
            engineBridge.process(request);
            preferencesService.saveAuthorMeta(authorMeta); // Save on success
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
