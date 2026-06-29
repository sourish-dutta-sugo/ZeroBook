# AI_RULES - ZeroBook

## Permanent Instructions for AI Agents

### 1. Read Brain First
Always read `brain/MASTER_BRAIN.md` and relevant specialized knowledge files before starting any task. Never scan the entire repository unless explicitly necessary to find a needle in a haystack.

### 2. Maintain Synchronization
After any change to the codebase (feature addition, refactor, fix), immediately update the corresponding brain files. Keep the map accurate to the territory.

### 3. Log Every Change
Every modification must be recorded in `brain/CHANGE_HISTORY.md` following the established format (Date, Change, Files affected, Reason, Impact).

### 4. Preservation of Intent
- Preserve the April-March Financial Year logic.
- Maintain the double-entry accounting integrity in `AppRepository`.
- Do not bypass the `isSetupCompleted` or `pinRequired` gates in `MainActivity`.

### 5. Architectural Consistency
- Favor the established MVVM pattern unless a deliberate architectural shift is requested.
- If adding a new entity, ensure it follows the "Migration + Idempotent Guard" pattern in `AppDatabase.kt`.

### 6. Minimal Footprint
Modify the minimum number of files necessary to achieve the goal. Avoid sweeping changes unless refactoring is the primary objective.

### 7. Understand Before Editing
Do not propose changes to complex logic (like `saveAndPostVoucher`) until the full sequence of transactional operations is understood.
