# Contributing to PDF Wallet

Thank you for your interest in contributing to PDF Wallet! Contributions are welcome in the form of bug reports, documentation improvements, tests, design refinements, and code changes.

---

## Before you start

1. Search existing [issues](https://github.com/NerdyNode/pwallet/issues) and pull requests before opening a new one.
2. For significant changes, open an issue first so the approach can be discussed.
3. Never include credentials, API keys, private service configuration, real personal documents, or generated build output in a contribution.

---

## Development setup

See [`setup_guide/SETUP.md`](setup_guide/SETUP.md) for the complete local setup guide.

### Requirements

| Requirement | Version |
| --- | --- |
| Android Studio | Latest stable + Android SDK 36 |
| JDK | 21 |
| Android API | 26+ |
| Device / Emulator | Play Store-enabled for ML Kit and Auth features |

### Build and test

```bash
# Linux / macOS
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest

# Windows
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

> Configure optional integrations locally. Keep environment-specific files and secrets untracked.

For a build without Firebase configuration, do not create `app/google-services.json`.
The Google Services plugin is enabled automatically when a developer supplies their own configuration file.

---

## Branches and commits

- Create a focused branch from the default branch.
- Keep each change small and logically grouped.
- Use clear commit messages following the [Conventional Commits](https://www.conventionalcommits.org/) format:

  ```text
  feat: add document collection filters
  fix: preserve metadata during backup restore
  test: cover document parser edge cases
  docs: improve setup instructions
  refactor: simplify thumbnail generation
  chore: update Compose BOM version
  ```

- Avoid committing unrelated formatting changes or generated files.

---

## Pull requests

A good pull request should:

- **Explain** what changed and why
- **Identify** any user-visible behavior changes
- **Include tests** for parsing, mapping, persistence, or lifecycle logic where applicable
- **Include screenshots or recordings** for meaningful UI changes
- **Mention** any configuration or migration requirements
- **Confirm** that secrets and personal data are not included

---

## Code guidelines

- Follow existing Kotlin and Compose patterns in the project.
- Keep business logic out of composables — use ViewModels and repositories.
- Use lifecycle-aware Flow collection.
- Run long-running work through WorkManager or injected coroutine scopes.
- Add or update Room migration tests when changing the database schema.
- Represent integration failures explicitly instead of silently falling back.
- Keep accessibility, dark theme support, and adaptive layouts in mind.

---

## Reporting bugs

When filing a bug, please include:

- A clear description of the problem
- Step-by-step reproduction steps
- Expected vs actual behavior
- Android version and device/emulator form factor
- Relevant, **redacted** log output

> ⚠️ Do not attach real identity documents, tickets, credentials, or other private data.

---

## Security vulnerabilities

If you discover a security issue, please **do not open a public issue**. See [`SECURITY.md`](SECURITY.md) for our responsible disclosure process.

---

## Troubleshooting

| Problem | Fix |
| --- | --- |
| Gradle cannot find Android SDK | Verify `sdk.dir` in `local.properties` |
| Firebase features unavailable | Ensure `app/google-services.json` exists and matches your app ID |
| ML Kit not working | Use a device/emulator with Google Play services |
| Build fails with JDK error | Confirm JDK 21 is selected in Android Studio |

---

## License

By contributing to PDF Wallet, you agree that your contribution is provided under the [MIT License](LICENSE).
