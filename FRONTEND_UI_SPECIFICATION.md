# Frontend UI Specification Document

**Application:** ZeroBook  
**Package:** com.zerobook.app  
**Platform:** Android  
**Version Documented:** 2.2.1+ (latest changelog: 2.2.3)  
**UI Framework:** Jetpack Compose (Material 3)  
**Layout:** Single-Activity, Single-Column (with tablet split-pane support)  

---

## Table of Contents

1. [Application Entry Flow](#1-application-entry-flow)
2. [Splash Screen](#2-splash-screen)
3. [Setup Screen](#3-setup-screen)
4. [PIN Lock Screen](#4-pin-lock-screen)
5. [Changelog Dialog](#5-changelog-dialog)
6. [Main Application Shell](#6-main-application-shell)
7. [Bottom Navigation Bar](#7-bottom-navigation-bar)
8. [Dashboard Screen](#8-dashboard-screen)
9. [Vouchers Screen — List View](#9-vouchers-screen--list-view)
10. [Voucher Type Selection](#10-voucher-type-selection)
11. [Voucher Entry Form](#11-voucher-entry-form)
12. [Voucher Item Entry Sheet](#12-voucher-item-entry-sheet)
13. [Voucher Filter & Sort Sheets](#13-voucher-filter--sort-sheets)
14. [Parties Screen](#14-parties-screen)
15. [Party Add/Edit Form](#15-party-addedit-form)
16. [Party Detail Screen](#16-party-detail-screen)
17. [Settings Screen](#17-settings-screen)
18. [Business Profile Settings](#18-business-profile-settings)
19. [Theme & Colors Settings](#19-theme--colors-settings)
20. [Customize Settings](#20-customize-settings)
21. [Financial Year Settings](#21-financial-year-settings)
22. [PIN Protection Settings](#22-pin-protection-settings)
23. [About Settings](#23-about-settings)
24. [Email Automation Settings](#24-email-automation-settings)
25. [Reports Screen](#25-reports-screen)
26. [Trial Balance Report](#26-trial-balance-report)
27. [Trading & Profit/Loss Report](#27-trading--profitloss-report)
28. [Balance Sheet Report](#28-balance-sheet-report)
29. [GST Summary Report](#29-gst-summary-report)
30. [Outstanding Receivables Report](#30-outstanding-receivables-report)
31. [Outstanding Payables Report](#31-outstanding-payables-report)
32. [Stock Report Screen](#32-stock-report-screen)
33. [Products Screen](#33-products-screen)
34. [Product Editor Screen](#34-product-editor-screen)
35. [Bank & Cash Register Screen](#35-bank--cash-register-screen)
36. [Manual Transaction Form](#36-manual-transaction-form)
37. [Expenses Screen](#37-expenses-screen)
38. [Expense Entry Form](#38-expense-entry-form)
39. [Income Screen](#39-income-screen)
40. [Income Entry Form](#40-income-entry-form)
41. [Quick Sale Screen](#41-quick-sale-screen)
42. [Invoice Viewer Screen](#42-invoice-viewer-screen)
43. [Ledger Books Screen](#43-ledger-books-screen)
44. [Barcode Scanner Dialog](#44-barcode-scanner-dialog)
45. [Parsed Bill Items Dialog](#45-parsed-bill-items-dialog)
46. [Universal Selection System](#46-universal-selection-system)
47. [Theme System](#47-theme-system)
48. [Animation & Motion System](#48-animation--motion-system)
49. [Navigation Map](#49-navigation-map)
50. [Responsive Layout Rules](#50-responsive-layout-rules)

---

## 1. Application Entry Flow

The application follows a strict sequential entry pipeline:

### Stage 1: System Splash Screen
- **Type:** AndroidX SplashScreen API (system-level, non-Compose)
- **Background:** `#FAF8F5` (warm off-white)
- **Icon:** `@drawable/logo_transparent` (transparent PNG logo, centered)
- **Icon background:** Transparent
- **Behavior:** Dismisses automatically once Activity initialization completes

### Stage 2: Database Initialization
- **Background:** `AppColors.screenBg` (theme-dependent warm off-white)
- **Loading state:** Centered `CircularProgressIndicator` (blue `#1A73E8`) with text `"Initializing Secure Database..."` (14sp, medium weight, dark color)
- **Error state:** Centered white card (16dp rounded corners, 24dp padding) containing:
  - Title: `"Database Connection Failed"` (bold, 18sp, red `#DC3545`)
  - Error message text (14sp, gray `#555555`)
  - Blue button: `"Retry Connection"` (8dp rounded) — recreates the Activity
- **Success:** Proceeds to Stage 3

### Stage 3: In-App Splash Screen
- **Type:** Compose `Crossfade` transition (240ms, `FastOutSlowInEasing`)
- Displays the `SplashScreen` composable (see Section 2)
- After timeout, crossfades to main content

### Stage 4: Setup Status + PIN Check
- **Loading state:** Centered `CircularProgressIndicator` with text `"Preparing ZeroBook..."`
- **If setup not completed:** Shows `SetupScreen` (Section 3)
- **If PIN required and not authenticated:** Shows `PinLockScreen` (Section 4)
- **Otherwise:** Shows main application shell

### Stage 5: Changelog Dialog
- Displayed after setup and PIN are satisfied
- Only shown when a new version has been released since last seen
- See Section 5 for details

---

## 2. Splash Screen

**Route:** None (pre-navigation)  
**Entry point:** Database init success → automatic  
**Exit point:** Auto-advances after 900ms delay

### Layout
- **Root:** Full-screen `Box`, centered content
- **Background:** `AppColors.screenBg` (default: warm off-white `#FAF8F5`)

### Visible UI Elements

| Element | Details |
|---------|---------|
| Logo image | `R.drawable.logo_transparent`, 120dp x 120dp, centered |

### Animations
- **Scale:** Starts at 0.85x, springs to 1.0x with bouncy overshoot (damping 0.8, stiffness 320)
- **Alpha:** Fades from 0.0 to 1.0 over 120ms (`FastOutSlowInEasing`)
- Both animations trigger simultaneously on composition

### Interactions
- None. Entirely non-interactive, auto-advancing screen.

---

## 3. Setup Screen

**Route:** None (pre-navigation)  
**Entry point:** First app launch (no business profile saved)  
**Exit point:** Submit button → dialog → main app

### Top Bar
- **Logo:** `R.drawable.logo_transparent`, 36x36dp
- **Title:** "ZeroBook", bold, 20sp
- **Background:** `Colors.surface`

### Layout
- Vertically scrollable column
- **Phone** (< 600dp): 16dp horizontal padding, 8dp top padding, fields stacked vertically
- **Tablet** (>= 600dp): 24dp horizontal padding, 12dp top padding, fields side-by-side in rows, centered at max 760dp

### Screen Title
- Text: `"Business Profile Setup"`
- Size: 24sp (tablet) / 22sp (phone), SemiBold
- Center-aligned

### Section 1: Business Profile Information

**Section header:** `"Business Profile Information"` — 16sp, SemiBold, `AppColors.primary`

| # | Field | Type | Required | Keyboard | Notes |
|---|-------|------|----------|----------|-------|
| 1 | Business Name | `RetailTextField`, single-line | Yes (*) | Default | Test tag: `setup_business_name` |
| 2 | Owner / Signatory Name | `RetailTextField`, single-line | No | Default | |
| 3 | Address (Street / Area) | `RetailTextField`, multi-line | Yes (*) | Default | |
| 4 | PIN Code (6-digit) | `RetailTextField`, single-line | Yes (*) | Numeric | Max 6 chars, digits only. Trailing spinner during PIN lookup. Auto-fills City + State on success. |
| 5 | City / District | `RetailTextField`, single-line | No | Default | Auto-populated by PIN or GSTIN lookup |
| 6 | State & GST Code | Read-only `RetailTextField` + dropdown | Yes | Default | Default: Maharashtra (Code 27). Trailing down-arrow. Shows `"State Name (Code: XX)"`. |
| 7 | Phone Number | `RetailTextField`, single-line | No | Phone | |
| 8 | Email Address | `RetailTextField`, single-line | No | Email | |
| 9 | GSTIN | `RetailTextField`, single-line | No | Default | Max 15 chars, auto-uppercased. At 15 chars: API lookup with spinner. Auto-fills Business Name if blank. |
| 10 | PAN | `RetailTextField`, single-line | No | Default | Auto-uppercased. Auto-populated from GSTIN chars 2-11. |

### Section 2: Bank Details for Invoice Payments

**Section header:** `"Bank Details for Invoice Payments"` — 15sp, Bold, `AppColors.primary`

| # | Field | Type | Required | Keyboard | Notes |
|---|-------|------|----------|----------|-------|
| 11 | Account Number | `RetailTextField`, single-line | No | Numeric | Digits only |
| 12 | IFSC Code | `RetailTextField`, single-line | No | Default | Max 11 chars, auto-uppercased. At 11 chars: API lookup with spinner. Auto-fills Bank Name + Branch. |
| 13 | Bank Name | `RetailTextField`, single-line | No | Default | Auto-populated by IFSC lookup |
| 14 | Bank Branch | `RetailTextField`, single-line | No | Default | Auto-populated by IFSC lookup |

### Validation Warning
- Shown when `submitAttempted = true` AND required fields missing
- Text: `"⚠ Pending: Business Name, Address, Pin Code"` (13sp, SemiBold, error red)

### Submit Button
- **Label:** `"Initialize Business"` (16sp, Bold, white on primary)
- **Size:** Full-width, 54dp tall, 10dp rounded corners
- **Press animation:** Scale down to 0.98x (spring)
- **Behavior:**
  1. Sets `submitAttempted = true`
  2. If required fields missing → scrolls to and focuses first invalid field, shows keyboard
  3. If all valid → opens "Explore with Sample Data?" dialog

### Sample Data Dialog
- **Title:** `"Explore with Sample Data?"`
- **Body:** Description of sample products, parties, and vouchers
- **Confirm:** `"Yes, Import"` — saves profile, loads sample data, proceeds to main app
- **Dismiss:** `"No, Start Clean"` — saves profile without sample data, proceeds to main app
- Dismissible on outside tap

---

## 4. PIN Lock Screen

**Route:** None (conditional overlay)  
**Entry point:** App launch when PIN is enabled  
**Exit point:** Correct PIN entered → main app

### Layout
- Full-screen `Column`, centered vertically and horizontally
- 32dp padding
- Background: `AppColors.screenBg`

### UI Elements (top to bottom)

| Element | Details |
|---------|---------|
| Title | `"ZeroBook Ledger Lock"` — 22sp, bold, primary color |
| Subtitle | `"Enter 4-digit offline PIN parameters to access financial databases"` — 12sp, gray, centered |
| PIN dots | Row of 4 squares (16x16dp, 8dp rounded). Active: primary fill. Inactive: light gray `#E5E5E5` fill. 12dp spacing. |
| Error message | `"Incorrect safety PIN! Try again."` — red, 12sp (conditional) |

### Keypad
- 4x3 grid, 60% screen width
- 16dp vertical spacing between rows
- Each key: 56x56dp box, centered content, press animation

| Row 1 | Row 2 | Row 3 | Row 4 |
|-------|-------|-------|-------|
| 1 | 2 | 3 | CLR |
| 4 | 5 | 6 | 0 |
| 7 | 8 | 9 | OK |

- **Digit keys:** 18sp, bold, black
- **CLR / OK:** 18sp, bold, primary color

### Key Behavior
- **Digit key:** Appends to input (max 4). Auto-submits if 4 digits match correct PIN.
- **CLR:** Clears input to empty
- **OK:** Validates PIN. Correct → authenticates. Incorrect → shows error, clears input.

---

## 5. Changelog Dialog

**Type:** `AlertDialog` (non-dismissable on outside tap)  
**Entry point:** New app version detected after setup + PIN  
**Exit point:** "Got it" button

### UI Elements
- **Title:** `"What's New in ZeroBook {version}"` — dark color `#111827`
- **Body:** Scrollable column (max 60% screen height), 10dp spacing. Each change: `"• {change}"` in `#111827`
- **Confirm:** `"Got it"` button — saves version as seen, dismisses dialog
- **Container:** White background
- Cannot be dismissed by tapping outside

---

## 6. Main Application Shell

After all entry stages, the main app renders:
- Root `Scaffold` with `statusBarsPadding()` and `navigationBarsPadding()`
- `containerColor = AppColors.screenBg`
- No global TopAppBar (each screen provides its own)
- Bottom navigation bar (conditionally visible on top-level screens)
- `NavHost` with navigation transitions

---

## 7. Bottom Navigation Bar

**Visibility:** Only on top-level destinations (Dashboard, Vouchers, Parties, Settings)  
**Container:** `NavigationBar`, `AppColors.cardBg` background, 6dp tonal elevation, 72dp height

### Items

| # | Route | Label | Icon | Position |
|---|-------|-------|------|----------|
| 1 | `dashboard` | "Dashboard" | `GridView` (grid) | Leftmost |
| 2 | `vouchers` | "Vouchers" | `Assignment` (auto-mirrored) | Second |
| 3 | `parties` | "Parties" | `Group` (people) | Third |
| 4 | `settings` | "Settings" | `Settings` (gear) | Rightmost |

### Item Styling
- **Selected:** Icon and label in `AppColors.primary`, scale 1.02x
- **Unselected:** Icon and label in `AppColors.textTertiary`, scale 1.0x
- **Indicator:** `AppColors.primary` at 12% alpha
- **Label:** 11sp, single line, no soft wrap, 2dp below icon
- **Icon size:** 20dp

### Behavior
- Currently selected item is disabled (cannot re-tap)
- Tap triggers: popUpTo start destination (Dashboard), save state, launchSingleTop, restore state

---

## 8. Dashboard Screen

**Route:** `dashboard` (start destination)  
**Entry point:** App launch, bottom nav tap  
**Exit point:** Quick action buttons navigate to other screens

### Layout
- Vertically scrollable column
- Bottom padding: 80dp (clears bottom nav)
- Horizontal padding: 16dp (phone) / 24dp (tablet)
- Top padding: 16dp
- Vertical spacing: 12dp (phone) / 16dp (tablet)

### 8.1 Top Branding Row

A `Row` with space-between arrangement:

**Left side:**
| Element | Details |
|---------|---------|
| App logo | `R.drawable.logo_transparent`, 30dp (phone) / 36dp (tablet) |
| App name | `"ZeroBook"`, 17sp (phone) / 20sp (tablet), bold |

**Right side (end-aligned column):**
| Element | Details |
|---------|---------|
| Business name | Profile business name or `"Business Profile"`, 11sp / 13sp, bold, 1 line, ellipsis |
| GSTIN / Non-GST | Profile GSTIN or `"Non-GST"`, 10sp / 12sp, medium, tertiary color, 1 line, ellipsis |
| Financial year | `"FY {label}"`, 10sp / 11sp, tertiary, 1 line, ellipsis |

### 8.2 Low Stock Warning Banner (conditional)

Shown when any products have stock alert enabled and current stock <= threshold.

| Element | Details |
|---------|---------|
| Card background | `#FFF3E0` (warm orange) |
| Border | 1dp, `#F59E0B` |
| Corner radius | 14dp |
| Indicator dot | 8dp circle, solid `#F59E0B` |
| Text | `"{N} product(s) at or below threshold"`, 12sp, medium, `#92400E` |

### 8.3 KPI Cards Section

Up to 10 KPI cards displayed in a horizontally scrollable `LazyRow`:

| # | Title | Subtitle | Highlight Color |
|---|-------|----------|-----------------|
| 1 | Today's Sales | "vs yesterday" | `#1A73E8` (blue) |
| 2 | Today's Purchases | "vs yesterday" | `#6B7280` (gray) |
| 3 | This Month's Sales | "running total" | `#059669` (green) |
| 4 | Net Profit (Est.) | "positive"/"negative" | Green if >= 0, Red if < 0 |
| 5 | Receivables | "amount due in" | `#EA580C` (orange) |
| 6 | Payables | "amount due out" | `#7C3AED` (purple) |
| 7 | Cash Account | "current balance" | `#0891B2` (cyan) |
| 8 | Bank & UPI | "current balance" | `#2563EB` (blue) |
| 9 | Inventory | "stock value" | `#7C3AED` (purple) |
| 10 | GST | "total tax" | `#059669` (green) — only if profile has GSTIN |

#### KPI Card Styling (`KpiPremiumCard`)
- 16dp rounded corners, 120dp (phone) / 140dp (tablet) height
- 1dp border with highlight color at 12% opacity
- 2dp elevation shadow
- Subtle gradient overlay (highlight at 4% to transparent)
- Title: uppercase, 10sp / 12sp, bold, tertiary color, 0.8sp letter spacing
- Amount: 22sp / 26sp, bold, primary text color
- Subtitle: 11sp / 12sp, tertiary color
- Color indicator dot: 6dp circle in top-right corner

#### KPI Animation Modes (3 switchable modes)

**Mode 1: Standard Horizontal (default)**
- Cards: 260dp (phone) / 320dp (tablet) wide, 12dp gap
- Horizontal padding: 20dp (phone) / 40dp (tablet)
- Pagination dots: circle (8dp active, 5dp inactive), auto-fade after 1.5s idle

**Mode 2: Wallet Stack**
- Cards overlap with -8dp gap
- Off-center cards: max 10% scale reduction, max 50% alpha, z-ordered by distance
- Horizontal padding: 32dp (phone) / 48dp (tablet)
- Pagination dots: pill-shaped (18x6dp active, 4x4dp inactive)

**Mode 3: Spotlight Carousel**
- 3D perspective: off-center cards scale down (max 20%), fade (max 60%), rotate Y-axis (up to -8deg)
- Horizontal padding: 40dp (phone) / 56dp (tablet)
- Pagination dots: pill-shaped

#### KPI Card Tap → Analytics Popup

Tapping any KPI card opens a modal analytics popup:

**Overlay:** Full-screen black at 50% opacity, dismissible on tap

**Modal Card:**
- Width: 92% (phone) / 70% (tablet), max height: 80% screen
- 24dp rounded corners, 16dp elevation, scrollable, 20dp padding

**Header:** Generic title (e.g. "Sales"), "Business insight" subtitle, close button (X)

**Current Value Card:** 28sp bold formatted currency, filter icon button

**Filter Options (dropdown):** TODAY, THIS_WEEK, THIS_MONTH, THIS_QUARTER, THIS_YEAR, CUSTOM_DATE, CUSTOM_DATE_RANGE

**Growth Badge:** Pill — green for positive (e.g. "+12.3%"), red for negative

**Chart Card:** 200dp height, three chart types switchable via chips:
- **Line chart:** Horizontal grid, line segments with area fill, data point circles
- **Bar chart:** Rounded rectangles, 60% bar width / 40% gap
- **Pie chart:** Arc slices from center, starts at 12 o'clock

**Chart Type Chips:** "Line" (`ShowChart`), "Bar" (`BarChart`), "Pie" (`PieChart`)

**Stats row:** Highest, Lowest, Average values  
**Trend text:** "Momentum is trending upward" / "Momentum is easing slightly" / "Performance is stable"

### 8.4 Universal Search Bar

**Field:** Pill-shaped `OutlinedTextField`
- Placeholder: `"Search vouchers, ledger, or stock..."`
- Leading: Search icon (18dp)
- Trailing: Clear icon (20dp, visible when text non-empty)
- Background: cardBg at 85% alpha (focused) / 70% alpha (unfocused)

**Floating Search FAB:** Appears when search bar is contracted and query is blank
- Circular FAB, bottom-end position (20dp from end, 100dp from bottom)
- Primary background, white Search icon (22dp), 6dp elevation
- Enter/exit: fade + slide up/down animation

**Search Results Overlay:** When query is non-empty
- Card: full-width, max 400dp height, cardBg, 1dp border, 16dp corners, 4dp elevation
- Three sections (each with colored header, up to 5 results):
  - **Vouchers** (primary color header): voucher number, party, date, amount
  - **Ledger Entries** (purple header): account head, narration, Dr/Cr amount
  - **Stock Items** (green header): name, HSN, stock count, sale rate
- Empty: `"No results found."`

### 8.5 Progress Tracker Card (conditional)

Pill-shaped card shown when progress tracker is enabled in settings.

| Element | Details |
|---------|---------|
| Title | `"{Metric} Progress"` (e.g. "Sales Progress"), 13sp, semi-bold |
| Percentage | `"XX.X%"`, 13sp, bold, primary color |
| Progress bar | `LinearProgressIndicator`, 6dp height, pill-shaped, primary fill |
| Period label | `"{Period} target"` (e.g. "Monthly target"), 11sp, tertiary |

**Tap:** Opens Progress Details dialog:
- Title: `"Progress Analytics"`
- Shows: Metric, Period, Target, Current (bold), Progress % (primary, bold)
- Confirm: `"Close"` button

### 8.6 Quick Access Grid

Card titled `"Quick Access"` with 5 action buttons in a grid:

| # | Label | Icon | Background Tint | Action |
|---|-------|------|-----------------|--------|
| 1 | Quick Sale | `Bolt` | `#1A73E8` @ 10% | Navigate to Quick Sale |
| 2 | Receipt | `Payments` | `#059669` @ 10% | Navigate to Bank & Cash |
| 3 | Payments | `Add` | `#DC2626` @ 10% | Navigate to Bank & Cash |
| 4 | Reports | `Assignment` | `#EA580C` @ 10% | Navigate to Reports |
| 5 | Expenses | `TrendingUp` | `#7C3AED` @ 10% | Navigate to Expenses |

**Button anatomy:** 46dp (phone) / 52dp (tablet) icon container, 14dp rounded corners, icon 20dp / 24dp, label 11sp / 12sp semi-bold

### 8.7 Recent Transactions Card

Card titled `"Recent Transactions"` with filter/sort buttons and up to 8 transaction items.

#### Header Row
- Left: `"Recent Transactions"`, 14sp, bold
- Right: Two `OutlinedButton`s (32dp height):
  - **Filter:** `FilterList` icon + `"Filter"` text — opens dropdown with 12-13 filter options
  - **Sort:** `Sort` icon + `"Sort"` text — opens dropdown with 8 sort options

#### Filter Options
All Transactions, Sales, Purchase, Receipt, Payment, Income, Expense, Receivable, Payable, Due, Cancelled, Draft, GST Transactions (if GSTIN present)

#### Sort Options
Newest First (default), Oldest First, Amount High->Low, Amount Low->High, Voucher Number Ascending, Voucher Number Descending, Party Name A->Z, Party Name Z->A

#### Transaction Count
`"{N} recent transaction(s)"`, 12sp, tertiary

#### Transaction Row (up to 8)
| Element | Details |
|---------|---------|
| Icon container | 38dp square, 10dp corners |
| Icon background | RECEIPT/SALE: green tint, PAYMENT/PURCHASE: red tint, else: blue tint |
| Icon | RECEIPT: ArrowUpward, PAYMENT: ArrowDownward, SALE: Receipt, else: Payments |
| Type label | 11sp, semi-bold, tertiary |
| Voucher number | 13sp, semi-bold, primary text |
| Party + date | `"partyName . formattedDate"` or `"Cash . formattedDate"`, 10sp, tertiary |
| Amount | 13sp, bold, primary text |
| Status badge | 10sp colored: "Paid" (green), "Due" (red), "Partially Paid" (orange), "Cancelled" (tertiary) |

#### Empty State
- No vouchers: Assignment icon (56dp), `"No vouchers yet"`, `"Tap + to create your first voucher"`
- No matches: FilterList icon (48dp), `"No matching transactions yet."`

#### "View All Transactions" Button
- `TextButton`, centered, `"View All Transactions"` in primary color, semi-bold
- Navigates to Vouchers screen

### 8.8 Back Behavior
- First back press: clears search query if non-blank
- Second back press: dismisses analytics popup if open

---

## 9. Vouchers Screen — List View

**Route:** `vouchers`  
**Entry point:** Bottom nav "Vouchers" tab  
**Exit point:** Back arrow, bottom nav, FAB to create new

### Layout Modes
- **Mobile:** Single-column `Scaffold`
- **Desktop (>= 600dp):** Side-by-side `Row` — 360dp list panel + detail panel (shows InvoiceScreen for selected voucher, or placeholder text `"Select a voucher to see the layout representation."`)

### Top Area
- **Title:** `"Vouchers"` — 28sp, bold, 20dp horizontal / 24dp top padding
- **Search bar:** Pill-shaped `OutlinedTextField`, placeholder `"Search vouchers..."`, leading Search icon, trailing Clear icon
- **Filter button:** Pill `OutlinedButton` with `FilterList` icon + `"Filter"` text. Green dot badge (8dp) when filters active.
- **Sort button:** Pill `OutlinedButton` with `Sort` icon + `"Sort"` or `"Sort: {label}"` text.
- **Active filter chips:** `FlowRow` of `InputChip`s (each with Close icon to remove) + "Clear All" `AssistChip`

### Voucher List (`LazyColumn`)
- 10dp spacing between items, 88dp bottom padding for FAB
- Keyed by `voucher.id`

#### Voucher Card
- 1dp border, 14dp rounded corners, 3dp shadow
- Selected: blue tint border, primary at 6% alpha background
- **Row 1:** Voucher number (left, semi-bold 13sp) + Type badge (right, colored `Surface` with 6dp rounded corners)
- **Party name:** 13sp medium, secondary color
- **Row 2:** Date (left, 11sp) + Net amount (right, bold 14sp)

#### Selection Behavior
- **Long press:** Enters multi-selection mode
- **Tap in selection mode:** Toggles card selection
- **Tap normally (mobile):** Navigates to `NewVoucherScreen` for editing
- **Tap normally (desktop):** Sets selected voucher, shows in detail panel

### FAB
- Bottom-right, 56dp circular, primary color, Add icon (24dp)
- Entrance animation + press scale, 8dp shadow with primary ambient
- Test tag: `add_voucher_fab`
- Tap: Navigates to new voucher creation (type selection)

### Empty States
1. **No vouchers at all:** Assignment icon (56dp), `"No vouchers yet"`, `"Tap + to create your first voucher"`
2. **No matching vouchers:** FilterList icon (48dp), `"No vouchers match your filters."`, `"Adjust the filters..."` + `"Clear Filters"` button

### Delete Confirmation Dialog
- Title: `"Delete this voucher?"` or `"Delete N selected vouchers?"`
- Body: `"This action cannot be undone."`
- Confirm: `"Delete"` button
- Dismiss: `"Cancel"` text button

---

## 10. Voucher Type Selection

**Route:** Part of `NewVoucherScreen` (step 1)  
**Entry point:** FAB on Vouchers list  
**Exit point:** Back arrow or type card tap

### Top Bar
- Title: `"New Voucher"`
- Subtitle: `"Select voucher type"` (14sp, secondary)
- Back arrow

### Voucher Type Grid
- `LazyVerticalGrid`: 1 column (phone), 2 columns (tablet)
- 15 voucher type cards

#### Type Card
- Colored circular icon (42dp, 10% accent background) + Title (14sp semi-bold) + Description (12sp, tertiary, 1-line ellipsis)
- Subtle border with type-specific accent color at 15% alpha

| Key | Title | Description | Accent Color |
|-----|-------|-------------|--------------|
| JOURNAL | Journal | Manual ledger adjustment | Slate |
| SALE | Sales | Record sales to customers | Green |
| PURCHASE | Purchase | Record purchases from suppliers | Purple |
| RECEIPT | Receipt | Receive customer payments | Emerald |
| PAYMENT | Payment | Record outgoing payments | Red |
| SALE_RETURN | Sales Return | Customer returns goods | Orange |
| PURCHASE_RETURN | Purchase Return | Return goods to supplier | Dark Red |
| BILLS_RECEIVABLE | Bills Receivable | View amounts owed to you | Teal |
| BILLS_PAYABLE | Bills Payable | View amounts you owe | Amber |
| DEBIT_NOTE | Debit Note | Raise debit against party | Purple |
| CREDIT_NOTE | Credit Note | Issue credit to party | Blue |
| QUOTATION | Quotation | Create estimate or quote | Indigo |
| DELIVERY_CHALLAN | Delivery Challan | Record goods dispatch | Cyan |
| INCOME | Income | Track incoming funds | Teal |
| EXPENSE | Expense | Record a business expense | Pink |

#### Special Redirects
- **EXPENSE** → Redirects to Expenses screen
- **INCOME** → Redirects to Income screen
- **BILLS_RECEIVABLE** → Shows aged bills list view
- **BILLS_PAYABLE** → Shows payables bills list view

---

## 11. Voucher Entry Form

**Route:** `new_voucher?voucherId={voucherId}`  
**Entry point:** Voucher type selection, edit from list  
**Exit point:** Back arrow, save button

### Two-Step vs Three-Step Flow
- **Journal:** Single-step form
- **Sale / Purchase / Sale Return / Purchase Return / Debit Note / Credit Note / Quotation / Delivery Challan:** Three-step horizontal pager (Party & Items → Payment & Charges → Review)
- **Receipt / Payment:** Single-step form

### Three-Step Navigation
- **TabRow** below top bar with step tabs
- **Bottom bar:** `"Back"` outlined button + `"Next"` primary button (steps 1-2)
- Swipe between steps via `HorizontalPager`

### Common Form Fields

#### Step 1: Party & Items
| Field | Details |
|-------|---------|
| Voucher Number | Read-only, auto-generated |
| Date | Read-only, formatted |
| Select Party | Clickable surface: `"Tap to select or add party"`. Selected: party name (bold) + phone + balance. Trailing: search or close icon. |
| Party Info Card | Shows party name, balance, phone, state, interstate status (color-coded) |
| Narration | `RetailTextField`, "Voucher Narration / Memo Card Details" |
| Items section | Header: "Items Included" + "Stock" button + "Add Item" button |
| Item cards | Product name (bold), HSN, qty x rate, GST rate, total (bold), delete icon |
| Return types | Shows "Original Qty" + editable "Return Qty" field |
| Summary bar | "Items N \| Taxable ₹X \| GST ₹Y \| Total ₹Z" |

#### Step 2: Payment & Charges
| Field | Details |
|-------|---------|
| Payment mode selector | Row of `FilterChip`s: CASH, BANK, UPI, CHEQUE (and PART PAYMENT, CREDIT for Sale) |
| Payment mode details | Mode-specific fields (see below) |
| Additional Charges (Sale only) | Expandable card with charge type dropdown, amount field |
| Transport Details | 5 fields: Transporter Name, Vehicle No., LR/GR No., Transporter GSTIN, Destination |
| References | Reference No. & Date, Other References |

#### Payment Mode Details
- **CASH:** Descriptive text about local cash ledger
- **BANK:** Bank Name, Account Holder Name, IFSC + Branch Name (side by side)
- **CHEQUE:** Cheque No + Bank Name (side by side) + Branch Name + Memo
- **UPI:** Description about QR scanner + optional UPI/Reference ID field
- **PART PAYMENT:** "Amount Paid Now" field + sub-mode chips + Balance Due (red) + Due date picker
- **CREDIT:** "Full amount on credit" description + Due Date picker + amount due (orange)

#### Step 3: Review
| Element | Details |
|---------|---------|
| Review Card | "Review Before Saving" title, party, mode, items count |
| Live Invoice Preview | WebView rendering HTML invoice (mobile: full width, desktop: right panel) |
| GST Summary Card | Taxable amount, each charge, IGST or CGST+SGST, Round Off, "Net Total Owed" |

### Sticky Bottom Bar
- **Surface** with 8dp elevation, navigation-bar padding
- **Left:** "Net Amount" label (11sp) + Amount (18sp, extra-bold, primary)
- **Right (Sale/Return types):** Two buttons:
  - `"Save & Post"` — slate gray, check icon (saves without printing)
  - `"Print"` — primary color, assignment icon (saves and prints)
- **Right (Other types):** Single `"Save"` button — primary color
- All buttons: 44dp height, press scale, 8dp rounded

### Confirm Save Dialog
- Title: `"Confirm Save"` or `"Update this voucher?"`
- Body: Save confirmation text
- Confirm: TextButton (primary, bold)
- Cancel: TextButton (red)

### UPI Payment Dialog (for UPI + Sale type)
- Title: Green check + `"ZeroBook UPI Cash Terminal"`
- QR Code: Custom Canvas-drawn abstract QR in 220dp white box
- Simulated animation: Generating → Scanned → Verified → Confirmed
- On confirmation: Large green checkmark + `"TRANSACTION DONE"`
- Amount: `"AMOUNT DUE: ₹X"` (18sp, bold, primary)
- Progress card with animated steps
- Manual Override button + Cancel button
- Auto-dismisses after animation

### Print Receipt Dialog
- Title: Green check + `"Transaction Saved Successfully"`
- Preview area: 420dp `Surface` with WebView invoice (zoom-enabled)
- Loading state: Skeleton placeholder shimmer
- Action buttons:
  - `"Save PDF As"` — primary, download icon → system file picker
  - `"Share via WhatsApp / Other"` — green (#25D366), share icon → share intent
- Dismiss: `"Close & Exit"` text button (red)

---

## 12. Voucher Item Entry Sheet

**Type:** `ModalBottomSheet`  
**Entry point:** "Add Item" button or tap existing item  
**Exit point:** Cancel or Save button

### Title
- `"Add Item"` or `"Edit Item"` (18sp, bold)

### Fields

| # | Field | Details |
|---|-------|---------|
| 1 | Product Name * | Search/type field with dropdown. Trailing: Search icon (barcode scanner), dropdown arrow. Dropdown shows max 8 matching products (name + HSN + rate). "Create new product" option at bottom. |
| 2 | HSN/SAC Code | Digits only, max 8 chars. Suggested HSN chip after 1.2s debounce. |
| 3 | Quantity * | Decimal input. Error: "Quantity must be greater than zero" |
| 4 | Unit | Dropdown: PCS, KG, LTR, MTR, BOX, BAG, NOS |
| 5 | Rate (₹) * | Decimal input with ₹ prefix. Error: "Rate must be greater than zero" |
| 6 | Discount | Decimal input + toggle chip (% / ₹) |
| 7 | GST Rate (conditional) | Dropdown: 0%, 5%, 12%, 18%, 28% (or global rate chip if enabled) |

### Live Calculation Card
- Taxable Amount: formatted currency
- GST Amount: formatted currency
- Item Total: formatted currency (bold)

### Action Buttons
- **Cancel:** `OutlinedButton`, weight(1f)
- **Save:** `Button` primary, weight(1f), `"Add to Invoice"` or `"Update Item"`

---

## 13. Voucher Filter & Sort Sheets

### Filter Bottom Sheet
- Title: `"Filter vouchers"` + `"Clear All"` text button
- Fields (12dp spacing):
  1. **Voucher Type:** `FlowRow` of `FilterChip`s — 16 options (ALL, JOURNAL, SALE, PURCHASE, RECEIPT, PAYMENT, SALE_RETURN, PURCHASE_RETURN, DEBIT_NOTE, CREDIT_NOTE, BILLS_RECEIVABLE, BILLS_PAYABLE, QUOTATION, DELIVERY_CHALLAN, INCOME, EXPENSE)
  2. **Date:** Two `OutlinedButton`s for start/end date → opens `DatePickerDialog`
  3. **Party:** `OutlinedTextField` for party name
  4. **Payment Status:** `FlowRow` — Any status, Paid, Partially paid, Unpaid
  5. **Amount:** Min and Max decimal fields
  6. **Reference:** `OutlinedTextField`
- Bottom: `"Cancel"` + `"Apply"` buttons

### Sort Bottom Sheet
- Title: `"Sort vouchers"`
- 8 options as clickable rows with check icon when selected:
  - Default order, Newest First, Oldest First, Highest Amount, Lowest Amount, Voucher Number, Party Name (A-Z), Party Name (Z-A)
- Bottom: `"Cancel"` + `"Apply"` buttons

---

## 14. Parties Screen

**Route:** `parties`  
**Entry point:** Bottom nav "Parties" tab  
**Exit point:** Back arrow, bottom nav

### Layout Modes
- **Mobile:** Full-screen list, tap navigates to detail
- **Desktop (>= 600dp):** Split-pane — 360dp left (list) + right (detail/form)

### Top Area
- **Title:** `"Parties"`, 22sp, bold
- **Search field:** `RetailTextField`, placeholder `"Search by name, phone or GSTIN..."`, trailing search icon
- **Filter chips:** Three `FilterChip`s: `"ALL"`, `"CUSTOMER"`, `"SUPPLIER"` — 4dp rounded, 9sp (desktop) / 11sp (mobile)

### Party List (`LazyColumn`)
- 8dp (desktop) / (mobile spacing), 120dp bottom padding

#### Party Card
- 1dp border, 16dp rounded corners
- Selected: primary border at 35% alpha, primary at 8% alpha background
- **Left section:** Party name (bold, 12sp/14sp) + Type badge (small card, 8sp/9sp) + Phone + GSTIN (mobile only)
- **Right section:** Edit icon button (primary) + Balance amount (bold, 12sp/14sp) + Balance label ("DR"/"CR" or "DR (Receivable)"/"CR (Payable)")
- **Color:** Red for DR (>= 0), Green for CR (< 0)

#### Empty States
- **No parties at all:** Person icon (56dp), `"No parties added yet. Tap + to add."`, 15sp, medium
- **No matches:** `"No matching parties found."`, 14sp, secondary

### FAB
- Bottom-right, primary color, Add icon
- Tap: Opens Add Party Form

### Multi-Selection
- Long press enters selection mode
- Selection top bar: count, Select All, Delete, Close
- Delete confirmation dialog

---

## 15. Party Add/Edit Form

**Entry point:** FAB on Parties list, Edit icon on party card  
**Exit point:** Back arrow, Save/Update button

### Top Bar
- Title: `"Add New Party"` or `"Edit Party"` (bold)
- Back arrow

### Form Fields

| # | Field | Required | Details |
|---|-------|----------|---------|
| 1 | Party Name | Yes (*) | Single line, test tag: `party_name_input` |
| 2 | Type | No | `FilterChip` row: CUSTOMER, SUPPLIER, BOTH. Default: CUSTOMER |
| 3 | Phone Number | No | Phone keyboard |
| 4 | Email Address | No | Email keyboard |
| 5 | Address | No | Multi-line |
| 6 | PIN Code | No | Numeric, max 6 digits. PIN lookup after 300ms debounce. |
| 7 | City | No | Auto-filled by PIN lookup |
| 8 | State | No | Read-only with dropdown. Default: West Bengal. |
| 9 | GSTIN | No | 15-digit, auto-uppercased. `GstinValidationFeedback` below. Auto-fills PAN + State. |
| 10 | PAN | No | Auto-uppercased |
| 11 | Opening Balance (₹) | No | Decimal keyboard + DR/CR chip toggle |
| 12 | Credit Limit | No | Decimal keyboard |
| 13 | Credit Days | No | Number keyboard, digits only |
| 14 | Notes | No | Multi-line (min 3 lines) |

### Validation
- Name is the only required field
- Error text: `"Please fill in all mandatory (*) fields correctly."` or `"Party name is required."`

### Save Button
- Full-width, 48dp, primary color, check icon
- Add mode: `"Save Party"`, test tag: `save_party_button`
- Edit mode: `"Update Party"`

---

## 16. Party Detail Screen

**Route:** `party_detail/{partyId}`  
**Entry point:** Tap party card (mobile), auto-select (desktop)  
**Exit point:** Back arrow

### Top Bar
- Title: Party name or `"Party Ledger"` fallback
- Back arrow

### Party Details Card
- 0.5dp border, 8dp rounded, 12dp padding
- **Row 1:** Phone icon + `"Phone: {phone} | Email: {email}"`
- **Row 2:** Map icon + `"GSTIN: {gstin or Unregistered} | State: {state}"`
- **Divider**
- **Row 3:** `"Outstanding Balance:"` (bold, 13sp) + formatted balance (bold, 16sp, red for DR, green for CR)

### Payment Reminder Email Button
- Full-width, 44dp, primary color
- Icon: Email + text `"Send Email Payment Reminder"`
- Only shown when outstanding > 0
- Opens device email client with pre-filled payment reminder

### Ledger Statement Table
- Header: `"LEDGER STATEMENT"`, bold, 14sp
- Table columns: Date (0.9f) | Debit (1.0f) | Credit (1.0f) | Balance (1.2f)
- Header row: `#FAF9F9` background, 11sp bold
- Data rows: Alternating backgrounds, 11sp, running balance computed cumulatively
- DR amounts in debit color, CR amounts in credit color
- Empty state: `"No ledger activities recorded"`

### Loading State
- `PartyDetailSkeleton`: Two skeleton cards with shimmer placeholders

---

## 17. Settings Screen

**Route:** `settings`  
**Entry point:** Bottom nav "Settings" tab  
**Exit point:** Back arrow (mobile)

### Layout Modes
- **Mobile:** Full-screen menu → sub-mode replaces menu
- **Desktop (>= 600dp):** Split-pane — 360dp left (menu always visible) + right (sub-mode content)

### Menu Items (8 cards)

Each `SettingsMenuCard`: Icon badge (primary at 12% alpha) + Title (bold, 15sp) + Description (11sp, secondary) + Right chevron

| # | Title | Description | Icon | Action |
|---|-------|-------------|------|--------|
| 1 | Edit Business Profile | Update GSTIN, Address, PAN, signature, bank details | `Business` | Navigate to Business Profile |
| 2 | Manage Products Master | Configure stock prices, units, HSN codes | `ShoppingBag` | Navigate to Products screen |
| 3 | Ledger Books & Account Heads | View ledger accounts with balances | `AccountBalance` | Navigate to Ledger Books |
| 4 | Customize | KPI animation, progress tracker, dashboard options | `CheckCircle` | Navigate to Customize |
| 5 | Theme & Colors | Switch between Beach, Blue, Green, Purple, Teal | `CheckCircle` | Navigate to Theme |
| 6 | Financial Year Control | Configure custom financial year | `DateRange` | Navigate to FY settings |
| 7 | Change Log | Read what changed in each release | `Info` | Opens changelog dialog |
| 8 | About ZeroBook | Compliance versions, regulatory details | `Info` | Navigate to About |

### Backup & Restore Card

Below the menu items:

- **Section header:** `"BACKUP & RESTORE DATA"`, bold, 13sp, secondary
- Description text (11sp, secondary)
- **Row 1:**
  - `"Export to CSV"` — primary button → triggers CSV export
  - `"Import CSV"` — outlined button → opens system file picker
- **Row 2:**
  - `"Backup Database"` — outlined button → backs up SQLite database

---

## 18. Business Profile Settings

**Route:** Sub-screen within Settings  
**Entry point:** "Edit Business Profile" menu item

### Top Bar
- Title: `"Business Profile"` (bold)
- Back arrow (hidden on desktop)

### Form Fields

All fields scrollable with 12dp spacing:

| # | Field | Required | Details |
|---|-------|----------|---------|
| 1 | Business Name * | Yes | RetailTextField, auto-scroll on validation error |
| 2 | Owner / Proprietor | No | RetailTextField |
| 3 | Business Address * | Yes | RetailTextField, auto-scroll on validation |
| 4 | PIN Code * | Yes | Numeric, max 6 digits, PIN lookup spinner |
| 5 | City / District | No | Auto-filled by PIN lookup |
| 6 | State & GST Code | Yes | Read-only with dropdown + "Detect via Location" button |
| 7 | Phone Number | No | Phone keyboard |
| 8 | Email Address | No | Email keyboard |
| 9 | GSTIN Number | No | 15-digit, auto-uppercased, `GstinValidationFeedback` |
| 10 | PAN Card Number | No | Auto-uppercased |
| 11 | Account Number | No | Numeric |
| 12 | IFSC Code | No | Max 11 chars, auto-uppercased, API lookup at 11 chars |
| 13 | Bank Name | No | Auto-populated by IFSC lookup |
| 14 | Bank Branch | No | Auto-populated by IFSC lookup |
| 15 | Business Logo | No | Image upload card (Gallery/Image or PDF/Document buttons) |
| 16 | Authorized Signature | No | Image/PDF upload with preview |
| 17 | Terms & Conditions / Declaration | No | Multi-line |

### Business Logo Upload Card
- Title + subtitle
- Image thumbnail (90x70dp) or upload placeholder
- Two buttons: "Gallery / Image" (primary) + "PDF / Document" (gray)
- Preview dialog on tap

### Signature Upload Card
- Title + subtitle
- Image/PDF preview when uploaded
- Change/Remove buttons
- Upload button when empty

### Validation Warning
- `"⚠ Pending: {comma-separated field labels}"` — error color, 13sp, SemiBold

### Save Button
- Full-width, 48dp, check icon + `"Save Alterations Settings"` (bold)
- Auto-scrolls to first invalid field on error
- Shows Toast on success

---

## 19. Theme & Colors Settings

**Route:** Sub-screen within Settings  
**Entry point:** "Theme & Colors" menu item

### Top Bar
- Title: `"Theme & Colors"` (bold)
- Back arrow

### Content
Single card titled `"App Appearance"` with subtitle `"Pick a theme and the whole app updates immediately."`

### Theme Picker Row
5 theme options in a horizontal row, each with:
- **Circle swatch:** 42dp circle with theme color. Selected: primary border + white check icon. Beach: special brown border when unselected.
- **Label text:** 11sp, secondary

| Theme | Swatch Color |
|-------|-------------|
| Beach | `#FDF6EC` (cream) |
| Blue | `#1A73E8` |
| Green | `#1E8A3C` |
| Purple | `#6200EA` |
| Teal | `#0F9D8A` |

### Behavior
- Tap applies theme instantly (no confirmation needed)
- Theme persisted to SharedPreferences
- All app colors update immediately

---

## 20. Customize Settings

**Route:** Sub-screen within Settings  
**Entry point:** "Customize" menu item

### Top Bar
- Title: `"Customize"` (bold)
- Back arrow

### Content

#### KPI Animation Mode
- Header: `"KPI Animation Mode"` (bold, 13sp, secondary)
- 3 selectable rows:
  - "Standard Horizontal"
  - "Wallet Stack (Default)"
  - "Spotlight Carousel"
- Selected row: primary border + bold text + green check icon
- Unselected: default border

#### Progress Tracker
- Header: `"Progress Tracker"` (bold, 14sp)
- Toggle: "Show Progress Tracker" + Switch
- Fields: Metric (default "Sales"), Time (default "Monthly"), Target Amount (default "200000")
- Save button: primary, `"Save"` text
- Toast: `"Dashboard settings saved"`

---

## 21. Financial Year Settings

**Route:** Sub-screen within Settings  
**Entry point:** "Financial Year Control" menu item

### Top Bar
- Title: `"Financial Year Control"` (bold)
- Back arrow

### Content
Outer card with centered column:

- **Header:** `"FINANCIAL YEAR CONFIGURATION"`, 12sp, SemiBold, primary, letter-spacing 1sp
- **Title:** `"Active Financial Year"`, 18sp, bold
- **Start Year field:** 220dp width, numeric keyboard, placeholder "e.g. 2025", max 4 digits
- **Info card:** Primary background at 5% alpha
  - Current active year label
  - End year
  - FY Label
  - Auto-advance explanation

### Save Button
- Full-width, 48dp, primary, bold white text `"Save Financial Year"`
- Validates 4-digit input
- Toast on success

---

## 22. PIN Protection Settings

**Route:** Sub-screen within Settings  
**Entry point:** Accessible via Settings (when available)

### Top Bar
- Title: `"App Pin Protection Lock"` (bold)
- Back arrow

### Content

#### Toggle Row
- Left: "PIN Code Protection Toggle" (bold) + description (11sp, gray)
- Right: Switch (toggles `pin_enabled` in SharedPreferences)

#### PIN Input (when enabled)
- OutlinedTextField: "Initialize 4-digit security PIN"
- Max 4 chars, decimal keyboard
- Test tag: `app_pin_setup`
- Helper text: "Current stored code will be prompted next time you trigger the app."

### Apply Button
- Full-width, 48dp, check icon + "Apply Parameters"
- Navigates back to menu, Toast: `"Security configurations recorded"`

---

## 23. About Settings

**Route:** Sub-screen within Settings  
**Entry point:** "About ZeroBook" menu item

### Top Bar
- Title: `"About ZeroBook Detail"` (bold)
- Back arrow

### Content (centered)
| Element | Details |
|---------|---------|
| App logo | `R.drawable.logo_icon`, 72dp |
| App name | "ZeroBook", 20sp, bold |
| Tagline | "Record. Transact. Grow.", 13sp, secondary |
| Version | "Version {BuildConfig.VERSION_NAME}", 12sp, secondary |
| Description | "Built for Indian small retailers", 11sp, secondary |
| Change Log card | Clickable card: "Change Log" title + "View the full release history" + right chevron |

---

## 24. Email Automation Settings

**Route:** Sub-screen within Settings  
**Entry point:** "Email Automation" menu item (via Settings)

### Card 1: Email Automation Control
- Header: Email icon + "Email Automation" (bold, 16sp)
- Status text: Dynamic based on enabled state
- Enable toggle: Switch with prerequisite checks (consent, connected account, business email)
- Sender section: "From: {businessName} <{email}>"
- Connect/Disconnect button: "Connect Gmail Account" (primary) or "Disconnect Gmail" (outlined)
- Spinner while connecting

### Card 2: Recipients
- Empty state: "No pending recipients available"
- Recipient rows: Name, email, due amount, due date, invoice reference, schedule info
- Edit button + Delete icon per recipient

### Card 3: Missing Email Recipients
- Bulleted list of party names without emails
- Helper text about adding emails to profiles

### Card 4: Recent Activity
- Empty: "No automation activity yet..."
- Entries: recipient, subject, status (SENT in primary, else error)

### Consent Dialog
- Title: "Email Automation Consent"
- Checkbox + "I agree" text
- Confirm: "Agree & Continue" (enabled only when checked)
- Dismiss: "Cancel"

### Edit Rule Dialog
- Title: "Automation Settings"
- Fields: Subject, Message (multi-line), Send time, Repeat frequency, Send mode
- Save + Cancel buttons

---

## 25. Reports Screen

**Route:** `reports`  
**Entry point:** Dashboard quick action "Reports"  
**Exit point:** Back arrow

### Layout Modes
- **Mobile:** Menu → detail navigation (toggled by `activeReport` state)
- **Desktop (>= 600dp):** Split-pane — 360dp left (menu) + right (detail)

### Menu Title
- `"Regulatory Financial Books & GST Reports"`, FontWeight.Black, 16sp

### Menu Header
- `"Double Entry Ledger Reports Summary ({financialYear})"`, bold, 13sp, secondary

### 9 Menu Cards

| # | Title | Description | Action |
|---|-------|-------------|--------|
| 1 | Trial Balance | "Interactive Group-wise trial verification statement of net Debits and Credits" | Shows Trial Balance |
| 2 | Profit & Loss Account | "View real double-entry Trading (Gross GP) and P&L statements side-by-side" | Shows Trading & P&L |
| 3 | Balance Sheet | "Double entry balanced visual assets, liabilities, and proprietor equity" | Shows Balance Sheet |
| 4 | GST Summary Status | "Estimated output liabilities offset against input credits" | Shows GST Summary |
| 5 | Outstanding Receivables (Email automation) | "Client outstanding report with automated invoice reminders" | Shows Receivables |
| 6 | Outstanding Payables | "Supplier credits outstanding ledger status" | Shows Payables |
| 7 | Stock Report | "Current stock, thresholds, and low stock export" | Shows Stock Report |
| 8 | Ledger Books | "Browse chart of accounts and ledger balances" | Navigates to Ledger Books screen |
| 9 | Expenses | "Track operating expenses and export the register" | Navigates to Expenses screen |

---

## 26. Trial Balance Report

### Table Header
- Background: `AppColors.primary`
- Columns: PARTICULARS (1.5f) | DEBIT DR (₹) (1f) | CREDIT CR (₹) (1f)
- White text, bold, 11sp

### 11 Groups (expandable)

| # | Group | Default Side |
|---|-------|-------------|
| 1 | Capital Account | Credit (₹5,00,000) |
| 2 | Sundry Creditors (Current Liabilities) | Credit |
| 3 | Duties & Taxes (GST Payable) | Credit |
| 4 | Fixed Assets | Debit (₹0) |
| 5 | Cash-in-hand & Bank Accounts | Debit |
| 6 | Sundry Debtors (Current Assets) | Debit |
| 7 | Stock-in-hand (Inventory) | Debit |
| 8 | Duties & Taxes (ITC Credit) | Debit |
| 9 | Sales Accounts | Credit |
| 10 | Purchase Accounts | Debit |
| 11 | Indirect Adjustments (Round Off) | Conditional |

### Group Header Row
- `sectionHeaderBg` background
- Expand/collapse arrow (16dp) + Group name (bold, 12sp, primary) + Debit amount (monospace, bold) + Credit amount (monospace, bold)

### Ledger Row (when expanded)
- Alternating backgrounds (even/odd)
- Name (11sp) + Debit or Credit amount (monospace, 11sp, semi-bold)
- Indented (24dp start padding)

### Unreconciled Difference Warning
- Background: warning at 12% alpha
- Text: "Unreconciled Difference (review required):" + DR/CR amounts in error/success colors

### Grand Totals Row
- Background: primary if balanced, error if not
- Text: "GRAND TOTALS (BALANCED)" or "GRAND TOTALS (NOT BALANCED — REVIEW)"
- DR + CR totals in monospace, bold, white on primary

---

## 27. Trading & Profit/Loss Report

### Part I: Trading Account
- Card titled "PART I: TRADING ACCOUNT (Gross Margin Analysis)"
- Dual-column layout with border

**Debit Side (Costs):**
- Opening Stock, Purchase Cost, Gross Profit c/o (if positive)
- Total row

**Credit Side (Revenue):**
- Sales Value, Stock Valuation, Gross Loss c/o (if negative)
- Total row

### Part II: Profit & Loss Account
- Card titled "PART II: PROFIT & LOSS ACCOUNT (Business Net Margin)"
- Dual-column layout

**Debit (Charges):**
- Gross Loss b/f, Round Off Expense, NET REAL PROFIT (if positive, highlighted)

**Credit (Gains):**
- Gross Profit b/f, Round Off Income, NET REAL LOSS (if negative, highlighted)

### Helper Composables
- `PLColumnItem`: 34dp height row, name (10sp) + amount (9sp monospace)
- `PLTotalItem`: Primary-tinted background, "Total" + amount

---

## 28. Balance Sheet Report

### Subtitle
- "Double Entry Statement of Assets, Liabilities, and Owner's Capital", 12sp, secondary

### Dual-Column Layout

**Left: Liabilities & Equity**
- Capital (Balancing Account)
- Net Profit this term (highlighted)
- Sundry Creditors
- GST Duties Liability (conditional)
- Total

**Right: Assets**
- Stock-in-hand Inventory
- Sundry Debtors
- Cash-in-hand Account
- Bank Account Reserves
- GST Input Tax Assets (conditional)
- Total

### Balance Difference Warning
- Shown when difference >= ₹1.0
- Background: warning at 12% alpha
- Text: "Balance Sheet does not tie out — review required:" + amount

---

## 29. GST Summary Report

### Card 1: Output Tax
- Title: "OUTWARD SUPPLY LIABILITIES (OUTPUT TAX)"
- Rows: CGST Output Collector, SGST Output Collector, IGST Output Collector
- Total row

### Card 2: Input Tax Credit
- Title: "INWARD SUPPLY CREDIT offsets (INPUT TAX CREDIT)"
- Rows: CGST Input credits, SGST Input credits, IGST Input credits
- Total row

### Card 3: Net Payable Position
- Title: "GST PAYABLE POSITION (NET TO FILE)"
- Net result row: "Net Payable GST to Government:" (error color) or "Carryover ITC balance (Asset Credit):" (success color)
- Amount: 16sp, monospace, bold
- Helper text: Filing deadline explanation

---

## 30. Outstanding Receivables Report

### Tabs
- Tab 0: "By Customers" — `DebtorsRemindersView`
- Tab 1: "Bill-wise Outstanding" — `AgedBillsListView` (default)

### DebtorsRemindersView
- Empty: "No active outstanding debtor balances."
- Each debtor card: Party name, email, phone, outstanding amount (red), "Send Email" button (primary, launches email intent)

### AgedBillsListView
- Empty: "All credit invoices have been fully paid off!"
- Each bill card:
  - Invoice # (bold) + Status badge (PAID=green, OVERDUE=red, PARTIAL=blue, UNPAID=amber)
  - Customer name
  - Bill date, due date, days overdue
  - Net bill amount, Pending amount (red)
  - "Receive Payment" button → navigates to new voucher (RECEIPT type)

### Payment Dialog (from Receive Payment)
- Title: "Receive Payment - Invoice #{voucherNo}"
- Outstanding display (red, bold)
- Amount to Pay field
- Payment mode: CASH / BANK / UPI buttons
- Receipt date + "+1 Day" button
- "Capture Payment" button + "Dismiss" text button

---

## 31. Outstanding Payables Report

- Empty: "No active supplier bills are awaiting payment."
- Each bill card:
  - Purchase # (bold) + Status badge (PARTIAL/UNPAID)
  - Supplier name
  - Bill date, phone
  - Original amount, Paid so far, Remaining (red)
  - "Pay" button → navigates to new voucher (PAYMENT type)

---

## 32. Stock Report Screen

**Route:** Embedded in Reports or accessible from Voucher Item Sheet  
**Entry point:** Reports menu "Stock Report", or "Stock" button in voucher form

### Search + Export Row
- Search field: filters by product name or HSN code
- "Export CSV" button → exports to device storage

### Filter Chips
- "All", "Low Stock", "Out of Stock"

### Product List
Each card:
- Product name (bold) + Status chip (In Stock=green, Low Stock=orange, Out of Stock=red)
- "HSN: {code}"
- "Current stock: {qty} {unit}"
- "Low stock threshold: {N}" or "Low stock alert disabled"

---

## 33. Products Screen

**Route:** `products`  
**Entry point:** Settings "Manage Products Master"  
**Exit point:** Back arrow

### Top Bar
- Title: `"Manage Products"` (bold)
- Back arrow
- Selection mode: `UniversalSelectionTopAppBar` (count, Select All, Delete, Close)

### Search Bar
- Placeholder: "Search by product, HSN, or barcode"
- Filters: name, HSN code, barcode value

### Low Stock Warning Card
- Orange background, amber dot, `"{N} product(s) need stock attention"`

### Product List
Each `ProductRow` card:
- 1dp border, selected: primary tint
- **Left:** Product name (bold, 14sp) + HSN + Stock + Alert threshold + Barcode
- **Right:** Edit/Delete icons + Status chip (In Stock/Low Stock/Out of Stock) + Sale Rate (bold, primary)

### Empty State
- "No products configured yet."

### Product Detail Dialog (on tap)
- Product name title + all details (HSN, Unit, Sale Rate, Purchase Rate, GST%, Opening Stock, Current Stock, Alert info, Barcode, Secondary unit)
- "Edit" button + "Close" text button

### Delete Confirmation Dialog
- "Delete this product?" or "Delete N selected products?"
- "This action cannot be undone."
- "Delete" + "Cancel" buttons

### FAB
- Primary, Add icon
- Opens Product Editor in ADD mode

---

## 34. Product Editor Screen

**Entry point:** FAB on Products list, Edit icon, inline creation  
**Exit point:** Back arrow, Save button

### Top Bar
- Title: "Add Product" or "Edit Product" (bold)
- Back arrow

### Form Fields

| # | Field | Required | Details |
|---|-------|----------|---------|
| 1 | Product / Item Name * | Yes | Single line |
| 2 | HSN/SAC Code | No | Digits only, max 8 chars, "Find HSN" button → HSN lookup dialog |
| 3 | Batch Number | No | Toggle + text field (placeholder: "e.g. BTH2025001") |
| 4 | Expiry Date | No | Toggle + text field (placeholder: "DD-MM-YYYY") |
| 5 | Serial Number Tracking | No | Toggle only |
| 6 | Barcode / QR Code | No | Text field + Search icon → Barcode Scanner Dialog |
| 7 | Unit of Measurement * | Yes | Dropdown: PCS, KG, GM, MG, LTR, ML, BOX, BAG, NOS, MTR |
| 8 | Sale Rate (Rs) * | Yes | Decimal field, clears "0" on focus |
| 9 | Cost Rate (Rs) * | Yes | Decimal field, clears "0" on focus |
| 10 | GST Rate | No | Dropdown: 0%, 5%, 12%, 18%, 28% |
| 11 | Opening Stock Quantity | No | Decimal field |
| 12 | Current Stock | No | Decimal field |
| 13 | Low stock alert | No | Toggle + threshold field |
| 14 | Secondary Unit | No | Uppercase auto |
| 15 | Conversion Factor | No | Decimal field |

### HSN Lookup Dialog
- Title: "Select HSN"
- Scrollable list (260dp) of HSN results (code bold + description)
- Empty: "No HSN results found for this product name."
- "Close" text button

### Save Button
- Full-width, 48dp, primary, check icon
- "Save Product" or "Update Product" (bold)
- Validation error: "Please fill the required product details before saving."

---

## 35. Bank & Cash Register Screen

**Route:** `bank_cash`  
**Entry point:** Dashboard quick actions (Receipt/Payments)  
**Exit point:** Back arrow

### Top Bar
- Title: "Bank & Cash Register" (bold)
- Back arrow

### Balance Summary Card
- Two columns separated by vertical divider:
  - **Left:** "CASH BALANCE" (11sp, semi-bold) + formatted amount (18sp, bold, green if >= 0, red if < 0)
  - **Right:** "BANK BALANCE" (same layout)

### Sub-Tab Filter Chips
- "CASH" and "BANK" FilterChips, 4dp rounded

### Transaction Log Header
- "TRANSACTION LOG" — bold, 12sp, gray

### Transaction List
Each card:
- Icon: ArrowUpward (green, RECEIPT) or ArrowDownward (red, PAYMENT), 24dp
- Party name (bold, 13sp) — fallback: "General Direct"
- "Mode: {mode} | {narration}" — gray, 11sp
- Date — light gray, 10sp
- Amount: "+ ₹{amount}" (green, bold) or "- ₹{amount}" (dark, bold), 14sp

### Empty State
- "No cash or bank transactions registered in this selected view."

### FAB
- Primary, Add icon → opens Manual Transaction Form

---

## 36. Manual Transaction Form

**Entry point:** FAB on Bank & Cash Register  
**Exit point:** Back arrow, Commit button

### Top Bar
- Title: "Record manual flow" (bold)
- Back arrow

### Fields

| # | Field | Details |
|---|-------|---------|
| 1 | Tx Type | FilterChip row: RECEIPT, PAYMENT |
| 2 | Transfer Mode * | Dropdown: CASH, BANK, CHEQUE, UPI, NEFT, RTGS, IMPS |
| 3 | Transaction Amount * | Decimal keyboard, test tag: `manual_tx_amount` |
| 4 | Linked Party Account | Dropdown: "None (Direct Transaction)" or party names |
| 5 | Cheque / draft Number | Shown when mode = CHEQUE |
| 6 | Cleared Bank Name | Shown when mode = CHEQUE |
| 7 | Memo Narration | Text field |

### Validation
- Error: "Please fill in the amount with valid number values."

### Submit Button
- Full-width, 48dp, check icon + "Commit Transaction" (bold)
- Test tag: `save_manual_tx_button`

---

## 37. Expenses Screen

**Route:** `expenses`  
**Entry point:** Dashboard quick action "Expenses", Reports menu  
**Exit point:** Back arrow

### Top Bar
- Title: "Expenses" (bold)
- Back arrow

### Filter Chips
- "All" + first 4 categories: Rent, Electricity, Water, Telephone

### Export Button
- "Export to CSV" — exports filtered expenses, Toast with save location

### Expense List
Each card:
- Left: Category name (bold), description (or "No description"), date + payment mode
- Right: Formatted amount (bold, primary), reference number (if present)

### Empty State
- Implicit (empty LazyColumn)

### FAB
- Primary, Add icon → opens Expense Entry Form

---

## 38. Expense Entry Form

**Entry point:** FAB on Expenses list  
**Exit point:** Back arrow, Save button

### Top Bar
- Title: "Add Expense" (bold)
- Back arrow

### Fields

| # | Field | Details |
|---|-------|---------|
| 1 | Date | Read-only, formatted |
| 2 | Expense No | Read-only, auto-generated: `EXP/{financialYear}/{last4digits}` |
| 3 | Category | Read-only with dropdown + "Choose Category" button |
| 4 | Description | Text field |
| 5 | Amount | Decimal keyboard |
| 6 | Payment Mode | FilterChips: CASH, BANK, UPI, CHEQUE |
| 7 | Reference No | Text field |
| 8 | Upload Receipt | Button → system file picker (image/pdf) |

### Categories
Rent, Electricity, Water, Telephone, Internet, Staff Salary, Transport, Packaging, Maintenance, Office Supplies, Advertising, Insurance, Bank Charges, Miscellaneous, Other

### Save Button
- Full-width, 48dp, primary, "Save Expense"
- Invalid amount: Toast "Enter a valid amount"

---

## 39. Income Screen

**Route:** `income` (not in active bottom nav)  
**Entry point:** Voucher type selection "Income"  
**Exit point:** Back arrow

### Layout
Identical structure to Expenses Screen:
- Filter chips: All, Sales Commission, Interest Received, Rental Income, Freelance Services
- Export to CSV button
- Income list (same card layout)
- FAB → Income Entry Form

---

## 40. Income Entry Form

**Entry point:** FAB on Income list  
**Exit point:** Back arrow, Save button

### Fields
Identical structure to Expense Entry Form with:
- Income No: auto-generated: `INC/{financialYear}/{last4digits}`
- Categories: Sales Commission, Interest Received, Rental Income, Freelance Services, Consulting Fees, Investment Returns, Grants, Donations, Refunds Received, Other Income
- Save button: "Save Income"

---

## 41. Quick Sale Screen

**Route:** `quick_sale`  
**Entry point:** Dashboard quick action "Quick Sale"  
**Exit point:** Back arrow

### Top Bar
- Bolt (lightning) icon + "Quick Sale" (bold)
- Back arrow

### Customer Selection Card
- "Walk-in Customer" (semi-bold) + Switch toggle
- When switch OFF: Up to 6 FilterChip items for CUSTOMER/BOTH type parties

### Two-Column Layout

**Left Column (42% width):**
1. "Search products" text field
2. Unit filter chips: "All" + up to 3 distinct product units
3. Product cards: name (bold), sale rate (primary), stock count

**Right Column (58% width):**
1. "Cart" header (bold, 16sp)
2. Cart item list: product name, qty x rate, minus button, total
3. Payment mode chips: CASH, UPI, CARD
4. Total card: "Total" label + formatted total (24sp, extra-bold, primary)
5. "BILL NOW" button: Full-width, 56dp, dark green (`#2E7D32`), white bold text

### Cart Behavior
- Tap product: adds to cart (qty=1) or increments qty
- Tap minus on cart item: decrements qty (removes at 0)
- Empty cart: Toast "Add items first"
- Non-empty: Generates voucher, saves, clears cart, Toast "Quick sale saved"

---

## 42. Invoice Viewer Screen

**Route:** `invoice/{voucherId}`  
**Entry point:** Tap voucher from list (desktop), print flow, voucher detail  
**Exit point:** Back arrow, Close button

### Top Bar
- Title: "Invoice Viewer" (bold) + invoice number subtitle (11sp, gray)
- Back arrow
- Action icons: Zoom Out (-), Zoom In (+), Refresh, Edit (pencil)
- Background: White

### Invoice Content
- Card (10dp rounded, white) containing WebView with invoice HTML
- Loading: Centered `CircularProgressIndicator`
- Progress: Full-width `LinearProgressIndicator`

### Bottom Action Bar
5 action buttons in a row:

| # | Label | Icon | Behavior |
|---|-------|------|----------|
| 1 | Share | Share | Generate PDF → system share sheet |
| 2 | Save As | Download | Generate PDF → system file picker. Snackbar with "Open" action. |
| 3 | Print | Print | Generate PDF → Android PrintManager |
| 4 | WhatsApp | Send | Generate PDF → WhatsApp share |
| 5 | Close | Close | Navigate back |

Each button: 64dp width, icon (24dp) + label (10sp). Disabled: gray `#AAAAAA`.

### Convert Button (conditional)
- Shown for QUOTATION or DELIVERY_CHALLAN types
- Full-width, 48dp, primary
- "Convert to Invoice" or "Convert Challan to Invoice"

---

## 43. Ledger Books Screen

**Route:** `ledger_books`  
**Entry point:** Settings menu, Reports menu  
**Exit point:** Back arrow

### Top Bar
- Title: "Ledger Books" (bold, primary text)
- Back arrow
- Background: `AppColors.topBarBg`

### Subtitle
- "Full ledger chart with current debit / credit balances" — secondary, 13sp

### Ledger List
- Grouped by `groupName`, sorted alphabetically
- 4dp spacing, 120dp bottom padding

#### Section Headers
- Full-width, `sectionHeaderBg` background, rounded top (8dp)
- Group name: semi-bold, 13sp, primary color

#### Account Rows
- Alternating backgrounds (even/odd)
- 0.5dp border, 12dp horizontal / 10dp vertical padding
- **Left:** Account name (13sp) + "System account" label (11sp, tertiary) if applicable
- **Right:** Balance text: "Settled" (credit color if < 0.01), "₹ {formatted} DR" (debit color), or "₹ {formatted} CR" (credit color), 12sp, semi-bold
- **Edit icon:** Pencil (primary) → opens Edit Ledger Sheet

#### View Dialog (on row tap)
- Account name title (bold)
- Body: Group, Opening Balance, Balance Type, Phone/Email/Address (if party), "System account" note
- "Edit" button + "Close" text button

#### Edit Ledger Bottom Sheet
- Title: "Edit Ledger" (bold, 18sp)
- Fields: Ledger Name (disabled for system), Under Group (disabled for system), Opening Balance (decimal with "Rs" prefix), DR/CR chips, Phone/Email/Address (if party)
- Save button: primary

### Empty State
- "No ledger accounts configured yet."

---

## 44. Barcode Scanner Dialog

**Type:** `AlertDialog` with camera preview  
**Entry point:** Search icon in voucher item sheet, barcode field in product editor

### Permission Flow
- Requests camera permission if not granted
- Permission denied dialog: "Camera permission needed" + "Allow camera access to scan a barcode or QR code." + "Close"

### Scanner UI
- Title: "Scan barcode"
- Full-screen black background with camera preview
- White square guide overlay (220dp)
- `CircularProgressIndicator` (white) while scanning
- "Cancel" text button

### Behavior
- Uses ML Kit BarcodeScanning
- On successful scan: returns scanned value, dismisses dialog

---

## 45. Parsed Bill Items Dialog

**Type:** `AlertDialog`  
**Entry point:** "Extract Items from Bill" button (OCR scan of supplier bill)

### UI Elements
- Title: "Items found in bill"
- Subtitle: "Review and confirm. Edit if needed."
- Scrollable list (max 420dp) of parsed items, each in a Card:
  - Checkbox + "Include" label + "Delete" text button
  - Name field (editable)
  - Qty + Rate fields (side by side, decimal)
  - HSN + Unit fields (side by side)
- Confirm: "Add N items to voucher" button
- Dismiss: "Skip — add manually" text button

---

## 46. Universal Selection System

### UniversalSelectionTopAppBar
- Shown when selection mode is active
- Title: "{N} selected" (primary text)
- Left: Close (X) button
- Right: "Select All" / "Deselect All" toggle + "Delete" button (red, conditional)

### UniversalSelectionIndicator
- 24dp circular checkbox
- Animated color transitions (220ms): selected = primary fill + white check, unselected = transparent fill + border

### Behavior
- Long press on list items enters selection mode
- Tap toggles individual selection
- Select All/Deselect All toggles all visible items
- Delete removes all selected items with confirmation

---

## 47. Theme System

### 5 Available Themes

| Theme | Screen Background | Card/Surface | Accent Primary | Accent Light |
|-------|------------------|--------------|----------------|--------------|
| BEACH (default) | `#FDF6EC` warm cream | `#FFF8F0` | `#1A73E8` blue | `#E8F0FE` |
| BLUE | `#F2F4F7` cool gray | `#FFFFFF` | `#1A73E8` blue | `#EEF2FF` |
| GREEN | `#F1F8F4` mint | `#FFFFFF` | `#1E8A3C` green | `#E6F4EA` |
| PURPLE | `#F5F0FF` lavender | `#FFFFFF` | `#6200EA` purple | `#EDE7F6` |
| TEAL | `#F1FBF8` aqua | `#FFFFFF` | `#0F9D8A` teal | `#E0F4EF` |

### Theme Properties (11 per theme)
`name`, `backgroundPrimary`, `backgroundSecondary`, `backgroundTertiary`, `accentPrimary`, `accentLight`, `textPrimary`, `textSecondary`, `textTertiary`, `statusBarColor`, `statusBarDarkIcons`

### Static Color Tokens (shared across themes)

**Primary:** `#1A73E8` (blue), white text on primary  
**Text:** `#0D0D0D` (primary), `#444444` (secondary), `#AAAAAA` (tertiary)  
**Borders:** `#E0E4EA`  
**Status:** `#1976D2` (success — blue), `#D32F2F` (danger), `#FFA500` (warning), `#17A2B8` (info)  
**Balance:** `#C62828` (debit red), `#1E8A3C` (credit green)  
**Badges:** Sale (green tint), Purchase (orange tint), Return (red tint), Receipt (blue tint), Payment (blue tint)

### Persistence
- Stored in SharedPreferences (`"selected_theme"` in `"zerobook_pref"`)
- Default fallback: BEACH
- "DARK" string maps to TEAL (no actual dark mode exists)

### Typography
- System font throughout (FontFamily.Default)
- Only `bodyLarge` customized: 16sp, Normal weight, 24sp line height, 0.5sp letter spacing
- All other styles inherit Material 3 defaults

---

## 48. Animation & Motion System

### Screen Transitions
- **Forward navigation:** Slide in from right (1/12 width) + fade from 0.02 alpha, 90ms
- **Back navigation:** Slide in from left (1/12 width) + fade, 90ms

### Press Feedback
- Scale: 1.0 → 0.97 on press, spring back
- TranslationY: pushes down 0.75dp

### Bottom Navigation
- Selected icon/label: scale 1.02x, 70ms
- Unselected: scale 1.0x

### Dialog Transitions
- Enter: slide up 1/6 height + fade from 0.3 + scale from 0.96
- Exit: slide down 1/8 height + fade + scale to 0.985

### FAB Entrance
- Scale: 0.82 → 1.0
- Rotation: -12deg → 0deg
- Alpha: 0 → 1, spring-based

### Micro-Animations
- **AnimatedCounter:** Value counts up from 0 over 1000ms (EaseOutCubic)
- **Floating card effect:** Gentle vertical bob (0 → -4px), 2000ms cycle
- **Shimmer loading:** Alpha pulse 0.3 → 0.8, 1500ms cycle
- **Pulse:** Scale 1.0 → 1.05, 1500ms cycle
- **Bounce:** TranslationY 0 → -8px → 0, 600ms total

### Reduced Motion Support
- Reads `Settings.Global.ANIMATOR_DURATION_SCALE`
- Compact screen detection (width <= 360dp or height <= 640dp)
- When reduced: durations cut to 60%, spring damping increased (less bounce)

---

## 49. Navigation Map

### Complete Route Table

| Route | Screen | Top-Level | Back Behavior |
|-------|--------|-----------|---------------|
| `dashboard` | DashboardScreen | Yes | App exit |
| `vouchers` | VouchersScreen | Yes | Preserves state |
| `parties` | PartiesScreen | Yes | Preserves state |
| `settings` | SettingsScreen | Yes | Preserves state |
| `reports` | ReportsScreen | No | popBackStack |
| `products` | ProductsScreen | No | popBackStack |
| `bank_cash` | BankCashScreen | No | popBackStack |
| `expenses` | ExpensesScreen | No | popBackStack |
| `quick_sale` | QuickSaleScreen | No | popBackStack |
| `new_voucher?voucherId={id}` | NewVoucherScreen | No | popBackStack |
| `invoice/{voucherId}` | InvoiceScreen | No | popBackStack |
| `ledger_books` | LedgerListScreen | No | popBackStack |
| `party_detail/{partyId}` | PartyDetailScreen | No | popBackStack |

### Navigation Entry Points per Screen

| Screen | How User Reaches It |
|--------|-------------------|
| Dashboard | App launch, bottom nav "Dashboard" |
| Vouchers | Bottom nav "Vouchers", Dashboard "View All Transactions" |
| Parties | Bottom nav "Parties" |
| Settings | Bottom nav "Settings" |
| Reports | Dashboard quick action "Reports" |
| Products | Settings "Manage Products Master" |
| Bank & Cash | Dashboard quick actions "Receipt" or "Payments" |
| Expenses | Dashboard quick action "Expenses", Reports "Expenses" |
| Quick Sale | Dashboard quick action "Quick Sale" |
| New Voucher | Vouchers FAB (+), Dashboard sale/purchase actions |
| Invoice | Voucher list tap (desktop), print flow, save flow |
| Ledger Books | Settings "Ledger Books", Reports "Ledger Books" |
| Party Detail | Tap party card (mobile), auto-select (desktop) |

### Sub-Screens (within parent navigation)

| Sub-Screen | Parent Screen |
|------------|--------------|
| Business Profile Settings | Settings |
| Customize Settings | Settings |
| Theme Settings | Settings |
| Financial Year Settings | Settings |
| PIN Protection Settings | Settings |
| About Settings | Settings |
| Email Automation Settings | Settings |

### Dialog/Sheet Entry Points

| Component | Triggered From |
|-----------|---------------|
| Filter Bottom Sheet | Filter button on Vouchers list |
| Sort Bottom Sheet | Sort button on Vouchers list |
| Voucher Item Entry Sheet | "Add Item" in voucher form, tap existing item |
| Party Picker Sheet | "Select Party" in voucher form |
| Create Party Inline Sheet | "Create new customer/supplier" in party picker |
| Stock Report Sheet | "Stock" button in voucher form |
| Barcode Scanner Dialog | Search icon in item sheet, barcode field in product editor |
| Parsed Bill Items Dialog | "Extract Items from Bill" OCR button |
| Date Picker | Date fields in filter and form dialogs |
| Sample Data Dialog | Setup screen submit |
| Changelog Dialog | App launch (new version) |
| Delete Confirmation | Delete actions across all list screens |
| Confirm Save Dialog | Voucher save buttons |
| UPI Payment Dialog | UPI + Sale type voucher save |
| Print Receipt Dialog | Successful voucher save |
| Product Detail Dialog | Tap product card |
| HSN Lookup Dialog | "Find HSN" button |
| Edit Ledger Sheet | Edit icon on ledger row |
| Progress Details Dialog | Tap progress tracker card |
| KPI Analytics Popup | Tap any KPI card |

---

## 50. Responsive Layout Rules

### Breakpoint
- **Phone:** < 600dp width
- **Tablet:** >= 600dp width

### Layout Changes by Screen

| Screen | Phone | Tablet |
|--------|-------|--------|
| Dashboard | Single column, 16dp padding | Single column, 24dp padding, larger cards |
| Vouchers | Single column, tap → navigate | Split-pane: 360dp list + detail panel |
| Parties | Single column, tap → navigate | Split-pane: 360dp list + detail/form |
| Settings | Single column, menu → sub-mode | Split-pane: 360dp menu + detail |
| Reports | Menu → detail navigation | Split-pane: 360dp menu + report |
| Setup | Fields stacked vertically, 16dp padding | Fields side-by-side where possible, 24dp padding, centered at 760dp max |
| Invoice | Full-width preview | Full-width with side actions |

### Size Adjustments

| Element | Phone | Tablet |
|---------|-------|--------|
| KPI card width | 260dp | 320dp |
| KPI card height | 120dp | 140dp |
| Quick action icon container | 46dp | 52dp |
| Quick action icon | 20dp | 24dp |
| Analytics popup width | 92% screen | 70% screen |
| Logo (dashboard) | 30dp | 36dp |
| App name (dashboard) | 17sp | 20sp |
| Filter chip font | 11sp | 9sp (desktop) |
| Card padding | 14dp | 16dp |

---

*End of Frontend UI Specification Document*
