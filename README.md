## ManuSmith Shell – feladatai

### 🎯 Fő cél

Kényelmes, egyszerű **UI/CLI**, ami elrejti az engine részleteit, és 1–2 kattintással elvégezteti a munkát.

---

## Minimum (MVP)

1. **Fájl kiválasztása**

   * `.docx` input kiválasztása `FileChooser`-rel vagy drag&drop.
   * Felhasználó látja a kiválasztott fájl nevét.

2. **Metaadatok bekérése**

   * Form: szerző név, cím, email, telefon, mű címe.
   * Szószám → automatikusan kiszámolja az engine, de a mező felülírható.

3. **Formázási opciók**

   * Checkbox: *italic → underline* konverzió.
   * (Később: klasszikus vs modern Shunn).

4. **Kimenet helye**

   * Mentés dialógus vagy alapértelmezett fájlnév: `Author - Title.docx`.
   * Export gomb → engine dolgozik → „Sikeres mentés” üzenet.

5. **Alap státusz jelzés**

   * Állapotsáv vagy label: „Fájl beolvasva”, „Konverzió folyamatban”, „Kész”.

---

## Kényelmi funkciók (v1.0 fölött)

* **Cover Letter Generator tab**

  * Sablonból automatikus generálás `.txt`-be.
  * Piacnév, műfaj, simsub igen/nem.

* **TypoKit tab**

  * Fájl kiválasztás → profil kiválasztása (HU/EN/DE).
  * Preview panel (bal: eredeti, jobb: normalizált).
  * Mentés új fájlként.

* **QuickConvert tab**

  * Drag&drop → `.docx/.odt/.md/.txt` round-trip konverzió.
  * Automatikus kimenet „out” mappába.

* **CrossClip Lite**

  * Tray ikon: [ ] auto-clean clipboard.
  * „Paste Clean” → mindig formázásmentes + tipófix.
  * Light/dark mód váltó.

---

## Shell logikai rétegei

1. **UI Controller** (JavaFX `Controller` osztályok)

   * Input bekérés, események (gombnyomás, checkbox).
   * Fájl dialógusok.

2. **Service réteg**

   * Hívja az engine API-ját (`DocxReader`, `ShunnFormatter`, `DocxWriter`).
   * Hibakezelés → UI-n alert dialógus.

3. **Integration**

   * Engine JAR-t dependency-ként behúzza.
   * A shell sosem implementál parsingot, csak felhasználja.

---

## Felhasználói élmény kulcsa

* **Egyszerű workflow:** Input → Metaadat → Generate → Output.
* **Kattintásszám minimalizálása.**
* **„Kész, működik” érzés:** azonnali preview vagy log nem kell, csak *biztonságos output*.

---

👉 Tehát a **shell feladata nem más**, mint a **grafikus keret** az engine körül:

* Fájlkiválasztás
* Metaadat-bekérés
* Opcionális beállítások
* Mentés és státuszvisszajelzés
