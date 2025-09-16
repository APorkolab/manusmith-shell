package manual;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Creates test documents for testing the application
 */
public class CreateTestDocuments {
    public static void main(String[] args) throws IOException {
        // Create a simple DOCX file
        XWPFDocument document = new XWPFDocument();
        
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("Ez egy teszt kézirat \"idézőjelekkel\" és 'aposztrófokkal'... Itt van egy -- gondolatjel is.");
        
        paragraph = document.createParagraph();
        run = paragraph.createRun();
        run.setText("");
        
        paragraph = document.createParagraph();
        run = paragraph.createRun();
        run.setText("Ez egy új bekezdés dőlt szöveggel: ");
        
        run = paragraph.createRun();
        run.setText("ez dőlt");
        run.setItalic(true);
        
        run = paragraph.createRun();
        run.setText(" és ez nem.");
        
        paragraph = document.createParagraph();
        run = paragraph.createRun();
        run.setText("***");
        
        paragraph = document.createParagraph();
        run = paragraph.createRun();
        run.setText("Ez egy új jelenet.");
        
        paragraph = document.createParagraph();
        run = paragraph.createRun();
        run.setText("---");
        
        paragraph = document.createParagraph();
        run = paragraph.createRun();
        run.setText("És ez is egy másik jelenet.");
        
        try (FileOutputStream out = new FileOutputStream("test-manuscript.docx")) {
            document.write(out);
        }
        document.close();
        
        // Create ODT test content (simplified)
        String odtContent = "Ez egy ODT teszt fájl tartalma.\\n\\nKét bekezdéssel.";
        Files.writeString(Paths.get("test-document.odt"), odtContent);
        
        System.out.println("Test documents created:");
        System.out.println("- test-manuscript.docx");
        System.out.println("- test-document.odt");
        System.out.println("- test-typofix.txt (already exists)");
        System.out.println("- README.md (already exists)");
    }
}