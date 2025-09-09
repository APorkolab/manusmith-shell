package org.manusmith.shell.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.jmx.JmxMeterRegistry;
import org.manusmith.shell.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for collecting and exposing application metrics.
 * Provides monitoring capabilities for performance tracking and operations insights.
 */
public class MetricsService {
    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);
    
    static MetricsService instance; // package-private for testing
    private final MeterRegistry meterRegistry;
    private final ConfigurationService configurationService;
    
    // Application metrics
    private final Timer documentProcessingTimer;
    private final AtomicInteger activeOperations;
    private final AtomicLong totalProcessedSize;
    
    // System metrics
    private final MemoryMXBean memoryBean;
    private final RuntimeMXBean runtimeBean;

    private MetricsService() {
        this.configurationService = ConfigurationService.getInstance();
        this.meterRegistry = createMeterRegistry();
        
        // Initialize timers
        this.documentProcessingTimer = Timer.builder("document.processing.time")
                .description("Time taken to process documents")
                .register(meterRegistry);
                
        this.activeOperations = new AtomicInteger(0);
        this.totalProcessedSize = new AtomicLong(0);
        
        // System metrics
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.runtimeBean = ManagementFactory.getRuntimeMXBean();
        
        registerGauges();
        
        if (configurationService.getMetricsConfig().isEnabled()) {
            logger.info("Metrics service initialized successfully");
        } else {
            logger.info("Metrics service initialized but disabled by configuration");
        }
    }

    public static synchronized MetricsService getInstance() {
        if (instance == null) {
            instance = new MetricsService();
        }
        return instance;
    }

    private MeterRegistry createMeterRegistry() {
        ApplicationConfig.MetricsConfig metricsConfig = configurationService.getMetricsConfig();
        
        if (metricsConfig.isEnabled() && metricsConfig.getJmx().isEnabled()) {
            JmxMeterRegistry jmxRegistry = new JmxMeterRegistry(
                    io.micrometer.jmx.JmxConfig.DEFAULT, 
                    io.micrometer.core.instrument.Clock.SYSTEM);
            jmxRegistry.config().commonTags("application", "manusmith-shell");
            return jmxRegistry;
        } else {
            // Return a no-op registry if metrics are disabled
            return new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        }
    }

    private void registerGauges() {
        // Active operations gauge
        Gauge.builder("operations.active", this, MetricsService::getActiveOperations)
                .description("Number of currently active operations")
                .register(meterRegistry);
                
        // Total processed size gauge
        Gauge.builder("documents.processed.size.total", this, MetricsService::getTotalProcessedSize)
                .description("Total size of processed documents in bytes")
                .baseUnit("bytes")
                .register(meterRegistry);
                
        // Memory usage gauges
        Gauge.builder("jvm.memory.used", this, m -> (double) m.memoryBean.getHeapMemoryUsage().getUsed())
                .description("JVM memory used")
                .baseUnit("bytes")
                .register(meterRegistry);
                
        Gauge.builder("jvm.memory.max", this, m -> (double) m.memoryBean.getHeapMemoryUsage().getMax())
                .description("JVM memory max")
                .baseUnit("bytes")
                .register(meterRegistry);
                
        // Uptime gauge
        Gauge.builder("application.uptime", this, m -> m.runtimeBean.getUptime() / 1000.0)
                .description("Application uptime in seconds")
                .baseUnit("seconds")
                .register(meterRegistry);
    }

    /**
     * Records a successful document processing operation
     */
    public void recordDocumentProcessed(String documentType, long fileSizeBytes, Duration processingTime) {
        if (!configurationService.getMetricsConfig().isEnabled()) {
            return;
        }
        
        Counter.builder("documents.processed")
                .description("Total number of documents processed")
                .tags("type", documentType, "status", "success")
                .register(meterRegistry)
                .increment();
        
        documentProcessingTimer.record(processingTime);
        totalProcessedSize.addAndGet(fileSizeBytes);
        
        logger.debug("Recorded document processing: type={}, size={}, time={}ms", 
                documentType, fileSizeBytes, processingTime.toMillis());
    }

    /**
     * Records a failed document processing operation
     */
    public void recordDocumentProcessingError(String documentType, String errorType, Throwable error) {
        if (!configurationService.getMetricsConfig().isEnabled()) {
            return;
        }
        
        Counter.builder("documents.processed")
                .description("Total number of documents processed")
                .tags("type", documentType, "status", "error")
                .register(meterRegistry)
                .increment();
        
        Counter.builder("errors.total")
                .description("Total number of errors")
                .tags("type", errorType, "exception", error.getClass().getSimpleName())
                .register(meterRegistry)
                .increment();
        
        logger.debug("Recorded document processing error: type={}, errorType={}, exception={}", 
                documentType, errorType, error.getClass().getSimpleName());
    }

    /**
     * Records the start of an operation
     */
    public void recordOperationStarted() {
        activeOperations.incrementAndGet();
    }

    /**
     * Records the end of an operation
     */
    public void recordOperationCompleted() {
        activeOperations.decrementAndGet();
    }

    /**
     * Records a custom counter increment
     */
    public void recordCounter(String name, String... tags) {
        if (!configurationService.getMetricsConfig().isEnabled()) {
            return;
        }
        
        Counter.builder(name)
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records a custom timer
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Safe timer sample that handles disabled metrics
     */
    public long stopTimer(Timer.Sample sample, String timerName) {
        if (!configurationService.getMetricsConfig().isEnabled()) {
            return 0;
        }
        return sample.stop(Timer.builder(timerName).register(meterRegistry));
    }

    /**
     * Gets the current number of active operations
     */
    public double getActiveOperations() {
        return activeOperations.get();
    }

    /**
     * Gets the total size of processed documents
     */
    public double getTotalProcessedSize() {
        return totalProcessedSize.get();
    }

    /**
     * Gets the total number of processed documents
     */
    public double getTotalDocumentsProcessed() {
        return meterRegistry.find("documents.processed").counters().stream()
                .mapToDouble(Counter::count)
                .sum();
    }

    /**
     * Gets the total number of errors
     */
    public double getTotalErrors() {
        return meterRegistry.find("errors.total").counters().stream()
                .mapToDouble(Counter::count)
                .sum();
    }

    /**
     * Gets the meter registry for advanced usage
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    /**
     * Gets application health summary
     */
    public HealthStatus getHealthStatus() {
        long uptime = runtimeBean.getUptime();
        double memoryUsagePercent = (double) memoryBean.getHeapMemoryUsage().getUsed() 
                / memoryBean.getHeapMemoryUsage().getMax() * 100;
        
        String status = "UP";
        if (memoryUsagePercent > 90) {
            status = "DEGRADED";
        }
        if (activeOperations.get() > configurationService.getPerformanceConfig().getMaxConcurrentOperations()) {
            status = "DEGRADED";
        }
        
        return new HealthStatus(status, uptime, memoryUsagePercent, activeOperations.get());
    }

    public static class HealthStatus {
        private final String status;
        private final long uptimeMs;
        private final double memoryUsagePercent;
        private final int activeOperations;

        public HealthStatus(String status, long uptimeMs, double memoryUsagePercent, int activeOperations) {
            this.status = status;
            this.uptimeMs = uptimeMs;
            this.memoryUsagePercent = memoryUsagePercent;
            this.activeOperations = activeOperations;
        }

        public String getStatus() { return status; }
        public long getUptimeMs() { return uptimeMs; }
        public double getMemoryUsagePercent() { return memoryUsagePercent; }
        public int getActiveOperations() { return activeOperations; }
    }
}
