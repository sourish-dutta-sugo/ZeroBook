# ZeroBook UI/UX Design Specification
## Adapted from FinWise Finance Management Mobile App UI Kit

**Status:** Design Review (Pre-Implementation)  
**Date:** July 2026  
**Reference Figma:** [Finance Management Mobile App UI UX Kit](https://www.figma.com/design/a3TG9T2MFvuS0RUJAA67tN/Finance-Management-Mobile-App-UI-UX-Kit-for-Budget-Tracker-Financial-Prototype-Design--Community-)  
**App Source of Truth:** `FRONTEND_UI_SPECIFICATION.md`, `FRONTEND_DESIGN.md`, `APP_STRUCTURE.md`, `brain/UI_SYSTEM.md`  
**Deliverables:** Penpot file (connected) + `design-prototype/index.html` gallery  
**Codebase:** NOT modified — review-only artifacts

---

## 1. Design Intent

This design adapts the **FinWise / Finance Management UI Kit** visual language (156+ screens, light/dark, card-based finance UI, onboarding carousel, analytics charts, security PIN flows) to **ZeroBook** — an Indian retail accounting app with GST, vouchers, parties, ledgers, and reports.

### What we borrow from the Figma kit
| Figma Kit Pattern | ZeroBook Adaptation |
|---|---|
| Green primary + warm neutrals | `#1A5C45` primary, `#F8F7F4` background |
| Splash with logo on brand color | ZeroBook logo + "GST Accounts for India" |
| 3-step onboarding carousel | Business setup wizard (3 steps) |
| PIN / biometric security screens | 4-digit PIN lock (existing flow) |
| Home dashboard with balance hero card | Cash & Bank balance gold card |
| Transaction list with colored avatars | Voucher list grouped by date |
| Category / filter chips | Voucher type & party type filters |
| Analytics charts (bar/line/pie) | KPI analytics popup (optional, simplified) |
| Bottom navigation (4–5 tabs) | Dashboard · Vouchers · Parties · Settings |
| Settings flat list with sections | Business / Data / App / Support sections |
| Add transaction / expense flows | New Voucher, Expense Entry, Quick Sale POS |
| Reports & analysis screens | Trial Balance, P&L, Balance Sheet, GST |

### What stays ZeroBook-specific (NOT copied from budget tracker)
- 15 voucher types (Sale, Purchase, Journal, Returns, etc.)
- GSTIN / PAN / HSN / IFSC Indian field validation
- Double-entry ledger & chart of accounts
- Party DR/CR balances
- Invoice PDF / WhatsApp share
- Financial year (April–March)
- Sample data import on first setup

---

## 2. Design Token System (ZEROCOOL)

Single theme — replaces 5-theme system per `FRONTEND_DESIGN.md`.

| Token | Hex | Usage |
|---|---|---|
| `primary` | `#1A5C45` | Headers, buttons, active nav, brand |
| `primaryLight` | `#E8F5E9` | Onboarding hero, tinted backgrounds |
| `background` | `#F8F7F4` | Screen background |
| `surface` | `#FFFFFF` | Cards, inputs, bottom nav |
| `surfaceVariant` | `#EFEFED` | Chips, secondary cards |
| `textPrimary` | `#1C1C1E` | Headings, amounts (non-colored) |
| `textSecondary` | `#4A4A4A` | Body, subtitles |
| `textTertiary` | `#888888` | Labels, placeholders |
| `outline` | `#E0E4EA` | Borders, dividers |
| `income` | `#2E7D52` | Credit, sales, receivables |
| `expense` | `#C0392B` | Debit, purchases, payables |
| `balanceGold` | `#C8A96E` | Dashboard balance hero card |
| `accentGold` | `#B8860B` | Highlights, payment type |

### Typography
- **Amounts:** Monospace Bold (28/22/18/14sp)
- **Screen titles:** SemiBold 18–22sp
- **Body:** Regular 14sp
- **Labels / caps:** Medium 11sp, letter-spacing 0.8sp

### Shape & Spacing
- Card radius: 12dp · Button: 10dp · Input: 8dp · Chip: 8dp
- Screen padding: 16dp phone / 24dp tablet
- List row height: 64dp (vouchers) / 60dp (parties)
- Bottom nav: 72dp

---

## 3. Complete Screen Inventory

### Phase A — Entry Flow (Screens 01–08)
| # | Screen | Figma Kit Equivalent | ZeroBook Route |
|---|---|---|---|
| 01 | System Splash | App launch splash | AndroidX SplashScreen API |
| 02 | In-App Splash | Brand splash | `SplashScreen.kt` |
| 03 | Onboarding Welcome | Welcome carousel slide 1 | New (optional pre-setup) |
| 04 | Onboarding Features | Carousel slides 2–3 | New (optional) |
| 05 | Business Setup Step 1 | Sign up / profile form | `SetupScreen.kt` |
| 06 | Business Setup Step 2 | Bank details form | `SetupScreen.kt` |
| 07 | Sample Data Dialog | Import demo data prompt | Setup dialog |
| 08 | PIN Lock | PIN security screen | `PinLockScreen` |

### Phase B — Main Navigation (Screens 09–12)
| # | Screen | Figma Kit Equivalent | ZeroBook Route |
|---|---|---|---|
| 09 | Dashboard | Home / overview | `dashboard` |
| 10 | Vouchers List | Transactions list | `vouchers` |
| 11 | Parties List | Contacts / payees | `parties` |
| 12 | Settings Menu | Profile & settings | `settings` |

### Phase C — Voucher Flows (Screens 13–28)
| # | Screen | ZeroBook Function |
|---|---|---|
| 13 | Voucher Type Selection | 15 type cards |
| 14 | New Sale — Party & Items | Step 1 of 3 |
| 15 | New Sale — Payment & Charges | Step 2 |
| 16 | New Sale — Review | Step 3 + invoice preview |
| 17 | New Purchase | Same 3-step flow |
| 18 | Receipt Voucher | Single-step |
| 19 | Payment Voucher | Single-step |
| 20 | Journal Entry | Single-step |
| 21 | Voucher Item Sheet | Bottom sheet |
| 22 | Voucher Filter Sheet | Filter bottom sheet |
| 23 | Voucher Sort Sheet | Sort bottom sheet |
| 24 | Voucher Detail Sheet | Bottom sheet (not full page) |
| 25 | UPI Payment Dialog | Sale + UPI mode |
| 26 | Print Receipt Dialog | Post-save preview |
| 27 | Parsed Bill Items (OCR) | OCR dialog |
| 28 | Invoice Viewer | `invoice/{id}` |

### Phase D — Party Flows (Screens 29–33)
| # | Screen | ZeroBook Function |
|---|---|---|
| 29 | Add Party Form | `CreatePartyInlineSheet` |
| 30 | Edit Party Form | Same sheet, edit mode |
| 31 | Party Detail / Ledger | `party_detail/{id}` |
| 32 | Party Picker Sheet | Voucher party selection |
| 33 | Payment Reminder Email | Email intent trigger |

### Phase E — Settings Sub-screens (Screens 34–42)
| # | Screen | ZeroBook Function |
|---|---|---|
| 34 | Business Profile | `BusinessProfileSettingsSection` |
| 35 | Financial Year | FY settings |
| 36 | PIN Protection | PIN setup in settings |
| 37 | Customize (KPI/Progress) | Progress tracker settings |
| 38 | Backup & Restore | CSV + DB backup |
| 39 | About ZeroBook | About screen |
| 40 | Changelog Dialog | Version dialog |
| 41 | Email Automation | `EmailAutomationSection` |
| 42 | Products Master link | Navigates to products |

### Phase F — Reports (Screens 43–52)
| # | Screen | ZeroBook Function |
|---|---|---|
| 43 | Reports Menu | Flat 2-section list |
| 44 | Trial Balance | Expandable groups |
| 45 | Profit & Loss | Trading + P&L dual column |
| 46 | Balance Sheet | Assets vs Liabilities |
| 47 | GST Summary | Output vs Input tax |
| 48 | Outstanding Receivables | Debtors + aged bills |
| 49 | Outstanding Payables | Supplier bills |
| 50 | Stock Report | Stock levels + filters |
| 51 | Ledger Books | Chart of accounts |
| 52 | Expenses Register | Link from reports |

### Phase G — Inventory & POS (Screens 53–60)
| # | Screen | ZeroBook Function |
|---|---|---|
| 53 | Products List | `products` |
| 54 | Product Editor (Add) | Product form |
| 55 | Product Editor (Edit) | Edit mode |
| 56 | HSN Lookup Dialog | HSN search |
| 57 | Barcode Scanner | Camera dialog |
| 58 | Quick Sale POS | `quick_sale` |
| 59 | Bank & Cash Register | `bank_cash` |
| 60 | Manual Transaction Form | Add receipt/payment |

### Phase H — Income & Expenses (Screens 61–66)
| # | Screen | ZeroBook Function |
|---|---|---|
| 61 | Expenses List | `expenses` |
| 62 | Expense Entry Form | Bottom form |
| 63 | Income List | `income` |
| 64 | Income Entry Form | Bottom form |
| 65 | Expense Category Picker | Dropdown |
| 66 | Income Category Picker | Dropdown |

### Phase I — Utility & States (Screens 67–72)
| # | Screen | ZeroBook Function |
|---|---|---|
| 67 | Database Loading | Init spinner |
| 68 | Database Error | Retry card |
| 69 | Empty Vouchers | Empty state |
| 70 | Empty Parties | Empty state |
| 71 | Multi-Select Mode | Universal selection bar |
| 72 | Delete Confirmation | Alert dialog |

**Total designed screens: 72 core + 48 component/state variants = 120 review frames**

*(Matches Figma kit scale: 156 screens × 2 themes; we design 120 ZeroBook-specific frames covering all app functionality)*

---

## 4. Key Screen Layouts

### 4.1 Dashboard (Figma Home → ZeroBook Dashboard)
```
┌─────────────────────────────┐
│ PRIMARY HEADER (220dp)      │
│ Hello, [Business]    [Av]   │
│ What would you like to do?  │
│ ┌─────────────────────────┐ │
│ │ GOLD BALANCE CARD       │ │
│ │ Cash & Bank  ₹1,24,500  │ │
│ └─────────────────────────┘ │
├─────────────────────────────┤
│ [Sale][Purchase][Vouch][Rpt]│
│ Today: Sales ₹12,400 | ...  │
│ RECENT          View all →  │
│ ○ Sharma Store    ₹4,200    │
│ ○ Metro Supplies  ₹8,900    │
├─────────────────────────────┤
│ ■ Dashboard  ○ Vouchers ... │
└─────────────────────────────┘
```

### 4.2 Vouchers List (Figma Transactions → ZeroBook Vouchers)
- Top: Title + search pill
- Filter chips: All | Sale | Purchase | Receipt | Payment | Journal
- Date group headers (caps, surfaceVariant bg)
- Rows: 40dp colored circle + party + voucher# + amount (monospace, green/red)
- FAB: + (primary, bottom-right)

### 4.3 Setup Wizard (Figma Sign Up → ZeroBook Setup)
- Top 40%: Primary green with step title
- Step bars: 3 thin rectangles
- White form area with RetailTextField-style inputs
- Full-width Continue button (52dp)

---

## 5. Component Library

| Component | Spec |
|---|---|
| Primary Button | 48–52dp height, `#1A5C45` fill, white text, 10dp radius |
| Outlined Button | White fill, primary border 1.5dp |
| Filter Chip (active) | Primary fill, white 12sp text, 36dp height |
| Filter Chip (inactive) | surfaceVariant, outline border |
| Voucher Row | 64dp, circle avatar, divider inset 56dp |
| Balance Card | `#C8A96E`, 16dp radius, monospace amount 28sp |
| Bottom Nav | 72dp, no pill indicator, primary active icon |
| Bottom Sheet | 20dp top radius, drag handle 4×32dp pill |
| KPI Card | Removed — replaced by balance hero + summary bar |
| Search Pill | 44dp height, surfaceVariant, 8dp radius |

---

## 6. Motion (from FRONTEND_DESIGN.md)

| Interaction | Duration | Easing |
|---|---|---|
| Splash fade | 400ms | EaseOut |
| Screen push | 300ms | EaseOut |
| Screen pop | 250ms | EaseIn |
| Bottom sheet | 350ms | Spring |
| Filter chip change | 150ms | EaseOut |
| Chart bars | 400ms + 30ms stagger | EaseOut |

**Removed:** count-up animations, wallet stack KPI carousel, floating cards, bounce effects.

---

## 7. Review Artifacts

| Artifact | Location | Purpose |
|---|---|---|
| Penpot design file | Connected Penpot project | Editable frames for all flows |
| HTML gallery | `design-prototype/index.html` | Browser review without Penpot |
| This spec | `design-prototype/ZEROBOOK_UI_DESIGN_SPEC.md` | Screen mapping & tokens |
| Source design doc | `FRONTEND_DESIGN.md` | Implementation guide (post-approval) |

### Penpot Pages Created
1. `00 — Design System` — Tokens, colors, typography, components
2. `01 — Entry Flow` — Splash, Setup, PIN, Onboarding
3. `02 — Main App` — Dashboard, Vouchers, Parties, Settings
4. `03 — Secondary Screens` — Reports, Voucher Entry, Quick Sale, Products, Invoice

---

## 8. Implementation Notes (After Approval)

When ready to implement:
1. Follow phase order in `FRONTEND_DESIGN.md` Section 7
2. Start with token foundation (`ThemeConfig.kt`, `AppColors.kt`, `Type.kt`)
3. Do NOT reintroduce multi-theme selector
4. Preserve all navigation routes and ViewModels
5. Use existing composables (`RetailTextField`, selection system, etc.) — restyle only

---

## 9. Figma Kit → ZeroBook Screen Mapping (Top 30)

| Figma Kit Screen | ZeroBook Screen |
|---|---|
| Splash / Launch | 01–02 Entry splash |
| Welcome Onboarding | 03–04 Optional onboarding |
| Sign Up / Register | 05–06 Business Setup |
| Login | N/A (offline app, PIN only) |
| PIN Entry | 08 PIN Lock |
| Home Dashboard | 09 Dashboard |
| Total Balance Card | Gold balance hero |
| Recent Transactions | Recent vouchers list |
| All Transactions | 10 Vouchers List |
| Add Transaction | 13 Type Selection → 14–16 New Voucher |
| Transaction Detail | 24 Voucher Detail Sheet |
| Categories | Voucher type chips |
| Budget Overview | Today's summary bar |
| Analytics / Charts | KPI popup (simplified) or Reports |
| Expense Add | 62 Expense Entry |
| Income Add | 64 Income Entry |
| Reports | 43 Reports Menu |
| Monthly Report | 45 P&L Report |
| Profile | 12 Settings |
| Edit Profile | 34 Business Profile |
| Security Settings | 36 PIN Protection |
| Notifications | N/A (future) |
| Search | Voucher/Party search pills |
| Filter | 22 Filter Sheet |
| Empty State | 69–70 Empty states |
| Success Dialog | 26 Print Receipt Dialog |
| Payment Method | Payment mode chips in voucher |
| Bank Accounts | 59 Bank & Cash |
| Quick Add | 58 Quick Sale POS |
| Invoice / Receipt | 28 Invoice Viewer |

---

*End of Design Specification — Ready for review before codebase implementation.*
