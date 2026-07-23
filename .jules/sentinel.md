# Sentinel Journal - ZeroBook Security

## 2026-07-17 - Robolectric Java Version Mismatch & Backup Security
**Vulnerability:**
The application had backup configuration files (`backup_rules.xml` and `data_extraction_rules.xml`) completely empty/commented out, while storing unencrypted Room database files (`ZeroBook.db`) on disk. This database contains high-risk PII (GSTIN, PAN, Bank Details) and plain credentials (SMTP passwords), making it vulnerable to extraction via `adb backup` or device transfers. Furthermore, a non-existent StateFlow property `kpiAnimationMode` in `DashboardViewModel` prevented compilation, and testing the application using Robolectric against the new Android compile SDK 36 threw `UnsupportedOperationException` on Java 17 due to Robolectric requiring Java 21 for SDK 36.

**Learning:**
1. Offline finance and ledger applications must disable system-wide backups (`android:allowBackup="false"`) and explicitly configure `<exclude>` directives to target specific databases as defense-in-depth, preventing cloud and device-to-device credential leakage.
2. In projects with newer Android compileSdk versions (like API 36), Robolectric attempts to build the sandbox on that compileSdk. If the build environment's Java toolchain is constrained to an older version (like Java 17), Robolectric fails. In these cases, tests must be pinned to the highest compatible Android SDK version supported by that Java environment (e.g., SDK 34 on Java 17) using `@Config(sdk = [34])`.

**Prevention:**
- Always keep backups disabled (`allowBackup="false"`) and define strict exclusion rules for database folders and private preferences in backup XML files.
- Ensure automated UI/Robolectric tests are pinned to compatible SDK levels that align with the compiler/JVM toolchain configured in `build.gradle.kts`.
