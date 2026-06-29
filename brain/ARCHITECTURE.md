# ARCHITECTURE - ZeroBook

## Architecture Pattern: MVVM (Model-View-ViewModel)

### Layers:
1.  **View (Jetpack Compose):**
    - Screens and components located in `com.zerobook.app.ui.screens`.
    - State is hoisted and managed by `AppViewModel`.
2.  **ViewModel (`AppViewModel`):**
    - The "God Object" managing app-wide state: setup status, PIN lock, active financial year, and screen-specific state.
    - Communicates with `AppRepository`.
3.  **Repository (`AppRepository`):**
    - The central hub for all business logic.
    - Handles "Voucher Save -> Posting" pipeline.
    - Manages transactional operations across multiple Room DAOs.
4.  **Model (Room Entities & DAOs):**
    - Entities defined in `com.zerobook.app.data.Entities`.
    - DAOs defined in `com.zerobook.app.data.AppDaos`.

## Data Flow
- **User Action:** Interaction in Compose Screen.
- **ViewModel Update:** Call to `AppViewModel`.
- **Repository Operation:** `AppViewModel` calls `AppRepository` (often inside a `db.withTransaction` block).
- **Database Persistence:** `AppRepository` uses DAOs to update SQLite.
- **Reactive UI:** DAOs expose `Flow<List<T>>` which bubbles up to the UI via the ViewModel, triggering recomposition.

## Major Systems
- **Voucher Posting Pipeline:** The most critical logic path in `AppRepository.saveAndPostVoucher`.
- **Financial Year Management:** Logic for April-March transitions and balance carry-forwards.
- **GST Calculation:** Distributed logic for determining tax splits based on Place of Supply.
- **Email/Reminder System:** Hybrid of WorkManager and direct SMTP/Intent sharing.

## Communication between layers
- Screens use `AppViewModel` for state and events.
- `AppViewModel` uses `AppRepository` for data fetching and mutations.
- `AppRepository` uses `AppDatabase` and its DAOs for persistence.
