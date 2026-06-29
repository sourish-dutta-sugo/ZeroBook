# DEPENDENCY_MAP - ZeroBook

## Frameworks & Language
- **Kotlin:** Language (1.9.22+)
- **Compose:** UI (Material 3)
- **Room:** Persistence (2.7.0-alpha01)

## Architecture & Logic
- **WorkManager:** Background tasks (Email reminders).
- **Navigation Compose:** Screen transitions.
- **DataStore:** Preference persistence.
- **Coroutines/Flow:** Async operations and reactive UI.

## Utilities
- **Coil:** Image loading.
- **JavaMail (Android port):** SMTP email delivery.
- **Moshi:** JSON parsing.
- **Retrofit/OkHttp:** Networking (scaffolding).
- **CameraX / ML Kit:** Barcode scanning and text recognition.
- **Play Services Location:** PIN-code geocoding (scaffolding).

## Testing
- **JUnit 4:** Unit tests.
- **Espresso:** UI tests.
- **Robolectric:** JVM-based Android tests.
- **Roborazzi:** Screenshot testing.

## Build Plugins
- **AGP (Android Gradle Plugin):** 9.1.1
- **KSP (Kotlin Symbol Processing):** Room/Moshi code generation.
- **Secrets Gradle Plugin:** Env variable injection.
