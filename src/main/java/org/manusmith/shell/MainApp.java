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
        setInstance(this);
        this.primaryStage = primaryStage;
        primaryStage.setScene(loadScene());
        primaryStage.show();

        // Setup tray icon
        // The engine bridge could be a singleton or managed by a DI framework later.
        // For now, we create it here and pass it where needed.
        EngineBridge engineBridge = new EngineBridge();
        TrayIntegration tray = new TrayIntegration(primaryStage, engineBridge);
        tray.setupTray();

        // Handle window close to hide to tray
        primaryStage.setOnCloseRequest(event -> {
            primaryStage.hide();
            event.consume(); // Consume the event to prevent the window from closing
        });
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
        launch(args);
    }
}
