package com.example.single_lottery.ui.user.home;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.single_lottery.R;
import com.example.single_lottery.EventModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserEventDetailActivity extends AppCompatActivity {

    private TextView textViewEventName, textViewEventDescription, textViewEventTime,
            textViewRegistrationDeadline, textViewLotteryTime,
            textViewWaitingListCount, textViewLotteryCount;
    private ImageView imageViewPoster;
    private ImageButton backButton;
    private Button buttonSignUp;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home_event_detail);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // 获取传递的 event_id
        eventId = getIntent().getStringExtra("event_id");
        if (eventId == null) {
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化视图
        textViewEventName = findViewById(R.id.textViewEventName);
        textViewEventDescription = findViewById(R.id.textViewEventDescription);
        textViewEventTime = findViewById(R.id.textViewEventTime);
        textViewRegistrationDeadline = findViewById(R.id.textViewRegistrationDeadline);
        textViewLotteryTime = findViewById(R.id.textViewLotteryTime);
        textViewWaitingListCount = findViewById(R.id.textViewWaitingListCount);
        textViewLotteryCount = findViewById(R.id.textViewLotteryCount);
        imageViewPoster = findViewById(R.id.imageViewPoster);

        loadEventData(eventId);

        // Initialize the Sign Up button
        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonSignUp.setOnClickListener(v -> checkAndSignUpForEvent());
    }

    private void checkAndSignUpForEvent() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String registrationId = eventId + "_" + userId;

        // 检查用户是否已经报名
        db.collection("registered_events").document(registrationId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        // 用户已报名
                        Toast.makeText(this, "You are already signed up for this event!", Toast.LENGTH_SHORT).show();
                    } else {
                        // 用户尚未报名，执行报名操作
                        signUpForEvent(userId, registrationId);
                    }
                });
    }

    private void signUpForEvent(String userId, String registrationId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.runTransaction(transaction -> {
            DocumentReference eventRef = db.collection("events").document(eventId);
            DocumentSnapshot snapshot = transaction.get(eventRef);

            // 获取当前报名数量
            long currentCount = snapshot.getLong("currentSignUpCount") != null ? snapshot.getLong("currentSignUpCount") : 0;

            // 增加报名数量
            transaction.update(eventRef, "currentSignUpCount", currentCount + 1);

            // 将报名信息添加到 registered_events 集合
            DocumentReference registrationRef = db.collection("registered_events").document(registrationId);
            Map<String, Object> registration = new HashMap<>();
            registration.put("userId", userId);
            registration.put("eventId", eventId);
            registration.put("timestamp", FieldValue.serverTimestamp());
            transaction.set(registrationRef, registration);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Successfully signed up for the event!", Toast.LENGTH_SHORT).show();
            buttonSignUp.setEnabled(false);
            loadEventData(eventId); // 更新页面显示
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to sign up. Please try again.", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadEventData(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        EventModel event = documentSnapshot.toObject(EventModel.class);
                        if (event != null) {
                            textViewEventName.setText(event.getName());
                            textViewEventDescription.setText(event.getDescription());
                            textViewEventTime.setText(event.getTime());
                            textViewRegistrationDeadline.setText(event.getRegistrationDeadline());
                            textViewLotteryTime.setText(event.getLotteryTime());
                            textViewLotteryCount.setText(String.valueOf(event.getLotteryCount()));

                            // 获取当前报名数量和最大等待人数
                            int currentSignUpCount = documentSnapshot.getLong("currentSignUpCount") != null
                                    ? documentSnapshot.getLong("currentSignUpCount").intValue() : 0;
                            int maxWaitingListCount = event.getWaitingListCount();

                            // 设置“报名数量/最大数量”格式
                            textViewWaitingListCount.setText(currentSignUpCount + "/" + maxWaitingListCount);

                            if (event.getPosterUrl() != null) {
                                Glide.with(this).load(event.getPosterUrl()).into(imageViewPoster);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // 错误处理
                });
    }
}
