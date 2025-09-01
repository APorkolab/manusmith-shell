package org.manusmith.shell.service;

import org.manusmith.shell.dto.ConvertRequest;

import java.io.IOException;

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
        // This is a simplified implementation for .txt to .docx conversion.
        // A real engine would handle multiple formats.
        System.out.println("Converting " + inputFile.getName() + " to " + outputFile.getName());

        if (!inputFile.getName().toLowerCase().endsWith(".txt")) {
            throw new IOException("QuickConvert currently only supports .txt files.");
        }

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
}
