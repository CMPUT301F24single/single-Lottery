package com.example.single_lottery.ui.organizer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.single_lottery.MapsActivity;

import com.bumptech.glide.Glide;
import com.example.single_lottery.R;
import com.example.single_lottery.EventModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity for viewing event details by organizers.
 * Provides comprehensive view of event information and management tools including:
 * - Basic event information display
 * - Participant list management
 * - QR code generation
 * - Map location view
 * - User status tracking
 *
 * Handles:
 * - Loading and displaying event details from Firestore
 * - Managing different user lists (waiting, selected, accepted)
 * - Navigation to QR code generation and map views
 *
 * @author [Jingyao Gu]
 * @version 1.0
 * @see AppCompatActivity
 * @see EventModel
 * @since 1.0
 */
public class OrganizerHomeViewEventActivity extends AppCompatActivity {

    private TextView textViewEventName, textViewEventTime, textViewRegistrationDeadline,
            textViewLotteryTime, textViewWaitingListCount, textViewLotteryCount, textViewEventDescription;
    private ImageView imageViewPoster;
    private Button buttonViewWaitingList, buttonViewSelectedUsers, buttonViewAcceptedUsers, buttonGenerateQRCode;

    /**
     * Initializes the event viewing interface and sets up:
     * - UI component references
     * - Event data loading
     * - Button click listeners
     * - Navigation handlers
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                          this Bundle contains the data it most recently supplied
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_home_view_event);

        ImageButton backButton = findViewById(R.id.backButton);
        buttonGenerateQRCode = findViewById(R.id.buttonGenerateQRCode);
        backButton.setOnClickListener(v -> finish()); // return to previous page

        Button mapButton = findViewById(R.id.mapButton);
        mapButton.setOnClickListener(v -> {
            String eventId = getIntent().getStringExtra("event_id"); // 确保这里的键名与传递的一致
            Log.d("OrganizerHomeViewEventActivity", "event_id: " + eventId);
            if (eventId != null) {
                Intent intent = new Intent(OrganizerHomeViewEventActivity.this, MapsActivity.class);
                intent.putExtra("eventId", eventId); // 将 eventId 传递
                startActivity(intent);
            } else {
                Toast.makeText(this, "Event ID is null", Toast.LENGTH_SHORT).show(); // 处理 eventId 为 null 的情况
            }
        });


        // Initialize back button
        textViewEventName = findViewById(R.id.textViewEventName);
        textViewEventDescription = findViewById(R.id.textViewEventDescription);
        textViewEventTime = findViewById(R.id.textViewEventTime);
        textViewRegistrationDeadline = findViewById(R.id.textViewRegistrationDeadline);
        textViewLotteryTime = findViewById(R.id.textViewLotteryTime);
        textViewWaitingListCount = findViewById(R.id.textViewWaitingListCount);
        textViewLotteryCount = findViewById(R.id.textViewLotteryCount);
        imageViewPoster = findViewById(R.id.imageViewPoster);
        buttonViewWaitingList = findViewById(R.id.buttonViewWaitingList);
        buttonViewSelectedUsers = findViewById(R.id.buttonViewSelectedUsers);
        buttonViewAcceptedUsers = findViewById(R.id.buttonViewAcceptedUsers);

        // Get event ID from intent
        String eventId = getIntent().getStringExtra("event_id");

        // Load event data from Firestore
        loadEventData(eventId);

        buttonGenerateQRCode.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerHomeViewEventActivity.this, QRCodeActivity.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });

        buttonViewWaitingList.setOnClickListener(v -> viewWaitingList(eventId));

        // Set the click event of the user button to view the selected and unselected
        buttonViewSelectedUsers.setOnClickListener(v -> viewSelectedAndNotSelectedUsers(eventId));

        // Set the click event of the View Accepted User Button
        buttonViewAcceptedUsers.setOnClickListener(v -> viewAcceptedUsers(eventId));
    }

    /**
     * Reloads event data when activity resumes to ensure current information.
     */
    @Override
    protected void onResume() {
        super.onResume();
        String eventId = getIntent().getStringExtra("event_id");  // Make sure you pass the correct eventId
        loadEventData(eventId);  // Reload activity data every time you return to the page
    }

    /**
     * Loads event details from Firestore and updates UI components.
     * Displays event information including name, description, times, and poster.
     *
     * @param eventId ID of the event to load
     */
    private void loadEventData(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        EventModel event = documentSnapshot.toObject(EventModel.class);
                        if (event != null) {
                            textViewEventName.setText(event.getName());
                            textViewEventDescription.setText(event.getDescription());
                            textViewEventTime.setText(event.getTime());
                            textViewRegistrationDeadline.setText(event.getRegistrationDeadline());
                            textViewLotteryTime.setText(event.getLotteryTime());
                            textViewWaitingListCount.setText(String.valueOf(event.getWaitingListCount()));
                            textViewLotteryCount.setText(String.valueOf(event.getLotteryCount()));

                            // Use Glide to display event posters
                            if (event.getPosterUrl() != null) {
                                Glide.with(this).load(event.getPosterUrl()).into(imageViewPoster);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                });
    }

    /**
     * Displays list of users waiting to participate in the event.
     * Shows waiting list in an AlertDialog.
     *
     * @param eventId ID of the event to view waiting list for
     */
    private void viewWaitingList(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("registered_events")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    StringBuilder waitingList = new StringBuilder();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String userId = document.getString("userId");
                        waitingList.append(userId).append("\n");
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Waiting List")
                            .setMessage(waitingList.toString())
                            .setPositiveButton("OK", null)
                            .show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load waiting list.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Displays lists of selected and non-selected users for the event.
     * Shows both winners and non-winners in an AlertDialog.
     *
     * @param eventId ID of the event to view selected users for
     */
    private void viewSelectedAndNotSelectedUsers(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("registered_events")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    StringBuilder winnersList = new StringBuilder("Winners:\n");
                    StringBuilder nonWinnersList = new StringBuilder("Non-Winners:\n");

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String userId = document.getString("userId");
                        String status = document.getString("status");

                        // Check and add users to the corresponding list
                        if ("Winner".equals(status)) {
                            winnersList.append(userId).append("\n");
                        } else if ("Not Selected".equals(status)) {
                            nonWinnersList.append(userId).append("\n");
                        }
                    }

                    // Make sure the data is displayed in the pop-up box
                    new AlertDialog.Builder(this)
                            .setTitle("Selected Users")
                            .setMessage(winnersList.toString() + "\n" + nonWinnersList.toString())
                            .setPositiveButton("OK", null)
                            .show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load selected users.", Toast.LENGTH_SHORT).show());
    }


    /**
     * Displays list of users who have accepted their selection for the event.
     * Shows accepted users in an AlertDialog.
     *
     * @param eventId ID of the event to view accepted users for
     */
    private void viewAcceptedUsers(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("registered_events")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "Accepted")  // Only query users whose status is Accepted
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    StringBuilder acceptedUsersList = new StringBuilder();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String userId = document.getString("userId");
                        acceptedUsersList.append(userId).append("\n");
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Accepted Users")
                            .setMessage(acceptedUsersList.toString())
                            .setPositiveButton("OK", null)
                            .show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load accepted users.", Toast.LENGTH_SHORT).show());
    }
}
