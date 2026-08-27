# MASTER BRAIN - ZeroBook

## Project Identity
**Project Name:** ZeroBook
**Tagline:** Record. Transact. Grow.
**Target Audience:** Indian small retailers (kirana stores, small traders).
**Status:** Native Android-only reference app (Kotlin/Compose).

## Project Purpose
ZeroBook is designed to help small business owners who have outgrown pen-and-paper ledgers but find full accounting software too complex. It provides GST-compliant invoicing, double-entry style ledgers, inventory management, and reports in a mobile-first, one-handed interface.

## Technology Stack
- **Language:** Kotlin (JVM 17)
- **UI Framework:** Jetpack Compose + Material 3
- **Local Database:** Room 2.7.0 (SQLite)
- **Architecture:** MVVM (AppViewModel + AppRepository + Room DAOs)
- **Async:** Coroutines + Flow
- **Background Work:** WorkManager
- **Persistence:** Jetpack DataStore + SharedPreferences
- **Email:** JavaMail (SMTP) + Intent-based sharing
- **Build System:** Gradle Kotlin DSL

## Brain Index
- [PROJECT_CORE.md](PROJECT_CORE.md) - High-level vision, status, and important decisions.
- [ARCHITECTURE.md](ARCHITECTURE.md) - Design patterns, data flow, and system structure.
- [CODEBASE_MAP.md](CODEBASE_MAP.md) - File responsibilities and folder structure.
- [FEATURES.md](FEATURES.md) - Detailed feature breakdown and flows.
- [UI_SYSTEM.md](UI_SYSTEM.md) - Navigation, screens, components, and design system.
- [DATA_SYSTEM.md](DATA_SYSTEM.md) - Database schema, models, and storage logic.
- [DEPENDENCY_MAP.md](DEPENDENCY_MAP.md) - External libraries and their roles.
- [SECURITY_AUDIT.md](SECURITY_AUDIT.md) - Current security posture and risks.
- [TECHNICAL_DEBT.md](TECHNICAL_DEBT.md) - Known issues, duplicates, and refactor targets.
- [CHANGE_HISTORY.md](CHANGE_HISTORY.md) - Log of all modifications.
- [AI_RULES.md](AI_RULES.md) - Rules for future AI agents.

## Rules for AI Agents
1. **Read MASTER_BRAIN.md first.**
2. Consult specific knowledge files before exploring or modifying code.
3. Keep the brain synchronized with any code changes.
4. Update `CHANGE_HISTORY.md` after every modification.
5. Follow the rules defined in [AI_RULES.md](AI_RULES.md).
