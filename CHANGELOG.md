# Changelog

All notable changes to PDF Wallet will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
