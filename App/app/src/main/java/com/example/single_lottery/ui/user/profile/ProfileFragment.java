package com.example.single_lottery.ui.user.profile;

import static android.app.Activity.RESULT_OK;

import static java.util.function.Predicate.isEqual;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.UUID;

/**
 * Fragment for managing user profile and data in the Single Lottery application.
 * Handles profile information display, editing capabilities and image management.
 *
 * @author [Haorui Gao]
 * @version 1.0
 */
public class ProfileFragment extends Fragment {
    private TextView nameTextView, emailTextView, phoneTextView;
    private Button editButton, uploadButton, removeImageButton;
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
        removeImageButton = view.findViewById(R.id.removeImageButton);
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
        removeImageButton.setOnClickListener(v -> removeProfileImage());

        return view;
    }

    /**
     * Retrieves user profile data from Firestore database.
     * Updates UI with fetched data or sets default values if no data exists.
     *
     * @param installationId Unique device installation identifier
     */
    private void loadUserProfile(String installationId) {
        DocumentReference docRef = firestore.collection("users").document(installationId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                userName = task.getResult().getString("name");
                userEmail = task.getResult().getString("email");
                userPhone = task.getResult().getString("phone");
                String profileImageUrl = task.getResult().getString("profileImageUrl");
                updateUserDetails(profileImageUrl);
            }
        }).addOnFailureListener(e -> {
            Log.e("ProfileFragment", "failed to load user profile: " + e.getMessage());
        });
    }

    /**
     * Updates the UI components with user profile information.
     * Handles profile image loading and placeholder generation.
     *
     * @param profileImageUrl URL of user's profile image in Firebase Storage
     */
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
            generateLetterAvatar(userName);
        }
    }

    /**
     * Generates a letter avatar when no profile image is set.
     * Creates circular avatar with user initials.
     *
     * @param name User's display name for initial generation
     */
    private void generateLetterAvatar(String name) {
        String[] nameParts = name.split("\\s+");
        String initials = "";
        if(name.isEmpty()){
            initials += '-';
        }
        else if (nameParts.length > 0) {
            initials += nameParts[0].charAt(0); 
        }
        if (nameParts.length > 1) {
            initials += nameParts[1].charAt(0); 
        }
    
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        canvas.drawCircle(50, 50, 50, paint); 
    
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(initials, 50, 65, paint); 
    
        profileImageView.setImageBitmap(bitmap);
    }

    /**
     * Displays dialog for editing profile information.
     * Allows modification of name, email and phone number.
     */
    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Profile");

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

    /**
     * Launches system image picker for profile photo selection.
     * Initiates intent for image content type.
     */
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "select image"), 1);
    }

    /**
     * Handles result from image selection activity.
     * Processes selected image and initiates upload.
     *
     * @param requestCode Activity request identifier
     * @param resultCode Result status from image picker
     * @param data Intent containing selected image data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
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

    /**
     * Sets value or hint text for EditText components.
     * Handles null or empty input values.
     *
     * @param editText Target EditText component
     * @param value Text value to set
     * @param hint Hint text for empty state
     */
    private void setEditTextValue(EditText editText, String value, String hint) {
        if (value == null || value.isEmpty()) {
            editText.setHint(hint);
        } else {
            editText.setText(value);
        }
    }

    /**
     * Manages profile image upload process to Firebase Storage.
     * Handles existing image deletion and new image upload.
     */
    private void uploadProfileImage() {
        if (profileImageUri != null) {
            DocumentReference docRef = firestore.collection("users").document(installationId);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    String oldImageUri = task.getResult().getString("profileImageUrl");
                    if (oldImageUri != null) {
                        StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUri);
                        oldImageRef.delete().addOnSuccessListener(aVoid -> {
                            Log.d("ProfileFragment", "Old image deleted successfully.");
                            uploadNewImage();
                        }).addOnFailureListener(e -> {
                            Log.e("ProfileFragment", "Failed to delete old image: " + e.getMessage());
                            uploadNewImage();
                        });
                    } else {
                        uploadNewImage();
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e("ProfileFragment", "Failed to get user profile: " + e.getMessage());
            });
        } else {
            Log.e("ProfileFragment", "profileImageUri is null");
        }
    }

    /**
     * Performs new image upload to Firebase Storage.
     * Creates unique filename and updates profile data.
     */
    private void uploadNewImage() {
        final StorageReference profileImageRef = storageReference.child("profileImages/" + UUID.randomUUID().toString() + ".jpg");
        profileImageRef.putFile(profileImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String profileImageUrl = uri.toString();
                        saveUserDataToFirestore(installationId, profileImageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Failed to upload new image: " + e.getMessage());
                });
    }

    /**
     * Removes current profile image from storage.
     * Updates profile to use default avatar.
     */
    private void removeProfileImage() {
        DocumentReference docRef = firestore.collection("users").document(installationId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String oldImageUri = task.getResult().getString("profileImageUrl");
                if (oldImageUri != null) {
                    StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUri);
                    oldImageRef.delete().addOnSuccessListener(aVoid -> {
                        Log.d("ProfileFragment", "Old image deleted successfully.");
                        saveUserDataToFirestore(installationId, null);
                        loadUserProfile(installationId);
                    }).addOnFailureListener(e -> {
                        Log.e("ProfileFragment", "Failed to delete old image: " + e.getMessage());
                    });
                } else {
                    saveUserDataToFirestore(installationId, null);
                    loadUserProfile(installationId);
                }
            } else {
                Log.e("ProfileFragment", "No existing user document found.");
            }
        }).addOnFailureListener(e -> {
            Log.e("ProfileFragment", "Failed to get user profile: " + e.getMessage());
        });
    }

    /**
     * Persists user profile data to Firestore database.
     * Creates or updates existing user document.
     *
     * @param installationId Device installation identifier
     * @param profileImageUri Storage URL of profile image
     */
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
