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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_event_viewdetail);

        // Bind views
        Button backButton = findViewById(R.id.backButton);
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
        cancelRegistrationButton = findViewById(R.id.cancelRegistrationButton);
        acceptButton = findViewById(R.id.acceptButton);
        declineButton = findViewById(R.id.declineButton);

        eventId = getIntent().getStringExtra("event_id");
        userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        loadEventDetails(eventId);

        cancelRegistrationButton.setOnClickListener(v -> cancelRegistration(eventId));
        acceptButton.setOnClickListener(v -> updateLotteryStatus("Accepted"));
        declineButton.setOnClickListener(v -> updateLotteryStatus("Declined"));
    }

    private void loadEventDetails(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String eventName = documentSnapshot.getString("name");
                        String eventDescription = documentSnapshot.getString("description");
                        eventTime = documentSnapshot.getString("time"); // 获取活动时间
                        registrationDeadline = documentSnapshot.getString("registrationDeadline");
                        lotteryTime = documentSnapshot.getString("lotteryTime");
                        String waitingListCount = documentSnapshot.getLong("waitingListCount") + "";
                        String lotteryCount = documentSnapshot.getLong("lotteryCount") + "";

                        eventNameTextView.setText(eventName);
                        eventTimeTextView.setText(eventTime);
                        registrationDeadlineTextView.setText(registrationDeadline);
                        lotteryTimeTextView.setText(lotteryTime);
                        waitingListCountTextView.setText(waitingListCount);
                        lotteryCountTextView.setText(lotteryCount);
                        eventDescriptionTextView.setText(eventDescription);

                        String posterUrl = documentSnapshot.getString("posterUrl");
                        if (posterUrl != null && !posterUrl.isEmpty()) {
                            Glide.with(this).load(posterUrl).into(eventPosterImageView);
                        }

                        updateEventStatus();
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("UserEventDetailActivity", "Error loading event details", e));
    }

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
                // 如果当前时间在活动时间之后，则显示“活动结束”
                eventStatusValueTextView.setText("Event Ended");
                cancelRegistrationButton.setVisibility(View.GONE);
                acceptButton.setVisibility(View.GONE);
                declineButton.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                        } else if ("Declined".equals(userStatus)) {
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

    private void cancelRegistration(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("registered_events")
                .whereEqualTo("userId", userId)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            db.collection("registered_events").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Registration canceled", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to cancel registration", Toast.LENGTH_SHORT).show());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("UserEventDetailActivity", "Error canceling registration", e));
    }
}