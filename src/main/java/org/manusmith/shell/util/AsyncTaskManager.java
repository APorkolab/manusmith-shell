package org.manusmith.shell.util;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility class for managing asynchronous tasks with JavaFX UI updates.
 * Provides memory-efficient handling of large file operations.
 */
public class AsyncTaskManager {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncTaskManager.class);
    private static final ExecutorService executorService = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors() / 2)
    );
    
    /**
     * Execute a background task with progress tracking
     */
    public static <T> void executeWithProgress(
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Exception> onError,
            ProgressIndicator progressIndicator,
            String taskName) {
        
        Task<T> task = new Task<T>() {
            @Override
            protected T call() throws Exception {
                logger.info("Starting background task: {}", taskName);
                updateMessage("Initializing " + taskName + "...");
                updateProgress(0, 100);
                
                try {
                    T result = backgroundTask.get();
                    updateProgress(100, 100);
                    updateMessage("Completed " + taskName);
                    return result;
                } catch (Exception e) {
                    logger.error("Error in background task {}: {}", taskName, e.getMessage(), e);
                    throw e;
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    try {
                        onSuccess.accept(getValue());
                        if (progressIndicator != null) {
                            progressIndicator.setVisible(false);
                        }
                    } catch (Exception e) {
                        logger.error("Error in success callback: {}", e.getMessage(), e);
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    Exception exception = (Exception) getException();
                    onError.accept(exception != null ? exception : new RuntimeException("Unknown error occurred"));
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                });
            }
        };
        
        if (progressIndicator != null) {
            progressIndicator.progressProperty().bind(task.progressProperty());
            progressIndicator.setVisible(true);
        }
        
        executorService.submit(task);
    }
    
    /**
     * Execute a simple background task without progress tracking
     */
    public static <T> CompletableFuture<T> executeAsync(Supplier<T> backgroundTask) {
        return CompletableFuture.supplyAsync(backgroundTask, executorService);
    }
    
    /**
     * Execute a UI update on the JavaFX Application Thread
     */
    public static void runOnUIThread(Runnable uiUpdate) {
        if (Platform.isFxApplicationThread()) {
            uiUpdate.run();
        } else {
            Platform.runLater(uiUpdate);
        }
    }
    
    /**
     * Check memory usage and suggest garbage collection if needed
     */
    public static void checkMemoryUsage(String context) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double memoryUsagePercent = (double) usedMemory / totalMemory * 100;
        
        logger.debug("Memory usage in {}: {:.1f}% ({} MB used, {} MB free)", 
                context, memoryUsagePercent, usedMemory / (1024 * 1024), freeMemory / (1024 * 1024));
        
        // Suggest GC if memory usage is high
        if (memoryUsagePercent > 85) {
            logger.info("High memory usage detected ({}%), suggesting garbage collection", memoryUsagePercent);
            System.gc();
        }
    }
    
    /**
     * Shutdown the executor service gracefully
     */
    public static void shutdown() {
        logger.info("Shutting down AsyncTaskManager executor service");
        executorService.shutdown();
    }
}
