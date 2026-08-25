# Developer Setup Guide

This guide explains how to prepare a local development environment for PDF Wallet without sharing project credentials or private service configuration.

---

## Prerequisites

| Requirement | Version |
| --- | --- |
| Android Studio | Latest stable |
| Android SDK | 36 |
| JDK | 21 |
| Android API | 26+ (Android 8.0 Oreo) |
| Google Play services | Required for ML Kit, Auth, and Drive features |

---

## 1. Clone and open

```bash
git clone https://github.com/NerdyNode/pwallet.git
cd pwallet
```

Open the project in **Android Studio** and allow Gradle synchronization to complete before running or editing.

---

## 2. Configure local properties

Copy the example file and fill in your machine-specific values:

```bash
# Linux / macOS / Windows
cp setup_guide/local.properties.example local.properties
```

Then open `local.properties` and set:

| Key | Required | Description |
| --- | --- | --- |
| `sdk.dir` | ✅ Always | Full path to your local Android SDK directory |
| `GEMINI_API_KEY` | ⚙️ Optional | Only needed when enabling AI analysis features |
| `WEB_CLIENT_ID` | ⚙️ Optional | Only needed when enabling Google Sign-In |

> **Important:** `local.properties` is listed in `.gitignore` and must never be committed.

---

## 3. Firebase configuration (optional)

Firebase configuration is intentionally not stored in this repository. The app builds and runs without it — only Firebase-backed features (Auth, Remote Config, App Check) require it.

To enable Firebase services:

1. Create or select a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Register an Android app using the package name: `com.pdfwallet`
3. Enable only the services required for your development environment
4. Download the generated `google-services.json`
5. Place it at `app/google-services.json`
6. Confirm Git reports it as ignored: `git status` should not list it

> See `setup_guide/google-services.json.example` for the expected file structure.

---

## 4. Build

```bash
# Linux / macOS
./gradlew assembleDebug

# Windows
.\gradlew.bat assembleDebug
```

For a release build:

```bash
# Linux / macOS
./gradlew assembleRelease

# Windows
.\gradlew.bat assembleRelease
```

---

## 5. Run tests

**JVM unit tests:**

```bash
# Linux / macOS
./gradlew testDebugUnitTest

# Windows
.\gradlew.bat testDebugUnitTest
```

**Connected Android tests** (requires a device or emulator):

```bash
# Linux / macOS
./gradlew connectedDebugAndroidTest

# Windows
.\gradlew.bat connectedDebugAndroidTest
```

---

## 6. Device and emulator notes

Use a **Play Store-enabled emulator** or a **physical device** when testing:
- ML Kit (text recognition, barcode scanning, document scanner)
- Google Sign-In / Credential Manager
- Camera capture
- Notifications
- Biometric authentication

A standard AOSP emulator without Google Play services will not support these features.

---

## Troubleshooting

### Gradle cannot find the Android SDK

Verify `sdk.dir` in `local.properties`, or configure the SDK path through **Android Studio → SDK Manager**.

### Firebase features are unavailable

Check that your own `app/google-services.json` is present and matches the `com.pdfwallet` application ID. Do not copy configuration from another developer or commit it to the repository.

### ML Kit features do not work

Use a device or emulator with Google Play services installed, and grant the required camera or notification permissions at runtime.

### Build fails with JDK errors

Confirm that JDK 21 is set in **Android Studio → Project Structure → SDK Location → JDK Location**, and that `JAVA_HOME` points to JDK 21 if building from the command line.

### Gradle sync fails after pulling updates

Run `./gradlew --refresh-dependencies` to clear cached dependency metadata, then re-sync.

