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

    private void setupLanguageSelector() {
        languageSelector.getItems().addAll("English", "Magyar");
        String savedLanguage = preferencesService.getLanguage();
        if (savedLanguage != null) {
            languageSelector.setValue(savedLanguage.equals("hu") ? "Magyar" : "English");
        } else {
            String defaultLanguage = Locale.getDefault().getLanguage();
            languageSelector.setValue(defaultLanguage.equals("hu") ? "Magyar" : "English");
        }

        languageSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                changeLanguage(newVal);
            }
        });
    }

    private void changeLanguage(String language) {
        String langCode = language.equals("Magyar") ? "hu" : "en";
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
        // To be implemented
        StatusService.getInstance().updateStatus("Preferences clicked.");
    }
}
