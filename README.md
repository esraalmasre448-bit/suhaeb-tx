# SUHAEB CHAT

Standalone Arabic-first Android messaging app built with Kotlin and Jetpack Compose.

## Current implementation
- Native Android shell with SUHAEB CHAT black/neon-green branding.
- Firebase email/password registration, verification-link flow, sign-in and password recovery.
- Owner-scoped profile bootstrap and a read-only chat-list listener.
- Firestore and Storage are intentionally restrictive; direct messages, media and groups are not enabled yet.

## Build
GitHub Actions builds a debug APK. A debug APK is for testing and is not a signed production release.

## Security tests
Install Node.js 20 and Java 17, then run:
```sh
npm install
npm run test:rules
```
The command starts the Firestore Emulator and runs authorization tests against `firestore.rules`.

## Firebase
The Android Firebase config file identifies the Firebase project and app; it is not a service-account credential. Never commit service-account JSON, signing keys, passwords, or admin tokens. Do not deploy rules until emulator tests pass and the schema has been reviewed.
