package com.example.single_lottery.ui.profile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.single_lottery.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    private TextView nameTextView, emailTextView, phoneTextView;
    private Button editButton;

    private String userName = "Name";
    private String userEmail = "Email";
    private String userPhone = "Phone";

    private FirebaseFirestore firestore; // Firestore实例

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        editButton = view.findViewById(R.id.editButton);
        
        firestore = FirebaseFirestore.getInstance();

        updateUserDetails();

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        return view;
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

        nameInput.setHint(userName);
        emailInput.setHint(userEmail);
        phoneInput.setHint(userPhone);
    
        builder.setView(dialogView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userName = nameInput.getText().toString().trim();
                        userEmail = emailInput.getText().toString().trim();
                        userPhone = phoneInput.getText().toString().trim();
                        updateUserDetails();
    
                        saveUserDataToFirestore();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    
        builder.create().show();
    }
    
    

    private void saveUserDataToFirestore() {
        String profileImageUrl = "drawable://ic_placeholder";
        User user = new User(userName, userEmail, userPhone, profileImageUrl);
        
        firestore.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                })
                .addOnFailureListener(e -> {
                });
    }

}