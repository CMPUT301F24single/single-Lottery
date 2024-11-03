
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
            // 使用包含 User, Organizer, 和 Admin 按钮的 landing 布局
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
                    // Admin按钮的处理（这里跳转到AdminActivity）
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
                .whereEqualTo("lotteryCompleted", false) // 仅找未完成抽奖的活动
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
                                performLottery(eventId, lotteryCount);
                            }
                        } catch (ParseException e) {
                            Log.e("MainActivity", "Error parsing lottery time", e);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Error checking lottery time", e));
    }

    // 抽奖功能
    private void performLottery(String eventId, int lotteryCount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Step 1: Check if lottery has already been completed
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean lotteryCompleted = documentSnapshot.getBoolean("lotteryCompleted");
                        if (lotteryCompleted != null && lotteryCompleted) {
                            Log.d("Lottery", "Lottery already completed for this event. Skipping...");
                        } else {
                            // Proceed with lottery
                            executeLottery(eventId, lotteryCount);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Lottery", "Failed to check lottery status", e));
    }

    private void executeLottery(String eventId, int lotteryCount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("registered_events")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> registeredUsers = querySnapshot.getDocuments();
                    int winnersCount = Math.min(registeredUsers.size(), lotteryCount);  // 使用传入的 lotteryCount

                    // Select winners
                    List<DocumentSnapshot> winners = registeredUsers.subList(0, winnersCount);
                    for (DocumentSnapshot winnerDoc : winners) {
                        db.collection("registered_events").document(winnerDoc.getId())
                                .update("status", "Winner")
                                .addOnSuccessListener(aVoid -> Log.d("Lottery", "User " + winnerDoc.getString("userId") + " has won the lottery"))
                                .addOnFailureListener(e -> Log.e("Lottery", "Failed to update winner status", e));
                    }

                    // Select losers
                    List<DocumentSnapshot> losers = registeredUsers.subList(winnersCount, registeredUsers.size());
                    for (DocumentSnapshot loserDoc : losers) {
                        db.collection("registered_events").document(loserDoc.getId())
                                .update("status", "Not Selected")
                                .addOnSuccessListener(aVoid -> Log.d("Lottery", "User " + loserDoc.getString("userId") + " did not win"))
                                .addOnFailureListener(e -> Log.e("Lottery", "Failed to update loser status", e));
                    }

                    // Step 3: Mark lottery as completed
                    db.collection("events").document(eventId)
                            .update("lotteryCompleted", true)
                            .addOnSuccessListener(aVoid -> Log.d("Lottery", "Lottery marked as completed"))
                            .addOnFailureListener(e -> Log.e("Lottery", "Failed to mark lottery as completed", e));
                })
                .addOnFailureListener(e -> Log.e("Lottery", "Error fetching registered users", e));
    }

}

