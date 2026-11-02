# Cloud-First Connectivity and Message Delivery

## Non-negotiable product requirement
SUHAEB TX must not depend on the sender's phone being online or running. The backend is hosted in the cloud. A user's phone is a client, never the server or the source of truth for other users' conversations.

## Expected behavior
1. When a sender is online, the app writes a message to the cloud backend. The UI distinguishes a local pending message from a message accepted by the server.
2. Once accepted by the backend, the message remains available to authorized conversation members even if the sender closes the app, signs out, powers off the phone, or loses connectivity.
3. If a recipient is offline, the message remains stored in the backend. When the recipient reconnects, the app synchronizes conversation history and displays the message.
4. Push notifications may alert an offline/background recipient, but notification delivery is best-effort and must never be the only way messages are stored or synchronized.
5. If the sender composes a message while offline, keep it in a local outgoing queue and show it as pending. Retry safely after reconnection; use a stable client-generated message ID/idempotency key to prevent duplicates.
6. Delivery/read states must be truthful. A local pending message is not "sent"; server acceptance is not "delivered to device"; delivered is not "read". Read receipts must respect the recipient's privacy setting.

## Proposed Firebase architecture
- Firebase Authentication: account identity and verified sign-in.
- Cloud Firestore: authoritative conversation and message records, membership, privacy preferences, and synchronization.
- Firebase Cloud Messaging (FCM): best-effort push notifications; never the message database.
- Cloud Storage: private media objects, with authenticated membership checks and short-lived/controlled access.
- Trusted server-side code (for example Cloud Functions or another trusted backend): operations that must not trust client-supplied membership, roles, counters, notification fan-out, and abuse/rate-limit enforcement.

## Security and consistency requirements
- Only authenticated conversation members may read conversation messages.
- A client may create a message only as its authenticated user and only in a conversation where membership is already established by trusted logic.
- Clients must not add arbitrary participants, grant roles, or write another user's delivery/read state.
- Use stable message IDs and server timestamps; validate payload size/type and rate limits.
- Use pagination and bounded listeners rather than loading entire histories.
- Local cache is device-local convenience, not a substitute for cloud persistence or authorization.
- Do not promise end-to-end encryption until a reviewed protocol and key-management design are implemented and tested.

## Availability and cost truth
Cloud hosting means other users can continue using the service while one user's phone is offline. It does not mean unlimited capacity, guaranteed zero downtime, or unlimited free usage. Firebase quotas, billing-plan limits, storage, bandwidth, and any trusted server functions must be checked before launch. Keep the project on the no-cost plan where feasible, monitor quotas, and never enable paid billing without the owner's explicit approval.

## Acceptance tests
- Sender sends a message; server acknowledges it; sender force-stops app and turns off phone. Recipient still receives/syncs the message.
- Recipient is offline when a message is sent; recipient reconnects later and receives the stored message without sender reconnecting.
- Sender writes while offline; message remains visibly pending and is sent once after reconnection, with no duplicate.
- Push notification is disabled or delayed; message still appears after app synchronization.
- Non-members cannot read messages or inject messages into a conversation.
- UI never marks a pending message as server-sent, delivered, or read.
