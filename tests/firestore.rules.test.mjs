import { readFile } from "node:fs/promises";
import { before, after, beforeEach, test } from "node:test";
import {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds
} from "@firebase/rules-unit-testing";
import {
  collection,
  doc,
  getDoc,
  getDocs,
  query,
  serverTimestamp,
  setDoc,
  updateDoc,
  where
} from "firebase/firestore";

const projectId = "demo-suhaeb-chat";
let env;

before(async () => {
  const rules = await readFile(new URL("../firestore.rules", import.meta.url), "utf8");
  env = await initializeTestEnvironment({
    projectId,
    firestore: { rules, host: "127.0.0.1", port: 8080 }
  });
});

after(async () => {
  await env.cleanup();
});

beforeEach(async () => {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, "chats", "chat-alice-bob"), {
      memberIds: ["alice", "bob"],
      createdBy: "alice",
      type: "direct"
    });
    await setDoc(doc(db, "chats", "chat-carol-dave"), {
      memberIds: ["carol", "dave"],
      createdBy: "carol",
      type: "direct"
    });
    await setDoc(doc(db, "chats", "chat-alice-bob", "messages", "message-1"), {
      senderId: "alice",
      text: "hello"
    });
  });
});

const verified = (uid) => env.authenticatedContext(uid, {
  email: uid + "@example.test",
  email_verified: true
}).firestore();

const unverified = (uid) => env.authenticatedContext(uid, {
  email: uid + "@example.test",
  email_verified: false
}).firestore();

test("unauthenticated users cannot read or create profiles", async () => {
  const db = env.unauthenticatedContext().firestore();
  await assertFails(getDoc(doc(db, "users", "alice")));
  await assertFails(setDoc(doc(db, "users", "alice"), {
    uid: "alice",
    displayName: "Alice",
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  }));
});

test("unverified users cannot access profile or chat data", async () => {
  const db = unverified("alice");
  await assertFails(getDoc(doc(db, "users", "alice")));
  await assertFails(getDoc(doc(db, "chats", "chat-alice-bob")));
});

test("verified users can create and read only their own valid profile", async () => {
  const db = verified("alice");
  await assertSucceeds(setDoc(doc(db, "users", "alice"), {
    uid: "alice",
    displayName: "Alice",
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  }));
  await assertSucceeds(getDoc(doc(db, "users", "alice")));
  await assertFails(getDoc(doc(db, "users", "bob")));
  await assertFails(getDocs(collection(db, "users")));
});

test("profile updates cannot change uid, createdAt, add fields, or delete", async () => {
  const db = verified("alice");
  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "users", "alice"), {
      uid: "alice",
      displayName: "Alice",
      createdAt: new Date("2025-01-01T00:00:00Z"),
      updatedAt: new Date("2025-01-01T00:00:00Z")
    });
  });

  await assertSucceeds(updateDoc(doc(db, "users", "alice"), {
    displayName: "Alice Updated",
    updatedAt: serverTimestamp()
  }));
  await assertFails(updateDoc(doc(db, "users", "alice"), {
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  }));
  await assertFails(updateDoc(doc(db, "users", "alice"), {
    uid: "bob",
    updatedAt: serverTimestamp()
  }));
  await assertFails(updateDoc(doc(db, "users", "alice"), {
    isAdmin: true,
    updatedAt: serverTimestamp()
  }));
});

test("verified chat members can read their chats", async () => {
  const db = verified("alice");
  await assertSucceeds(getDoc(doc(db, "chats", "chat-alice-bob")));
  const ownChats = query(
    collection(db, "chats"),
    where("memberIds", "array-contains", "alice")
  );
  await assertSucceeds(getDocs(ownChats));
});

test("users cannot read chats where they are not members or run broad chat queries", async () => {
  const db = verified("alice");
  await assertFails(getDoc(doc(db, "chats", "chat-carol-dave")));
  await assertFails(getDocs(collection(db, "chats")));
});

test("chat creation and nested message reads/writes remain disabled", async () => {
  const db = verified("alice");
  await assertFails(setDoc(doc(db, "chats", "new-chat"), {
    memberIds: ["alice", "bob"],
    createdBy: "alice",
    type: "direct"
  }));
  await assertFails(getDoc(doc(db, "chats", "chat-alice-bob", "messages", "message-1")));
  await assertFails(setDoc(doc(db, "chats", "chat-alice-bob", "messages", "message-2"), {
    senderId: "alice",
    text: "not enabled yet"
  }));
});

test("unknown collections are denied by default", async () => {
  const db = verified("alice");
  await assertFails(getDoc(doc(db, "admin", "config")));
  await assertFails(setDoc(doc(db, "privateData", "secret"), { value: "x" }));
});
