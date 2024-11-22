package com.example.single_lottery.ui.admin;

import android.graphics.Bitmap;
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
import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AdminEventDetailActivity extends AppCompatActivity {

    private TextView textViewName;
    private TextView textViewDescription;
    private TextView textViewTime;
    private TextView textViewRegistrationDeadline;
    private TextView textViewLotteryTime;
    private TextView textViewWaitingListCount;
    private TextView textViewLotteryCount;
    private ImageView imageViewPoster;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 初始化视图
        initViews();

        // 获取从Intent传递的eventId
        String eventId = getIntent().getStringExtra("eventId");
        if (eventId != null) {
            loadEventDetails(eventId);
        } else {
            Log.e("AdminEventDetail", "Event ID is missing in intent.");
        }

        // 绑定删除按钮
        Button buttonDeletePoster = findViewById(R.id.buttonDeletePoster);
        buttonDeletePoster.setOnClickListener(v -> {
            deletePoster(eventId);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { // 返回按钮的 ID
            onBackPressed(); // 返回上一页
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        textViewName = findViewById(R.id.textViewName);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewTime = findViewById(R.id.textViewTime);
        textViewRegistrationDeadline = findViewById(R.id.textViewRegistrationDeadline);
        textViewLotteryTime = findViewById(R.id.textViewLotteryTime);
        textViewWaitingListCount = findViewById(R.id.textViewWaitingListCount);
        textViewLotteryCount = findViewById(R.id.textViewLotteryCount);
        imageViewPoster = findViewById(R.id.imageViewPoster);
    }

    private void loadEventDetails(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // 设置活动信息
                        textViewName.setText(doc.getString("name"));
                        textViewDescription.setText(doc.getString("description"));
                        textViewTime.setText(doc.getString("time"));
                        textViewRegistrationDeadline.setText(doc.getString("registrationDeadline"));
                        textViewLotteryTime.setText(doc.getString("lotteryTime"));
                        textViewWaitingListCount.setText(doc.getLong("waitingListCount") + "/100");
                        textViewLotteryCount.setText(String.valueOf(doc.getLong("lotteryCount")));

                        // 加载海报图片
                        String posterUrl = doc.getString("posterUrl");
                        if (posterUrl != null && !posterUrl.isEmpty()) {
                            Glide.with(this).load(posterUrl).into(imageViewPoster);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("AdminEventDetail", "Error loading event details", e));
    }

    private void deletePoster(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // 确保用户已登录
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not signed in. Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String posterUrl = documentSnapshot.getString("posterUrl");
                    Log.d("DeletePoster", "Poster URL: " + posterUrl);

                    if (posterUrl != null && !posterUrl.isEmpty()) {
                        // 删除存储中的海报文件
                        StorageReference posterRef = storage.getReferenceFromUrl(posterUrl);
                        posterRef.delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("DeletePoster", "Poster deleted successfully from storage");
                                    // 更新数据库，移除海报 URL
                                    db.collection("events").document(eventId)
                                            .update("posterUrl", null)
                                            .addOnSuccessListener(unused -> {
                                                Log.d("DeletePoster", "Poster URL removed from Firestore");
                                                ImageView imageViewPoster = findViewById(R.id.imageViewPoster);
                                                imageViewPoster.setImageResource(android.R.color.transparent);
                                                Toast.makeText(this, "Poster deleted successfully", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("DeletePoster", "Failed to update Firestore", e);
                                                Toast.makeText(this, "Failed to update database", Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("DeletePoster", "Failed to delete poster from storage", e);
                                    Toast.makeText(this, "Failed to delete poster from storage", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "No poster URL found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DeletePoster", "Error fetching event details", e);
                    Toast.makeText(this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
                });
    }


}