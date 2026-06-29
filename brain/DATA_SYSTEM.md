# DATA_SYSTEM - ZeroBook

## Database: Room (SQLite)
**Database Name:** `ZeroBook.db`
**Current Version:** 13

### Core Entities (`Entities.kt`):
- **`Voucher`**: Header for all transactions (SALE, PURCHASE, etc.).
- **`VoucherItem`**: Line items for vouchers, linking products to transactions.
- **`Party`**: Customers and Suppliers.
- **`Product`**: Inventory items.
- **`LedgerEntry`**: Double-entry accounting rows.
- **`FinancialYear`**: April-March period definition.
- **`BusinessProfile`**: Singleton row for business identity and SMTP settings.
- **`BillReceivable`**: Tracking for credit sales and due dates.
- **`BankCashTransaction`**: Cash/Bank book entries.

## Data Flow & Processing
- **Double Entry Logic:** Hardcoded in `AppRepository.saveAndPostVoucher`. POSTED vouchers are expanded into multiple `LedgerEntry` rows.
- **Stock Recalculation:** `current_stock` is a cached column in the `Product` table. It is recomputed by walking the `VoucherItem` table for the current financial year whenever a voucher is saved or deleted.
- **Opening Balances:** Managed via `*FinancialYearBalance` tables. Populated during the "Close Financial Year" process to avoid re-summing all historical data.

## Models
- **Enums:** Voucher types (SALE, PURCHASE, etc.) are handled as String enums.
- **UUIDs:** Used for Primary Keys to ensure future sync compatibility.

## Storage
- **Room:** Main structured data.
- **DataStore:** Simple flags (e.g., last seen changelog version).
- **SharedPreferences:** Email reminder templates and logs (`"zerobook_pref"`).

## API Communication
- **Retrofit/Moshi:** Scaffolding exists in dependencies but no live API services are currently implemented in the main logic flow.
- **Future:** Planned GST portal and HSN lookup integrations.
