package org.manusmith.shell.service;

import javafx.scene.Scene;

import java.util.Objects;

public class ThemeService {

    public enum Theme {
        LIGHT("styles/app.css"),
        DARK("styles/dark.css");

        private final String stylesheet;

        Theme(String stylesheet) {
            this.stylesheet = stylesheet;
        }

        public String getStylesheet() {
            return Objects.requireNonNull(getClass().getResource("/" + stylesheet)).toExternalForm();
        }
    }

    private static final ThemeService INSTANCE = new ThemeService();
    private Theme currentTheme = Theme.LIGHT;

    private ThemeService() {}

    public static ThemeService getInstance() {
        return INSTANCE;
    }

    public void toggleTheme(Scene scene) {
        // Remove current theme before applying new one
        scene.getStylesheets().remove(currentTheme.getStylesheet());

        // Switch to the other theme
        currentTheme = (currentTheme == Theme.LIGHT) ? Theme.DARK : Theme.LIGHT;

        // Apply new theme
        scene.getStylesheets().add(currentTheme.getStylesheet());
        StatusService.getInstance().updateStatus("Theme changed to " + currentTheme.name());
    }

    public void applyCurrentTheme(Scene scene) {
        scene.getStylesheets().add(currentTheme.getStylesheet());
    }
}
