# Free FCM Sender - Setup (Wagmiya / Petpulse)

This adds a scheduled GitHub Actions job that sends lost-pet SOS push
notifications through Firebase Cloud Messaging (FCM). It needs no paid server:
GitHub runs the job for free on a schedule.

How it fits together:

- `.github/workflows/send-sos-notifications.yml` - the scheduled job.
- `.github/scripts/send-sos-notifications.js` - the sender script it runs.

Neither file contains your Firebase key. The key is read at run time from a
repository secret and deleted again at the end of the job.

---

## 1. Create the `FIREBASE_SERVICE_ACCOUNT` repository secret

1. In the Firebase console, open your project (this app uses project
   `petz-58515`) -> Project settings -> Service accounts.
2. Click **Generate new private key**, confirm, and download the JSON file.
   Keep this file safe - it is a full-access credential.
3. Open the downloaded JSON in a text editor and copy the **entire** contents,
   including the outer `{` and `}` braces.
4. In GitHub, go to your repository -> **Settings** -> **Secrets and
   variables** -> **Actions** -> **New repository secret**.
5. Name it exactly:

   ```
   FIREBASE_SERVICE_ACCOUNT
   ```

6. Paste the whole JSON into the **Secret** box and click **Add secret**.

The workflow reads this as `secrets.FIREBASE_SERVICE_ACCOUNT`. It is never
written into any file in the repository and never printed in the logs.

## 2. Keep the repository private

Set the repository to **Private** (Settings -> General -> Danger Zone ->
Change visibility). The secret is protected by GitHub either way, but the app
code should stay private.

## 3. What the workflow does and when

- It runs automatically **every 5 minutes**.
- You can also run it on demand: **Actions** tab ->
  **Send Lost Pet SOS Notifications** -> **Run workflow**.

Each run:

1. Reads recent `lost_pet_alerts` documents that do not yet have `notified: true`.
2. Collects the FCM tokens of all **other** users (the alert's own owner is
   never notified).
3. Sends a push notification per token with the alert id attached.
4. Removes the `fcmToken` field from users whose token FCM reports as dead.
5. Marks each alert `notified: true` (plus a `notifiedAt` timestamp) so it is
   never sent twice.

## 4. Important: scheduled jobs are not instant

- A GitHub Actions schedule is **best-effort**, not real time. The job is
  queued roughly every 5 minutes, but during busy periods it can start a few
  minutes late. Expect a short delay, not an instant alert.
- **GitHub automatically disables scheduled workflows after 60 days with no
  repository activity.** If the app goes quiet for two months, the schedule
  stops. To keep it alive, push any small commit (or use the repo) at least
  once every 60 days, and re-enable the workflow from the Actions tab
  (**Enable workflow**) if it was turned off. A failed run does not count as
  activity.

## 5. How to check it worked

1. **Actions** tab -> **Send Lost Pet SOS Notifications** -> open the latest
   run. A green tick means it completed.
2. Open the run -> **Send lost-pet SOS notifications** step. You should see a
   line like:

   ```
   Summary: alertsConsidered=2 alertsNotified=2 tokensTotal=37 sent=74 failed=0 deadTokensRemoved=1
   ```

   No tokens, keys or personal data are ever logged - only counts.
3. In the Firebase console, open a `lost_pet_alerts` document you expect to be
   notified: it should now have `notified: true` and a `notifiedAt` timestamp.
4. If a phone should have received the push, confirm the app has saved an
   `fcmToken` on that user's `users/{uid}` document and that notifications are
   allowed on the device.

If the workflow fails at the "Write service account to a temporary file" step,
the `FIREBASE_SERVICE_ACCOUNT` secret is missing or empty - repeat step 1.

## 6. Notes

- This job only reads alerts and users and writes the `notified` /
  `notifiedAt` fields and clears dead `fcmToken` values. It does not touch the
  Android app, Gradle files, the keystore or `firestore.rules`.
- The Admin SDK bypasses Firestore security rules, which is expected and fine
  for a trusted server-side job.
- To change the "recent" window (default 60 minutes), edit
  `RECENT_WINDOW_MINUTES` in `.github/scripts/send-sos-notifications.js`.
