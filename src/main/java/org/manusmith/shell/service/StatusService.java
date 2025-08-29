package org.manusmith.shell.service;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.manusmith.shell.util.Fx;

public class StatusService {

    private static final StatusService INSTANCE = new StatusService();

    private final StringProperty statusProperty = new SimpleStringProperty("Ready.");

    private StatusService() {
        // Private constructor for singleton
    }

    public static StatusService getInstance() {
        return INSTANCE;
    }

    public StringProperty statusProperty() {
        return statusProperty;
    }

    public void updateStatus(String message) {
        Fx.run(() -> statusProperty.set(message));
    }

    public void clearStatus() {
        updateStatus("Ready.");
    }
}
