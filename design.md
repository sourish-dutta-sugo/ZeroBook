# ZeroBook — Complete Design Specification

**Version:** 3.0  
**Date:** July 2026  
**Platform:** Android (Jetpack Compose, Material 3)  
**Scope:** All screens, sub-screens, dialogs, and button behaviors  

---

## Table of Contents

1. [Design Philosophy](#1-design-philosophy)
2. [Color System](#2-color-system)
3. [Typography](#3-typography)
4. [Spacing & Shapes](#4-spacing--shapes)
5. [Application Entry Flow](#5-application-entry-flow)
6. [Main Application Shell](#6-main-application-shell)
7. [Bottom Navigation Bar](#7-bottom-navigation-bar)
8. [Dashboard Screen](#8-dashboard-screen)
9. [Vouchers Screen](#9-vouchers-screen)
10. [Parties Screen](#10-parties-screen)
11. [Settings Screen](#11-settings-screen)
12. [Reports Screen](#12-reports-screen)
13. [Products Screen](#13-products-screen)
14. [Bank & Cash Register Screen](#14-bank--cash-register-screen)
15. [Expenses Screen](#15-expenses-screen)
16. [Income Screen](#16-income-screen)
17. [Quick Sale Screen](#17-quick-sale-screen)
18. [Invoice Viewer Screen](#18-invoice-viewer-screen)
19. [Ledger Books Screen](#19-ledger-books-screen)
20. [Voucher Entry Form](#20-voucher-entry-form)
21. [Party Add/Edit Form](#21-party-addedit-form)
22. [Party Detail Screen](#22-party-detail-screen)
23. [Product Editor Screen](#23-product-editor-screen)
24. [Manual Transaction Form](#24-manual-transaction-form)
25. [Expense Entry Form](#25-expense-entry-form)
26. [Income Entry Form](#26-income-entry-form)
27. [Business Profile Settings](#27-business-profile-settings)
28. [Theme & Colors Settings](#28-theme--colors-settings)
29. [Customize Settings](#29-customize-settings)
30. [Financial Year Settings](#30-financial-year-settings)
31. [PIN Protection Settings](#31-pin-protection-settings)
32. [About Settings](#32-about-settings)
33. [Email Automation Settings](#33-email-automation-settings)
34. [Dialogs & Bottom Sheets](#34-dialogs--bottom-sheets)
35. [Button Behaviors Reference](#35-button-behaviors-reference)
36. [Navigation Map](#36-navigation-map)
37. [Responsive Layout Rules](#37-responsive-layout-rules)

---

## 1. Design Philosophy

1. **Finance-first legibility.** Rupee amounts are always in monospace. Labels are small and quiet. Data does the talking.
2. **Trust through consistency.** Every screen uses the same colors, fonts, spacing, and motion. No surprises.
3. **Flat over fancy.** No 3D renders, no gradients, no count-up animations, no staggered list entrances. Solid fills, clean lines, immediate data.
4. **Local-first architecture.** All data stored locally on device. No cloud sync required.
5. **Indian business context.** GST compliance, Indian currency formatting, local business workflows.

---

## 2. Color System

### Primary Palette

| Token | Hex | Usage |
|-------|-----|-------|
| PRIMARY | `#1A5C45` | Headers, buttons, active states |
| PRIMARY_LIGHT | `#E8F5E9` | Tint backgrounds, badges |
| INCOME_GREEN | `#2E7D52` | Credit/income figures |
| EXPENSE_RED | `#C0392B` | Debit/expense figures |
| ACCENT_GOLD | `#B8860B` | Highlights, totals |
| BALANCE_CARD | `#C8A96E` | Dashboard balance card |

### Neutral Palette

| Token | Hex | Usage |
|-------|-----|-------|
| SURFACE | `#F8F7F4` | Screen background |
| SURFACE_WHITE | `#FFFFFF` | Cards, sheets |
| SURFACE_VARIANT | `#EFEFED` | Secondary cards, inputs |
| ON_SURFACE | `#1C1C1E` | Primary text |
| ON_SURFACE_70 | `#4A4A4A` | Secondary text |
| ON_SURFACE_50 | `#888888` | Tertiary text, labels |
| OUTLINE | `#E0E4EA` | Borders, dividers |

### Status Colors

| Color | Hex | Usage |
|-------|-----|-------|
| SUCCESS | `#1976D2` | Paid status, success messages |
| DANGER | `#D32F2F` | Error states, overdue |
| WARNING | `#FFA500` | Low stock, partial payments |
| INFO | `#17A2B8` | Informational badges |

### Badge Colors

| Badge | Background | Text |
|-------|------------|------|
| Sale | `#E8F5E9` | `#2E7D52` |
| Purchase | `#FFF3E0` | `#E65100` |
| Receipt | `#E3F2FD` | `#1565C0` |
| Payment | `#FFEBEE` | `#C62828` |
| Return | `#FCE4EC` | `#AD1457` |

---

## 3. Typography

### Font Families
- **Primary:** System default (FontFamily.Default)
- **Monospace:** For all currency amounts (FontFamily.Monospace)

### Type Scale

| Style | Size | Weight | Usage |
|-------|------|--------|-------|
| Display Large | 28sp | Bold | Large rupee amounts |
| Display Medium | 22sp | Bold | Card totals |
| Display Small | 18sp | Medium | Sub-totals |
| Headline Large | 18sp | SemiBold | Screen titles |
| Headline Medium | 16sp | SemiBold | Section titles |
| Headline Small | 14sp | SemiBold | Card titles |
| Title Large | 16sp | Medium | List primary text |
| Title Medium | 14sp | Medium | List secondary text |
| Title Small | 12sp | Medium | Labels, badges |
| Body Large | 16sp | Normal | Body text |
| Body Medium | 14sp | Normal | Descriptions |
| Body Small | 12sp | Normal | Fine print |
| Label Large | 14sp | Medium | Button text |
| Label Medium | 12sp | Medium | Chip text |
| Label Small | 11sp | Medium | Section headers |

---

## 4. Spacing & Shapes

### Spacing Scale

| Token | Value | Usage |
|-------|-------|-------|
| xs | 4dp | Tight spacing |
| sm | 8dp | Card padding, list gaps |
| md | 12dp | Section spacing |
| lg | 16dp | Screen padding |
| xl | 24dp | Large sections |
| xxl | 32dp | Major sections |

### Border Radius

| Shape | Radius | Usage |
|-------|--------|-------|
| Card | 12dp | Cards, sheets |
| Chip | 8dp | Filter chips, badges |
| Button | 10dp | Buttons |
| Input | 8dp | Text fields |
| Avatar | Circle | Profile circles |
| BottomSheet | Top 20dp | Bottom sheets |

---

## 5. Application Entry Flow

### Stage 1: System Splash Screen
- **Type:** AndroidX SplashScreen API
- **Background:** `#FAF8F5` (warm off-white)
- **Icon:** `@drawable/logo_transparent` (centered)
- **Behavior:** Auto-dismisses when Activity initializes

### Stage 2: Database Initialization
- **Background:** `AppColors.screenBg`
- **Loading:** `CircularProgressIndicator` (blue) + "Initializing Secure Database..."
- **Error:** White card with error message + "Retry Connection" button
- **Success:** Proceeds to Stage 3

### Stage 3: In-App Splash Screen
- **Type:** Compose `Crossfade` (240ms)
- **Content:** Logo with scale+fade animation
- **Exit:** Auto-advances after 900ms

### Stage 4: Setup Status + PIN Check
- **Loading:** "Preparing ZeroBook..." with spinner
- **If setup not completed:** Shows `SetupScreen`
- **If PIN required:** Shows `PinLockScreen`
- **Otherwise:** Shows main app

### Stage 5: Changelog Dialog
- **Type:** `AlertDialog` (non-dismissable)
- **Content:** Version changes list
- **Exit:** "Got it" button

---

## 6. Main Application Shell

- Root `Scaffold` with `statusBarsPadding()` and `navigationBarsPadding()`
- `containerColor = AppColors.screenBg`
- No global TopAppBar (each screen provides its own)
- Bottom navigation bar (conditionally visible)
- `NavHost` with navigation transitions

---

## 7. Bottom Navigation Bar

**Visibility:** Only on top-level destinations  
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

#### Empty States
- No vouchers: Assignment icon (56dp), `"No vouchers yet"`, `"Tap + to create your first voucher"`
- No matches: FilterList icon (48dp), `"No matching transactions yet."`

#### "View All Transactions" Button
- `TextButton`, centered, `"View All Transactions"` in primary color, semi-bold
- Navigates to Vouchers screen

### 8.8 Back Behavior
- First back press: clears search query if non-blank
- Second back press: dismisses analytics popup if open

---

## 9. Vouchers Screen

**Route:** `vouchers`  
**Entry point:** Bottom nav "Vouchers" tab  
**Exit point:** Back arrow, bottom nav, FAB to create new

### Layout Modes
- **Mobile:** Single-column `Scaffold`
- **Desktop (>= 600dp):** Side-by-side `Row` — 360dp list panel + detail panel

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

## 10. Parties Screen

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

## 11. Settings Screen

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

## 12. Reports Screen

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

## 13. Products Screen

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

## 14. Bank & Cash Register Screen

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

## 15. Expenses Screen

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

## 16. Income Screen

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

## 17. Quick Sale Screen

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

## 18. Invoice Viewer Screen

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

## 19. Ledger Books Screen

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

## 20. Voucher Entry Form

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

## 21. Party Add/Edit Form

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

## 22. Party Detail Screen

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

## 23. Product Editor Screen

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

## 24. Manual Transaction Form

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

## 25. Expense Entry Form

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

## 26. Income Entry Form

**Entry point:** FAB on Income list  
**Exit point:** Back arrow, Save button

### Fields
Identical structure to Expense Entry Form with:
- Income No: auto-generated: `INC/{financialYear}/{last4digits}`
- Categories: Sales Commission, Interest Received, Rental Income, Freelance Services, Consulting Fees, Investment Returns, Grants, Donations, Refunds Received, Other Income
- Save button: "Save Income"

---

## 27. Business Profile Settings

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

## 28. Theme & Colors Settings

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

## 29. Customize Settings

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

## 30. Financial Year Settings

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

## 31. PIN Protection Settings

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

## 32. About Settings

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

## 33. Email Automation Settings

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

## 34. Dialogs & Bottom Sheets

### 34.1 Voucher Type Selection
**Route:** Part of `NewVoucherScreen` (step 1)  
**Entry point:** FAB on Vouchers list  
**Exit point:** Back arrow or type card tap

#### Top Bar
- Title: `"New Voucher"`
- Subtitle: `"Select voucher type"` (14sp, secondary)
- Back arrow

#### Voucher Type Grid
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

### 34.2 Voucher Item Entry Sheet
**Type:** `ModalBottomSheet`  
**Entry point:** "Add Item" button or tap existing item  
**Exit point:** Cancel or Save button

#### Title
- `"Add Item"` or `"Edit Item"` (18sp, bold)

#### Fields

| # | Field | Details |
|---|-------|---------|
| 1 | Product Name * | Search/type field with dropdown. Trailing: Search icon (barcode scanner), dropdown arrow. Dropdown shows max 8 matching products (name + HSN + rate). "Create new product" option at bottom. |
| 2 | HSN/SAC Code | Digits only, max 8 chars. Suggested HSN chip after 1.2s debounce. |
| 3 | Quantity * | Decimal input. Error: "Quantity must be greater than zero" |
| 4 | Unit | Dropdown: PCS, KG, LTR, MTR, BOX, BAG, NOS |
| 5 | Rate (₹) * | Decimal input with ₹ prefix. Error: "Rate must be greater than zero" |
| 6 | Discount | Decimal input + toggle chip (% / ₹) |
| 7 | GST Rate (conditional) | Dropdown: 0%, 5%, 12%, 18%, 28% (or global rate chip if enabled) |

#### Live Calculation Card
- Taxable Amount: formatted currency
- GST Amount: formatted currency
- Item Total: formatted currency (bold)

#### Action Buttons
- **Cancel:** `OutlinedButton`, weight(1f)
- **Save:** `Button` primary, weight(1f), `"Add to Invoice"` or `"Update Item"`

### 34.3 Voucher Filter & Sort Sheets

#### Filter Bottom Sheet
- Title: `"Filter vouchers"` + `"Clear All"` text button
- Fields (12dp spacing):
  1. **Voucher Type:** `FlowRow` of `FilterChip`s — 16 options
  2. **Date:** Two `OutlinedButton`s for start/end date → opens `DatePickerDialog`
  3. **Party:** `OutlinedTextField` for party name
  4. **Payment Status:** `FlowRow` — Any status, Paid, Partially paid, Unpaid
  5. **Amount:** Min and Max decimal fields
  6. **Reference:** `OutlinedTextField`
- Bottom: `"Cancel"` + `"Apply"` buttons

#### Sort Bottom Sheet
- Title: `"Sort vouchers"`
- 8 options as clickable rows with check icon when selected
- Bottom: `"Cancel"` + `"Apply"` buttons

### 34.4 Barcode Scanner Dialog
**Type:** `AlertDialog` with camera preview  
**Entry point:** Search icon in voucher item sheet, barcode field in product editor

#### Permission Flow
- Requests camera permission if not granted
- Permission denied dialog: "Camera permission needed" + "Allow camera access to scan a barcode or QR code." + "Close"

#### Scanner UI
- Title: "Scan barcode"
- Full-screen black background with camera preview
- White square guide overlay (220dp)
- `CircularProgressIndicator` (white) while scanning
- "Cancel" text button

#### Behavior
- Uses ML Kit BarcodeScanning
- On successful scan: returns scanned value, dismisses dialog

### 34.5 Parsed Bill Items Dialog
**Type:** `AlertDialog`  
**Entry point:** "Extract Items from Bill" button (OCR scan of supplier bill)

#### UI Elements
- Title: "Items found in bill"
- Subtitle: "Review and confirm. Edit if needed."
- Scrollable list (max 420dp) of parsed items, each in a Card:
  - Checkbox + "Include" label + "Delete" text button
  - Name field (editable)
  - Qty + Rate fields (side by side, decimal)
  - HSN + Unit fields (side by side)
- Confirm: "Add N items to voucher" button
- Dismiss: "Skip — add manually" text button

### 34.6 Universal Selection System

#### UniversalSelectionTopAppBar
- Shown when selection mode is active
- Title: "{N} selected" (primary text)
- Left: Close (X) button
- Right: "Select All" / "Deselect All" toggle + "Delete" button (red, conditional)

#### UniversalSelectionIndicator
- 24dp circular checkbox
- Animated color transitions (220ms): selected = primary fill + white check, unselected = transparent fill + border

#### Behavior
- Long press on list items enters selection mode
- Tap toggles individual selection
- Select All/Deselect All toggles all visible items
- Delete removes all selected items with confirmation

---

## 35. Button Behaviors Reference

### 35.1 Primary Actions

| Button | Location | Behavior |
|--------|----------|----------|
| Save & Post | Voucher Entry (Sale/Return) | Saves voucher without printing |
| Print | Voucher Entry (Sale/Return) | Saves voucher and opens print dialog |
| Save | Voucher Entry (Other types) | Saves voucher |
| Save Party | Party Form | Creates/updates party |
| Save Product | Product Editor | Creates/updates product |
| Save Expense | Expense Form | Creates expense entry |
| Save Income | Income Form | Creates income entry |
| Commit Transaction | Manual Transaction | Records manual transaction |
| Initialize Business | Setup Screen | Validates and saves business profile |
| Save Financial Year | FY Settings | Updates financial year |
| Apply Parameters | PIN Settings | Saves PIN configuration |
| Save Alterations Settings | Business Profile | Updates business profile |
| Save | Customize Settings | Saves dashboard customization |

### 35.2 Navigation Actions

| Button | Location | Behavior |
|--------|----------|----------|
| Back arrow | All sub-screens | Navigates back to parent screen |
| Bottom nav tabs | Dashboard, Vouchers, Parties, Settings | Switches between top-level screens |
| View All Transactions | Dashboard | Navigates to Vouchers screen |
| Quick action buttons | Dashboard | Navigates to respective screens |
| Convert to Invoice | Invoice Viewer (Quotation/Challan) | Converts document type |
| Send Email Payment Reminder | Party Detail | Opens email client with pre-filled reminder |

### 35.3 Export Actions

| Button | Location | Behavior |
|--------|----------|----------|
| Export to CSV | Expenses, Income, Stock Report | Exports data to CSV file |
| Import CSV | Settings | Opens system file picker for CSV import |
| Backup Database | Settings | Backs up SQLite database |
| Share | Invoice Viewer | Generates PDF and opens share sheet |
| Save As | Invoice Viewer | Generates PDF and opens file picker |
| Print | Invoice Viewer | Generates PDF and opens print manager |
| WhatsApp | Invoice Viewer | Generates PDF and shares via WhatsApp |

### 35.4 Toggle Actions

| Toggle | Location | Behavior |
|--------|----------|----------|
| KPI Animation Mode | Customize Settings | Switches between 3 animation modes |
| Show Progress Tracker | Customize Settings | Enables/disables progress tracker on dashboard |
| PIN Code Protection | PIN Settings | Enables/disables app PIN lock |
| Walk-in Customer | Quick Sale | Toggles between walk-in and selected customer |
| Email Automation | Email Settings | Enables/disables automated email reminders |

### 35.5 Destructive Actions

| Button | Location | Behavior |
|--------|----------|----------|
| Delete | Voucher List (selection mode) | Deletes selected vouchers with confirmation |
| Delete | Party List (selection mode) | Deletes selected parties with confirmation |
| Delete | Product List (selection mode) | Deletes selected products with confirmation |
| Clear All | Voucher Filters | Clears all active filters |
| CLR | PIN Lock Screen | Clears PIN input |
| Clear All Data | Settings (danger zone) | Clears all app data with confirmation |

### 35.6 Modal Actions

| Button | Location | Behavior |
|--------|----------|----------|
| Got it | Changelog Dialog | Dismisses dialog, marks version as seen |
| Yes, Import | Sample Data Dialog | Saves profile, loads sample data, proceeds |
| No, Start Clean | Sample Data Dialog | Saves profile without sample data, proceeds |
| Cancel | Various dialogs | Dismisses dialog without action |
| Close | Various dialogs | Dismisses dialog |
| Apply | Filter/Sort sheets | Applies selected filters/sort options |
| Add Item | Voucher Entry | Opens item entry sheet |
| Add to Invoice | Item Entry Sheet | Adds item to voucher |
| Update Item | Item Entry Sheet | Updates existing item |
| Create new product | Item Entry Sheet | Opens product editor inline |
| Receive Payment | Receivables Report | Opens payment dialog |
| Pay | Payables Report | Opens payment dialog |
| Manual Override | UPI Payment Dialog | Skips simulated UPI flow |
| Close & Exit | Print Receipt Dialog | Dismisses print dialog |

---

## 36. Navigation Map

### 36.1 Complete Route Table

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

### 36.2 Navigation Entry Points per Screen

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

### 36.3 Sub-Screens (within parent navigation)

| Sub-Screen | Parent Screen |
|------------|--------------|
| Business Profile Settings | Settings |
| Customize Settings | Settings |
| Theme Settings | Settings |
| Financial Year Settings | Settings |
| PIN Protection Settings | Settings |
| About Settings | Settings |
| Email Automation Settings | Settings |

### 36.4 Dialog/Sheet Entry Points

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

## 37. Responsive Layout Rules

### 37.1 Breakpoint
- **Phone:** < 600dp width
- **Tablet:** >= 600dp width

### 37.2 Layout Changes by Screen

| Screen | Phone | Tablet |
|--------|-------|--------|
| Dashboard | Single column, 16dp padding | Single column, 24dp padding, larger cards |
| Vouchers | Single column, tap → navigate | Split-pane: 360dp list + detail panel |
| Parties | Single column, tap → navigate | Split-pane: 360dp list + detail/form |
| Settings | Single column, menu → sub-mode | Split-pane: 360dp menu + detail |
| Reports | Menu → detail navigation | Split-pane: 360dp menu + report |
| Setup | Fields stacked vertically, 16dp padding | Fields side-by-side where possible, 24dp padding, centered at 760dp max |
| Invoice | Full-width preview | Full-width with side actions |

### 37.3 Size Adjustments

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

*Document version: 3.0 — July 2026*  
*Codebase: ZeroBook Android (Jetpack Compose, Material3, Room, MVVM)*