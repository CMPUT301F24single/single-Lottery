
package com.example.single_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.single_lottery.ui.organizer.OrganizerActivity;
import com.example.single_lottery.ui.user.UserActivity;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private boolean showLandingScreen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if we should show the landing screen
        showLandingScreen = getIntent().getBooleanExtra("showLandingScreen", true);

        if (showLandingScreen) {
            // Use the landing layout with User, Organizer, and Admin buttons
            setContentView(R.layout.activity_landing);

            Button buttonUser = findViewById(R.id.button_user);
            Button buttonOrganizer = findViewById(R.id.button_organizer);
            Button buttonAdmin = findViewById(R.id.button_admin);

            buttonUser.setOnClickListener(v -> {
                // Start RoleFragment for User
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                intent.putExtra("showLandingScreen", false);
                intent.putExtra("role", "user");
                startActivity(intent);
                finish();
            });

            buttonOrganizer.setOnClickListener(v -> {
                // Start OrganizerActivity for Organizer
                Intent intent = new Intent(MainActivity.this, OrganizerActivity.class);
                intent.putExtra("showLandingScreen", false);
                intent.putExtra("role", "user");
                startActivity(intent);
                finish();
            });

            buttonAdmin.setOnClickListener(v -> {
                // Handle Admin logic here
            });


        }
    }
}