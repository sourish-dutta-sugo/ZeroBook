# Package Migration Plan: ZeroBook

This document outlines the synchronized migration from prototype package names to the official production identity.

## 1. Package Information

| Attribute | Current Value | Target Value (Production) |
|:---|:---|:---|
| **Namespace** | `com.example` | `com.zerobook.app` |
| **Application ID** | `com.aistudio.retailbook.acntgp` | `com.zerobook.app` |
| **Root Directory** | `app/src/main/java/com/example` | `app/src/main/java/com/zerobook/app` |

---

## 2. Affected Files

### Source Code (Kotlin & Java)
- All files in `app/src/main/java/com/example/` (~50+ files)
- All files in `app/src/test/java/com/example/`
- All files in `app/src/androidTest/java/com/example/`
- All import statements referencing `com.example`

### Build & Configuration
- `app/build.gradle.kts` (namespace and applicationId)
- `app/src/main/AndroidManifest.xml` (Activity references, Provider authorities)

### Resources
- XML layout/resource references (if any, though project is primarily Compose)
- `app/src/main/res/xml/provider_paths.xml` (if it contains package-specific paths)

---

## 3. Risks & Impact

- **Data Loss:** Changing the `applicationId` will change the app's internal storage directory on Android. Existing installations will treat the new version as a different app or lose access to the `ZeroBook.db` file located in the old package's internal storage.
  - *Mitigation:* This migration is intended for "Production Readiness" before the first public Play Store release. Internal testers will need to perform a clean install.
- **Build Breakage:** Partial renaming will lead to "Unresolved reference" errors across the project.
  - *Mitigation:* The migration will be performed as a single synchronized operation.
- **Provider Authorities:** The `FileProvider` authority must be updated to match the new `applicationId` to prevent conflicts with other apps.

---

## 4. Required Migration Steps

1.  **Preparation:** Close Android Studio's background indexing if possible or ensure a clean state.
2.  **Metadata Scrubbing:** Remove "RetailBook" and "AI Studio" references from non-code files.
3.  **Legacy Deletion:** Remove the `com.vibecoding` package.
4.  **Directory Refactor:**
    - Rename `app/src/main/java/com/example` to `app/src/main/java/com/zerobook/app`.
    - Rename corresponding `test` and `androidTest` directories.
5.  **Code Refactor:**
    - Update `package` declarations in all `.kt` and `.java` files.
    - Update `import` statements in all `.kt` and `.java` files.
6.  **Manifest & Gradle Update:**
    - Update `namespace` and `applicationId` in `app/build.gradle.kts`.
    - Update `AndroidManifest.xml` to ensure it correctly points to `.MainActivity` and uses `${applicationId}` for authorities.
7.  **Theme Renaming:** Rename `MyApplicationTheme` to `ZeroBookTheme` throughout the project.
8.  **Verification:** Perform a Gradle Sync and project build.

---

## 5. Potential Build Issues
- **KSP/Room Generation:** Room's generated DAOs and Database classes might need a clean/rebuild to recognize the new package.
- **BuildConfig:** Any code relying on `com.example.BuildConfig` will need to import `com.zerobook.app.BuildConfig`.
- **Resource R class:** Imports of `com.example.R` must change to `com.zerobook.app.R`.
