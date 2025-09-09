package org.manusmith.shell.util;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
// Runnable is in java.lang, not java.util.function

/**
 * Manages keyboard shortcuts and accessibility features for the application.
 * Provides consistent keyboard navigation and accelerators across all components.
 */
public class KeyboardShortcutManager {
    
    private static final Logger logger = LoggerFactory.getLogger(KeyboardShortcutManager.class);
    
    private final Scene scene;
    private final Map<KeyCombination, Runnable> shortcuts = new HashMap<>();
    
    public KeyboardShortcutManager(Scene scene) {
        this.scene = scene;
        setupGlobalShortcuts();
    }
    
    /**
     * Setup global application shortcuts
     */
    private void setupGlobalShortcuts() {
        scene.getAccelerators().clear();
        
        // File operations
        addShortcut(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN), "Open File");
        addShortcut(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), "Save File");
        addShortcut(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), "Save As");
        addShortcut(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN), "New File");
        
        // Edit operations
        addShortcut(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN), "Undo");
        addShortcut(new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN), "Redo");
        addShortcut(new KeyCodeCombination(KeyCode.A, KeyCombination.SHORTCUT_DOWN), "Select All");
        
        // View operations
        addShortcut(new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN), "Toggle Theme");
        addShortcut(new KeyCodeCombination(KeyCode.F11), "Toggle Fullscreen");
        addShortcut(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.SHORTCUT_DOWN), "Zoom Out");
        addShortcut(new KeyCodeCombination(KeyCode.PLUS, KeyCombination.SHORTCUT_DOWN), "Zoom In");
        addShortcut(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.SHORTCUT_DOWN), "Reset Zoom");
        
        // Navigation
        addShortcut(new KeyCodeCombination(KeyCode.TAB, KeyCombination.SHORTCUT_DOWN), "Next Tab");
        addShortcut(new KeyCodeCombination(KeyCode.TAB, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), "Previous Tab");
        addShortcut(new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN), "Close Tab");
        
        // Function keys
        addShortcut(new KeyCodeCombination(KeyCode.F1), "Help");
        addShortcut(new KeyCodeCombination(KeyCode.F5), "Refresh/Process");
        addShortcut(new KeyCodeCombination(KeyCode.F12), "Developer Tools");
        
        // Application
        addShortcut(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN), "Preferences");
        addShortcut(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN), "Quit");
        
        logger.info("Initialized {} global keyboard shortcuts", shortcuts.size());
    }
    
    /**
     * Add a keyboard shortcut with action
     */
    public void addShortcut(KeyCombination combination, String actionName, Runnable action) {
        shortcuts.put(combination, action);
        scene.getAccelerators().put(combination, action);
        logger.debug("Added keyboard shortcut: {} -> {}", combination, actionName);
    }
    
    /**
     * Add a keyboard shortcut (placeholder for now)
     */
    public void addShortcut(KeyCombination combination, String actionName) {
        addShortcut(combination, actionName, () -> 
            logger.debug("Shortcut triggered: {} ({})", combination, actionName)
        );
    }
    
    /**
     * Remove a keyboard shortcut
     */
    public void removeShortcut(KeyCombination combination) {
        shortcuts.remove(combination);
        scene.getAccelerators().remove(combination);
    }
    
    /**
     * Setup accessibility features for a node
     */
    public static void setupAccessibility(Node node, String accessibilityText) {
        if (node == null) return;
        
        // Set accessible text
        node.setAccessibleText(accessibilityText);
        
        // Set focusable if it's an interactive element
        if (node instanceof Button) {
            node.setFocusTraversable(true);
            
            // Add keyboard activation for buttons
            node.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.ENTER) {
                    if (node instanceof Button) {
                        ((Button) node).fire();
                    }
                    event.consume();
                }
            });
        }
        
        // Add focus indicators
        addFocusIndicators(node);
    }
    
    /**
     * Add visual focus indicators for keyboard navigation
     */
    private static void addFocusIndicators(Node node) {
        if (node == null) return;
        
        node.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Add focus styling
                node.setStyle(node.getStyle() + "; -fx-border-color: #2196F3; -fx-border-width: 2px;");
            } else {
                // Remove focus styling
                String style = node.getStyle();
                if (style != null) {
                    style = style.replaceAll("; -fx-border-color: #2196F3; -fx-border-width: 2px;", "");
                    node.setStyle(style);
                }
            }
        });
    }
    
    /**
     * Setup tab navigation for TabPane
     */
    public void setupTabNavigation(TabPane tabPane) {
        if (tabPane == null) return;
        
        // Ctrl+Tab / Ctrl+Shift+Tab for tab switching
        addShortcut(
            new KeyCodeCombination(KeyCode.TAB, KeyCombination.SHORTCUT_DOWN),
            "Next Tab",
            () -> {
                int currentIndex = tabPane.getSelectionModel().getSelectedIndex();
                int nextIndex = (currentIndex + 1) % tabPane.getTabs().size();
                tabPane.getSelectionModel().select(nextIndex);
            }
        );
        
        addShortcut(
            new KeyCodeCombination(KeyCode.TAB, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN),
            "Previous Tab", 
            () -> {
                int currentIndex = tabPane.getSelectionModel().getSelectedIndex();
                int prevIndex = currentIndex - 1;
                if (prevIndex < 0) {
                    prevIndex = tabPane.getTabs().size() - 1;
                }
                tabPane.getSelectionModel().select(prevIndex);
            }
        );
        
        // Numeric keys for direct tab access (Ctrl+1, Ctrl+2, etc.)
        for (int i = 0; i < Math.min(tabPane.getTabs().size(), 9); i++) {
            final int tabIndex = i;
            KeyCode keyCode = KeyCode.valueOf("DIGIT" + (i + 1));
            
            addShortcut(
                new KeyCodeCombination(keyCode, KeyCombination.SHORTCUT_DOWN),
                "Switch to Tab " + (i + 1),
                () -> tabPane.getSelectionModel().select(tabIndex)
            );
        }
    }
    
    /**
     * Setup mnemonics for menu items
     */
    public static void setupMnemonics(MenuItem menuItem, KeyCode mnemonicKey) {
        if (menuItem == null || mnemonicKey == null) return;
        
        String text = menuItem.getText();
        if (text != null && !text.isEmpty()) {
            String mnemonicChar = mnemonicKey.getName().toLowerCase();
            
            // Find the character in the text and add underscore before it
            int index = text.toLowerCase().indexOf(mnemonicChar);
            if (index >= 0) {
                String newText = text.substring(0, index) + "_" + text.substring(index);
                menuItem.setText(newText);
                menuItem.setMnemonicParsing(true);
            }
        }
    }
    
    /**
     * Enable high contrast mode for accessibility
     */
    public void enableHighContrastMode(boolean enable) {
        if (scene == null) return;
        
        if (enable) {
            scene.getStylesheets().add(getClass().getResource("/styles/high-contrast.css").toExternalForm());
            logger.info("High contrast mode enabled");
        } else {
            scene.getStylesheets().removeIf(stylesheet -> stylesheet.contains("high-contrast.css"));
            logger.info("High contrast mode disabled");
        }
    }
    
    /**
     * Set up focus traversal order for better keyboard navigation
     */
    public static void setupFocusTraversal(Node... nodes) {
        if (nodes == null || nodes.length == 0) return;
        
        for (int i = 0; i < nodes.length; i++) {
            final int currentIndex = i; // Make effectively final for lambda
            Node current = nodes[i];
            Node next = nodes[(i + 1) % nodes.length];
            
            current.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.TAB && !event.isShiftDown()) {
                    next.requestFocus();
                    event.consume();
                } else if (event.getCode() == KeyCode.TAB && event.isShiftDown()) {
                    Node previous = nodes[currentIndex == 0 ? nodes.length - 1 : currentIndex - 1];
                    previous.requestFocus();
                    event.consume();
                }
            });
        }
    }
    
    /**
     * Show keyboard shortcuts help dialog
     */
    public void showShortcutsHelp(Stage parentStage) {
        // This could open a dialog showing all available shortcuts
        // For now, just log them
        logger.info("Available keyboard shortcuts:");
        shortcuts.forEach((combination, action) -> 
            logger.info("  {} -> Action", combination)
        );
    }
    
    /**
     * Get all registered shortcuts
     */
    public Map<KeyCombination, Runnable> getShortcuts() {
        return new HashMap<>(shortcuts);
    }
    
    /**
     * Clear all shortcuts
     */
    public void clearShortcuts() {
        shortcuts.clear();
        scene.getAccelerators().clear();
        logger.info("All keyboard shortcuts cleared");
    }
}
