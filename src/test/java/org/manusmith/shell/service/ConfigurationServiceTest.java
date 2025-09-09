package org.manusmith.shell.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.manusmith.shell.config.ApplicationConfig;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for ConfigurationService
 */
class ConfigurationServiceTest {

    private ConfigurationService configurationService;

    @BeforeEach
    void setUp() {
        // Reset singleton for clean tests
        ConfigurationService.instance = null;
        configurationService = ConfigurationService.getInstance();
    }

    @Test
    void getInstance_shouldReturnSingleton() {
        ConfigurationService instance1 = ConfigurationService.getInstance();
        ConfigurationService instance2 = ConfigurationService.getInstance();
        
        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    void getConfig_shouldReturnNonNullConfiguration() {
        ApplicationConfig config = configurationService.getConfig();
        
        assertThat(config).isNotNull();
        assertThat(config.getApplication()).isNotNull();
        assertThat(config.getUi()).isNotNull();
        assertThat(config.getProcessing()).isNotNull();
        assertThat(config.getPerformance()).isNotNull();
        assertThat(config.getMetrics()).isNotNull();
        assertThat(config.getSecurity()).isNotNull();
    }

    @Test
    void getApplicationInfo_shouldReturnValidApplicationInfo() {
        ApplicationConfig.ApplicationInfo appInfo = configurationService.getApplicationInfo();
        
        assertThat(appInfo).isNotNull();
        assertThat(appInfo.getName()).isEqualTo("ManuSmith Shell");
        assertThat(appInfo.getVersion()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(appInfo.getDescription()).isNotEmpty();
    }

    @Test
    void getUiConfig_shouldReturnValidUiConfiguration() {
        ApplicationConfig.UiConfig uiConfig = configurationService.getUiConfig();
        
        assertThat(uiConfig).isNotNull();
        assertThat(uiConfig.getTheme()).isNotNull();
        assertThat(uiConfig.getWindow()).isNotNull();
        assertThat(uiConfig.getLocale()).isNotNull();
        
        // Test theme configuration
        assertThat(uiConfig.getTheme().getDefaultTheme()).isIn("light", "dark");
        assertThat(uiConfig.getTheme().isAllowUserToggle()).isTrue();
        
        // Test window configuration
        assertThat(uiConfig.getWindow().getDefaultWidth()).isGreaterThan(0);
        assertThat(uiConfig.getWindow().getDefaultHeight()).isGreaterThan(0);
        assertThat(uiConfig.getWindow().getMinWidth()).isGreaterThan(0);
        assertThat(uiConfig.getWindow().getMinHeight()).isGreaterThan(0);
        
        // Test locale configuration
        assertThat(uiConfig.getLocale().getDefaultLocale()).isNotEmpty();
        assertThat(uiConfig.getLocale().getSupported()).isNotEmpty();
    }

    @Test
    void getProcessingConfig_shouldReturnValidProcessingConfiguration() {
        ApplicationConfig.ProcessingConfig processingConfig = configurationService.getProcessingConfig();
        
        assertThat(processingConfig).isNotNull();
        assertThat(processingConfig.getMaxFileSizeMB()).isBetween(1, 500);
        assertThat(processingConfig.getTempDirectory()).isNotEmpty();
        assertThat(processingConfig.getSupportedFormats()).isNotNull();
        assertThat(processingConfig.getBackup()).isNotNull();
        
        // Test backup configuration
        ApplicationConfig.ProcessingConfig.BackupConfig backup = processingConfig.getBackup();
        assertThat(backup.isEnabled()).isTrue();
        assertThat(backup.getRetentionDays()).isBetween(1, 365);
    }

    @Test
    void getPerformanceConfig_shouldReturnValidPerformanceConfiguration() {
        ApplicationConfig.PerformanceConfig perfConfig = configurationService.getPerformanceConfig();
        
        assertThat(perfConfig).isNotNull();
        assertThat(perfConfig.isAsyncProcessing()).isTrue();
        assertThat(perfConfig.getThreadPoolSize()).isBetween(1, 16);
        assertThat(perfConfig.getMaxConcurrentOperations()).isBetween(1, 10);
    }

    @Test
    void getMetricsConfig_shouldReturnValidMetricsConfiguration() {
        ApplicationConfig.MetricsConfig metricsConfig = configurationService.getMetricsConfig();
        
        assertThat(metricsConfig).isNotNull();
        assertThat(metricsConfig.isEnabled()).isTrue();
        assertThat(metricsConfig.getJmx()).isNotNull();
        assertThat(metricsConfig.getJmx().isEnabled()).isTrue();
        assertThat(metricsConfig.getJmx().getDomain()).isEqualTo("org.manusmith.shell");
    }

    @Test
    void getSecurityConfig_shouldReturnValidSecurityConfiguration() {
        ApplicationConfig.SecurityConfig securityConfig = configurationService.getSecurityConfig();
        
        assertThat(securityConfig).isNotNull();
        assertThat(securityConfig.getFileAccess()).isNotNull();
        assertThat(securityConfig.getValidation()).isNotNull();
        
        // Test file access configuration
        ApplicationConfig.SecurityConfig.FileAccessConfig fileAccess = securityConfig.getFileAccess();
        assertThat(fileAccess.getBlockedExtensions()).isNotEmpty();
        assertThat(fileAccess.getBlockedExtensions()).contains("exe", "bat", "sh", "cmd");
        
        // Test validation configuration
        ApplicationConfig.SecurityConfig.ValidationConfig validation = securityConfig.getValidation();
        assertThat(validation.getMaxPathLength()).isBetween(50, 500);
        assertThat(validation.isSanitizeFilenames()).isTrue();
    }

    @Test
    void saveUserConfiguration_shouldCreateConfigurationFile(@TempDir Path tempDir) throws Exception {
        // Set temporary home directory
        System.setProperty("user.home", tempDir.toString());
        
        configurationService.saveUserConfiguration();
        
        Path expectedConfigPath = tempDir.resolve(".manusmith-shell/config/application.yml");
        assertThat(Files.exists(expectedConfigPath)).isTrue();
        assertThat(Files.size(expectedConfigPath)).isGreaterThan(0);
    }

    @Test 
    void reload_shouldReloadConfiguration() {
        ApplicationConfig originalConfig = configurationService.getConfig();
        
        assertThatCode(() -> configurationService.reload()).doesNotThrowAnyException();
        
        ApplicationConfig reloadedConfig = configurationService.getConfig();
        assertThat(reloadedConfig).isNotNull();
        // Configuration values should be the same after reload
        assertThat(reloadedConfig.getApplication().getName())
                .isEqualTo(originalConfig.getApplication().getName());
    }

    @Test
    void configurationValidation_shouldPassForDefaultConfiguration() {
        // This test verifies that the default configuration passes validation
        ApplicationConfig config = configurationService.getConfig();
        
        assertThat(config.getApplication().getName()).isNotEmpty();
        assertThat(config.getApplication().getVersion()).isNotEmpty();
        assertThat(config.getUi().getWindow().getDefaultWidth()).isGreaterThanOrEqualTo(600);
        assertThat(config.getUi().getWindow().getDefaultHeight()).isGreaterThanOrEqualTo(400);
        assertThat(config.getProcessing().getMaxFileSizeMB()).isBetween(1, 500);
        assertThat(config.getPerformance().getThreadPoolSize()).isBetween(1, 16);
        assertThat(config.getPerformance().getMaxConcurrentOperations()).isBetween(1, 10);
    }
}
