# Firebase Push Notifications Setup

The Android app is already fully configured to RECEIVE push notifications (both while the app is active and in the background) through `MyFirebaseMessagingService.kt`. The Admin Panel is also fully configured to SEND notifications (broadcasting to `notifications/{userId}/items` and `fcm_messages`).

However, for Push Notifications to be delivered to devices in the background, you **MUST** deploy Firebase Cloud Functions.

## 🛠️ Deployment Instructions

1. Open your terminal on your computer.
2. Ensure you have Node.js and the Firebase CLI installed (`npm install -g firebase-tools`).
3. Login to Firebase: `firebase login`
4. Go to the `firebase-functions` folder that I created for you.
5. Initialize or select your project: `firebase use YOUR_PROJECT_ID`
6. Install dependencies: `npm install`
7. Deploy the functions: `firebase deploy --only functions`

Once deployed, anytime you use the Admin Panel to send a notification, or when a user applies for a job, the Cloud Function will automatically push the FCM message to the users' devices! 🚀
