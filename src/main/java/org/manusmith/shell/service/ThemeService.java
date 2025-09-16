package org.manusmith.shell.service;

import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * Enhanced Theme Service with Material Design support and system theme detection
 */
public class ThemeService {
    private static final Logger logger = LoggerFactory.getLogger(ThemeService.class);
    private static final String THEME_PREFERENCE_KEY = "app.theme";
    private static final String AUTO_THEME_PREFERENCE_KEY = "app.theme.auto";

    public enum Theme {
        MATERIAL_LIGHT("Material Light", "styles/material-light.css"),
        MATERIAL_DARK("Material Dark", "styles/material-dark.css"),
        CLASSIC_LIGHT("Classic Light", "styles/app.css"),
        CLASSIC_DARK("Classic Dark", "styles/dark.css");

        private final String displayName;
        private final String stylesheet;

        Theme(String displayName, String stylesheet) {
            this.displayName = displayName;
            this.stylesheet = stylesheet;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getStylesheet() {
            try {
                return Objects.requireNonNull(getClass().getResource("/" + stylesheet)).toExternalForm();
            } catch (Exception e) {
                logger.warn("Could not load stylesheet: {}", stylesheet, e);
                return null;
            }
        }

        public boolean isDark() {
            return this == MATERIAL_DARK || this == CLASSIC_DARK;
        }

        public boolean isLight() {
            return this == MATERIAL_LIGHT || this == CLASSIC_LIGHT;
        }
    }

    private static final ThemeService INSTANCE = new ThemeService();
    private Theme currentTheme;
    private boolean autoTheme;
    private final Preferences preferences;

    private ThemeService() {
        this.preferences = Preferences.userNodeForPackage(ThemeService.class);
        loadThemePreferences();
    }

    public static ThemeService getInstance() {
        return INSTANCE;
    }

    /**
     * Load theme preferences from user preferences
     */
    private void loadThemePreferences() {
        try {
            String savedTheme = preferences.get(THEME_PREFERENCE_KEY, Theme.MATERIAL_LIGHT.name());
            this.currentTheme = Theme.valueOf(savedTheme);
            this.autoTheme = preferences.getBoolean(AUTO_THEME_PREFERENCE_KEY, false);
            
            if (autoTheme) {
                this.currentTheme = detectSystemTheme();
            }
            
            logger.info("Loaded theme preference: {} (auto: {})", currentTheme.getDisplayName(), autoTheme);
        } catch (Exception e) {
            logger.warn("Failed to load theme preferences, using default", e);
            this.currentTheme = Theme.MATERIAL_LIGHT;
            this.autoTheme = false;
        }
    }

    /**
     * Save current theme preferences
     */
    private void saveThemePreferences() {
        try {
            preferences.put(THEME_PREFERENCE_KEY, currentTheme.name());
            preferences.putBoolean(AUTO_THEME_PREFERENCE_KEY, autoTheme);
            preferences.flush();
            logger.debug("Saved theme preferences: {} (auto: {})", currentTheme.getDisplayName(), autoTheme);
        } catch (Exception e) {
            logger.warn("Failed to save theme preferences", e);
        }
    }

    /**
     * Detect system theme preference (works on macOS and Windows 10+)
     */
    private Theme detectSystemTheme() {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            
            if (osName.contains("mac")) {
                // macOS system theme detection
                Process process = new ProcessBuilder("defaults", "read", "-g", "AppleInterfaceStyle").start();
                process.waitFor();
                
                if (process.exitValue() == 0) {
                    // Dark mode is enabled
                    logger.debug("macOS dark mode detected");
                    return Theme.MATERIAL_DARK;
                } else {
                    // Light mode or detection failed
                    logger.debug("macOS light mode detected or detection failed");
                    return Theme.MATERIAL_LIGHT;
                }
            } else if (osName.contains("win")) {
                // Windows 10/11 system theme detection
                Process process = new ProcessBuilder("reg", "query", 
                    "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize", 
                    "/v", "AppsUseLightTheme").start();
                process.waitFor();
                
                if (process.exitValue() == 0) {
                    // Check registry value (0 = dark, 1 = light)
                    try (java.util.Scanner scanner = new java.util.Scanner(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            if (line.contains("AppsUseLightTheme") && line.contains("0x0")) {
                                logger.debug("Windows dark mode detected");
                                return Theme.MATERIAL_DARK;
                            }
                        }
                    }
                }
                
                logger.debug("Windows light mode detected or detection failed");
                return Theme.MATERIAL_LIGHT;
            }
        } catch (Exception e) {
            logger.debug("System theme detection failed, using default", e);
        }
        
        // Default to light theme if detection fails
        return Theme.MATERIAL_LIGHT;
    }

    /**
     * Toggle between light and dark versions of current theme style
     */
    public void toggleTheme(Scene scene) {
        Theme newTheme;
        
        if (currentTheme.isDark()) {
            // Switch to light version
            newTheme = currentTheme == Theme.MATERIAL_DARK ? Theme.MATERIAL_LIGHT : Theme.CLASSIC_LIGHT;
        } else {
            // Switch to dark version  
            newTheme = currentTheme == Theme.MATERIAL_LIGHT ? Theme.MATERIAL_DARK : Theme.CLASSIC_DARK;
        }
        
        setTheme(scene, newTheme);
    }

    /**
     * Set specific theme
     */
    public void setTheme(Scene scene, Theme theme) {
        if (theme == currentTheme) {
            return; // No change needed
        }
        
        // Remove current theme
        String currentStylesheet = currentTheme.getStylesheet();
        if (currentStylesheet != null) {
            scene.getStylesheets().remove(currentStylesheet);
        }
        
        // Apply new theme
        this.currentTheme = theme;
        String newStylesheet = theme.getStylesheet();
        if (newStylesheet != null) {
            scene.getStylesheets().add(newStylesheet);
        }
        
        // Save preferences
        saveThemePreferences();
        
        StatusService.getInstance().updateStatus("Theme changed to " + theme.getDisplayName());
        logger.info("Theme changed to: {}", theme.getDisplayName());
    }

    /**
     * Apply current theme to scene
     */
    public void applyCurrentTheme(Scene scene) {
        String stylesheet = currentTheme.getStylesheet();
        if (stylesheet != null && !scene.getStylesheets().contains(stylesheet)) {
            scene.getStylesheets().add(stylesheet);
            logger.debug("Applied theme: {}", currentTheme.getDisplayName());
        }
    }

    /**
     * Enable/disable automatic theme detection
     */
    public void setAutoTheme(Scene scene, boolean enable) {
        this.autoTheme = enable;
        
        if (enable) {
            Theme systemTheme = detectSystemTheme();
            setTheme(scene, systemTheme);
        }
        
        saveThemePreferences();
        logger.info("Auto theme {}: {}", enable ? "enabled" : "disabled", enable ? currentTheme.getDisplayName() : "manual");
    }

    /**
     * Get current theme
     */
    public Theme getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Check if auto theme is enabled
     */
    public boolean isAutoThemeEnabled() {
        return autoTheme;
    }

    /**
     * Get all available themes
     */
    public Theme[] getAvailableThemes() {
        return Theme.values();
    }

    /**
     * Refresh system theme if auto theme is enabled
     */
    public void refreshSystemTheme(Scene scene) {
        if (autoTheme) {
            Theme detectedTheme = detectSystemTheme();
            if (detectedTheme != currentTheme) {
                setTheme(scene, detectedTheme);
            }
        }
    }
}
