package com.example.single_lottery.ui.organizer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.single_lottery.MapsActivity;

import com.bumptech.glide.Glide;
import com.example.single_lottery.R;
import com.example.single_lottery.EventModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizerHomeViewEventActivity extends AppCompatActivity {

    private TextView textViewEventName, textViewEventTime, textViewRegistrationDeadline,
            textViewLotteryTime, textViewWaitingListCount, textViewLotteryCount, textViewEventDescription;
    private ImageView imageViewPoster;
    private Button buttonViewWaitingList, buttonViewSelectedUsers, buttonViewAcceptedUsers, buttonGenerateQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_home_view_event);

        ImageButton backButton = findViewById(R.id.backButton);
        buttonGenerateQRCode = findViewById(R.id.buttonGenerateQRCode);
        backButton.setOnClickListener(v -> finish()); // 返回上一个页面

        Button mapButton = findViewById(R.id.mapButton);
        mapButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerHomeViewEventActivity.this, MapsActivity.class);
            startActivity(intent);
        });


        // 初始化视图
        textViewEventName = findViewById(R.id.textViewEventName);
        textViewEventDescription = findViewById(R.id.textViewEventDescription);
        textViewEventTime = findViewById(R.id.textViewEventTime);
        textViewRegistrationDeadline = findViewById(R.id.textViewRegistrationDeadline);
        textViewLotteryTime = findViewById(R.id.textViewLotteryTime);
        textViewWaitingListCount = findViewById(R.id.textViewWaitingListCount);
        textViewLotteryCount = findViewById(R.id.textViewLotteryCount);
        imageViewPoster = findViewById(R.id.imageViewPoster);
        buttonViewWaitingList = findViewById(R.id.buttonViewWaitingList);
        buttonViewSelectedUsers = findViewById(R.id.buttonViewSelectedUsers);
        buttonViewAcceptedUsers = findViewById(R.id.buttonViewAcceptedUsers);

        // 获取传递的 event_id
        String eventId = getIntent().getStringExtra("event_id");

        // 从 Firestore 加载活动数据
        loadEventData(eventId);

        buttonGenerateQRCode.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerHomeViewEventActivity.this, QRCodeActivity.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });

        // 设置查看等待名单按钮的点击事件
        buttonViewWaitingList.setOnClickListener(v -> viewWaitingList(eventId));

        // 设置查看选中和未选中用户按钮的点击事件
        buttonViewSelectedUsers.setOnClickListener(v -> viewSelectedAndNotSelectedUsers(eventId));

        // 设置查看已接受用户按钮的点击事件
        buttonViewAcceptedUsers.setOnClickListener(v -> viewAcceptedUsers(eventId));
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
                        EventModel event = documentSnapshot.toObject(EventModel.class);
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

    // 查看等待名单的方法
    private void viewWaitingList(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("registered_events")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    StringBuilder waitingList = new StringBuilder();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String userId = document.getString("userId");
                        waitingList.append(userId).append("\n");
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Waiting List")
                            .setMessage(waitingList.toString())
                            .setPositiveButton("OK", null)
                            .show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load waiting list.", Toast.LENGTH_SHORT).show());
    }

    // 查看选中和未选中用户的方法
    private void viewSelectedAndNotSelectedUsers(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("registered_events")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    StringBuilder winnersList = new StringBuilder("Winners:\n");
                    StringBuilder nonWinnersList = new StringBuilder("Non-Winners:\n");

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String userId = document.getString("userId");
                        String status = document.getString("status");

                        // 检查并添加用户到相应的名单
                        if ("Winner".equals(status)) {
                            winnersList.append(userId).append("\n");
                        } else if ("Not Selected".equals(status)) {
                            nonWinnersList.append(userId).append("\n");
                        }
                    }

                    // 确保数据显示在弹出框中
                    new AlertDialog.Builder(this)
                            .setTitle("Selected Users")
                            .setMessage(winnersList.toString() + "\n" + nonWinnersList.toString())
                            .setPositiveButton("OK", null)
                            .show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load selected users.", Toast.LENGTH_SHORT).show());
    }


    // 查看已接受用户的方法
    private void viewAcceptedUsers(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("registered_events")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "Accepted")  // 仅查询状态为 Accepted 的用户
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    StringBuilder acceptedUsersList = new StringBuilder();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String userId = document.getString("userId");
                        acceptedUsersList.append(userId).append("\n");
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Accepted Users")
                            .setMessage(acceptedUsersList.toString())
                            .setPositiveButton("OK", null)
                            .show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load accepted users.", Toast.LENGTH_SHORT).show());
    }
}
