# Changelog

All notable changes to PDF Wallet will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [2.1.1] - 2026-09-17

### Added

- **Pass template system** — Modular template architecture with dedicated renderers for Airline, Train, Movie, Government ID, and Generic document types
- **Template contract & registry** — `TemplateContract` interface and `TemplateRegistry` for type-safe template resolution
- **Shared pass components** — Reusable Compose components in `SharedComponents.kt` for consistent pass UI
- **Palette helper** — Dynamic color extraction from document thumbnails (`PaletteHelper.kt`)
- **Barcode config** — Unified barcode configuration model (`BarcodeConfig.kt`)
- **AI extraction validator** — `ExtractionValidator` for post-extraction quality checks
- **Gemini prompt builder** — `GeminiPromptBuilder` for structured, reusable AI prompt construction
- **BarcodePreFilter** — Filters irrelevant barcode candidates before scanning
- **BarcodeRanker & RankedBarcode** — Ranks detected barcodes by confidence and relevance
- **ImageBarcodePreprocessor** — Pre-processes images to improve barcode detection accuracy
- **MlKitOcrExtractor** — Dedicated ML Kit OCR text extraction service
- **PdfBoxTextExtractor** — Apache PdfBox-based text extraction for better PDF text layer support
- **PdfXObjectBarcodeExtractor** — Extracts barcodes embedded as PDF XObjects
- **TextMerger, TextPreProcessor, TextQualityChecker** — Pipeline for normalizing and validating extracted text
- **StorageCleanupWorker** — Background worker that removes orphaned files from private storage
- **TicketEditBottomSheet** — In-app bottom sheet for editing ticket metadata
- **Geist font family** — Added Geist Regular, Medium, SemiBold, Bold, and Black typefaces
- **Lifecycle-aware Compose extensions** — Added `lifecycle-runtime-compose` and `lifecycle-process`
- **Palette KTX** — Added `androidx.palette:palette-ktx` for dynamic color theming
- **JNI packaging fix** — `useLegacyPackaging = false` for proper native lib packaging

### Changed

- **Pass rendering refactored** — Replaced monolithic `PassTemplate.kt` and `PassTemplateResolver.kt` with the new template registry system
- **`DocumentType`** — Expanded and reorganized document type enum
- **`Document` entity** — Schema updated with new metadata fields
- **`DocumentDao`** — Optimized queries for new document model
- **`AppDatabase`** — Incremented version with migration support
- **`AiDocumentResult` & `AiResultMapper`** — Aligned with new extraction validator pipeline
- **`GeminiDocumentAnalyser`** — Integrated `GeminiPromptBuilder` and `ExtractionValidator`
- **`BarcodeExtractor`** — Delegated to new `BarcodePreFilter`, `BarcodeRanker`, and `ImageBarcodePreprocessor`
- **`DocumentToBitmapConverter`** — Performance improvements for high-resolution PDF rendering
- **`DocumentProcessingWorker`** — Integrated full new PDF and barcode pipeline
- **`DocumentRepository`** — Improved import coordination and deduplication logic
- **`SettingsRepository`** — Added new preference keys
- **`DatabaseModule`** — Updated for new schema version
- **`PdfFileManager`** — Cleanup improvements aligned with `StorageCleanupWorker`
- **`HomeScreen`** — UI refresh and navigation improvements
- **`DocumentDetailScreen`** — Updated to use new document model
- **`TicketDetailScreen` & `TicketDetailViewModel`** — Supports inline editing via `TicketEditBottomSheet`
- **`SettingsScreen` & `SettingsViewModel`** — New preferences and controls
- **`DocumentCard`** — Refreshed card design with palette-based theming
- **`Color.kt`, `DocAccent.kt`, `Type.kt`** — Updated design tokens and Geist font integration
- **`BarcodeGenerator`** — Minor improvements
- **`MainActivity` & `MainScreen`** — Navigation structure updates
- **CameraX** bumped from `1.3.4` → `1.4.1`
- **`versionCode`** bumped from `1` → `2`
- **`versionName`** bumped from `1.0` → `2.1.1`

### Removed

- `TicketMetadata.kt` — Superseded by the unified `DocumentMetadata` model
- `PassTemplate.kt` — Replaced by the new template registry system
- `PassTemplateResolver.kt` — Replaced by `TemplateRegistry`

---

## [1.0.0] - 2026-08-26


### Added

- **Document import** via Android share sheet, file picker, and camera scanner
- **Background processing** with WorkManager — PDF rendering, barcode scanning, and structured data extraction
- **AI document analysis** using Gemini SDK (optional, requires API key)
- **Encrypted local database** using Room + SQLCipher for metadata storage
- **Biometric app lock** for protecting access to the wallet
- **Document collections** for organizing documents by category
- **Search, filter, and sort** across all stored documents
- **Expiry and lifecycle tracking** with background checks via WorkManager
- **Google Drive sync** for optional cloud backup (requires Google account)
- **Local ZIP backup and restore** for full data portability
- **Home-screen widget** using Glance for quick document access
- **Adaptive Material 3 UI** supporting phones, tablets, landscape, and foldables
- **Glassmorphism design system** with dynamic theming
- **Document detail views** with ticket and pass rendering
- **Camera scanner** powered by CameraX and ML Kit
- **Share receiver** for accepting documents from other apps
- **Debug logs screen** for development diagnostics
- **Pass card renderer** with template-based display
- **Offline processing queue** for documents imported without connectivity
- **Waitlist check worker** for managing access states
- GitHub Actions CI workflow for automated builds and tests

---

*This changelog is maintained manually. For a full commit history, see [GitHub commits](https://github.com/NerdyNode/pwallet/commits/main).*
