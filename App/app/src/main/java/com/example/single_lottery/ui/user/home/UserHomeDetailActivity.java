package com.example.single_lottery.ui.user.home;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.single_lottery.MapsActivity;
import com.example.single_lottery.R;
import com.example.single_lottery.EventModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for displaying event details and handling user registration.
 * Shows event information and allows users to sign up before deadline.
 *
 * @author [Jingyao Gu]
 * @version 1.0
 */
public class UserHomeDetailActivity extends AppCompatActivity {

    private TextView textViewEventName, textViewEventDescription, textViewEventTime,
            textViewRegistrationDeadline, textViewLotteryTime,
            textViewWaitingListCount, textViewLotteryCount;
    private ImageView imageViewPoster;
    private ImageButton backButton;
    private Button buttonSignUp;
    private String eventId;
    private String registrationDeadline;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home_event_detail);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Get the passed event_id
        eventId = getIntent().getStringExtra("event_id");
        if (eventId == null) {
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textViewEventName = findViewById(R.id.textViewEventName);
        textViewEventDescription = findViewById(R.id.textViewEventDescription);
        textViewEventTime = findViewById(R.id.textViewEventTime);
        textViewRegistrationDeadline = findViewById(R.id.textViewRegistrationDeadline);
        textViewLotteryTime = findViewById(R.id.textViewLotteryTime);
        textViewWaitingListCount = findViewById(R.id.textViewWaitingListCount);
        textViewLotteryCount = findViewById(R.id.textViewLotteryCount);
        imageViewPoster = findViewById(R.id.imageViewPoster);

        loadEventData(eventId);

        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonSignUp.setOnClickListener(v -> signUpForEvent());
    }

    /**
     * Loads event details from Firestore and updates UI.
     * Retrieves event data including name, description, times and registration counts.
     *
     * @param eventId ID of event to load
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
                            textViewLotteryCount.setText(String.valueOf(event.getLotteryCount()));

                            registrationDeadline = event.getRegistrationDeadline(); // Store Registration Deadline

                            if (event.getPosterUrl() != null) {
                                Glide.with(this).load(event.getPosterUrl()).into(imageViewPoster);
                            }

                            // Get real-time registration number
                            countRegistrations(eventId, event.getWaitingListCount());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                });
    }

    /**
     * Updates registration count display.
     * Shows current/maximum registration ratio.
     *
     * @param eventId             Event to count registrations for
     * @param maxWaitingListCount Maximum allowed registrations
     */
    private void countRegistrations(String eventId, int maxWaitingListCount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the number of registration records that meet the eventId condition in the registered_events collection
        db.collection("registered_events")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int currentSignUpCount = queryDocumentSnapshots.size(); // The number of documents obtained is the number of applicants

                    // Set the format of "Number of registrations/maximum number"
                    textViewWaitingListCount.setText(currentSignUpCount + "/" + maxWaitingListCount);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load registration count.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Handles event registration process.
     * Validates registration deadline and checks for duplicate registration.
     * Creates registration record in Firestore if validation passes.
     *
     * @throws ParseException If registration deadline date parsing fails
     */
    private void signUpForEvent() {
        try {
            // Check if you are within the registration deadline
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date deadlineDate = dateFormat.parse(registrationDeadline);
            Date currentDate = new Date();

            if (currentDate.after(deadlineDate)) {
                // If the registration deadline has passed, display a reminder message and exit
                Toast.makeText(this, "Registration is closed. Sign-up is not allowed.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Registration Logic
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            // Create a registration record in the registered_events collection
            Map<String, Object> registrationData = new HashMap<>();
            registrationData.put("eventId", eventId);
            registrationData.put("userId", userId);
            registrationData.put("timestamp", System.currentTimeMillis()); // Optional: Record the registration time

            // Check if the user has registered
            db.collection("registered_events")
                    .whereEqualTo("eventId", eventId)
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            // The user has not registered yet, add a registration record
                            db.collection("registered_events").add(registrationData)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(this, "Successfully signed up for the event!", Toast.LENGTH_SHORT).show();
                                        buttonSignUp.setEnabled(false);
                                        loadEventData(eventId); // Update page display
                                        
                                        getUserLocation(eventId, userId);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to sign up. Please try again.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "You have already signed up for this event.", Toast.LENGTH_SHORT).show();
                            buttonSignUp.setEnabled(false);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to check registration status. Please try again.", Toast.LENGTH_SHORT).show();
                    });

        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing registration deadline", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation(eventId, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
            } else {
                Toast.makeText(this, "Location permission is required to get your location.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void getUserLocation(String eventId, String userId) {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            android.location.Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    double latitude = lastKnownLocation.getLatitude();
                    double longitude = lastKnownLocation.getLongitude();

                    Log.d("UserLocation", "Latitude: " + latitude + ", Longitude: " + longitude);

                    saveUserLocation(eventId, userId, latitude, longitude);
                } else {
                    Toast.makeText(this, "Failed to get your location.", Toast.LENGTH_SHORT).show();
                }
        } else {
            Toast.makeText(this, "Location permission is required.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserLocation(String eventId, String userId, double latitude, double longitude) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("eventId", eventId);
        locationData.put("userId", userId);
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);

        db.collection("user_locations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("user_locations").document(docId)
                                .update(locationData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Location updated!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to update location.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        db.collection("user_locations").add(locationData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "Location saved!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to save location.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to query user location.", Toast.LENGTH_SHORT).show();
                });
    }
}