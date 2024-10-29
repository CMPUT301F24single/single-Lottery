package com.example.single_lottery.ui.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.example.single_lottery.R;
import com.example.single_lottery.ui.home.HomeFragment;
import com.example.single_lottery.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OrganizerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("OrganizerEventCreateActivity", "onCreate called");
        setContentView(R.layout.organizer_homepage);

        BottomNavigationView navView = findViewById(R.id.nav_view_organizer);

        navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_new) {
                Intent intent = new Intent(OrganizerActivity.this, OrganizerEventCreateActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.navigation_home) {
                loadHomeFragment();
                return true;
            } else if (item.getItemId() == R.id.navigation_profile) {
                loadProfileFragment();
                return true;
            }
            return false;
        });

    }
    private void loadHomeFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment_organizer, new HomeFragment())
                .commit();
    }
    private void loadProfileFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment_organizer, new ProfileFragment())
                .commit();
    }
}