package org.manusmith.shell.service;

import org.manusmith.shell.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Security service for validating file access and sanitizing inputs.
 * Provides enterprise-grade security controls for file operations.
 */
public class SecurityService {
    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);
    
    private final ConfigurationService configurationService;
    private final Pattern pathTraversalPattern;
    private final Pattern filenamePattern;
    
    public SecurityService() {
        this.configurationService = ConfigurationService.getInstance();
        // Pattern to detect path traversal attempts
        this.pathTraversalPattern = Pattern.compile(".*(\\.\\./|\\.\\.\\\\).*");
        // Pattern for safe filenames (alphanumeric, spaces, hyphens, underscores, dots)
        this.filenamePattern = Pattern.compile("^[a-zA-Z0-9\\s\\-_.]+$");
    }

    /**
     * Validates file access permissions and security constraints
     * 
     * @param file The file to validate
     * @throws SecurityException If file access is not allowed
     */
    public void validateFileAccess(File file) throws SecurityException {
        if (file == null) {
            throw new SecurityException("File cannot be null");
        }

        String filePath = file.getAbsolutePath();
        ApplicationConfig.SecurityConfig securityConfig = configurationService.getSecurityConfig();

        // Check path length
        if (filePath.length() > securityConfig.getValidation().getMaxPathLength()) {
            throw new SecurityException("File path exceeds maximum allowed length: " + 
                    securityConfig.getValidation().getMaxPathLength());
        }

        // Check for path traversal attempts
        if (pathTraversalPattern.matcher(filePath).matches()) {
            logger.warn("Path traversal attempt detected: {}", filePath);
            throw new SecurityException("Path traversal attempts are not allowed");
        }

        // Check file extension against blocked list
        String extension = getFileExtension(file.getName());
        if (isBlockedExtension(extension)) {
            logger.warn("Blocked file extension access attempt: {}", extension);
            throw new SecurityException("File extension not allowed: " + extension);
        }

        // Check user home restriction
        if (securityConfig.getFileAccess().isRestrictToUserHome()) {
            String userHome = System.getProperty("user.home");
            if (!filePath.startsWith(userHome)) {
                throw new SecurityException("File access restricted to user home directory");
            }
        }

        // Sanitize filename if required
        if (securityConfig.getValidation().isSanitizeFilenames()) {
            if (!filenamePattern.matcher(file.getName()).matches()) {
                throw new SecurityException("Filename contains invalid characters: " + file.getName());
            }
        }

        logger.debug("File access validation passed: {}", filePath);
    }

    /**
     * Checks if a file extension is in the blocked list
     */
    private boolean isBlockedExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        
        List<String> blockedExtensions = configurationService.getSecurityConfig()
                .getFileAccess().getBlockedExtensions();
        
        return blockedExtensions.contains(extension.toLowerCase());
    }

    /**
     * Gets file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        
        return "";
    }

    /**
     * Sanitizes a filename by removing or replacing invalid characters
     */
    public String sanitizeFilename(String filename) {
        if (filename == null) {
            return null;
        }

        // Remove or replace invalid characters
        String sanitized = filename.replaceAll("[^a-zA-Z0-9\\s\\-_.]", "_");
        
        // Limit length
        int maxLength = Math.min(255, configurationService.getSecurityConfig()
                .getValidation().getMaxPathLength());
        
        if (sanitized.length() > maxLength) {
            String extension = getFileExtension(sanitized);
            int maxNameLength = maxLength - extension.length() - 1;
            if (maxNameLength > 0) {
                sanitized = sanitized.substring(0, maxNameLength) + "." + extension;
            } else {
                sanitized = sanitized.substring(0, maxLength);
            }
        }

        logger.debug("Filename sanitized: {} -> {}", filename, sanitized);
        return sanitized;
    }

    /**
     * Validates file size against configured limits
     */
    public void validateFileSize(File file) throws SecurityException {
        if (!file.exists()) {
            return; // File will be created, so we can't check size yet
        }

        long fileSizeBytes = file.length();
        long maxSizeBytes = configurationService.getProcessingConfig().getMaxFileSizeMB() * 1024 * 1024L;

        if (fileSizeBytes > maxSizeBytes) {
            throw new SecurityException(String.format(
                    "File size (%d bytes) exceeds maximum allowed size (%d bytes)",
                    fileSizeBytes, maxSizeBytes));
        }

        logger.debug("File size validation passed: {} bytes", fileSizeBytes);
    }

    /**
     * Calculates and logs file checksum for integrity tracking
     */
    public String calculateChecksum(File file) throws IOException {
        if (!file.exists() || !file.canRead()) {
            throw new IOException("Cannot read file for checksum calculation: " + file.getAbsolutePath());
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            byte[] hashBytes = digest.digest(fileBytes);
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            
            String checksum = sb.toString();
            logger.debug("File checksum calculated: {} -> {}", file.getName(), checksum);
            
            return checksum;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Creates a secure temporary file in the configured temp directory
     */
    public Path createSecureTempFile(String prefix, String suffix) throws IOException {
        String tempDir = configurationService.getProcessingConfig().getTempDirectory();
        Path tempDirPath = Path.of(tempDir);
        
        // Ensure temp directory exists
        Files.createDirectories(tempDirPath);
        
        // Create temp file with secure permissions
        Path tempFile = Files.createTempFile(tempDirPath, prefix, suffix);
        
        // Set restrictive permissions (owner read/write only)
        try {
            Files.setPosixFilePermissions(tempFile, 
                    java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException e) {
            // POSIX permissions not supported on this system (e.g., Windows)
            logger.debug("POSIX permissions not supported, skipping permission setup");
        }
        
        logger.debug("Secure temp file created: {}", tempFile);
        return tempFile;
    }

    /**
     * Validates that a directory is safe for operations
     */
    public void validateDirectory(File directory) throws SecurityException {
        if (directory == null) {
            throw new SecurityException("Directory cannot be null");
        }

        if (!directory.exists()) {
            throw new SecurityException("Directory does not exist: " + directory.getAbsolutePath());
        }

        if (!directory.isDirectory()) {
            throw new SecurityException("Path is not a directory: " + directory.getAbsolutePath());
        }

        validateFileAccess(directory);
    }
}
