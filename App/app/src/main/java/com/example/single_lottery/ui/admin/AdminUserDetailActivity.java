package com.example.single_lottery.ui.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.Settings;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Activity for displaying and managing user details in admin view.
 * Allows viewing user information and handling profile/avatar deletion.
 * Manages user data display and Firestore operations.
 *
 * @author [Jingyao Gu]
 * @author [Aaron kim]
 * @version 1.0
 */
public class AdminUserDetailActivity extends AppCompatActivity {
    /** Key for passing user data through intent */
    public static final String EXTRA_USER = "extra_user";
    /**
     * Initializes the activity and sets up UI components.
     * Loads user data from intent and configures button listeners.
     * Handles profile image loading and display.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_user_detail);
        setTitle("User Details");

        // Get the passed user data
        EventModel user = (EventModel) getIntent().getSerializableExtra(EXTRA_USER);

        // Bind UI elements
        ImageView userProfileImage = findViewById(R.id.userProfileImage);
        TextView userName = findViewById(R.id.userName);
        TextView userEmail = findViewById(R.id.userEmail);
        TextView userPhone = findViewById(R.id.userPhone);
        Button btnDeleteAvatar = findViewById(R.id.btnDeleteAvatar);
        Button btnDeleteProfile = findViewById(R.id.btnDeleteProfile);
        ImageView backButton = findViewById(R.id.adminUserBackButton);  // Back button reference

        if (user != null) {
            userName.setText(user.getName());
            userEmail.setText(String.format("Email: %s", user.getEmail()));
            userPhone.setText(String.format("Phone: %s", user.getPhone()));

            // Get the profile image URL from the user model (Firestore data)
            String profileImageUrl = user.getProfileImageUrl();  // Assuming Firestore data is being passed correctly here

            // Check if the profile image URL is valid
            if (profileImageUrl == null || profileImageUrl.isEmpty()) {
                // If profile image URL is null or empty, generate the letter avatar
                generateLetterAvatar(user.getName(), userProfileImage);
            } else {
                // If profile image URL is available, load it using Glide
                Glide.with(this)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.ic_profile)  // Placeholder image while loading
                        .into(userProfileImage);
            }

            // Delete avatar button logic
            btnDeleteAvatar.setOnClickListener(v -> deleteAvatar(user));

            // Delete user button logic
            btnDeleteProfile.setOnClickListener(v -> deleteProfile(user));
        }

        // Set click listener for the back button
        backButton.setOnClickListener(v -> finish());
    }
    /**
     * Handles action bar item selections, specifically back navigation.
     *
     * @param item The selected menu item
     * @return true if the event was handled, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { // 返回按钮的 ID
            onBackPressed(); // 返回上一页
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
        String currentUID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(user.getEventId());

        // Prompt to confirm deletion
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Query query = db.collection("registered_events")
                            .whereEqualTo("userId", currentUID);

                    query.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    // Delete each document
                                    db.collection("registered_events").document(document.getId()).delete()
                                            .addOnSuccessListener(aVoid -> {
                                                System.out.println("Document successfully deleted!");
                                            })
                                            .addOnFailureListener(e -> {
                                                System.err.println("Error deleting document: " + e.getMessage());
                                            });
                                }
                            } else {
                                System.out.println("No documents found with eventId: " + currentUID);
                            }
                        } else {
                            System.err.println("Query failed: " + task.getException().getMessage());
                        }
                    });
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
        //Check if eventId is null
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

    /**
     * Generates a letter avatar when no profile image is set.
     * Creates a circular avatar with user initials.
     *
     * @param name User's display name for initial generation
     */
    private void generateLetterAvatar(String name, ImageView profileImageView) {
        String initials = "";
        if (name == null || name.isEmpty()) {
            initials += '-';
        } else {
            String[] nameParts = name.split("\\s+");
            if (nameParts.length > 0) {
                initials += nameParts[0].charAt(0); // First letter of first name
            }
            if (nameParts.length > 1) {
                initials += nameParts[1].charAt(0); // First letter of last name
            }
        }

        // Create a Bitmap with 100x100 size (for circular avatar)
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw a circular background
        Paint paint = new Paint();
        paint.setColor(Color.GRAY); // Background color
        canvas.drawCircle(50, 50, 50, paint); // Circle at center (50, 50) with radius 50

        // Draw initials in the center of the circle
        paint.setColor(Color.WHITE); // Text color
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER); // Center-align text
        canvas.drawText(initials, 50, 65, paint); // Draw the initials at position (50, 65)

        // Set the generated bitmap as the profile image
        profileImageView.setImageBitmap(bitmap);
    }
}
