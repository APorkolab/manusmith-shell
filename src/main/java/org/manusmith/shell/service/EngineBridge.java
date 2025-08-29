package org.manusmith.shell.service;

import org.manusmith.shell.dto.ConvertRequest;

public class EngineBridge {

    public void process(ConvertRequest request) {
        // This will later call the actual typokit-engine
        System.out.println("Simulating engine processing for: " + request);
    }

    public String cleanText(String text, String profile) {
        // This will later call the actual typokit-engine's text cleaning functions
        System.out.println("Simulating text cleaning for: " + text.substring(0, Math.min(text.length(), 50)) + "..." + " with profile: " + profile);
        if (profile == null) {
            return text;
        }
        switch (profile) {
            case "HU":
                return text.toUpperCase();
            case "EN":
                return text.toLowerCase();
            case "DE":
                return new StringBuilder(text).reverse().toString();
            default:
                return text;
        }
    }

    public void quickConvert(java.io.File inputFile, java.io.File outputFile) throws java.io.IOException {
        // In a real scenario, this would call the engine to do a complex conversion.
        // For simulation, we'll just copy the file to the output location.
        System.out.println("Simulating quick conversion from " + inputFile.getName() + " to " + outputFile.getName());
        java.nio.file.Files.copy(inputFile.toPath(), outputFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
}
