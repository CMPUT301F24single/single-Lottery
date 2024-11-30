package com.example.single_lottery.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminUserDetailActivity extends AppCompatActivity {
    public static final String EXTRA_USER = "extra_user";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_user_detail);
        setTitle("User Details");

        // 获取传递的用户数据
        EventModel user = (EventModel) getIntent().getSerializableExtra(EXTRA_USER);

        // 绑定 UI 元素
        ImageView userProfileImage = findViewById(R.id.userProfileImage);
        TextView userName = findViewById(R.id.userName);
        TextView userEmail = findViewById(R.id.userEmail);
        TextView userPhone = findViewById(R.id.userPhone);
        Button btnDeleteAvatar = findViewById(R.id.btnDeleteAvatar);
        Button btnDeleteProfile = findViewById(R.id.btnDeleteProfile);
        ImageView backButton = findViewById(R.id.adminUserBackButton);  // Back button reference

        if (user != null) {
            userName.setText(user.getName());
            userEmail.setText(String.format("Email: %s", user.getEmail()));
            userPhone.setText(String.format("Phone: %s", user.getPhone()));

            // 加载头像
            Glide.with(this)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(userProfileImage);

            // 删除头像按钮逻辑
            btnDeleteAvatar.setOnClickListener(v -> deleteAvatar(user));

            // 删除用户按钮逻辑
            btnDeleteProfile.setOnClickListener(v -> deleteProfile(user));
        }

        // Set click listener for the back button
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { // 返回按钮的 ID
            onBackPressed(); // 返回上一页
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 删除用户文档逻辑
    private void deleteProfile(EventModel user) {
        if (user.getEventId() == null) {
            Toast.makeText(this, "User ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(user.getEventId());

        // 提示确认删除
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    userRef.delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "User deleted successfully!", Toast.LENGTH_SHORT).show();
                                finish(); // 关闭当前页面
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    // 删除头像逻辑
    private void deleteAvatar(EventModel user) {
        // 检查 eventId 是否为 null
        if (user.getEventId() == null) {
            Toast.makeText(this, "User ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(user.getEventId());

        userRef.update("profileImageUrl", null)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Avatar deleted successfully!", Toast.LENGTH_SHORT).show();
                    ImageView userProfileImage = findViewById(R.id.userProfileImage);
                    userProfileImage.setImageResource(R.drawable.ic_profile);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete avatar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
