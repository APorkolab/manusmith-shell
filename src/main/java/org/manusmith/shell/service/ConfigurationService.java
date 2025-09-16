package org.manusmith.shell.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.manusmith.shell.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Service for loading and managing application configuration.
 * Provides singleton access to configuration with validation and fallback mechanisms.
 */
public class ConfigurationService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
    private static final String DEFAULT_CONFIG_FILE = "/application.yml";
    private static final String USER_CONFIG_FILE = "config/application.yml";
    
    static ConfigurationService instance; // package-private for testing
    private ApplicationConfig config;
    private final ObjectMapper yamlMapper;
    private final Validator validator;

    private ConfigurationService() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        loadConfiguration();
    }

    public static synchronized ConfigurationService getInstance() {
        if (instance == null) {
            instance = new ConfigurationService();
        }
        return instance;
    }

    private void loadConfiguration() {
        try {
            // Try to load user-specific configuration first
            Path userConfigPath = Paths.get(System.getProperty("user.home"))
                    .resolve(".manusmith-shell")
                    .resolve(USER_CONFIG_FILE);
            
            if (Files.exists(userConfigPath)) {
                logger.info("Loading user configuration from: {}", userConfigPath);
                config = yamlMapper.readValue(Files.newInputStream(userConfigPath), ApplicationConfig.class);
            } else {
                // Fall back to default configuration from resources
                logger.info("Loading default configuration from resources");
                try (InputStream is = getClass().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
                    if (is == null) {
                        throw new IllegalStateException("Default configuration file not found: " + DEFAULT_CONFIG_FILE);
                    }
                    config = yamlMapper.readValue(is, ApplicationConfig.class);
                }
            }

            // Validate configuration
            validateConfiguration();
            
            // Expand system properties in configuration
            expandSystemProperties();
            
            logger.info("Configuration loaded successfully");
        } catch (Exception e) {
            logger.error("Failed to load configuration, using defaults", e);
            // Create a minimal working configuration as fallback
            config = createDefaultConfiguration();
            logger.info("Using fallback default configuration");
        }
    }

    private void validateConfiguration() {
        Set<ConstraintViolation<ApplicationConfig>> violations = validator.validate(config);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Configuration validation failed:\n");
            for (ConstraintViolation<ApplicationConfig> violation : violations) {
                sb.append("  - ")
                  .append(violation.getPropertyPath())
                  .append(": ")
                  .append(violation.getMessage())
                  .append("\n");
            }
            throw new IllegalStateException(sb.toString());
        }
    }

    private void expandSystemProperties() {
        // Expand system properties in tempDirectory
        if (config.getProcessing().getTempDirectory() != null) {
            config.getProcessing().setTempDirectory(
                expandSystemProperty(config.getProcessing().getTempDirectory()));
        }

        // Expand system properties in backup directory
        if (config.getProcessing().getBackup().getDirectory() != null) {
            config.getProcessing().getBackup().setDirectory(
                expandSystemProperty(config.getProcessing().getBackup().getDirectory()));
        }
    }

    private String expandSystemProperty(String value) {
        if (value == null) return null;
        
        // Simple system property expansion
        if (value.contains("${java.io.tmpdir}")) {
            value = value.replace("${java.io.tmpdir}", System.getProperty("java.io.tmpdir"));
        }
        if (value.contains("${user.home}")) {
            value = value.replace("${user.home}", System.getProperty("user.home"));
        }
        if (value.contains("${user.dir}")) {
            value = value.replace("${user.dir}", System.getProperty("user.dir"));
        }
        
        return value;
    }

    /**
     * Reloads the configuration from disk
     */
    public void reload() {
        logger.info("Reloading configuration");
        loadConfiguration();
    }

    /**
     * Saves the current configuration to user-specific location
     */
    public void saveUserConfiguration() throws IOException {
        Path userConfigDir = Paths.get(System.getProperty("user.home"))
                .resolve(".manusmith-shell")
                .resolve("config");
        
        Files.createDirectories(userConfigDir);
        Path userConfigPath = userConfigDir.resolve("application.yml");
        
        yamlMapper.writeValue(Files.newOutputStream(userConfigPath), config);
        logger.info("User configuration saved to: {}", userConfigPath);
    }

    // Getters for configuration sections
    public ApplicationConfig getConfig() {
        return config;
    }

    public ApplicationConfig.ApplicationInfo getApplicationInfo() {
        return config.getApplication();
    }

    public ApplicationConfig.UiConfig getUiConfig() {
        return config.getUi();
    }

    public ApplicationConfig.ProcessingConfig getProcessingConfig() {
        return config.getProcessing();
    }

    public ApplicationConfig.PerformanceConfig getPerformanceConfig() {
        return config.getPerformance();
    }

    public ApplicationConfig.MetricsConfig getMetricsConfig() {
        return config.getMetrics();
    }

    public ApplicationConfig.SecurityConfig getSecurityConfig() {
        return config.getSecurity();
    }
    
    /**
     * Creates a minimal default configuration when YAML loading fails
     */
    private ApplicationConfig createDefaultConfiguration() {
        ApplicationConfig defaultConfig = new ApplicationConfig();
        
        // Application info
        ApplicationConfig.ApplicationInfo appInfo = new ApplicationConfig.ApplicationInfo();
        appInfo.setName("ManuSmith Shell");
        appInfo.setVersion("2.0.0");
        appInfo.setDescription("Professional manuscript processing tool");
        defaultConfig.setApplication(appInfo);
        
        // UI config
        ApplicationConfig.UiConfig uiConfig = new ApplicationConfig.UiConfig();
        ApplicationConfig.UiConfig.ThemeConfig themeConfig = new ApplicationConfig.UiConfig.ThemeConfig();
        themeConfig.setDefaultTheme("light");
        themeConfig.setAllowUserToggle(true);
        uiConfig.setTheme(themeConfig);
        
        ApplicationConfig.UiConfig.WindowConfig windowConfig = new ApplicationConfig.UiConfig.WindowConfig();
        windowConfig.setDefaultWidth(1000);
        windowConfig.setDefaultHeight(700);
        windowConfig.setMinWidth(800);
        windowConfig.setMinHeight(600);
        windowConfig.setResizable(true);
        uiConfig.setWindow(windowConfig);
        
        ApplicationConfig.UiConfig.LocaleConfig localeConfig = new ApplicationConfig.UiConfig.LocaleConfig();
        localeConfig.setDefaultLocale("en");
        localeConfig.setSupported(java.util.List.of("en", "hu"));
        uiConfig.setLocale(localeConfig);
        
        defaultConfig.setUi(uiConfig);
        
        // Processing config
        ApplicationConfig.ProcessingConfig processingConfig = new ApplicationConfig.ProcessingConfig();
        processingConfig.setTempDirectory(System.getProperty("java.io.tmpdir") + "/manusmith-shell");
        processingConfig.setMaxFileSizeMB(50);
        
        ApplicationConfig.ProcessingConfig.BackupConfig backupConfig = new ApplicationConfig.ProcessingConfig.BackupConfig();
        backupConfig.setEnabled(true);
        backupConfig.setDirectory(System.getProperty("user.home") + "/.manusmith-shell/backups");
        backupConfig.setRetentionDays(30);
        processingConfig.setBackup(backupConfig);
        
        defaultConfig.setProcessing(processingConfig);
        
        // Performance config
        ApplicationConfig.PerformanceConfig performanceConfig = new ApplicationConfig.PerformanceConfig();
        performanceConfig.setAsyncProcessing(true);
        performanceConfig.setThreadPoolSize(4);
        performanceConfig.setMaxConcurrentOperations(2);
        defaultConfig.setPerformance(performanceConfig);
        
        // Metrics config
        ApplicationConfig.MetricsConfig metricsConfig = new ApplicationConfig.MetricsConfig();
        metricsConfig.setEnabled(true);
        ApplicationConfig.MetricsConfig.JmxConfig jmxConfig = new ApplicationConfig.MetricsConfig.JmxConfig();
        jmxConfig.setEnabled(true);
        jmxConfig.setDomain("org.manusmith.shell");
        metricsConfig.setJmx(jmxConfig);
        defaultConfig.setMetrics(metricsConfig);
        
        // Security config
        ApplicationConfig.SecurityConfig securityConfig = new ApplicationConfig.SecurityConfig();
        defaultConfig.setSecurity(securityConfig);
        
        return defaultConfig;
    }
}
