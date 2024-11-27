package com.example.single_lottery.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.single_lottery.R;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AdminOrganizerDetailActivity extends AppCompatActivity {

    private String organizerId;
    private String profileImageUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_organizer_detail);

        // 设置返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 获取 Intent 数据
        organizerId = getIntent().getStringExtra("organizerId");
        String organizerName = getIntent().getStringExtra("organizerName");
        String organizerEmail = getIntent().getStringExtra("organizerEmail");
        String organizerPhone = getIntent().getStringExtra("organizerPhone");
        String organizerInfo = getIntent().getStringExtra("organizerInfo");
        profileImageUrl = getIntent().getStringExtra("organizerProfileImageUrl");

        // 初始化视图
        TextView textViewName = findViewById(R.id.textViewOrganizerName);
        TextView textViewEmail = findViewById(R.id.textViewOrganizerEmail);
        TextView textViewPhone = findViewById(R.id.textViewOrganizerPhone);
        TextView textViewInfo = findViewById(R.id.textViewOrganizerInfo);
        ImageView imageViewProfile = findViewById(R.id.imageViewOrganizerProfile);
        Button buttonDeleteAvatar = findViewById(R.id.buttonDeleteAvatar);
        Button buttonDeleteProfile = findViewById(R.id.buttonDeleteProfile);

        // 设置值
        textViewName.setText("Name: " + organizerName);
        textViewEmail.setText("Email: " + organizerEmail);
        textViewPhone.setText("Phone: " + organizerPhone);
        textViewInfo.setText("Info: " + organizerInfo);

        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(this).load(profileImageUrl).into(imageViewProfile);
        } else {
            imageViewProfile.setImageResource(R.drawable.ic_profile); // 替换为默认头像资源
        }

        // 删除头像按钮点击事件
        buttonDeleteAvatar.setOnClickListener(v -> deleteAvatar());

        // 删除 Profile 按钮点击事件
        buttonDeleteProfile.setOnClickListener(v -> deleteProfile());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // 返回上一页
        return true;
    }

    private void deleteAvatar() {
        if (profileImageUrl == null || profileImageUrl.isEmpty()) {
            Toast.makeText(this, "No avatar to delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference profileImageRef = storage.getReferenceFromUrl(profileImageUrl);

        profileImageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("AdminOrganizerDetail", "Avatar deleted successfully");
                    FirebaseFirestore.getInstance().collection("organizers")
                            .document(organizerId)
                            .update("profileImageUrl", null)
                            .addOnSuccessListener(unused -> {
                                Log.d("AdminOrganizerDetail", "Avatar URL removed from Firestore");
                                Toast.makeText(this, "Avatar deleted successfully", Toast.LENGTH_SHORT).show();
                                ImageView imageViewProfile = findViewById(R.id.imageViewOrganizerProfile);
                                imageViewProfile.setImageResource(R.drawable.ic_profile);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("AdminOrganizerDetail", "Failed to update Firestore", e);
                                Toast.makeText(this, "Failed to update database", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminOrganizerDetail", "Failed to delete avatar", e);
                    Toast.makeText(this, "Failed to delete avatar", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteProfile() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("organizers").document(organizerId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("AdminOrganizerDetail", "Profile deleted successfully");
                    Toast.makeText(this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();
                    finish(); // 关闭当前页面
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminOrganizerDetail", "Failed to delete profile", e);
                    Toast.makeText(this, "Failed to delete profile", Toast.LENGTH_SHORT).show();
                });
    }
}