package org.manusmith.shell.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.manusmith.shell.dto.AuthorMeta;
import org.manusmith.shell.dto.ConvertRequest;
import org.manusmith.shell.dto.FormattingPrefs;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceTest {

    private ValidationService validationService;
    private File validFile;
    private ResourceBundle messages;

    @BeforeEach
    void setUp(@TempDir File tempDir) throws IOException {
        validationService = new ValidationService();
        validFile = new File(tempDir, "input.docx");
        assertTrue(validFile.createNewFile(), "Failed to create temp file for test");
        messages = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
    }

    private ConvertRequest createValidRequest() {
        AuthorMeta meta = new AuthorMeta("John Doe", "123 Street", "a@b.com", "555", "My Title", "123");
        FormattingPrefs prefs = new FormattingPrefs(false);
        return new ConvertRequest(validFile, new File("output.docx"), meta, prefs);
    }

    @Test
    void validate_withValidRequest_shouldReturnNoErrors() {
        ConvertRequest request = createValidRequest();
        List<String> errors = validationService.validate(request);
        assertTrue(errors.isEmpty(), "A valid request should not produce errors.");
    }

    @Test
    void validate_withMissingInputFile_shouldReturnError() {
        ConvertRequest request = new ConvertRequest(new File("nonexistent.docx"), new File("out.docx"), createValidRequest().authorMeta(), createValidRequest().formattingPrefs());
        List<String> errors = validationService.validate(request);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains(messages.getString("validation.input_file_missing").substring(0, 10)));
    }

    @Test
    void validate_withMissingAuthor_shouldReturnError() {
        AuthorMeta meta = new AuthorMeta("", "123 Street", "a@b.com", "555", "My Title", "123");
        ConvertRequest request = new ConvertRequest(validFile, new File("out.docx"), meta, createValidRequest().formattingPrefs());
        List<String> errors = validationService.validate(request);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains(messages.getString("validation.author_name_required").substring(0, 10)));
    }

    @Test
    void validate_withMissingTitle_shouldReturnError() {
        AuthorMeta meta = new AuthorMeta("John Doe", "123 Street", "a@b.com", "555", "", "123");
        ConvertRequest request = new ConvertRequest(validFile, new File("out.docx"), meta, createValidRequest().formattingPrefs());
        List<String> errors = validationService.validate(request);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains(messages.getString("validation.manuscript_title_required").substring(0, 10)));
    }

    @Test
    void validate_withMultipleErrors_shouldReturnAllErrors() {
        AuthorMeta meta = new AuthorMeta("", "123 Street", "a@b.com", "555", "", "123");
        ConvertRequest request = new ConvertRequest(new File("nonexistent.docx"), null, meta, createValidRequest().formattingPrefs());
        List<String> errors = validationService.validate(request);
        assertEquals(4, errors.size(), "Should detect missing input file, output file, author, and title.");
    }
}
