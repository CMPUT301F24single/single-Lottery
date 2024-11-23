package com.example.single_lottery.ui.admin;

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
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

public class AdminUserDetailActivity extends AppCompatActivity {

    private TextView userName, userEmail, userPhone;
    private ImageView userAvatar;
    private Button deleteAvatarButton, deleteUserButton;

    private String userId;
    private String profileImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_user_detail);

        // 设置返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 初始化视图
        userName = findViewById(R.id.textUserNameDetail);
        userEmail = findViewById(R.id.textUserEmail);
        userPhone = findViewById(R.id.textUserPhone);
        userAvatar = findViewById(R.id.imageUserAvatar);
        deleteAvatarButton = findViewById(R.id.buttonDeleteAvatar);
        deleteUserButton = findViewById(R.id.buttonDeleteUser);

        // 获取Intent传递的数据
        userId = getIntent().getStringExtra("userId");
        profileImageUrl = getIntent().getStringExtra("profileImageUrl");
        String name = getIntent().getStringExtra("userName");
        String email = getIntent().getStringExtra("userEmail");
        String phone = getIntent().getStringExtra("userPhone");

        // 设置数据到视图
        userName.setText(name != null ? "Name: " + name : "Name: No Name");
        userEmail.setText(email != null ? "Email: " + email : "Email: No Email");
        userPhone.setText(phone != null ? "Phone: " + phone : "Phone: No Phone");

        // 加载头像
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(this).load(profileImageUrl).into(userAvatar);
        } else {
            userAvatar.setImageResource(R.drawable.ic_profile); // 默认头像
        }

        // 设置删除头像按钮点击事件
        deleteAvatarButton.setOnClickListener(v -> deleteAvatar());

        // 设置删除用户按钮点击事件
        deleteUserButton.setOnClickListener(v -> deleteUser());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // 返回上一页
        return true;
    }

    private void deleteAvatar() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (userId == null) {
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // 将 profileImageUrl 设置为 null
        db.collection("users").document(userId)
                .update("profileImageUrl", null)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Avatar deleted successfully", Toast.LENGTH_SHORT).show();
                    userAvatar.setImageResource(R.drawable.ic_profile); // 切换为默认头像
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete avatar", Toast.LENGTH_SHORT).show());
    }

    private void deleteUser() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (userId == null) {
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // 删除用户文档
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    finish(); // 关闭当前页面
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show());
    }
}