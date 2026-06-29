# Production Readiness Audit: Project Identity & Migration Plan

**Project:** ZeroBook  
**Auditor:** Senior Android Architect  
**Status:** Initial Audit Complete  
**Date:** June 29, 2026  

---

## 1. Executive Summary
The "ZeroBook" application currently exists in a transitional state between an AI-assisted prototype and a production-ready application. While the user-facing branding (App Name, Splash Logo) is largely consistent with "ZeroBook," the underlying technical identity is heavily fragmented with references to placeholder names, previous project titles ("RetailBook"), and AI builder tool metadata ("AI Studio").

**Current Official Identity:** ZeroBook  
**Target Package Name:** `com.zerobook.app` (Recommended)  
**Target Application ID:** `com.zerobook.app` (Recommended)  

---

## 2. Current Identity Problems

| Category | Finding | Impact |
|:---|:---|:---|
| **Application ID** | `com.aistudio.retailbook.acntgp` | **High.** This ID appears in the Play Store URL and identifies the app on the device. It contains "aistudio" (builder tool) and "retailbook" (old name). |
| **Namespace** | `com.example` | **High.** Standard Android placeholder. Must be unique for production. |
| **Package Structure** | `com.example.*` | **High.** All source code is located in the `com.example` package, which is a generic placeholder. |
| **Legacy Packages** | `com.vibecoding.zerobook_androidonly0` | **Medium.** Leftover code from a previous rename. Contains duplicate/obsolete theme and activity code. |
| **AI Tool Metadata** | `metadata.json`, `.env.example`, `.gitignore` | **Low.** References to Google AI Studio, Gemini API placeholders, and builder-specific capabilities. |
| **Placeholder Names** | `MyApplicationTheme`, `Greeting` | **Low.** Generic names from the standard Android Studio template. |

---

## 3. Files Containing Incorrect Identity

### Build Configuration
- **`app/build.gradle.kts`**: Contains `applicationId = "com.aistudio.retailbook.acntgp"` and `namespace = "com.example"`.
- **`settings.gradle.kts`**: While `rootProject.name` is correct, sub-projects might still inherit generic attributes.

### Source Code (Invasive)
- **`app/src/main/java/com/example/`**: ~50+ files using `package com.example`.
- **`app/src/main/java/com/vibecoding/`**: Legacy package containing unused theme and activity code.
- **`app/src/test/java/com/example/`** & **`app/src/androidTest/java/com/example/`**: Testing packages using placeholders.

### Manifest & Resources
- **`app/src/main/AndroidManifest.xml`**: References `.MainActivity` (resolving to `com.example.MainActivity`).
- **`app/src/main/java/com/example/ui/theme/Theme.kt`**: Defines `MyApplicationTheme`.

### Metadata & Documentation
- **`metadata.json`**: References `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API`.
- **`.env.example`**: Contains comments regarding AI Studio secrets.
- **`.gitignore`**: Contains `/assets/.aistudio`.
- **`BRAIN.md`**: Explicitly documents the identity fragmentation as technical debt.

---

## 4. Risk Assessment

- **Risk Level: HIGH**
- **Deployment Risk:** If the app is published with the current `applicationId`, it cannot be changed later without losing the existing user base and Play Store listing.
- **Technical Debt:** The mix of `com.example` and `com.vibecoding` packages makes the codebase confusing for new contributors and AI agents.
- **Data Integrity:** Changing the `applicationId` will change the internal storage path (e.g., `/data/data/com.aistudio...` to `/data/data/com.zerobook...`). If an update is pushed with a new ID, the app will lose access to its existing Room database (`ZeroBook.db`) unless a migration or backup/restore strategy is implemented.

---

## 5. Safe vs. Risky Changes

### What can be safely changed NOW:
- Deleting the `com.vibecoding` package and all files within it (confirmed as legacy/dead code).
- Updating `.env.example`, `.gitignore`, and `metadata.json` to remove AI Studio/Gemini placeholders.
- Renaming `MyApplicationTheme` to `ZeroBookTheme` within the `com.example` package.
- Updating comments and documentation (`BRAIN.md`, `README.md`).

### What should NOT be changed without a full refactor:
- **`applicationId`**: Do not change this until the final production ID is decided and a plan for existing local data is in place.
- **`namespace` / Package Renaming**: This requires a synchronized refactor across all `.kt`, `.xml`, and `.gradle.kts` files. Doing this partially will break the build.

---

## 6. Recommended Migration Order

1.  **Legacy Cleanup**: Delete `app/src/main/java/com/vibecoding/` and its corresponding test directories.
2.  **Metadata Scrubbing**: Remove AI Studio references from `metadata.json`, `.env.example`, and `.gitignore`.
3.  **UI Identity**: Rename `MyApplicationTheme` to `ZeroBookTheme` and clean up any remaining "My Application" strings in resources.
4.  **Namespace Refactor**: Use Android Studio's "Rename Package" tool to move `com.example` to the final production package (e.g., `com.zerobook.app`). This will automatically update all imports.
5.  **Application ID Update**: Synchronize the `applicationId` in `app/build.gradle.kts` with the new production package name.
6.  **Provider Authorities**: Ensure `${applicationId}.provider` in `AndroidManifest.xml` is correctly resolving after the ID change.
7.  **Final Audit**: Scan for any remaining "example," "vibecoding," or "aistudio" strings in comments and strings.xml.
