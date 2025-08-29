package org.manusmith.shell.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.manusmith.shell.service.StatusService;

public class MainController {

    @FXML private Label lblStatus;

    @FXML
    public void initialize() {
        // Bind the status label to the singleton StatusService
        lblStatus.textProperty().bind(StatusService.getInstance().statusProperty());
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
