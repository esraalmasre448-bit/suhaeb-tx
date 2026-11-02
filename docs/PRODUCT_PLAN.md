# SUHAEB TX — Product Plan

## Product principles
- Standalone Android application; no dependency on the user's health app.
- Arabic-first RTL experience with English localization.
- Privacy controls must be enforced by the backend, not merely hidden in the UI.
- No fake delivery, online, read, call, or encryption indicators.
- Ship in small tested phases; do not label mock screens as connected features.

## Phase 1 — Foundation
- Android app shell, navigation, design tokens, accessibility and localization.
- Firebase project registration and Auth integration.
- Profile creation with unique public username.
- Firestore data model and security rules.
- CI debug APK artifact.

## Phase 2 — Direct messaging
- Conversation creation and participant authorization.
- Realtime text messages, pagination, delivery/read state.
- Block, report, mute, and delete-for-self controls.
- Abuse throttling and server-side validation.

## Phase 3 — Media and voice
- Image/video/file uploads with size/type limits.
- Short voice notes and playback.
- Storage rules tied to conversation membership.
- Upload cancellation and retry handling.

## Phase 4 — Privacy and social
- Presence and last-seen preferences.
- Read receipts and typing indicators preferences.
- Groups, stories, and account controls.
- Privacy settings enforced consistently across clients.

## Phase 5 — Calls and release
- Evaluate a supported calling provider and its cost/limits before integration.
- Push notifications and background behavior.
- Threat model, security tests, accessibility checks, crash monitoring.
- Signed release build and key backup plan.

## Explicitly out of scope until proven
- Intercepting or controlling WhatsApp.
- Claiming end-to-end encryption without a reviewed protocol and implementation.
- Claiming real-time chat or calls before backend integration and multi-device tests.
