# CampusFlow Firebase Backend

This directory contains the Firebase backend configuration, security rules, Firestore composite indexes, and Cloud Functions for **CampusFlow** (`project_id: campusflow-c936d`).

## Directory Structure

```text
Backend/
├── .firebaserc              # Default Firebase project alias (campusflow-c936d)
├── firebase.json            # Firebase CLI configuration (Firestore, Storage, Functions)
├── firestore.rules          # UID-isolated Cloud Firestore security rules & schema checks
├── firestore.indexes.json   # Composite indexes for user subcollections
├── storage.rules            # UID-isolated Firebase Storage rules (profile images & attachments)
├── functions/
│   ├── package.json         # Node 20 Cloud Functions dependencies
│   ├── tsconfig.json        # TypeScript configuration
│   └── src/index.ts         # Scheduled FCM reminders & FCM token registration
└── README.md                # Setup & deployment guide
```

## Firestore Data Model

All student records are strictly isolated under the authenticated user's UID:

```text
users/
  {uid}/                     # Profile: name, email, course, semester, university, photoUrl, createdAt, updatedAt
    tasks/{taskId}
    assignments/{assignmentId}
    subjects/{subjectId}
    attendance/{attendanceId}  # Deterministic ID: {subjectRemoteId}_{YYYY-MM-DD}
    quizResults/{resultId}
    notes/{noteId}
    studySessions/{sessionId}
    settings/preferences     # themeMode, notificationsEnabled, dailyStudyGoalMinutes
    fcmTokens/{tokenId}      # Device FCM registration tokens
```

## Deployment Instructions

### Option 1: Firebase CLI (Recommended)

1. Install Firebase CLI and log in:
   ```bash
   npm install -g firebase-tools
   firebase login
   ```
2. Navigate to the `Backend` directory:
   ```bash
   cd Backend
   ```
3. Install Cloud Functions dependencies and build TypeScript:
   ```bash
   cd functions
   npm install
   npm run build
   cd ..
   ```
4. Deploy Security Rules and Indexes:
   ```bash
   firebase deploy --only firestore:rules,firestore:indexes,storage
   ```
5. Deploy Cloud Functions (requires Firebase Blaze plan for Cloud Scheduler + Cloud Functions v2):
   ```bash
   firebase deploy --only functions
   ```

### Option 2: Firebase Console

1. Open [Firebase Console](https://console.firebase.google.com/) and select **campusflow-c936d**.
2. **Authentication**:
   - Go to **Build → Authentication → Sign-in method**.
   - Enable **Email/Password**.
3. **Cloud Firestore**:
   - Go to **Build → Firestore Database** and create the database in production mode.
   - Open the **Rules** tab and paste the contents of `Backend/firestore.rules`, then click **Publish**.
4. **Firebase Storage**:
   - Go to **Build → Storage** and initialize Storage.
   - Open the **Rules** tab and paste the contents of `Backend/storage.rules`, then click **Publish**.
5. **Cloud Messaging**:
   - Ensure Firebase Cloud Messaging API (V1) is enabled in **Project Settings → Cloud Messaging**.
   - Note: Scheduled reminders (`sendScheduledReminders`) require deploying `Backend/functions` on the Blaze plan.

## Testing Security Rules Locally

With Firebase Emulator Suite installed:

```bash
cd Backend
firebase emulators:start --only auth,firestore,storage
```
