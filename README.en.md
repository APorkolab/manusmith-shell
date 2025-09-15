# ManuSmith Shell

## 🎯 Main Goal

A comfortable, simple **UI/CLI** that hides the engine details and completes tasks with just 1-2 clicks.

## ✅ Status

| Feature | Status | Notes |
| :--- | :--- | :--- |
| **MVP** (Shunn formatting) | Implemented | The `italic → underline` conversion works. |
| **Cover Letter** | Implemented | The cover letter generator works. |
| **TypoKit** | Implemented | Basic typographic fixes (HU/DE/EN) and scene break normalization implemented. |
| **QuickConvert** | Fully implemented | Supported conversions: `.txt`↔`.docx`, `.md`→`.txt`, `.odt`↔`.txt`, `.odt`↔`.docx`. |
| **CrossClip Lite** | Implemented | System tray icon, theme switcher, and clipboard cleaning functionality. |

---

## Minimum Viable Product (MVP)

1. **File Selection**
   * Select `.docx` input with `FileChooser` or drag&drop.
   * User sees the name of the selected file.

2. **Metadata Input**
   * Form: author name, title, email, phone, work title.
   * Word count → automatically calculated by the engine, but the field can be overridden.

3. **Formatting Options**
   * Checkbox: *italic → underline* conversion.
   * (Later: classic vs. modern Shunn).

4. **Output Location**
   * Save dialog or default filename: `Author - Title.docx`.
   * Export button → engine works → "Successful save" message.

5. **Basic Status Indication**
   * Status bar or label: "File read", "Conversion in progress", "Done".

---

## Convenience Features (above v1.0)

* **Cover Letter Generator tab**
  * Automatic generation from template to `.txt`.
  * Market name, genre, simsub yes/no.

* **TypoKit tab**
  * File selection → profile selection (HU/EN/DE).
  * Preview panel (left: original, right: normalized).
  * Save as new file.

* **QuickConvert tab**
  * Drag&drop → `.docx/.odt/.md/.txt` full round-trip conversion.
  * Native ODT file support (reading and writing).
  * Preserves italic formatting during ODT conversions.
  * Automatic output to "out" folder.

* **CrossClip Lite**
  * Tray icon: [ ] auto-clean clipboard.
  * "Paste Clean" → always format-free + typo fix.
  * Light/dark mode toggle.

---

## Shell Logical Layers

1. **UI Controller** (JavaFX `Controller` classes)
   * Input collection, events (button press, checkbox).
   * File dialogs.

2. **Service Layer**
   * Calls the engine API (`DocxReader`, `ShunnFormatter`, `DocxWriter`).
   * Error handling → alert dialog in UI.

3. **Integration**
   * Pulls in the engine JAR as a dependency (v2.0.0).
   * Full ODT (OpenDocument Text) support with manusmith-engine v2.0.0.
   * The shell never implements parsing, only uses it.

---

## Key User Experience

* **Simple workflow:** Input → Metadata → Generate → Output.
* **Minimizing clicks.**
* **"Done, it works" feeling:** no need for immediate preview or log, just *safe output*.

---

👉 So the **shell's task is nothing more** than the **graphical frame** around the engine:

* File selection
* Metadata collection
* Optional settings
* Saving and status feedback

## Recent Updates

* Integrated with ManuSmith Engine v2.0.0
* Full ODT (OpenDocument Text) file support
* Complete round-trip conversions between formats
* Preserves formatting (including italic text) during conversions
* Improved stability and performance
* CI/CD integration for automated testing and deployment