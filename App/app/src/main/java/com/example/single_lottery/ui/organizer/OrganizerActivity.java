package com.example.single_lottery.ui.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.single_lottery.MainActivity;
import com.example.single_lottery.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * The main control activity for event organizers in the Single Lottery application.
 * This activity serves as the primary navigation hub for organizers, providing access to:
 * - Event management through OrganizerHomeFragment
 * - Event creation via OrganizerEventCreateActivity
 * - Profile management using OrganizerProfilePageFragment
 */
public class OrganizerActivity extends AppCompatActivity {

    /**
     * Initializes and sets up the organizer's main interface.
     * Configures the bottom navigation with three main sections:
     * - Home: Displays event management interface via OrganizerHomeFragment
     * - New Event: Launches event creation interface via OrganizerEventCreateActivity
     * - Profile: Shows organizer profile management via OrganizerProfilePageFragment
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_activity);

        BottomNavigationView navView = findViewById(R.id.nav_view_organizer);

        navView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.navigation_home) {
                selectedFragment = new OrganizerHomeFragment();
            } else if (item.getItemId() == R.id.navigation_new) {
                Intent intent = new Intent(OrganizerActivity.this, OrganizerEventCreateActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.navigation_profile) {
                selectedFragment = new OrganizerProfilePageFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment_organizer, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Initialize with the default fragment (OrganizerHomeFragment)
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment_organizer, new OrganizerHomeFragment())
                .commit();
        navView.setSelectedItemId(R.id.navigation_home);  // Make sure "Home" is selected initially
    }

    @Override
    public void onResume() {
        super.onResume();

        // When coming back from the event creation activity, ensure the correct tab is selected
        BottomNavigationView navView = findViewById(R.id.nav_view_organizer);
        navView.setSelectedItemId(R.id.navigation_home);  // Set Home tab selected when returning
    }

    // Inflate the menu in the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actionbar, menu);
        return true;
    }

    // Handle the item selection in the action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_return) {
            // Return to homepage when the icon is clicked
            Intent intent = new Intent(OrganizerActivity.this, MainActivity.class);
            intent.putExtra("showLandingScreen", true); // Ensure you have the landing screen logic in MainActivity
            startActivity(intent);
            finish();  // Optionally finish the current activity
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
