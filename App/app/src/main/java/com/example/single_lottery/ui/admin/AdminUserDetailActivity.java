package com.example.single_lottery.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
/**
 * Activity for displaying and managing user details in admin view.
 * Allows viewing user information and handling profile/avatar deletion.
 *
 * @author Jingyao Gu
 * @version 1.0
 */
public class AdminUserDetailActivity extends AppCompatActivity {
    /** Key for passing user data through intent */
    public static final String EXTRA_USER = "extra_user";
    /**
     * Initializes the activity, sets up UI components and loads user details.
     * Configures buttons for avatar and profile deletion.
     *
     * @param savedInstanceState Saved instance state bundle
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_user_detail);

        // Get the passed user data
        EventModel user = (EventModel) getIntent().getSerializableExtra(EXTRA_USER);

        // Binding UI Elements
        ImageView userProfileImage = findViewById(R.id.userProfileImage);
        TextView userName = findViewById(R.id.userName);
        TextView userEmail = findViewById(R.id.userEmail);
        TextView userPhone = findViewById(R.id.userPhone);
        Button btnDeleteAvatar = findViewById(R.id.btnDeleteAvatar);
        Button btnDeleteProfile = findViewById(R.id.btnDeleteProfile);

        if (user != null) {
            userName.setText(user.getName());
            userEmail.setText(String.format("Email: %s", user.getEmail()));
            userPhone.setText(String.format("Phone: %s", user.getPhone()));

            // Loading avatar
            Glide.with(this)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(userProfileImage);

            // Delete the avatar button logicv
            btnDeleteAvatar.setOnClickListener(v -> deleteAvatar(user));

            // Delete User Button Logic
            btnDeleteProfile.setOnClickListener(v -> deleteProfile(user));
        }
    }
    /**
     * Handles action bar item selections, specifically back navigation.
     *
     * @param item The selected menu item
     * @return true if the event was handled, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { // The ID of the back button
            onBackPressed(); // return to previous page
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Deletes user's profile after confirmation.
     * Removes user data from Firestore database.
     * Shows confirmation dialog before deletion.
     *
     * @param user The user to be deleted
     */
    private void deleteProfile(EventModel user) {
        if (user.getEventId() == null) {
            Toast.makeText(this, "User ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(user.getEventId());

        // Prompt to confirm deletion
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    userRef.delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "User deleted successfully!", Toast.LENGTH_SHORT).show();
                                finish(); // Close the current page
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }


    /**
     * Deletes user's avatar image.
     * Removes profile image URL from database and shows default avatar.
     *
     * @param user The user whose avatar should be deleted
     */
    private void deleteAvatar(EventModel user) {
        // Check if eventId is null
        if (user.getEventId() == null) {
            Toast.makeText(this, "User ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(user.getEventId());

        userRef.update("profileImageUrl", null)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Avatar deleted successfully!", Toast.LENGTH_SHORT).show();
                    ImageView userProfileImage = findViewById(R.id.userProfileImage);
                    userProfileImage.setImageResource(R.drawable.ic_profile);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete avatar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}