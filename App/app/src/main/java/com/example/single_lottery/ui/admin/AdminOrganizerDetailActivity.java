package com.example.single_lottery.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.single_lottery.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
/**
 * Activity for displaying and managing organizer details in admin view.
 * Allows viewing organizer information and handling profile/avatar deletion.
 *
 * @author Jingyao Gu
 * @version 1.0
 */
public class AdminOrganizerDetailActivity extends AppCompatActivity {

    private String organizerId;
    private String profileImageUrl;
    /**
     * Initializes the activity, sets up UI components and loads organizer details.
     * Configures buttons for avatar and profile deletion.
     *
     * @param savedInstanceState Saved instance state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_organizer_detail);

        // Set the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get organizer details from Intent
        organizerId = getIntent().getStringExtra("organizerId");
        String organizerName = getIntent().getStringExtra("organizerName");
        String organizerEmail = getIntent().getStringExtra("organizerEmail");
        String organizerPhone = getIntent().getStringExtra("organizerPhone");
        profileImageUrl = getIntent().getStringExtra("organizerProfileImageUrl");

        // Initialize UI components
        ImageButton backButton = findViewById(R.id.adminOrganizerBackButton);
        ImageView profileImageView = findViewById(R.id.adminOrganizerProfileImage);
        TextView textViewName = findViewById(R.id.adminOrganizerName);
        TextView textViewEmail = findViewById(R.id.adminOrganizerEmail);
        TextView textViewPhone = findViewById(R.id.adminOrganizerPhone);
        Button deleteAvatarButton = findViewById(R.id.adminDeleteAvatar);
        Button deleteProfileButton = findViewById(R.id.adminOrganizerDeleteProfile);

        // Set organizer details
        textViewName.setText(organizerName);
        textViewEmail.setText("Email: " + organizerEmail);
        textViewPhone.setText("Phone: " + organizerPhone);

        // Load profile image
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(this).load(profileImageUrl).into(profileImageView);
        } else {
            imageViewProfile.setImageResource(R.drawable.ic_profile); // Replace with default avatar resource
        }

        // Delete avatar button click event
        buttonDeleteAvatar.setOnClickListener(v -> deleteAvatar());

        // Delete the Profile button click event
        buttonDeleteProfile.setOnClickListener(v -> deleteProfile());
    }
    /**
     * Handles up navigation, returning to previous screen.
     *
     * @return true if up navigation handled successfully
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Return to previous page
        return true;
    }
    /**
     * Deletes organizer's avatar image.
     * Removes image from storage and updates database reference.
     * Shows default avatar after successful deletion.
     */
    private void deleteAvatar() {
        if (profileImageUrl == null || profileImageUrl.isEmpty()) {
            Toast.makeText(this, "No avatar to delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference profileImageRef = storage.getReferenceFromUrl(profileImageUrl);

        profileImageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("AdminOrganizerDetail", "Avatar deleted successfully");
                    FirebaseFirestore.getInstance().collection("organizers")
                            .document(organizerId)
                            .update("profileImageUrl", null)
                            .addOnSuccessListener(unused -> {
                                Log.d("AdminOrganizerDetail", "Avatar URL removed from Firestore");
                                Toast.makeText(this, "Avatar deleted successfully", Toast.LENGTH_SHORT).show();
                                ImageView profileImageView = findViewById(R.id.adminOrganizerProfileImage);
                                profileImageView.setImageResource(R.drawable.ic_profile);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("AdminOrganizerDetail", "Failed to update Firestore", e);
                                Toast.makeText(this, "Failed to update database.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminOrganizerDetail", "Failed to delete avatar", e);
                    Toast.makeText(this, "Failed to delete avatar.", Toast.LENGTH_SHORT).show();
                });
    }
    /**
     * Deletes organizer's entire profile.
     * Removes profile data from Firestore database.
     * Closes activity after successful deletion.
     */
    private void deleteProfile() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("organizers").document(organizerId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("AdminOrganizerDetail", "Profile deleted successfully");
                    Toast.makeText(this, "Profile deleted successfully.", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminOrganizerDetail", "Failed to delete profile", e);
                    Toast.makeText(this, "Failed to delete profile.", Toast.LENGTH_SHORT).show();
                });
    }
}