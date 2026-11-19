# midadd (مداد) — Native Android Application

Native Kotlin & Jetpack Compose rewrite of **midadd**, an AI Markdown-to-Word (.docx), PDF, and Google Docs formatting studio built with Material Design 3 and Firebase Cloud Firestore.

## Features
- **Word-Style Ribbon & Ruler Editor**: Multi-tab `WordRibbon` (Home, Insert, Layout, Review, View, File), live `WordRuler`, split Markdown source + A4 WebView preview, undo/redo history, Find & Replace, Word Count & Document Statistics, Watermarks, and 7 UI languages with native Arabic RTL/Bidi support.
- **Native OpenXML (.docx) Engine & OMML Equations**: Pure Kotlin `DocxExporter`, `DocxImporter`, and `LatexToOmml` converter generating editable Microsoft Word OOXML equations (`<m:oMath>`), styled RTL/LTR tables, callouts, and code blocks.
- **PDF & Google Docs Export**: Android `PrintManager` A4 PDF output and rich HTML clipboard (`ClipData.newHtmlText`) for pasting formatted tables and headings directly into Google Docs.
- **33 Professional Templates & Technical Guides**: Categorized, searchable template library with favorites synced to Firestore, plus bilingual Markdown/LaTeX/RTL technical guides.
- **Google Sign-In & Cloud Firestore Sync**: Passwordless Google Sign-In via Jetpack `CredentialManager` with multi-user isolated cloud drafts, favorite templates, preferences, and export credits.
