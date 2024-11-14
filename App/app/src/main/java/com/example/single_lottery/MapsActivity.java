package com.example.single_lottery;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.single_lottery.databinding.ActivityMapsBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Activity for displaying event location on Google Maps.
 * Shows event venue with marker at University of Alberta CCIS building.
 *
 * @author [Haorui Gao]
 * @version 1.0
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String eventId = getIntent().getStringExtra("eventId");
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
        String eventId = getIntent().getStringExtra("event_id");
        loadLocations(eventId);
    }
}