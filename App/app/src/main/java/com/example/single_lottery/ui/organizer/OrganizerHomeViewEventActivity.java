package com.example.single_lottery.ui.organizer;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.single_lottery.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizerHomeViewEventActivity extends AppCompatActivity {

    private TextView textViewEventName, textViewEventTime, textViewRegistrationDeadline,
            textViewLotteryTime, textViewWaitingListCount, textViewLotteryCount, textViewEventDescription;
    private ImageView imageViewPoster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_view_event);

        // 初始化视图
        textViewEventName = findViewById(R.id.textViewEventName);
        textViewEventDescription = findViewById(R.id.textViewEventDescription);
        textViewEventTime = findViewById(R.id.textViewEventTime);
        textViewRegistrationDeadline = findViewById(R.id.textViewRegistrationDeadline);
        textViewLotteryTime = findViewById(R.id.textViewLotteryTime);
        textViewWaitingListCount = findViewById(R.id.textViewWaitingListCount);
        textViewLotteryCount = findViewById(R.id.textViewLotteryCount);
        imageViewPoster = findViewById(R.id.imageViewPoster);


        // 获取传递的 event_id
        String eventId = getIntent().getStringExtra("event_id");

        // 从 Firestore 加载活动数据
        loadEventData(eventId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String eventId = getIntent().getStringExtra("event_id");  // 确保传递了正确的 eventId
        loadEventData(eventId);  // 每次返回页面时重新加载活动数据
    }

    private void loadEventData(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        OrganizerHomeEventModel event = documentSnapshot.toObject(OrganizerHomeEventModel.class);
                        if (event != null) {
                            textViewEventName.setText(event.getName());
                            textViewEventDescription.setText(event.getDescription());
                            textViewEventTime.setText(event.getTime());
                            textViewRegistrationDeadline.setText(event.getRegistrationDeadline());
                            textViewLotteryTime.setText(event.getLotteryTime());
                            textViewWaitingListCount.setText(String.valueOf(event.getWaitingListCount()));
                            textViewLotteryCount.setText(String.valueOf(event.getLotteryCount()));

                            // 使用 Glide 显示活动海报
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