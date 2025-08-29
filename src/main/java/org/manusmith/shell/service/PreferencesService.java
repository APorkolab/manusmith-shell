package org.manusmith.shell.service;

import org.manusmith.shell.dto.AuthorMeta;

import java.util.prefs.Preferences;

public class PreferencesService {

    private final Preferences prefs;

    // Preference keys
    private static final String AUTHOR = "author";
    private static final String ADDRESS = "address";
    private static final String EMAIL = "email";
    private static final String PHONE = "phone";
    private static final String TITLE = "title"; // Note: Title might not be ideal to save, but let's include it.

    public PreferencesService() {
        // Using a node specific to this application class
        this.prefs = Preferences.userNodeForPackage(PreferencesService.class);
    }

    public void saveAuthorMeta(AuthorMeta meta) {
        if (meta == null) return;
        prefs.put(AUTHOR, meta.author());
        prefs.put(ADDRESS, meta.address());
        prefs.put(EMAIL, meta.email());
        prefs.put(PHONE, meta.phone());
        // We probably don't want to save the title and word count as they are manuscript-specific.
        // prefs.put(TITLE, meta.title());
    }

    public AuthorMeta loadAuthorMeta() {
        String author = prefs.get(AUTHOR, "");
        String address = prefs.get(ADDRESS, "");
        String email = prefs.get(EMAIL, "");
        String phone = prefs.get(PHONE, "");
        // Return null if no author is saved, indicating no preferences exist yet.
        if (author.isEmpty()) {
            return null;
        }
        // Create an AuthorMeta object with empty title and words, as they are not saved.
        return new AuthorMeta(author, address, email, phone, "", "");
    }
}
