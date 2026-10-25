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
 *     lastLat / lastLng - last known coordinates (Number)
 *     sosRadiusEnabled  - the user's own 5 km SOS opt-out (Boolean, default true);
 *                         an explicit false skips this user
 *
 * A SECOND, independent pass gives the OWNER of a lost-pet alert ONE push per
 * "found pet" sighting report. Fields used:
 *   found_reports/{reportId}
 *     alertId          - the linked lost_pet_alerts document id (String)
 *     petName          - the pet's name (String)
 *     location         - where the pet was seen (String)
 *     createdAt        - epoch millis (Number)
 *     ownerNotified    - set true by this script after the owner push (Boolean);
 *                        the app writes false on create
 *     ownerNotifiedAt  - added by this script (Timestamp)
 *   lost_pet_alerts/{alertId}.ownerId - the owner uid to notify (String)
 */

const admin = require('firebase-admin');

// How far back an alert still counts as "recent" (minutes).
const RECENT_WINDOW_MINUTES = parseInt(process.env.SOS_RECENT_WINDOW_MINUTES || '60', 10);
// How many of the newest alerts to inspect per run.
const MAX_ALERTS_PER_RUN = parseInt(process.env.SOS_MAX_ALERTS || '200', 10);
// Only notify users whose last known location is within this radius (km) of the
// alert. Defaults to 5 km; override with the SOS_RADIUS_KM repository variable.
const SOS_RADIUS_KM = parseFloat(process.env.SOS_RADIUS_KM || '5');
// FCM multicast accepts at most 500 tokens per call.
const FCM_BATCH_SIZE = 500;
// The app writes "date" in device-local time. The user base is in India, so
// legacy/unparsable string dates are interpreted as IST (UTC+05:30).
const IST_OFFSET_MS = 5.5 * 60 * 60 * 1000;

const EARTH_RADIUS_KM = 6371;

/** Great-circle distance between two coordinates, in kilometres. */
function haversineKm(lat1, lon1, lat2, lon2) {
  const toRad = (deg) => (deg * Math.PI) / 180;
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return EARTH_RADIUS_KM * c;
}

/**
 * True when a pair of coordinates is usable for a distance check: both are
 * finite numbers and they are not the 0,0 "never recorded" sentinel.
 */
function hasUsableCoords(lat, lng) {
  if (typeof lat !== 'number' || typeof lng !== 'number') return false;
  if (!isFinite(lat) || !isFinite(lng)) return false;
  return !(lat === 0 && lng === 0);
}

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
        holders.push({
          id: doc.id,
          ref: doc.ref,
          token: token.trim(),
          uid: doc.get('uid') || null,
          lastLat: doc.get('lastLat'),
          lastLng: doc.get('lastLng'),
          sosRadiusEnabled: doc.get('sosRadiusEnabled'),
        });
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

/**
 * True when the user still wants nearby SOS alerts. The field defaults to true,
 * so only an explicit `false` opts a user out.
 */
function wantsSosAlerts(holder) {
  return holder.sosRadiusEnabled !== false;
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

/** Sighting reports that have not yet been pushed to their owner. */
async function loadUnnotifiedFoundReports() {
  let docs = [];
  try {
    const snap = await db
      .collection('found_reports')
      .where('ownerNotified', '==', false)
      .limit(MAX_ALERTS_PER_RUN)
      .get();
    docs = snap.docs;
  } catch (e) {
    console.warn('Found-report query failed (' + e.message + '); scanning "found_reports" instead.');
    const snap = await db.collection('found_reports').limit(MAX_ALERTS_PER_RUN).get();
    docs = snap.docs.filter((d) => d.get('ownerNotified') !== true);
  }
  return docs.map((doc) => ({ id: doc.id, ref: doc.ref, data: doc.data() || {} }));
}

/** Mark a sighting report as owner-notified so it is never pushed twice. */
async function markReportOwnerNotified(ref) {
  await ref.update({
    ownerNotified: true,
    ownerNotifiedAt: admin.firestore.FieldValue.serverTimestamp(),
  });
}

/**
 * Give the OWNER of a lost-pet alert exactly one notification per sighting,
 * then mark that report ownerNotified so it is never sent again. This runs
 * independently of the general SOS fan-out, so it still reaches the owner for
 * an alert that was already marked `notified`.
 */
async function notifyOwnersOfSightings() {
  const reports = await loadUnnotifiedFoundReports();
  console.log('Unnotified sighting reports: ' + reports.length);
  if (reports.length === 0) return;

  let pushed = 0;
  let skipped = 0;
  let deadRemoved = 0;

  for (const report of reports) {
    try {
      const alertId = report.data.alertId;
      if (typeof alertId !== 'string' || alertId.trim() === '') {
        skipped += 1;
        continue;
      }

      const alertSnap = await db.collection('lost_pet_alerts').doc(alertId).get();
      if (!alertSnap.exists) {
        skipped += 1;
        continue;
      }
      const ownerId = alertSnap.get('ownerId');
      if (typeof ownerId !== 'string' || ownerId.trim() === '') {
        skipped += 1;
        continue;
      }

      const ownerSnap = await db.collection('users').doc(ownerId).get();
      if (!ownerSnap.exists) {
        skipped += 1;
        continue;
      }
      const token = ownerSnap.get('fcmToken');
      if (typeof token !== 'string' || token.trim() === '') {
        // No device to notify yet. Leave the report unmarked so the owner still
        // gets it once they have a token.
        skipped += 1;
        continue;
      }

      const petName =
        (report.data.petName && String(report.data.petName).trim()) ||
        (alertSnap.get('petName') && String(alertSnap.get('petName')).trim()) ||
        'Your pet';
      const where = report.data.location ? String(report.data.location).trim() : '';

      let messageId;
      try {
        messageId = await messaging.send({
          token: token.trim(),
          notification: {
            title: 'Someone may have found ' + petName,
            body: where
              ? 'Reported near ' + where + '. Tap to see the sighting.'
              : 'A new sighting was reported for your lost-pet alert. Tap to see it.',
          },
          data: {
            type: 'found_pet_sighting',
            alertId: alertId,
            reportId: report.id,
          },
          android: { priority: 'high' },
        });
      } catch (e) {
        const code = e && e.code;
        if (code && DEAD_TOKEN_CODES.has(code)) {
          try {
            await removeDeadToken(ownerSnap.ref);
            deadRemoved += 1;
          } catch (cleanupErr) {
            console.error('Could not clear dead owner token on ' + ownerSnap.ref.path + ': ' + cleanupErr.message);
          }
        } else {
          console.error('Owner push failed for report ' + report.id + ': ' + (e && e.message ? e.message : e));
        }
        continue;
      }

      // One push delivered: mark the report so it never sends again.
      await markReportOwnerNotified(report.ref);
      pushed += 1;
      console.log('Report ' + report.id + ': owner notified (messageId=' + messageId + ')');
    } catch (e) {
      console.error('Skipping report ' + report.id + ' after error: ' + (e && e.message ? e.message : e));
    }
  }

  console.log(
    'Owner sighting push summary: reportsConsidered=' + reports.length +
    ' pushed=' + pushed +
    ' skipped=' + skipped +
    ' deadTokensRemoved=' + deadRemoved
  );
}

async function main() {
  console.log('Lost-pet SOS sender starting.');

  // 1) Owner sighting pushes. Runs FIRST and independently of the general SOS
  // fan-out below, so a sighting on an already-notified alert still reaches the
  // owner. A failure here never stops the SOS run.
  try {
    await notifyOwnersOfSightings();
  } catch (e) {
    console.error('Owner sighting push pass failed: ' + (e && e.message ? e.message : e));
  }

  // 2) General SOS fan-out (unchanged behaviour).
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
      // Respect each user's own opt-out: a user who turned the 5 km alerts off
      // is skipped even when they are within the radius.
      const nonOwnerHolders = holders.filter((h) => !isOwner(h, ownerId) && wantsSosAlerts(h));

      // 5 km radius filter. An alert without usable coordinates keeps the old
      // "notify everyone" behaviour; an alert WITH coordinates skips users who
      // have no saved coordinates (they cannot be placed on the map).
      let targets;
      const alertHasCoords = hasUsableCoords(alert.data.lat, alert.data.lng);
      if (!alertHasCoords) {
        targets = nonOwnerHolders;
        console.log(
          'Alert ' + alert.id +
            ': no usable coordinates; notifying all ' + targets.length + ' non-owner holders.'
        );
      } else {
        targets = nonOwnerHolders.filter((h) => {
          if (!hasUsableCoords(h.lastLat, h.lastLng)) {
            console.log(
              'Alert ' + alert.id + ': skipping user ' + h.id + ' (no saved coordinates).'
            );
            return false;
          }
          const distanceKm = haversineKm(alert.data.lat, alert.data.lng, h.lastLat, h.lastLng);
          return distanceKm <= SOS_RADIUS_KM;
        });
        console.log(
          'Alert ' + alert.id +
            ': ' + targets.length + ' of ' + nonOwnerHolders.length +
            ' non-owner holders within ' + SOS_RADIUS_KM + ' km.'
        );
      }

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
