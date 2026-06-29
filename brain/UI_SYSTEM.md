# UI_SYSTEM - ZeroBook

## Screens & Navigation
Managed in `MainActivity.kt` using `Jetpack Compose Navigation`.

### Core Routes:
- `dashboard`: Home, aggregates, search.
- `vouchers`: List and filter all types.
- `parties`: Customer/Supplier list and detail ledgers.
- `settings`: Profile, themes, SMTP config, FY management.
- `reports`: Business and stock reports.
- `ledger_books`: Chart of accounts.
- `products`: Inventory catalog.
- `new_voucher?voucherId={id}`: Edit/Create voucher flow.
- `invoice/{id}`: PDF preview and sharing.

## Navigation Patterns
- **Bottom Navigation:** Dashboard, Vouchers, Parties, Settings.
- **State Preservation:** Uses `navigateToTopLevel` helper to maintain scroll/filter state across tab switches.
- **Contextual Navigation:** Reports, Products, Expenses accessed via Dashboard or Settings shortcuts.

## UI Hierarchy
- `Scaffold` provides the top-level structure (TopBar, BottomBar).
- `NavHost` content changes based on current route.
- `AppViewModel` provides the hoisted state for all screens.

## Design System
- **Framework:** Material 3.
- **Themes:** Multi-theme system (Beach, Blue, Green...) defined in `ui/theme`.
- **Animations:** `PremiumMotion.kt` provides spring-physics shared transitions (One UI/OxygenOS style).
- **GST Input Helpers:** Standardized input fields for GSTIN, PAN, and Indian currency.

## User Interaction Flows
1.  **Voucher Entry:** Tap "+" -> Select Type -> Add Party -> Add Products -> Verify Tax -> Save -> Share PDF.
2.  **Party Ledger View:** Select Party -> View transaction list -> Filter by Date/Type -> Export CSV/PDF.
3.  **Setup Flow:** Branded Splash -> Business Details Entry -> PIN Configuration -> Dashboard.
