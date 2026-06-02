const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

const TOPIC_PREFIX = "/topics/";

/**
 * Callable: send data-only FCM to a topic (HTTP v1 via Admin SDK).
 *
 * Client payload: { target, type, title, body }
 * - target: "/topics/{name}" (legacy format) or "{name}"
 * - type: MESSAGE | DUTY | STUDY_UPDATE | GENERAL
 */
exports.sendNotification = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Giriş gerekli");
  }

  const uid = request.auth.uid;
  const { target, type, title, body } = request.data ?? {};

  if (!target || !type || !title || !body) {
    throw new HttpsError("invalid-argument", "target, type, title ve body zorunludur");
  }

  if (typeof target !== "string" || typeof type !== "string" ||
      typeof title !== "string" || typeof body !== "string") {
    throw new HttpsError("invalid-argument", "Geçersiz alan türü");
  }

  const topic = normalizeTopic(target.trim());
  if (!topic) {
    throw new HttpsError("invalid-argument", "Geçersiz hedef");
  }

  const db = getFirestore();
  const userSnap = await db.collection("User").doc(uid).get();
  if (!userSnap.exists) {
    throw new HttpsError("permission-denied", "Kullanıcı bulunamadı");
  }

  const user = userSnap.data();
  if (user.personType !== "Teacher") {
    throw new HttpsError("permission-denied", "Yalnızca öğretmenler bildirim gönderebilir");
  }

  const kurumKodu = String(user.kurumKodu ?? "");
  await assertTopicAllowed(db, uid, kurumKodu, topic);

  const allowedTypes = ["MESSAGE", "DUTY", "STUDY_UPDATE", "GENERAL"];
  if (!allowedTypes.includes(type)) {
    throw new HttpsError("invalid-argument", "Geçersiz bildirim türü");
  }

  const message = {
    topic,
    data: {
      type,
      title,
      body,
    },
    android: {
      priority: "high",
    },
  };

  try {
    await getMessaging().send(message);
  } catch (err) {
    console.error("FCM send failed:", err);
    throw new HttpsError("internal", err.message || "Bildirim gönderilemedi");
  }

  return { success: true };
});

function normalizeTopic(target) {
  if (target.startsWith(TOPIC_PREFIX)) {
    return target.slice(TOPIC_PREFIX.length);
  }
  return target;
}

async function assertTopicAllowed(db, teacherUid, kurumKodu, topic) {
  if (topic === teacherUid) {
    return;
  }

  if (kurumKodu && topic === kurumKodu) {
    return;
  }

  if (!kurumKodu) {
    throw new HttpsError("permission-denied", "Kurum kodu tanımlı değil");
  }

  const studentSnap = await db
      .collection("School")
      .doc(kurumKodu)
      .collection("Student")
      .doc(topic)
      .get();

  if (!studentSnap.exists) {
    throw new HttpsError("permission-denied", "Bu hedefe bildirim gönderme yetkiniz yok");
  }

  const studentTeacher = studentSnap.data()?.teacher;
  if (studentTeacher !== teacherUid) {
    throw new HttpsError("permission-denied", "Bu öğrenciye bildirim gönderme yetkiniz yok");
  }
}
