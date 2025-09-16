package org.manusmith.shell.service;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import io.micrometer.core.instrument.Timer;
import org.manusmith.shell.dto.ConvertRequest;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enterprise-grade document processing bridge with monitoring, logging, and async capabilities.
 * Provides high-level document processing operations with comprehensive error handling and metrics.
 */
public class EngineBridge {
    private static final Logger logger = LoggerFactory.getLogger(EngineBridge.class);
    
    private final DocxProcessingService docxProcessingService;
    private final MetricsService metricsService;
    private final ConfigurationService configurationService;
    private final ExecutorService executorService;
    private final SecurityService securityService;
    
    public EngineBridge() {
        this.docxProcessingService = new DocxProcessingService();
        this.metricsService = MetricsService.getInstance();
        this.configurationService = ConfigurationService.getInstance();
        this.securityService = new SecurityService();
        
        int threadPoolSize = configurationService.getPerformanceConfig().getThreadPoolSize();
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        
        logger.info("EngineBridge initialized with thread pool size: {}", threadPoolSize);
    }

    /**
     * Processes a document conversion request synchronously.
     * 
     * @param request The conversion request containing input/output files and metadata
     * @throws IOException If file processing fails
     * @throws IllegalArgumentException If request validation fails
     */
    public void process(ConvertRequest request) throws IOException {
        Objects.requireNonNull(request, "ConvertRequest cannot be null");
        
        // Security validation
        securityService.validateFileAccess(request.inputFile());
        securityService.validateFileAccess(request.outputFile().getParentFile());
        
        // Metrics recording
        metricsService.recordOperationStarted();
        Timer.Sample sample = metricsService.startTimer();
        
        Instant startTime = Instant.now();
        String documentType = getFileExtension(request.inputFile().getName());
        
        try {
            logger.info("Starting document processing: {} -> {}", 
                    request.inputFile().getName(), request.outputFile().getName());
            
            docxProcessingService.processDocument(request);
            
            Duration processingTime = Duration.between(startTime, Instant.now());
            long fileSize = request.inputFile().length();
            
            metricsService.recordDocumentProcessed(documentType, fileSize, processingTime);
            logger.info("Document processing completed successfully in {}ms", processingTime.toMillis());
            
        } catch (Exception e) {
            logger.error("Document processing failed for file: {}", request.inputFile().getName(), e);
            metricsService.recordDocumentProcessingError(documentType, "processing_error", e);
            throw e;
        } finally {
            metricsService.stopTimer(sample, "document.processing.total");
            metricsService.recordOperationCompleted();
        }
    }
    
    /**
     * Processes a document conversion request asynchronously.
     * 
     * @param request The conversion request
     * @return CompletableFuture that completes when processing is done
     */
    public CompletableFuture<Void> processAsync(ConvertRequest request) {
        if (!configurationService.getPerformanceConfig().isAsyncProcessing()) {
            // Fall back to synchronous processing
            return CompletableFuture.runAsync(() -> {
                try {
                    process(request);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                process(request);
            } catch (IOException e) {
                throw new RuntimeException("Async processing failed", e);
            }
        }, executorService);
    }
    
    /**
     * Extracts file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unknown";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase(java.util.Locale.ROOT);
        }
        
        return "unknown";
    }

    /**
     * Cleans and normalizes text according to the specified typography profile.
     * 
     * @param text The text to clean
     * @param profile The typography profile (HU, DE, EN, Shunn)
     * @return The cleaned text
     */
    public String cleanText(String text, String profile) {
        if (text == null || profile == null) {
            logger.warn("Null text or profile provided for cleaning: text={}, profile={}", 
                    text != null ? "[" + text.length() + " chars]" : "null", profile);
            return text;
        }

        logger.info("Cleaning text with profile: {} (length: {} chars)", profile, text.length());
        Timer.Sample sample = metricsService.startTimer();
        
        try {
            // General typography fixes
            text = text.replaceAll("(?<=\\w)-{2,}(?=\\w)", "—"); // unspaced em-dash
            text = text.replaceAll("(?<=\\s)-{2,}(?=\\s)", " — "); // spaced em-dash
            text = text.replaceAll("\\.\\.\\.", "…");

            switch (profile) {
            case "HU":
                // Hungarian: „low-high” quotes, »guillemets« for inner, spaced en-dash for thoughts
                text = text.replaceAll(" \"([^\"]*)\"", " „$1”");
                text = text.replaceAll(" '([^']*)'", " »$1«");
                text = text.replaceAll(" — ", " – ");
                break;
            case "DE":
                // German: „low-high“ quotes, ‚low-high‘ for inner, spaced en-dash for thoughts
                text = text.replaceAll(" \"([^\"]*)\"", " „$1“");
                text = text.replaceAll(" '([^']*)'", " ‚$1‘");
                text = text.replaceAll(" — ", " – ");
                break;
            case "EN":
                // English: “high-high” quotes, ‘high-high’ for inner, unspaced em-dash for breaks
                text = text.replaceAll(" \"([^\"]*)\"", " “$1”");
                text = text.replaceAll(" '([^']*)'", " ‘$1’");
                text = text.replaceAll(" – ", "—");
                break;
            case "Shunn":
                // Shunn manuscript format (text-level): standardize scene breaks
                // Replace lines with *** or --- with a centered #
                text = text.replaceAll("(?m)^\\s*\\*\\*\\*\\s*$", " # ");
                text = text.replaceAll("(?m)^\\s*---\\s*$", " # ");
                break;
            default:
                logger.warn("Unknown text cleaning profile: {}", profile);
                // Return text with only general typography fixes applied
                break;
            }
        
            logger.debug("Text cleaned with profile: {}", profile);
            metricsService.recordCounter("text.cleaning.completed", "profile", profile);
            return text;
            
        } catch (Exception e) {
            logger.error("Error during text cleaning with profile: {}", profile, e);
            metricsService.recordCounter("text.cleaning.error", "profile", profile, 
                    "error", e.getClass().getSimpleName());
            throw new RuntimeException("Text cleaning failed", e);
        } finally {
            metricsService.stopTimer(sample, "text.cleaning.time");
        }
    }

    /**
     * Performs a quick file format conversion between supported document types.
     * 
     * @param inputFile The source file
     * @param outputFile The target file
     * @throws IOException If conversion fails
     */
    public void quickConvert(java.io.File inputFile, java.io.File outputFile) throws java.io.IOException {
        Objects.requireNonNull(inputFile, "Input file cannot be null");
        Objects.requireNonNull(outputFile, "Output file cannot be null");
        
        // Security validation
        securityService.validateFileAccess(inputFile);
        securityService.validateFileSize(inputFile);
        securityService.validateFileAccess(outputFile.getParentFile());
        
        String inputName = inputFile.getName().toLowerCase(java.util.Locale.ROOT);
        String outputName = outputFile.getName().toLowerCase(java.util.Locale.ROOT);
        
        logger.info("Quick converting {} to {}", inputName, outputName);
        
        // Metrics recording
        metricsService.recordOperationStarted();
        Timer.Sample sample = metricsService.startTimer();
        Instant startTime = Instant.now();
        
        try {

            if (inputName.endsWith(".txt") && outputName.endsWith(".docx")) {
                convertTxtToDocx(inputFile, outputFile);
            } else if (inputName.endsWith(".docx") && outputName.endsWith(".txt")) {
                convertDocxToTxt(inputFile, outputFile);
            } else if (inputName.endsWith(".md") && outputName.endsWith(".txt")) {
                convertMdToTxt(inputFile, outputFile);
            } else if (inputName.endsWith(".odt") && outputName.endsWith(".txt")) {
                convertOdtToTxt(inputFile, outputFile);
            } else {
                throw new IOException("Unsupported conversion: from " + inputName + " to " + outputName);
            }
            
            Duration processingTime = Duration.between(startTime, Instant.now());
            long fileSize = inputFile.length();
            String conversionType = getFileExtension(inputName) + "_to_" + getFileExtension(outputName);
            
            metricsService.recordDocumentProcessed(conversionType, fileSize, processingTime);
            logger.info("Quick conversion completed successfully in {}ms", processingTime.toMillis());
            
        } catch (Exception e) {
            logger.error("Quick conversion failed: {} -> {}", inputName, outputName, e);
            String conversionType = getFileExtension(inputName) + "_to_" + getFileExtension(outputName);
            metricsService.recordDocumentProcessingError(conversionType, "conversion_error", e);
            throw e;
        } finally {
            metricsService.stopTimer(sample, "document.quick_conversion.time");
            metricsService.recordOperationCompleted();
        }
    }

    private void convertTxtToDocx(java.io.File inputFile, java.io.File outputFile) throws java.io.IOException {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(inputFile), java.nio.charset.StandardCharsets.UTF_8));
             org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument()) {
            String line;
            while ((line = reader.readLine()) != null) {
                document.createParagraph().createRun().setText(line);
            }
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFile)) {
                document.write(fos);
            }
        }
    }

    private void convertDocxToTxt(java.io.File inputFile, java.io.File outputFile) throws java.io.IOException {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(inputFile);
             org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument(fis);
             java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(outputFile), java.nio.charset.StandardCharsets.UTF_8))) {

            org.apache.poi.xwpf.extractor.XWPFWordExtractor extractor = new org.apache.poi.xwpf.extractor.XWPFWordExtractor(document);
            writer.write(extractor.getText());
        }
    }

    private void convertMdToTxt(java.io.File inputFile, java.io.File outputFile) throws java.io.IOException {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        com.vladsch.flexmark.util.ast.TextCollectingVisitor textVisitor = new com.vladsch.flexmark.util.ast.TextCollectingVisitor();

        String markdownContent = Files.readString(inputFile.toPath());
        Node document = parser.parse(markdownContent);
        String plainText = textVisitor.collectAndGetText(document);

        Files.writeString(outputFile.toPath(), plainText);
    }

    private void convertOdtToTxt(java.io.File inputFile, java.io.File outputFile) throws java.io.IOException {
        try (OdfTextDocument doc = OdfTextDocument.loadDocument(inputFile);
             java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(outputFile), java.nio.charset.StandardCharsets.UTF_8))) {

            // Extract text content using the document's text iterator
            StringBuilder sb = new StringBuilder();
            org.w3c.dom.NodeList paragraphs = doc.getContentDom().getElementsByTagName("text:p");
            for (int i = 0; i < paragraphs.getLength(); i++) {
                org.w3c.dom.Node p = paragraphs.item(i);
                if (p.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    sb.append(p.getTextContent()).append("\n");
                }
            }
            writer.write(sb.toString());
        } catch (Exception e) {
            // Wrap the generic exception from ODF Toolkit into an IOException
            throw new java.io.IOException("Failed to process ODT file: " + e.getMessage(), e);
        }
    }
}
