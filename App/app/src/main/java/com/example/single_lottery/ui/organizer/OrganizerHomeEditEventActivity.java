package com.example.single_lottery.ui.organizer;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.single_lottery.R;
import com.example.single_lottery.EventModel;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Activity for editing existing lottery events by organizers.
 * Provides functionality for:
 * - Viewing and editing event details
 * - Updating event poster image
 * - Managing event parameters
 * - Saving changes to Firebase
 *
 * This activity handles:
 * - Loading existing event data from Firestore
 * - Image selection and upload to Firebase Storage
 * - Form validation and data updates
 * - User feedback through Toast messages
 *
 * @author [Jingyao Gu]
 * @version 1.0
 * @see AppCompatActivity
 * @see FirebaseFirestore
 * @see EventModel
 * @since 1.0
 */
public class OrganizerHomeEditEventActivity extends AppCompatActivity {

    private EditText editTextEventName, editTextEventDescription, editTextWaitingListCount, editTextLotteryCount;
    private TextView editTextRegistrationTime, editRegistrationTime, editTextEventTime, editEventTime, editTextLotteryTime,
            editLotteryTime;
    private ImageView imageViewPoster;
    private Button buttonUpdate;
    private String eventId;
    private Switch locationRequirementSwitch;


    /**
     * Initializes the event editing interface and loads existing event data.
     * Sets up:
     * - UI components and their references
     * - Event data loading from Firestore
     * - Click listeners for updates and image selection
     * - Back navigation
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                          this Bundle contains the data it most recently supplied
     */
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_home_edit_event);
        setTitle("Edit Event Details");

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Initializing the View
        editTextEventName = findViewById(R.id.editTextEventName);
        editTextEventDescription = findViewById(R.id.editTextEventDescription);
        editTextEventTime = findViewById(R.id.editEventTime);
        editTextRegistrationTime = findViewById(R.id.editRegistrationTime);
        editTextLotteryTime = findViewById(R.id.editLotteryTime);
        editTextWaitingListCount = findViewById(R.id.editTextWaitingListCount);
        editEventTime = findViewById(R.id.editedEventTime);
        editRegistrationTime = findViewById(R.id.editedRegistrationTime);
        editLotteryTime = findViewById(R.id.editedLotteryTime);
        editTextLotteryCount = findViewById(R.id.editTextLotteryCount);
        imageViewPoster = findViewById(R.id.imageViewPoster);
        buttonUpdate = findViewById(R.id.buttonUpdateEvent);
        locationRequirementSwitch = findViewById(R.id.locationRequirementSwitch);

        Button buttonChangePoster = findViewById(R.id.buttonChangePoster); // Change Poster Button
        buttonChangePoster.setOnClickListener(v -> openImagePicker()); // 设置点击事件

        editTextEventTime.setOnClickListener(v -> showDateTimePicker(editEventTime));
        editTextRegistrationTime.setOnClickListener(v -> showDateTimePicker(editRegistrationTime));
        editTextLotteryTime.setOnClickListener(v -> showDateTimePicker(editLotteryTime));


        // Get the passed event_id
        // String eventId = getIntent().getStringExtra("event_id");
        eventId = getIntent().getStringExtra("event_id");

        // Load activity data and populate
        loadEventData(eventId);

        // Set the update button click event
        buttonUpdate.setOnClickListener(v -> updateEventData(eventId));
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
    /**
     * Opens system image picker for selecting a new event poster.
     * Launches intent for image selection from device storage.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1002); // 1002 is the request code
    }

    /**
     * Handles the result from image picker activity.
     * Updates UI and uploads new image if selection was successful.
     *
     * @param requestCode The request code passed to startActivityForResult()
     * @param resultCode The result code returned by the child activity
     * @param data An Intent that carries the result data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1002 && resultCode == RESULT_OK && data != null) {
            Uri posterUri = data.getData();
            imageViewPoster.setImageURI(posterUri); // Update ImageView to display the new image

            // Upload the image to Firebase or update it to the activity data
            uploadPosterToFirebase(posterUri, eventId);
        }
    }


    /**
     * Uploads new event poster to Firebase Storage and updates event data.
     *
     * @param posterUri URI of the selected poster image
     * @param eventId ID of the event being edited
     */
    private void uploadPosterToFirebase(Uri posterUri, String eventId) {
        if (posterUri != null) {
            StorageReference posterRef = FirebaseStorage.getInstance().getReference().child("event_posters/" + System.currentTimeMillis() + ".jpg");
            posterRef.putFile(posterUri).addOnSuccessListener(taskSnapshot ->
                    posterRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Update the posterUrl field in the activity
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("events").document(eventId).update("posterUrl", uri.toString())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Poster updated successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to update poster", Toast.LENGTH_SHORT).show();
                                });
                    })
            ).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to upload new poster", Toast.LENGTH_SHORT).show()
            );
        }
    }

    /**
     * Reloads event data when activity resumes.
     * Ensures displayed data is current.
     */
    @Override
    protected void onResume() {
        super.onResume();
        String eventId = getIntent().getStringExtra("event_id");  // Get eventId from Intent
        if (eventId != null) {
            loadEventData(eventId);  // Load the data for the specified activity
        }
    }

    /**
     * Loads existing event data from Firestore and populates form fields.
     * Retrieves and displays:
     * - Event details (name, description, times)
     * - Participant limits
     * - Event poster
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
                            editTextEventName.setText(event.getName());
                            editTextEventDescription.setText(event.getDescription());
                            editEventTime.setText(event.getTime());
                            editRegistrationTime.setText(event.getRegistrationDeadline());
                            editLotteryTime.setText(event.getLotteryTime());
                            if (event.getWaitingListCount() != Integer.MAX_VALUE){
                                editTextWaitingListCount.setText(String.valueOf(event.getWaitingListCount()));
                            }
                            editTextLotteryCount.setText(String.valueOf(event.getLotteryCount()));

                            locationRequirementSwitch.setChecked(event.isRequiresLocation());

                            // Check if a poster URL exists
                            if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                                Glide.with(this).load(event.getPosterUrl()).into(imageViewPoster);
                            } else {
                                // If no poster URL, load default image
                                Glide.with(this).load(R.drawable.defaultbackground).into(imageViewPoster);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Error handling
                    Log.e("OrganizerHomeEditEvent", "Failed to load event data", e);
                });
    }


    /**
     * Updates event data in Firestore with current form values.
     * Validates and saves changes to:
     * - Event details
     * - Time settings
     * - Participant limits
     *
     * @param eventId ID of the event to update
     * @throws NumberFormatException if numeric fields contain invalid values
     */
    private void updateEventData(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String editedEventName = editTextEventName.getText().toString();
        String editedEventTime = editEventTime.getText().toString();
        String editedRegistrationDeadline = editRegistrationTime.getText().toString();
        String editedLotteryTime = editLotteryTime.getText().toString();
        String editedWaitingListCount = editTextWaitingListCount.getText().toString().trim();
        String editedLotteryCount = editTextLotteryCount.getText().toString().trim();
        String editedEventDescription = editTextEventDescription.getText().toString();
        int waitingListCount;
        int lotteryCount;
        try {
            // Set default values for waiting list and lottery numbers
            waitingListCount = editedWaitingListCount.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(editedWaitingListCount);
            lotteryCount = editedLotteryCount.isEmpty() ? waitingListCount : Integer.parseInt(editedLotteryCount);

            if (editedLotteryCount.isEmpty()){
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

        if(!(editedRegistrationDeadline.compareTo(editedLotteryTime) < 0)){
            Toast.makeText(this, "Time of lottery must come after the registration deadline.", Toast.LENGTH_SHORT).show();
            Log.d("OrganizerEventCreateActivity", "Tried to make lottery date come before the registration date for the event.");
            return;
        }

        if(!(editedLotteryTime.compareTo(editedEventTime) < 0)){
            Toast.makeText(this, "Time of event must come after the time of the lottery.", Toast.LENGTH_SHORT).show();
            Log.d("OrganizerEventCreateActivity", "Tried to make event time come before time of the lottery.");
            return;
        }

        db.collection("events").document(eventId).update(
                "name", editedEventName,
                "time", editedEventTime,
                "registrationDeadline", editedRegistrationDeadline,
                "lotteryTime", editedLotteryTime,
                "waitingListCount", waitingListCount,
                "lotteryCount", lotteryCount,
                "description", editedEventDescription,
                "requiresLocation", locationRequirementSwitch.isChecked()

        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Event successfully updated!", Toast.LENGTH_SHORT).show();

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "There was an error updating your event.", Toast.LENGTH_SHORT).show();
        });
    }
}