# FEATURES - ZeroBook

## 1. Onboarding & Setup
- **Purpose:** Capture business details for GST compliance and invoice headers.
- **Files:** `MainActivity.kt`, `SetupScreen.kt`, `ProfileFormSupport.kt`.
- **Flow:** Splash -> Check `BusinessProfile` -> Redirect to Setup if null -> Form (Business Name, GSTIN, PAN, Bank, PIN-based address) -> Dashboard.

## 2. Universal Search & Dashboard
- **Purpose:** Quick access to vouchers, parties, and products.
- **Files:** `DashboardScreen.kt`, `DashboardViewModel.kt`.
- **Flow:** Summary cards (Receivables/Payables) -> Universal search bar -> Quick action shortcuts.

## 3. Voucher Management (Sales, Purchases, etc.)
- **Purpose:** Recording all financial transactions and stock movements.
- **Files:** `VouchersScreen.kt`, `AppRepository.kt`, `Entities.kt`.
- **Flow:** Create Voucher -> Add Items -> Calculate Tax -> Save (calls `saveAndPostVoucher`) -> Update Ledger & Stock -> Generate Invoice.

## 4. Double-Entry Ledger System
- **Purpose:** Maintaining accounting integrity.
- **Files:** `AppRepository.kt`, `LedgerListScreen.kt`, `PartiesScreen.kt`.
- **Flow:** Every POSTED voucher generates 2+ `LedgerEntry` rows. Balances are derived by summing entries for a specific `accountHead`.

## 5. Inventory Tracking
- **Purpose:** Real-time stock visibility.
- **Files:** `ProductsScreen.kt`, `AppRepository.kt`.
- **Flow:** `current_stock` is a cached column in `Product` table, recomputed from voucher movements on every save.

## 6. GST Reporting & Compliance
- **Purpose:** Simplifying tax filing.
- **Files:** `ReportsScreen.kt`, `VouchersScreen.kt`.
- **Flow:** Tax split (CGST/SGST vs IGST) calculated during voucher entry. Reports aggregate these fields by period.

## 7. Email & Reminders
- **Purpose:** Automating receivable collections.
- **Files:** `EmailReminderScheduler.kt`, `EmailComposer.kt`, `ReminderWorker.kt`.
- **Flow:** WorkManager trigger -> Check overdue `BillReceivable` -> Build email draft (Intent) or Send direct (SMTP).

## 8. Invoice Generation
- **Purpose:** Creating shareable business documents.
- **Files:** `InvoiceGenerator.kt`, `WebViewPdfWriter.java`.
- **Flow:** Voucher ID -> HTML Template -> WebView Render -> PDF File -> Share/Print.

## 9. PIN Lock Security
- **Purpose:** Protecting sensitive financial data.
- **Files:** `MainActivity.kt`, `AppViewModel.kt`.
- **Flow:** App Launch -> Check `pinRequired` -> Show PIN input -> Authenticate -> Grant access.
