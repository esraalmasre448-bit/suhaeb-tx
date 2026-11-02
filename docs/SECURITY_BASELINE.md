# Security Baseline

This is a design checklist, not a claim that production security is already implemented.

## Identity
- Firebase Authentication is the identity provider candidate.
- Public usernames must be unique and separate from private email/phone fields.
- Never trust a client-supplied role, owner ID, participant list, or moderation status.
- Account deletion and recovery need explicit, tested flows.

## Firestore
- Deny access by default.
- A user may read a conversation only when their authenticated UID is a participant.
- A user may create a message only as themselves and only inside a conversation they belong to.
- Validate immutable fields, allowed state transitions, payload sizes, and timestamps.
- Do not permit clients to grant themselves roles or alter another user's privacy settings.
- Add emulator tests for allowed and denied cases before deployment.

## Storage
- Require authentication and conversation membership.
- Restrict MIME types and file sizes.
- Prevent path spoofing and unauthorized overwrite/delete.
- Do not expose private media through public permanent URLs.

## Privacy
- Presence, last-seen, read receipts, typing indicators, and profile discovery must honor user settings.
- Blocking must prevent new direct messages and be enforced server-side.
- Report and abuse controls must not depend on client-only checks.

## Secrets and release
- Never commit Firebase service-account keys, signing keys, passwords, or API secrets.
- Keep signing credentials in protected GitHub Actions secrets.
- Do not ship admin credentials or privileged service tokens in the APK.
- Debug builds are for testing; release builds require signing and release review.

## Launch gate
No production launch until Firestore/Storage rules, emulator tests, abuse controls, privacy behavior, and account recovery have been reviewed.
