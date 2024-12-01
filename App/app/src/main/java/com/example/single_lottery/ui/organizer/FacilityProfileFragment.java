package com.example.single_lottery.ui.organizer;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.single_lottery.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.UUID;

public class FacilityProfileFragment extends Fragment {
    private TextView nameTextView, locationTextView;
    private Button editButton, uploadButton, removeImageButton;
    private ImageView profileImageView;
    private String facilityName;
    private String facilityLocation;
    private String installationId;
    private Uri profileImageUri;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        FirebaseInstallations.getInstance().getId()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        installationId = task.getResult();
                        loadFacilityProfile(installationId);
                    } else {
                        Log.e("ProfileFragment", "failed to get installation id: " + task.getException());
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_facility_profile, container, false);
        nameTextView = view.findViewById(R.id.nameTextView);
        locationTextView = view.findViewById(R.id.locationTextView);
        editButton = view.findViewById(R.id.editButton);
        uploadButton = view.findViewById(R.id.uploadButton);
        removeImageButton = view.findViewById(R.id.removeImageButton);
        profileImageView = view.findViewById(R.id.profileImageView);
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        editButton.setOnClickListener(v -> showEditDialog());
        uploadButton.setOnClickListener(v -> selectImage());
        removeImageButton.setOnClickListener(v -> removeProfileImage());
        return view;
    }
    private void loadFacilityProfile(String installationId) {
        DocumentReference docRef = firestore.collection("facilities").document(installationId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                facilityName = task.getResult().getString("name");
                facilityLocation = task.getResult().getString("location");
                String profileImageUrl = task.getResult().getString("profileImageUrl");
                updateFacilityDetails(profileImageUrl);
            }
        }).addOnFailureListener(e -> {
            Log.e("ProfileFragment", "failed to load user profile: " + e.getMessage());
        });
    }
    private void updateFacilityDetails(String profileImageUrl) {
        nameTextView.setText(facilityName);
        locationTextView.setText(facilityLocation);
        if (profileImageUrl != null) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(profileImageView);
        } else {
            generateLetterAvatar(facilityName);
        }
    }
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
    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Profile");
        View dialogView = getLayoutInflater().inflate(R.layout.edit_dialog_facility, null);
        final EditText nameInput = dialogView.findViewById(R.id.nameInputFac);
        final EditText locationInput = dialogView.findViewById(R.id.locationInputFac);
        setEditTextValue(nameInput, facilityName, "Enter facility name");
        setEditTextValue(locationInput, facilityLocation, "Enter facility location");
        builder.setView(dialogView)
                .setPositiveButton("save", (dialog, which) -> {
                    facilityName = nameInput.getText().toString().trim();
                    facilityLocation = locationInput.getText().toString().trim();
                    updateFacilityDetails(null);
                    saveFacilityDataToFirestore(installationId, null);
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), profileImageUri);
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
        if (profileImageUri != null) {
            DocumentReference docRef = firestore.collection("facilities").document(installationId);
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
                Log.e("ProfileFragment", "Failed to get facility profile: " + e.getMessage());
            });
        } else {
            Log.e("ProfileFragment", "profileImageUri is null");
        }
    }
    private void uploadNewImage() {
        final StorageReference profileImageRef = storageReference.child("facility_profileImages/" + UUID.randomUUID().toString() + ".jpg");
        profileImageRef.putFile(profileImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String profileImageUrl = uri.toString();
                        saveFacilityDataToFirestore(installationId, profileImageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Failed to upload new image: " + e.getMessage());
                });
    }
    private void removeProfileImage() {
        DocumentReference docRef = firestore.collection("facilities").document(installationId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String oldImageUri = task.getResult().getString("profileImageUrl");
                if (oldImageUri != null) {
                    StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUri);
                    oldImageRef.delete().addOnSuccessListener(aVoid -> {
                        Log.d("ProfileFragment", "Old image deleted successfully.");
                        saveFacilityDataToFirestore(installationId, null);
                        loadFacilityProfile(installationId);
                    }).addOnFailureListener(e -> {
                        Log.e("ProfileFragment", "Failed to delete old image: " + e.getMessage());
                    });
                } else {
                    saveFacilityDataToFirestore(installationId, null);
                    loadFacilityProfile(installationId);
                }
            } else {
                Log.e("ProfileFragment", "No existing user document found.");
            }
        }).addOnFailureListener(e -> {
            Log.e("ProfileFragment", "Failed to get user profile: " + e.getMessage());
        });
    }
    private void saveFacilityDataToFirestore(String installationId, String profileImageUrl) {
        String deviceID = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        if (installationId == null) {
            Log.e("ProfileFragment", "installationId is null");
            return;
        }
        Facility facility = new Facility(facilityName, facilityLocation, profileImageUrl);
        firestore.collection("facilities")
                .document(installationId)
                .set(facility)
                .addOnSuccessListener(aVoid ->
                        Log.d("ProfileFragment", "profile updated successfully"))
                .addOnFailureListener(e ->
                        Log.e("ProfileFragment", "profile update failed: " + e.getMessage()));

        firestore.collection("events")
                .whereEqualTo("organizerDeviceID", deviceID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        documentSnapshot.getReference()
                                .update("facility", facilityName)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Field updated successfully.");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error updating document: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting documents: " + e.getMessage());
                });
    }
}
