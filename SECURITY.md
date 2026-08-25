# Security Policy

## Supported Versions

| Version | Supported |
| --- | --- |
| Latest (main branch) | ✅ Yes |

---

## Reporting a Vulnerability

PDF Wallet handles sensitive personal documents, so security issues are taken seriously.

**Please do NOT open a public GitHub issue for security vulnerabilities.**

Instead, report security issues responsibly by emailing us at:

> 📧 **security@nerdynode.dev** *(or open a [private security advisory](https://github.com/NerdyNode/pwallet/security/advisories/new) on GitHub)*

### What to include

Please provide as much of the following as possible:

- A clear description of the vulnerability
- Steps to reproduce the issue
- The potential impact and affected versions
- Any suggested fixes or mitigations

### What to expect

- We will acknowledge receipt within **72 hours**
- We aim to assess and respond with a timeline within **7 days**
- We will keep you updated as we work on a fix
- We will credit researchers in the release notes (unless you prefer anonymity)

---

## Security considerations

PDF Wallet is designed with privacy as a core principle:

- Documents are stored in **app-private storage** inaccessible to other apps
- Metadata is encrypted at rest using **SQLCipher**
- Backups may contain sensitive user data — treat them as confidential
- **Never commit** google-services.json, local.properties, or any credentials to the repository
- AI analysis is **optional** and off by default

---

## Scope

Issues in scope:

- Unauthorized access to stored documents or metadata
- Encryption weaknesses or bypass
- Sensitive data leaks in logs or over the network
- Authentication or biometric bypass
- Insecure handling of backup files

Issues out of scope:

- Vulnerabilities in third-party dependencies (please report those upstream)
- Issues requiring physical access to an unlocked device with developer options enabled
- Issues in the user's Firebase project configuration (outside this codebase)
