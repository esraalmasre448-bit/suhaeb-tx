# SUHAEB CHAT — Product Requirements

## Product definition
SUHAEB CHAT is an independent native Android messenger built with Kotlin and Jetpack Compose. It is Arabic-first (RTL), uses its own identity and backend, and does not modify, intercept, or connect to WhatsApp's private protocol.

## Core features planned
- Email/password registration, sign-in, verification, password recovery and profile.
- One-to-one and group chats with server-enforced membership.
- Realtime text messages, pagination, edits, replies, delivery/read state and retry.
- Image/video/document attachments and voice notes.
- Push notifications, blocking/reporting and privacy controls.
- Status posts and calls only after feasibility, cost and security review.

## Security and product truth
- Never show sample messages, fake presence, fake delivery indicators, or fake calls as live.
- Every private read/write must be enforced by backend rules.
- Do not expose passwords, service-account keys, signing keys, or admin secrets.
- Do not claim end-to-end encryption until a reviewed protocol and key lifecycle are implemented.
- Privacy switches must affect server behavior, not just UI.
- Avoid enabling client-side group membership changes without trusted validation and anti-abuse controls.

## Current implementation status
- Native Android Compose shell and Arabic-first branding: implemented.
- Firebase email/password auth, verification link and password reset: implemented in the current code; runtime testing remains required.
- Profile get/create/update flow: implemented; Firestore access remains blocked until reviewed rules are deployed.
- Chat list listener: present but cannot return useful data until chat creation and reads are enabled.
- Direct messaging, group management, attachments, voice notes, notifications, statuses, calls, block/report and end-to-end encryption: not implemented.
- Phone-number contact discovery and invitations: security design documented in `docs/CONTACT_DISCOVERY_DESIGN.md`; implementation is blocked pending verified phone linking and a trusted, rate-limited lookup backend.
- Firestore emulator security tests are being added in this phase.
- Storage is intentionally deny-all.
- Debug APK build is not a signed production release.
