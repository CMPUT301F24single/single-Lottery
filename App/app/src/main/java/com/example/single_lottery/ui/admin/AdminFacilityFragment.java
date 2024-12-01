package com.example.single_lottery.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.R;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * Fragment for managing facilities in admin view.
 * Shows list of facilities and their associated events, with ability to delete facilities.
 *
 * @author Jingyao Gu
 * @version 1.0
 */
public class AdminFacilityFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminFacilityAdapter facilityAdapter;
    private List<Map<String, String>> facilitiesWithEvents = new ArrayList<>();

    /**
     * Creates and initializes the fragment's user interface.
     * Sets up RecyclerView with adapter and loads facility data.
     *
     * @param inflater The layout inflater
     * @param container The parent view container
     * @param savedInstanceState Saved instance state bundle
     * @return The created fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_facility, container, false);

        recyclerView = view.findViewById(R.id.facilityRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        facilityAdapter = new AdminFacilityAdapter(facilitiesWithEvents, this::deleteEventsByFacility);
        recyclerView.setAdapter(facilityAdapter);

        // Add DividerItemDecoration (for dividing list of events)
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(),
                LinearLayoutManager.VERTICAL
        );
        recyclerView.addItemDecoration(dividerItemDecoration);

        loadFacilitiesFromFirestore();

        return view;
    }

    /**
     * Loads facilities and their associated events from Firestore.
     * Clears existing list and updates with fresh data.
     * Updates adapter when data is loaded.
     */
    private void loadFacilitiesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    facilitiesWithEvents.clear(); // clear old data

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String facility = document.getString("facility");
                        String eventName = document.getString("name"); // get event name

                        if (facility != null && !facility.isEmpty() && eventName != null && !eventName.isEmpty()) {
                            Map<String, String> facilityEvent = new HashMap<>();
                            facilityEvent.put("facility", facility);
                            facilityEvent.put("eventName", eventName);
                            facilitiesWithEvents.add(facilityEvent); // add to list
                        }
                    }

                    facilityAdapter.notifyDataSetChanged(); // update RecyclerView
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load facilities.", Toast.LENGTH_SHORT).show();
                });
    }
    /**
     * Deletes all events associated with a specific facility.
     * Uses batch operation to delete multiple events atomically.
     * Updates UI after successful deletion.
     *
     * @param facility The name of the facility whose events should be deleted
     */
    private void deleteEventsByFacility(String facility) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .whereEqualTo("facility", facility)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        batch.delete(document.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "All events with facility deleted successfully.", Toast.LENGTH_SHORT).show();
                                // Remove all records of this facility
                                facilitiesWithEvents.removeIf(map -> map.get("facility").equals(facility));
                                facilityAdapter.notifyDataSetChanged(); // renew UI
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to delete events.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to find events.", Toast.LENGTH_SHORT).show();
                });
    }
}