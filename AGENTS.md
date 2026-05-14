# Project Rules for Codex

## Tech stack
- Android, Kotlin, Jetpack Compose
- Architecture: Clean (data/domain/presentation)
- DI: (Hilt/Koin)  # fill yours

## Commands to validate
- ./gradlew test
- ./gradlew lint
- ./gradlew assembleDebug

## Coding rules
- Prefer immutable state + StateFlow
- No new dependencies unless asked
- Keep changes minimal and high-confidence
- Add/adjust unit tests when logic changes
