import * as admin from "firebase-admin";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { logger } from "firebase-functions/v2";

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

interface NotificationPayload {
  title: string;
  body: string;
  type: "ASSIGNMENT_REMINDER" | "TASK_REMINDER" | "STUDY_REMINDER";
  referenceId: string;
}

/**
 * Helper to send an FCM notification to all registered devices for a user,
 * automatically cleaning up invalid/expired registration tokens.
 */
async function sendNotificationToUser(
  uid: string,
  payload: NotificationPayload
): Promise<number> {
  const tokensSnapshot = await db
    .collection("users")
    .doc(uid)
    .collection("fcmTokens")
    .get();

  if (tokensSnapshot.empty) {
    return 0;
  }

  const tokens = tokensSnapshot.docs
    .map((doc) => doc.get("token") as string | undefined)
    .filter((token): token is string => Boolean(token));

  if (tokens.length === 0) {
    return 0;
  }

  const response = await messaging.sendEachForMulticast({
    tokens,
    notification: {
      title: payload.title,
      body: payload.body,
    },
    data: {
      type: payload.type,
      referenceId: payload.referenceId,
    },
    android: {
      priority: "high",
      notification: {
        channelId:
          payload.type === "ASSIGNMENT_REMINDER"
            ? "campusflow_assignments"
            : payload.type === "TASK_REMINDER"
              ? "campusflow_tasks"
              : "campusflow_study_planner",
      },
    },
  });

  // Remove stale tokens
  const cleanupPromises: Promise<FirebaseFirestore.WriteResult>[] = [];
  response.responses.forEach((res, idx) => {
    if (!res.success) {
      const code = res.error?.code;
      if (
        code === "messaging/invalid-registration-token" ||
        code === "messaging/registration-token-not-registered"
      ) {
        const docToDelete = tokensSnapshot.docs[idx];
        if (docToDelete) {
          cleanupPromises.push(docToDelete.ref.delete());
        }
      }
    }
  });
  await Promise.all(cleanupPromises);

  return response.successCount;
}

/**
 * Scheduled function running every 60 minutes to check upcoming assignments,
 * pending tasks, and study sessions, and push FCM reminders.
 */
export const sendScheduledReminders = onSchedule(
  {
    schedule: "every 60 minutes",
    region: "asia-south1",
  },
  async () => {
    const nowMillis = Date.now();
    const in24HoursMillis = nowMillis + 24 * 60 * 60 * 1000;
    const in1HourMillis = nowMillis + 60 * 60 * 1000;

    const usersSnapshot = await db.collection("users").get();
    let totalSent = 0;

    for (const userDoc of usersSnapshot.docs) {
      const uid = userDoc.id;

      // Check user notification preferences
      const prefsDoc = await db
        .collection("users")
        .doc(uid)
        .collection("settings")
        .doc("preferences")
        .get();

      const notificationsEnabled =
        prefsDoc.exists && prefsDoc.get("notificationsEnabled") !== undefined
          ? Boolean(prefsDoc.get("notificationsEnabled"))
          : true;

      if (!notificationsEnabled) {
        continue;
      }

      // 1. Upcoming assignments due within 24 hours
      const assignmentsSnap = await db
        .collection("users")
        .doc(uid)
        .collection("assignments")
        .where("isCompleted", "==", false)
        .where("dueDateMillis", ">=", nowMillis)
        .where("dueDateMillis", "<=", in24HoursMillis)
        .get();

      for (const doc of assignmentsSnap.docs) {
        if (doc.get("reminderSentAtMillis")) continue;
        const title = (doc.get("title") as string) || "Assignment";
        const sent = await sendNotificationToUser(uid, {
          title: "Upcoming Assignment Deadline",
          body: `"${title}" is due within the next 24 hours.`,
          type: "ASSIGNMENT_REMINDER",
          referenceId: doc.id,
        });
        if (sent > 0) {
          totalSent += sent;
          await doc.ref.update({ reminderSentAtMillis: nowMillis });
        }
      }

      // 2. Pending tasks due within 24 hours
      const tasksSnap = await db
        .collection("users")
        .doc(uid)
        .collection("tasks")
        .where("dueDateMillis", ">=", nowMillis)
        .where("dueDateMillis", "<=", in24HoursMillis)
        .get();

      for (const doc of tasksSnap.docs) {
        const status = doc.get("status") as string | undefined;
        if (status === "COMPLETED" || doc.get("reminderSentAtMillis")) continue;
        const title = (doc.get("title") as string) || "Task";
        const sent = await sendNotificationToUser(uid, {
          title: "Task Due Soon",
          body: `Don't forget: "${title}" is due soon.`,
          type: "TASK_REMINDER",
          referenceId: doc.id,
        });
        if (sent > 0) {
          totalSent += sent;
          await doc.ref.update({ reminderSentAtMillis: nowMillis });
        }
      }

      // 3. Study sessions starting within 1 hour
      const sessionsSnap = await db
        .collection("users")
        .doc(uid)
        .collection("studySessions")
        .where("startTimeMillis", ">=", nowMillis)
        .where("startTimeMillis", "<=", in1HourMillis)
        .get();

      for (const doc of sessionsSnap.docs) {
        const isCompleted = Boolean(doc.get("isCompleted"));
        if (isCompleted || doc.get("reminderSentAtMillis")) continue;
        const duration = Number(doc.get("durationMinutes") || 60);
        const sent = await sendNotificationToUser(uid, {
          title: "Study Session Starting Soon",
          body: `Your ${duration}-minute study session starts within an hour.`,
          type: "STUDY_REMINDER",
          referenceId: doc.id,
        });
        if (sent > 0) {
          totalSent += sent;
          await doc.ref.update({ reminderSentAtMillis: nowMillis });
        }
      }
    }

    logger.info(`sendScheduledReminders completed. Sent ${totalSent} notifications.`);
  }
);

/**
 * Callable endpoint allowing the authenticated Android client to register/refresh
 * its FCM device token securely.
 */
export const registerFcmToken = onCall(
  { region: "asia-south1" },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "Authentication is required.");
    }

    const token = request.data?.token;
    if (typeof token !== "string" || token.trim().length === 0 || token.length > 512) {
      throw new HttpsError("invalid-argument", "A valid FCM token is required.");
    }

    const uid = request.auth.uid;
    const tokenHash = Buffer.from(token).toString("base64url").slice(0, 64);

    await db
      .collection("users")
      .doc(uid)
      .collection("fcmTokens")
      .doc(tokenHash)
      .set(
        {
          ownerUid: uid,
          token,
          platform: "android",
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        },
        { merge: true }
      );

    return { registered: true };
  }
);
