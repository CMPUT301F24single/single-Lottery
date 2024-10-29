
package com.example.single_lottery;

import android.os.Bundle;

import com.example.single_lottery.ui.organizer.OrganizerActivity;
import com.example.single_lottery.ui.role.RoleFragment;
import com.example.single_lottery.ui.user.UserActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.single_lottery.databinding.ActivityMainBinding;

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
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
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
        } else {
            // Load RoleFragment for User role
            setContentView(R.layout.activity_main); // Ensure you have an empty container for fragments
            String role = getIntent().getStringExtra("role");
            if ("user".equals(role)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_main, new RoleFragment())
                        .commit();
            }
        }
    }
}
