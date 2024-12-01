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

import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "notifications";
    private static final String TAG = "NotificationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
    }

    public static void sendNotification(Context context, String title, String message, List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            Log.d("NotificationActivity", "No users to notify.");
            return;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notifications", "Event Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        for (String userId : userIds) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifications")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            int notificationId = userId.hashCode(); // Unique ID per user
            notificationManager.notify(notificationId, builder.build());
        }
    }

}
