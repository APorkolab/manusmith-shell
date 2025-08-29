package org.manusmith.shell.service;

import org.manusmith.shell.dto.ConvertRequest;

public class EngineBridge {

    public void process(ConvertRequest request) {
        // This will later call the actual typokit-engine
        System.out.println("Simulating engine processing for: " + request);
    }
}
