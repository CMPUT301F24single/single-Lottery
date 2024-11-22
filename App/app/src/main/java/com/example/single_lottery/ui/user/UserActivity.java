package com.example.single_lottery.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.single_lottery.MainActivity;
import com.example.single_lottery.R;
import com.example.single_lottery.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main activity for user interface.
 * Manages bottom navigation and fragment navigation between Home, Events and Profile sections.
 *
 * @author [Haorui Gao]
 * @version 1.0
 */
public class UserActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    /**
     * Initializes user interface and sets up navigation.
     * Configures bottom navigation view and navigation controller.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        navController.navigate(R.id.navigation_home);
    }

    /**
     * Handles navigation up action.
     * Required for proper back navigation.
     *
     * @return true if navigation handled, false otherwise
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu to add items to the action bar if present
        getMenuInflater().inflate(R.menu.menu_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_return) {
            // Handle the return to homepage
            Intent intent = new Intent(UserActivity.this, MainActivity.class);
            intent.putExtra("showLandingScreen", true); // Ensure the landing screen is shown
            startActivity(intent);
            finish();  // Optionally finish the current activity so the user can't go back to it
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
