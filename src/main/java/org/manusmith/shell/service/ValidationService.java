package org.manusmith.shell.service;

import org.manusmith.shell.dto.AuthorMeta;
import org.manusmith.shell.dto.ConvertRequest;
import org.manusmith.shell.util.Strings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ValidationService {
    
    private final ResourceBundle messages;
    
    public ValidationService() {
        this.messages = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
    }

    public List<String> validate(ConvertRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.inputFile() == null || !request.inputFile().exists()) {
            errors.add(messages.getString("validation.input_file_missing"));
        } else if (!request.inputFile().canRead()) {
            errors.add(messages.getString("validation.input_file_not_readable"));
        }

        if (request.outputFile() == null) {
            errors.add(messages.getString("validation.output_file_missing"));
        } else if (request.outputFile().exists() && !request.outputFile().canWrite()) {
            errors.add(messages.getString("validation.output_file_not_writable"));
        }

        AuthorMeta meta = request.authorMeta();
        if (meta == null) {
            errors.add(messages.getString("validation.author_metadata_missing"));
        } else {
            if (Strings.isBlank(meta.author())) {
                errors.add(messages.getString("validation.author_name_required"));
            }
            if (Strings.isBlank(meta.title())) {
                errors.add(messages.getString("validation.manuscript_title_required"));
            }
        }

        return errors;
    }
}
