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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserHomeDetailActivity extends AppCompatActivity {

    private TextView textViewEventName, textViewEventDescription, textViewEventTime,
            textViewRegistrationDeadline, textViewLotteryTime,
            textViewWaitingListCount, textViewLotteryCount;
    private ImageView imageViewPoster;
    private ImageButton backButton;
    private Button buttonSignUp;
    private String eventId;
    private String registrationDeadline; // 报名截止日期

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

        // 初始化报名按钮
        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonSignUp.setOnClickListener(v -> signUpForEvent());
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

                            registrationDeadline = event.getRegistrationDeadline(); // 存储报名截止日期

                            if (event.getPosterUrl() != null) {
                                Glide.with(this).load(event.getPosterUrl()).into(imageViewPoster);
                            }

                            // 获取实时报名人数
                            countRegistrations(eventId, event.getWaitingListCount());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // 错误处理
                });
    }

    private void countRegistrations(String eventId, int maxWaitingListCount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 查询registered_events集合中符合eventId条件的报名记录数
        db.collection("registered_events")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int currentSignUpCount = queryDocumentSnapshots.size(); // 获取文档数量即为报名人数

                    // 设置“报名数量/最大数量”格式
                    textViewWaitingListCount.setText(currentSignUpCount + "/" + maxWaitingListCount);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load registration count.", Toast.LENGTH_SHORT).show();
                });
    }

    private void signUpForEvent() {
        try {
            // 检查是否在报名截止日期前
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date deadlineDate = dateFormat.parse(registrationDeadline);
            Date currentDate = new Date();

            if (currentDate.after(deadlineDate)) {
                // 如果报名截止日期已过，显示提示信息并退出
                Toast.makeText(this, "Registration is closed. Sign-up is not allowed.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 报名逻辑
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            // 创建一个报名记录到 registered_events 集合中
            Map<String, Object> registrationData = new HashMap<>();
            registrationData.put("eventId", eventId);
            registrationData.put("userId", userId);
            registrationData.put("timestamp", System.currentTimeMillis()); // 可选：记录报名时间

            // 检查用户是否已经报名
            db.collection("registered_events")
                    .whereEqualTo("eventId", eventId)
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            // 用户尚未报名，添加报名记录
                            db.collection("registered_events").add(registrationData)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(this, "Successfully signed up for the event!", Toast.LENGTH_SHORT).show();
                                        buttonSignUp.setEnabled(false);
                                        loadEventData(eventId); // 更新页面显示
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to sign up. Please try again.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // 用户已经报名
                            Toast.makeText(this, "You have already signed up for this event.", Toast.LENGTH_SHORT).show();
                            buttonSignUp.setEnabled(false);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to check registration status. Please try again.", Toast.LENGTH_SHORT).show();
                    });

        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing registration deadline", Toast.LENGTH_SHORT).show();
        }
    }
}
