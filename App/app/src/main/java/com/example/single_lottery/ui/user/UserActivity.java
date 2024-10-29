package com.example.single_lottery.ui.user;

import android.os.Bundle;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.single_lottery.R;
import com.example.single_lottery.ui.events.EventsFragment;
import com.example.single_lottery.ui.home.HomeFragment;
import com.example.single_lottery.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_homepage); // 使用新的布局文件

        BottomNavigationView navView = findViewById(R.id.nav_view_user);

        navView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.navigation_dashboard) {
                selectedFragment = new EventsFragment();
            } else if (item.getItemId() == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment_user, selectedFragment)
                        .commit();
            }
            return true;
        });

        // 默认加载 Home Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment_user, new HomeFragment())
                .commit();
    }
}
