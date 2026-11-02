# SUHAEB TX

Standalone Android messaging app built with Kotlin and Jetpack Compose.

## Initial status
This commit establishes the Android app foundation and an Arabic-first starter interface. Firebase credentials are intentionally not included. Live authentication, messaging, uploads, and calls must not be represented as working until a Firebase project and secure backend rules are configured and tested.

## Android
- Application ID: `com.suhaeb.tx`
- Kotlin + Jetpack Compose
- Minimum Android: API 24
- Target SDK: 35

## Firebase setup
1. Create a Firebase project and register package `com.suhaeb.tx`.
2. Add the generated `google-services.json` to `app/` (do not commit service-account keys).
3. Enable Authentication, Firestore, and Storage after reviewing security rules.
4. Configure server-enforced authorization, rate limits, abuse reporting, and retention policies before production.

## Build
GitHub Actions builds a debug APK. A debug APK is not a signed production release.
