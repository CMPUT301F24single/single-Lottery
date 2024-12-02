package com.example.single_lottery.ui.admin;

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
 * Activity class for the admin interface that provides administrative functions
 * and management features.
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
            } else if (item.getItemId() == R.id.nav_organizer) {
                selectedFragment = new AdminOrganizerFragment();
            } else if (item.getItemId() == R.id.nav_user) {
                selectedFragment = new AdminUserFragment();
            } else if (item.getItemId() == R.id.nav_facility) {
                selectedFragment = new AdminFacilityFragment(); // 新增 Facility Fragment
            } else {
                return false;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_fragment_container, selectedFragment)
                    .commit();
            return true;
        });
}
