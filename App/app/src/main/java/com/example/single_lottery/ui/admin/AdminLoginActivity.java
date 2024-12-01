package com.example.single_lottery.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.single_lottery.MainActivity;
import com.example.single_lottery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
/**
 * Activity for admin login authentication.
 * Handles admin credentials verification and login process using Firestore.
 *
 * @author Jingyao Gu
 * @version 1.0
 */
public class AdminLoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private FirebaseFirestore db;
    /**
     * Initializes the activity, sets up UI components and click listeners.
     * Configures admin login form and back navigation.
     *
     * @param savedInstanceState Saved instance state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_login);

        // Initialize Firestore and UI components
        db = FirebaseFirestore.getInstance();
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        // back button functionality
        ImageButton loginBackButton = findViewById(R.id.backButton);
        loginBackButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminLoginActivity.this, MainActivity.class);
            // Clear the back stack to avoid returning to the login activity after navigating back
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Optionally finish this activity
        });


        // Login button click event
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                performFirestoreLogin(email, password);
            }
        });
    }

    /**
     * Authenticates admin login credentials against Firestore.
     * Verifies email and password match stored admin records.
     * Navigates to admin main screen on successful login.
     *
     * @param email Admin's email address
     * @param password Admin's password
     */
    private void performFirestoreLogin(String email, String password) {
        db.collection("admin")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first matching document
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String storedPassword = document.getString("password");

                        // Verify that the passwords match
                        if (storedPassword != null && storedPassword.equals(password)) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            // Jump to the administrator's main page
                            Intent intent = new Intent(AdminLoginActivity.this, AdminActivity.class);
                            startActivity(intent);
                            finish();
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
}