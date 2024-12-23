package com.example.single_lottery.ui.organizer;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.single_lottery.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for creating new lottery events by organizers.
 * This activity provides functionality for:
 * - Event information input (name, description, times)
 * - Event poster upload
 * - Participant capacity settings
 * - Event creation and submission to Firebase
 *
 * The activity handles:
 * - Image selection and upload to Firebase Storage
 * - Date and time selection through dialogs
 * - Form validation and data submission to Firestore
 * - User feedback through Toast messages
 *
 * @author [Jingyao Gu]
 * @version 1.0
 * @see AppCompatActivity
 * @see FirebaseFirestore
 * @see StorageReference
 * @since 1.0
 */

public class OrganizerEventCreateActivity extends AppCompatActivity {

    /**
     * Initializes the event creation interface and sets up all necessary components.
     * Configures:
     * - UI elements binding
     * - Firebase instances
     * - Click listeners for date/time selection
     * - Image upload functionality
     * - Event submission handling
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                          this Bundle contains the data it most recently supplied
     */

    private ImageView eventPosterImageView;
    private EditText eventNameEditText, eventDescriptionEditText, waitingListCountEditText, lotteryCountEditText;
    private TextView eventTimeTextView, registrationDeadlineTextView, lotteryTimeTextView, selectedEventTimeTextView, selectedRegistrationDeadlineTextView, selectedLotteryTimeTextView;  // 改为 TextView
    private Uri posterUri;
    private EditText eventFacilityEditText; // New
    private Switch locationRequirementSwitch;
    private String installationId;
    private String facility;

    private FirebaseFirestore db;
    private StorageReference storageRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_create_fragment);

        FirebaseInstallations.getInstance().getId()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String installationId = task.getResult();
                        FirebaseFirestore.getInstance()
                                .collection("facilities")
                                .document(installationId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        facility = documentSnapshot.getString("name");
                                        Log.d("ProfileFragment", "Facility Name: " + facility);
                                    } else {
                                        Log.d("ProfileFragment", "No such document found.");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ProfileFragment", "Error fetching document: " + e.getMessage());
                                });

                    } else {
                        Log.e("ProfileFragment", "Failed to get installation id: " + task.getException());
                    }
                });

        setTitle("Create Event");

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Initialize the Firebase instance
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference().child("event_posters");

        // Binding Views
        eventPosterImageView = findViewById(R.id.eventPosterImageView);
        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
        eventTimeTextView = findViewById(R.id.eventTimeTextView);  // 改为 TextView
        registrationDeadlineTextView = findViewById(R.id.registrationDeadlineTextView);  // 改为 TextView
        waitingListCountEditText = findViewById(R.id.waitingListCountEditText);
        lotteryCountEditText = findViewById(R.id.lotteryCountEditText);
        lotteryTimeTextView = findViewById(R.id.lotteryTimeTextView);  // 改为 TextView
        selectedEventTimeTextView = findViewById(R.id.selectedEventTimeTextView);  // 改为 TextView
        selectedRegistrationDeadlineTextView = findViewById(R.id.selectedRegistrationDeadlineTextView);  // 改为 TextView
        selectedLotteryTimeTextView = findViewById(R.id.selectedLotteryTimeTextView);  // 改为 TextView
        locationRequirementSwitch = findViewById(R.id.locationRequirementSwitch);


        Button uploadPosterButton = findViewById(R.id.uploadPosterButton);
        Button createEventButton = findViewById(R.id.createEventButton);

        uploadPosterButton.setOnClickListener(v -> openImagePicker());
        createEventButton.setOnClickListener(v -> uploadEventToFirebase());

        // Set a click event to pop up a date and time picker
        eventTimeTextView.setOnClickListener(v -> showDateTimePicker(selectedEventTimeTextView));
        registrationDeadlineTextView.setOnClickListener(v -> showDateTimePicker(selectedRegistrationDeadlineTextView));
        lotteryTimeTextView.setOnClickListener(v -> showDateTimePicker(selectedLotteryTimeTextView));
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            posterUri = data.getData();
            eventPosterImageView.setImageURI(posterUri);
        }
    }

    private void showDateTimePicker(final TextView textView) {
        final Calendar currentDate = Calendar.getInstance();
        final Calendar date = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            date.set(year, monthOfYear, dayOfMonth);
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                // Check if the selected time is before the current time on the same day
                if (year == currentDate.get(Calendar.YEAR) && monthOfYear == currentDate.get(Calendar.MONTH) && dayOfMonth == currentDate.get(Calendar.DAY_OF_MONTH) &&
                        (hourOfDay < currentDate.get(Calendar.HOUR_OF_DAY) || (hourOfDay == currentDate.get(Calendar.HOUR_OF_DAY) && minute < currentDate.get(Calendar.MINUTE)))) {
                    // If the selected time is before current time, use current time
                    hourOfDay = currentDate.get(Calendar.HOUR_OF_DAY);
                    minute = currentDate.get(Calendar.MINUTE);
                }
                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                date.set(Calendar.MINUTE, minute);
                // Format and set date and time
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                textView.setText(sdf.format(date.getTime()));
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false);
            timePickerDialog.show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));
        // Disable dates before the current date
        datePickerDialog.getDatePicker().setMinDate(currentDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void uploadEventToFirebase() {
        // Check if a required field is empty
        Log.d("OrganizerEventCreateActivity", "uploadEventToFirebase() called");

        String eventName = eventNameEditText.getText().toString().trim();
        String eventTime = selectedEventTimeTextView.getText().toString().trim();
        String registrationDeadline = selectedRegistrationDeadlineTextView.getText().toString().trim();
        String lotteryTime = selectedLotteryTimeTextView.getText().toString().trim();
        String eventDescription = eventDescriptionEditText.getText().toString().trim();
        boolean requiresLocation = locationRequirementSwitch.isChecked();

        // Get device code
        String organizerDeviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Check required fields
        if (eventName.isEmpty() || eventTime.isEmpty() || registrationDeadline.isEmpty() || lotteryTime.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            Log.d("OrganizerEventCreateActivity", "Required fields are missing");
            return;
        }

        int waitingListCount;
        int lotteryCount;

        try {
            String waitingListStr = waitingListCountEditText.getText().toString().trim();
            String lotteryCountStr = lotteryCountEditText.getText().toString().trim();

            // Set default values for waiting list and lottery numbers
            waitingListCount = waitingListStr.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(waitingListStr);
            lotteryCount = lotteryCountStr.isEmpty() ? waitingListCount : Integer.parseInt(lotteryCountStr);

            if (lotteryCountStr.isEmpty()){
                Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the number of tickets is less than or equal to the number of waiting lists
            if (lotteryCount > waitingListCount) {
                Toast.makeText(this, "Lottery count cannot exceed waiting list count", Toast.LENGTH_SHORT).show();
                Log.d("OrganizerEventCreateActivity", "Lottery count exceeds waiting list count");
                return;
            }
            if(lotteryCount <= 0){
                Toast.makeText(this, "Invalid lottery count.", Toast.LENGTH_SHORT).show();
                Log.d("OrganizerEventCreateActivity", "Lottery count was <= 0.");
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for waiting list and lottery count", Toast.LENGTH_SHORT).show();
            Log.d("OrganizerEventCreateActivity", "Invalid number format for waiting list or lottery count");
            return;
        }

        if(!(registrationDeadline.compareTo(lotteryTime) < 0)){
            Toast.makeText(this, "Time of lottery must come after the registration deadline.", Toast.LENGTH_SHORT).show();
            Log.d("OrganizerEventCreateActivity", "Tried to make lottery date come before the registration date for the event.");
            return;
        }

        if(!(lotteryTime.compareTo(eventTime) < 0)){
            Toast.makeText(this, "Time of event must come after the time of the lottery.", Toast.LENGTH_SHORT).show();
            Log.d("OrganizerEventCreateActivity", "Tried to make event time come before time of the lottery.");
            return;
        }

        // Check Poster Upload
        if (posterUri != null) {
            StorageReference posterRef = storageRef.child(System.currentTimeMillis() + ".jpg");
            Log.d("OrganizerEventCreateActivity", "Uploading poster to Firebase Storage");
            posterRef.putFile(posterUri).addOnSuccessListener(taskSnapshot ->
                    posterRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d("OrganizerEventCreateActivity", "Poster uploaded, URL: " + uri.toString());
                        saveEventData(uri.toString(), eventName, eventTime, facility, registrationDeadline, lotteryTime, waitingListCount, lotteryCount, organizerDeviceID, eventDescription, requiresLocation);
                    })
            ).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to upload poster", Toast.LENGTH_SHORT).show();
                Log.d("OrganizerEventCreateActivity", "Poster upload failed: " + e.getMessage());
            });
        } else {
            Log.d("OrganizerEventCreateActivity", "No poster, saving event data directly");
            saveEventData(null, eventName, eventTime, facility, registrationDeadline, lotteryTime, waitingListCount, lotteryCount, organizerDeviceID, eventDescription, requiresLocation);
        }
    }

    private void saveEventData(String posterUrl, String eventName, String eventTime, String facility,
                               String registrationDeadline, String lotteryTime,
                               int waitingListCount, int lotteryCount, String organizerDeviceID, String eventDescription, boolean requiresLocation) {
        Map<String, Object> event = new HashMap<>();
        event.put("name", eventName);
        event.put("time", eventTime);
        event.put("facility", facility);
        event.put("registrationDeadline", registrationDeadline);
        event.put("lotteryTime", lotteryTime);
        event.put("waitingListCount", waitingListCount);
        event.put("lotteryCount", lotteryCount);
        event.put("posterUrl", posterUrl);
        event.put("organizerDeviceID", organizerDeviceID);
        event.put("description", eventDescription);
        event.put("drawnStatus", false);
        event.put("requiresLocation", requiresLocation);

        db.collection("events").add(event).addOnSuccessListener(documentReference -> {
            Toast.makeText(this, "Event created successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(OrganizerEventCreateActivity.this, OrganizerActivity.class);
            startActivity(intent);
            finish(); // Optionally close this activity after redirecting
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to create event", Toast.LENGTH_SHORT).show();
        });

    }
}
