package org.manusmith.shell;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.manusmith.shell.service.EngineBridge;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load the resource bundle
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());

        URL fxmlUrl = getClass().getResource("/fxml/main.fxml");
        if (fxmlUrl == null) {
            System.err.println("Cannot find main.fxml in resources. Check classpath.");
            throw new IOException("Cannot find FXML file.");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl, bundle);
        Scene scene = new Scene(loader.load(), 800, 600);

        primaryStage.setTitle(bundle.getString("app.title"));
        primaryStage.setScene(scene);

        // Apply the default theme
        org.manusmith.shell.service.ThemeService.getInstance().applyCurrentTheme(scene);

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

    public static void main(String[] args) {
        launch(args);
    }
}
