# ZeroBook — Frontend Design Document

**Version:** 1.0
**Date:** July 2026
**Scope:** Full frontend redesign using existing codebase assets, components, and navigation structure
**Reference:** Deopay screenshot + Finance Management UI/UX Kit (Budget Tracker)

---

## 1. GUIDING PRINCIPLES

1. **Preserve what works.** Every existing screen, composable, ViewModel, data entity, and drawable asset stays. We restyle, not rebuild.
2. **One palette, zero themes.** Kill the 5-theme system (Beach, Blue, Green, Purple, Teal). Replace with a single, predictable palette.
3. **Trust through consistency.** Every screen uses the same colors, fonts, spacing, and motion. No surprises.
4. **Finance-first legibility.** Rupee amounts are always in monospace. Labels are small and quiet. Data does the talking.
5. **Flat over fancy.** No 3D renders, no gradients, no count-up animations, no staggered list entrances. Solid fills, clean lines, immediate data.

---

## 2. WHAT STAYS (PRESERVED ASSETS & COMPONENTS)

### 2.1 Drawable Assets (ALL PRESERVED)
| Asset | File | Usage | Status |
|---|---|---|---|
| Logo icon (PNG) | `logo_icon.png` | Splash, launcher | KEEP |
| Logo transparent | `logo_transparent.png` | Dashboard header, about | KEEP |
| Logo mark (vector) | `logo_mark.xml` | App icon variant | KEEP |
| Launcher foreground | `ic_launcher_foreground.xml` | Adaptive icon | KEEP |
| Launcher background | `ic_launcher_background.xml` | Adaptive icon | KEEP |
| All mipmap densities | `mipmap-*dpi/` | Launcher | KEEP |

### 2.2 Preserved Composable Components
| Component | File | What It Does | Design System Mapping |
|---|---|---|---|
| `RetailTextField` | `SharedComponents.kt` | Standard text input | Restyle to 8dp radius, PRIMARY focus border |
| `StateDropdownMenu` | `SharedComponents.kt` | Indian state picker | Keep as-is (functional, not visual) |
| `PinLookupResult` + `fetchPinLookup` | `SharedComponents.kt` | Pin code API lookup | Keep as-is (data, not UI) |
| `GstinValidationFeedback` | `GstinHelpers.kt` | GSTIN real-time feedback | Keep, restyle colors to palette |
| `themedInputColors` | `ThemeAwareComponents.kt` | Theme-aware input colors | Simplify to single palette |
| `zeroBookInputColors` | `InputColors.kt` | Material3 TextFieldColors | Restyle to single palette |
| `UniversalSelectionController` | `UniversalSelectionController.kt` | Multi-select state machine | KEEP (functional) |
| `UniversalSelectionIndicator` | `UniversalSelectionComponents.kt` | Selection checkbox overlay | KEEP (functional) |
| `UniversalSelectionTopAppBar` | `UniversalSelectionComponents.kt` | Batch action bar | KEEP, restyle to palette |
| Skeleton components | `Skeleton.kt` | Loading placeholders | KEEP, restyle shimmer to palette |
| `BarcodeScannerDialog` | `BarcodeScannerDialog.kt` | Camera barcode scan | KEEP (functional) |
| `ProfileFormSupport` | `ProfileFormSupport.kt` | Form field utilities | KEEP (functional) |
| `ProductOptionalFields` | `ProductOptionalFields.kt` | Optional product fields | KEEP (functional) |
| `PartySheets` | `PartySheets.kt` | Party detail bottom sheets | Restyle to palette |
| `VoucherItemSheet` | `VoucherItemSheet.kt` | Voucher item bottom sheet | Restyle to palette |
| `BusinessProfileSettingsSection` | `BusinessProfileSettingsSection.kt` | Business profile form | Restyle to palette |

### 2.3 Preserved Data Layer (NO CHANGES)
- `AppDatabase.kt`, `AppDaos.kt`, `Entities.kt` — Room database
- `AppRepository.kt` — Data repository
- `AppPreferences.kt` — SharedPreferences wrapper
- `FinancialYearUtils.kt` — FY logic
- `HsnLookup.kt` — HSN code lookup
- `InvoiceDefaults.kt` — Invoice defaults
- `VoucherExtras.kt` — Voucher helpers
- `Utils.kt` — Formatting (Indian currency, dates)
- `Migrations.kt` — Database migrations
- `ChangelogLoader.kt` — Changelog data

### 2.4 Preserved Services (NO CHANGES)
- `InvoiceGenerator.kt` — PDF invoice generation
- `WebViewPdfWriter.java` — WebView-based PDF rendering
- `ExportStorageManager.kt` — File export
- `CsvTransferManager.kt` — CSV import/export
- `EmailComposer.kt` — Email drafting
- `EmailAutomationService.kt` — Scheduled emails

### 2.5 Preserved ViewModels (NO CHANGES to business logic)
- `AppViewModel.kt` — All CRUD, search, financial year
- `DashboardViewModel.kt` — KPI state, animation mode

### 2.6 Preserved Navigation Structure
- Single Activity (`MainActivity.kt`)
- 4-tab bottom nav: Dashboard | Vouchers | Parties | Settings
- All 13 routes remain identical
- `ZeroBookNavHost` composable stays, only transition animations change

---

## 3. DESIGN TOKEN SYSTEM

### 3.1 Color Palette — Single System

**File to modify:** `ThemeConfig.kt` → Replace all 5 `AppTheme` variants with one `ZEROCOOL` theme.
**File to modify:** `AppColors.kt` → Update to use new palette.
**File to modify:** `Colors.kt` → Replace with new palette constants.
**File to delete:** `PremiumThemeConfig.kt` → Merge relevant tokens into `AppColors.kt`.

```kotlin
// ThemeConfig.kt — REPLACE entire AppTheme companion object

data class AppTheme(
    val name: String,
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val backgroundTertiary: Color,
    val accentPrimary: Color,
    val accentLight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val statusBarColor: Color,
    val statusBarDarkIcons: Boolean
) {
    companion object {
        // THE ONLY THEME
        val ZEROCOOL = AppTheme(
            name = "ZEROCOOL",
            backgroundPrimary = Color(0xFFF8F7F4),    // Warm off-white
            backgroundSecondary = Color(0xFFFFFFFF),   // Pure white (cards)
            backgroundTertiary = Color(0xFFEFEFED),    // Subtle card bg
            accentPrimary = Color(0xFF1A5C45),         // Deep forest green
            accentLight = Color(0xFFE8F5E9),           // Light green tint
            textPrimary = Color(0xFF1C1C1E),           // Near-black
            textSecondary = Color(0xFF4A4A4A),         // Dark gray
            textTertiary = Color(0xFF888888),          // Medium gray
            statusBarColor = Color(0xFFF8F7F4),
            statusBarDarkIcons = true
        )

        fun fromName(name: String?) = ZEROCOOL  // Always returns single theme
    }
}
```

**New semantic color tokens** (add to `AppColors.kt`):

```kotlin
// AppColors.kt — ADD these after existing properties

// Financial colors (from design system)
val INCOME_GREEN = Color(0xFF2E7D52)    // Credit / income figures
val EXPENSE_RED = Color(0xFFC0392B)     // Debit / expense figures
val ACCENT_GOLD = Color(0xFFB8860B)     // Sparingly — totals, highlights

// Badge colors (keep existing, already correct)
// badgeSale, badgePurchase, badgeReceipt, badgePayment, badgeReturn — KEEP

// Balance card
val BALANCE_CARD_BG = Color(0xFFC8A96E) // Warm gold for dashboard balance card
val BALANCE_CARD_TEXT = Color(0xFFFFFFFF)
```

### 3.2 Typography

**File to modify:** `Type.kt` → Full Material3 Typography definition.

```kotlin
// Type.kt — REPLACE entire Typography object

val Typography = Typography(
    // Display numbers (rupee amounts) — MONOSPACE
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),

    // Headlines (screen titles)
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,  // Poppins via XML or fallback
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),

    // Titles (card titles, list primary text)
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),

    // Body
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),

    // Labels (badges, section headers, small text)
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.8.sp
    )
)
```

**Rule:** Amounts use `FontFamily.Monospace`. Everything else uses `FontFamily.Default`.

### 3.3 Spacing Scale

**Add to `GlobalStyles.kt`:**

```kotlin
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}
```

### 3.4 Border Radius

**Add to `GlobalStyles.kt`:**

```kotlin
object Shape {
    val Card = RoundedCornerShape(12.dp)
    val Chip = RoundedCornerShape(8.dp)
    val Button = RoundedCornerShape(10.dp)
    val Input = RoundedCornerShape(8.dp)
    val Avatar = CircleShape
    val BottomSheet = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
}
```

### 3.5 Elevation / Shadow

Cards use `SURFACE_VARIANT` background for hierarchy, NOT shadow. Shadow only on:
- Bottom navigation bar (existing `tonalElevation = 6.dp` — KEEP)
- Bottom sheets (Material default — KEEP)
- FAB (existing `elevation = 6.dp` — KEEP)

---

## 4. FILE-BY-FILE CHANGE MAP

### 4.1 FILES TO DELETE
| File | Reason |
|---|---|
| `PremiumThemeConfig.kt` | Multi-theme system killed. Tokens merged into `AppColors.kt`. |
| `PremiumAnimations.kt` | Count-up, stagger, floating card, bounce — all removed per design system. |

### 4.2 FILES TO REWRITE
| File | What Changes |
|---|---|
| `ThemeConfig.kt` | 5 themes → 1 theme (ZEROCOOL). `ThemeViewModel.setTheme()` becomes no-op. |
| `AppColors.kt` | Add `INCOME_GREEN`, `EXPENSE_RED`, `ACCENT_GOLD`, `BALANCE_CARD_BG`. Remove multi-theme reads. |
| `Colors.kt` | Replace all colors with new palette. Keep badge colors. |
| `Type.kt` | Full Material3 Typography with monospace for amounts. |
| `GlobalStyles.kt` | Add `Spacing`, `Shape` objects. Update existing modifiers to new tokens. |
| `Theme.kt` | Simplify to single-theme `ZeroBookTheme`. Remove `LocalAppTheme` switching. |
| `ThemeHelpers.kt` | Update helper colors to new palette. |
| `ThemeAwareComponents.kt` | Simplify to single palette. |
| `InputColors.kt` | Restyle to new palette. |

### 4.3 FILES TO RESTYLE (cosmetic changes only)
| File | Changes |
|---|---|
| `PremiumMotion.kt` → rename to `ZeroBookMotion.kt` | Replace spring-heavy specs with simpler durations. Add `ZeroBookMotion` constants object. Keep `pressScale`, `premiumClickable`, `PremiumBottomNavContent` modifiers. Remove `premiumFabEntrance` (replace with simple fade-in). |
| `SplashScreen.kt` | Full green background, remove scale animation, add alpha-fade-only entrance. |
| `SetupScreen.kt` | Two-zone split layout. Progress bars instead of step indicators. |
| `DashboardScreen.kt` | Two-zone layout: green header + balance card + quick actions + recent list. |
| `VouchersScreen.kt` | Date-grouped list, color-coded circles, bottom sheet detail. |
| `PartiesScreen.kt` | Search bar at top, filter chips, balance summary strip. |
| `ReportsScreen.kt` | Flat list of reports (not card grid). |
| `SettingsScreen.kt` | Flat list with section headers (not card grid). Remove theme selector. |
| `QuickSaleScreen.kt` | Dense POS layout, item grid. |
| `BankCashScreen.kt` | Account card stacking. |
| `InvoiceScreen.kt` | Bottom sheet detail pattern. |
| `LedgerListScreen.kt` | Dr/Cr column distinction. |
| `ProductsScreen.kt` | Category list with icon + stats. |
| `ExpensesScreen.kt` | Restyle to palette. |
| `IncomeScreen.kt` | Restyle to palette. |
| `StockReportScreen.kt` | Restyle to palette. |
| `MainActivity.kt` | Update bottom nav styling. Update status bar color. Remove theme switching logic. |
| `EmailAutomationSection.kt` | REMOVE from Settings surface (not retail accounting). Keep file but don't render in Settings. |

### 4.4 FILES UNCHANGED
| File | Reason |
|---|---|
| `AppViewModel.kt` | Business logic untouched |
| `DashboardViewModel.kt` | Business logic untouched |
| `AppDatabase.kt` | Data layer untouched |
| `AppDaos.kt` | Data layer untouched |
| `Entities.kt` | Data layer untouched |
| `AppRepository.kt` | Data layer untouched |
| `AppPreferences.kt` | Data layer untouched |
| `FinancialYearUtils.kt` | Business logic untouched |
| `HsnLookup.kt` | Business logic untouched |
| `InvoiceDefaults.kt` | Business logic untouched |
| `VoucherExtras.kt` | Business logic untouched |
| `Utils.kt` | Formatting untouched |
| `Migrations.kt` | Database untouched |
| `ChangelogLoader.kt` | Data untouched |
| `InvoiceGenerator.kt` | Service untouched |
| `WebViewPdfWriter.java` | Service untouched |
| `ExportStorageManager.kt` | Service untouched |
| `CsvTransferManager.kt` | Service untouched |
| `EmailComposer.kt` | Service untouched |
| `EmailAutomationService.kt` | Service untouched |
| `FilePicker.kt` | Utility untouched |
| `UniversalSelectionController.kt` | Selection logic untouched |
| `UniversalSelectionComponents.kt` | Selection UI untouched |
| `Skeleton.kt` | Loading UI untouched (restyle only) |
| `GstinHelpers.kt` | Validation logic untouched |
| `BarcodeScannerDialog.kt` | Camera UI untouched |
| `ProfileFormSupport.kt` | Form utilities untouched |
| `ProductOptionalFields.kt` | Form fields untouched |
| `PartySheets.kt` | Sheet content untouched (restyle only) |
| `VoucherItemSheet.kt` | Sheet content untouched (restyle only) |
| `BusinessProfileSettingsSection.kt` | Form content untouched (restyle only) |

---

## 5. SCREEN-BY-SCREEN DESIGN

### 5.1 Splash Screen (`SplashScreen.kt`)

**Current state:** `screenBg` background, `logo_transparent.png` with scale+fade animation, 900ms delay.

**New design:**
```
Background: PRIMARY (#1A5C45) — full bleed, not screenBg
Logo: logo_transparent.png, 72dp, centered at 42% from top
Animation: alpha 0→1 only, 400ms total, EaseOut
  - Logo: delay 0ms
  - App name "ZeroBook": delay 150ms, white, SemiBold 22sp
  - Tagline "GST Accounts for India": delay 250ms, white 70%, 13sp
Exit: cross-fade to dashboard at 600ms

DELETE: scale animation, PremiumSpringSpec, 900ms delay
```

**Specific changes to `SplashScreen.kt`:**
1. Replace `AppColors.screenBg` background with `Color(0xFF1A5C45)`
2. Remove `animateFloatAsState` for scale — use only alpha
3. Add "ZeroBook" text below logo (white, SemiBold 22sp)
4. Add "GST Accounts for India" tagline below name (white 70%, 13sp)
5. Change delay from 900ms to 600ms
6. Remove `PremiumSpringSpec` and `PremiumFadeSpec` imports — use `tween` directly

### 5.2 Setup Screen (`SetupScreen.kt`)

**Current state:** Single scrollable form with `AppColors.screenBg` background.

**New design:**
```
Two-zone split layout:
  Top section (40% height): PRIMARY (#1A5C45) background
    - "Let's set up ZeroBook" in white, 20sp SemiBold
    - Step indicator icon

  Bottom section (60%): SURFACE white
    - Step bars: 3 thin rectangles (4dp height, 8dp radius)
      Active = PRIMARY fill, Inactive = OUTLINE color
    - Form fields stacked with 12dp gap
    - "Continue" button: full-width, PRIMARY, 52dp height, 10dp radius

Step transitions: horizontal slide from right, 300ms, EaseOut
PIN setup: 6 large circles (40dp each, 8dp gap)
  Empty: border only (2dp, OUTLINE)
  Filled: PRIMARY fill
  Numpad: 3×4 grid, each key 64dp, Monospace 24sp, 12dp radius
```

**Specific changes to `SetupScreen.kt`:**
1. Add `Box` with PRIMARY background for top 40%
2. Add step indicator bars (3 thin rectangles)
3. Restyle "Continue" button to 52dp height, 10dp radius
4. Add PIN dot circles for PIN step
5. Add numpad grid for PIN step

### 5.3 Dashboard Screen (`DashboardScreen.kt`)

**Current state:** Scrollable column with branding row, KPI cards, search, quick access grid, recent transactions.

**New design — Two-zone layout:**

```
ZONE 1 — Brand Header (220dp height)
  Background: PRIMARY (#1A5C45)
  Content:
    Row 1: "Hello, [Business Name]" — white, 18sp Medium
            Avatar circle (initials) right-aligned
    Row 2: "What would you like to do?" — white 70%, 13sp

  Balance Card (floats between Zone 1 and Zone 2):
    Background: #C8A96E (warm gold)
    Dot-grid texture overlay: white 8% opacity (6dp dots, 20dp spacing)
    Top-left: "Cash & Bank Balance" — white 80%, 12sp
    Center: "₹ 1,24,500" — Monospace 28sp Bold, white
    Bottom-left: "Updated just now" — white 60%, 11sp
    Right: logo_transparent.png (decorative, 64dp, bleeds right edge)
    Corner radius: 16dp
    NO shadow, NO count-up animation

ZONE 2 — Actions + Data (remaining height)
  Background: SURFACE (#F8F7F4)

  Quick Actions row (4 items):
    [+ Sale] [+ Purchase] [Vouchers] [Reports]
    Each: 72×72dp card, SURFACE_VARIANT bg, 12dp radius
    Icon: 24dp outline, PRIMARY color
    Label: 11sp, 4dp below icon, ON_SURFACE 70%

  Today's summary bar:
    "Today: Sales ₹12,400 | Outstanding ₹3,200"
    INCOME_GREEN 10% tint bg, 8dp radius

  Recent Vouchers list:
    Header: "Recent" (LABEL_CAPS) + "View all →" right link
    Each row (64dp):
      Left: 40dp colored circle with initial
      Center: Party name (14sp) + type + date (12sp, 60%)
      Right: Amount Monospace 14sp, INCOME_GREEN or EXPENSE_RED
    Divider: 0.5dp, inset 56dp from left

Bottom Navigation (existing 4 tabs):
  Active: icon PRIMARY fill + label PRIMARY
  Inactive: outline icon OUTLINE + label OUTLINE
  Background: SURFACE
  Top border: 0.5dp OUTLINE
  NO pill indicator
```

**Specific changes to `DashboardScreen.kt`:**
1. Add PRIMARY-colored header zone (220dp) with greeting + avatar
2. Add warm gold balance card with dot-grid texture SVG
3. Replace KPI card carousel/stack with simple 4-column quick action row
4. Add today's summary bar below quick actions
5. Restyle recent transactions list to 64dp rows with colored circles
6. Remove `AnimatedCounter`, `floatingCardEffect`, `transactionAppearAnimation`
7. Remove search bar from dashboard (move to vouchers screen only)
8. Remove progress tracker card (not in design system)
9. Remove KPI analytics popup (not in design system)
10. Keep `lowStockProducts` warning card, restyle to palette

### 5.4 Vouchers Screen (`VouchersScreen.kt`)

**Current state:** Search bar, filter/sort buttons, voucher type cards, LazyColumn with complex filter sheet.

**New design:**
```
Top bar:
  Left: "Vouchers" — 18sp SemiBold
  Right: Filter icon + Search icon

Type filter chips (horizontal scroll):
  All | Sale | Purchase | Receipt | Payment | Journal
  Active: PRIMARY fill, white text
  Inactive: SURFACE_VARIANT, ON_SURFACE 70%, OUTLINE border 1dp
  Height: 36dp, radius: 8dp

Date-grouped list:
  Date header: full-width SURFACE_VARIANT bg, 8dp vertical padding
    "Monday, 21 July 2025" in LABEL_CAPS, ON_SURFACE 50%

  Voucher row (64dp height):
    Left circle (40dp): color by type
      SALE → INCOME_GREEN, PURCHASE → EXPENSE_RED
      RECEIPT → PRIMARY, PAYMENT → ACCENT_GOLD
      JOURNAL → neutral gray
    Center:
      Line 1: Party name, 14sp, ON_SURFACE
      Line 2: Voucher# + type, 12sp, 60%
    Right:
      Amount: Monospace 14sp, INCOME_GREEN or EXPENSE_RED
      Status badge: 8dp radius, 10sp
    Divider: 0.5dp, inset 56dp

Empty state:
  Center of screen
  "No vouchers here" — 16sp Medium
  "Tap + to record your first entry" — 13sp, 60%
  "+ New Entry" button below

Voucher Detail (bottom sheet):
  Drag handle: 4×32dp pill, OUTLINE, centered
  Amount: Monospace 36sp, centered, color by type
  Type badge below amount

  Metadata section (SURFACE_VARIANT card, 12dp radius, 16dp padding):
    Each row: Label (11sp LABEL_CAPS, 50%) + Value (14sp)
    Hairline divider between rows

  Action row (bottom):
    [Edit] [Share PDF] — two equal buttons
    Edit: outlined, PRIMARY border + text
    Share: filled PRIMARY
    NO delete button

Voucher Entry (full screen):
  Top bar: "New Sale" / "Edit Voucher" + Close (×) right
  Party field: search-as-you-type, 300ms debounce
  Item rows: name | qty controls (−/+) | rate | line total
  Tax summary: SURFACE_VARIANT card
  Save button: full-width, 52dp, PRIMARY, pinned above keyboard
```

**Specific changes to `VouchersScreen.kt`:**
1. Replace search bar + filter/sort buttons with type filter chips (horizontal scroll)
2. Group vouchers by date with date headers
3. Restyle voucher rows to 64dp with colored circles
4. Move voucher detail to bottom sheet (not separate screen)
5. Remove complex filter sheet (replace with simple type chips + search)
6. Keep `VoucherFilterState` for internal logic, simplify UI
7. Keep `UniversalSelectionController` for batch operations

### 5.5 Parties Screen (`PartiesScreen.kt`)

**Current state:** Search bar, type filter, party list with add form.

**New design:**
```
Search bar at top (inside screen):
  Height: 44dp, SURFACE_VARIANT bg, 8dp radius
  Placeholder: "Search parties..." — 13sp, 50%
  Search icon left, Clear icon right when active

Filter row:
  [All] [Customers] [Suppliers] — 3 chip tabs
  Same style as voucher chips

Outstanding balance strip:
  Two boxes side by side:
    [Receivable ₹X] [Payable ₹X]
    INCOME_GREEN tint | EXPENSE_RED tint
    12sp label + Monospace 16sp amount

Party row (60dp height):
  Left: 40dp circle avatar
    Customer: INCOME_GREEN tint bg, first letter
    Supplier: EXPENSE_RED tint bg
  Center:
    Name: 14sp, ON_SURFACE
    Balance: Monospace 12sp, INCOME_GREEN or EXPENSE_RED
  Right: Chevron icon, 16dp, OUTLINE

Party Detail / Ledger:
  Header card (PRIMARY bg):
    Party initial circle (56dp, white bg, PRIMARY text)
    + party name (20sp white) + type tag

  Stats row (white, 3 equal cells):
    "Total Due" | "Last Transaction" | "Vouchers"
    Monospace 18sp number + 11sp label

  Ledger list (date-grouped):
    Each row: Dr amount (EXPENSE_RED) | Cr amount (INCOME_GREEN)
    Running balance below, 60%
    Bottom: Closing balance row (PRIMARY tint)
```

**Specific changes to `PartiesScreen.kt`:**
1. Add outstanding balance strip at top
2. Restyle party rows to 60dp with colored circle avatars
3. Add chevron right indicator
4. Keep `UniversalSelectionController` for batch operations
5. Move party detail to bottom sheet pattern

### 5.6 Reports Screen (`ReportsScreen.kt`)

**Current state:** Complex report screens with charts, trial balance, P&L, etc.

**New design:**
```
Reports list (flat, NOT card grid):

Section 1 — "Financial Reports"
  › Trial Balance
  › Profit & Loss
  › Balance Sheet
  › Day Book

Section 2 — "Business Reports"
  › Sales Register
  › Purchase Register
  › Outstanding (Receivables/Payables)
  › GST Summary (GSTR-1, GSTR-3B)
  › Stock Summary

Each row: 52dp height, chevron right, 14sp
Section headers: LABEL_CAPS style, 11sp, 50%, 16dp left padding

When a report is opened (chart view):
  Period toggle: [Daily] [Weekly] [Monthly] [Yearly]
    Pill tabs, 36dp height, PRIMARY active fill

  Chart area (180dp height):
    Bar chart, solid fills (no gradient)
    Income bars: INCOME_GREEN solid
    Expense bars: EXPENSE_RED solid
    X-axis: date/day labels, 10sp, 50%
    Y-axis: amount labels, 10sp, 50%, abbreviated (₹10K, ₹1L)
    Animation: bars grow from baseline, 400ms, 30ms stagger, EaseOut

  Summary row below chart:
    Two equal boxes: [Income total] [Expense total]
    80dp height, SURFACE_VARIANT bg, 12dp radius
    11sp label + Monospace 18sp amount
```

**Specific changes to `ReportsScreen.kt`:**
1. Replace card grid with flat two-section list
2. Keep all existing report implementations (Trial Balance, P&L, etc.)
3. Restyle chart bars to solid fills
4. Update chart animation to 400ms + 30ms stagger
5. Remove gradient fills from chart bars

### 5.7 Settings Screen (`SettingsScreen.kt`)

**Current state:** Complex settings with theme selector, email automation, business profile.

**New design:**
```
Flat list (NOT card grid):

Section: "Business"
  › Business Profile (name, GSTIN, address)
  › Financial Year (current FY, year-end close)
  › GST Configuration (GST type, Composition Y/N)

Section: "Data"
  › Backup & Restore
  › Export Data (CSV, PDF)
  › Import from Tally

Section: "App"
  › PIN / Biometric Security
  › Email Reminders (toggle)
  › Language (English / Hindi)

Section: "Support"
  › Help
  › About ZeroBook
  › Send Feedback

Danger zone (bottom, separated):
  Clear All Data (red text)

Each setting row (52dp):
  Leading icon (24dp, PRIMARY) optional
  Label 14sp + value/status 12sp 60% on same row
  Trailing: toggle | chevron | value chip

Section header (full-width):
  36dp height, SURFACE_VARIANT bg
  LABEL_CAPS style, 11sp, 50%, 16dp left padding
```

**Specific changes to `SettingsScreen.kt`:**
1. Remove theme selector section (Beach, Blue, Green, Purple, Teal)
2. Remove `EmailAutomationSection.kt` from rendered content (keep file)
3. Flatten layout to simple list with section headers
4. Remove gradient-decorated section cards
5. Keep all functional settings (backup, export, PIN, etc.)

### 5.8 Quick Sale Screen (`QuickSaleScreen.kt`)

**Current state:** Quick sale entry form.

**New design:**
```
Full white screen, dense POS layout:

Top bar: "Quick Sale" — 18sp SemiBold, Customer field right

Item entry area:
  Search field (full-width, 48dp, 8dp radius)
  Recent/favourite items as horizontal chips below

Item list (added items):
  Each row: item name | qty controls (−/+) | rate | line total
  Qty controls: 28dp buttons, immediate haptic feedback
  Line total: Monospace, right-aligned, auto-calculated

Totals card (sticky at bottom):
  SURFACE_VARIANT bg, 16dp radius top only
  Rows: Subtotal | Discount | Tax | Round Off | Total
  Total row: larger Monospace, PRIMARY text

Payment buttons (full width, 52dp, stacked):
  [Cash] — PRIMARY fill
  [UPI / Card] — outlined, PRIMARY border
```

**Specific changes to `QuickSaleScreen.kt`:**
1. Restyle item rows to dense layout
2. Add sticky totals card at bottom
3. Restyle payment buttons
4. Remove any animations from item list

### 5.9 Bank Cash Screen (`BankCashScreen.kt`)

**New design:**
```
Account card stacking:
  Show peek of second card behind first (8dp offset, 90% scale)
  Each card: SURFACE_VARIANT bg, 12dp radius

Balance history list:
  Date-grouped, same pattern as vouchers
  Each row: transaction type | amount Monospace | date
```

### 5.10 Invoice Screen (`InvoiceScreen.kt`)

**New design:**
```
Bottom sheet detail pattern (not full page):
  Drag handle: 4×32dp pill
  Amount: Monospace 36sp, centered
  Metadata section: SURFACE_VARIANT card
  Action row: [Edit] [Share PDF] [Download]
```

### 5.11 Ledger List Screen (`LedgerListScreen.kt`)

**New design:**
```
Date-grouped list:
  Each row shows Dr/Cr columns visually distinct:
    Dr amount: right-aligned, EXPENSE_RED, Monospace
    Cr amount: right-aligned, INCOME_GREEN, Monospace
    Running balance: smaller, below amount, 60%

  Bottom: Closing balance row, PRIMARY tint
    "Balance: Dr ₹X" or "Balance: Cr ₹X" — Monospace 16sp Bold
```

### 5.12 Products Screen (`ProductsScreen.kt`)

**New design:**
```
Category list with icon + stats:
  Each category: icon circle (colored) + name + item count + total value
  Chevron right

Product row:
  Left: category color circle
  Center: name + HSN + stock level
  Right: sale rate Monospace
```

### 5.13 Navigation (`MainActivity.kt`)

**Bottom nav restyle:**
```
4 tabs: Dashboard | Vouchers | Parties | Settings
Active: icon PRIMARY fill + label PRIMARY, 12sp
Inactive: outline icon OUTLINE + label OUTLINE, 12sp
Background: SURFACE (#FFFFFF)
Top border: 0.5dp OUTLINE
NO pill background, NO floating nav
Height: 72dp (KEEP existing)
```

**Status bar:**
```
Background: #F8F7F4 (warm off-white)
Dark icons: true
```

---

## 6. MOTION SYSTEM

### 6.1 Replace `PremiumMotion.kt` → `ZeroBookMotion.kt`

**Keep from `PremiumMotion.kt`:**
- `PremiumMotionPrefs` (reduced motion support)
- `Modifier.pressScale()` (button press feedback)
- `Modifier.premiumClickable()` (click with ripple)
- `Modifier.premiumCombinedClickable()` (long-press support)
- `PremiumBottomNavContent` (bottom nav icon animation)

**Replace in `PremiumMotion.kt`:**
- `premiumScreenTransition()` → simpler slide, 300ms, EaseOut
- `premiumEnterTransition()` → 300ms slide + 200ms fade
- `premiumExitTransition()` → 250ms slide + 200ms fade
- `premiumDialogEnter()` → 350ms slide-up + fade (keep for bottom sheets)
- `premiumDialogExit()` → 250ms slide-down + fade
- Remove `premiumFabEntrance()` → replace with simple `fadeIn(200ms)`

**New constants object:**

```kotlin
object ZeroBookMotion {
    const val SCREEN_FORWARD = 300
    const val SCREEN_BACK = 250
    const val SHEET_APPEAR = 350
    const val SHEET_DISMISS = 250
    const val FADE = 200
    const val FILTER_CHANGE = 150
    const val CHART_BAR = 400
    const val CHART_BAR_STAGGER = 30
    const val FAB_ITEM = 200
    const val FAB_STAGGER = 60

    val EASE_OUT = FastOutSlowInEasing
    val EASE_IN = LinearOutSlowInEasing
}
```

### 6.2 Delete `PremiumAnimations.kt`

Remove entirely. The following animations are deleted:
- `AnimatedCounter` — numbers display immediately, no count-up
- `floatingCardEffect` — no floating cards
- `transactionAppearAnimation` — no staggered list entrances
- `premiumButtonPress` — replaced by `pressScale` from `PremiumMotion.kt`
- `rememberChartDrawingProgress` — keep chart animation but simplify
- `shimmerLoadingAnimation` — keep `Skeleton.kt` shimmer instead
- `slideInFromBottom` — not used
- `scaleAndFadeIn` — not used
- `pulseAnimation` — not used
- `rotationAnimation` — keep for loading spinners only
- `bounceAnimation` — not used

### 6.3 Motion Rules Per Screen

| Screen | Animation |
|---|---|
| Splash | Alpha fade only, 400ms |
| Setup | Horizontal slide between steps, 300ms |
| Dashboard | Fade-in only, 200ms. No count-up. No stagger. |
| Vouchers | Fade-in, 200ms. Filter chip change: 150ms. |
| Parties | Fade-in, 200ms. |
| Reports | Fade-in, 200ms. Chart bars: 400ms + 30ms stagger. |
| Settings | No animation. Lists don't animate. |
| Quick Sale | Ripple on tap only. |
| Bottom sheets | Slide-up, 350ms, spring (400, 0.8). |
| Screen push | Slide, 300ms, EaseOut. |
| Screen pop | Slide, 250ms, EaseIn. |

---

## 7. IMPLEMENTATION ORDER

### Phase 1 — Foundation (do first, everything depends on it)
1. `ThemeConfig.kt` — Single ZEROCOOL theme
2. `Colors.kt` — New palette constants
3. `AppColors.kt` — Add semantic tokens
4. `Type.kt` — Full typography
5. `GlobalStyles.kt` — Spacing, Shape objects
6. `Theme.kt` — Simplify wrapper
7. `ThemeHelpers.kt` — Update helpers
8. `ThemeAwareComponents.kt` — Simplify
9. `InputColors.kt` — Restyle

### Phase 2 — Motion (do second, screens depend on it)
10. Rename `PremiumMotion.kt` → `ZeroBookMotion.kt`
11. Replace transition specs
12. Delete `PremiumAnimations.kt`

### Phase 3 — Core Screens (do third)
13. `SplashScreen.kt` — Green background, alpha fade
14. `MainActivity.kt` — Bottom nav restyle, remove theme switching
15. `DashboardScreen.kt` — Two-zone layout
16. `VouchersScreen.kt` — Date-grouped list, filter chips
17. `PartiesScreen.kt` — Balance strip, colored avatars

### Phase 4 — Secondary Screens
18. `SettingsScreen.kt` — Flat list, remove theme selector
19. `ReportsScreen.kt` — Flat report list
20. `QuickSaleScreen.kt` — Dense POS layout
21. `SetupScreen.kt` — Two-zone split
22. `BankCashScreen.kt` — Account card stacking
23. `InvoiceScreen.kt` — Bottom sheet pattern
24. `LedgerListScreen.kt` — Dr/Cr columns
25. `ProductsScreen.kt` — Category list

### Phase 5 — Cleanup
26. Delete `PremiumThemeConfig.kt`
27. Delete `PremiumAnimations.kt`
28. Remove `EmailAutomationSection.kt` from Settings render
29. Remove theme selector from Settings
30. Verify all screens use new palette

---

## 8. VERIFICATION CHECKLIST

After implementation, verify:

- [ ] Only one theme exists (ZEROCOOL)
- [ ] No theme selector in Settings
- [ ] Splash uses green background, alpha fade only
- [ ] Dashboard has two-zone layout (green header + white body)
- [ ] Balance card shows immediately (no count-up animation)
- [ ] Quick actions are 4-column row (not card grid)
- [ ] Voucher list is date-grouped with colored circles
- [ ] Party list has balance strip and colored avatars
- [ ] Reports are flat list (not card grid)
- [ ] Settings is flat list with section headers
- [ ] All amounts use Monospace font
- [ ] No gradient fills on any buttons
- [ ] No staggered list entrance animations
- [ ] No floating card effects
- [ ] No bounce animations
- [ ] No spring physics on page navigation
- [ ] Bottom nav has no pill indicator
- [ ] All existing drawable assets preserved
- [ ] All existing data layer files unchanged
- [ ] All existing services unchanged
- [ ] All existing ViewModels unchanged
- [ ] Navigation routes unchanged
- [ ] Bottom nav tabs unchanged (Dashboard, Vouchers, Parties, Settings)

---

*Document version: 1.0 — July 2026*
*Codebase: ZeroBook Android (Jetpack Compose, Material3, Room, MVVM)*
