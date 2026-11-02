# WhatsApp hybrid integration — feasibility and implementation gates

## Product goal
Provide a WhatsApp section inside SUHAEB CHAT that can display and send messages for a WhatsApp number that the account owner is authorized to connect, while keeping SUHAEB CHAT's own messaging separate.

This is an integration with Meta's supported business messaging interfaces. It is **not** a clone of WhatsApp, a consumer-account companion device, or an implementation of WhatsApp's private multi-device protocol.

## Critical feasibility finding

### Supported path: WhatsApp Business App Coexistence
Meta's Coexistence onboarding can connect an eligible existing **WhatsApp Business App** number to the WhatsApp Business Platform / Cloud API while the owner continues to use the Business App. It is initiated through Meta Embedded Signup and the platform's approved onboarding flow.

Important distinctions:
- Coexistence is for eligible WhatsApp Business App numbers. It does not provide a supported way to connect an ordinary personal WhatsApp Messenger account to a third-party Android app.
- It is not the normal Linked Devices QR flow. Do not implement a fake QR scanner or ask users to scan a SUHAEB-generated QR in WhatsApp > Linked Devices.
- Eligibility and availability depend on Meta's onboarding configuration and the provider's enabled Coexistence capability. Do not promise every number can connect.
- The integration needs a Meta business app / business assets, Embedded Signup configuration, a supported provider/Tech Provider setup, webhooks, and server-side token handling. Exact Meta app review and business verification gates must be confirmed for the selected provider before implementation.
- Cloud API messages are subject to Meta's messaging policies, customer-service windows, approved templates where required, rate/quality limits, and current pricing. This is not an unrestricted personal WhatsApp account.
- App-side and API-side functionality is not identical. Groups and voice/video calling must not be promised through this integration. Historical sync, where enabled and consented to, has bounded coverage and is not a guarantee of a complete lifetime archive.

### Unsupported path
Do not connect ordinary WhatsApp Messenger personal accounts by reverse-engineering WhatsApp Web/mobile protocols, copying session tokens, scraping private databases, or automating unofficial clients. Do not ask users to share QR session credentials, OTPs, or WhatsApp account secrets with SUHAEB CHAT.

## Security and privacy architecture
1. Android launches Meta's official Embedded Signup / authorization experience; it never receives or stores a permanent Cloud API access token.
2. A trusted backend completes OAuth/token exchange, stores secrets in a server-side secret store, verifies webhook signatures, and validates webhook events.
3. Backend maps the connected WhatsApp Business Account and phone-number ID to the authenticated SUHAEB CHAT owner. A client cannot choose or overwrite another owner's mapping.
4. Android talks only to SUHAEB CHAT backend endpoints. Never call Cloud API with a permanent token embedded in the APK.
5. Store only message/contact data needed for the enabled experience, encrypt sensitive data at rest, enforce owner-scoped access, and provide disconnect/data deletion controls.
6. Explain clearly that WhatsApp Business Platform processing is different from personal end-to-end encrypted chats; do not claim end-to-end encryption for messages stored or processed by the connected business backend.
7. Keep the user's SUHAEB CHAT alias/anonymous profile separate from WhatsApp identity. The alias does not hide the connected WhatsApp Business phone number from WhatsApp recipients or Meta.
8. Do not enable integration until privacy notice, consent, retention, deletion, webhook authentication, rate limits, and abuse controls are reviewed.

## Implementation phases

### Phase 0 — owner eligibility and provider decision (blocking)
Confirm:
- Is the number currently registered in WhatsApp Business App, not ordinary WhatsApp Messenger?
- Is the owner willing to connect a business number to Meta's Business Platform through Embedded Signup?
- Does the owner have or can create a Meta Business Portfolio and the required business assets?
- Which supported Tech Provider/BSP will provide Embedded Signup and Coexistence, and what are its fees and supported countries?
- Is the owner comfortable with Meta Cloud API pricing and business messaging rules?
No number is connected and no data is imported during this phase.

### Phase 1 — integration foundation
- Create a Meta developer app and configure the required WhatsApp products and Embedded Signup only after Phase 0 passes.
- Provision a trusted backend and HTTPS webhook endpoint.
- Implement OAuth state/CSRF protection, token exchange, webhook signature verification, owner mapping, and audit logs.
- Keep all permanent credentials server-side.

### Phase 2 — Android connection experience
- Add Settings > Connected services > WhatsApp Business.
- Show eligibility explanation, consent, official connect action, connection state, and disconnect action.
- Never generate a fake QR. Display the Meta/provider-hosted onboarding flow only.
- Clearly distinguish WhatsApp Business connection from SUHAEB CHAT's own account and chats.

### Phase 3 — supported messaging
- Implement webhook ingestion, deduplication/idempotency, message status handling, supported one-to-one text/media, and API send operations.
- Enforce Meta's current template and customer-service window rules.
- Do not display groups, calls, or unsupported historical messages as if they were available.

### Phase 4 — security, tests, and release
- Add backend authorization and webhook tests, replay protection, rate limiting, deletion/disconnect tests, and Android integration tests.
- Review privacy policy and user consent.
- Build a test APK only after the integration can be exercised with an eligible test business number.
- No Firebase Firestore production rules deployment, Meta production activation, merge, or release signing is implied by this design document.

## Current implementation status
- SUHAEB CHAT currently has Firebase email/password authentication and a basic read-only chat-list shell.
- There is no WhatsApp connection UI, Meta Embedded Signup, Cloud API backend, webhook receiver, message sync, or WhatsApp send implementation.
- Firebase Firestore rules currently deny chat writes and nested message access; do not weaken them to implement WhatsApp.
- This document records the feasibility boundary and implementation plan. It does not claim that WhatsApp integration is working.

## Acceptance criteria
- A personal WhatsApp Messenger number is clearly marked unsupported for Coexistence.
- An ineligible WhatsApp Business number receives a clear, non-destructive explanation.
- No QR/session token/OTP is collected by SUHAEB CHAT outside Meta's official flow.
- No permanent Meta token exists in the Android app, repository, CI logs, or Firestore client-readable data.
- A connected owner can see only the WhatsApp business assets and messages mapped to that authenticated owner.
- Disconnect revokes/removes the mapping and stops further message access.
- SUHAEB CHAT's own identity, alias, chats, and privacy controls remain independent.
