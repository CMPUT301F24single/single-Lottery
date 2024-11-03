package com.example.single_lottery.ui.user.events;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.single_lottery.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserEventDetailActivity extends AppCompatActivity {
    private TextView eventNameTextView, eventTimeTextView, registrationDeadlineTextView,
            lotteryTimeTextView, waitingListCountTextView, lotteryCountTextView, eventDescriptionTextView;
    private ImageView eventPosterImageView;
    private TextView eventStatusValueTextView;
    private Button cancelRegistrationButton;

    private String registrationDeadline; // 报名截止时间字符串
    private String lotteryTime; // 抽奖时间字符串

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_event_viewdetail);

        // 绑定视图
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        eventPosterImageView = findViewById(R.id.imageViewPoster);
        eventNameTextView = findViewById(R.id.eventNameTextView);
        eventTimeTextView = findViewById(R.id.eventTimeTextView);
        registrationDeadlineTextView = findViewById(R.id.registrationDeadlineTextView);
        lotteryTimeTextView = findViewById(R.id.lotteryTimeTextView);
        waitingListCountTextView = findViewById(R.id.waitingListCountTextView);
        lotteryCountTextView = findViewById(R.id.lotteryCountTextView);
        eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
        eventStatusValueTextView = findViewById(R.id.eventStatusValueTextView);
        cancelRegistrationButton = findViewById(R.id.cancelRegistrationButton);

        // 获取传递的 eventId
        String eventId = getIntent().getStringExtra("event_id");
        loadEventDetails(eventId);

        // 设置取消注册按钮的点击事件
        cancelRegistrationButton.setOnClickListener(v -> cancelRegistration(eventId));
    }

    private void loadEventDetails(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 从 documentSnapshot 获取数据
                        String eventName = documentSnapshot.getString("name");
                        String eventDescription = documentSnapshot.getString("description");
                        String eventTime = documentSnapshot.getString("time");
                        registrationDeadline = documentSnapshot.getString("registrationDeadline");
                        lotteryTime = documentSnapshot.getString("lotteryTime");
                        String waitingListCount = documentSnapshot.getLong("waitingListCount") + "";
                        String lotteryCount = documentSnapshot.getLong("lotteryCount") + "";

                        // 设置 TextViews 的文本
                        eventNameTextView.setText(eventName);
                        eventTimeTextView.setText(eventTime);
                        registrationDeadlineTextView.setText(registrationDeadline);
                        lotteryTimeTextView.setText(lotteryTime);
                        waitingListCountTextView.setText(waitingListCount);
                        lotteryCountTextView.setText(lotteryCount);
                        eventDescriptionTextView.setText(eventDescription);

                        // 加载图片
                        String posterUrl = documentSnapshot.getString("posterUrl");
                        if (posterUrl != null && !posterUrl.isEmpty()) {
                            Glide.with(this).load(posterUrl).into(eventPosterImageView);
                        }

                        // 更新活动状态
                        updateEventStatus();
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("UserEventDetailActivity", "Error loading event details", e));
    }

    private void updateEventStatus() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date deadlineDate = dateFormat.parse(registrationDeadline);
            Date lotteryDate = dateFormat.parse(lotteryTime);
            Date currentDate = new Date();

            // 判断当前时间和截止时间、抽奖时间的关系
            if (currentDate.before(deadlineDate)) {
                // 报名截止日期之前
                eventStatusValueTextView.setText("Open for Registration");
                cancelRegistrationButton.setVisibility(View.VISIBLE);  // 显示取消注册按钮
            } else if (currentDate.after(deadlineDate) && currentDate.before(lotteryDate)) {
                // 报名截止日期之后但抽奖时间之前
                eventStatusValueTextView.setText("Registration Closed - Awaiting Lottery");
                cancelRegistrationButton.setVisibility(View.GONE);  // 隐藏取消注册按钮
            } else {
                // 抽奖时间之后
                eventStatusValueTextView.setText("Registration Closed");
                cancelRegistrationButton.setVisibility(View.GONE);  // 隐藏取消注册按钮
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelRegistration(String eventId) {
        String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("registered_events")
                .whereEqualTo("userId", userId)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            db.collection("registered_events").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Registration canceled", Toast.LENGTH_SHORT).show();
                                        finish();  // 关闭当前页面并返回
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to cancel registration", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(this, "Registration not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error fetching registration", Toast.LENGTH_SHORT).show());
    }
}
