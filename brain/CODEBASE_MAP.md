# CODEBASE_MAP - ZeroBook

## Important Folders & Files

### `app/src/main/java/com/example/` (The Core Source)
- **`MainActivity.kt`**: App entry point, splash handling, navigation host, and PIN gate logic.
- **`data/`**: Room-related logic and persistence helpers.
    - `Entities.kt`: All `@Entity` data classes (The Schema).
    - `AppDaos.kt`: All `@Dao` interfaces.
    - `AppDatabase.kt`: RoomDatabase singleton and migration triggers.
    - `Migrations.kt`: Formal migration objects and idempotent guards.
    - `AppRepository.kt`: Centralized business logic (Voucher posting, calculations).
    - `EmailReminderScheduler.kt`: WorkManager scheduling and email sending logic.
    - `FinancialYearUtils.kt`: April-March FY calculation helpers.
- **`services/`**: External system integrations and document generation.
    - `InvoiceGenerator.kt`: HTML -> PDF conversion for invoices.
    - `EmailComposer.kt`: JavaMail SMTP wrapper.
    - `CsvTransferManager.kt`: Import/Export logic.
    - `ExportStorageManager.kt`: Shared storage handling.
- **`ui/`**: User Interface components.
    - `AppViewModel.kt`: The primary state holder for the application.
    - `screens/`: Individual screen implementations (e.g., `VouchersScreen.kt`, `DashboardScreen.kt`).
    - `theme/`: Material 3 theme definitions.
    - `animation/`: Motion and transition logic.
- **`utils/`**: General purpose helper functions.

### `app/src/main/java/com/vibecoding/zerobook_androidonly0/` (Legacy)
- **DEBT:** This contains duplicate files from a previous package name and should be treated as dead code or moved/deleted after verification.

### `app/src/main/assets/`
- `changelog.json`: Data for the "What's New" dialog.

## Responsibility Matrix
- **Schema Changes:** `Entities.kt` + `Migrations.kt` + `AppDatabase.kt`.
- **Business Rules:** `AppRepository.kt`.
- **UI State:** `AppViewModel.kt`.
- **Navigation:** `MainActivity.kt`.
- **Tax Logic:** Screens (currently duplicated) and `AppRepository.kt`.
