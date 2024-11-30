package com.example.single_lottery.ui.admin;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class AdminFacilityEventActivity extends AppCompatActivity {

    public static final String facility_name = "facility_name";
    @Override

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        AdminEventFragment adminEventFragment = new AdminEventFragment();
        String facility = getIntent().getStringExtra(facility_name);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Events at: " + facility);
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
