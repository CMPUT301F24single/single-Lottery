package com.example.single_lottery;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import android.content.ContentResolver;


import com.example.single_lottery.ui.notification.Notification;
import com.example.single_lottery.ui.notification.NotificationActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.w3c.dom.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OfflineWorker extends Worker {

    public OfflineWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        performLotteryCheck();

        new Handler(Looper.getMainLooper()).postDelayed(this::sendNotifications, 2000); // delay the execution of sendNotifications() by 2 seconds

        return Result.success();
    }

    /**
     * Checks all events and performs lottery draws if deadline is reached.
     * Verifies lottery time and triggers draw for eligible events.
     */
    public void performLotteryCheck() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String eventId = document.getId();
                        String lotteryTime = document.getString("lotteryTime");
                        int lotteryCount = document.getLong("lotteryCount").intValue();

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        try {
                            Date lotteryDate = dateFormat.parse(lotteryTime);
                            if (new Date().after(lotteryDate)) {
                                checkIfAlreadyDrawn(eventId, lotteryCount);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("LotteryWorker", "Error checking lottery time", e));
    }

    /**
     * Verifies if lottery has already been performed for an event.
     *
     * @param eventId Event to check
     * @param lotteryCount Number of winners to select
     */
    private void checkIfAlreadyDrawn(String eventId, int lotteryCount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId) // Check in the events collection
                .get()
                .addOnSuccessListener(eventSnapshot -> {
                    if (eventSnapshot.exists()) {
                        Boolean drawn = eventSnapshot.getBoolean("drawnStatus"); // Fetch the 'drawn' field
                        if (Boolean.FALSE.equals(drawn)) {
                            // Lottery hasn't been drawn yet, proceed to perform the lottery
                            performLottery(eventId, lotteryCount);
                        } else {
                            Log.d("LotteryWorker", "Lottery has already been drawn for event: " + eventId);
                        }
                    } else {
                        Log.e("LotteryWorker", "Event not found for event ID: " + eventId);
                    }
                })
                .addOnFailureListener(e -> Log.e("LotteryWorker", "Error checking lottery status", e));
    }

    /**
     * Executes lottery draw for an event.
     * Randomly selects winners, updates participant status,
     * and notifies users of results.
     *
     * @param eventId Event to perform lottery for
     * @param lotteryCount Number of winners to select
     */
    private void performLottery(String eventId, int lotteryCount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // First, get the event name by querying the events collection
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDocumentSnapshot -> {
                    if (eventDocumentSnapshot.exists()) {
                        String eventName = eventDocumentSnapshot.getString("name");

                        // Fetch the registered users for this event
                        db.collection("registered_events")
                                .whereEqualTo("eventId", eventId)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    List<DocumentSnapshot> registeredUsers = querySnapshot.getDocuments();
                                    int winnersCount = Math.min(registeredUsers.size(), lotteryCount);

                                    // Shuffle the list to randomize
                                    Collections.shuffle(registeredUsers);

                                    // Select winners and losers
                                    List<DocumentSnapshot> winners = registeredUsers.subList(0, winnersCount);
                                    List<DocumentSnapshot> losers = registeredUsers.subList(winnersCount, registeredUsers.size());

                                    // Prepare batch to ensure atomic operations
                                    WriteBatch batch = db.batch();

                                    // Handle winners
                                    for (DocumentSnapshot winner : winners) {
                                        String userId = winner.getString("userId"); // Ensure this matches your Firestore field name
                                        if (userId != null) {
                                            // Query the registered_events collection with both eventId and userId to get the specific document
                                            db.collection("registered_events")
                                                    .whereEqualTo("eventId", eventId)
                                                    .whereEqualTo("userId", userId)
                                                    .get()
                                                    .addOnSuccessListener(winnerQuerySnapshot -> {
                                                        if (!winnerQuerySnapshot.isEmpty()) {
                                                            // Since we expect only one document, we can take the first one
                                                            DocumentSnapshot registeredEvent = winnerQuerySnapshot.getDocuments().get(0);

                                                            // Update the status of the winner to "Winner"
                                                            batch.update(db.collection("registered_events").document(registeredEvent.getId()), "status", "Winner");

                                                            // Create and add notification for the winner
                                                            Notification winnerNotification = new Notification(
                                                                    eventName + " - Lottery Results",
                                                                    "Congratulations, you are a winner!",
                                                                    userId
                                                            );
                                                            DocumentReference winnerNotificationRef = db.collection("notifications").document();
                                                            batch.set(winnerNotificationRef, winnerNotification);
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> Log.e("LotteryWorker", "Error querying registered_events for userId: " + userId, e));
                                        }
                                    }

                                    // Handle losers
                                    for (DocumentSnapshot loser : losers) {
                                        String userId = loser.getString("userId"); // Ensure this matches your Firestore field name
                                        if (userId != null) {
                                            // Create and add notification for the loser
                                            Notification loserNotification = new Notification(
                                                    eventName + " - Lottery Results",
                                                    "Sorry, you were not selected.",
                                                    userId
                                            );
                                            DocumentReference loserNotificationRef = db.collection("notifications").document();
                                            batch.set(loserNotificationRef, loserNotification);
                                        }
                                    }

                                    // Commit the batch
                                    batch.commit()
                                            .addOnSuccessListener(aVoid -> {
                                                db.collection("events").document(eventId).update("drawnStatus", true)
                                                        .addOnSuccessListener(unused -> Log.d("LotteryWorker", "Lottery completed for event: " + eventId))
                                                        .addOnFailureListener(e -> Log.e("LotteryWorker", "Error updating event status", e));
                                            })
                                            .addOnFailureListener(e -> Log.e("LotteryWorker", "Error committing batch", e));
                                })
                                .addOnFailureListener(e -> Log.e("LotteryWorker", "Error retrieving registered users", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("LotteryWorker", "Error retrieving event name", e));
    }










    /**
     * Sends a notification using NotificationActivity's sendNotification method.
     *
     */
    private void sendNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the current user's ID (ANDROID_ID)
        String currentUserId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Query the notifications collection for notifications relevant to the current user
        db.collection("notifications")
                .whereEqualTo("userId", currentUserId) // Filter notifications based on userId
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Loop through all notifications for the current user
                        for (DocumentSnapshot notificationDoc : querySnapshot.getDocuments()) {
                            // Extract the fields from the notification document
                            String title = notificationDoc.getString("title");
                            String message = notificationDoc.getString("message");
                            String userId = notificationDoc.getString("userId");

                            // Check if the title, message, and userId are valid
                            if (title != null && message != null && userId != null) {
                                // Retrieve user notification preferences
                                db.collection("users")
                                        .whereEqualTo("uid", userId)
                                        .get()
                                        .addOnSuccessListener(userSnapshot -> {
                                            if (!userSnapshot.isEmpty()) {
                                                // Get the user's notification preference
                                                DocumentSnapshot userDoc = userSnapshot.getDocuments().get(0);
                                                boolean notificationStatus = userDoc.getBoolean("notificationsEnabled");

                                                // If notifications are enabled, send the notification
                                                if (notificationStatus) {
                                                    // Send the notification
                                                    NotificationActivity.sendNotification(getApplicationContext(), title, message, userId);
                                                } else {
                                                    // Log that notifications are disabled for this user
                                                    Log.d("Notification", "Notifications are disabled for user " + userId);
                                                }
                                                // Delete the notification document from Firestore
                                                db.collection("notifications").document(notificationDoc.getId())
                                                        .delete()
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d("Notification", "Notification for user " + userId + " deleted.");
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e("Notification", "Error deleting notification for user " + userId, e);
                                                        });
                                            } else {
                                                Log.e("Notification", "User document not found for userId: " + userId);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Notification", "Error retrieving user preferences for userId: " + userId, e);
                                        });
                            } else {
                                Log.e("Notification", "Invalid notification data: Missing title, message, or userId.");
                            }
                        }
                    } else {
                        Log.d("Notification", "No notifications to send.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Notification", "Error retrieving notifications", e);
                });
    }

}
