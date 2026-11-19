const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const DATABASE_ID = "ai-studio-android-midadia-e7a0be23-5dc1-4e3e-a875-717c086eb62f";
const ALICE_UID = "aliceUid123";
const BOB_UID = "bobUid456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write profiles or documents", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore(DATABASE_ID);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("documents").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("documents").doc("doc1").set({
      userId: ALICE_UID,
      title: "Hack",
      markdown: "# Hi",
      direction: "ltr",
      orientation: "portrait",
      margins: "normal",
      fontFamily: "Cairo",
      fontSizeSp: 14,
      watermark: "",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("UserProfile: owner create, partial update, credit cap, and PII isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore(DATABASE_ID);
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      credits: 25,
      maxCredits: 25,
      preferredLanguage: "en",
      darkMode: false,
      favoriteTemplateIds: [],
      createdAt: now,
      updatedAt: now,
    })
  );

  // Partial update on credits & favorites succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      credits: 24,
      favoriteTemplateIds: ["graduation"],
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cross-user read & write rejected (PII isolation)
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({ credits: 999, updatedAt: new Date(Date.now() - 500) })
  );

  // Ghost field & future timestamp rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({ isAdmin: true, updatedAt: new Date(Date.now() - 500) })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({ credits: 20, updatedAt: new Date(Date.now() + 60000) })
  );
});

test("SavedDocument: Kotlin-mirrored create, update, list, delete, and rejection of @DocumentId id field", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore(DATABASE_ID);
  const created = new Date(Date.now() - 10000);

  // Valid create (no 'id' field in body)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("documents").doc("doc_1").set({
      userId: ALICE_UID,
      title: "Research Paper",
      markdown: "# Quantum Computing\n\n$$E = mc^2$$",
      direction: "ltr",
      orientation: "portrait",
      margins: "normal",
      fontFamily: "Cairo",
      fontSizeSp: 14,
      watermark: "",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Owner list & partial update succeed
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("documents").get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("documents").doc("doc_1").update({
      title: "Updated Paper",
      markdown: "# Updated\n\n$$a^2 + b^2 = c^2$$",
      direction: "rtl",
      orientation: "landscape",
      margins: "narrow",
      fontFamily: "Amiri",
      fontSizeSp: 16,
      watermark: "DRAFT",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject including @DocumentId `id` field in payload
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("documents").doc("doc_2").set({
      id: "doc_2",
      userId: ALICE_UID,
      title: "Bad Doc",
      markdown: "text",
      direction: "ltr",
      orientation: "portrait",
      margins: "normal",
      fontFamily: "Cairo",
      fontSizeSp: 14,
      watermark: "",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject immutable createdAt overwrite
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("documents").doc("doc_1").update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Cross-user isolation
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("documents").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("documents").doc("doc_1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("documents").doc("doc_1").delete());

  // Owner delete succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("documents").doc("doc_1").delete()
  );
});
