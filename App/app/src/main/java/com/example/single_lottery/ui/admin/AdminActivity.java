package com.example.single_lottery.ui.admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.single_lottery.R;
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

        // 初始化 BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.admin_nav_bottom_navigation);

        // 默认加载 EventFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_fragment_container, new AdminEventFragment())
                    .commit();
        }

        // 设置底部导航栏的切换逻辑
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_event) {
                selectedFragment = new AdminEventFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new AdminProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.admin_fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });
    }
}
