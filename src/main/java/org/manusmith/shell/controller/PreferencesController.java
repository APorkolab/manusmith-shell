package org.manusmith.shell.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import org.manusmith.shell.service.PreferencesService;

public class PreferencesController {

    @FXML private CheckBox cbAlwaysNormalize;
    private PreferencesService preferencesService;

    @FXML
    public void initialize() {
        preferencesService = new PreferencesService();
        cbAlwaysNormalize.setSelected(preferencesService.getAlwaysNormalize());

        cbAlwaysNormalize.selectedProperty().addListener((obs, oldVal, newVal) -> {
            preferencesService.setAlwaysNormalize(newVal);
        });
    }
}
