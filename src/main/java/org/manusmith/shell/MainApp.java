package org.manusmith.shell;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.manusmith.shell.service.EngineBridge;
import org.manusmith.shell.service.PreferencesService;
import org.manusmith.shell.util.Fx;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainApp extends Application {

    private static volatile MainApp instance;
    private Stage primaryStage;

    public static void reload() {
        MainApp currentInstance = getInstance();
        if (currentInstance != null && currentInstance.primaryStage != null) {
            try {
                currentInstance.primaryStage.setScene(loadScene());
            } catch (IOException e) {
                e.printStackTrace();
                Fx.error("Error", "Failed to reload the UI.");
            }
        }
    }
    
    public static MainApp getInstance() {
        return instance;
    }
    
    private static synchronized void setInstance(MainApp app) {
        instance = app;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        System.out.println("JavaFX start() method called");
        setInstance(this);
        this.primaryStage = primaryStage;
        
        try {
            System.out.println("Loading scene...");
            Scene scene = loadScene();
            primaryStage.setScene(scene);
            System.out.println("Scene loaded and set");
            
            primaryStage.show();
            System.out.println("Primary stage shown");
        } catch (Exception e) {
            System.err.println("Error in start method: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // Handle window close to hide to tray
        primaryStage.setOnCloseRequest(event -> {
            primaryStage.hide();
            event.consume(); // Consume the event to prevent the window from closing
        });
        
        // Setup tray icon after stage is shown
        try {
            System.out.println("Setting up tray integration...");
            EngineBridge engineBridge = new EngineBridge();
            TrayIntegration tray = new TrayIntegration(primaryStage, engineBridge);
            tray.setupTray();
            System.out.println("Tray integration setup completed");
        } catch (Exception e) {
            System.err.println("Warning: Could not setup tray integration: " + e.getMessage());
            // Continue without tray - this is not critical for basic functionality
        }
    }

    private static Scene loadScene() throws IOException {
        // Load saved language or use default
        PreferencesService preferencesService = new PreferencesService();
        String langCode = preferencesService.getLanguage();
        Locale locale = (langCode != null) ? Locale.forLanguageTag(langCode) : Locale.getDefault();

        // Load the resource bundle
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);

        URL fxmlUrl = MainApp.class.getResource("/fxml/main.fxml");
        if (fxmlUrl == null) {
            System.err.println("Cannot find main.fxml in resources. Check classpath.");
            throw new IOException("Cannot find FXML file.");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl, bundle);
        Scene scene = new Scene(loader.load(), 800, 600);

        if (instance != null && instance.primaryStage != null) {
            instance.primaryStage.setTitle(bundle.getString("app.title"));
        }

        // Apply the default theme
        org.manusmith.shell.service.ThemeService.getInstance().applyCurrentTheme(scene);

        return scene;
    }

    public static void main(String[] args) {
        // Set macOS specific properties before JavaFX launch
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.awt.UIElement", "false");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");
            System.setProperty("java.awt.headless", "false");
        }
        
        // Debug information
        System.out.println("Starting ManuSmith Shell...");
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("JavaFX available: " + isJavaFXAvailable());
        
        launch(args);
    }
    
    private static boolean isJavaFXAvailable() {
        try {
            Class.forName("javafx.application.Application");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
