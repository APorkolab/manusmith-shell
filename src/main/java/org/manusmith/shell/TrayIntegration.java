package org.manusmith.shell;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.manusmith.shell.service.EngineBridge;
import org.manusmith.shell.service.StatusService;
import org.manusmith.shell.service.ThemeService;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;

public class TrayIntegration {

    private final Stage stage;
    private final EngineBridge engineBridge;
    private TrayIcon trayIcon;
    private Timeline clipboardTimeline;
    private String lastClipboardText = "";

    public TrayIntegration(Stage stage, EngineBridge engineBridge) {
        this.stage = stage;
        this.engineBridge = engineBridge;
    }

    public void setupTray() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray is not supported.");
            return;
        }

        Platform.setImplicitExit(false);
        this.trayIcon = new TrayIcon(createPlaceholderImage(), "ManuSmith Shell");
        trayIcon.setImageAutoSize(true);

        PopupMenu popupMenu = createPopupMenu();
        trayIcon.setPopupMenu(popupMenu);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            System.err.println("Failed to add tray icon: " + e.getMessage());
        }
    }

    private PopupMenu createPopupMenu() {
        PopupMenu popupMenu = new PopupMenu();

        MenuItem showItem = new MenuItem("Show ManuSmith");
        showItem.addActionListener(e -> Platform.runLater(() -> {
            stage.show();
            stage.toFront();
        }));

        CheckboxMenuItem autoCleanItem = new CheckboxMenuItem("Auto-clean Clipboard");
        autoCleanItem.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                startClipboardMonitor();
            } else {
                stopClipboardMonitor();
            }
        });

        MenuItem themeItem = new MenuItem("Toggle Dark/Light Mode");
        themeItem.addActionListener(e -> Platform.runLater(() ->
            ThemeService.getInstance().toggleTheme(stage.getScene())
        ));

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            stopClipboardMonitor();
            SystemTray.getSystemTray().remove(trayIcon);
            Platform.exit();
        });

        popupMenu.add(showItem);
        popupMenu.addSeparator();
        popupMenu.add(autoCleanItem);
        popupMenu.add(themeItem);
        popupMenu.addSeparator();
        popupMenu.add(exitItem);

        return popupMenu;
    }

    private void startClipboardMonitor() {
        if (clipboardTimeline != null) {
            clipboardTimeline.stop();
        }
        clipboardTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> checkAndCleanClipboard()));
        clipboardTimeline.setCycleCount(Timeline.INDEFINITE);
        clipboardTimeline.play();
        StatusService.getInstance().updateStatus("Auto-clean enabled.");
    }

    private void stopClipboardMonitor() {
        if (clipboardTimeline != null) {
            clipboardTimeline.stop();
            clipboardTimeline = null;
        }
        StatusService.getInstance().updateStatus("Auto-clean disabled.");
    }

    private void checkAndCleanClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String currentText = (String) contents.getTransferData(DataFlavor.stringFlavor);
                if (currentText != null && !currentText.equals(lastClipboardText)) {
                    String cleanedText = engineBridge.cleanText(currentText, null);
                    if (!cleanedText.equals(currentText)) {
                        lastClipboardText = cleanedText;
                        clipboard.setContents(new StringSelection(cleanedText), null);
                        StatusService.getInstance().updateStatus("Clipboard cleaned.");
                    } else {
                        lastClipboardText = currentText;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore exceptions, e.g. "clipboard unavailable"
        }
    }

    private Image createPlaceholderImage() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(new Color(220, 220, 220));
        g2d.fillRect(0, 0, 16, 16);
        g2d.setColor(new Color(60, 60, 60));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("M", 2, 13);
        g2d.dispose();
        return image;
    }
}
