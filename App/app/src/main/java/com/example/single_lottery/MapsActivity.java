package com.example.single_lottery;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.example.single_lottery.ui.organizer.OrganizerHomeViewEventActivity;
import com.example.single_lottery.ui.user.UserActivity;
import com.google.android.gms.maps.model.LatLng;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.single_lottery.R;
import com.example.single_lottery.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private com.example.single_lottery.databinding.ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Map");

        String eventId = getIntent().getStringExtra("eventId");  // Corrected key for event ID
        Log.d("MapsActivity", "Event ID:" + eventId);
        loadLocations(eventId);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }

    private void loadLocations(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("MapsActivity", "Event ID:" + eventId);

        if (eventId == null) {
            Log.e("MapsActivity", "Event ID is null");
            return;
        }

        db.collection("user_locations")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("MapsActivity", "Documents found: " + queryDocumentSnapshots.size());
                    ArrayList<LatLng> locations = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        double latitude = document.getDouble("latitude");
                        double longitude = document.getDouble("longitude");
                        Log.d("MapsActivity", "Latitude: " + latitude + ", Longitude: " + longitude);
                        locations.add(new LatLng(latitude, longitude));
                    }
                    if (mMap != null) {
                        addMarkersToMap(locations);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load locations.", Toast.LENGTH_SHORT).show();
                    Log.e("MapsActivity", "Error getting documents: ", e);
                });
    }

    private void addMarkersToMap(ArrayList<LatLng> locations) {
        if (locations != null && !locations.isEmpty()) {
            for (LatLng location : locations) {
                mMap.addMarker(new MarkerOptions().position(location));
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locations.get(0), 15));
        } else {
            Toast.makeText(this, "No locations found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String eventId = getIntent().getStringExtra("eventId");  // Corrected key for event ID
        loadLocations(eventId);
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
        getMenuInflater().inflate(R.menu.map_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Handle toolbar navigation
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.map_action_return) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
