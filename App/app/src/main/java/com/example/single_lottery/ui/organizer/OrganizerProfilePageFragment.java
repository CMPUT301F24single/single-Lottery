package com.example.single_lottery.ui.organizer;

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
 * Fragment for organizer profile management.
 * Handles profile display, editing, and syncs with Firebase.
 *
 * @author [Haorui Gao]
 * @version 1.0
 */
public class OrganizerProfilePageFragment extends Fragment {
    // UI Views
    private TextView nameTextView, emailTextView, phoneTextView, infoTextView;
    private Button editButton, uploadButton, removeImageButton;
    private ImageView profileImageView;

    // Profile data
    private String organizerName;
    private String organizerEmail;
    private String organizerPhone;
    private String companyInfo;
    private String installationId;
    private Uri profileImageUri;

    // Firebase references
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Initialize views and setup listeners
        View view = inflater.inflate(R.layout.fragment_organizer_profile, container, false);

        // Find and bind views
        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        infoTextView = view.findViewById(R.id.infoTextView);
        editButton = view.findViewById(R.id.editButton);
        uploadButton = view.findViewById(R.id.uploadButton);
        removeImageButton = view.findViewById(R.id.removeImageButton);
        profileImageView = view.findViewById(R.id.profileImageView);

        // Setup Firebase
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Get installation ID and load profile
        FirebaseInstallations.getInstance().getId()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        installationId = task.getResult();
                        loadOrganizerProfile(installationId);
                    } else {
                        Log.e("OrganizerProfilePageFragment", "failed to get installation id: " + task.getException());
                    }
                });
        // Setup button click listeners
        editButton.setOnClickListener(v -> showEditDialog());
        uploadButton.setOnClickListener(v -> selectImage());
        removeImageButton.setOnClickListener(v -> removeProfileImage());

        return view;
    }
    /**
     * Loads organizer profile from Firebase.
     * Checks both user and organizer collections.
     */
    private void loadOrganizerProfile(String installationId) {
        // Check user collection first
        // Then check organizer collection if not found
        DocumentReference userDocRef = firestore.collection("users").document(installationId);
        userDocRef.get().addOnCompleteListener(userTask -> {
            if (userTask.isSuccessful() && userTask.getResult() != null && userTask.getResult().exists()) {
                organizerName = userTask.getResult().getString("name");
                organizerEmail = userTask.getResult().getString("email");
                organizerPhone = userTask.getResult().getString("phone");
                companyInfo = userTask.getResult().getString("info");
                String profileImageUrl = userTask.getResult().getString("profileImageUrl");
                updateOrganizerDetails(profileImageUrl);
            } else {
                DocumentReference orgDocRef = firestore.collection("organizers").document(installationId);
                orgDocRef.get().addOnCompleteListener(orgTask -> {
                    if (orgTask.isSuccessful() && orgTask.getResult() != null && orgTask.getResult().exists()) {
                        organizerName = orgTask.getResult().getString("name");
                        organizerEmail = orgTask.getResult().getString("email");
                        organizerPhone = orgTask.getResult().getString("phone");
                        companyInfo = orgTask.getResult().getString("info");
                        String profileImageUrl = orgTask.getResult().getString("profileImageUrl");
                        updateOrganizerDetails(profileImageUrl);
                    } else {
                        Log.e("OrganizerProfilePageFragment", "No existing user or organizer profile found.");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("OrganizerProfilePageFragment", "failed to load organizer profile: " + e.getMessage());
                });
            }
        }).addOnFailureListener(e -> {
            Log.e("OrganizerProfilePageFragment", "failed to load user profile: " + e.getMessage());
        });
    }
    /**
     * Updates UI with profile details.
     * Shows profile image if exists, or generates avatar.
     */
    private void updateOrganizerDetails(String profileImageUrl) {
        // Update text fields
        // Load profile image
        nameTextView.setText(organizerName);
        emailTextView.setText(organizerEmail);
        phoneTextView.setText(organizerPhone);
        infoTextView.setText(companyInfo);
        if (profileImageUrl != null) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.org)
                    .error(R.drawable.org)
                    .into(profileImageView);
        } else {
            generateLetterAvatar(organizerName);
        }
    }

    /**
     * Creates avatar with initials when no profile image exists.
     * Uses first letters of first and last name.
     */
    private void generateLetterAvatar(String name) {
        // Create bitmap and draw avatar
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
     * Shows dialog to edit profile details.
     * Updates Firestore on save.
     */
    private void showEditDialog() {
        // Show edit dialog
        // Save changes on confirm
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Profile");

        View dialogView = getLayoutInflater().inflate(R.layout.edit_org_dialog, null);
        final EditText nameInput = dialogView.findViewById(R.id.nameInput);
        final EditText emailInput = dialogView.findViewById(R.id.emailInput);
        final EditText phoneInput = dialogView.findViewById(R.id.phoneInput);
        final EditText infoInput = dialogView.findViewById(R.id.infoInput);

        setEditTextValue(nameInput, organizerName, "Enter your name");
        setEditTextValue(emailInput, organizerEmail, "Enter your email");
        setEditTextValue(phoneInput, organizerPhone, "Enter your phone number");
        setEditTextValue(infoInput, companyInfo, "Enter your company info");

        builder.setView(dialogView)
                .setPositiveButton("save", (dialog, which) -> {
                    organizerName = nameInput.getText().toString().trim();
                    organizerEmail = emailInput.getText().toString().trim();
                    organizerPhone = phoneInput.getText().toString().trim();
                    companyInfo = infoInput.getText().toString().trim();
                    updateOrganizerDetails(null);
                    saveOrganizerDataToFirestore(installationId, profileImageUri != null ? profileImageUri.toString() : null);
                })
                .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
    /**
     * Opens image picker for profile photo.
     */
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "select image"), 1);
    }

    /**
     * Handles selected image result.
     * Uploads new image to Firebase.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Process selected image
        // Upload if valid
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), profileImageUri);
                profileImageView.setImageBitmap(bitmap);
                uploadProfileImage(); // 上传图片
            } catch (IOException e) {
                Log.e("OrganizerProfilePageFragment", "Error getting bitmap: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    /**
     * Sets text or hint in EditText.
     */
    private void setEditTextValue(EditText editText, String value, String hint) {
        if (value == null || value.isEmpty()) {
            editText.setHint(hint);
        } else {
            editText.setText(value);
        }
    }
    /**
     * Uploads new profile image to Firebase.
     * Deletes old image first if exists.
     */
    private void uploadProfileImage() {
        if (profileImageUri != null) {
            DocumentReference docRef = firestore.collection("organizers").document(installationId);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    String oldImageUri = task.getResult().getString("profileImageUrl");
                    if (oldImageUri != null) {
                        StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUri);
                        oldImageRef.delete().addOnSuccessListener(aVoid -> {
                            Log.d("OrgProfileFragment", "Old image deleted successfully.");
                            uploadNewImage();
                        }).addOnFailureListener(e -> {
                            Log.e("OrgProfileFragment", "Failed to delete old image: " + e.getMessage());
                            uploadNewImage();
                        });
                    } else {
                        uploadNewImage();
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e("OrgProfileFragment", "Failed to get organizer profile: " + e.getMessage());
            });
        } else {
            Log.e("OrgProfileFragment", "profileImageUri is null");
        }
    }
    /**
     * Uploads image to Firebase storage.
     */
    private void uploadNewImage() {
        final StorageReference profileImageRef = storageReference.child("organizer_profileImages/" + UUID.randomUUID().toString() + ".jpg");
        profileImageRef.putFile(profileImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String profileImageUrl = uri.toString();
                        saveOrganizerDataToFirestore(installationId, profileImageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Failed to upload new image: " + e.getMessage());
                });
    }
    /**
     * Removes profile image.
     * Deletes from storage and clears profile URL.
     */
    private void removeProfileImage() {
        DocumentReference docRef = firestore.collection("organizers").document(installationId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String oldImageUri = task.getResult().getString("profileImageUrl");
                if (oldImageUri != null) {
                    StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUri);
                    oldImageRef.delete().addOnSuccessListener(aVoid -> {
                        Log.d("OrgProfileFragment", "Old image deleted successfully.");
                        saveOrganizerDataToFirestore(installationId, null);
                        loadOrganizerProfile(installationId);
                    }).addOnFailureListener(e -> {
                        Log.e("OrgProfileFragment", "Failed to delete old image: " + e.getMessage());
                    });
                } else {
                    saveOrganizerDataToFirestore(installationId, null);
                    loadOrganizerProfile(installationId);
                }
            } else {
                Log.e("OrgProfileFragment", "No existing user document found.");
            }
        }).addOnFailureListener(e -> {
            Log.e("OrgProfileFragment", "Failed to get user profile: " + e.getMessage());
        });
    }
    /**
     * Saves profile data to Firestore.
     * Updates both user and organizer collections.
     */
    private void saveOrganizerDataToFirestore(String installationId, String profileImageUrl) {
        if (installationId == null) {
            Log.e("OrganizerProfilePageFragment", "installationId is null");
            return;
        }

        OrganizerProfile organizerProfile = new OrganizerProfile(organizerName, organizerEmail, organizerPhone, companyInfo, profileImageUrl);

        firestore.collection("users").document(installationId)
                .set(organizerProfile)
                .addOnSuccessListener(aVoid ->
                        Log.d("OrganizerProfilePageFragment", "user profile updated successfully"))
                .addOnFailureListener(e ->
                        Log.e("OrganizerProfilePageFragment", "user profile update failed: " + e.getMessage()));

        firestore.collection("organizers")
                .document(installationId)
                .set(organizerProfile)
                .addOnSuccessListener(aVoid ->
                        Log.d("OrganizerProfilePageFragment", "organizer profile updated successfully"))
                .addOnFailureListener(e ->
                        Log.e("OrganizerProfilePageFragment", "organizer profile update failed: " + e.getMessage()));
    }
}