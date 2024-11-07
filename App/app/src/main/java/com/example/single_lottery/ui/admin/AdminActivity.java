package com.example.single_lottery.ui.admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.single_lottery.R;
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
        setContentView(R.layout.admin_homepage); // need to create a corresponding layout file

    }
}
