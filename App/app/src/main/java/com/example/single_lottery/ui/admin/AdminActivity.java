package com.example.single_lottery.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.example.single_lottery.MainActivity;
import com.example.single_lottery.R;

public class AdminActivity extends AppCompatActivity {

    /**
     * Initializes the admin interface and sets up the necessary components.
     * This method is called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                          this Bundle contains the data it most recently supplied.
     *                          Otherwise it is null.
     * @see Bundle
     * @see AppCompatActivity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_homepage); // Set the layout for the admin homepage
    }

    // Inflate the menu in the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actionbar, menu); // Ensure you have the correct menu XML
        return true;
    }

    // Handle the item selection in the action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_return) {
            // Return to homepage when the icon is clicked
            Intent intent = new Intent(AdminActivity.this, MainActivity.class);
            intent.putExtra("showLandingScreen", true); // Optionally pass extra to show the landing screen
            startActivity(intent);
            finish();  // Optionally finish the current activity
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
