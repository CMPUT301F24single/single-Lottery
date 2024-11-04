
package com.example.single_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.single_lottery.ui.admin.AdminActivity;
import com.example.single_lottery.ui.organizer.OrganizerActivity;

import com.example.single_lottery.ui.user.UserActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private boolean showLandingScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 检查是否显示初始的 landing screen
        showLandingScreen = getIntent().getBooleanExtra("showLandingScreen", true);

        if (showLandingScreen) {
            setContentView(R.layout.activity_landing);

            Button buttonUser = findViewById(R.id.button_user);
            Button buttonOrganizer = findViewById(R.id.button_organizer);
            Button buttonAdmin = findViewById(R.id.button_admin);

            // 点击任意按钮时进行抽奖检查
            View.OnClickListener listener = v -> {
                performLotteryCheck(); // 执行抽奖检查

                // 根据点击的按钮跳转到不同的活动
                Intent intent;
                if (v.getId() == R.id.button_user) {
                    intent = new Intent(MainActivity.this, UserActivity.class);
                } else if (v.getId() == R.id.button_organizer) {
                    intent = new Intent(MainActivity.this, OrganizerActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, AdminActivity.class);
                }
                intent.putExtra("showLandingScreen", false);
                startActivity(intent);
                finish();
            };

            buttonUser.setOnClickListener(listener);
            buttonOrganizer.setOnClickListener(listener);
            buttonAdmin.setOnClickListener(listener);
        }
    }

    // 抽奖检查功能
    private void performLotteryCheck() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String eventId = document.getId();
                        String lotteryTime = document.getString("lotteryTime");
                        int lotteryCount = document.getLong("lotteryCount").intValue();

                        // 检查是否到达抽奖时间
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        try {
                            Date lotteryDate = dateFormat.parse(lotteryTime);
                            Date currentDate = new Date();
                            if (currentDate.after(lotteryDate)) {
                                checkIfAlreadyDrawn(eventId, lotteryCount);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Error checking lottery time", e));
    }

    // 检查是否已经进行过抽奖
    private void checkIfAlreadyDrawn(String eventId, int lotteryCount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("registered_events")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "Winner") // 检查是否已有 "Winner" 状态的记录
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // 如果没有 "Winner" 状态的记录，执行抽奖
                        performLottery(eventId, lotteryCount);
                    } else {
                        Log.d("MainActivity", "Lottery already performed for event: " + eventId);
                    }
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Error checking if lottery already drawn", e));
    }

    // 执行抽奖
    private void performLottery(String eventId, int lotteryCount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("registered_events")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> registeredUsers = queryDocumentSnapshots.getDocuments();
                        int winnersCount = Math.min(registeredUsers.size(), lotteryCount);

                        // 随机抽奖逻辑
                        Collections.shuffle(registeredUsers); // 随机打乱报名用户
                        List<DocumentSnapshot> winners = registeredUsers.subList(0, winnersCount);

                        // 更新赢家状态
                        for (DocumentSnapshot winnerDoc : winners) {
                            db.collection("registered_events").document(winnerDoc.getId())
                                    .update("status", "Winner")
                                    .addOnSuccessListener(aVoid -> Log.d("Lottery", "User " + winnerDoc.getString("userId") + " has won the lottery"))
                                    .addOnFailureListener(e -> Log.e("Lottery", "Failed to update winner status", e));
                        }

                        // 更新非赢家状态
                        List<DocumentSnapshot> losers = registeredUsers.subList(winnersCount, registeredUsers.size());
                        for (DocumentSnapshot loserDoc : losers) {
                            db.collection("registered_events").document(loserDoc.getId())
                                    .update("status", "Not Selected")
                                    .addOnSuccessListener(aVoid -> Log.d("Lottery", "User " + loserDoc.getString("userId") + " did not win"))
                                    .addOnFailureListener(e -> Log.e("Lottery", "Failed to update loser status", e));
                        }

                        Toast.makeText(this, "Lottery completed. Winners have been selected.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("Lottery", "No users registered for event " + eventId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Lottery", "Error performing lottery", e));
    }
}
