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
/**
 * Activity for handling notifications in the Single Lottery application.
 * Provides functionality to send and manage user notifications for events.
 *
 * @author [Gabriel Bautista]
 * @version 1.0
 */
public class NotificationActivity extends AppCompatActivity {
    /** Channel ID for notification grouping */
    private static final String CHANNEL_ID = "notifications";
    private static final String TAG = "NotificationActivity";
    /**
     * Initializes the activity and sets up the notification view.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
    }
    /**
     * Sends a notification to a specific user.
     * Creates notification channel for Android O and above.
     * Assigns unique notification ID based on user ID.
     *
     * @param context Application context for creating notification
     * @param title Title text of the notification
     * @param message Content text of the notification
     * @param userId ID of the user to receive notification, used for unique notification ID
     */
    public static void sendNotification(Context context, String title, String message, String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.d("NotificationActivity", "No users to notify.");
            return;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notifications", "Event Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifications")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        // Display the notification with unique ID
        int notificationId = userId.hashCode(); // Unique ID per user
        notificationManager.notify(notificationId, builder.build());

    }

}
