package org.manusmith.shell.controller;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that previously disabled tabs (TypoFix and Cover Letter) are now enabled.
 */
@ExtendWith(ApplicationExtension.class)
public class EnabledTabsTest {

    private TabPane tabs;

    @Start
    public void start(Stage stage) throws Exception {
        // Set locale to Hungarian for testing
        Locale.setDefault(Locale.forLanguageTag("hu"));
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        loader.setResources(bundle);
        Scene scene = new Scene(loader.load());
        
        stage.setScene(scene);
        stage.show();
        
        // Get TabPane from the loaded scene
        this.tabs = (TabPane) scene.lookup("#tabs");
    }

    @Test
    public void testTypoFixTabIsEnabled() {
        WaitForAsyncUtils.waitForFxEvents();
        
        assertNotNull(tabs, "TabPane should be loaded");
        assertTrue(tabs.getTabs().size() >= 3, "Should have at least 3 tabs");
        
        // Find TypoFix tab (should be the 3rd tab, index 2)
        Tab typoFixTab = tabs.getTabs().get(2);
        assertFalse(typoFixTab.isDisabled(), "TypoFix tab should not be disabled");
        assertEquals("TypoFix", typoFixTab.getText(), "TypoFix tab should have correct text");
    }

    @Test 
    public void testCoverLetterTabIsEnabled() {
        WaitForAsyncUtils.waitForFxEvents();
        
        assertNotNull(tabs, "TabPane should be loaded");
        assertTrue(tabs.getTabs().size() >= 4, "Should have at least 4 tabs");
        
        // Find Cover Letter tab (should be the 4th tab, index 3)
        Tab coverLetterTab = tabs.getTabs().get(3);
        assertFalse(coverLetterTab.isDisabled(), "Cover Letter tab should not be disabled");
        assertEquals("Kísérőlevél", coverLetterTab.getText(), "Cover Letter tab should have correct text");
    }

    @Test
    public void testAllTabsAreAccessible() {
        WaitForAsyncUtils.waitForFxEvents();
        
        assertNotNull(tabs, "TabPane should be loaded");
        
        for (int i = 0; i < tabs.getTabs().size(); i++) {
            Tab tab = tabs.getTabs().get(i);
            assertFalse(tab.isDisabled(), "Tab " + i + " (" + tab.getText() + ") should not be disabled");
            
            // Try to select the tab
            tabs.getSelectionModel().select(i);
            WaitForAsyncUtils.waitForFxEvents(); // Wait for selection to complete
            assertEquals(i, tabs.getSelectionModel().getSelectedIndex(), "Should be able to select tab " + i);
        }
    }
}