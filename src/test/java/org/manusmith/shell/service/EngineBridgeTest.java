package org.manusmith.shell.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.manusmith.shell.dto.AuthorMeta;
import org.manusmith.shell.dto.ConvertRequest;
import org.manusmith.shell.dto.FormattingPrefs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for EngineBridge
 */
class EngineBridgeTest {

    private EngineBridge engineBridge;

    @BeforeEach
    void setUp() {
        // Reset singletons for clean tests
        ConfigurationService.instance = null;
        MetricsService.instance = null;
        this.engineBridge = new EngineBridge();
    }

    @Test
    void process_withNullRequest_shouldThrowException() {
        assertThatThrownBy(() -> engineBridge.process(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("ConvertRequest cannot be null");
    }

    @Test
    void process_withValidRequest_shouldProcessSuccessfully(@TempDir Path tempDir) throws Exception {
        // Create test files
        File inputFile = createTestDocxFile(tempDir, "input.docx");
        File outputFile = tempDir.resolve("output.docx").toFile();
        
        // Create valid request
        AuthorMeta meta = new AuthorMeta("Test Author", "123 Street", "test@example.com", 
                "555-0123", "Test Title", "1000");
        FormattingPrefs prefs = new FormattingPrefs(true);
        ConvertRequest request = new ConvertRequest(inputFile, outputFile, meta, prefs);

        // Process should not throw exception
        assertThatCode(() -> engineBridge.process(request))
                .doesNotThrowAnyException();
    }

    @Test 
    void processAsync_withValidRequest_shouldCompleteSuccessfully(@TempDir Path tempDir) throws Exception {
        File inputFile = createTestDocxFile(tempDir, "input.docx");
        File outputFile = tempDir.resolve("output.docx").toFile();
        
        AuthorMeta meta = new AuthorMeta("Test Author", "123 Street", "test@example.com", 
                "555-0123", "Test Title", "1000");
        FormattingPrefs prefs = new FormattingPrefs(false);
        ConvertRequest request = new ConvertRequest(inputFile, outputFile, meta, prefs);

        CompletableFuture<Void> future = engineBridge.processAsync(request);
        
        assertThat(future).isNotNull();
        assertThatCode(() -> future.get()).doesNotThrowAnyException();
    }

    @Test
    void cleanText_withNullText_shouldReturnNull() {
        String result = engineBridge.cleanText(null, "EN");
        
        assertThat(result).isNull();
    }

    @Test
    void cleanText_withNullProfile_shouldReturnOriginalText() {
        String originalText = "Test text";
        
        String result = engineBridge.cleanText(originalText, null);
        
        assertThat(result).isEqualTo(originalText);
    }

    @Test
    void cleanText_withEnglishProfile_shouldApplyEnglishRules() {
        String inputText = "This has \"quotes\" and 'inner quotes' and -- dashes.";
        
        String result = engineBridge.cleanText(inputText, "EN");
        
        // Check that typographic quotes are applied
        assertThat(result).contains("\u201c"); // left double quote  
        assertThat(result).contains("\u201d"); // right double quote
        assertThat(result).contains("—"); // em-dash
    }

    @Test
    void cleanText_withHungarianProfile_shouldApplyHungarianRules() {
        String inputText = "This has \"quotes\" and 'inner quotes' and — dashes.";
        
        String result = engineBridge.cleanText(inputText, "HU");
        
        assertThat(result).contains("\u201e"); // double low-9 quotation mark
        assertThat(result).contains("\u201d"); // left double quotation mark
        assertThat(result).contains("\u00bb"); // right-pointing double angle quotation mark
        assertThat(result).contains("\u00ab"); // left-pointing double angle quotation mark
        assertThat(result).contains("–"); // en-dash
    }

    @Test
    void cleanText_withGermanProfile_shouldApplyGermanRules() {
        String inputText = "This has \"quotes\" and 'inner quotes' and — dashes.";
        
        String result = engineBridge.cleanText(inputText, "DE");
        
        assertThat(result).contains("\u201e"); // double low-9 quotation mark
        assertThat(result).contains("\u201c"); // left double quotation mark  
        assertThat(result).contains("\u201a"); // single low-9 quotation mark
        assertThat(result).contains("\u2018"); // left single quotation mark
        assertThat(result).contains("–"); // en-dash
    }

    @Test
    void cleanText_withShunnProfile_shouldApplyShunnRules() {
        String inputText = "Scene break:\n***\nAnother break:\n---\nNormal text.";
        
        String result = engineBridge.cleanText(inputText, "Shunn");
        
        assertThat(result).contains(" # ");
        assertThat(result).doesNotContain("***");
        assertThat(result).doesNotContain("---");
    }

    @Test
    void cleanText_withUnknownProfile_shouldReturnTextWithGeneralFixes() {
        String inputText = "Text with -- dashes and ... ellipsis.";
        
        String result = engineBridge.cleanText(inputText, "UNKNOWN");
        
        assertThat(result).contains("—"); // em-dash should be applied
        assertThat(result).contains("…"); // ellipsis should be applied
    }

    @Test
    void quickConvert_withNullInputFile_shouldThrowException() {
        File outputFile = new File("output.txt");
        
        assertThatThrownBy(() -> engineBridge.quickConvert(null, outputFile))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Input file cannot be null");
    }

    @Test
    void quickConvert_withNullOutputFile_shouldThrowException(@TempDir Path tempDir) throws Exception {
        File inputFile = createTestTxtFile(tempDir, "input.txt");
        
        assertThatThrownBy(() -> engineBridge.quickConvert(inputFile, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Output file cannot be null");
    }

    @Test
    void quickConvert_txtToDocx_shouldConvertSuccessfully(@TempDir Path tempDir) throws Exception {
        File inputFile = createTestTxtFile(tempDir, "input.txt");
        File outputFile = tempDir.resolve("output.docx").toFile();
        
        assertThatCode(() -> engineBridge.quickConvert(inputFile, outputFile))
                .doesNotThrowAnyException();
        
        assertThat(outputFile.exists()).isTrue();
        assertThat(outputFile.length()).isGreaterThan(0);
    }

    @Test
    void quickConvert_docxToTxt_shouldConvertSuccessfully(@TempDir Path tempDir) throws Exception {
        File inputFile = createTestDocxFile(tempDir, "input.docx");
        File outputFile = tempDir.resolve("output.txt").toFile();
        
        assertThatCode(() -> engineBridge.quickConvert(inputFile, outputFile))
                .doesNotThrowAnyException();
        
        assertThat(outputFile.exists()).isTrue();
        assertThat(outputFile.length()).isGreaterThan(0);
    }

    @Test
    void quickConvert_mdToTxt_shouldConvertSuccessfully(@TempDir Path tempDir) throws Exception {
        File inputFile = createTestMarkdownFile(tempDir, "input.md");
        File outputFile = tempDir.resolve("output.txt").toFile();
        
        assertThatCode(() -> engineBridge.quickConvert(inputFile, outputFile))
                .doesNotThrowAnyException();
        
        assertThat(outputFile.exists()).isTrue();
    }

    @Test
    void quickConvert_odtToTxt_shouldConvertSuccessfully(@TempDir Path tempDir) throws Exception {
        File inputFile = createTestOdtFile(tempDir, "input.odt");
        File outputFile = tempDir.resolve("output.txt").toFile();
        
        assertThatCode(() -> engineBridge.quickConvert(inputFile, outputFile))
                .doesNotThrowAnyException();
        
        assertThat(outputFile.exists()).isTrue();
    }

    @Test
    void quickConvert_withUnsupportedConversion_shouldThrowIOException(@TempDir Path tempDir) throws Exception {
        File inputFile = createTestTxtFile(tempDir, "input.txt");
        File outputFile = tempDir.resolve("output.pdf").toFile();
        
        assertThatThrownBy(() -> engineBridge.quickConvert(inputFile, outputFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Unsupported conversion");
    }

    // Helper methods for creating test files

    private File createTestTxtFile(Path tempDir, String filename) throws IOException {
        File txtFile = tempDir.resolve(filename).toFile();
        String content = "This is a test document.\n\nIt has multiple paragraphs.\n\nEnd of document.";
        Files.write(txtFile.toPath(), content.getBytes());
        return txtFile;
    }

    private File createTestDocxFile(Path tempDir, String filename) throws IOException {
        File docxFile = tempDir.resolve(filename).toFile();
        
        // Create a minimal DOCX file using Apache POI
        try (org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument()) {
            org.apache.poi.xwpf.usermodel.XWPFParagraph paragraph = document.createParagraph();
            org.apache.poi.xwpf.usermodel.XWPFRun run = paragraph.createRun();
            run.setText("Test document content with italic text.");
            run.setItalic(true);
            
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(docxFile)) {
                document.write(fos);
            }
        }
        
        return docxFile;
    }

    private File createTestMarkdownFile(Path tempDir, String filename) throws IOException {
        File mdFile = tempDir.resolve(filename).toFile();
        String content = "# Test Document\n\nThis is **bold** and *italic* text.\n\n## Section\n\nSome content.";
        Files.write(mdFile.toPath(), content.getBytes());
        return mdFile;
    }

    private File createTestOdtFile(Path tempDir, String filename) throws IOException {
        File odtFile = tempDir.resolve(filename).toFile();
        
        try (org.odftoolkit.odfdom.doc.OdfTextDocument document = org.odftoolkit.odfdom.doc.OdfTextDocument.newTextDocument()) {
            document.newParagraph("Test ODT document content.");
            document.save(odtFile);
        } catch (Exception e) {
            // If ODT creation fails, create a dummy file for testing
            Files.write(odtFile.toPath(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
        }
        
        return odtFile;
    }
}
