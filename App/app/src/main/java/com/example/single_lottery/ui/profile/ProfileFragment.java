package com.example.single_lottery.ui.profile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.single_lottery.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;

public class ProfileFragment extends Fragment {
    private TextView nameTextView, emailTextView, phoneTextView;
    private Button editButton;

    private String userName;
    private String userEmail;
    private String userPhone;
    private String installationId; 

    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        editButton = view.findViewById(R.id.editButton);

        firestore = FirebaseFirestore.getInstance();

        FirebaseInstallations.getInstance().getId()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    installationId = task.getResult();
                    loadUserProfile(installationId);
                } else {
                    Log.e("ProfileFragment", "Failed to get Installation ID: " + task.getException());
                }
            });

        editButton.setOnClickListener(v -> showEditDialog());

        return view;
    }

    private void loadUserProfile(String installationId) {
        DocumentReference docRef = firestore.collection("users").document(installationId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                userName = task.getResult().getString("name");
                userEmail = task.getResult().getString("email");
                userPhone = task.getResult().getString("phone");
                updateUserDetails();
            } else {
                userName = "Name";
                userEmail = "Email";
                userPhone = "Phone";
                updateUserDetails();
            }
        }).addOnFailureListener(e -> {
            Log.e("ProfileFragment", "Error loading user profile: " + e.getMessage());
        });
    }

    private void updateUserDetails() {
        nameTextView.setText(userName);
        emailTextView.setText(userEmail);
        phoneTextView.setText(userPhone);
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit User Details");

        View dialogView = getLayoutInflater().inflate(R.layout.edit_dialog, null);
        final EditText nameInput = dialogView.findViewById(R.id.nameInput);
        final EditText emailInput = dialogView.findViewById(R.id.emailInput);
        final EditText phoneInput = dialogView.findViewById(R.id.phoneInput);

        setEditTextValue(nameInput, userName, "Enter your name");
        setEditTextValue(emailInput, userEmail, "Enter your email");
        setEditTextValue(phoneInput, userPhone, "Enter your phone number");

        builder.setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    userName = nameInput.getText().toString().trim(); 
                    userEmail = emailInput.getText().toString().trim();
                    userPhone = phoneInput.getText().toString().trim();
                    updateUserDetails();

                    saveUserDataToFirestore(installationId);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void setEditTextValue(EditText editText, String value, String hint) {
        if (value == null || value.isEmpty()) {
            editText.setHint(hint);
        } else {
            editText.setText(value);
        }
    }
    private void saveUserDataToFirestore(String installationId) {
        if (installationId == null) {
            Log.e("ProfileFragment", "Installation ID is null");
            return; 
        }

        String profileImageUrl = "drawable://ic_placeholder";
        User user = new User(userName, userEmail, userPhone, profileImageUrl);

        firestore.collection("users")
                .document(installationId)  
                .set(user)
                .addOnSuccessListener(documentReference -> {
                    Log.d("ProfileFragment", "User profile updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Error updating user profile: " + e.getMessage());
                });
    }
}
