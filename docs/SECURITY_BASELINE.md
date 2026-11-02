# Security Baseline

This document distinguishes implemented controls from launch requirements.

## Implemented in the current starter
- Firebase Authentication email/password flow with verified-email gating in the Android UI.
- Firestore rules require the verified-email token claim.
- User profiles are owner-readable, cannot be listed, and have a narrow field allowlist.
- Profile creation requires server timestamps; profile updates cannot change uid or createdAt.
- Chat metadata is read-only for verified members. Chat writes, nested messages, and unknown collections are denied.
- Storage remains deny-all until upload paths and UI are implemented.
- Emulator tests cover authenticated/unauthenticated access and default-deny behavior.

## Not implemented / launch blockers
- Direct chat creation, sending messages, group membership and trusted server-side membership changes.
- Block/report controls, anti-spam rate limiting and moderation.
- Storage media authorization and uploads.
- App Check, FCM, account export/deletion and retention.
- End-to-end encryption (must not be claimed).
- Production rules deployment and a signed release build.

## Firestore policy
- Deny by default.
- Require a verified Firebase email for profile and chat reads/writes.
- Keep profile fields narrowly allowlisted and immutable identifiers unchanged.
- Only permit chat reads for a member; do not allow client chat creation until recipient discovery, abuse controls, and membership invariants are implemented.
- Run the Firebase Emulator Suite tests before any deployment.

## Secrets and release
- Never commit service-account credentials, signing keys, passwords, or admin tokens.
- A Firebase Android configuration file is not a service-account key; restrict API keys with appropriate Google Cloud API and Android app restrictions.
- Debug APKs are test builds. Release builds need protected signing secrets and review.

## Launch gate
No production launch until rules tests, Storage authorization, abuse controls, account recovery, privacy behavior, and device tests have been reviewed.
