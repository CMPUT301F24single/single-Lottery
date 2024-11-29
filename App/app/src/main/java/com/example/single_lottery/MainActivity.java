
package com.example.single_lottery;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;

import com.example.single_lottery.ui.admin.AdminLoginActivity;
import com.example.single_lottery.ui.organizer.OrganizerActivity;

import com.example.single_lottery.ui.user.UserActivity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.OneTimeWorkRequest;

import java.util.concurrent.TimeUnit;

/**
 * Main entry point activity for Single Lottery application.
 * Handles role selection and automatic lottery execution for events.
 *
 * @author [Haorui Gao]
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    private boolean showLandingScreen;
    private static final int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scheduleLotteryWorker();


        // Check if the initial landing screen is displayed
        showLandingScreen = getIntent().getBooleanExtra("showLandingScreen", true);

        if (showLandingScreen) {
            setContentView(R.layout.activity_landing);

            Button buttonUser = findViewById(R.id.button_user);
            Button buttonOrganizer = findViewById(R.id.button_organizer);
            Button buttonAdmin = findViewById(R.id.button_admin);
            //ImageView event_alert = findViewById(R.id.event_alert);

            // Set up button click listeners
            View.OnClickListener listener = v -> {
                OneTimeWorkRequest lotteryWorkRequest = new OneTimeWorkRequest.Builder(LotteryWorker.class).build();
                WorkManager.getInstance(this).enqueue(lotteryWorkRequest);

                Intent intent = null;

                if (v.getId() == R.id.button_user) {
                    intent = new Intent(MainActivity.this, UserActivity.class);
                } else if (v.getId() == R.id.button_organizer) {
                    intent = new Intent(MainActivity.this, OrganizerActivity.class);
                } else if (v.getId() == R.id.button_admin) {
                    // Admin Login verification
                    intent = new Intent(MainActivity.this, AdminLoginActivity.class);
                }

                if (intent != null) {
                    intent.putExtra("showLandingScreen", false);
                    startActivity(intent);
                    finish();
                }
            };

            buttonUser.setOnClickListener(listener);
            buttonOrganizer.setOnClickListener(listener);
            buttonAdmin.setOnClickListener(listener);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "Notification permission granted.");
            } else {
                Log.d("Permission", "Notification permission denied.");
                Toast.makeText(this, "You need to grant notification permission to use Single Lottery.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void scheduleLotteryWorker() {
        PeriodicWorkRequest lotteryWorkRequest = new PeriodicWorkRequest.Builder(LotteryWorker.class,
                15, TimeUnit.MINUTES // Check every 15 mins
        ).build();
        WorkManager.getInstance(this).enqueue(lotteryWorkRequest);
    }
}

