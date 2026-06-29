# PROJECT_CORE - ZeroBook

## What this project is
A 100%-native Android application for business management. It is currently a reference implementation for a planned Kotlin Multiplatform (KMP) rebuild.

## Main Objective
To provide a simple, robust, offline-first ledger and invoicing tool for Indian retailers, supporting:
- GST compliance (CGST/SGST/IGST).
- Double-entry accounting principles simplified for non-accountants.
- Inventory tracking.
- Financial reporting.

## Current Status
- **Development Phase:** Functional native Android app.
- **Roadmap:** Transitioning to KMP with SQLDelight (not yet implemented in this repository).
- **Deployment:** Targets Android API 24+ (minSdk) to 35 (targetSdk).

## Important Decisions
- **Offline First:** The app operates entirely locally without a required backend sync in its current state.
- **UUIDs for PKs:** Most entities use String UUIDs as primary keys to prevent collisions in future multi-device sync scenarios.
- **MVVM Pattern:** Using a centralized `AppViewModel` and `AppRepository` for simplicity in the current scale.
- **Custom License:** "View/fork/contribute but don't rebrand or republish under another name".
- **Indian Conventions:** April-March Financial Year, Indian currency formatting (lakh/crore), and PIN-code based address derivation.
