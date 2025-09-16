package org.manusmith.shell.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.manusmith.shell.service.FileDialogs;
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
    @FXML private TabPane tabs;

    private PreferencesService preferencesService;
    private FileDialogs fileDialogs;

    @FXML
    public void initialize() {
        // Bind the status label to the singleton StatusService
        lblStatus.textProperty().bind(StatusService.getInstance().statusProperty());

        preferencesService = new PreferencesService();
        fileDialogs = new FileDialogs();
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
        StatusService.getInstance().updateStatus("Opening file browser...");
        
        fileDialogs.showOpenDocxDialog(lblStatus.getScene().getWindow())
            .ifPresent(file -> {
                // Get the current tab and try to set the input file
                int selectedTabIndex = tabs.getSelectionModel().getSelectedIndex();
                
                if (selectedTabIndex == 0) { // Convert tab
                    // Need to access the ConvertController to set the input file
                    // For now, just show status message
                    StatusService.getInstance().updateStatus("File selected: " + file.getName() + " - Please drag and drop to the Convert tab or use its Browse button.");
                } else if (selectedTabIndex == 1) { // Quick Convert tab
                    StatusService.getInstance().updateStatus("File selected: " + file.getName() + " - Please drag and drop to the Quick Convert area.");
                } else {
                    StatusService.getInstance().updateStatus("File selected: " + file.getName() + " - Please switch to Convert or Quick Convert tab to use this file.");
                }
            });
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
    
    @FXML
    private void onToggleTheme() {
        // Toggle between light and dark theme
        Window window = languageSelector.getScene().getWindow();
        if (window instanceof Stage stage) {
            org.manusmith.shell.service.ThemeService.getInstance().toggleTheme(stage.getScene());
            StatusService.getInstance().updateStatus("Theme toggled.");
        } else {
            StatusService.getInstance().updateStatus("Cannot toggle theme - invalid window type.");
        }
    }
}
