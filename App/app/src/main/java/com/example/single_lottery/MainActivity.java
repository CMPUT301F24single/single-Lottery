package com.example.single_lottery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.single_lottery.ui.admin.AdminActivity;
import com.example.single_lottery.ui.organizer.OrganizerActivity;
import com.example.single_lottery.ui.user.UserActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.installations.FirebaseInstallations;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private boolean showLandingScreen;
    private static final int REQUEST_CODE = 100;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance(); // Initialize Firestore
        mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth

        // do OfflineScheduler on boot up
        OneTimeWorkRequest offlineWorkRequest = new OneTimeWorkRequest.Builder(OfflineWorker.class)
                .build();
        // Enqueue the work request
        WorkManager.getInstance(this).enqueue(offlineWorkRequest);

        OfflineScheduler();

        showLandingScreen = getIntent().getBooleanExtra("showLandingScreen", true);

        if (showLandingScreen) {
            setContentView(R.layout.activity_landing);
            saveUID();  // Save UID
            Button buttonUser = findViewById(R.id.button_user);
            Button buttonOrganizer = findViewById(R.id.button_organizer);
            Button buttonAdmin = findViewById(R.id.button_admin);
            ImageButton notificationButton = findViewById(R.id.notificationButton);  // Add reference to the notification button

            View.OnClickListener listener = v -> {
                if (v.getId() == R.id.button_admin) {
                    showAdminLoginPopup();
                } else {
                    Intent intent = new Intent(MainActivity.this,
                            v.getId() == R.id.button_user ? UserActivity.class : OrganizerActivity.class);
                    intent.putExtra("showLandingScreen", false);
                    startActivity(intent);
                    finish();
                }
            };

            buttonUser.setOnClickListener(listener);
            buttonOrganizer.setOnClickListener(listener);
            buttonAdmin.setOnClickListener(listener);

            // Add click listener for the notificationButton
            notificationButton.setOnClickListener(v -> {
                // Create the AlertDialog with ToggleButton and OK button
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Notification Settings");

                // Create a ToggleButton
                final ToggleButton toggleButton = new ToggleButton(MainActivity.this);
                toggleButton.setTextOn("Notifications On");
                toggleButton.setTextOff("Notifications Off");

                // Retrieve the current notification setting
                FirebaseInstallations.getInstance().getId()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String installationId = task.getResult();  // Get Installation ID
                                Log.d("Installation ID", "Installation ID: " + installationId);

                                // Check if notification preference exists in Firestore
                                db.collection("users")
                                        .document(installationId)
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                // Get the notification preference from Firestore
                                                Boolean notificationsEnabled = documentSnapshot.getBoolean("notificationsEnabled");
                                                if (notificationsEnabled != null) {
                                                    toggleButton.setChecked(notificationsEnabled);
                                                } else {
                                                    // Fallback to SharedPreferences if no preference exists in Firestore
                                                    SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
                                                    boolean savedPreference = sharedPreferences.getBoolean("notifications_enabled", true);  // Default to true if not set
                                                    toggleButton.setChecked(savedPreference);
                                                }
                                            } else {
                                                // If the document doesn't exist, use SharedPreferences as fallback
                                                SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
                                                boolean savedPreference = sharedPreferences.getBoolean("notifications_enabled", true);  // Default to true if not set
                                                toggleButton.setChecked(savedPreference);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d("Firestore", "Error fetching notification preference: " + e.getMessage());
                                            // Fallback to SharedPreferences if Firestore call fails
                                            SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
                                            boolean savedPreference = sharedPreferences.getBoolean("notifications_enabled", true);  // Default to true if not set
                                            toggleButton.setChecked(savedPreference);
                                        });
                            } else {
                                Log.d("Installation ID", "Failed to get Installation ID.");
                                // Fallback to SharedPreferences if installation ID is not retrieved
                                SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
                                boolean savedPreference = sharedPreferences.getBoolean("notifications_enabled", true);  // Default to true if not set
                                toggleButton.setChecked(savedPreference);
                            }
                        });

                // Add the ToggleButton to the dialog
                builder.setView(toggleButton);

                // Set the OK button to dismiss the dialog
                builder.setPositiveButton("OK", (dialog, which) -> {
                    // Handle the action if necessary (e.g., save the notification state)
                    boolean isNotificationEnabled = toggleButton.isChecked();

                    // Save the notification preference in shared preferences
                    SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("notifications_enabled", isNotificationEnabled);
                    editor.apply();

                    // Now, update the notification preference in Firestore
                    FirebaseInstallations.getInstance().getId()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String installationId = task.getResult();  // Get Installation ID
                                    Log.d("Installation ID", "Installation ID: " + installationId);

                                    // Update the Firestore document with the notification preference
                                    db.collection("users")
                                            .document(installationId)
                                            .update("notificationsEnabled", isNotificationEnabled)
                                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Notification preference updated successfully"))
                                            .addOnFailureListener(e -> Log.d("Firestore", "Error updating notification preference: " + e.getMessage()));
                                } else {
                                    Log.d("Installation ID", "Failed to get Installation ID.");
                                }
                            });
                });

                // Show the dialog
                builder.create().show();
            });


        }
    }


    private void saveUID() {
        // Step 1: Get the Firebase Installation ID
        FirebaseInstallations.getInstance().getId()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String installationId = task.getResult();  // Get Installation ID
                        Log.d("Installation ID", "Installation ID: " + installationId);

                        // Step 2: Get the current Firebase user UID (Android ID)
                        String uid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                        if (uid != null) {
                            // Step 3: Check if the UID field exists
                            db.collection("users")
                                    .document(installationId)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            // Check if 'uid' is already set (using lowercase 'uid')
                                            if (!documentSnapshot.contains("uid")) {
                                                // If 'uid' is not set, update the 'uid' field
                                                db.collection("users")
                                                        .document(installationId)
                                                        .update("uid", uid)  // Use lowercase 'uid'
                                                        .addOnSuccessListener(aVoid ->
                                                                Log.d("Firestore", "UID stored successfully"))
                                                        .addOnFailureListener(e ->
                                                                Log.d("Firestore", "Error storing UID: " + e.getMessage()));
                                            } else {
                                                Log.d("Firestore", "UID already exists.");
                                            }
                                        } else {
                                            // Document does not exist, create it and set 'uid'
                                            db.collection("users")
                                                    .document(installationId)
                                                    .set(Collections.singletonMap("uid", uid))  // Use lowercase 'uid'
                                                    .addOnSuccessListener(aVoid ->
                                                            Log.d("Firestore", "Document created and UID stored successfully"))
                                                    .addOnFailureListener(e ->
                                                            Log.d("Firestore", "Error storing UID: " + e.getMessage()));
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Log.d("Firestore", "Error checking document existence: " + e.getMessage()));
                        } else {
                            Log.d("Firestore", "Android ID is null. Cannot store UID.");
                        }
                    } else {
                        Log.d("Installation ID", "Failed to get Installation ID.");
                    }
                });
    }





    private void showAdminLoginPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Admin Login");

        // Create input fields dynamically
        final EditText emailInput = new EditText(this);
        emailInput.setHint("Email");
        final EditText passwordInput = new EditText(this);
        passwordInput.setHint("Password");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // Add inputs to a layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);
        layout.addView(emailInput);
        layout.addView(passwordInput);
        builder.setView(layout);

        // Add buttons
        builder.setPositiveButton("Login", null); // We'll override this later
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            // Override the "Login" button
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    performFirestoreLogin(email, password, dialog);
                }
            });
        });

        dialog.show();
    }

    private void performFirestoreLogin(String email, String password, AlertDialog dialog) {
        db.collection("admin")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String storedPassword = document.getString("password");

                        if (storedPassword != null && storedPassword.equals(password)) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss(); // Close the dialog
                            // Navigate to AdminActivity
                            startActivity(new Intent(MainActivity.this, AdminActivity.class));
                            finish(); // Finish MainActivity if desired
                        } else {
                            Toast.makeText(this, "Incorrect password.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Admin not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "Notification permission granted.");
            } else {
                Log.d("Permission", "Notification permission denied.");
                Toast.makeText(this, "You need to grant notification permission to use Single Lottery.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void OfflineScheduler() {
        PeriodicWorkRequest lotteryWorkRequest = new PeriodicWorkRequest.Builder(OfflineWorker.class,
                15, TimeUnit.MINUTES // Check every 15 mins
        ).build();
        WorkManager.getInstance(this).enqueue(lotteryWorkRequest);
    }
}
