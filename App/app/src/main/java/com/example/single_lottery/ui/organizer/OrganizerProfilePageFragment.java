package com.example.single_lottery.ui.organizer;

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
import com.example.single_lottery.ui.organizer.Organizer;
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

public class OrganizerProfilePageFragment extends Fragment {
    private TextView nameTextView, emailTextView, phoneTextView, infoTextView;
    private Button editButton, uploadButton;
    private ImageView profileImageView;

    private String organizerName;
    private String organizerEmail;
    private String organizerPhone;
    private String companyInfo;
    private String installationId;
    private Uri profileImageUri;

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organizer_profile, container, false);

        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        infoTextView = view.findViewById(R.id.infoTextView);
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
                        loadOrganizerProfile(installationId);
                    } else {
                        Log.e("OrganizerProfilePageFragment", "failed to get installation id: " + task.getException());
                    }
                });

        editButton.setOnClickListener(v -> showEditDialog());
        uploadButton.setOnClickListener(v -> selectImage());

        return view;
    }

    private void loadOrganizerProfile(String installationId) {
        DocumentReference docRef = firestore.collection("organizers").document(installationId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                organizerName = task.getResult().getString("name");
                organizerEmail = task.getResult().getString("email");
                organizerPhone = task.getResult().getString("phone");
                companyInfo = task.getResult().getString("info");
                String profileImageUrl = task.getResult().getString("profileImageUrl");
                updateOrganizerDetails(profileImageUrl);
            } else {
                organizerName = "Name";
                organizerEmail = "Email";
                organizerPhone = "Phone";
                companyInfo = "info";
                updateOrganizerDetails(null);
            }
        }).addOnFailureListener(e -> {
            Log.e("OrganizerProfilePageFragment", "failed to load user profile: " + e.getMessage());
        });
    }

    private void updateOrganizerDetails(String profileImageUrl) {
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
            profileImageView.setImageResource(R.drawable.org);
        }
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("edit profile");

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
                    saveOrganizerDataToFirestore(installationId, null);
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
        if (profileImageUri != null) {
            final StorageReference profileImageRef = storageReference.child("organizer_profileImages/" + UUID.randomUUID().toString() + ".jpg");

            profileImageRef.putFile(profileImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String profileImageUrl = uri.toString();
                            saveOrganizerDataToFirestore(installationId, profileImageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("OrganizerProfilePageFragment", "failed to upload image: " + e.getMessage());
                    });
        } else {
            Log.e("OrganizerProfilePageFragment", "profileImageUri is null");
        }
    }

    private void saveOrganizerDataToFirestore(String installationId, String profileImageUri) {
        if (installationId == null) {
            Log.e("OrganizerProfilePageFragment", "installationId is null");
            return;
        }

        Organizer organizer = new Organizer(organizerName, organizerEmail, organizerPhone, companyInfo, profileImageUri);
        firestore.collection("organizers")
                .document(installationId)
                .set(organizer)
                .addOnSuccessListener(aVoid ->
                        Log.d("OrganizerProfilePageFragment", "profile updated successfully"))
                .addOnFailureListener(e ->
                        Log.e("OrganizerProfilePageFragment", "profile update failed: " + e.getMessage()));

    }
}
