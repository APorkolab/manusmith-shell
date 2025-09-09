package org.manusmith.shell.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Application configuration class that represents the structure of application.yml
 */
public class ApplicationConfig {

    @Valid
    @NotNull
    @JsonProperty("application")
    private ApplicationInfo application;

    @Valid
    @NotNull
    @JsonProperty("ui")
    private UiConfig ui;

    @Valid
    @NotNull
    @JsonProperty("processing")
    private ProcessingConfig processing;

    @Valid
    @NotNull
    @JsonProperty("performance")
    private PerformanceConfig performance;

    @Valid
    @NotNull
    @JsonProperty("metrics")
    private MetricsConfig metrics;

    @Valid
    @NotNull
    @JsonProperty("security")
    private SecurityConfig security;

    // Getters and setters
    public ApplicationInfo getApplication() { return application; }
    public void setApplication(ApplicationInfo application) { this.application = application; }

    public UiConfig getUi() { return ui; }
    public void setUi(UiConfig ui) { this.ui = ui; }

    public ProcessingConfig getProcessing() { return processing; }
    public void setProcessing(ProcessingConfig processing) { this.processing = processing; }

    public PerformanceConfig getPerformance() { return performance; }
    public void setPerformance(PerformanceConfig performance) { this.performance = performance; }

    public MetricsConfig getMetrics() { return metrics; }
    public void setMetrics(MetricsConfig metrics) { this.metrics = metrics; }

    public SecurityConfig getSecurity() { return security; }
    public void setSecurity(SecurityConfig security) { this.security = security; }

    public static class ApplicationInfo {
        @NotEmpty
        private String name;
        @NotEmpty
        private String version;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class UiConfig {
        @Valid
        @NotNull
        private ThemeConfig theme;
        
        @Valid
        @NotNull
        private WindowConfig window;
        
        @Valid
        @NotNull
        private LocaleConfig locale;

        public ThemeConfig getTheme() { return theme; }
        public void setTheme(ThemeConfig theme) { this.theme = theme; }

        public WindowConfig getWindow() { return window; }
        public void setWindow(WindowConfig window) { this.window = window; }

        public LocaleConfig getLocale() { return locale; }
        public void setLocale(LocaleConfig locale) { this.locale = locale; }

        public static class ThemeConfig {
            @JsonProperty("default")
            private String defaultTheme = "light";
            private boolean allowUserToggle = true;

            public String getDefaultTheme() { return defaultTheme; }
            public void setDefaultTheme(String defaultTheme) { this.defaultTheme = defaultTheme; }

            public boolean isAllowUserToggle() { return allowUserToggle; }
            public void setAllowUserToggle(boolean allowUserToggle) { this.allowUserToggle = allowUserToggle; }
        }

        public static class WindowConfig {
            @Min(600)
            private int defaultWidth = 1000;
            @Min(400)
            private int defaultHeight = 700;
            @Min(400)
            private int minWidth = 800;
            @Min(300)
            private int minHeight = 600;
            private boolean resizable = true;

            public int getDefaultWidth() { return defaultWidth; }
            public void setDefaultWidth(int defaultWidth) { this.defaultWidth = defaultWidth; }

            public int getDefaultHeight() { return defaultHeight; }
            public void setDefaultHeight(int defaultHeight) { this.defaultHeight = defaultHeight; }

            public int getMinWidth() { return minWidth; }
            public void setMinWidth(int minWidth) { this.minWidth = minWidth; }

            public int getMinHeight() { return minHeight; }
            public void setMinHeight(int minHeight) { this.minHeight = minHeight; }

            public boolean isResizable() { return resizable; }
            public void setResizable(boolean resizable) { this.resizable = resizable; }
        }

        public static class LocaleConfig {
            @JsonProperty("default")
            private String defaultLocale = "en";
            private List<String> supported = List.of("en", "hu");

            public String getDefaultLocale() { return defaultLocale; }
            public void setDefaultLocale(String defaultLocale) { this.defaultLocale = defaultLocale; }

            public List<String> getSupported() { return supported; }
            public void setSupported(List<String> supported) { this.supported = supported; }
        }
    }

    public static class ProcessingConfig {
        private String tempDirectory;
        @Min(1)
        @Max(500)
        private int maxFileSizeMB = 50;
        private Map<String, List<String>> supportedFormats;
        private BackupConfig backup;

        public String getTempDirectory() { return tempDirectory; }
        public void setTempDirectory(String tempDirectory) { this.tempDirectory = tempDirectory; }

        public int getMaxFileSizeMB() { return maxFileSizeMB; }
        public void setMaxFileSizeMB(int maxFileSizeMB) { this.maxFileSizeMB = maxFileSizeMB; }

        public Map<String, List<String>> getSupportedFormats() { return supportedFormats; }
        public void setSupportedFormats(Map<String, List<String>> supportedFormats) { this.supportedFormats = supportedFormats; }

        public BackupConfig getBackup() { return backup; }
        public void setBackup(BackupConfig backup) { this.backup = backup; }

        public static class BackupConfig {
            private boolean enabled = true;
            private String directory;
            @Min(1)
            @Max(365)
            private int retentionDays = 30;

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }

            public String getDirectory() { return directory; }
            public void setDirectory(String directory) { this.directory = directory; }

            public int getRetentionDays() { return retentionDays; }
            public void setRetentionDays(int retentionDays) { this.retentionDays = retentionDays; }
        }
    }

    public static class PerformanceConfig {
        private boolean asyncProcessing = true;
        @Min(1)
        @Max(16)
        private int threadPoolSize = 4;
        @Min(1)
        @Max(10)
        private int maxConcurrentOperations = 2;

        public boolean isAsyncProcessing() { return asyncProcessing; }
        public void setAsyncProcessing(boolean asyncProcessing) { this.asyncProcessing = asyncProcessing; }

        public int getThreadPoolSize() { return threadPoolSize; }
        public void setThreadPoolSize(int threadPoolSize) { this.threadPoolSize = threadPoolSize; }

        public int getMaxConcurrentOperations() { return maxConcurrentOperations; }
        public void setMaxConcurrentOperations(int maxConcurrentOperations) { this.maxConcurrentOperations = maxConcurrentOperations; }
    }

    public static class MetricsConfig {
        private boolean enabled = true;
        private JmxConfig jmx;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public JmxConfig getJmx() { return jmx; }
        public void setJmx(JmxConfig jmx) { this.jmx = jmx; }

        public static class JmxConfig {
            private boolean enabled = true;
            private String domain = "org.manusmith.shell";

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }

            public String getDomain() { return domain; }
            public void setDomain(String domain) { this.domain = domain; }
        }
    }

    public static class SecurityConfig {
        private FileAccessConfig fileAccess;
        private ValidationConfig validation;

        public FileAccessConfig getFileAccess() { return fileAccess; }
        public void setFileAccess(FileAccessConfig fileAccess) { this.fileAccess = fileAccess; }

        public ValidationConfig getValidation() { return validation; }
        public void setValidation(ValidationConfig validation) { this.validation = validation; }

        public static class FileAccessConfig {
            private boolean restrictToUserHome = false;
            private List<String> blockedExtensions = List.of("exe", "bat", "sh", "cmd");

            public boolean isRestrictToUserHome() { return restrictToUserHome; }
            public void setRestrictToUserHome(boolean restrictToUserHome) { this.restrictToUserHome = restrictToUserHome; }

            public List<String> getBlockedExtensions() { return blockedExtensions; }
            public void setBlockedExtensions(List<String> blockedExtensions) { this.blockedExtensions = blockedExtensions; }
        }

        public static class ValidationConfig {
            @Min(50)
            @Max(500)
            private int maxPathLength = 260;
            private boolean sanitizeFilenames = true;

            public int getMaxPathLength() { return maxPathLength; }
            public void setMaxPathLength(int maxPathLength) { this.maxPathLength = maxPathLength; }

            public boolean isSanitizeFilenames() { return sanitizeFilenames; }
            public void setSanitizeFilenames(boolean sanitizeFilenames) { this.sanitizeFilenames = sanitizeFilenames; }
        }
    }
}
