package org.manusmith.shell.dto;

import java.io.File;

/**
 * A comprehensive request object for the conversion process.
 */
public record ConvertRequest(
    File inputFile,
    File outputFile,
    AuthorMeta authorMeta,
    FormattingPrefs formattingPrefs
) {}
