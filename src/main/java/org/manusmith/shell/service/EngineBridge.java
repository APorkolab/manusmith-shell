package org.manusmith.shell.service;

import org.manusmith.shell.dto.ConvertRequest;

public class EngineBridge {

    public void process(ConvertRequest request) {
        // This will later call the actual typokit-engine
        System.out.println("Simulating engine processing for: " + request);
    }

    public String cleanText(String text) {
        // This will later call the actual typokit-engine's text cleaning functions
        System.out.println("Simulating text cleaning for: " + text.substring(0, Math.min(text.length(), 50)) + "...");
        return text.trim().replaceAll("\\s+", " "); // Simple cleaning: trim and collapse whitespace
    }
}
