package org.manusmith.shell;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.manusmith.shell.service.EngineBridge;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;

public class TrayIntegration {

    private final Stage stage;
    private final EngineBridge engineBridge;
    private TrayIcon trayIcon;

    public TrayIntegration(Stage stage, EngineBridge engineBridge) {
        this.stage = stage;
        this.engineBridge = engineBridge;
    }

    public void setupTray() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray is not supported. Skipping tray integration.");
            return;
        }

        Platform.setImplicitExit(false); // Don't exit on window close

        this.trayIcon = new TrayIcon(createPlaceholderImage(), "ManuSmith Shell");
        trayIcon.setImageAutoSize(true);

        PopupMenu popupMenu = new PopupMenu();
        MenuItem showItem = new MenuItem("Show ManuSmith");
        MenuItem pasteCleanItem = new MenuItem("Paste Clean");
        MenuItem exitItem = new MenuItem("Exit");

        showItem.addActionListener(e -> Platform.runLater(() -> {
            stage.show();
            stage.toFront();
        }));

        pasteCleanItem.addActionListener(e -> handlePasteClean());

        exitItem.addActionListener(e -> {
            SystemTray.getSystemTray().remove(trayIcon);
            Platform.exit();
        });

        popupMenu.add(showItem);
        popupMenu.add(pasteCleanItem);
        popupMenu.addSeparator();
        popupMenu.add(exitItem);

        trayIcon.setPopupMenu(popupMenu);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            System.err.println("Failed to add tray icon: " + e.getMessage());
        }
    }

    private void handlePasteClean() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        if (contents != null && contents.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor)) {
            try {
                String text = (String) contents.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor);
                String cleanedText = engineBridge.cleanText(text);
                StringSelection stringSelection = new StringSelection(cleanedText);
                clipboard.setContents(stringSelection, null);
                System.out.println("Clipboard text cleaned.");
            } catch (Exception ex) {
                System.err.println("Failed to clean clipboard text: " + ex.getMessage());
            }
        }
    }

    private Image createPlaceholderImage() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(new Color(220, 220, 220)); // Lighter gray
        g2d.fillRect(0, 0, 16, 16);
        g2d.setColor(new Color(60, 60, 60));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("M", 2, 13);
        g2d.dispose();
        return image;
    }
}
