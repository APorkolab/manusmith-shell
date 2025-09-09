package org.manusmith.shell.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for SecurityService
 */
class SecurityServiceTest {

    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        // Reset ConfigurationService for clean tests
        ConfigurationService.instance = null;
        this.securityService = new SecurityService();
    }

    @Test
    void validateFileAccess_withValidFile_shouldPass(@TempDir Path tempDir) throws Exception {
        File validFile = tempDir.resolve("test.txt").toFile();
        Files.createFile(validFile.toPath());

        assertThatCode(() -> securityService.validateFileAccess(validFile))
                .doesNotThrowAnyException();
    }

    @Test
    void validateFileAccess_withNullFile_shouldThrowSecurityException() {
        assertThatThrownBy(() -> securityService.validateFileAccess(null))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("File cannot be null");
    }

    @Test
    void validateFileAccess_withBlockedExtension_shouldThrowSecurityException(@TempDir Path tempDir) throws Exception {
        File blockedFile = tempDir.resolve("malware.exe").toFile();
        Files.createFile(blockedFile.toPath());

        assertThatThrownBy(() -> securityService.validateFileAccess(blockedFile))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("File extension not allowed: exe");
    }

    @Test
    void validateFileAccess_withPathTraversal_shouldThrowSecurityException() {
        File pathTraversalFile = new File("../../../etc/passwd");

        assertThatThrownBy(() -> securityService.validateFileAccess(pathTraversalFile))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Path traversal attempts are not allowed");
    }

    @Test
    void validateFileAccess_withInvalidCharacters_shouldThrowSecurityException(@TempDir Path tempDir) throws Exception {
        File invalidFile = tempDir.resolve("test<>file.txt").toFile();
        Files.createFile(invalidFile.toPath());

        assertThatThrownBy(() -> securityService.validateFileAccess(invalidFile))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Filename contains invalid characters");
    }

    @Test
    void sanitizeFilename_withValidFilename_shouldReturnUnchanged() {
        String validFilename = "valid-filename_123.txt";
        
        String result = securityService.sanitizeFilename(validFilename);
        
        assertThat(result).isEqualTo(validFilename);
    }

    @Test
    void sanitizeFilename_withInvalidCharacters_shouldSanitize() {
        String invalidFilename = "test<>file|name*.txt";
        
        String result = securityService.sanitizeFilename(invalidFilename);
        
        assertThat(result).isEqualTo("test__file_name_.txt");
    }

    @Test
    void sanitizeFilename_withNullInput_shouldReturnNull() {
        String result = securityService.sanitizeFilename(null);
        
        assertThat(result).isNull();
    }

    @Test
    void sanitizeFilename_withLongFilename_shouldTruncate() {
        String longFilename = "a".repeat(300) + ".txt";
        
        String result = securityService.sanitizeFilename(longFilename);
        
        assertThat(result.length()).isLessThanOrEqualTo(260); // Max path length from config
        assertThat(result).endsWith(".txt");
    }

    @Test
    void validateFileSize_withValidFile_shouldPass(@TempDir Path tempDir) throws Exception {
        File validFile = tempDir.resolve("small-file.txt").toFile();
        Files.write(validFile.toPath(), "test content".getBytes());

        assertThatCode(() -> securityService.validateFileSize(validFile))
                .doesNotThrowAnyException();
    }

    @Test
    void validateFileSize_withNonExistentFile_shouldPass(@TempDir Path tempDir) {
        File nonExistentFile = tempDir.resolve("non-existent.txt").toFile();

        assertThatCode(() -> securityService.validateFileSize(nonExistentFile))
                .doesNotThrowAnyException();
    }

    @Test
    void calculateChecksum_withValidFile_shouldReturnChecksum(@TempDir Path tempDir) throws Exception {
        File testFile = tempDir.resolve("test.txt").toFile();
        String testContent = "Hello, World!";
        Files.write(testFile.toPath(), testContent.getBytes());

        String checksum = securityService.calculateChecksum(testFile);

        assertThat(checksum).isNotNull();
        assertThat(checksum).hasSize(64); // SHA-256 produces 64 character hex string
        assertThat(checksum).matches("[a-f0-9]+");
    }

    @Test
    void calculateChecksum_withNonExistentFile_shouldThrowIOException(@TempDir Path tempDir) {
        File nonExistentFile = tempDir.resolve("non-existent.txt").toFile();

        assertThatThrownBy(() -> securityService.calculateChecksum(nonExistentFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Cannot read file for checksum calculation");
    }

    @Test
    void calculateChecksum_withSameContent_shouldProduceSameChecksum(@TempDir Path tempDir) throws Exception {
        String content = "test content";
        
        File file1 = tempDir.resolve("file1.txt").toFile();
        File file2 = tempDir.resolve("file2.txt").toFile();
        
        Files.write(file1.toPath(), content.getBytes());
        Files.write(file2.toPath(), content.getBytes());

        String checksum1 = securityService.calculateChecksum(file1);
        String checksum2 = securityService.calculateChecksum(file2);

        assertThat(checksum1).isEqualTo(checksum2);
    }

    @Test
    void createSecureTempFile_shouldCreateFileWithCorrectPrefix() throws Exception {
        Path tempFile = securityService.createSecureTempFile("test-", ".tmp");

        assertThat(Files.exists(tempFile)).isTrue();
        assertThat(tempFile.getFileName().toString()).startsWith("test-");
        assertThat(tempFile.getFileName().toString()).endsWith(".tmp");
        
        // Clean up
        Files.deleteIfExists(tempFile);
    }

    @Test
    void createSecureTempFile_shouldCreateFileInConfiguredTempDirectory() throws Exception {
        Path tempFile = securityService.createSecureTempFile("test-", ".tmp");
        
        ConfigurationService configService = ConfigurationService.getInstance();
        String expectedTempDir = configService.getProcessingConfig().getTempDirectory();
        
        // Normalize paths to handle different path separators and resolve any path issues
        String normalizedTempFilePath = tempFile.toAbsolutePath().normalize().toString();
        String normalizedExpectedTempDir = Path.of(expectedTempDir).toAbsolutePath().normalize().toString();
        
        assertThat(normalizedTempFilePath).contains(normalizedExpectedTempDir);
        
        // Clean up
        Files.deleteIfExists(tempFile);
    }

    @Test
    void validateDirectory_withValidDirectory_shouldPass(@TempDir Path tempDir) {
        assertThatCode(() -> securityService.validateDirectory(tempDir.toFile()))
                .doesNotThrowAnyException();
    }

    @Test
    void validateDirectory_withNullDirectory_shouldThrowSecurityException() {
        assertThatThrownBy(() -> securityService.validateDirectory(null))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Directory cannot be null");
    }

    @Test
    void validateDirectory_withNonExistentDirectory_shouldThrowSecurityException(@TempDir Path tempDir) {
        File nonExistentDir = tempDir.resolve("non-existent").toFile();

        assertThatThrownBy(() -> securityService.validateDirectory(nonExistentDir))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Directory does not exist");
    }

    @Test
    void validateDirectory_withFile_shouldThrowSecurityException(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("file.txt").toFile();
        Files.createFile(file.toPath());

        assertThatThrownBy(() -> securityService.validateDirectory(file))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Path is not a directory");
    }

    @Test
    void multipleBlockedExtensions_shouldAllBeRejected(@TempDir Path tempDir) throws Exception {
        String[] blockedExtensions = {"exe", "bat", "sh", "cmd"};
        
        for (String ext : blockedExtensions) {
            File blockedFile = tempDir.resolve("test." + ext).toFile();
            Files.createFile(blockedFile.toPath());

            assertThatThrownBy(() -> securityService.validateFileAccess(blockedFile))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("File extension not allowed: " + ext);
        }
    }

    @Test
    void allowedExtensions_shouldPass(@TempDir Path tempDir) throws Exception {
        String[] allowedExtensions = {"txt", "docx", "odt", "md", "pdf"};
        
        for (String ext : allowedExtensions) {
            File allowedFile = tempDir.resolve("test." + ext).toFile();
            Files.createFile(allowedFile.toPath());

            assertThatCode(() -> securityService.validateFileAccess(allowedFile))
                    .doesNotThrowAnyException();
        }
    }
}
