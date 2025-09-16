package manual;

import org.manusmith.shell.service.EngineBridge;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Manual test to debug TypoFix functionality
 */
public class TypoFixDebugTest {
    public static void main(String[] args) throws Exception {
        EngineBridge engineBridge = new EngineBridge();
        
        // Test text from our test file
        String testText = Files.readString(Paths.get("test-typofix.txt"));
        
        System.out.println("=== ORIGINAL TEXT ===");
        System.out.println(testText);
        System.out.println("\n=== HUNGARIAN PROFILE ===");
        String hunResult = engineBridge.cleanText(testText, "HU");
        System.out.println(hunResult);
        
        System.out.println("\n=== ENGLISH PROFILE ===");
        String enResult = engineBridge.cleanText(testText, "EN");
        System.out.println(enResult);
        
        System.out.println("\n=== GERMAN PROFILE ===");
        String deResult = engineBridge.cleanText(testText, "DE");
        System.out.println(deResult);
        
        System.out.println("\n=== SHUNN PROFILE ===");
        String shunnResult = engineBridge.cleanText(testText, "Shunn");
        System.out.println(shunnResult);
        
        // Test specific patterns
        System.out.println("\n=== PATTERN TESTS ===");
        testPattern(engineBridge, "Simple \"quotes\" test", "HU");
        testPattern(engineBridge, "Line with ***", "Shunn");
        testPattern(engineBridge, "Line with ---", "Shunn");
        testPattern(engineBridge, "Ellipsis test...", "EN");
        testPattern(engineBridge, "Em-dash test --", "EN");
    }
    
    private static void testPattern(EngineBridge bridge, String input, String profile) {
        String result = bridge.cleanText(input, profile);
        System.out.println(String.format("Input:  '%s'", input));
        System.out.println(String.format("Output: '%s' [%s]", result, profile));
        System.out.println();
    }
}