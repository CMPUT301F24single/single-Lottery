package com.example.single_lottery.ui.admin;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
/**
 * Activity for displaying events associated with a specific facility.
 * Loads and displays AdminEventFragment with facility-specific filtering.
 * Handles navigation and toolbar setup for facility event view.
 *
 * @author [Jingyao Gu]
 * @author [Aaron kim]
 * @version 1.0
 */
public class AdminFacilityEventActivity extends AppCompatActivity {
    /** Key for passing facility name through intent or bundle */
    public static final String facility_name = "facility_name";
    /**
     * Initializes the activity and sets up the AdminEventFragment.
     * Configures action bar with facility name and back navigation.
     * Passes facility name to fragment for event filtering.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        AdminEventFragment adminEventFragment = new AdminEventFragment();
        String facility = getIntent().getStringExtra(facility_name);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Events at " + facility);
        }

        if (savedInstanceState == null){
            Bundle bundle = new Bundle();
            bundle.putString(facility_name, facility);
            adminEventFragment.setArguments(bundle);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(android.R.id.content, adminEventFragment);
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
