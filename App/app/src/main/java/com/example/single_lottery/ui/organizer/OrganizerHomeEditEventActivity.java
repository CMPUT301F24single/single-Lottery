package com.example.single_lottery.ui.organizer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.single_lottery.R;
import com.example.single_lottery.EventModel;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

    private EditText editTextEventName, editTextEventDescription, editTextEventTime, editTextRegistrationDeadline,
            editTextLotteryTime, editTextWaitingListCount, editTextLotteryCount;
    private ImageView imageViewPoster;
    private Button buttonUpdate;
    private String eventId;
    private EditText editTextEventFacility; // new
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_home_edit_event);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Initializing the View
        editTextEventName = findViewById(R.id.editTextEventName);
        editTextEventDescription = findViewById(R.id.editTextEventDescription);
        editTextEventFacility = findViewById(R.id.editTextEventFacility); // new
        editTextEventTime = findViewById(R.id.editTextEventTime);
        editTextRegistrationDeadline = findViewById(R.id.editTextRegistrationDeadline);
        editTextLotteryTime = findViewById(R.id.editTextLotteryTime);
        editTextWaitingListCount = findViewById(R.id.editTextWaitingListCount);
        editTextLotteryCount = findViewById(R.id.editTextLotteryCount);
        imageViewPoster = findViewById(R.id.imageViewPoster);
        buttonUpdate = findViewById(R.id.buttonUpdateEvent);
        locationRequirementSwitch = findViewById(R.id.locationRequirementSwitch);

        Button buttonChangePoster = findViewById(R.id.buttonChangePoster); // Change Poster Button
        buttonChangePoster.setOnClickListener(v -> openImagePicker()); // 设置点击事件


        // Get the passed event_id
        // String eventId = getIntent().getStringExtra("event_id");
        eventId = getIntent().getStringExtra("event_id");

        // Load activity data and populate
        loadEventData(eventId);

        // Set the update button click event
        buttonUpdate.setOnClickListener(v -> updateEventData(eventId));
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

            //Upload the image to Firebase or update it to the activity data
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
                            editTextEventTime.setText(event.getTime());
                            editTextRegistrationDeadline.setText(event.getRegistrationDeadline());
                            editTextLotteryTime.setText(event.getLotteryTime());
                            editTextWaitingListCount.setText(String.valueOf(event.getWaitingListCount()));
                            editTextLotteryCount.setText(String.valueOf(event.getLotteryCount()));

                            if (event.getFacility() != null) {
                                editTextEventFacility.setText(event.getFacility());
                            }

                            locationRequirementSwitch.setChecked(event.isRequiresLocation());

                            if (event.getPosterUrl() != null) {
                                Glide.with(this).load(event.getPosterUrl()).into(imageViewPoster);
                            }
                        }
                    }

                })

                .addOnFailureListener(e -> {
                    // Error handling
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
        db.collection("events").document(eventId).update(
                "name", editTextEventName.getText().toString(),
                "time", editTextEventTime.getText().toString(),
                "registrationDeadline", editTextRegistrationDeadline.getText().toString(),
                "lotteryTime", editTextLotteryTime.getText().toString(),
                "waitingListCount", Integer.parseInt(editTextWaitingListCount.getText().toString()),
                "lotteryCount", Integer.parseInt(editTextLotteryCount.getText().toString()),
                "description", editTextEventDescription.getText().toString(),
                "facility", editTextEventFacility.getText().toString(), // new
                "requiresLocation", locationRequirementSwitch.isChecked()

        ).addOnSuccessListener(aVoid -> {
            // Update successful prompt
        }).addOnFailureListener(e -> {
            // Update failure handling
        });
    }
}