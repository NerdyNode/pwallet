# Developer setup

This guide explains how to prepare a local development environment without
sharing project credentials or private service configuration.

## Prerequisites

- Android Studio with Android SDK 36
- JDK 21
- Android API 26 or newer
- Google Play services for Google-dependent features

## Clone and open

```powershell
git clone https://github.com/NerdyNode/pdf-wallet.git
cd pdf-wallet
```

Open the project in Android Studio and allow Gradle synchronization to finish.

## Local properties

Copy `GitHub/local.properties.example` to `local.properties`, then set:

- `sdk.dir` to the local Android SDK directory
- `GEMINI_API_KEY` only when enabling AI analysis
- `WEB_CLIENT_ID` only when enabling Google authentication

`local.properties` is ignored by Git.

## Firebase-dependent features

Firebase configuration is intentionally not stored in this repository.

To enable Firebase services:

1. Create or select a Firebase project.
2. Register an Android application using the package name configured in the app module.
3. Enable only the services required by your development environment.
4. Download the generated `google-services.json`.
5. Place it at `app/google-services.json`.
6. Confirm that Git reports the file as ignored before committing.

The Google Services Gradle plugin is applied only when this file exists. A
Firebase configuration is therefore optional for source compilation and for
work that does not exercise Firebase-backed features.

## Build

```powershell
.\gradlew.bat assembleDebug
```

## Tests

JVM unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

Connected Android tests:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## Device and emulator notes

Use a Play Store-enabled emulator or a physical device when testing ML Kit,
Google authentication, camera capture, notifications, or biometric behavior.

## Troubleshooting

### Gradle cannot find the Android SDK

Verify `sdk.dir` in `local.properties`, or configure the SDK path through
Android Studio.

### Firebase features are unavailable

Check that your own `app/google-services.json` is present and matches the
application ID. Do not copy configuration from another developer or commit it
to the repository.

### ML Kit features do not work

Use a device or emulator with Google Play services and grant the required
camera or notification permissions.
