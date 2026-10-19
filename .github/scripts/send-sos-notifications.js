'use strict';

/**
 * Free FCM sender for the Wagmiya (Petpulse) app.
 *
 * Runs on a GitHub Actions schedule (every 5 minutes). It reads recent
 * lost-pet SOS alerts from Firestore that have not been notified yet and
 * sends a push notification to every OTHER user who has a saved FCM token,
 * using the Firebase Admin SDK (FCM HTTP v1 under the hood).
 *
 * Authentication comes from the GOOGLE_APPLICATION_CREDENTIALS environment
 * variable, which the workflow points at a temporary copy of the
 * FIREBASE_SERVICE_ACCOUNT repository secret. No key is stored in this file.
 *
 * Firestore fields used (matching what the app writes):
 *   lost_pet_alerts/{alertId}
 *     ownerId   - the alert owner's uid (String)  -> never notified
 *     petName   - the pet's name (String)
 *     date      - "yyyy-MM-dd HH:mm" local time (String) -> recency + ordering
 *     notified  - added by this script (Boolean)
 *     notifiedAt- added by this script (Timestamp)
 *   users/{uid}
 *     fcmToken  - the device token (String); deleted here when it goes dead
 */

const admin = require('firebase-admin');

// How far back an alert still counts as "recent" (minutes).
const RECENT_WINDOW_MINUTES = parseInt(process.env.SOS_RECENT_WINDOW_MINUTES || '60', 10);
// How many of the newest alerts to inspect per run.
const MAX_ALERTS_PER_RUN = parseInt(process.env.SOS_MAX_ALERTS || '200', 10);
// FCM multicast accepts at most 500 tokens per call.
const FCM_BATCH_SIZE = 500;
// The app writes "date" in device-local time. The user base is in India, so
// legacy/unparsable string dates are interpreted as IST (UTC+05:30).
const IST_OFFSET_MS = 5.5 * 60 * 60 * 1000;

// FCM error codes that mean the token is dead and should be removed.
const DEAD_TOKEN_CODES = new Set([
  'messaging/registration-token-not-registered',
  'messaging/invalid-registration-token',
  'messaging/invalid-recipient',
  'messaging/invalid-argument',
]);

admin.initializeApp({
  credential: admin.credential.applicationDefault(),
});

const db = admin.firestore();
const messaging = admin.messaging();

/** Parse an alert's "date" value into a JS Date (String, Timestamp or Date). */
function parseAlertDate(value) {
  if (!value) return null;
  if (typeof value === 'string') {
    const m = value.match(/^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})/);
    if (m) {
      const y = +m[1];
      const mo = +m[2];
      const d = +m[3];
      const h = +m[4];
      const mi = +m[5];
      return new Date(Date.UTC(y, mo - 1, d, h, mi) - IST_OFFSET_MS);
    }
    const parsed = new Date(value);
    return isNaN(parsed.getTime()) ? null : parsed;
  }
  if (typeof value.toDate === 'function') return value.toDate(); // Firestore Timestamp
  if (value instanceof Date) return value;
  return null;
}

/** Recent alerts that have not been notified yet, newest first. */
async function loadRecentUnnotifiedAlerts() {
  const snap = await db
    .collection('lost_pet_alerts')
    .orderBy('date', 'desc')
    .limit(MAX_ALERTS_PER_RUN)
    .get();

  const cutoff = Date.now() - RECENT_WINDOW_MINUTES * 60 * 1000;
  const alerts = [];
  snap.forEach((doc) => {
    const data = doc.data() || {};
    if (data.notified === true) return;
    const when = parseAlertDate(data.date);
    if (when && when.getTime() < cutoff) return; // too old
    alerts.push({ id: doc.id, ref: doc.ref, data });
  });
  return alerts;
}

/** Every user document that carries a non-empty fcmToken. */
async function loadTokenHolders() {
  const holders = [];
  const collect = (snap) => {
    snap.forEach((doc) => {
      const token = doc.get('fcmToken');
      if (typeof token === 'string' && token.trim() !== '') {
        holders.push({ id: doc.id, ref: doc.ref, token: token.trim(), uid: doc.get('uid') || null });
      }
    });
  };
  try {
    const snap = await db
      .collection('users')
      .where('fcmToken', '!=', '')
      .orderBy('fcmToken')
      .get();
    collect(snap);
  } catch (e) {
    console.warn('Targeted token query failed (' + e.message + '); scanning "users" instead.');
    collect(await db.collection('users').get());
  }
  return holders;
}

/** True when a user document belongs to the alert's own owner. */
function isOwner(holder, ownerId) {
  if (!ownerId) return false;
  return holder.id === ownerId || holder.uid === ownerId;
}

/** Send one alert to the given holders; return counts and any dead tokens. */
async function sendToHolders(alert, holders) {
  const petName = (alert.data.petName && String(alert.data.petName).trim()) || 'A pet';
  const message = {
    notification: {
      title: 'Lost pet alert near you',
      body: petName + ' is missing. Tap to see details.',
    },
    data: {
      type: 'lost_pet_alert',
      alertId: alert.id,
    },
    android: { priority: 'high' },
  };

  let sent = 0;
  let failed = 0;
  const deadRefs = [];

  for (let i = 0; i < holders.length; i += FCM_BATCH_SIZE) {
    const batch = holders.slice(i, i + FCM_BATCH_SIZE);
    const tokens = batch.map((h) => h.token);
    let resp;
    try {
      resp = await messaging.sendEachForMulticast(Object.assign({ tokens: tokens }, message));
    } catch (e) {
      failed += batch.length;
      console.error('Multicast batch failed for alert ' + alert.id + ': ' + e.message);
      continue;
    }
    resp.responses.forEach((r, idx) => {
      if (r.success) {
        sent += 1;
        return;
      }
      failed += 1;
      const code = r.error && r.error.code;
      if (code && DEAD_TOKEN_CODES.has(code)) deadRefs.push(batch[idx].ref);
    });
  }

  return { sent: sent, failed: failed, deadRefs: deadRefs };
}

/** Mark an alert so the next run never sends it again. */
async function markNotified(ref) {
  await ref.update({
    notified: true,
    notifiedAt: admin.firestore.FieldValue.serverTimestamp(),
  });
}

/** Remove the fcmToken field from a user document (dead token cleanup). */
async function removeDeadToken(ref) {
  await ref.update({ fcmToken: admin.firestore.FieldValue.delete() });
}

async function main() {
  console.log('Lost-pet SOS sender starting.');

  const alerts = await loadRecentUnnotifiedAlerts();
  console.log('Recent unnotified alerts: ' + alerts.length);
  if (alerts.length === 0) {
    console.log('Nothing to do.');
    return;
  }

  const holders = await loadTokenHolders();
  console.log('Users with an FCM token: ' + holders.length);

  let totalSent = 0;
  let totalFailed = 0;
  let totalDeadRemoved = 0;
  let notified = 0;

  for (const alert of alerts) {
    try {
      const ownerId = alert.data.ownerId;
      const targets = holders.filter((h) => !isOwner(h, ownerId));

      if (targets.length === 0) {
        console.log('Alert ' + alert.id + ': no recipients; marking notified.');
        await markNotified(alert.ref);
        notified += 1;
        continue;
      }

      const result = await sendToHolders(alert, targets);
      totalSent += result.sent;
      totalFailed += result.failed;

      // Drop tokens FCM reported as dead so they are not retried forever.
      const seen = new Set();
      for (const ref of result.deadRefs) {
        if (seen.has(ref.path)) continue;
        seen.add(ref.path);
        try {
          await removeDeadToken(ref);
          totalDeadRemoved += 1;
        } catch (e) {
          console.error('Could not clear dead token on ' + ref.path + ': ' + e.message);
        }
      }

      await markNotified(alert.ref);
      notified += 1;

      console.log(
        'Alert ' + alert.id +
        ': recipients=' + targets.length +
        ' sent=' + result.sent +
        ' failed=' + result.failed +
        ' deadRemoved=' + seen.size
      );
    } catch (e) {
      // One bad document must never stop the run.
      console.error('Skipping alert ' + alert.id + ' after error: ' + e.message);
    }
  }

  console.log(
    'Summary: alertsConsidered=' + alerts.length +
    ' alertsNotified=' + notified +
    ' tokensTotal=' + holders.length +
    ' sent=' + totalSent +
    ' failed=' + totalFailed +
    ' deadTokensRemoved=' + totalDeadRemoved
  );
}

main()
  .then(() => {
    console.log('Lost-pet SOS sender finished.');
  })
  .catch((e) => {
    console.error('Fatal error in SOS sender: ' + (e && e.stack ? e.stack : e));
    process.exitCode = 1;
  });
