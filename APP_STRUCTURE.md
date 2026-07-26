# ZeroBook - Complete App Structure Document

## Project Overview

| Property | Value |
|----------|-------|
| **App Name** | ZeroBook |
| **Package** | `com.zerobook.app` |
| **Version** | 2.2.1 (versionCode 4) |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 35 |
| **Compile SDK** | 36 |
| **Language** | Kotlin (100%) |
| **UI Framework** | Jetpack Compose (Material3) |
| **Architecture** | Single-Activity, Navigation Compose |
| **Database** | Room (SQLite) + DataStore Preferences |

---

## Architecture Summary

ZeroBook is a **100% Jetpack Compose** accounting/bookkeeping application. It uses:

- **1 Activity** (`MainActivity`) as the sole entry point and navigation host
- **0 Fragments** -- all screens are `@Composable` functions
- **0 XML Layouts** -- all UI is built declaratively with Compose
- **0 Navigation XML graphs** -- navigation is in-code via `NavHost` + `composable()` DSL
- **0 Menu XML files** -- menus are Compose `DropdownMenu` / `TopAppBar` components
- **3 ViewModels** (`AppViewModel`, `DashboardViewModel`, `ThemeViewModel`)
- **23 Compose screen files** with 100+ `@Composable` function definitions

---

## Tech Stack & Key Dependencies

| Category | Libraries |
|----------|-----------|
| **Compose BOM** | `androidx.compose.bom` |
| **UI** | `compose.material3`, `compose.ui`, `compose.ui.graphics` |
| **Icons** | `compose.material.icons.core`, `compose.material.icons.extended` |
| **Navigation** | `androidx.navigation.compose` |
| **Activity** | `androidx.activity.compose` |
| **Lifecycle** | `lifecycle.runtime.compose`, `lifecycle.viewmodel.compose` |
| **Room** | `room.ktx`, `room.runtime`, `room.compiler` (KSP) |
| **DataStore** | `datastore.preferences` |
| **Splash** | `androidx.core:core-splashscreen:1.0.1` |
| **WorkManager** | `work-runtime-ktx:2.10.1` |
| **CameraX** | `camera2`, `camera.core`, `camera.lifecycle`, `camera.view` |
| **ML Kit** | `play-services.mlkit.text.recognition`, `play-services.mlkit.barcode.scanning` |
| **Location** | `play-services-location` |
| **Image Loading** | `coil.compose` |
| **Email** | `com.sun.mail:android-mail:1.6.7` |
| **Google Auth** | `play-services-auth:21.3.0` |
| **Security** | `security-crypto:1.1.0-alpha06` |
| **Permissions** | `accompanist.permissions` |
| **Coroutines** | `kotlinx.coroutines.android`, `kotlinx.coroutines.core` |

---

## Complete Screen Flow

```
App Launch
    │
    ▼
┌─────────────────────────┐
│   AndroidX Splash API   │  (System splash with app theme)
│   installSplashScreen() │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│   Database Init State   │  Loading → Success / Error
└───────────┬─────────────┘
            │
            ▼
    ┌───────────────┐
    │ Setup Complete?│
    └───┬───────┬───┘
        │       │
       YES      NO
        │       │
        │       ▼
        │  ┌─────────────────────┐
        │  │    SetupScreen      │  First-time business setup wizard
        │  │  (954 lines)        │
        │  └──────────┬──────────┘
        │             │
        ▼             ▼
    ┌───────────────┐
    │  PIN Enabled? │
    └───┬───────┬───┘
        │       │
       YES      NO
        │       │
        ▼       │
  ┌─────────────────┐
  │  PinLockScreen  │  4-digit numeric keypad
  │  (inline)       │
  └───────┬─────────┘
          │
          ▼
┌─────────────────────────┐
│   In-App SplashScreen   │  Animated logo (900ms timeout)
│   (SplashScreen.kt)     │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────┐
│                   DASHBOARD SCREEN                      │
│                  (START DESTINATION)                     │
│                                                         │
│  Bottom Navigation: Dashboard | Vouchers | Parties |    │
│                     Settings                            │
└──────┬──────────┬──────────┬──────────┬─────────────────┘
       │          │          │          │
       ▼          ▼          ▼          ▼
   ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
   │Vouchers│ │Parties │ │Settings│ │Reports │
   │ Screen │ │ Screen │ │ Screen │ │ Screen │
   └───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘
       │          │          │          │
       ▼          ▼          ▼          ▼
   Sub-screens  Sub-screens Sub-screens Sub-reports
```

---

## All Screens - Detailed Breakdown

### SCREEN 0: Splash Screen (In-App)

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/SplashScreen.kt` |
| **Lines** | 68 |
| **Route** | N/A (shown before navigation initializes) |
| **Function** | `SplashScreen(onTimeout: () -> Unit)` |

**Description:** A brief 900ms animated splash that displays the `logo_transparent` drawable centered on screen with scale and fade animations. Calls `onTimeout()` after 900ms to proceed to the main app. This is a second splash after the AndroidX system splash screen.

---

### SCREEN 1: Setup Screen (First-Time Onboarding)

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/SetupScreen.kt` |
| **Lines** | 954 |
| **Route** | N/A (shown conditionally before navigation) |
| **Function** | `SetupScreen(viewModel, onSetupComplete)` |

**Description:** A scrollable business profile setup form shown on first launch when `isSetupCompleted` is false. This is a comprehensive wizard that collects all business information needed to operate the accounting system.

**Fields collected:**
- Business name (required)
- Owner name (required)
- Address
- City
- PIN code (with auto-lookup via Indian PIN API)
- State (dropdown of Indian states with GST codes)
- Phone number
- Email address
- GSTIN (with validation)
- PAN
- Bank name
- Account number
- IFSC code (with API verification)
- Branch name

**Features:**
- PIN code auto-lookup fetches city and state automatically
- GSTIN validation
- IFSC verification via API
- Location-based auto-detection of state using FusedLocationProviderClient
- Option to load sample/demo data
- Requests location permissions for auto-detection

**Navigation:** After completion, proceeds to Dashboard.

---

### SCREEN 2: PIN Lock Screen

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/MainActivity.kt` (line 656) |
| **Lines** | Inline in MainActivity |
| **Route** | N/A (shown conditionally before navigation) |
| **Function** | `PinLockScreen(correctPin, onAuthentic)` |

**Description:** A 4-digit PIN lock overlay that blocks access to the app when PIN lock is enabled. Features a numeric keypad (1-9, CLR, 0, OK) with 4 dot indicators for entered digits.

**Features:**
- Auto-unlocks on correct 4-digit entry
- Error message display for wrong PIN
- CLR button to clear entered digits
- Blocks all app access until correct PIN is entered
- PIN stored encrypted via DataStore preferences

---

### SCREEN 3: Dashboard Screen (Home)

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/DashboardScreen.kt` |
| **Lines** | 1,836 |
| **Route** | `dashboard` (START DESTINATION) |
| **Function** | `DashboardScreen(viewModel, dashboardViewModel, isDesktop, onQuickAction)` |

**Description:** The main home screen and start destination of the navigation graph. This is the most feature-rich screen, providing a complete business overview.

**Sections:**

1. **Header** -- Business profile name, logo, financial year label, GSTIN

2. **KPI Cards** (with 3 animation modes):
   - Today's Sales
   - Today's Purchases
   - This Month's Sales
   - Net Profit
   - GST Value

3. **Balance Snapshot:**
   - Cash Balance
   - Bank Balance
   - Outstanding Receivables
   - Outstanding Payables

4. **Progress Tracker** (configurable):
   - Target amount vs actual
   - Metric selection (Sales/Purchases/Net Profit)
   - Period selection (Monthly/Weekly/Daily/Quarterly/Yearly)

5. **Low Stock Alerts**

6. **Quick Action Buttons:**
   - New Sale / New Purchase
   - Receipt / Payment
   - Reports
   - Quick Sale
   - Expenses
   - Party
   - Vouchers

7. **Recent Transactions List** with filtering:
   - All, Sales, Purchase, Receipt, Payment, Income, Expense, Receivable, Payable, Due, Cancelled, Draft, GST

8. **Search** across vouchers, ledger entries, and products

9. **Analytics** with time period filter:
   - Today, This Week, This Month, This Quarter, This Year, Custom Date, Custom Date Range

10. **Charts:** Line, Bar, Pie

**Bottom Navigation Bar:** Dashboard | Vouchers | Parties | Settings

---

### SCREEN 4: Vouchers Screen

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/VouchersScreen.kt` |
| **Lines** | 5,650 (largest file in the project) |
| **Route** | `vouchers` (top-level tab) |
| **Function** | `VouchersScreen(viewModel, isDesktop, navigateToNewVoucher, navigateToInvoice)` |

**Description:** Full voucher management screen with list, filter, search, sort, and inline invoice preview capabilities.

**Supported Voucher Types (15):**
1. Sale
2. Purchase
3. Receipt
4. Payment
5. Journal
6. Sales Return
7. Purchase Return
8. Bills Receivable
9. Bills Payable
10. Debit Note
11. Credit Note
12. Quotation
13. Delivery Challan
14. Income
15. Expense

**Features:**
- Grid/list view of all vouchers
- Filter chips for each voucher type
- Sort options: Default, Newest, Oldest, Highest Amount, Lowest Amount
- Search bar
- Each voucher card shows: type, party name, amount, date, outstanding balance, status
- Inline PDF preview/WebView preview of invoices
- Attachment support (file picker, OCR bill extraction via MLKit)
- Multi-select mode with bulk delete

**Sub-screens accessed from here:**
- `NewVoucherScreen` (create/edit)
- `InvoiceScreen` (view invoice)

---

### SCREEN 5: New Voucher Screen (Create/Edit)

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/VouchersScreen.kt` (line 1517) |
| **Lines** | Part of VouchersScreen.kt |
| **Route** | `new_voucher?voucherId={voucherId}` |
| **Function** | `NewVoucherScreen(viewModel, voucherId, isDesktop, onNavigateBack, onNavigateToInvoice, onNavigateToPartyDetail)` |

**Description:** Multi-step voucher creation/editing form. Supports all 15 voucher types with context-specific fields.

**Step 1: Voucher Type Selection**
- Visual cards for all 15 voucher types

**Step 2: Voucher Form**
- Voucher number (auto-generated)
- Date picker
- Party selection (Customer/Supplier)
- Payment mode
- Line items (via VoucherItemEntrySheet bottom sheet)
- Additional charges
- CGST/SGST/IGST calculations (auto-computed)
- Narration
- Bank details
- Cheque details
- Transport details
- Attachment/file upload
- OCR bill scanning (MLKit text recognition)
- Journal entry rows (for Journal type)
- Return reason, source voucher (for return types)
- Credit days, partial payment, advance receipt options

**Interactions:**
- On save, navigates to InvoiceScreen or back
- Can create a sale from an existing purchase voucher
- Party selection navigates to PartyDetailScreen

---

### SCREEN 6: Voucher Item Entry Sheet (Bottom Sheet)

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/VoucherItemSheet.kt` |
| **Lines** | 572 |
| **Route** | ModalBottomSheet (within NewVoucherScreen) |
| **Function** | `VoucherItemEntrySheet(products, existingItem, isPurchase, hasGst, globalGstEnabled, globalGstRate, isInterstate, preselectedProduct, onDismiss, onSave, onCreateProduct, onProductConsumed)` |

**Description:** A `ModalBottomSheet` for adding/editing a single line item within a voucher.

**Fields:**
- Product search/selection from existing products
- Barcode scanner integration (opens BarcodeScannerDialog)
- HSN code (with auto-suggestion from HsnLookup)
- Quantity
- Unit (PCS, KG, LTR, MTR, BOX, BAG, NOS)
- Rate
- Discount (percentage or flat)
- GST rate (0%, 5%, 12%, 18%, 28%)
- Taxable amount and total (auto-calculated)

---

### SCREEN 7: Invoice Screen

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/InvoiceScreen.kt` |
| **Lines** | 396 |
| **Route** | `invoice/{voucherId}` |
| **Function** | `InvoiceScreen(viewModel, voucherId, onNavigateBack, onEditVoucher, onCreateSaleFromVoucher)` |

**Description:** Invoice viewer and action screen. Renders the full HTML invoice in a WebView.

**Display:**
- Full HTML invoice rendered in WebView
- Invoice number in the top bar
- PDF generation and preview

**Actions:**
- Edit voucher (navigates back to NewVoucherScreen in edit mode)
- Create sale from voucher (for purchase vouchers)
- Print (Android PrintManager)
- Share PDF (generic share + WhatsApp-specific)
- Download/Save PDF to device storage
- Refresh invoice

---

### SCREEN 8: Parties Screen

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/PartiesScreen.kt` |
| **Lines** | 1,340 |
| **Route** | `parties` (top-level tab) |
| **Function** | `PartiesScreen(viewModel, isDesktop, onPartySelected)` |

**Description:** Customer and supplier management screen.

**Features:**
- List of all parties (customers and suppliers)
- Search by name, phone, GSTIN
- Filter: ALL, CUSTOMER, SUPPLIER
- Each party card shows: name, type, phone, outstanding balance (DR/CR)
- Multi-select mode with bulk delete
- FAB to add new party (opens CreatePartyInlineSheet)
- Inline edit option

**Sub-screens accessed from here:**
- `PartyDetailScreen` (view party ledger)
- `CreatePartyInlineSheet` (add party)

---

### SCREEN 9: Party Detail Screen

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/PartiesScreen.kt` (line 1088) |
| **Lines** | Part of PartiesScreen.kt |
| **Route** | `party_detail/{partyId}` |
| **Function** | `PartyDetailScreen(viewModel, partyId, onNavigateBack)` |

**Description:** Detailed view of a single party with their complete ledger statement.

**Display:**
- Party details card: phone, email, GSTIN, state
- Outstanding balance (DR/CR) with color coding
- Full ledger statement (general ledger) in reverse chronological order:
  - Date, voucher type, voucher number, debit, credit, running balance

**Actions:**
- Payment reminder email button (pre-filled with bank details)
- Call party button (opens phone dialer)
- WhatsApp party button (opens WhatsApp)
- Outstanding receivable/payable summary

---

### SCREEN 10: Create Party Bottom Sheet

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/PartySheets.kt` |
| **Lines** | 519 |
| **Route** | ModalBottomSheet (within PartiesScreen) |
| **Function** | `CreatePartyInlineSheet(partyType, onSave, onDismiss)` |

**Description:** `ModalBottomSheet` form for creating a new party.

**Fields:**
- Name (required)
- Party Type (Customer / Supplier / Both)
- Phone
- Email
- GSTIN (with validation)
- PAN
- Address
- City
- State (with PIN auto-lookup)
- PIN code
- Opening balance
- Balance type (DR/CR)
- Credit limit
- Credit days
- Notes
- Profile image/picture picker

---

### SCREEN 11: Settings Screen

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/SettingsScreen.kt` |
| **Lines** | 2,128 |
| **Route** | `settings` (top-level tab) |
| **Function** | `SettingsScreen(viewModel, themeViewModel, isDesktop, navigateToProducts, navigateToLedgerBooks, onNavigateBack)` |

**Description:** Menu-based settings screen with sub-sections and navigation to sub-screens.

**Menu Items:**
1. **Business Profile** -- navigates to BusinessProfileSettingsSection
2. **Products** -- navigates to ProductsScreen
3. **Ledger Books** -- navigates to LedgerListScreen
4. **Customize** -- navigates to ProgressTrackerSettingsScreen
5. **Email Automation** -- embedded EmailAutomationSection
6. **Theme / Dark Mode** toggle
7. **Financial Year** selection
8. **CSV Export** (all vouchers, parties, products, ledger)
9. **CSV Import**
10. **PIN Lock** configuration
11. **Changelog** ("What's New" dialog)
12. **App version** display

---

### SCREEN 12: Business Profile Settings Section

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/BusinessProfileSettingsSection.kt` |
| **Lines** | 738 |
| **Route** | Sub-screen within Settings |
| **Function** | `BusinessProfileSettingsSection(viewModel, profile, isDesktop, onBackToMenu)` |

**Description:** Full business profile editor form. Same fields as SetupScreen but for editing an existing profile.

**Fields:**
- Business name, owner name
- Address, city, state, PIN (with auto-lookup)
- Phone, email
- GSTIN (with validation), PAN
- Bank name, account number, IFSC (with verification), branch
- Logo upload
- Signature upload
- Terms & conditions text editor

---

### SCREEN 13: Progress Tracker / Customize Settings

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/SettingsScreen.kt` (line 1876) |
| **Lines** | Part of SettingsScreen.kt |
| **Route** | Sub-screen within Settings |
| **Function** | `ProgressTrackerSettingsScreen(onBackToMenu)` |

**Description:** Dashboard customization screen.

**Settings:**
- KPI Animation Mode: Standard Horizontal, Wallet Stack, Spotlight Carousel
- Progress Tracker toggle (show/hide)
- Metric selection: Sales, Purchases, Net Profit
- Period selection: Monthly, Weekly, Daily, Quarterly, Yearly
- Target amount input

All preferences saved via AppPreferences (DataStore).

---

### SCREEN 14: Email Automation Section

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/EmailAutomationSection.kt` |
| **Lines** | 384 |
| **Route** | Embedded within SettingsScreen |
| **Function** | `EmailAutomationSection(viewModel)` |

**Description:** Email automation settings for payment reminders.

**Features:**
- Gmail account connection via Google Sign-In
- Automation enable/disable toggle
- Email automation rules (for payment reminders)
- Email history log
- Consent dialog for email automation
- Missing email warnings for parties with outstanding balances

---

### SCREEN 15: Products Screen

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/ProductsScreen.kt` |
| **Lines** | 840 |
| **Route** | `products` |
| **Function** | `ProductsScreen(viewModel, onNavigateBack)` |

**Description:** Product management screen with search, filter, and CRUD operations.

**Features:**
- List of all products with search (by name, HSN, barcode)
- Each product shows: name, HSN, sale rate, current stock, low stock alert status
- Multi-select mode with bulk delete
- FAB to add new product (opens ProductEditorScreen)
- Low stock count indicator

---

### SCREEN 16: Product Editor Screen

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/ProductsScreen.kt` (line 484) |
| **Lines** | Part of ProductsScreen.kt |
| **Route** | Sub-screen within Products |
| **Function** | `ProductEditorScreen(viewModel, mode, existingProduct, onDismiss)` |

**Description:** Full-screen form for adding/editing a product.

**Fields:**
- Product name (required)
- HSN code (with search/lookup)
- Unit (PCS, KG, GM, MG, LTR, ML, BOX, BAG, NOS, MTR)
- Sale rate
- Purchase rate
- GST rate (0%, 5%, 12%, 18%, 28%)
- Opening stock
- Current stock
- Low stock threshold with alert toggle
- Barcode value (with barcode scanner)

**Optional Fields (via ProductOptionalFields):**
- Secondary unit, conversion factor
- Batch number toggle
- Expiry date toggle
- Serial number tracking toggle

---

### SCREEN 17: Bank & Cash Screen

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/BankCashScreen.kt` |
| **Lines** | 462 |
| **Route** | `bank_cash` |
| **Function** | `BankCashScreen(viewModel, onNavigateBack)` |

**Description:** Bank and cash register management screen.

**Display:**
- Balance summary card: Cash Balance + Bank Balance (with color coding)
- Tab switcher: CASH / BANK
- Transaction log (LazyColumn):
  - Each shows: type, amount, party, date, payment mode

**Actions:**
- FAB to add new transaction (opens AddTransactionForm)

---

### SCREEN 18: Expenses Screen

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/ExpensesScreen.kt` |
| **Lines** | 287 |
| **Route** | `expenses` |
| **Function** | `ExpensesScreen(viewModel, onNavigateBack)` |

**Description:** Expense tracking screen with categories and export.

**Categories:**
Rent, Electricity, Water, Telephone, Internet, Staff Salary, Transport, Packaging, Maintenance, Office Supplies, Advertising, Insurance, Bank Charges, Miscellaneous, Other

**Features:**
- Filter by category
- FAB to add new expense (opens ExpenseEntryScreen bottom form)
- Export to CSV button
- Each expense card shows: category, description, date, payment mode, amount, reference

---

### SCREEN 19: Income Screen

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/IncomeScreen.kt` |
| **Lines** | 286 |
| **Route** | Not in active navigation routes (available in code) |
| **Function** | `IncomeScreen(viewModel, onNavigateBack)` |

**Description:** Income tracking screen for non-voucher income.

**Categories:**
Sales Commission, Interest Received, Rental Income, Freelance Services, Consulting Fees, Investment Returns, Grants, Donations, Refunds Received, Other Income

**Features:**
- Filter by category
- FAB to add new income (opens IncomeEntryScreen bottom form)
- Export to CSV button
- Each income card shows: category, description, date, payment mode, amount, reference

---

### SCREEN 20: Quick Sale Screen

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/QuickSaleScreen.kt` |
| **Lines** | 268 |
| **Route** | `quick_sale` |
| **Function** | `QuickSaleScreen(viewModel, onNavigateBack)` |

**Description:** Simplified point-of-sale screen for fast transactions.

**Features:**
- Walk-in Customer toggle (if off, shows customer selection chips)
- Product search and filter by unit type
- Product list (name, price, stock)
- Shopping cart with quantity adjustment and running total
- Payment mode selection: CASH, UPI, CARD
- Total display with "Charge & Save" button
- Creates a SALE voucher directly

---

### SCREEN 21: Reports Screen

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/ReportsScreen.kt` |
| **Lines** | 2,092 |
| **Route** | `reports` |
| **Function** | `ReportsScreen(viewModel, isDesktop, navigateToLedgerBooks, navigateToExpenses, navigateToNewVoucher, onNavigateBack)` |

**Description:** Comprehensive regulatory financial reports screen with 7 sub-reports.

**Sub-Reports:**

1. **Trial Balance** -- Interactive Net Debits & Credits verification
2. **Profit & Loss Account** -- Gross Margin & Net P&L side-by-side
3. **Balance Sheet** -- Real assets, liabilities & equity distribution
4. **GST Summary Status** -- Tax output liability vs Input credits (CGST/SGST/IGST)
5. **Outstanding Receivables** -- Customer aging list & alerts
6. **Outstanding Payables** -- Supplier credit balances
7. **Stock Report** -- Current stock, low stock, out-of-stock items

**Data Computed:**
- Sales revenue, purchases cost
- Current stock value
- GST payable/receivable
- Debtors/creditors lists
- Gross/net profit
- Assets/liabilities
- Capital/equity balancing

Desktop layout has left menu + right detail pane.

---

### SCREEN 22: Stock Report Screen

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/StockReportScreen.kt` |
| **Lines** | 169 |
| **Route** | Embedded within ReportsScreen |
| **Function** | `StockReportScreen(products)` |

**Description:** Stock report sub-screen within Reports.

**Features:**
- Search by product name or HSN code
- Filter: ALL, LOW STOCK, OUT OF STOCK
- Each product card shows: name, HSN, current stock + unit, low stock threshold
- Status chip with color coding: In Stock (green), Low Stock (orange), Out of Stock (red)
- Export to CSV button

---

### SCREEN 23: Ledger List Screen

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/LedgerListScreen.kt` |
| **Lines** | 426 |
| **Route** | `ledger_books` |
| **Function** | `LedgerListScreen(viewModel, onNavigateBack)` |

**Description:** Full chart of accounts with balances.

**Display:**
- Accounts grouped by group name
- Each account row shows: name, opening balance, current balance (DR/CR)
- Section headers for account groups
- Tap to view details (AlertDialog): group, opening balance, balance type, phone, email, address
- Edit capability via EditLedgerSheet

---

### SCREEN 24: Barcode Scanner Dialog

| Property | Detail |
|----------|--------|
| **File** | `app/src/main/java/com/zerobook/app/ui/screens/BarcodeScannerDialog.kt` |
| **Lines** | 137 |
| **Route** | AlertDialog (shown from VoucherItemEntrySheet, ProductEditorScreen) |
| **Function** | `BarcodeScannerDialog(onDismiss, onScanned)` |

**Description:** CameraX + MLKit barcode/QR scanner dialog.

**Features:**
- Camera permission request
- Live camera preview in the dialog
- White rectangle guide overlay
- Automatic barcode detection and callback
- Cancel button

---

## Navigation Routes Summary

### Top-Level Routes (Bottom Navigation)

| Route | Screen | Icon |
|-------|--------|------|
| `dashboard` | DashboardScreen | GridView |
| `vouchers` | VouchersScreen | Assignment |
| `parties` | PartiesScreen | Group |
| `settings` | SettingsScreen | Settings |

### Secondary Routes

| Route | Screen | Access From |
|-------|--------|-------------|
| `reports` | ReportsScreen | Dashboard quick action |
| `products` | ProductsScreen | Settings menu |
| `bank_cash` | BankCashScreen | Dashboard quick action |
| `expenses` | ExpensesScreen | Dashboard quick action |
| `quick_sale` | QuickSaleScreen | Dashboard quick action |
| `new_voucher?voucherId={id}` | NewVoucherScreen | VouchersScreen, Dashboard |
| `invoice/{voucherId}` | InvoiceScreen | NewVoucherScreen, VouchersScreen |
| `ledger_books` | LedgerListScreen | Settings menu, Reports |
| `party_detail/{partyId}` | PartyDetailScreen | PartiesScreen |

### Bottom Sheets / Dialogs

| Component | Parent Screen |
|-----------|---------------|
| VoucherItemEntrySheet | NewVoucherScreen |
| CreatePartyInlineSheet | PartiesScreen |
| BarcodeScannerDialog | VoucherItemEntrySheet, ProductEditorScreen |
| PinLockScreen | MainActivity (conditional) |
| ChangelogDialog | MainActivity (on version upgrade) |

---

## Data Layer Structure

### Room Database Entities

| Entity | Purpose |
|--------|---------|
| `Party` | Customer/Supplier master |
| `Product` | Product/Item master |
| `Voucher` | All voucher types (sale, purchase, receipt, etc.) |
| `VoucherItem` | Line items within vouchers |
| `LedgerEntry` | Double-entry ledger postings |
| `Expense` | Expense records |
| `Income` | Income records |
| `BankCashTransaction` | Bank/Cash register entries |

### ViewModels

| ViewModel | Scope | Purpose |
|-----------|-------|---------|
| `AppViewModel` | Activity-level | Database init, CRUD for all entities, voucher save logic, CSV export, party/product/voucher flows |
| `DashboardViewModel` | Screen-level | Header state (FY label, business name, GSTIN), KPI animation mode preference |
| `ThemeViewModel` | Activity-level | Theme management, current theme state, theme switching, status bar color control |

### Key Services

| Service | Purpose |
|---------|---------|
| `InvoiceGenerator` | Generates HTML invoice for WebView rendering |
| `EmailComposer` | SMTP email sending for payment reminders |
| `EmailAutomationService` | Gmail OAuth2 integration, automated email rules |
| `ExportStorageManager` | File export (PDF, CSV) to device storage |
| `CsvTransferManager` | CSV import/export for data migration |
| `WebViewPdfWriter` | Java bridge for WebView-to-PDF rendering |

---

## UI Support Infrastructure

### Animations
- `PremiumAnimations.kt` -- Animated counters, floating card effects, transaction animations, button press depth
- `PremiumMotion.kt` -- Navigation transitions, press scale effects, reduced-motion support, bottom nav content

### Selection System
- `UniversalSelectionController.kt` -- Multi-selection state management (toggle, selectAll, exitSelection)
- `UniversalSelectionComponents.kt` -- Selection UI: top app bar, indicator circle, selection-aware top bar

### Theme System (13 files)
- `Theme.kt` -- ZeroBookTheme composable wrapper
- `ThemeConfig.kt` -- Theme data classes, ThemeViewModel, theme presets
- `Colors.kt`, `Color.kt` -- Color palette definitions
- `AppColors.kt` -- Current theme-aware color provider
- `Type.kt` -- Typography definitions
- `ThemeHelpers.kt` -- Theme utility functions
- `ThemeAwareComponents.kt` -- Theme-aware reusable composables
- `Skeleton.kt` -- Loading skeleton shimmer components
- `PremiumThemeConfig.kt` -- Premium theme configuration
- `InputColors.kt` -- Input field color schemes
- `GstinHelpers.kt` -- GSTIN validation and parsing
- `GlobalStyles.kt` -- Global style constants

### Shared Components
- `SharedComponents.kt` -- `RetailTextField`, `StateDropdownMenu`, `fetchPinLookup()` API helper
- `ProfileFormSupport.kt` -- Business profile helpers, required field validation, Indian state resolution, GPS reverse geocoding
- `ProductOptionalFields.kt` -- Product optional fields UI: HSN, batch number, expiry date, serial number toggles

---

## Complete File Inventory

### Source Files (Kotlin)

| Category | Count | Files |
|----------|-------|-------|
| Activity | 1 | `MainActivity.kt` |
| Compose Screens | 23 | SplashScreen, SetupScreen, DashboardScreen, VouchersScreen, PartiesScreen, SettingsScreen, ReportsScreen, ProductsScreen, BankCashScreen, ExpensesScreen, IncomeScreen, QuickSaleScreen, InvoiceScreen, LedgerListScreen, StockReportScreen, BarcodeScannerDialog, EmailAutomationSection, BusinessProfileSettingsSection, PartySheets, VoucherItemSheet, SharedComponents, ProfileFormSupport, ProductOptionalFields |
| ViewModels | 3 | AppViewModel, DashboardViewModel, ThemeViewModel |
| Data Layer | 13 | AppDatabase, AppDaos, AppRepository, Entities, AppPreferences, InvoiceDefaults, HsnLookup, FinancialYearUtils, VoucherExtras, Migrations, EmailReminderScheduler, ChangelogLoader, Utils |
| Services | 6 | InvoiceGenerator, EmailComposer, EmailAutomationService, ExportStorageManager, CsvTransferManager, WebViewPdfWriter |
| Animations | 2 | PremiumAnimations, PremiumMotion |
| Selection | 2 | UniversalSelectionController, UniversalSelectionComponents |
| Theme | 13 | Theme, ThemeConfig, Colors, Color, AppColors, Type, ThemeHelpers, ThemeAwareComponents, Skeleton, PremiumThemeConfig, InputColors, GstinHelpers, GlobalStyles |
| Utilities | 2 | FilePicker, PrintResultCallbackBridge |

### Resource Files (XML)

| Category | Files |
|----------|-------|
| Values | strings.xml, colors.xml, themes.xml, ic_launcher_background.xml |
| XML Config | provider_paths.xml, data_extraction_rules.xml, backup_rules.xml |
| Drawable | logo_transparent.png, logo_icon.png, logo_mark.xml, ic_launcher_foreground.xml, ic_launcher_background.xml |
| Mipmap | ic_launcher.xml, ic_launcher_round.xml (across density buckets) |

---

## Summary

ZeroBook is a complete, production-grade Indian accounting application with:

- **1 Single Activity** architecture
- **24 distinct user-facing screens** (including sub-screens, bottom sheets, and dialogs)
- **15 voucher types** supported
- **7 financial reports** (Trial Balance, P&L, Balance Sheet, GST Summary, Receivables, Payables, Stock)
- **Full double-entry accounting** with ledger books
- **OCR bill scanning** via CameraX + MLKit
- **Barcode scanning** for product lookup
- **Email automation** with Gmail OAuth
- **PDF generation and sharing** (print, WhatsApp, download)
- **CSV import/export** for data migration
- **PIN lock security** with encrypted storage
- **Multi-theme support** (light/dark/custom)
- **Responsive layout** with desktop mode support
- **100% Jetpack Compose** -- zero XML layouts, zero Fragments
