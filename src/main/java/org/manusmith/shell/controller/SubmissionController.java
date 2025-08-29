package org.manusmith.shell.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.manusmith.shell.service.FileDialogs;
import org.manusmith.shell.util.Fx;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class SubmissionController {

    @FXML private TextField tfMarket;
    @FXML private TextField tfGenre;
    @FXML private CheckBox cbSimSub;

    private FileDialogs fileDialogs;

    @FXML
    public void initialize() {
        this.fileDialogs = new FileDialogs();
    }

    @FXML
    private void onGenerate() {
        generateCoverLetterText().ifPresent(coverLetter -> {
            Optional<File> file = fileDialogs.showSaveTextDialog(tfMarket.getScene().getWindow(), tfMarket.getText() + " - Cover Letter.txt");
            file.ifPresent(f -> {
                try {
                    Files.writeString(f.toPath(), coverLetter);
                    Fx.alert("Success", "Cover letter saved to:\n" + f.getAbsolutePath());
                } catch (IOException e) {
                    Fx.error("Save Error", "Failed to save file:\n" + e.getMessage());
                }
            });
        });
    }

    @FXML
    private void onCopyToClipboard() {
        generateCoverLetterText().ifPresent(coverLetter -> {
            StringSelection stringSelection = new StringSelection(coverLetter);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            Fx.alert("Success", "Cover letter text copied to clipboard.");
        });
    }

    private Optional<String> generateCoverLetterText() {
        String market = tfMarket.getText();
        if (market == null || market.isBlank()) {
            Fx.error("Validation Error", "Market field is required.");
            return Optional.empty();
        }

        String genre = tfGenre.getText();
        boolean isSimSub = cbSimSub.isSelected();

        // This is a very basic template. In a real app, this would be more sophisticated.
        StringBuilder sb = new StringBuilder();
        sb.append("Dear editors at ").append(market).append(",\n\n");
        sb.append("Please consider my manuscript, [MANUSCRIPT TITLE], for publication.\n\n");
        sb.append("It is a ").append(genre.isBlank() ? "[genre]" : genre).append(" story of approximately [WORD COUNT] words.\n\n");
        if (isSimSub) {
            sb.append("This is a simultaneous submission.\n\n");
        }
        sb.append("Thank you for your time and consideration.\n\n");
        sb.append("Sincerely,\n");
        sb.append("[YOUR NAME]\n");

        return Optional.of(sb.toString());
    }
}
