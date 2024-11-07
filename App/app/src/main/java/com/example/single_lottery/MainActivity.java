
package com.example.single_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.single_lottery.ui.admin.AdminActivity;
import com.example.single_lottery.ui.organizer.OrganizerActivity;

import com.example.single_lottery.ui.user.UserActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Main entry point activity for Single Lottery application.
 * Handles role selection and automatic lottery execution for events.
 *
 * @author [Haorui Gao]
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    private boolean showLandingScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the initial landing screen is displayed
        showLandingScreen = getIntent().getBooleanExtra("showLandingScreen", true);

        if (showLandingScreen) {
            setContentView(R.layout.activity_landing);

            Button buttonUser = findViewById(R.id.button_user);
            Button buttonOrganizer = findViewById(R.id.button_organizer);
            Button buttonAdmin = findViewById(R.id.button_admin);

            // Lottery check when any button is clicked
            View.OnClickListener listener = v -> {
                performLotteryCheck(); // Perform draw check

                // Jump to different activities based on the button clicked
                Intent intent;
                if (v.getId() == R.id.button_user) {
                    intent = new Intent(MainActivity.this, UserActivity.class);
                } else if (v.getId() == R.id.button_organizer) {
                    intent = new Intent(MainActivity.this, OrganizerActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, AdminActivity.class);
                }
                intent.putExtra("showLandingScreen", false);
                startActivity(intent);
                finish();
            };

            buttonUser.setOnClickListener(listener);
            buttonOrganizer.setOnClickListener(listener);
            buttonAdmin.setOnClickListener(listener);
        }
    }

    /**
     * Checks all events and performs lottery draws if deadline is reached.
     * Verifies lottery time and triggers draw for eligible events.
     */
    private void performLotteryCheck() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String eventId = document.getId();
                        String lotteryTime = document.getString("lotteryTime");
                        int lotteryCount = document.getLong("lotteryCount").intValue();

                        // Check if the lottery time has arrived
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        try {
                            Date lotteryDate = dateFormat.parse(lotteryTime);
                            Date currentDate = new Date();
                            if (currentDate.after(lotteryDate)) {
                                checkIfAlreadyDrawn(eventId, lotteryCount);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Error checking lottery time", e));
    }

    /**
     * Verifies if lottery has already been performed for an event.
     *
     * @param eventId Event to check
     * @param lotteryCount Number of winners to select
     */
    private void checkIfAlreadyDrawn(String eventId, int lotteryCount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("registered_events")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "Winner") // Check if there is already a record with "Winner" status
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // If there is no record with "Winner" status, execute the lottery
                        performLottery(eventId, lotteryCount);
                    } else {
                        Log.d("MainActivity", "Lottery already performed for event: " + eventId);
                    }
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Error checking if lottery already drawn", e));
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

        db.collection("registered_events")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> registeredUsers = queryDocumentSnapshots.getDocuments();
                        int winnersCount = Math.min(registeredUsers.size(), lotteryCount);

                        // Random draw logic
                        Collections.shuffle(registeredUsers); // Randomly shuffle registered users
                        List<DocumentSnapshot> winners = registeredUsers.subList(0, winnersCount);

                        // Update winner status
                        for (DocumentSnapshot winnerDoc : winners) {
                            db.collection("registered_events").document(winnerDoc.getId())
                                    .update("status", "Winner")
                                    .addOnSuccessListener(aVoid -> Log.d("Lottery", "User " + winnerDoc.getString("userId") + " has won the lottery"))
                                    .addOnFailureListener(e -> Log.e("Lottery", "Failed to update winner status", e));
                        }

                        // Update non-winner status
                        List<DocumentSnapshot> losers = registeredUsers.subList(winnersCount, registeredUsers.size());
                        for (DocumentSnapshot loserDoc : losers) {
                            db.collection("registered_events").document(loserDoc.getId())
                                    .update("status", "Not Selected")
                                    .addOnSuccessListener(aVoid -> Log.d("Lottery", "User " + loserDoc.getString("userId") + " did not win"))
                                    .addOnFailureListener(e -> Log.e("Lottery", "Failed to update loser status", e));
                        }

                        Toast.makeText(this, "Lottery completed. Winners have been selected.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("Lottery", "No users registered for event " + eventId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Lottery", "Error performing lottery", e));
    }
}
