package com.example.single_lottery.ui.admin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.single_lottery.MainActivity;
import com.example.single_lottery.R;
import com.example.single_lottery.ui.organizer.OrganizerActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity class for the admin interface that provides administrative functions and management features.
 * This is the main entry point for admin users to manage the lottery system.
 *
 * @author [Haorui Gao]
 * @version 1.0
 * @see AppCompatActivity
 * @since 1.0
 */

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_homepage);
        getSupportActionBar().setTitle("Events");

        BottomNavigationView bottomNavigationView = findViewById(R.id.admin_nav_bottom_navigation);
        // 默认加载 AdminEventFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_fragment_container, new AdminEventFragment())
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;

            if (item.getItemId() == R.id.nav_event) {
                selectedFragment = new AdminEventFragment();
                getSupportActionBar().setTitle("Events");
            } else if (item.getItemId() == R.id.nav_organizer) {
                selectedFragment = new AdminOrganizerFragment();
                getSupportActionBar().setTitle("Organizers");
            } else if (item.getItemId() == R.id.nav_user) {
                selectedFragment = new AdminUserFragment();
                getSupportActionBar().setTitle("Users");
            } else if (item.getItemId() == R.id.nav_facility) {
                selectedFragment = new AdminFacilityFragment(); // 新增 Facility Fragment
                getSupportActionBar().setTitle("Facilities");
            } else {
                return false;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_fragment_container, selectedFragment)
                    .commit();
            return true;
        });
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
            Intent intent = new Intent(AdminActivity.this, MainActivity.class);
            intent.putExtra("showLandingScreen", true); // Ensure you have the landing screen logic in MainActivity
            startActivity(intent);
            finish();  // Optionally finish the current activity
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}