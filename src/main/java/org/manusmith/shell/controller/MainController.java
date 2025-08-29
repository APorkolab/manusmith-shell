package org.manusmith.shell.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.manusmith.shell.service.PreferencesService;
import org.manusmith.shell.service.StatusService;
import org.manusmith.shell.util.Fx;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class MainController {

    @FXML private Label lblStatus;
    @FXML private ChoiceBox<String> languageSelector;

    private PreferencesService preferencesService;

    @FXML
    public void initialize() {
        // Bind the status label to the singleton StatusService
        lblStatus.textProperty().bind(StatusService.getInstance().statusProperty());

        preferencesService = new PreferencesService();
        setupLanguageSelector();
    }

    private static final Map<String, String> LANGUAGE_MAP = Map.of(
            "English", "en",
            "Magyar", "hu"
    );

    private void setupLanguageSelector() {
        languageSelector.getItems().addAll(LANGUAGE_MAP.keySet());
        String savedLanguage = preferencesService.getLanguage();
        String currentLanguage = savedLanguage != null ? savedLanguage : Locale.getDefault().getLanguage();

        // Find the display name for the current language code
        String displayLanguage = LANGUAGE_MAP.entrySet().stream()
                .filter(entry -> entry.getValue().equals(currentLanguage))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("English");
        languageSelector.setValue(displayLanguage);

        languageSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                changeLanguage(newVal);
            }
        });
    }

    private void changeLanguage(String language) {
        String langCode = LANGUAGE_MAP.get(language);
        preferencesService.setLanguage(langCode);
        StatusService.getInstance().updateStatus("Language changed to: " + language + ".");
        org.manusmith.shell.MainApp.reload();
    }

    @FXML
    private void onOpen() {
        // This could be used for a global open action in the future
        StatusService.getInstance().updateStatus("Open action triggered.");
    }

    @FXML
    private void onPreferences() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/preferences.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Preferences");
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Fx.error("Error", "Could not open preferences window.");
        }
    }
}
