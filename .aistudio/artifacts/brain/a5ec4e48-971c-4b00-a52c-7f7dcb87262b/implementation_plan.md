# Saber Notes – Handwriting & Digital Notebook for Android

Eine elegante, performante und native Android-Notizen-App inspiriert von Saber (saber-notes/saber), optimiert für präzises Freihand-Schreiben, vielseitige Papiermuster, einen überlappungsfreien Textmarker, intelligente Farbumkehrung im Dark Mode und eine schwebende Werkzeugpalette für maximale Schreibfläche.

## User Review & Critical Decisions

> [!IMPORTANT]
> Basierend auf deinen Antworten im Klärungsinterview wurden folgende Kernentscheidungen für die App festgelegt:

- **Fokus-Features**: Freihand-Zeichnen mit Stiften (Fineliner/Kugelschreiber), speziellem Textmarker (ohne Farbüberlappungs-Eindunkelung) und anpassbaren Papiermustern (Kariert, Liniert, Punkte, Blanko).
- **Standard-Papierstil**: Kariertes und liniertes Papier im modernen Material 3 Design mit dynamischer Linienanpassung und invertiertem Dark Mode.
- **Werkzeugleiste**: Elegante, schwebende und einklappbare Werkzeugpalette (Floating Toolbar), die sich flexibel auf dem Bildschirm platzieren lässt, um den gesamten Canvas frei für Notizen zu halten.
- **Speicherstrategie**: Schnelle, zuverlässige lokale Persistenz mit Room & JSON-Vektordaten für sofortiges Laden, Offline-Nutzung und maximale Privatsphäre.

---

## 1. Overview & Core Concept

- **Was die App macht**: Saber Notes bringt das beste Schreibgefühl von Saber nativ auf Android. Nutzer können Notizbücher in Ordnern organisieren, mehrseitige Notizen erstellen, mit Stift oder Finger mit flüssiger Kurvenglättung schreiben, Textstellen sauber hervorheben, geometrische Formen zeichnen und Notizen als Bild oder Dokument exportieren.
- **Zielgruppe**: Schüler, Studierende, Kreative und Berufstätige, die ein natürliches Schreibgefühl auf Tablets und Smartphones schätzen – ohne Abo-Zwang und mit voller Datenkontrolle.
- **Kernwerte**:
  - *Natürliches Schreibgefühl*: Bezier-Spline-Glättung und Geschwindigkeitsdynamik für geschmeidige Striche.
  - *Saber Signature Highlighter*: Überlappende Textmarkerstriche dunkeln nicht ab, sondern behalten eine gleichmäßige Leuchtkraft.
  - *Smart Dark Mode*: Invertiert bei Bedarf die Notizlinien und Tinte (Weiß auf Schwarz) für augenschonendes Arbeiten bei Nacht.
  - *Schwebende Werkzeugpalette*: Schneller Zugriff auf Stifte, Farben, Strichstärken, Radierer, Formen und Rückgängig/Wiederholen.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Dashboard & Notizübersicht**:
   - Übersichtliche Ordner- und Notizbuch-Rasteransicht im Material 3 Look.
   - Schnelles Anlegen neuer Notizen mit voreingestelltem oder individuellem Papiermuster.
   - Suchleiste und Filter nach Notizbuchtiteln und Aktualisierungsdatum.
2. **Schreib- und Zeichen-Canvas**:
   - Vollflächiger Canvas mit frei beweglicher oder minimierbarer Floating-Toolbar.
   - Papiermuster umschaltbar: *Kariert (Grid)*, *Liniert (Ruled)*, *Punktraster (Dot Grid)*, *Blanko*.
   - Intelligenter Dark Mode Schalter (Invertierte Papier- und Tintenfarben).
   - Stift-Modi: Stift (Fineliner mit weicher Druckkurve), Highlighter (gleichmäßige Deckkraft), Radierer (Strichradierer & Punkt-Radierer), Formen-Assistent (Linie, Rechteck, Kreis, Pfeil).
   - Zoom- und Pan-Funktion (Zwei-Finger-Gesten) sowie Stylus/Finger-Erkennung.
   - Seiten-Navigation: Nächste/Vorherige Seite, neue Seite anhängen, Seitenübersicht.
3. **Notizverwaltung & Export**:
   - Titel und Ordnerzuweisung ändern.
   - Schneller Export der aktuellen Seite oder des gesamten Notizbuchs als Bild (PNG) zur Weitergabe via Android Share Sheet.

### Visual Identity & Theme

- **Aesthetic Direction**: *Minimalist Studio Notebook* – ruhige, aufgeräumte Ästhetik mit dezenten Papiertönen, akzentuierten M3-Farben (Primary Indigo/Deep Teal mit warmem Amber-Akzent).
- **Papierfarben**:
  - *Light Mode*: Warmes Elfenbein/Weiß (`#FCFBF9`) mit feinen, dezenten Linien (`#E2E8F0` / `#CBD5E1`).
  - *Smart Dark Mode*: Tiefes Graphit/Nachtblau (`#121316`) mit invertierten Linien (`#2A2D35`) und automatischer Kontrastanpassung schwarzer Striche zu strahlendem Weiß.
- **Typografie**: Moderner System-Font-Stack mit klarer Hierarchie: ausdrucksstarke Display-Titel für Notizbücher, gut lesbare Metadaten und intuitive Icons für Werkzeuge.

---

## 3. Key Product Decisions & Trade-Offs

### Entscheidung 1: Tintendynamik und Pfadglättung
- **Gewählter Ansatz**: Mathematische Bezier-Kurvenglättung (Catmull-Rom / kubische Splines) direkt auf dem Jetpack Compose `Canvas`.
- **Begründung**: Bietet 60/120 FPS flüssiges Zeichnen ohne Latenz und ohne externe native C++-Abhängigkeiten, die den Build verkomplizieren würden.

### Entscheidung 2: Saber Highlighter Blend Mode
- **Gewählter Ansatz**: Verwendung von `BlendMode.SrcOver` mit offscreen Alpha-Compositing oder separaten Highlighter-Ebenen.
- **Begründung**: Verhindert, dass sich überkreuzende Textmarker-Striche aufsummieren und wie schwarze Flecken wirken.

### Entscheidung 3: Schwebende Werkzeugpalette (Floating Palette)
- **Gewählter Ansatz**: Frei am Bildschirmrand positionierbare, einklappbare Capsule/Card mit `Modifier.pointerInput` und animiertem Aufklappen.
- **Begründung**: Maximale Zeichenfläche auf Handys und Tablets, während Werkzeuge (Stiftart, Stärke, Farbpalette, Undo/Redo) mit einem Fingertipp erreichbar bleiben.

### Entscheidung 4: Lokale Speicherung mit Room & TypeConverter
- **Gewählter Ansatz**: Room-Datenbank für Notizbücher, Seiten und Vektordaten (Pfade, Farben, Breiten als optimiertes JSON).
- **Begründung**: Garantiert sofortige Ladezeiten, funktioniert komplett offline ohne Account-Zwang und hält die Notizen sicher auf dem Gerät.

---

## 4. Technical Architecture & Data Strategy

### System- & Komponenten-Diagramm

```
┌─────────────────────────────────────────────────────────────┐
│                       UI Layer (M3)                         │
├──────────────────────────────┬──────────────────────────────┤
│       DashboardScreen        │         EditorScreen         │
│  - Notebook/Page Grid        │  - Gesture Canvas            │
│  - Folder Navigation         │  - FloatingToolPalette       │
│  - Search & Quick Create     │  - PaperPatternBackground    │
│  - Settings & Theme Dialog   │  - PageNavigationControls    │
└──────────────┬───────────────┴──────────────┬───────────────┘
               │                              │
               ▼                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    ViewModel Layer                          │
├──────────────────────────────┬──────────────────────────────┤
│      DashboardViewModel      │       EditorViewModel        │
│  - Load & Filter Notebooks   │  - Stroke Buffer & History   │
│  - Manage Folders & Deletions│  - Tool & Color State        │
│                              │  - Undo/Redo Stacks          │
│                              │  - Dark Mode Inversion Logic │
└──────────────┬───────────────┴──────────────┬───────────────┘
               │                              │
               ▼                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Repository Layer                         │
├─────────────────────────────────────────────────────────────┤
│                    NotesRepository                          │
│  - CRUD for Notebooks, Pages & Stroke Payloads              │
│  - Export Engine (Bitmap generation & Sharing)              │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                  Data Layer (Room DB)                       │
├──────────────────────────────┬──────────────────────────────┤
│         AppDatabase          │        Converters            │
│  - NotebookDao               │  - StrokeListConverter       │
│  - PageDao                   │  - Color/ToolConverter       │
└──────────────────────────────┴──────────────────────────────┘
```

### Datenmodelle

1. **NotebookEntity**:
   - `id: Long` (Primary Key)
   - `title: String`
   - `folder: String`
   - `createdAt: Long`
   - `updatedAt: Long`
   - `pageCount: Int`
   - `coverColor: Long`

2. **PageEntity**:
   - `id: Long` (Primary Key)
   - `notebookId: Long` (Foreign Key)
   - `pageIndex: Int`
   - `paperStyle: PaperStyle` (`BLANK`, `RULED`, `GRID`, `DOTS`, `CORNELL`)
   - `strokesJson: String` (Serialisierte Liste von Strichen mit Punkten, Farbe, Stärke, Tool-Typ)
   - `updatedAt: Long`

3. **Stroke**:
   - `points: List<StrokePoint>` (x, y, pressure)
   - `color: Long`
   - `width: Float`
   - `toolType: ToolType` (`PEN`, `HIGHLIGHTER`, `ERASER`, `SHAPE`)
   - `shapeType: ShapeType?` (`NONE`, `LINE`, `RECTANGLE`, `CIRCLE`, `ARROW`)

---

## 5. Geplante Umsetzungsphasen

1. **Phase 1: Grundlagen & Datenmodell**:
   - Room Entities (`NotebookEntity`, `PageEntity`), DAOs und Repository mit TypeConvertern.
   - Aktualisierung von `metadata.json` und Ressourcen (`strings.xml`, Icons).
2. **Phase 2: Canvas & Zeichen-Engine**:
   - Hochperformanter Zeichen-Canvas mit Pfadglättung, Touch-/Stylus-Verarbeitung.
   - Papiermuster-Renderer (Kariert, Liniert, Punkte, Cornell) mit Zoom/Pan.
   - Spezialisierter Highlighter mit gleichmäßigem Blend-Effekt und Strichradierer.
   - Smart Invert Mode (Farbenumkehrung im Dark Mode).
3. **Phase 3: Schwebende Werkzeugpalette**:
   - Bewegliche, kompakte Floating Toolbar mit Einklapp-Animation.
   - Farbkreis- und Stärke-Selektor, Tool-Umschalter, Undo/Redo.
4. **Phase 4: Dashboard & Notizverwaltung**:
   - M3 Notizbuch-Raster, Ordnerfilter, Schnellsuche, Seitenvorschau und Export-Funktion.
   - Kompilierungsprüfung und Feinschliff.
