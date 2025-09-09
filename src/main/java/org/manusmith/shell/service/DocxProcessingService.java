package org.manusmith.shell.service;

import org.apache.poi.xwpf.usermodel.*;
import org.manusmith.shell.dto.ConvertRequest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class DocxProcessingService {

    public void processDocument(ConvertRequest request) throws IOException {
        try (FileInputStream fis = new FileInputStream(request.inputFile());
             XWPFDocument document = new XWPFDocument(fis)) {

            if (request.formattingPrefs() != null && request.formattingPrefs().italicToUnderline()) {
                System.out.println("Applying italic-to-underline conversion...");
                convertItalicToUnderline(document);
            }

            try (FileOutputStream fos = new FileOutputStream(request.outputFile())) {
                document.write(fos);
            }
        }
    }

    private void convertItalicToUnderline(XWPFDocument document) {
        for (XWPFParagraph p : document.getParagraphs()) {
            // This is tricky because we can't modify the list of runs while iterating.
            // A common approach is to get the runs, and then work with indexes.
            for (int i = p.getRuns().size() - 1; i >= 0; i--) {
                XWPFRun run = p.getRuns().get(i);
                if (run.isItalic()) {
                    String text = run.getText(0);
                    if (text == null || text.isEmpty()) {
                        continue;
                    }

                    // Copy properties BEFORE removing the run to avoid XML disconnect
                    String fontFamily = null;
                    try {
                        fontFamily = run.getFontFamily();
                    } catch (Exception e) {
                        // Ignore if font family can't be retrieved
                    }
                    
                    int fontSize = -1;
                    try {
                        fontSize = run.getFontSize();
                    } catch (Exception e) {
                        // Ignore if font size can't be retrieved
                    }
                    
                    boolean isBold = false;
                    try {
                        isBold = run.isBold();
                    } catch (Exception e) {
                        // Ignore if bold status can't be retrieved
                    }

                    // Remove the old run
                    p.removeRun(i);

                    // Create a new run with the same text and properties, but underlined
                    XWPFRun newRun = p.insertNewRun(i);
                    newRun.setText(text);
                    newRun.setUnderline(UnderlinePatterns.SINGLE);

                    // Copy other properties
                    if (fontFamily != null) {
                        newRun.setFontFamily(fontFamily);
                    }
                    if (fontSize != -1) {
                       newRun.setFontSize(fontSize);
                    }
                    newRun.setBold(isBold);
                    // Not setting italic, as we are replacing it.
                }
            }
        }
    }
}
