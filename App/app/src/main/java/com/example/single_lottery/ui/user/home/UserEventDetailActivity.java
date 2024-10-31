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
        buttonSignUp.setOnClickListener(v -> signUpForEvent());
    }

    private void signUpForEvent() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> signUpData = new HashMap<>();

        // 添加用户ID或设备ID等唯一标识
        String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        signUpData.put("userId", userId);
        signUpData.put("eventId", eventId);
        signUpData.put("signUpTimestamp", FieldValue.serverTimestamp());

        // 保存到数据库的 signups 集合中
        db.collection("signups")
                .add(signUpData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Successfully signed up for the event!", Toast.LENGTH_SHORT).show();
                    buttonSignUp.setEnabled(false); // 禁用按钮以防止重复报名
                })
                .addOnFailureListener(e -> {
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
                            textViewWaitingListCount.setText(String.valueOf(event.getWaitingListCount()));
                            textViewLotteryCount.setText(String.valueOf(event.getLotteryCount()));

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
