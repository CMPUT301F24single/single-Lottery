package com.example.single_lottery;

import static com.example.single_lottery.ui.notification.NotificationActivity.sendNotification;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LotteryWorker extends Worker {

    public LotteryWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        performLotteryCheck(); // Call your lottery check logic
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
                        if (!drawn) {
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

        // Fetch event name
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(eventSnapshot -> {
                    // Get event name from Firestore
                    String eventName = eventSnapshot.getString("name");

                    if (eventName != null) {
                        db.collection("registered_events").whereEqualTo("eventId", eventId)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    List<DocumentSnapshot> registeredUsers = queryDocumentSnapshots.getDocuments();
                                    int winnersCount = Math.min(registeredUsers.size(), lotteryCount);

                                    // Shuffle the list to get random winners
                                    Collections.shuffle(registeredUsers);

                                    // Get winners and losers
                                    List<DocumentSnapshot> winners = registeredUsers.subList(0, winnersCount);
                                    List<DocumentSnapshot> losers = registeredUsers.subList(winnersCount, registeredUsers.size());

                                    // Update Firestore: mark winners and losers
                                    for (DocumentSnapshot winner : winners) {
                                        db.collection("registered_events").document(winner.getId()).update("status", "Winner");
                                        // Send notification to winner with event name in title
                                        sendNotification(getApplicationContext(),
                                                eventName + " - Lottery Results",
                                                "Congratulations, you are a winner!",
                                                "winner");
                                    }

                                    for (DocumentSnapshot loser : losers) {
                                        db.collection("registered_events").document(loser.getId()).update("status", "Not Selected");
                                        // Send notification to loser with event name in title
                                        sendNotification(getApplicationContext(),
                                                eventName + " - Lottery Results",
                                                "Sorry, you were not selected.",
                                                "loser");
                                    }

                                    // update event 'drawn' status to true
                                    db.collection("events").document(eventId).update("drawnStatus", true);

                                    Log.d("LotteryWorker", "Lottery completed for event: " + eventId);
                                })
                                .addOnFailureListener(e -> Log.e("LotteryWorker", "Error performing lottery", e));
                    } else {
                        Log.e("LotteryWorker", "Event name not found for event ID: " + eventId);
                    }
                })
                .addOnFailureListener(e -> Log.e("LotteryWorker", "Error retrieving event name", e));
    }

}
