package org.manusmith.shell.dto;

/**
 * A data carrier for author metadata, using a Java Record for immutability and conciseness.
 */
public record AuthorMeta(
    String author,
    String address,
    String email,
    String phone,
    String title,
    String words
) {}
