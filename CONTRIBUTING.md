# Contributing to PDF Wallet

Thank you for your interest in contributing to PDF Wallet. Contributions are welcome in the form of bug reports, documentation improvements, tests, design refinements, and code changes.

## Before you start

1. Search existing issues and pull requests before opening a new one.
2. For significant changes, open an issue first so the approach can be discussed.
3. Never include credentials, API keys, private service configuration, real personal documents, or generated build output in a contribution.

## Development setup

See [`GitHub/SETUP.md`](GitHub/SETUP.md) for the complete local setup guide.

### Requirements

- Android Studio with Android SDK 36
- JDK 21
- Android API 26 or newer
- A device or emulator suitable for the feature being tested

### Build and test

On Windows:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

Configure optional integrations locally. Keep environment-specific files and secrets untracked.

For a build without Firebase configuration, do not create `app/google-services.json`.
The Google Services plugin is enabled automatically when a developer supplies
their own configuration file.

## Branches and commits

- Create a focused branch from the default branch.
- Keep each change small and logically grouped.
- Use clear commit messages, for example:

  ```text
  feat: add document collection filters
  fix: preserve metadata during backup restore
  test: cover document parser edge cases
  docs: improve setup instructions
  ```

- Avoid committing unrelated formatting or generated files.

## Pull requests

A pull request should:

- Explain what changed and why.
- Identify any user-visible behavior changes.
- Include tests for parsing, mapping, persistence, or lifecycle logic where applicable.
- Include screenshots or recordings for meaningful UI changes.
- Mention configuration or migration requirements.
- Confirm that secrets and personal data are not included.

## Code guidelines

- Follow existing Kotlin and Compose patterns.
- Keep business logic out of composables.
- Use ViewModels and repositories for state and data operations.
- Use lifecycle-aware Flow collection.
- Run long-running work through WorkManager or injected coroutine scopes.
- Add or update Room migration tests when changing the schema.
- Represent integration failures explicitly instead of silently falling back.
- Keep accessibility, dark theme support, and adaptive layouts in mind.

## Reporting bugs

Include:

- A clear description of the problem
- Reproduction steps
- Expected and actual behavior
- Android version and device form factor
- Relevant, redacted log output

Do not attach real identity documents, tickets, credentials, or other private data.

## Troubleshooting

- Confirm that JDK 21 and Android SDK 36 are selected.
- Verify `sdk.dir` in `local.properties`.
- Use a Play Store-enabled emulator for ML Kit and Google-dependent features.
- If testing Firebase features, use a Firebase configuration belonging to your
  own development project.

## License

By contributing to PDF Wallet, you agree that your contribution is provided under the [MIT License](LICENSE).
