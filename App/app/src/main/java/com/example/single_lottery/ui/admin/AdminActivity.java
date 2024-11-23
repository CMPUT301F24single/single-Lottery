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

        BottomNavigationView bottomNavigationView = findViewById(R.id.admin_nav_bottom_navigation);

        // 默认加载 AdminEventFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_fragment_container, new AdminEventFragment())
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;

            // 根据选择的菜单项切换 Fragment
            if (item.getItemId() == R.id.nav_event) {
                selectedFragment = new AdminEventFragment();
            } else if (item.getItemId() == R.id.nav_organizer) {
                selectedFragment = new AdminOrganizerFragment();
            } else if (item.getItemId() == R.id.nav_user) {
                selectedFragment = new AdminUserFragment();
            } else {
                return false;
            }

            // 切换到选中的 Fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_fragment_container, selectedFragment)
                    .commit();
            return true;
        });
    }
}