package org.manusmith.shell.service;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.util.io.IndependentCharSequence;
import org.manusmith.shell.dto.ConvertRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class EngineBridge {

    private final DocxProcessingService docxProcessingService = new DocxProcessingService();

    public void process(ConvertRequest request) throws IOException {
        docxProcessingService.processDocument(request);
    }

    public String cleanText(String text, String profile) {
        if (text == null || profile == null) {
            return text;
        }

        System.out.println("Cleaning text with profile: " + profile);

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
                // No profile or unknown profile, return original text
                return text;
        }
        return text;
    }

    public void quickConvert(java.io.File inputFile, java.io.File outputFile) throws java.io.IOException {
        String inputName = inputFile.getName().toLowerCase();
        String outputName = outputFile.getName().toLowerCase();

        System.out.println("Quick Converting " + inputName + " to " + outputName);

        if (inputName.endsWith(".txt") && outputName.endsWith(".docx")) {
            convertTxtToDocx(inputFile, outputFile);
        } else if (inputName.endsWith(".docx") && outputName.endsWith(".txt")) {
            convertDocxToTxt(inputFile, outputFile);
        } else if (inputName.endsWith(".md") && outputName.endsWith(".txt")) {
            convertMdToTxt(inputFile, outputFile);
        } else {
            throw new IOException("Unsupported conversion: from " + inputName + " to " + outputName);
        }
    }

    private void convertTxtToDocx(java.io.File inputFile, java.io.File outputFile) throws java.io.IOException {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(inputFile));
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
             java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(outputFile))) {

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
}
