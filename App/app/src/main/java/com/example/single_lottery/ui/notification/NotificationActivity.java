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
/**
 * Activity for managing and sending system notifications.
 * Handles notification creation and delivery to different user groups.
 *
 * @author Haorui Gao
 * @version 1.0
 */
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    /** Channel ID for notification category */
    private static final String CHANNEL_ID = "notifications";
    /** Tag for logging */
    private static final String TAG = "NotificationActivity";
    /**
     * Creates and initializes the notification activity.
     *
     * @param savedInstanceState Saved instance state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
    }
    /**
     * Sends a notification to a specific user group.
     * Creates notification channel if needed and assigns notification ID based on group.
     * Groups include: winner, waiting, loser, accepted, cancelled.
     *
     * @param context Application context
     * @param title Notification title
     * @param message Notification message content
     * @param userGroup Target user group for notification
     */
    public static void sendNotification(Context context, String title, String message, List<String> userIds) {
        // Log user group for debugging
        // Validate user group
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

        // Only send if valid group
        if (notificationId == 0) {
            Log.d(TAG, "Notification not sent. Unknown user group.");
            return;
        }

        // Show notification
        notificationManager.notify(notificationId, builder.build());
        Log.d(TAG, "Notification sent to " + userGroup + " group.");
    }

}
