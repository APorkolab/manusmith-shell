package org.manusmith.shell.service;

import org.manusmith.shell.dto.AuthorMeta;
import org.manusmith.shell.dto.ConvertRequest;
import org.manusmith.shell.util.Strings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ValidationService {

    public List<String> validate(ConvertRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.inputFile() == null || !request.inputFile().exists()) {
            errors.add("Input file does not exist or was not selected.");
        } else if (!request.inputFile().canRead()) {
            errors.add("Cannot read the selected input file.");
        }

        if (request.outputFile() == null) {
            errors.add("Output file was not specified.");
        } else if (request.outputFile().exists() && !request.outputFile().canWrite()) {
            errors.add("Cannot write to the selected output file location.");
        }

        AuthorMeta meta = request.authorMeta();
        if (meta == null) {
            errors.add("Author metadata is missing.");
        } else {
            if (Strings.isBlank(meta.author())) {
                errors.add("Author name is required.");
            }
            if (Strings.isBlank(meta.title())) {
                errors.add("Manuscript title is required.");
            }
        }

        return errors;
    }
}
