package com.example.single_lottery.ui.organizer;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
/**
 * Activity for displaying the facility profile interface.
 * Handles initialization of the FacilityProfileFragment and navigation controls.
 *
 * @author [Aaron kim]
 * @version 1.0
 */
public class FacilityProfile extends AppCompatActivity {
    /**
     * Initializes the activity and sets up the fragment.
     * Configures action bar with title and back navigation.
     * Loads FacilityProfileFragment if no saved state exists.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Facility Profile");
        }

        if (savedInstanceState == null){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(android.R.id.content, new FacilityProfileFragment());
            transaction.commit();
        }
    }
    /**
     * Handles the back button press in the action bar.
     * Returns to the previous screen when back navigation is triggered.
     *
     * @return true if the navigation was handled
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}