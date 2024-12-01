package com.example.single_lottery;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.single_lottery.ui.notification.Notification;
import com.example.single_lottery.ui.notification.NotificationActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

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
        performLotteryCheck(); // Call your lottery check logic
        sendNotifications();
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
                        String eventName = eventDocumentSnapshot.getString("name"); // Extract event name

                        // Now fetch the registered users for this event
                        db.collection("registered_events")
                                .whereEqualTo("eventId", eventId)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    List<DocumentSnapshot> registeredUsers = querySnapshot.getDocuments();
                                    int winnersCount = Math.min(registeredUsers.size(), lotteryCount);

                                    // Shuffle the list to get random winners
                                    Collections.shuffle(registeredUsers);

                                    // Get winners and losers
                                    List<DocumentSnapshot> winners = registeredUsers.subList(0, winnersCount);
                                    List<DocumentSnapshot> losers = registeredUsers.subList(winnersCount, registeredUsers.size());

                                    // Mark winners and losers in Firestore
                                    List<String> winnerIds = new ArrayList<>();
                                    List<String> loserIds = new ArrayList<>();

                                    // Prepare batch for notifications
                                    WriteBatch batch = db.batch();

                                    // Mark winners and losers and prepare notification documents
                                    for (DocumentSnapshot winner : winners) {
                                        db.collection("registered_events").document(winner.getId()).update("status", "Winner");
                                        winnerIds.add(winner.getId()); // Add user ID to winners list

                                        // Upload notification for the winner to Firebase
                                        Notification winnerNotification = new Notification(
                                                eventName + " - Lottery Results",
                                                "Congratulations, you are a winner!",
                                                winner.getId()
                                        );
                                        DocumentReference winnerNotificationRef = db.collection("notifications").document();
                                        batch.set(winnerNotificationRef, winnerNotification);
                                    }

                                    for (DocumentSnapshot loser : losers) {
                                        db.collection("registered_events").document(loser.getId()).update("status", "Not Selected");
                                        loserIds.add(loser.getId()); // Add user ID to losers list

                                        // Upload notification for the loser to Firebase
                                        Notification loserNotification = new Notification(
                                                eventName + " - Lottery Results",
                                                "Sorry, you were not selected.",
                                                loser.getId()
                                        );
                                        DocumentReference loserNotificationRef = db.collection("notifications").document();
                                        batch.set(loserNotificationRef, loserNotification);
                                    }

                                    // Commit the batch to upload the notifications
                                    batch.commit()
                                            .addOnSuccessListener(aVoid -> {
                                                // Update event 'drawn' status to true
                                                db.collection("events").document(eventId).update("drawnStatus", true);

                                                Log.d("LotteryWorker", "Lottery completed for event: " + eventId);
                                                Toast.makeText(getApplicationContext(), "Lottery and notifications completed.", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("LotteryWorker", "Error uploading notifications", e);
                                                Toast.makeText(getApplicationContext(), "Failed to save notifications.", Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("LotteryWorker", "Error retrieving registered users", e);
                                    Toast.makeText(getApplicationContext(), "Failed to load registered users.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LotteryWorker", "Error retrieving event name", e);
                    Toast.makeText(getApplicationContext(), "Failed to load event name.", Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Sends a notification using NotificationActivity's sendNotification method.
     *
     */
    private void sendNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the notifications collection
        db.collection("notifications")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Loop through all notifications
                        for (DocumentSnapshot notificationDoc : querySnapshot.getDocuments()) {
                            // Extract the fields from the document
                            String title = notificationDoc.getString("title");
                            String message = notificationDoc.getString("message");
                            String userId = notificationDoc.getString("userId");

                            // Ensure title, message, and userId are valid
                            if (title != null && message != null && userId != null) {
                                // Send notification
                                NotificationActivity.sendNotification(getApplicationContext(), title, message, userId);

                                // Delete the notification document from the Firestore
                                db.collection("notifications").document(notificationDoc.getId())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Notification", "Notification for user " + userId + " deleted.");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Notification", "Error deleting notification for user " + userId, e);
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
