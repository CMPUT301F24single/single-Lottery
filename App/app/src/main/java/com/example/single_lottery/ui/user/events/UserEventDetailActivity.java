package com.example.single_lottery.ui.user.events;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.single_lottery.R;
import com.example.single_lottery.ui.notification.Notification;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity for displaying event details from user perspective.
 * Handles event registration, lottery status updates and registration cancellation.
 *
 * @author [Jingyao Gu]
 * @version 1.0
 */
public class UserEventDetailActivity extends AppCompatActivity {
    private TextView eventNameTextView, eventTimeTextView, registrationDeadlineTextView,
            lotteryTimeTextView, waitingListCountTextView, lotteryCountTextView, eventDescriptionTextView;
    private ImageView eventPosterImageView;
    private TextView eventStatusValueTextView;
    private Button cancelRegistrationButton, acceptButton, declineButton;

    private String registrationDeadline;
    private String lotteryTime;
    private String eventTime;
    private String eventId;
    private String userId;
    private TextView eventFacilityTextView; // new

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_event_viewdetail);
        setTitle("Event Details");

        // Bind views
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        eventPosterImageView = findViewById(R.id.imageViewPoster);
        eventNameTextView = findViewById(R.id.eventNameTextView);
        eventTimeTextView = findViewById(R.id.eventTimeTextView);
        registrationDeadlineTextView = findViewById(R.id.registrationDeadlineTextView);
        lotteryTimeTextView = findViewById(R.id.lotteryTimeTextView);
        waitingListCountTextView = findViewById(R.id.waitingListCountTextView);
        lotteryCountTextView = findViewById(R.id.lotteryCountTextView);
        eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
        eventStatusValueTextView = findViewById(R.id.eventStatusValueTextView);
        eventFacilityTextView = findViewById(R.id.eventFacilityTextView);
        cancelRegistrationButton = findViewById(R.id.cancelRegistrationButton);
        acceptButton = findViewById(R.id.acceptButton);
        declineButton = findViewById(R.id.declineButton);

        eventId = getIntent().getStringExtra("event_id");
        if (eventId == null) {
            Toast.makeText(this, "Event ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        loadEventDetails(eventId);

        cancelRegistrationButton.setOnClickListener(v -> cancelRegistration(eventId));
        acceptButton.setOnClickListener(v -> updateLotteryStatus("Accepted"));
        declineButton.setOnClickListener(v -> updateLotteryStatus("Cancelled"));
    }

    /**
     * Loads event details from Firestore and updates UI.
     * Displays event information including name, time, description and poster.
     *
     * @param eventId Unique identifier of the event
     */
    private void loadEventDetails(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the event details
                        String eventName = documentSnapshot.getString("name");
                        String eventDescription = documentSnapshot.getString("description");
                        String eventFacility = documentSnapshot.getString("facility");
                        eventTime = documentSnapshot.getString("time");
                        registrationDeadline = documentSnapshot.getString("registrationDeadline");
                        lotteryTime = documentSnapshot.getString("lotteryTime");
                        String waitingListCount = documentSnapshot.getLong("waitingListCount") + "";
                        String lotteryCount = documentSnapshot.getLong("lotteryCount") + "";

                        // Set event details to views
                        eventNameTextView.setText(eventName);
                        eventTimeTextView.setText(eventTime);
                        registrationDeadlineTextView.setText(registrationDeadline);
                        lotteryTimeTextView.setText(lotteryTime);
                        waitingListCountTextView.setText(waitingListCount);
                        lotteryCountTextView.setText(lotteryCount);
                        eventDescriptionTextView.setText(eventDescription);
                        eventFacilityTextView.setText(eventFacility);

                        // Safely retrieve the 'requiresLocation' field (boolean)
                        Boolean requiresLocationBoolean = documentSnapshot.getBoolean("requiresLocation");
                        // If 'requiresLocation' is null, set default value as false
                        boolean requiresLocation = (requiresLocationBoolean != null) ? requiresLocationBoolean : false;

                        // Set location text based on the boolean value
                        TextView eventLocationTextView = findViewById(R.id.eventLocationTextView);
                        eventLocationTextView.setText(requiresLocation ? "Yes" : "No");

                        // Load the poster image
                        String posterUrl = documentSnapshot.getString("posterUrl");
                        if (posterUrl != null && !posterUrl.isEmpty()) {
                            Glide.with(this).load(posterUrl).into(eventPosterImageView);
                        } else {
                            // Load a default image if posterUrl is empty or null
                            Glide.with(this).load(R.drawable.defaultbackground).into(eventPosterImageView);
                        }

                        // Update event status
                        updateEventStatus();
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("UserEventDetailActivity", "Error loading event details", e));
    }




    /**
     * Updates event status display based on current time and deadlines.
     * Shows different status messages and buttons based on event phase:
     * - Registration open
     * - Awaiting lottery
     * - Lottery completed
     * - Event ended
     */
    private void updateEventStatus() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date deadlineDate = dateFormat.parse(registrationDeadline);
            Date lotteryDate = dateFormat.parse(lotteryTime);
            Date eventEndDate = dateFormat.parse(eventTime); // 活动的结束时间
            Date currentDate = new Date();

            if (currentDate.before(deadlineDate)) {
                eventStatusValueTextView.setText("Open for Registration");
                cancelRegistrationButton.setVisibility(View.VISIBLE);
                acceptButton.setVisibility(View.GONE);
                declineButton.setVisibility(View.GONE);
            } else if (currentDate.after(deadlineDate) && currentDate.before(lotteryDate)) {
                eventStatusValueTextView.setText("Registration Closed - Awaiting Lottery");
                cancelRegistrationButton.setVisibility(View.GONE);
                acceptButton.setVisibility(View.GONE);
                declineButton.setVisibility(View.GONE);
            } else if (currentDate.after(lotteryDate) && currentDate.before(eventEndDate)) {
                checkUserLotteryStatus();
            } else if (currentDate.after(eventEndDate)) {
                // If the current time is after the event time, "Event Ended" will be displayed.
                eventStatusValueTextView.setText("Event Ended");
                cancelRegistrationButton.setVisibility(View.GONE);
                acceptButton.setVisibility(View.GONE);
                declineButton.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks and displays user's lottery status for the event.
     * Updates UI based on status (Winner, Not Selected, Accepted, Declined).
     */
    private void checkUserLotteryStatus() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("registered_events")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userRegistration = querySnapshot.getDocuments().get(0);
                        String userStatus = userRegistration.getString("status");

                        if ("Winner".equals(userStatus)) {
                            eventStatusValueTextView.setText("Lottery Completed - You Won!");
                            acceptButton.setVisibility(View.VISIBLE);
                            declineButton.setVisibility(View.VISIBLE);
                        } else if ("Not Selected".equals(userStatus)) {
                            eventStatusValueTextView.setText("Lottery Completed - Not Selected");
                            acceptButton.setVisibility(View.GONE);
                            declineButton.setVisibility(View.GONE);
                        } else if ("Accepted".equals(userStatus)) {
                            eventStatusValueTextView.setText("You accepted the invitation");
                            acceptButton.setVisibility(View.GONE);
                            declineButton.setVisibility(View.GONE);
                        } else if ("Cancelled".equals(userStatus)) {
                            eventStatusValueTextView.setText("You declined the invitation");
                            acceptButton.setVisibility(View.GONE);
                            declineButton.setVisibility(View.GONE);
                        }
                    } else {
                        eventStatusValueTextView.setText("Lottery Completed - No Participation Found");
                        acceptButton.setVisibility(View.GONE);
                        declineButton.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> Log.e("UserEventDetailActivity", "Error checking user lottery status", e));
    }
    /**
     * Updates user's lottery response status in Firestore.
     *
     * @param status New status ("Accepted" or "Declined")
     */
    private void updateLotteryStatus(String status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("registered_events")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        db.collection("registered_events").document(document.getId())
                                .update("status", status)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Status updated to " + status, Toast.LENGTH_SHORT).show();
                                    checkUserLotteryStatus();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Log.e("UserEventDetailActivity", "Error updating lottery status", e));
    }

    /**
     * Cancels user's event registration.
     * Removes registration record from Firestore.
     *
     * @param eventId ID of event to cancel registration for
     */
    private void cancelRegistration(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("registered_events")
                .whereEqualTo("userId", userId)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            // Get the user's status
                            String status = document.getString("status");

                            // Delete the user from registered_events collection
                            db.collection("registered_events").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Registration canceled", Toast.LENGTH_SHORT).show();

                                        // Call performRedraw if status is "Winner" or "Accepted"
                                        if ("Winner".equals(status) || "Accepted".equals(status)) {
                                            performRedraw(eventId); // Modify the lottery count if necessary
                                        }

                                        // Delete the user's location from user_locations collection
                                        db.collection("user_locations")
                                                .whereEqualTo("userId", userId)
                                                .get()
                                                .addOnSuccessListener(locationQuerySnapshot -> {
                                                    if (!locationQuerySnapshot.isEmpty()) {
                                                        for (DocumentSnapshot locationDocument : locationQuerySnapshot.getDocuments()) {
                                                            db.collection("user_locations").document(locationDocument.getId()).delete()
                                                                    .addOnSuccessListener(aVoid1 -> {
                                                                        Log.d("UserEventDetailActivity", "User location deleted successfully.");
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Toast.makeText(this, "Failed to delete user location", Toast.LENGTH_SHORT).show();
                                                                        Log.e("UserEventDetailActivity", "Error deleting user location", e);
                                                                    });
                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(e -> Log.e("UserEventDetailActivity", "Error querying user location", e));

                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to cancel registration", Toast.LENGTH_SHORT).show());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("UserEventDetailActivity", "Error canceling registration", e));
    }





    private void performRedraw(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // First, get the event name by querying the events collection
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDocumentSnapshot -> {
                    if (eventDocumentSnapshot.exists()) {
                        String eventName = eventDocumentSnapshot.getString("name");

                        // Fetch the registered users who are in the "Waiting" status for this event
                        db.collection("registered_events")
                                .whereEqualTo("eventId", eventId)
                                .whereEqualTo("status", "Waiting")
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    List<DocumentSnapshot> waitingUsers = querySnapshot.getDocuments();
                                    if (!waitingUsers.isEmpty()) {
                                        // Randomly pick one winner from the waiting users
                                        Collections.shuffle(waitingUsers);
                                        DocumentSnapshot winnerDocument = waitingUsers.get(0); // Select a random winner
                                        String userId = winnerDocument.getString("userId");

                                        // Ensure the eventId in the document matches the provided eventId
                                        String eventIdFromDoc = winnerDocument.getString("eventId");
                                        if (eventIdFromDoc != null && eventIdFromDoc.equals(eventId)) {
                                            // Update the status of the selected user to "Winner"
                                            db.collection("registered_events").document(winnerDocument.getId())
                                                    .update("status", "Winner")
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d("LotteryWorker", "User " + userId + " has been selected as a winner.");

                                                        // Optionally send a notification to the winner
                                                        String title = "Event Notification - " + eventName;
                                                        String message = "Congratulations, you have been selected!";

                                                        // Create and send the notification document
                                                        Notification notification = new Notification(title, message, userId);
                                                        db.collection("notifications").add(notification)
                                                                .addOnSuccessListener(documentReference -> {
                                                                    Log.d("LotteryWorker", "Notification sent to user " + userId);
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Log.e("LotteryWorker", "Error sending notification", e);
                                                                });
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e("LotteryWorker", "Error updating status to Winner", e);
                                                    });
                                        } else {
                                            Log.e("LotteryWorker", "User's eventId does not match the selected eventId.");
                                        }
                                    } else {
                                        Log.d("LotteryWorker", "No users found in the waiting list for the event.");
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("LotteryWorker", "Error retrieving registered users", e));
                    } else {
                        Log.e("LotteryWorker", "Event document does not exist.");
                    }
                })
                .addOnFailureListener(e -> Log.e("LotteryWorker", "Error retrieving event name", e));
    }




}