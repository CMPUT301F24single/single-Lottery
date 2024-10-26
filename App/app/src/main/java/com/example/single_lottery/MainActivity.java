package com.example.single_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.single_lottery.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean showLandingScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if we should show the landing screen
        showLandingScreen = getIntent().getBooleanExtra("showLandingScreen", true);

        if (showLandingScreen) {
            // Use the new landing layout with User, Organizer, and Admin buttons
            setContentView(R.layout.activity_landing);

            // Get references to the buttons
            Button buttonUser = findViewById(R.id.button_user);
            Button buttonOrganizer = findViewById(R.id.button_organizer);
            Button buttonAdmin = findViewById(R.id.button_admin);

            // Set click events for each button
            buttonUser.setOnClickListener(v -> {
                // Start a new instance of MainActivity with showLandingScreen as false
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.putExtra("showLandingScreen", false);
                startActivity(intent);
                finish(); // Close current instance
            });

            buttonOrganizer.setOnClickListener(v -> {
                // Start a new instance of MainActivity with showLandingScreen as false
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.putExtra("showLandingScreen", false);
                startActivity(intent);
                finish(); // Close current instance
            });

            buttonAdmin.setOnClickListener(v -> {
                // Handle Admin logic here
            });
        } else {
            // Original code to show the main interface with bottom navigation and fragments
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            BottomNavigationView navView = findViewById(R.id.nav_view);

            // Configure AppBar with top-level destinations
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                    .build();
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(binding.navView, navController);

            // Navigate to HomeFragment by default after landing screen
            navController.navigate(R.id.navigation_home);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
