# ZeroBook — Product Design Specification

**Version:** 3.0  
**Date:** July 2026  
**Platform:** Android (Native)  
**Status:** Pre-Alpha → Alpha  

---

## Table of Contents

1. [Product Vision](#1-product-vision)
2. [Target Users & Personas](#2-target-users--personas)
3. [Core Value Proposition](#3-core-value-proposition)
4. [Feature Matrix](#4-feature-matrix)
5. [Information Architecture](#5-information-architecture)
6. [User Journeys](#6-user-journeys)
7. [Business Logic & Rules](#7-business-logic--rules)
8. [Data Model Design](#8-data-model-design)
9. [Accounting Engine](#9-accounting-engine)
10. [GST Compliance](#10-gst-compliance)
11. [Inventory System](#11-inventory-system)
12. [Reporting System](#12-reporting-system)
13. [Security & Access Control](#13-security--access-control)
14. [Offline-First Architecture](#14-offline-first-architecture)
15. [Non-Functional Requirements](#15-non-functional-requirements)
16. [Success Metrics](#16-success-metrics)
17. [Constraints & Limitations](#17-constraints--limitations)
18. [Future Roadmap](#18-future-roadmap)

---

## 1. Product Vision

### 1.1 Mission Statement

ZeroBook is a mobile-first retail accounting and invoicing application designed specifically for Indian small retailers who have outgrown pen-and-paper ledgers but find full desktop accounting software too complex.

### 1.2 Problem Statement

**Who:** Small retailers, traders, and shop owners across India (kirana stores, electronics shops, apparel stores, general merchants).

**Why:** 
- GST compliance is mandatory since July 2017, but 70%+ of small retailers still use manual methods
- Existing solutions are either too complex (Tally Prime requires training) or too basic (billing apps with no ledger depth)
- Desktop software doesn't fit the mobile-first workflow of shop owners who manage their business from their phone
- Current tools lack the integration of billing, accounting, inventory, and GST in a single lightweight app

**How:** A single, lightweight mobile app that combines invoicing, double-entry-style ledgers, inventory, and reports in one place — designed mobile-first, usable one-handed in a shop.

### 1.3 Product Principles

1. **Simplicity First:** Every feature must be explainable in under 30 seconds to a first-time user
2. **Offline-First:** The app must work without internet connectivity for all core functions
3. **Indian Context:** Built for Indian business conventions (April-March FY, lakh/crore formatting, GST rules)
4. **Data Ownership:** All data stays on the user's device; no cloud dependency
5. **Progressive Disclosure:** Show only what's needed; hide complexity until requested
6. **Error Prevention:** Prevent mistakes before they happen (validation, defaults, confirmations)

### 1.4 Competitive Positioning

| Dimension | ZeroBook | Tally Prime | Khatabook | Vyapar |
|-----------|----------|-------------|-----------|--------|
| Platform | Mobile-first | Desktop-first | Mobile-first | Mobile-first |
| Accounting Depth | Double-entry lite | Full double-entry | Simple ledger | Simple ledger |
| GST Compliance | Invoice-level | Full (GSTR-1, 3B) | Basic | Basic |
| Inventory | Basic stock count | Full (batch, serial, valuation) | None | Basic |
| Price Point | Free | Paid (₹18,000+/yr) | Freemium | Freemium |
| Learning Curve | Low | High | Very Low | Low |
| Offline Support | Full | Full | Partial | Full |

---

## 2. Target Users & Personas

### 2.1 Primary Persona: Ramesh (Kirana Store Owner)

| Attribute | Detail |
|-----------|--------|
| Age | 45 years |
| Location | Tier-2 city (Nashik, Maharashtra) |
| Business | Kirana (grocery) store, 800 sq ft |
| Revenue | ₹15-20 lakh/month |
| Employees | 2 (himself + 1 helper) |
| Tech Savvy | Low-medium (uses WhatsApp, YouTube) |
| Current System | Paper ledger + calculator |
| Pain Points | GST filing confusion, can't track who owes money, inventory waste from overstocking |
| Goals | Easy billing, know who owes money, file GST correctly, track profit |
| Device | Redmi Note 12, Android 13 |

**Key Quote:** *"I know GST is important but I don't understand CGST vs SGST. I just want to make bills and know if I'm making money."*

### 2.2 Secondary Persona: Priya (Electronics Shop Owner)

| Attribute | Detail |
|-----------|--------|
| Age | 32 years |
| Location | Bangalore, Karnataka |
| Business | Electronics retail + service, 400 sq ft |
| Revenue | ₹8-12 lakh/month |
| Employees | 3 (self + 2 staff) |
| Tech Savvy | Medium (uses Instagram for business) |
| Current System | Basic billing app (no accounting) |
| Pain Points | Interstate sales GST confusion, warranty tracking, supplier payment management |
| Goals | Professional invoicing, track interstate sales, manage supplier payments |
| Device | Samsung Galaxy A54, Android 14 |

**Key Quote:** *"I need to show GST bifurcation on invoices. My customers expect professional bills."*

### 2.3 Tertiary Persona: Vikram (Apparel Wholesaler)

| Attribute | Detail |
|-----------|--------|
| Age | 38 years |
| Location | Surat, Gujarat |
| Business | Wholesale garments, 1200 sq ft warehouse |
| Revenue | ₹40-50 lakh/month |
| Employees | 5 (self + 4 staff) |
| Tech Savvy | Medium-high |
| Current System | Tally (limited features used) |
| Pain Points | Complex voucher entries, can't access on mobile, too many features he doesn't use |
| Goals | Simple voucher entry, mobile access, receivables tracking, basic inventory |
| Device | OnePlus 11, Android 14 |

**Key Quote:** *"Tally has everything but I only use 20% of it. I want something simple that works on my phone."*

### 2.4 User Segmentation

| Segment | Size | Needs | ZeroBook Fit |
|---------|------|-------|--------------|
| Solo shopkeepers | 60% | Simple billing, profit tracking, GST bills | Excellent |
| Small shops (2-3 staff) | 30% | Billing + receivables + basic inventory | Good |
| Wholesalers (4+ staff) | 10% | Multi-user, advanced inventory, complex vouchers | Partial |

---

## 3. Core Value Proposition

### 3.1 The ZeroBook Promise

**"Professional accounting for your shop, in your pocket."**

### 3.2 Value Pillars

| Pillar | What It Means | How We Deliver |
|--------|---------------|----------------|
| **GST-Ready Invoicing** | Create compliant invoices in 30 seconds | Auto CGST/SGST/IGST calculation, state detection, GSTIN validation |
| **Know Your Numbers** | Real-time profit, receivables, payables | Dashboard KPIs, outstanding reports, expense tracking |
| **Never Lose Data** | Paper can burn; digital doesn't | Local SQLite database, export to CSV, backup/restore |
| **Works Offline** | No internet needed for core features | All data stored locally, no cloud dependency |
| **Indian by Design** | Built for Indian business conventions | April-March FY, lakh/crore formatting, Indian state codes |

### 3.3 Key Differentiators

1. **Integrated Accounting + Billing:** Unlike billing-only apps, ZeroBook maintains proper double-entry ledgers
2. **Mobile-First Design:** Unlike Tally, designed for touch interaction on phones
3. **No Subscription:** One-time download, no recurring fees
4. **Privacy-First:** Data never leaves the device unless user exports it
5. **GST-Aware:** Automatic interstate/intrastate detection and GST calculation

---

## 4. Feature Matrix

### 4.1 Core Features (MVP)

| Feature | Status | Priority | Description |
|---------|--------|----------|-------------|
| Business Profile Setup | Implemented | P0 | GSTIN, PAN, address, bank details |
| Voucher Management | Partially Implemented | P0 | Sale, Purchase, Receipt, Payment, Journal |
| Party Management | Implemented | P0 | Customer/Supplier CRUD with balances |
| Product Management | Implemented | P0 | Catalog with HSN, rates, stock |
| GST Calculation | Implemented | P0 | Auto CGST/SGST/IGST based on state |
| Invoice Generation | Implemented | P0 | PDF invoice with GST bifurcation |
| Dashboard KPIs | Implemented | P0 | Today's sales, receivables, payables |
| Ledger System | Partially Implemented | P0 | Double-entry style posting |
| PIN Security | Implemented | P1 | 4-digit PIN lock |
| Financial Year | Implemented | P1 | FY switching and year-end close |

### 4.2 Enhanced Features (Post-MVP)

| Feature | Status | Priority | Description |
|---------|--------|----------|-------------|
| Quick Sale (POS) | Implemented | P1 | Fast counter billing |
| Expense Tracking | Implemented | P1 | Categorized expense entry |
| Income Tracking | Implemented | P1 | Non-sales income recording |
| Barcode Scanning | Implemented | P2 | Camera-based product lookup |
| OCR Bill Scanning | Implemented | P2 | Extract items from supplier bills |
| Stock Reports | Implemented | P1 | Current stock, low stock alerts |
| Email Automation | Implemented | P2 | Scheduled payment reminders |
| Multiple Themes | Implemented | P2 | 5 color themes |
| CSV Export/Import | Implemented | P1 | Data export for external use |
| Database Backup | Implemented | P1 | Full SQLite backup/restore |

### 4.3 Advanced Features (Future)

| Feature | Status | Priority | Description |
|---------|--------|----------|-------------|
| Trial Balance | Planned | P0 | Ledger verification report |
| Balance Sheet | Planned | P0 | Financial position statement |
| Profit & Loss | Planned | P0 | Income-expense statement |
| GSTR-1 Data | Planned | P1 | GST return data export |
| Batch/Expiry Tracking | Planned | P2 | Product batch management |
| Multi-User | Planned | P3 | Role-based access |
| Thermal Printer | Planned | P2 | Receipt printing support |
| E-Way Bill | Planned | P3 | Integrated e-way bill generation |

---

## 5. Information Architecture

### 5.1 App Structure

```
ZeroBook App
├── Entry Layer
│   ├── System Splash
│   ├── Database Init
│   ├── App Splash
│   ├── Setup (First Launch)
│   └── PIN Lock (If Enabled)
│
├── Main Application
│   ├── Dashboard
│   │   ├── KPI Cards (Sales, Purchases, Profit, etc.)
│   │   ├── Universal Search
│   │   ├── Quick Actions
│   │   ├── Progress Tracker
│   │   └── Recent Transactions
│   │
│   ├── Vouchers
│   │   ├── Voucher List (Filterable/Sortable)
│   │   ├── Voucher Type Selection
│   │   ├── Voucher Entry Form (3-Step)
│   │   │   ├── Step 1: Party & Items
│   │   │   ├── Step 2: Payment & Charges
│   │   │   └── Step 3: Review & Save
│   │   ├── Voucher Detail View
│   │   └── Invoice Viewer
│   │
│   ├── Parties
│   │   ├── Party List (Filterable)
│   │   ├── Party Add/Edit Form
│   │   └── Party Detail + Ledger
│   │
│   ├── Settings
│   │   ├── Business Profile
│   │   ├── Products Master
│   │   ├── Ledger Books
│   │   ├── Customize
│   │   ├── Theme & Colors
│   │   ├── Financial Year
│   │   ├── PIN Protection
│   │   ├── Email Automation
│   │   ├── Backup & Restore
│   │   └── About
│   │
│   ├── Reports
│   │   ├── Trial Balance
│   │   ├── Profit & Loss
│   │   ├── Balance Sheet
│   │   ├── GST Summary
│   │   ├── Outstanding Receivables
│   │   ├── Outstanding Payables
│   │   └── Stock Report
│   │
│   ├── Products
│   │   ├── Product List
│   │   ├── Product Editor
│   │   └── Stock Report
│   │
│   ├── Bank & Cash
│   │   ├── Balance Summary
│   │   ├── Transaction Log
│   │   └── Manual Transaction Form
│   │
│   ├── Expenses
│   │   ├── Expense List
│   │   └── Expense Entry Form
│   │
│   ├── Income
│   │   ├── Income List
│   │   └── Income Entry Form
│   │
│   └── Quick Sale (POS)
│       ├── Product Grid
│       ├── Cart
│       └── Payment
│
└── Services
    ├── Invoice Generator
    ├── CSV Export/Import
    ├── Email Composer
    ├── Barcode Scanner
    └── Database Backup
```

### 5.2 Navigation Hierarchy

| Level | Screens | Access |
|-------|---------|--------|
| **L0: Entry** | Splash, Setup, PIN | App launch |
| **L1: Top-Level** | Dashboard, Vouchers, Parties, Settings | Bottom navigation |
| **L2: Feature** | Reports, Products, Bank & Cash, Expenses, Income, Quick Sale | Quick actions, settings |
| **L3: Detail** | Voucher Entry, Party Detail, Product Editor, Invoice Viewer | List item taps |
| **L4: Modal** | Filter Sheet, Sort Sheet, Item Entry, Barcode Scanner | Contextual actions |

### 5.3 Screen Count Summary

| Category | Count | Examples |
|----------|-------|----------|
| Entry Screens | 4 | Splash, Setup, PIN, Changelog |
| Top-Level Screens | 4 | Dashboard, Vouchers, Parties, Settings |
| Feature Screens | 7 | Reports, Products, Bank & Cash, Expenses, Income, Quick Sale, Ledger Books |
| Detail Screens | 5 | Voucher Entry, Party Detail, Product Editor, Invoice Viewer, Party Ledger |
| Sub-Screens | 7 | Business Profile, Theme, Customize, FY, PIN, About, Email Automation |
| Modal Sheets | 6 | Filter, Sort, Item Entry, Party Picker, Barcode Scanner, Parsed Bill |
| Dialogs | 8 | Confirm Save, Delete, UPI Payment, Print Receipt, etc. |
| **Total** | **41** | |

---

## 6. User Journeys

### 6.1 First-Time User Journey

```
Download App
    ↓
System Splash (auto)
    ↓
Database Init (auto)
    ↓
App Splash (auto, 900ms)
    ↓
Setup Screen (required)
    ├── Enter Business Name *
    ├── Enter Address *
    ├── Enter PIN Code * (auto-fills City/State)
    ├── Enter GSTIN (optional, auto-fills PAN)
    ├── Enter Bank Details (optional)
    └── Tap "Initialize Business"
    ↓
Sample Data Dialog
    ├── "Yes, Import" → Loads demo data
    └── "No, Start Clean" → Empty state
    ↓
Dashboard (first view)
    ├── See KPI cards (all zero)
    ├── See Quick Actions
    └── Prompted to create first voucher
```

### 6.2 Daily Billing Journey (Kirana Store)

```
Open App
    ↓
PIN Lock (if enabled)
    ↓
Dashboard
    ├── Glance at today's sales KPI
    ├── Check receivables
    └── Tap "Quick Sale" or "Vouchers" → "+"
    ↓
Voucher Type Selection
    └── Tap "Sales"
    ↓
Voucher Entry Form
    ├── Select Party (or walk-in customer)
    ├── Add Items
    │   ├── Search product by name
    │   ├── Or scan barcode
    │   ├── Set quantity
    │   └── Auto-calculates GST
    ├── Review total
    ├── Select payment mode (Cash/UPI)
    └── Tap "Save & Post"
    ↓
Print Receipt Dialog
    ├── "Save PDF As" → Share with customer
    └── "Close & Exit" → Back to Dashboard
```

### 6.3 Receivables Collection Journey

```
Dashboard
    ├── See "Receivables" KPI card
    ├── Tap to view analytics popup
    └── Or tap "Reports" → "Outstanding Receivables"
    ↓
Outstanding Receivables Report
    ├── See list of customers with dues
    ├── Sort by amount (highest first)
    ├── Tap "Send Email" → Pre-filled reminder
    └── Or tap "Receive Payment" → New Receipt voucher
    ↓
Payment Dialog
    ├── Enter amount received
    ├── Select payment mode
    └── Tap "Capture Payment"
    ↓
Receipt Saved
    ├── Ledger updated
    ├── Party balance reduced
    └── Dashboard KPIs refresh
```

### 6.4 Monthly GST Filing Journey

```
Reports Screen
    └── Tap "GST Summary Status"
    ↓
GST Summary Report
    ├── See Output Tax (CGST, SGST, IGST collected)
    ├── See Input Tax Credit (CGST, SGST, IGST paid)
    ├── See Net Payable position
    └── Export data for filing
    ↓
External GST Portal
    ├── File GSTR-1 (using exported data)
    └── File GSTR-3B (using net payable figure)
```

### 6.5 Inventory Reorder Journey

```
Dashboard
    ├── See "Low Stock Warning" banner
    ├── Tap to view details
    └── Or go to Products → Stock Report
    ↓
Stock Report
    ├── Filter by "Low Stock"
    ├── See products below threshold
    ├── Note which suppliers to order from
    └── Create Purchase voucher for reorder
    ↓
Voucher Entry (Purchase)
    ├── Select supplier
    ├── Add items to reorder
    ├── Set quantities
    └── Save voucher
    ↓
Stock Updated
    ├── Current stock increases
    ├── Low stock warning clears
    └── Dashboard reflects new stock value
```

### 6.6 Year-End Closing Journey

```
Settings
    └── Tap "Financial Year Control"
    ↓
Financial Year Settings
    ├── Enter new FY start year (e.g., 2026)
    ├── Review current FY data
    └── Tap "Save Financial Year"
    ↓
Year-End Processing (automatic)
    ├── Calculate closing balances
    ├── Carry forward to new FY
    ├── Create opening balance entries
    └── Log audit trail
    ↓
New Financial Year Active
    ├── All vouchers now dated in new FY
    ├── Opening balances visible
    └── Previous FY data accessible via reports
```

---

## 7. Business Logic & Rules

### 7.1 Voucher Numbering

| Voucher Type | Format | Example | Reset |
|--------------|--------|---------|-------|
| Sale | SALE/{FY}/{seq} | SALE/2025-26/0001 | Annual |
| Purchase | PUR/{FY}/{seq} | PUR/2025-26/0001 | Annual |
| Receipt | REC/{FY}/{seq} | REC/2025-26/0001 | Annual |
| Payment | PAY/{FY}/{seq} | PAY/2025-26/0001 | Annual |
| Journal | JRN/{FY}/{seq} | JRN/2025-26/0001 | Annual |
| Expense | EXP/{FY}/{seq} | EXP/2025-26/0001 | Annual |
| Income | INC/{FY}/{seq} | INC/2025-26/0001 | Annual |

### 7.2 GST Rules

#### Intra-State (Same State)
- **CGST:** 50% of GST rate (e.g., 9% for 18% rate)
- **SGST:** 50% of GST rate (e.g., 9% for 18% rate)
- **Example:** ₹1,000 + 18% GST = ₹1,000 + ₹90 CGST + ₹90 SGST = ₹1,180

#### Inter-State (Different States)
- **IGST:** Full GST rate (e.g., 18%)
- **Example:** ₹1,000 + 18% GST = ₹1,000 + ₹180 IGST = ₹1,180

#### GST Rate Slabs
| Rate | Common Items |
|------|--------------|
| 0% | Essential items (unbranded food grains, fresh vegetables) |
| 5% | Packaged food, footwear under ₹1,000 |
| 12% | Processed food, footwear ₹1,000+ |
| 18% | Most goods and services (electronics, services) |
| 28% | Luxury items (cars, tobacco, aerated drinks) |

### 7.3 Rounding Rules

- All invoice amounts are rounded to nearest ₹1
- Rounding adjustment goes to "Round Off" account
- If amount ends in .50 or above, round up
- If amount ends in below .50, round down
- Round Off is shown separately on invoice

### 7.4 Payment Status Logic

| Status | Condition | Color |
|--------|-----------|-------|
| Paid | `outstandingAmount == 0` | Green |
| Partially Paid | `0 < outstandingAmount < totalAmount` | Orange |
| Unpaid | `outstandingAmount == totalAmount` | Red |
| Cancelled | `status == "CANCELLED"` | Gray |

### 7.5 Stock Movement Rules

| Voucher Type | Stock Effect |
|--------------|--------------|
| Sale | Decrease (qty × units) |
| Purchase | Increase (qty × units) |
| Sale Return | Increase (returned qty) |
| Purchase Return | Decrease (returned qty) |
| Delivery Challan | Decrease (dispatched qty) |
| Journal/Receipt/Payment | No stock effect |

### 7.6 Ledger Posting Rules

#### Sale (Intra-State, ₹10,000 + 18% GST)
```
Cash/Bank A/c          Dr.  11,800
    To Sales A/c                 10,000
    To CGST Payable A/c             900
    To SGST Payable A/c             900
```

#### Purchase (Inter-State, ₹5,000 + 18% GST)
```
Purchase A/c           Dr.   5,000
CGST Input Credit A/c  Dr.     450
SGST Input Credit A/c  Dr.     450
    To Supplier A/c              5,900
```

#### Receipt (Customer Pays)
```
Cash/Bank A/c          Dr.   X,XXX
    To Customer A/c             X,XXX
```

#### Payment (Pay Supplier)
```
Supplier A/c           Dr.   X,XXX
    To Cash/Bank A/c            X,XXX
```

### 7.7 Party Balance Rules

- **DR (Debit) Balance:** Party owes you money (Receivable)
- **CR (Credit) Balance:** You owe party money (Payable)
- **Balance = Opening Balance + Total Debits - Total Credits**
- Balance sign indicates direction: positive = DR, negative = CR

### 7.8 Financial Year Rules

- Indian standard: April 1 to March 31
- Example: FY 2025-26 = April 1, 2025 to March 31, 2026
- All vouchers, reports, and balances are FY-specific
- Year-end closing carries forward balances to next FY
- Previous FY data remains accessible via reports

---

## 8. Data Model Design

### 8.1 Core Entities

#### BusinessProfile
```
├── id (PK)
├── businessName
├── ownerName
├── address
├── pinCode
├── city
├── state
├── stateCode
├── phoneNumber
├── email
├── gstin
├── pan
├── accountNumber
├── ifscCode
├── bankName
├── bankBranch
├── logoUri
├── signatureUri
├── termsAndConditions
└── fyLabel (current financial year)
```

#### Party
```
├── id (PK, UUID)
├── name
├── type (CUSTOMER/SUPPLIER/BOTH)
├── phoneNumber
├── email
├── address
├── pinCode
├── city
├── state
├── stateCode
├── gstin
├── pan
├── openingBalance
├── balanceType (DR/CR)
├── creditLimit
├── creditDays
├── notes
├── createdAt
└── updatedAt
```

#### Product
```
├── id (PK, UUID)
├── name
├── hsnCode
├── unit (PCS/KG/LTR/MTR/BOX/BAG/NOS)
├── saleRate
├── purchaseRate
├── gstRate
├── openingStock
├── currentStock
├── lowStockThreshold
├── lowStockAlertEnabled
├── barcode
├── batchEnabled
├── batchNumber
├── expiryEnabled
├── expiryDate
├── serialTrackingEnabled
├── secondaryUnit
├── conversionFactor
├── createdAt
└── updatedAt
```

#### Voucher
```
├── id (PK, UUID)
├── voucherNumber (auto-generated)
├── type (SALE/PURCHASE/RECEIPT/PAYMENT/JOURNAL/...)
├── date
├── partyId (FK)
├── partyName (denormalized)
├── subtotal (taxable amount)
├── totalGst
├── roundOff
├── netAmount
├── paymentMode (CASH/BANK/UPI/CHEQUE)
├── paymentStatus (PAID/UNPAID/PARTIAL/CANCELLED)
├── outstandingAmount
├── narration
├── referenceNo
├── referenceDate
├── chequeNumber
├── chequeBank
├── chequeBranch
├── upiId
├── transporterName
├── vehicleNo
├── lrNo
├── transporterGstin
├── destination
├── isInterstate
├── createdAt
└── updatedAt
```

#### VoucherItem
```
├── id (PK, UUID)
├── voucherId (FK)
├── productId (FK)
├── productName (denormalized)
├── hsnCode
├── quantity
├── unit
├── rate
├── discount
├── discountType (PERCENTAGE/AMOUNT)
├── taxableAmount
├── gstRate
├── gstAmount
├── cgstAmount
├── sgstAmount
├── igstAmount
├── totalAmount
└── sortOrder
```

#### LedgerEntry
```
├── id (PK, UUID)
├── voucherId (FK)
├── accountHead (e.g., "Cash", "Bank", "Party: Ramesh Traders")
├── debit
├── credit
├── narration
├── date
├── fyLabel
├── createdAt
└── updatedAt
```

### 8.2 Supporting Entities

#### LedgerAccount
```
├── id (PK)
├── name
├── groupName
├── openingBalance
├── balanceType (DR/CR)
├── phone
├── email
├── address
├── isSystemAccount
└── partyId (nullable FK)
```

#### Expense
```
├── id (PK, UUID)
├── expenseNumber (auto-generated)
├── date
├── category
├── description
├── amount
├── paymentMode
├── referenceNo
├── receiptUri
├── fyLabel
└── createdAt
```

#### Income
```
├── id (PK, UUID)
├── incomeNumber (auto-generated)
├── date
├── category
├── description
├── amount
├── paymentMode
├── referenceNo
├── fyLabel
└── createdAt
```

### 8.3 Financial Year Entities

#### PartyFinancialYearBalance
```
├── partyId (FK)
├── fyLabel
├── openingBalance
├── balanceType
├── totalDebit
├── totalCredit
└── closingBalance
```

#### ProductFinancialYearBalance
```
├── productId (FK)
├── fyLabel
├── openingStock
├── openingStockValue
├── totalInward
├── totalOutward
├── closingStock
└── closingStockValue
```

#### LedgerAccountFinancialYearBalance
```
├── accountId (FK)
├── fyLabel
├── openingBalance
├── balanceType
├── totalDebit
├── totalCredit
└── closingBalance
```

---

## 9. Accounting Engine

### 9.1 Chart of Accounts Structure

#### Assets (Debit Balance)
```
├── Current Assets
│   ├── Cash-in-Hand
│   ├── Bank Accounts
│   │   ├── Cash Account
│   │   └── Bank Account
│   ├── Sundry Debtors (Customers)
│   ├── Stock-in-Trade (Inventory)
│   ├── GST Input Credit
│   │   ├── CGST Input
│   │   ├── SGST Input
│   │   └── IGST Input
│   └── Advances (To Suppliers)
```

#### Liabilities (Credit Balance)
```
├── Current Liabilities
│   ├── Sundry Creditors (Suppliers)
│   ├── GST Payable
│   │   ├── CGST Payable
│   │   ├── SGST Payable
│   │   └── IGST Payable
│   └── Advances (From Customers)
├── Capital Account
│   └── Owner's Capital
```

#### Income (Credit Balance)
```
├── Direct Income
│   ├── Sales Account
│   └── Sales Return Account
├── Indirect Income
│   ├── Interest Income
│   ├── Rental Income
│   └── Other Income
```

#### Expenses (Debit Balance)
```
├── Direct Expenses
│   ├── Purchase Account
│   └── Purchase Return Account
├── Indirect Expenses
│   ├── Salaries
│   ├── Rent
│   ├── Electricity
│   ├── Telephone
│   ├── Office Expenses
│   ├── Bank Charges
│   └── Miscellaneous
└── Round Off Account
```

### 9.2 Voucher Posting Logic

#### Sale Voucher (Intra-State)
```
Input: Party/Cash, Items with GST, Payment Mode

Step 1: Calculate line items
  taxableAmount = qty × rate - discount
  gstAmount = taxableAmount × gstRate / 100
  cgstAmount = gstAmount / 2
  sgstAmount = gstAmount / 2
  lineTotal = taxableAmount + gstAmount

Step 2: Sum voucher totals
  subtotal = Σ taxableAmount
  totalCgst = Σ cgstAmount
  totalSgst = Σ sgstAmount
  totalGst = totalCgst + totalSgst
  grossAmount = subtotal + totalGst
  netAmount = grossAmount + roundOff

Step 3: Post ledger entries
  Debit: Cash/Bank/Party = netAmount
  Credit: Sales Account = subtotal
  Credit: CGST Payable = totalCgst
  Credit: SGST Payable = totalSgst
  (If roundOff != 0, add balancing entry)

Step 4: Update stock
  For each item: currentStock -= quantity

Step 5: Update party balance
  If credit sale: party.balance += netAmount
```

#### Receipt Voucher
```
Input: Party, Amount, Payment Mode

Step 1: Post ledger entries
  Debit: Cash/Bank = amount
  Credit: Party: {name} = amount

Step 2: Update party balance
  party.balance -= amount

Step 3: Update voucher outstanding
  If against specific invoice: reduce outstandingAmount
```

### 9.3 Balance Calculation

```
Party Balance = Opening Balance + Σ(Debits) - Σ(Credits)

If result > 0: DR (Debit) balance — Party owes you
If result < 0: CR (Credit) balance — You owe party
If result = 0: Settled
```

### 9.4 Profit Calculation

```
Gross Profit = Sales - Cost of Goods Sold
COGS = Opening Stock + Purchases - Closing Stock

Net Profit = Gross Profit + Other Income - Operating Expenses

Note: Current implementation uses simplified formula:
  Net Profit ≈ Monthly Taxable Sales - Monthly Taxable Purchases
  (This is an approximation; full P&L requires proper COGS calculation)
```

---

## 10. GST Compliance

### 10.1 GSTIN Validation Rules

1. **Format:** 2-digit state code + PAN (10 chars) + 1 digit entity code + Z (default) + 1 check digit
2. **Length:** Exactly 15 characters
3. **State Code:** Must match valid Indian state/UT codes (01-38, 97)
4. **PAN:** Valid PAN embedded in positions 3-12
5. **Auto-Fill:** GSTIN lookup fills business name, state, PAN

### 10.2 State Code Mapping

| Code | State | Code | State |
|------|-------|------|-------|
| 01 | Jammu & Kashmir | 19 | West Bengal |
| 02 | Himachal Pradesh | 20 | Jharkhand |
| 03 | Punjab | 21 | Odisha |
| 04 | Chandigarh | 22 | Chhattisgarh |
| 05 | Uttarakhand | 23 | Madhya Pradesh |
| 06 | Haryana | 24 | Gujarat |
| 07 | Delhi | 25 | Daman & Diu |
| 08 | Rajasthan | 26 | Dadra & Nagar Haveli |
| 09 | Uttar Pradesh | 27 | Maharashtra |
| 10 | Bihar | 28 | Andhra Pradesh (Old) |
| 11 | Sikkim | 29 | Karnataka |
| 12 | Arunachal Pradesh | 30 | Goa |
| 13 | Nagaland | 31 | Lakshadweep |
| 14 | Manipur | 32 | Kerala |
| 15 | Mizoram | 33 | Tamil Nadu |
| 16 | Tripura | 34 | Puducherry |
| 17 | Meghalaya | 35 | Andaman & Nicobar |
| 18 | Assam | 36 | Telangana |
| | | 37 | Andhra Pradesh |
| | | 97 | Ladakh |

### 10.3 GST Calculation Matrix

| Transaction Type | Seller State | Buyer State | Tax Applied |
|------------------|--------------|-------------|-------------|
| Intra-State | Same | Same | CGST + SGST |
| Inter-State | Different | Different | IGST |
| Import | Outside India | India | IGST |
| Export | India | Outside India | Zero-rated |

### 10.4 Input Tax Credit (ITC) Rules

- ITC available only for business purchases
- ITC not available for exempt goods
- ITC must be claimed within stipulated time
- ITC reversed if payment not made within 180 days
- ITC available on transport charges (if GST charged)

### 10.5 GST Summary Report Components

#### Output Tax (Collected)
```
├── CGST Collected (Sales)
├── SGST Collected (Sales)
├── IGST Collected (Interstate Sales)
└── Total Output Tax
```

#### Input Tax Credit (Paid)
```
├── CGST Paid (Purchases)
├── SGST Paid (Purchases)
├── IGST Paid (Interstate Purchases)
└── Total Input Tax Credit
```

#### Net Position
```
Net GST Payable = Total Output Tax - Total Input Tax Credit
If positive: Pay to government
If negative: Carry forward ITC
```

---

## 11. Inventory System

### 11.1 Stock Valuation

**Current Method:** Moving Average Cost (simplified)

```
Current Stock = Opening Stock + Purchases + Purchase Returns - Sales - Sales Returns - Delivery Challans

Stock Value = Current Stock × Purchase Rate (weighted average)
```

**Future Enhancement:** FIFO (First-In-First-Out) tracking

### 11.2 Stock Alert System

| Condition | Status | Visual |
|-----------|--------|--------|
| `currentStock > lowStockThreshold` | In Stock | Green chip |
| `currentStock <= lowStockThreshold` | Low Stock | Orange chip |
| `currentStock == 0` | Out of Stock | Red chip |

### 11.3 Stock Movement Tracking

```
Each voucher item creates a stock movement:
  ├── Product ID
  ├── Quantity (positive for inward, negative for outward)
  ├── Voucher ID (reference)
  ├── Date
  └── FY Label
```

### 11.4 Multi-Unit Support

| Selling Unit | Stock Unit | Conversion |
|--------------|------------|------------|
| PCS | BOX | 12 (1 box = 12 pieces) |
| GM | KG | 1000 (1 kg = 1000 gm) |
| ML | LTR | 1000 (1 litre = 1000 ml) |

**Note:** Conversion factor is defined per product but not yet applied during stock calculations.

---

## 12. Reporting System

### 12.1 Report Types

#### Financial Reports
| Report | Purpose | Key Metrics |
|--------|---------|-------------|
| Trial Balance | Verify ledger accuracy | Total Debits = Total Credits |
| Profit & Loss | Business profitability | Revenue, Expenses, Net Profit |
| Balance Sheet | Financial position | Assets, Liabilities, Equity |

#### Tax Reports
| Report | Purpose | Key Metrics |
|--------|---------|-------------|
| GST Summary | GST liability | Output Tax, Input Credit, Net Payable |

#### Business Reports
| Report | Purpose | Key Metrics |
|--------|---------|-------------|
| Outstanding Receivables | Customer dues | Party-wise outstanding, aging |
| Outstanding Payables | Supplier dues | Party-wise outstanding, aging |
| Stock Report | Inventory status | Current stock, low stock items |

#### Accounting Reports
| Report | Purpose | Key Metrics |
|--------|---------|-------------|
| Ledger Books | Account balances | Group-wise accounts, Dr/Cr balances |

### 12.2 Report Data Flow

```
Dashboard KPIs ← Derived from Ledger Entries + Vouchers
     ↓
Reports ← Aggregated from Ledger Entries + Vouchers + Products
     ↓
Export ← CSV/PDF generation from report data
```

### 12.3 Dashboard KPI Definitions

| KPI | Calculation | Refresh |
|-----|-------------|---------|
| Today's Sales | SUM(sale.voucherAmount) WHERE date = today | Real-time |
| Today's Purchases | SUM(purchase.voucherAmount) WHERE date = today | Real-time |
| This Month's Sales | SUM(sale.voucherAmount) WHERE date in current month | Real-time |
| Net Profit (Est.) | Monthly Taxable Sales - Monthly Taxable Purchases | Real-time |
| Receivables | SUM(party.balance) WHERE balance > 0 | Real-time |
| Payables | SUM(party.balance) WHERE balance < 0 | Real-time |
| Cash Account | Ledger balance for "Cash" account | Real-time |
| Bank & UPI | Ledger balance for "Bank" account | Real-time |
| Inventory | SUM(product.currentStock × product.purchaseRate) | Real-time |
| GST | Output Tax - Input Tax Credit | Real-time |

---

## 13. Security & Access Control

### 13.1 Authentication

| Mechanism | Implementation | Scope |
|-----------|----------------|-------|
| PIN Lock | 4-digit numeric PIN | App access |
| Biometric | Android BiometricPrompt (planned) | App access |

### 13.2 Data Security

| Data Type | Storage | Protection |
|-----------|---------|------------|
| Business Profile | SQLite | Local only |
| GSTIN/PAN | SQLite | Local only |
| Bank Details | SQLite | Local only |
| SMTP Password | SharedPreferences | Plain text (risk) |
| PIN | SharedPreferences | Hashed |
| Voucher Data | SQLite | Local only |
| Backups | File system | User-managed |

### 13.3 Security Risks Identified

| Risk | Severity | Mitigation |
|------|----------|------------|
| SMTP password in plain text | High | Implement EncryptedSharedPreferences |
| No audit trail for data changes | High | Add audit log table |
| Physical deletion of vouchers | High | Implement soft-delete/cancellation |
| No data validation on GSTIN | Medium | Add checksum validation |
| No encryption of database file | Medium | Enable SQLCipher |
| PIN stored in SharedPreferences | Medium | Use Android Keystore |

### 13.4 Access Control (Current)

| Role | Access | Limitations |
|------|--------|-------------|
| Single User | Full access | No multi-user support |
| No Roles | All functions | No permission granularity |
| No Audit | No logging | Cannot track who did what |

---

## 14. Offline-First Architecture

### 14.1 Data Storage

```
Primary Storage: Room (SQLite)
├── Vouchers table
├── Parties table
├── Products table
├── Ledger Entries table
├── Expense/Income tables
└── Configuration tables

Preferences Storage: DataStore/SharedPreferences
├── Theme selection
├── PIN hash
├── Financial year label
└── App settings

File Storage: App Internal/External
├── Invoice PDFs
├── CSV exports
├── Database backups
└── Receipt images
```

### 14.2 Network Dependencies

| Feature | Network Required | Fallback |
|---------|------------------|----------|
| PIN Code Lookup | Yes | Manual entry |
| IFSC Lookup | Yes | Manual entry |
| GSTIN Lookup | Yes | Manual entry |
| Barcode Scan | No | Camera-based |
| Email Sending | Yes | Manual sharing |
| All Core Features | No | Full offline support |

### 14.3 Sync Strategy (Future)

```
Current: No sync (single device only)
Future: 
├── Optional cloud backup
├── Multi-device sync (via UUID primary keys)
└── Conflict resolution (last-write-wins)
```

---

## 15. Non-Functional Requirements

### 15.1 Performance

| Metric | Target | Current |
|--------|--------|---------|
| Cold start time | < 2 seconds | ~3 seconds |
| Hot start time | < 500ms | ~800ms |
| Voucher list scroll | 60 fps | 60 fps (1000 items) |
| Search response | < 200ms | ~150ms |
| Database query | < 100ms | ~50ms |
| PDF generation | < 3 seconds | ~2 seconds |
| Export to CSV | < 5 seconds | ~3 seconds |

### 15.2 Scalability

| Dimension | Current Limit | Notes |
|-----------|----------------|-------|
| Vouchers per FY | ~50,000 | SQLite performs well up to 100K rows |
| Parties | ~5,000 | Limited by search UX |
| Products | ~10,000 | Limited by product grid UX |
| Ledger Entries | ~500,000 | Indexed, performant |
| Database Size | ~500 MB | Before performance degrades |

### 15.3 Compatibility

| Requirement | Specification |
|-------------|---------------|
| Min Android Version | 7.0 (API 24) |
| Target Android Version | 14 (API 34) |
| Screen Sizes | 4.5" to 12.9" |
| Orientation | Portrait (primary), Landscape (tablet) |
| Languages | English (primary), Hindi (planned) |
| Accessibility | Basic (content descriptions, contrast) |

### 15.4 Reliability

| Metric | Target |
|--------|--------|
| Data loss | Never (SQLite ACID) |
| Crash rate | < 1% sessions |
| ANR rate | < 0.5% sessions |
| Backup integrity | 100% recoverable |

### 15.5 Usability

| Metric | Target |
|--------|--------|
| First-time setup | < 3 minutes |
| First voucher creation | < 2 minutes |
| Daily billing (per transaction) | < 30 seconds |
| Task success rate | > 90% |
| User satisfaction | > 4.0/5.0 |

---

## 16. Success Metrics

### 16.1 Product Metrics

| Metric | Definition | Target (6 months) |
|--------|------------|-------------------|
| Downloads | Total app installs | 10,000 |
| DAU | Daily active users | 1,000 |
| Retention (D7) | Users returning after 7 days | 40% |
| Retention (D30) | Users returning after 30 days | 25% |
| Vouchers/User/Month | Average vouchers created | 50 |
| Session Length | Average time per session | 8 minutes |
| Session Frequency | Average sessions per day | 3 |

### 16.2 Business Metrics

| Metric | Definition | Target |
|--------|------------|--------|
| NPS | Net Promoter Score | > 50 |
| App Store Rating | Google Play rating | > 4.2 |
| Support Tickets | Per 1000 users/month | < 50 |
| Feature Requests | Categorized and prioritized | Tracked |
| Bug Reports | Critical bugs per release | < 5 |

### 16.3 Technical Metrics

| Metric | Definition | Target |
|--------|------------|--------|
| Crash-Free Rate | Sessions without crash | > 99% |
| ANR-Free Rate | Sessions without ANR | > 99.5% |
| Database Corruption | Corrupted databases | 0 |
| Data Export Success | Successful CSV/PDF exports | > 99% |
| Email Delivery | Successful email sends | > 95% |

---

## 17. Constraints & Limitations

### 17.1 Technical Constraints

| Constraint | Impact | Mitigation |
|------------|--------|------------|
| Single-device only | No multi-device sync | Future KMP rebuild |
| SQLite limitations | No concurrent writes | Single-user design |
| Android-only | No iOS/Desktop | Future KMP rebuild |
| No cloud backend | No remote access | Optional cloud backup planned |
| Local storage limits | Device storage dependent | Export/backup features |

### 17.2 Business Constraints

| Constraint | Impact | Mitigation |
|------------|--------|------------|
| Single business profile | Multi-business owners limited | Future multi-company support |
| No multi-user | Single operator only | Future role-based access |
| No payroll | Cannot manage employees | Out of scope |
| No TDS/TCS | Limited compliance | Future enhancement |
| No e-invoice | Limited GST compliance | Future NIC API integration |

### 17.3 Regulatory Constraints

| Constraint | Impact | Mitigation |
|------------|--------|------------|
| GST rates change | Need rate updates | Make rates configurable |
| State codes update | Need code updates | Maintain mapping table |
| E-invoice mandate | Required for turnover > ₹5 Cr | Future integration |
| E-way bill mandate | Required for goods movement > ₹50K | Future integration |

---

## 18. Future Roadmap

### 18.1 Phase 1: Accounting Foundation (Q3 2026)

| Feature | Priority | Effort |
|---------|----------|--------|
| Trial Balance report | P0 | 2 weeks |
| Balance Sheet report | P0 | 3 weeks |
| Profit & Loss report | P0 | 3 weeks |
| Day Book report | P0 | 1 week |
| Soft-delete for vouchers | P0 | 2 weeks |
| Audit trail | P0 | 2 weeks |
| Fix ledger posting logic | P0 | 4 weeks |

### 18.2 Phase 2: GST Compliance (Q4 2026)

| Feature | Priority | Effort |
|---------|----------|--------|
| GSTR-1 data export | P1 | 3 weeks |
| GSTR-3B summary | P1 | 2 weeks |
| HSN-wise summary | P1 | 2 weeks |
| Reverse Charge support | P1 | 2 weeks |
| Composition scheme support | P2 | 1 week |

### 18.3 Phase 3: Retail Features (Q1 2027)

| Feature | Priority | Effort |
|---------|----------|--------|
| Thermal printer support | P2 | 3 weeks |
| Barcode scanning in billing | P2 | 2 weeks |
| Hold/Recall bill | P2 | 1 week |
| Split payment | P2 | 2 weeks |
| Batch/expiry tracking | P2 | 4 weeks |

### 18.4 Phase 4: Platform Expansion (Q2 2027)

| Feature | Priority | Effort |
|---------|----------|--------|
| Kotlin Multiplatform rebuild | P3 | 12 weeks |
| iOS version | P3 | 8 weeks |
| Desktop version | P3 | 6 weeks |
| Cloud sync (optional) | P3 | 8 weeks |
| Multi-user with roles | P3 | 6 weeks |

---

*Document version: 3.0 — July 2026*  
*Product: ZeroBook — Retail Accounting for Indian Small Businesses*  
*Codebase: Android (Kotlin, Jetpack Compose, Room, MVVM)*