package org.manusmith.shell.util;

// import javafx.embed.swing.SwingFXUtils; // Not available in standard JavaFX
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import javax.swing.*; // Using AWT instead
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Utility for generating file previews and thumbnails for supported document types.
 * Shows document metadata and creates visual previews for drag-drop areas.
 */
public class FilePreviewUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(FilePreviewUtil.class);
    
    // Cache for thumbnails to avoid regenerating them
    private static final Map<String, Image> thumbnailCache = new HashMap<>();
    
    // Standard thumbnail size
    private static final int THUMBNAIL_WIDTH = 120;
    private static final int THUMBNAIL_HEIGHT = 150;
    
    /**
     * Generate thumbnail for a file asynchronously
     */
    public static CompletableFuture<ImageView> generateThumbnailAsync(File file) {
        return CompletableFuture.supplyAsync(() -> generateThumbnail(file));
    }
    
    /**
     * Generate thumbnail for a file
     */
    public static ImageView generateThumbnail(File file) {
        if (file == null || !file.exists()) {
            return createDefaultThumbnail("File not found", "#FF6B6B");
        }
        
        try {
            // Check cache first
            String cacheKey = file.getAbsolutePath() + "_" + file.lastModified();
            Image cachedImage = thumbnailCache.get(cacheKey);
            
            if (cachedImage != null) {
                logger.debug("Using cached thumbnail for: {}", file.getName());
                return new ImageView(cachedImage);
            }
            
            // Generate new thumbnail based on file type
            String extension = getFileExtension(file).toLowerCase();
            ImageView thumbnail;
            switch (extension) {
                case "docx":
                    thumbnail = createDocumentThumbnail(file, "#2196F3", "DOCX");
                    break;
                case "odt":
                    thumbnail = createDocumentThumbnail(file, "#4CAF50", "ODT");
                    break;
                case "txt":
                    thumbnail = createTextThumbnail(file);
                    break;
                case "md":
                    thumbnail = createDocumentThumbnail(file, "#FF9800", "MD");
                    break;
                case "rtf":
                    thumbnail = createDocumentThumbnail(file, "#9C27B0", "RTF");
                    break;
                case "pdf":
                    thumbnail = createDocumentThumbnail(file, "#F44336", "PDF");
                    break;
                default:
                    thumbnail = createDefaultThumbnail(extension.toUpperCase(), "#757575");
                    break;
            }
            
            // Cache would go here if we had Image objects
            // thumbnailCache.put(cacheKey, thumbnail);
            
            thumbnail.setFitWidth(THUMBNAIL_WIDTH);
            thumbnail.setFitHeight(THUMBNAIL_HEIGHT);
            thumbnail.setPreserveRatio(true);
            
            return thumbnail;
            
        } catch (Exception e) {
            logger.error("Error generating thumbnail for {}: {}", file.getName(), e.getMessage(), e);
            return createDefaultThumbnail("Error", "#FF6B6B");
        }
    }
    
    /**
     * Create a document-style thumbnail with colored header
     */
    private static ImageView createDocumentThumbnail(File file, String headerColor, String fileType) {
        BufferedImage image = new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Background
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, 8, 8);
        
        // Drop shadow
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillRoundRect(2, 2, THUMBNAIL_WIDTH - 2, THUMBNAIL_HEIGHT - 2, 8, 8);
        
        // Border
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.drawRoundRect(0, 0, THUMBNAIL_WIDTH - 1, THUMBNAIL_HEIGHT - 1, 8, 8);
        
        // Header with file type
        g2d.setColor(Color.decode(headerColor));
        g2d.fillRoundRect(0, 0, THUMBNAIL_WIDTH, 30, 8, 8);
        g2d.fillRect(0, 22, THUMBNAIL_WIDTH, 8); // Square off bottom of header
        
        // File type text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(fileType);
        g2d.drawString(fileType, (THUMBNAIL_WIDTH - textWidth) / 2, 20);
        
        // File icon representation (lines)
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < 8; i++) {
            int y = 50 + (i * 10);
            int lineWidth = (i % 3 == 0) ? THUMBNAIL_WIDTH - 40 : THUMBNAIL_WIDTH - 20;
            g2d.drawLine(15, y, lineWidth, y);
        }
        
        // File info
        try {
            String sizeText = formatFileSize(file.length());
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.drawString(sizeText, 10, THUMBNAIL_HEIGHT - 5);
        } catch (Exception e) {
            logger.debug("Could not get file size for thumbnail");
        }
        
        g2d.dispose();
        
        // Create a simple colored rectangle as placeholder
        ImageView placeholder = new ImageView();
        placeholder.setStyle("-fx-background-color: " + headerColor + "; -fx-border-color: #ccc;");
        return placeholder;
    }
    
    /**
     * Create thumbnail for text files with content preview
     */
    private static ImageView createTextThumbnail(File file) {
        BufferedImage image = new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Background
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, 8, 8);
        
        // Border
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.drawRoundRect(0, 0, THUMBNAIL_WIDTH - 1, THUMBNAIL_HEIGHT - 1, 8, 8);
        
        // Header
        g2d.setColor(new Color(96, 125, 139)); // Blue Grey
        g2d.fillRoundRect(0, 0, THUMBNAIL_WIDTH, 25, 8, 8);
        g2d.fillRect(0, 17, THUMBNAIL_WIDTH, 8);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.drawString("TXT", 10, 17);
        
        // Try to show content preview
        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");
            
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.setFont(new Font("Consolas", Font.PLAIN, 8));
            
            int y = 40;
            int maxLines = Math.min(lines.length, 12);
            
            for (int i = 0; i < maxLines; i++) {
                String line = lines[i];
                if (line.length() > 20) {
                    line = line.substring(0, 17) + "...";
                }
                g2d.drawString(line, 8, y);
                y += 10;
            }
            
            if (lines.length > maxLines) {
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString("...", 8, y);
            }
            
        } catch (IOException e) {
            logger.debug("Could not read content for text thumbnail: {}", e.getMessage());
            
            // Fallback to generic representation
            g2d.setColor(new Color(0, 0, 0, 60));
            for (int i = 0; i < 10; i++) {
                int lineY = 35 + (i * 8);
                int lineWidth = THUMBNAIL_WIDTH - 20;
                g2d.drawLine(8, lineY, lineWidth, lineY);
            }
        }
        
        g2d.dispose();
        
        // Create a simple placeholder
        ImageView placeholder = new ImageView();
        placeholder.setStyle("-fx-background-color: #607D8B; -fx-border-color: #ccc;");
        return placeholder;
    }
    
    /**
     * Create default thumbnail for unknown file types
     */
    private static ImageView createDefaultThumbnail(String label, String color) {
        BufferedImage image = new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Background
        g2d.setColor(Color.decode(color));
        g2d.fillRoundRect(0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, 8, 8);
        
        // Label
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(label);
        int textHeight = fm.getHeight();
        
        g2d.drawString(label, 
            (THUMBNAIL_WIDTH - textWidth) / 2, 
            (THUMBNAIL_HEIGHT + textHeight) / 2 - 5
        );
        
        g2d.dispose();
        
        // Create a simple colored placeholder
        ImageView imageView = new ImageView();
        imageView.setStyle("-fx-background-color: " + color + "; -fx-border-color: #ccc;");
        imageView.setFitWidth(THUMBNAIL_WIDTH);
        imageView.setFitHeight(THUMBNAIL_HEIGHT);
        
        return imageView;
    }
    
    /**
     * Get file metadata information
     */
    public static FileMetadata getFileMetadata(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        
        try {
            Path path = file.toPath();
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            
            return new FileMetadata(
                file.getName(),
                formatFileSize(file.length()),
                file.length(),
                getFileExtension(file),
                LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault()),
                LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault()),
                attrs.isDirectory(),
                file.canRead(),
                file.canWrite()
            );
            
        } catch (IOException e) {
            logger.error("Error reading file metadata for {}: {}", file.getName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Format file size in human-readable format
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        
        String[] units = {"KB", "MB", "GB", "TB"};
        int unitIndex = -1;
        double size = bytes;
        
        do {
            size /= 1024.0;
            unitIndex++;
        } while (size >= 1024 && unitIndex < units.length - 1);
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }
    
    /**
     * Get file extension from filename
     */
    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : name.substring(lastDotIndex + 1);
    }
    
    /**
     * Check if file type is supported for preview
     */
    public static boolean isSupportedForPreview(String extension) {
        return extension.matches("(?i)(docx|odt|txt|md|rtf|pdf)");
    }
    
    /**
     * Clear thumbnail cache
     */
    public static void clearThumbnailCache() {
        thumbnailCache.clear();
        logger.debug("Thumbnail cache cleared");
    }
    
    /**
     * File metadata holder class
     */
    public static class FileMetadata {
        private final String name;
        private final String formattedSize;
        private final long sizeBytes;
        private final String extension;
        private final LocalDateTime created;
        private final LocalDateTime modified;
        private final boolean isDirectory;
        private final boolean canRead;
        private final boolean canWrite;
        
        public FileMetadata(String name, String formattedSize, long sizeBytes, String extension,
                          LocalDateTime created, LocalDateTime modified, boolean isDirectory,
                          boolean canRead, boolean canWrite) {
            this.name = name;
            this.formattedSize = formattedSize;
            this.sizeBytes = sizeBytes;
            this.extension = extension;
            this.created = created;
            this.modified = modified;
            this.isDirectory = isDirectory;
            this.canRead = canRead;
            this.canWrite = canWrite;
        }
        
        // Getters
        public String getName() { return name; }
        public String getFormattedSize() { return formattedSize; }
        public long getSizeBytes() { return sizeBytes; }
        public String getExtension() { return extension; }
        public LocalDateTime getCreated() { return created; }
        public LocalDateTime getModified() { return modified; }
        public boolean isDirectory() { return isDirectory; }
        public boolean canRead() { return canRead; }
        public boolean canWrite() { return canWrite; }
        
        @Override
        public String toString() {
            return String.format("%s (%s, %s)", name, formattedSize, extension.toUpperCase());
        }
    }
}
