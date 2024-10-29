
package com.example.single_lottery.ui.organizer;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import com.example.single_lottery.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OrganizerActivity extends AppCompatActivity {

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


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment_organizer, new OrganizerHomeFragment())
                .commit();
    }
}
