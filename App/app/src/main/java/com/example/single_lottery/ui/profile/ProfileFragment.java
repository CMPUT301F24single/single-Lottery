package com.example.single_lottery.ui.profile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.single_lottery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.UUID;

public class ProfileFragment extends Fragment {
    private TextView nameTextView, emailTextView, phoneTextView;
    private Button editButton, uploadButton;
    private ImageView profileImageView;

    private String userName;
    private String userEmail;
    private String userPhone;
    private String installationId;
    private Uri profileImageUri;

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        editButton = view.findViewById(R.id.editButton);
        uploadButton = view.findViewById(R.id.uploadButton);
        profileImageView = view.findViewById(R.id.profileImageView);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        FirebaseInstallations.getInstance().getId()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        installationId = task.getResult();
                        loadUserProfile(installationId);
                    } else {
                        Log.e("ProfileFragment", "failed to get installation id: " + task.getException());
                    }
                });

        editButton.setOnClickListener(v -> showEditDialog());
        uploadButton.setOnClickListener(v -> selectImage());

        return view;
    }

    private void loadUserProfile(String installationId) {
        DocumentReference docRef = firestore.collection("users").document(installationId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                userName = task.getResult().getString("name");
                userEmail = task.getResult().getString("email");
                userPhone = task.getResult().getString("phone");
                String profileImageUrl = task.getResult().getString("profileImageUrl");
                updateUserDetails(profileImageUrl);
            } else {
                userName = "Name";
                userEmail = "Email";
                userPhone = "Phone";
                updateUserDetails(null);
            }
        }).addOnFailureListener(e -> {
            Log.e("ProfileFragment", "failed to load user profile: " + e.getMessage());
        });
    }

    private void updateUserDetails(String profileImageUrl) {
        nameTextView.setText(userName);
        emailTextView.setText(userEmail);
        phoneTextView.setText(userPhone);
        if (profileImageUrl != null) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.ic_placeholder);
        }
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("edit profile");

        View dialogView = getLayoutInflater().inflate(R.layout.edit_dialog, null);
        final EditText nameInput = dialogView.findViewById(R.id.nameInput);
        final EditText emailInput = dialogView.findViewById(R.id.emailInput);
        final EditText phoneInput = dialogView.findViewById(R.id.phoneInput);

        setEditTextValue(nameInput, userName, "Enter your name");
        setEditTextValue(emailInput, userEmail, "Enter your email");
        setEditTextValue(phoneInput, userPhone, "Enter your phone number");

        builder.setView(dialogView)
                .setPositiveButton("save", (dialog, which) -> {
                    userName = nameInput.getText().toString().trim();
                    userEmail = emailInput.getText().toString().trim();
                    userPhone = phoneInput.getText().toString().trim();
                    updateUserDetails(null);
                    saveUserDataToFirestore(installationId, null);
                })
                .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "select image"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), profileImageUri);
                profileImageView.setImageBitmap(bitmap);
                uploadProfileImage(); 
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setEditTextValue(EditText editText, String value, String hint) {
        if (value == null || value.isEmpty()) {
            editText.setHint(hint);
        } else {
            editText.setText(value);
        }
    }

    private void uploadProfileImage() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("ProfileFragment", "user is null.");
            return;
        }

        if (profileImageUri != null) {
            final StorageReference profileImageRef = storageReference.child("profileImages/" + UUID.randomUUID().toString() + ".jpg");

            profileImageRef.putFile(profileImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String profileImageUrl = uri.toString();
                            saveUserDataToFirestore(installationId, profileImageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileFragment", "failed to upload image: " + e.getMessage());
                    });
        } else {
            Log.e("ProfileFragment", "profileImageUri is null");
        }
    }

    private void saveUserDataToFirestore(String installationId, String profileImageUri) {
        if (installationId == null) {
            Log.e("ProfileFragment", "installationId is null");
            return;
        }

        User user = new User(userName, userEmail, userPhone, profileImageUri);
        firestore.collection("users")
                .document(installationId) 
                .set(user)
                .addOnSuccessListener(aVoid ->
                        Log.d("ProfileFragment", "profile updated successfully"))
                .addOnFailureListener(e ->
                        Log.e("ProfileFragment", "profile update failed: " + e.getMessage()));
    }
}
