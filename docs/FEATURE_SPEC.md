# SUHAEB TX — Complete Product Requirements

## Product definition
SUHAEB TX is an independent native Android messaging application (Kotlin + Jetpack Compose), with an Arabic-first RTL interface and optional English localization. It is inspired by familiar messenger interaction patterns but uses its own identity, backend, accounts, and network. It is not a WhatsApp client, patch, clone of WhatsApp's private protocol, or a tool for changing WhatsApp accounts.

## Core messenger features
- Account registration, sign-in, sign-out, password recovery, verified email, profile photo, display name, unique username, bio, and account deletion/export flows.
- One-to-one conversations and group conversations with member management, roles, invitations, leave/report/block controls.
- Realtime text messaging, replies, edits, reactions, forwarding, message search, pagination, delivery state, and reliable retry/offline queue.
- Image, video, document, and other permitted file attachments with upload progress, cancellation, size/type validation, and download controls.
- Voice notes with recording, playback, duration, and upload retry.
- Audio/video calling only after a supported signaling/media provider and its costs, limits, and privacy implications are reviewed.
- Stories/status posts with expiry, audience controls, viewer list, and privacy options.
- Push notifications and per-chat mute/archive/pin controls.

## Reverse-privacy controls
All controls must be accurately enforced by the SUHAEB TX backend and other SUHAEB TX clients, not merely represented by local switches:
- Hide online status and last-seen time.
- Disable read receipts while still allowing the user to read messages.
- Hide typing indicators.
- View statuses without appearing in the viewer list.
- Per-account and per-chat privacy choices where technically consistent.
- Message retention/history controls, including a clearly disclosed local record of messages that a sender later deletes, only where the app itself received and lawfully retained the content.
- Chat lock using device-supported secure authentication and encrypted local storage.
- Block, report, mute, and control who can find/contact/add the user.

## Safety, privacy, and reliability
- Never expose passwords, private keys, service-account credentials, or admin secrets in the APK or public repository.
- Backend authorization for every private read/write; rate limits, abuse reporting, moderation, and safe group membership changes.
- App Check/device integrity evaluation before enforcement; secure local storage; screenshots protection only on screens where appropriate and clearly explained.
- No claim of end-to-end encryption until a reviewed protocol, key management, multi-device behavior, backup behavior, and security tests are implemented.
- No fake online/read/delivery/call indicators, fake users, or sample messages presented as live data.
- User controls must explain what they affect inside SUHAEB TX. They cannot hide activity or intercept/delete messages in the official WhatsApp application.
- Provide account/data deletion and retention behavior consistent with applicable law and user expectations.

## Delivery gates
1. Native Android shell and complete navigation/accessibility.
2. Firebase project setup, authentication, verified profiles, and tested Firestore/Storage authorization.
3. Real one-to-one messaging, realtime updates, delivery/read preferences, and abuse controls.
4. Media, files, voice notes, offline handling, notifications.
5. Groups, statuses, privacy enforcement, chat lock, and retention semantics.
6. Calling provider evaluation and implementation only if feasible within the agreed free/low-cost constraints.
7. Automated tests, emulator/device tests, signed release build, and release artifact.

## Current state (must be kept accurate)
The repository currently contains an early Compose UI skeleton and a Firebase rules/schema baseline. It does not yet contain a live Firebase project configuration, real authentication, realtime chat, media upload, voice recording, stories, calls, or backend-enforced privacy. A successful debug APK build of the starter shell is not a completed messenger. The public repository must not receive Firebase credentials or signing secrets.
