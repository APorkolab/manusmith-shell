package org.manusmith.shell.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import org.manusmith.shell.service.PreferencesService;

public class PreferencesController {

    @FXML private CheckBox cbAlwaysNormalize;
    @FXML private ChoiceBox<String> cbDefaultProfile;
    private PreferencesService preferencesService;

    @FXML
    public void initialize() {
        preferencesService = new PreferencesService();

        // Setup "Always Normalize" checkbox
        cbAlwaysNormalize.setSelected(preferencesService.getAlwaysNormalize());
        cbAlwaysNormalize.selectedProperty().addListener((obs, oldVal, newVal) -> {
            preferencesService.setAlwaysNormalize(newVal);
            cbDefaultProfile.setDisable(!newVal);
        });

        // Setup default profile choice box
        cbDefaultProfile.setItems(FXCollections.observableArrayList("HU", "EN", "DE", "Shunn"));
        cbDefaultProfile.setValue(preferencesService.getDefaultProfile());
        cbDefaultProfile.setDisable(!cbAlwaysNormalize.isSelected());
        cbDefaultProfile.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                preferencesService.setDefaultProfile(newVal);
            }
        });
    }
}
