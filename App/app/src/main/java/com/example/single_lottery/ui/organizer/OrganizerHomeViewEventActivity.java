package com.example.single_lottery.ui.organizer;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.single_lottery.MapsActivity;
import com.bumptech.glide.Glide;
import com.example.single_lottery.R;
import com.example.single_lottery.EventModel;
import com.example.single_lottery.ui.notification.Notification;
import com.example.single_lottery.ui.notification.NotificationActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

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
    private TextView textViewEventFacility; // new
    private TextView textViewLocationRequirement; // new
    private Button buttonViewWaitingList, buttonViewWinners, buttonViewLosers, buttonViewCancelledUsers, buttonViewAcceptedUsers, buttonGenerateQRCode;
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
        setTitle("Event Details");

        ImageButton backButton = findViewById(R.id.backButton);
        buttonGenerateQRCode = findViewById(R.id.buttonGenerateQRCode);
        backButton.setOnClickListener(v -> finish()); // return to previous page

        Button mapButton = findViewById(R.id.mapButton);
        mapButton.setOnClickListener(v -> {
            String eventId = getIntent().getStringExtra("event_id");
            Log.d("OrganizerHomeViewEventActivity", "event_id: " + eventId);
            if (eventId != null) {
                Intent intent = new Intent(OrganizerHomeViewEventActivity.this, MapsActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Event ID is null", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize back button
        textViewEventName = findViewById(R.id.textViewEventName);
        textViewEventDescription = findViewById(R.id.textViewEventDescription);
        textViewEventFacility = findViewById(R.id.textViewEventFacility); // new
        textViewEventTime = findViewById(R.id.textViewEventTime);
        textViewRegistrationDeadline = findViewById(R.id.textViewRegistrationDeadline);
        textViewLotteryTime = findViewById(R.id.textViewLotteryTime);
        textViewWaitingListCount = findViewById(R.id.textViewWaitingListCount);
        textViewLotteryCount = findViewById(R.id.textViewLotteryCount);
        textViewLocationRequirement = findViewById(R.id.textViewLocationRequirement); // Initialize loocation requirement text view
        imageViewPoster = findViewById(R.id.imageViewPoster);
        buttonViewWaitingList = findViewById(R.id.buttonViewWaitingList);
        buttonViewAcceptedUsers = findViewById(R.id.buttonViewAcceptedUsers);
        buttonViewWinners = findViewById(R.id.buttonViewWinners);
        buttonViewCancelledUsers = findViewById(R.id.buttonViewCancelledUsers);


        // Get event ID from intent
        String eventId = getIntent().getStringExtra("event_id");

        // Load event data from Firestore
        loadEventData(eventId);

        buttonGenerateQRCode.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerHomeViewEventActivity.this, OrganizerQRCode.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });

        buttonViewWaitingList.setOnClickListener(v -> viewWaitingList(eventId));

        // Set the click event of the user button to view the selected and unselected
        buttonViewWinners.setOnClickListener(v -> viewWinners(eventId));

        // Set the click event of the View Accepted User Button
        buttonViewAcceptedUsers.setOnClickListener(v -> viewAcceptedUsers(eventId));

        // Set the click event for the cancelled users button
        buttonViewCancelledUsers.setOnClickListener(v -> viewCancelledUsers(eventId));
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
                            textViewEventFacility.setText(event.getFacility());
                            textViewEventTime.setText(event.getTime());
                            textViewRegistrationDeadline.setText(event.getRegistrationDeadline());
                            textViewLotteryTime.setText(event.getLotteryTime());
                            textViewWaitingListCount.setText(String.valueOf(event.getWaitingListCount()));
                            textViewLotteryCount.setText(String.valueOf(event.getLotteryCount()));
                            textViewLocationRequirement.setText(event.isRequiresLocation() ? "Yes" : "No");


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

        // Get event name and waiting list as before
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDocumentSnapshot -> {
                    if (eventDocumentSnapshot.exists()) {
                        String eventName = eventDocumentSnapshot.getString("name");

                        db.collection("registered_events")
                                .whereEqualTo("eventId", eventId)
                                .whereEqualTo("status", "Waiting")
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    List<String> userIds = new ArrayList<>();

                                    LinearLayout userListLayout = new LinearLayout(this);
                                    userListLayout.setOrientation(LinearLayout.VERTICAL);

                                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                        String userId = document.getString("userId");
                                        String userName = document.getString("userName");

                                        userIds.add(userId);

                                        // Inflate the layout for each user
                                        View userItemView = LayoutInflater.from(this).inflate(R.layout.organizer_event_list_item, userListLayout, false);

                                        TextView userNameTextView = userItemView.findViewById(R.id.userNameTextView);
                                        userNameTextView.setText(userId);

                                        ImageButton userActionButton = userItemView.findViewById(R.id.userCancelButton);
                                        userActionButton.setOnClickListener(v -> {
                                            Toast.makeText(this, "User clicked: " + userName, Toast.LENGTH_SHORT).show();

                                            // Reference to the "registered_events" collection
                                            DocumentReference userEventRef = db.collection("registered_events")
                                                    .document(userId); // Assuming the document ID is the userId (adjust if necessary)

                                            // Update the status to "Cancelled"
                                            userEventRef.update("status", "Cancelled")
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(this, userName + " has been cancelled.", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(this, "Failed to update status.", Toast.LENGTH_SHORT).show();
                                                    });
                                        });


                                        // Add the inflated view to the user list layout
                                        userListLayout.addView(userItemView);
                                    }

                                    // Show waiting list in dialog
                                    new AlertDialog.Builder(this)
                                            .setTitle("Waiting List")
                                            .setView(userListLayout)
                                            .setPositiveButton("OK", null)
                                            .setNegativeButton("Notify", (dialog, which) -> {
                                                // Send notification logic
                                            })
                                            .show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load waiting list.", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load event name.", Toast.LENGTH_SHORT).show());
    }



    /**
     * Displays list of winners for the event.
     * Shows winners list in an AlertDialog.
     *
     * @param eventId ID of the event to view winners for
     */
    private void viewWinners(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Retrieve the event name first
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    String eventName = eventDoc.getString("name");

                    // Retrieve the list of winners
                    db.collection("registered_events")
                            .whereEqualTo("eventId", eventId)
                            .whereEqualTo("status", "Winner")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                List<String> winnerIds = new ArrayList<>();
                                StringBuilder winnersList = new StringBuilder();

                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    String userId = document.getString("userId");
                                    winnerIds.add(userId);  // Collect user IDs of winners
                                    winnersList.append(userId).append("\n");
                                }

                                // Show winners list in dialog with "Notify" button
                                new AlertDialog.Builder(this)
                                        .setTitle("Winners List")
                                        .setMessage(winnersList.toString())
                                        .setPositiveButton("OK", null)
                                        .setNegativeButton("Notify", (dialog, which) -> {
                                            // Show dialog to input custom message
                                            AlertDialog.Builder messageDialog = new AlertDialog.Builder(this);
                                            messageDialog.setTitle("Custom Message");
                                            final EditText input = new EditText(this);
                                            messageDialog.setView(input);
                                            messageDialog.setPositiveButton("Send", (innerDialog, which1) -> {
                                                String customMessage = input.getText().toString().trim();
                                                if (!customMessage.isEmpty()) {
                                                    if (!winnerIds.isEmpty()) {
                                                        String notificationTitle = "Event Notification - " + eventName;

                                                        // Batch write notifications to Firestore
                                                        WriteBatch batch = db.batch();
                                                        for (String userId : winnerIds) {
                                                            DocumentReference notificationRef = db.collection("notifications").document();
                                                            Notification notification = new Notification(
                                                                    notificationTitle,
                                                                    customMessage,
                                                                    userId
                                                            );
                                                            batch.set(notificationRef, notification);
                                                        }

                                                        // Commit batch and send notifications
                                                        batch.commit()
                                                                .addOnSuccessListener(aVoid -> {
                                                                    Toast.makeText(this, "Notification sent to winners.", Toast.LENGTH_SHORT).show();
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Toast.makeText(this, "Failed to save notifications.", Toast.LENGTH_SHORT).show();
                                                                });
                                                    } else {
                                                        Toast.makeText(this, "No winners to notify.", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Toast.makeText(this, "Message cannot be empty.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            messageDialog.setNegativeButton("Cancel", null);
                                            messageDialog.show();
                                        })
                                        .show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to load winners.", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load event details.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Displays list of accepted users for the event.
     * Shows accepted users in an AlertDialog.
     *
     * @param eventId ID of the event to view accepted users for
     */
    private void viewAcceptedUsers(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // First, get the event name by querying the events collection
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDocumentSnapshot -> {
                    if (eventDocumentSnapshot.exists()) {
                        String eventName = eventDocumentSnapshot.getString("name"); // Extract event name

                        // Now fetch the registered users for this event's accepted users
                        db.collection("registered_events")
                                .whereEqualTo("eventId", eventId)
                                .whereEqualTo("status", "Accepted")
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    List<String> acceptedIds = new ArrayList<>();
                                    StringBuilder acceptedList = new StringBuilder();

                                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                        String userId = document.getString("userId");
                                        acceptedIds.add(userId);  // Collect user IDs of accepted users
                                        acceptedList.append(userId).append("\n");
                                    }

                                    // Show accepted users list in dialog with "Notify" button
                                    new AlertDialog.Builder(this)
                                            .setTitle("Accepted Users")
                                            .setMessage(acceptedList.toString())
                                            .setPositiveButton("OK", null)
                                            .setNegativeButton("Notify", (dialog, which) -> {
                                                // Show dialog to input custom message
                                                AlertDialog.Builder messageDialog = new AlertDialog.Builder(this);
                                                messageDialog.setTitle("Custom Message");
                                                final EditText input = new EditText(this);
                                                messageDialog.setView(input);
                                                messageDialog.setPositiveButton("Send", (innerDialog, which1) -> {
                                                    String customMessage = input.getText().toString().trim();
                                                    if (!customMessage.isEmpty()) {
                                                        if (!acceptedIds.isEmpty()) {
                                                            String notificationTitle = "Event Notification - " + eventName;

                                                            // Batch write notifications to Firestore
                                                            WriteBatch batch = db.batch();
                                                            for (String userId : acceptedIds) {
                                                                DocumentReference notificationRef = db.collection("notifications").document();
                                                                Notification notification = new Notification(notificationTitle, customMessage, userId);
                                                                batch.set(notificationRef, notification);
                                                            }

                                                            // Commit batch and send notifications
                                                            batch.commit()
                                                                    .addOnSuccessListener(aVoid -> {
                                                                        Toast.makeText(this, "Notifications sent to accepted users.", Toast.LENGTH_SHORT).show();
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Toast.makeText(this, "Failed to save notifications.", Toast.LENGTH_SHORT).show();
                                                                    });
                                                        } else {
                                                            Toast.makeText(this, "No accepted users to notify.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(this, "Message cannot be empty.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                                messageDialog.setNegativeButton("Cancel", null);
                                                messageDialog.show();
                                            })
                                            .show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load accepted users.", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load event name.", Toast.LENGTH_SHORT).show());
    }





    /**
     * Displays list of cancelled users for the event.
     * This function is left blank as requested.
     *
     * @param eventId ID of the event to view cancelled users for
     */
    private void viewCancelledUsers(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // First, get the event name by querying the events collection
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDocumentSnapshot -> {
                    if (eventDocumentSnapshot.exists()) {
                        String eventName = eventDocumentSnapshot.getString("name"); // Extract event name

                        // Now fetch the cancelled users for this event (status = "Declined")
                        db.collection("registered_events")
                                .whereEqualTo("eventId", eventId)
                                .whereEqualTo("status", "Declined")
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    List<String> cancelledIds = new ArrayList<>();
                                    StringBuilder cancelledList = new StringBuilder();

                                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                        String userId = document.getString("userId");
                                        cancelledIds.add(userId);  // Collect user IDs of cancelled users
                                        cancelledList.append(userId).append("\n");
                                    }

                                    // Show cancelled users list in dialog with "Notify" button
                                    new AlertDialog.Builder(this)
                                            .setTitle("Cancelled Users")
                                            .setMessage(cancelledList.toString())
                                            .setPositiveButton("OK", null)
                                            .setNegativeButton("Notify", (dialog, which) -> {
                                                // Show dialog to input custom message
                                                AlertDialog.Builder messageDialog = new AlertDialog.Builder(this);
                                                messageDialog.setTitle("Custom Message");
                                                final EditText input = new EditText(this);
                                                messageDialog.setView(input);
                                                messageDialog.setPositiveButton("Send", (innerDialog, which1) -> {
                                                    String customMessage = input.getText().toString().trim();
                                                    if (!customMessage.isEmpty()) {
                                                        if (!cancelledIds.isEmpty()) {
                                                            String notificationTitle = "Event Cancellation - " + eventName;

                                                            // Batch write notifications to Firestore
                                                            WriteBatch batch = db.batch();
                                                            for (String userId : cancelledIds) {
                                                                DocumentReference notificationRef = db.collection("notifications").document();
                                                                Notification notification = new Notification(
                                                                        notificationTitle,
                                                                        customMessage,
                                                                        userId
                                                                );
                                                                batch.set(notificationRef, notification);
                                                            }

                                                            // Commit batch and send notifications
                                                            batch.commit()
                                                                    .addOnSuccessListener(aVoid -> {
                                                                        Toast.makeText(this, "Notifications sent to cancelled users.", Toast.LENGTH_SHORT).show();
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Toast.makeText(this, "Failed to save notifications.", Toast.LENGTH_SHORT).show();
                                                                    });
                                                        } else {
                                                            Toast.makeText(this, "No cancelled users to notify.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(this, "Message cannot be empty.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                                messageDialog.setNegativeButton("Cancel", null);
                                                messageDialog.show();
                                            })
                                            .show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load cancelled users.", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load event name.", Toast.LENGTH_SHORT).show());
    }



}
