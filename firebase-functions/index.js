const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

// 1. Trigger when a specific user notification is added
exports.sendUserNotification = functions.firestore
    .document("notifications/{userId}/items/{notificationId}")
    .onCreate(async (snap, context) => {
        const notification = snap.data();
        const userId = context.params.userId;

        // Don't send if already read (though they are usually unread on create)
        if (notification.isRead) return null;

        try {
            // Get user's FCM token
            const userDoc = await admin.firestore().collection("users").doc(userId).get();
            if (!userDoc.exists) return null;

            const userData = userDoc.data();
            const fcmToken = userData.fcmToken;

            if (!fcmToken) {
                console.log("No FCM token for user:", userId);
                return null;
            }

            const message = {
                token: fcmToken,
                notification: {
                    title: notification.title || "New Notification",
                    body: notification.body || "",
                },
                data: {
                    type: notification.type || "general",
                    click_action: "FLUTTER_NOTIFICATION_CLICK"
                },
                android: { priority: "high" }
            };

            // Send push notification
            await admin.messaging().send(message);
            console.log("Successfully sent notification to user:", userId);
        } catch (error) {
            console.error("Error sending notification:", error);
            if (error.code === 'messaging/registration-token-not-registered' || 
                error.code === 'messaging/invalid-registration-token') {
                console.log("Removing invalid FCM token for user:", userId);
                await admin.firestore().collection("users").doc(userId).update({
                    fcmToken: admin.firestore.FieldValue.delete()
                });
            }
        }
    });

// 2. Trigger when Admin sends a Global Announcement or FCM Message
exports.sendGlobalNotification = functions.firestore
    .document("fcm_messages/{messageId}")
    .onCreate(async (snap, context) => {
        const messageData = snap.data();
        
        try {
            const topic = messageData.topic || "all_users";
            const message = {
                topic: topic,
                notification: {
                    title: messageData.notification?.title || "Announcement",
                    body: messageData.notification?.body || "",
                },
                data: messageData.data || {}
            };

            await admin.messaging().send(message);
            console.log("Successfully sent global notification to topic:", topic);
        } catch (error) {
            console.error("Error sending global notification:", error);
        }
    });
