# SUHAEB TX — Firebase database contract

This is the initial Firestore/Storage contract for the Android app. It is a deployable rules baseline, not a provisioned Firebase project. The app must be wired to these exact paths before live features are enabled.

## Collections
- `users/{uid}`: private account profile; owner-only reads. Fields: uid, displayName, username, photoUrl, bio, createdAt, updatedAt, lastSeenAt, online, privacy, fcmTokens.
- `usernames/{normalizedUsername}`: immutable username reservation; uid, createdAt. Create through a trusted transaction/service to prevent races and impersonation.
- `chats/{chatId}`: memberIds, createdBy, createdAt, type (direct/group), optional title/photoUrl, lastMessage, lastMessageAt, updatedAt.
- `chats/{chatId}/messages/{messageId}`: senderId, type (text/image/video/audio/file), text or mediaUrl, fileName/fileSize, createdAt, optional replyToId/editedAt/deletedForEveryone/deliveredAt/readBy.
- `chats/{chatId}/typing/{uid}`: typing, updatedAt.
- `blocks/{uid}/blocked/{blockedUid}`: createdAt.
- `reports/{reportId}`: reporterId, targetUid, optional chatId/messageId, reason/details/createdAt.
- `statuses/{statusId}`: ownerId, mediaUrl, caption, createdAt, expiresAt, visibleTo.

## Storage paths
- `profile_images/{uid}/{fileName}`: owner writes; authenticated reads; image-only, max 5 MiB.
- `chat_media/{chatId}/{uid}/{fileName}`: chat members read; owner uploads; media/PDF/octet-stream, max 50 MiB.

## Required backend work before launch
1. Enable Firebase Authentication (email/password initially) and require verified email.
2. Register Android package `com.suhaeb.tx`; place generated `google-services.json` locally at `app/google-services.json`. Never commit it to this public repository.
3. Deploy Firestore and Storage rules with Firebase CLI after checking schema against the app's actual queries.
4. Add trusted server operations for username reservation, group membership changes, abuse throttling, moderation, and status expiry. Clients must not add arbitrary members or set admin roles.
5. Add App Check, FCM, retention/deletion flows, rate limits, and security tests.
6. Add indexes only when actual app queries require them; the empty index file is intentional at this starter stage.

## Limitations
These files do not create a Firebase project or provision a live database. Deployment requires the project owner's Firebase account and project ID. Rules do not provide end-to-end encryption. Voice/video calling needs a separate signaling/media service and is not enabled here.
