package com.example.single_lottery.ui.organizer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.single_lottery.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class OrganizerHomeEditEventActivity extends AppCompatActivity {

    private EditText editTextEventName, editTextEventDescription, editTextEventTime, editTextRegistrationDeadline,
            editTextLotteryTime, editTextWaitingListCount, editTextLotteryCount;
    private ImageView imageViewPoster;
    private Button buttonUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_edit_event);

        // 初始化视图
        editTextEventName = findViewById(R.id.editTextEventName);
        editTextEventDescription = findViewById(R.id.editTextEventDescription);
        editTextEventTime = findViewById(R.id.editTextEventTime);
        editTextRegistrationDeadline = findViewById(R.id.editTextRegistrationDeadline);
        editTextLotteryTime = findViewById(R.id.editTextLotteryTime);
        editTextWaitingListCount = findViewById(R.id.editTextWaitingListCount);
        editTextLotteryCount = findViewById(R.id.editTextLotteryCount);
        imageViewPoster = findViewById(R.id.imageViewPoster);
        buttonUpdate = findViewById(R.id.buttonUpdateEvent);

        // 获取传递的 event_id
        String eventId = getIntent().getStringExtra("event_id");

        // 加载活动数据并填充
        loadEventData(eventId);

        // 设置更新按钮点击事件
        buttonUpdate.setOnClickListener(v -> updateEventData(eventId));
    }

    @Override
    protected void onResume() {
        super.onResume();
        String eventId = getIntent().getStringExtra("event_id");  // 从Intent获取eventId
        if (eventId != null) {
            loadEventData(eventId);  // 加载指定活动的数据
        }
    }


    private void loadEventData(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        OrganizerHomeEventModel event = documentSnapshot.toObject(OrganizerHomeEventModel.class);
                        if (event != null) {
                            editTextEventName.setText(event.getName());
                            editTextEventDescription.setText(event.getDescription());
                            editTextEventTime.setText(event.getTime());
                            editTextRegistrationDeadline.setText(event.getRegistrationDeadline());
                            editTextLotteryTime.setText(event.getLotteryTime());
                            editTextWaitingListCount.setText(String.valueOf(event.getWaitingListCount()));
                            editTextLotteryCount.setText(String.valueOf(event.getLotteryCount()));

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

    private void updateEventData(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).update(
                "name", editTextEventName.getText().toString(),
                "time", editTextEventTime.getText().toString(),
                "registrationDeadline", editTextRegistrationDeadline.getText().toString(),
                "lotteryTime", editTextLotteryTime.getText().toString(),
                "waitingListCount", Integer.parseInt(editTextWaitingListCount.getText().toString()),
                "lotteryCount", Integer.parseInt(editTextLotteryCount.getText().toString()),
                "description", editTextEventDescription.getText().toString()

        ).addOnSuccessListener(aVoid -> {
            // 更新成功提示
        }).addOnFailureListener(e -> {
            // 更新失败处理
        });
    }
}