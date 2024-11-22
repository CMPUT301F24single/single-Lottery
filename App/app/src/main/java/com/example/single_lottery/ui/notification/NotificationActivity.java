package com.example.single_lottery.ui.notification;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.single_lottery.R;
import com.example.single_lottery.ui.user.events.UserEventDetailActivity;

public class NotificationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        TextView messageView = findViewById(R.id.messageView);
        String message = getIntent().getStringExtra("message");
        messageView.setText(message);

        String eventId = getIntent().getStringExtra("event_id");

        messageView.setOnClickListener(v -> {
            if (eventId != null) {
                Intent intent = new Intent(NotificationActivity.this, UserEventDetailActivity.class);
                intent.putExtra("event_id", eventId);
                startActivity(intent);
                finish(); 
            }
        });
    }
}
