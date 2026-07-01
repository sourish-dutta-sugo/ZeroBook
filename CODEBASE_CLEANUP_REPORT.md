# ZeroBook Codebase Cleanup Report

**Auditor:** Senior Android Performance Engineer
**Date:** June 29, 2024
**Status:** DRAFT (Phase 4)

## Safe To Remove

### Unused Screens (Kotlin Classes)
The following "Premium" screen implementations are not referenced in `MainActivity.kt` or any other part of the application's navigation graph.

*   **File:** `app/src/main/java/com/zerobook/app/ui/screens/DashboardScreenPremium.kt`
*   **File:** `app/src/main/java/com/zerobook/app/ui/screens/PartiesScreenPremium.kt`
*   **File:** `app/src/main/java/com/zerobook/app/ui/screens/ReportsScreenPremium.kt`
*   **File:** `app/src/main/java/com/zerobook/app/ui/screens/TransactionsScreenPremium.kt`
*   **File:** `app/src/main/java/com/zerobook/app/ui/screens/ExpensesBudgetScreenPremium.kt`
    *   **Reason:** Unused scaffolding/legacy code.
    *   **Impact:** Reduces build time and DEX size.
    *   **Confidence:** High.

### Unused Components
*   **File:** `app/src/main/java/com/zerobook/app/ui/components/PremiumComponents.kt`
    *   **Reason:** Only used by the Premium screens listed above. Contains a duplicate `formatIndianCurrency` function that is not used by the core app.
    *   **Impact:** Reduces codebase complexity.
    *   **Confidence:** High.

### Unused Resources (Images/XML)
*   **File:** `app/src/main/res/drawable/splash_branding.xml`
    *   **Reason:** Not referenced in any theme or layout. The app uses `Theme.ZeroBook.Splash` which points to `zerobook_icon`.
*   **File:** `app/src/main/res/drawable/zerobook_full_logo.png` (~1.2 MB)
    *   **Reason:** Only used by `splash_branding.xml`.
*   **File:** `app/src/main/res/drawable/zerobook_logo.png` (~1.2 MB)
*   **File:** `app/src/main/res/drawable-hdpi/zerobook_logo.png` (~1.2 MB)
*   **File:** `app/src/main/res/drawable-xhdpi/zerobook_logo.png` (~1.2 MB)
*   **File:** `app/src/main/res/drawable-xxhdpi/zerobook_logo.png` (~1.2 MB)
*   **File:** `app/src/main/res/drawable-xxxhdpi/zerobook_logo.png` (~1.2 MB)
    *   **Reason:** Not referenced in the code. These are large duplicate files.
    *   **Impact:** **Major App Size Reduction (~6 MB).**
    *   **Confidence:** High.

### Unused Dependencies
The following libraries are included in `build.gradle.kts` but have no usages in the production source code.

*   **Dependency:** `com.squareup.retrofit2:retrofit`
    *   **Purpose:** Networking.
    *   **Risk:** Low. Not used.
*   **Dependency:** `com.squareup.okhttp3:okhttp`
    *   **Purpose:** HTTP Client.
    *   **Risk:** Low. Not used.
*   **Dependency:** `com.squareup.moshi:moshi-kotlin`
    *   **Purpose:** JSON parsing.
    *   **Risk:** Low. Not used.
*   **Dependency:** `com.squareup.retrofit2:converter-moshi`
    *   **Purpose:** Retrofit Moshi converter.
    *   **Risk:** Low. Not used.
*   **Dependency:** `com.squareup.okhttp3:logging-interceptor`
    *   **Purpose:** Network logging.
    *   **Risk:** Low. Not used.
*   **Dependency:** `com.squareup.moshi:moshi-kotlin-codegen` (KSP)
    *   **Purpose:** Moshi code generation.
    *   **Risk:** Low. Not used.

### Unused Documentation / AI Artifacts (Root)
The following files in the project root appear to be legacy documentation or logs from previous AI sessions that are no longer relevant to the current state of the "Android-only" codebase.

*   **File:** `IMPLEMENTATION_GUIDE.md`
*   **File:** `PREMIUM_DESIGN_GUIDE.md`
*   **File:** `PREMIUM_REDESIGN_README.md`
*   **File:** `IDENTITY_MIGRATION_REPORT.md`
*   **File:** `PACKAGE_MIGRATION_PLAN.md`
*   **File:** `PROJECT_IDENTITY_AUDIT.md`
*   **File:** `build.log`, `gradle_zero.log`, `gradle_build_fix.log`, `gradle_install_fix.log`
    *   **Reason:** Redundant artifacts. `BRAIN.md` is now the single source of truth.
    *   **Impact:** Cleaner project root.
    *   **Confidence:** High.

### Redundant Logic (HSN Lookup)
*   **Files:** `app/src/main/java/com/zerobook/app/data/HsnLookup.kt` & `app/src/main/java/com/zerobook/app/utils/HsnLookup.kt`
    *   **Why uncertain:** Both are used in different parts of the app (`VoucherItemSheet.kt` vs `ProductsScreen.kt`).
    *   **Recommendation:** Consolidate into `com.zerobook.app.data.HsnLookup.kt` as it has more advanced features (suggesting based on local products).

### Parallel Reminder Systems
*   **Issue:** Overlapping reminder-schedule storage (SharedPreferences vs. Room SQL table).
    *   **Recommendation:** Consolidate onto the Room database for better data integrity and single source of truth.

## Must Keep

*   **`play-services-location`**: Used for GST state code auto-detection in `SetupScreen.kt` and `ProfileFormSupport.kt`.
*   **ML Kit Text Recognition**: Used for bill attachment OCR in `VouchersScreen.kt`.
*   **ML Kit Barcode Scanning**: Used in `BarcodeScannerDialog.kt`.
*   **JavaMail / SMTP**: Used for direct email sending feature.

## Performance Improvements

*   **Issue:** God-files `AppRepository.kt` (~1600 lines) and `VouchersScreen.kt` (~4600 lines).
    *   **Solution:** In a future phase, modularize `AppRepository` by domain and split `VouchersScreen` into smaller sub-composables.
*   **Issue:** Full database recalculations (`recalculateOutstandings`, `recalculateProductStocks`) on every voucher save.
    *   **Solution:** Move to incremental updates.
*   **Issue:** Unoptimized large PNGs.
    *   **Solution:** Convert to WebP or SVG (VectorDrawables) where possible.

## Security Improvements

*   **Issue:** Unencrypted SQLite database.
    *   **Solution:** Integrate SQLCipher.
*   **Issue:** Cleartext SMTP credentials in DB.
    *   **Solution:** Encrypt at rest using Android Keystore.
*   **Issue:** Vulnerable `backup_rules.xml`.
    *   **Solution:** Explicitly exclude `ZeroBook.db` from auto-backups until encrypted.
