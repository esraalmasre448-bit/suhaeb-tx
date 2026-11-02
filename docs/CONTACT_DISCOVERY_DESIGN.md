# Contact discovery and invitations

## User experience
1. A user can enter a phone number or select a device contact after granting the optional Contacts permission.
2. Numbers are normalized to E.164 before verification or lookup. Never assume a country code from the device locale.
3. A verified, opted-in SUHAEB CHAT account is shown as available to message.
4. A number with no discoverable account gets an invitation action using Android's share sheet (SMS, WhatsApp, or another installed app).
5. Users can opt out of phone-number discovery without losing their account or existing chats.

## Identity and privacy requirements
- Email/password remains the sign-in method unless the user explicitly links and verifies a phone number.
- A phone number must be verified with Firebase Phone Auth before it can be attached to an account. A typed number is not proof of ownership.
- Store the E.164 phone number only in a private, owner-readable profile field; do not expose it in public profile documents or chat metadata.
- Do not upload the device address book wholesale or retain contacts on the server.
- Do not create a public collection of plaintext phone numbers or deterministic, client-computable phone hashes.
- A client-side hash directory is not safe: phone-number spaces are enumerable, so hashes do not prevent reverse lookup.
- Discovery must be opt-in and must not reveal whether arbitrary numbers have accounts to unauthenticated callers.
- Apply per-account/device/IP rate limits and Firebase App Check or equivalent attestation to discovery requests.
- Provide account deletion and phone unlinking behavior before launch.

## Required backend flow
A trusted server endpoint must:
1. Verify the Firebase ID token and App Check token.
2. Confirm the caller has a verified, linked phone number and has opted into discovery.
3. Normalize and HMAC the queried E.164 number using a secret held only by the server.
4. Enforce request limits and return only a minimal public result (display name, avatar URL if public, opaque user ID).
5. Never return a phone-number directory or distinguish blocked/hidden accounts.
6. Create or retrieve a direct chat idempotently, with membership derived from authenticated identities rather than client-supplied member lists.

## Firebase and cost constraint
The project currently targets Firebase Spark/free. Firebase Cloud Functions deployment commonly requires the Blaze billing plan, so do not implement or deploy a Cloud Function on the assumption it is free. Select and review a trusted free-tier backend option before enabling phone discovery. A Firestore document keyed by SHA-256(phone) is explicitly rejected because it enables enumeration.

## Current implementation status
- This document is a design and security gate, not an implemented feature.
- The current app authenticates with email/password and has no verified phone-linking flow.
- The current Firestore rules deny directory reads, chat creation, and message writes.
- The current build must not claim phone search or real messaging works.
- Next implementation sequence: phone-link UI and verification; trusted discovery endpoint and rate-limit tests; private profile schema/rules; idempotent direct-chat creation; message send/read rules and emulator tests; UI integration; opt-in and deletion tests.

## Acceptance tests
- Unverified or unlinked phone numbers cannot be registered as discoverable.
- A non-opted-in account is not discoverable.
- A random caller cannot enumerate phone numbers or user accounts.
- A matching verified number returns only public profile fields.
- A nonmatching number returns a generic no-match response.
- Repeated direct-chat creation for the same two UIDs returns the same chat.
- A third party cannot add itself to a chat or read/write another chat's messages.
- Denied contacts permission does not prevent manual number entry or invitations.
