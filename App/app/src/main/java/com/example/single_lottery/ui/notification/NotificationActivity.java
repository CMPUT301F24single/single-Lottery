package com.example.single_lottery.ui.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.single_lottery.R;

public class NotificationActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "notifications";
    private static final String TAG = "NotificationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
    }

    public static void sendNotification(Context context, String title, String message, String userGroup) {
        Log.d(TAG, "User group: " + userGroup);
        if (userGroup == null || userGroup.isEmpty()) {
            Log.d(TAG, "Notification not sent. User group is not specified.");
            return;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notification", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Determine notification ID based on user group
        int notificationId;
        switch (userGroup.toLowerCase()) {
            case "winner":
                notificationId = 1;
                break;
            case "waiting":
                notificationId = 2;
                break;
            case "loser":
                notificationId = 3;
                break;
            case "accepted":
                notificationId = 4;
                break;
            case "cancelled":
                notificationId = 5;
                break;
            default:
                notificationId = 0;
                break;
        }

        if (notificationId == 0) {
            Log.d(TAG, "Notification not sent. Unknown user group.");
            return;
        }

        // Show notification
        notificationManager.notify(notificationId, builder.build());
        Log.d(TAG, "Notification sent to " + userGroup + " group.");
    }
}
