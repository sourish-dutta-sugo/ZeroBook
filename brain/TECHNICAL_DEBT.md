# TECHNICAL_DEBT - ZeroBook

## 1. Dead Code / Package Duplication
- **Issue:** Leftover files in `com.vibecoding.zerobook_androidonly0` after a project rename.
- **Risk:** Increases maintenance burden and confusion.
- **Action:** Delete the `vibecoding` package after verifying zero remaining references.

## 2. Distributed GST Logic
- **Issue:** Tax calculation rules (CGST/SGST vs IGST) are reimplemented in multiple screens (`VouchersScreen`, `QuickSaleScreen`, `ReportsScreen`).
- **Risk:** High chance of inconsistency if tax rules change.
- **Action:** Extract a centralized `GstCalculator` component.

## 3. God Objects
- **Issue:** `AppRepository.kt` (~1700 lines) and `VouchersScreen.kt` (~4800 lines) handle too many responsibilities.
- **Risk:** Slow comprehension and high risk of regression during modification.
- **Action:** Sub-divide repository by domain and split UI into smaller, focused composables.

## 4. Brittle Relationships (String Joins)
- **Issue:** `LedgerEntry.accountHead` uses a string like `"Party: ${party.name}"` to link entries to parties.
- **Risk:** Renaming a party orphans its historical ledger data.
- **Action:** Switch to ID-based (UUID) joins for ledger attribution.

## 5. Performance Bottlenecks
- **Issue:** Full recalculation of outstandings, stocks, and analytics on every voucher save.
- **Risk:** UI lag as data volume increases (linear scaling issue).
- **Action:** Implement incremental/delta updates for cached columns.

## 6. Redundant Storage Systems
- **Issue:** Email reminder schedules are stored in both `SharedPreferences` and a Room SQL table.
- **Risk:** State desynchronization.
- **Action:** Consolidate onto a single source of truth (preferably Room).

## 7. Configuration Mismatch
- **Issue:** `Party.creditDays` field exists but is ignored in favor of a hardcoded 15-day default in `saveAndPostVoucher`.
- **Action:** Wire `creditDays` into the bill-receivable calculation.
