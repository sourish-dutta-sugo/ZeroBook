# ZeroBook 📒

**Record. Transact. Grow.**

ZeroBook is a professional retail accounting and GST invoicing app built for small and medium Indian retailers — kirana stores, small shops, and traders who currently rely on pen-and-paper ledgers or oversized desktop accounting software that doesn't fit their workflow.

This repository contains the **native Android edition** of ZeroBook, built entirely with Kotlin and Jetpack Compose.

---

## 🎯 Why ZeroBook

Most small Indian retailers fall into a gap: too small for Tally Prime / desktop ERP, but too serious about their business for a plain notebook. They need GST-compliant billing, party (customer/supplier) ledgers, stock tracking, and basic financial reports — without needing an accountant on payroll or a steep learning curve.

ZeroBook targets this segment specifically:
- **Who:** Small retailers, traders, and shop owners across India, mostly first-time digital-accounting adopters.
- **Why:** GST compliance is mandatory, but existing tools are either too complex (Tally) or too basic (plain billing apps with no ledger/accounting depth).
- **How:** A single, lightweight mobile app that combines invoicing, double-entry-style ledgers, inventory, and reports in one place — designed mobile-first, usable one-handed in a shop.

---

## ✨ Core Features

- **GST Billing & Invoicing** — Create Sale, Purchase, Sale Return, Purchase Return, Receipt, Payment, Debit Note, and Credit Note vouchers with automatic CGST/SGST/IGST calculation based on intrastate/interstate detection.
- **Quick Sale** — A fast, simplified billing flow for high-frequency counter sales.
- **Party & Ledger Management** — Track customers and suppliers with running balances, financial-year-wise ledger accounts, and bills receivable.
- **Inventory / Products** — Product catalog with HSN code lookup, stock tracking, and stock reports.
- **Bank & Cash Tracking** — Record bank/cash transactions and reconcile against vouchers.
- **Expenses Module** — Log and categorize business expenses separately from trading vouchers.
- **Barcode Scanning & OCR** — CameraX + ML Kit-powered barcode scanning and text recognition (e.g. for scanning purchase bills/products).
- **Reports** — Business and stock-level reports for day-to-day decision-making.
- **Email Integration** — Send invoices/reports directly via an in-app email composer with scheduled reminders (WorkManager).
- **Multi-Theme UI** — 5 switchable color themes (Beach, Blue, Green, and more), built on Material 3.
- **Financial Year Management** — Data is partitioned and balanced by financial year, with audit logs for year transitions.
- **First-Run Setup** — A guided onboarding/setup screen for business profile details (GSTIN, PAN, bank info) before the first invoice is created.

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM (ViewModel + Repository pattern) |
| Local Database | Room (SQLite) |
| Async | Kotlin Coroutines & Flow |
| Networking | Retrofit + Moshi + OkHttp |
| Camera / Scanning | CameraX, ML Kit (Barcode Scanning, Text Recognition) |
| Background Work | WorkManager (email reminders) |
| Preferences | Jetpack DataStore |
| Image Loading | Coil |
| Email | JavaMail (Android port) |
| Build System | Gradle (Kotlin DSL), AGP 9.1.1, KSP |
| Testing | JUnit, Espresso, Robolectric, Roborazzi (screenshot testing) |

**Minimum SDK:** 24 (Android 7.0) · **Target SDK:** 35 · **Compile SDK:** 36

> **Note:** This is the original Android-only codebase. ZeroBook is currently being rebuilt as a **Kotlin Multiplatform (KMP) + Compose Multiplatform** project to additionally support Android tablets, iOS, Desktop (Windows/macOS/Linux), and Web — using SQLDelight in place of Room for a shared, multiplatform-friendly database layer. This repository represents the stable Android-only reference implementation that the multiplatform rebuild is targeting for feature parity.

---

## 📂 Project Structure

ZeroBook---Only Android/

├── app/

│   └── src/main/java/com/example/

│       ├── data/         # Room entities, DAOs, repository, migrations, GST/financial-year utils

│       ├── services/     # Invoice generation, CSV transfer, export storage, email composer

│       ├── ui/

│       │   ├── screens/  # All Compose screens (Dashboard, Vouchers, Parties, Products, etc.)

│       │   └── theme/    # Multi-theme system (Beach, Blue, Green, ...), Material 3 styling

│       └── utils/        # HSN lookup, file picker, helpers

├── playstore/            # Store listing assets (icon, feature graphic)

├── gradle/                # Version catalog (libs.versions.toml), wrapper

└── build.gradle.kts / settings.gradle.kts

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest stable)
- JDK 17
- An Android device/emulator running API 24+

### Build & Run
```bash
git clone https://github.com/<your-username>/ZeroBook.git
cd ZeroBook
./gradlew assembleDebug
```
Open the project in Android Studio and run it on a device/emulator. On first launch, the app opens the **Setup screen** for business profile entry; subsequent launches open directly to the **Dashboard**.

### Environment Variables
Copy `.env.example` to `.env` and fill in any required secrets (e.g., signing keys, API keys) before building a release variant.

---

## 🗺 Roadmap

- [ ] Full Kotlin Multiplatform rebuild (Android, iOS, Desktop, Web) sharing one business-logic/database layer via SQLDelight
- [ ] Tally-style keyboard shortcuts for desktop
- [ ] OCR-based automatic purchase bill data extraction
- [ ] Expanded e-way bill and CRM fields
- [ ] Batch/serial number and barcode-based inventory tracking
- [ ] Play Store public release

Development follows a research-then-build cadence: feature parity is mapped from this Android reference app, then implemented incrementally with thorough testing on every supported platform before merging.

---

## 🤝 Contributing

This project is under active solo development. Issues and feature suggestions are welcome — please open a GitHub Issue describing the use case (small-retailer workflows are the priority lens for any new feature).


# ZeroBook License

Copyright (c) 2026 Sugo. All Rights Reserved (with permissions granted below).

## Permissions

You ARE allowed to:
- View, download, and study the source code of this project.
- Fork the repository and modify the code for your own personal or internal use.
- Submit improvements, bug fixes, or feature additions back to this repository via Pull Requests.
- Build and run your own modified version locally for personal, non-commercial testing or learning purposes.

## Restrictions

You are NOT allowed to:
- Claim this project, or any modified/derivative version of it, as your own original work.
- Remove or alter the original author's name, credit, or copyright notice from the source code, README, or any associated documentation.
- Publish, distribute, sell, or release this app (or a renamed/rebranded/repackaged version of it) on the Google Play Store, Apple App Store, F-Droid, or any other app store or marketplace under your own name or any name other than "ZeroBook" with proper attribution to the original author.
- Use the ZeroBook name, branding, logo, or app icon for any other application or product.
- Sub-license, sell, or relicense this codebase, in whole or in part, as a separate proprietary product.
- Use this project or its code for any unlawful purpose.

## Contributions

By submitting a Pull Request, issue, or any other contribution to this repository, you agree that:
- Your contribution becomes part of this project under this same license.
- You will not claim ownership over the project as a whole due to your contribution.
- You grant the original author full rights to use, modify, merge, or reject your contribution at their discretion.

## No Warranty

This software is provided "as is", without warranty of any kind, express or implied, including but not limited to warranties of merchantability, fitness for a particular purpose, and non-infringement. In no event shall the author be liable for any claim, damages, or other liability arising from the use of this software.

## Summary (Plain English)

You're welcome to read, learn from, fork, and contribute to this project. You may NOT take this app, rename it, and release it as your own. Credit to the original author must always remain intact.
