package com.example.single_lottery.ui.admin;

import android.os.Bundle;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Fragment for managing facilities in admin view.
 * Shows list of facilities and their associated events, with ability to delete facilities.
 * Handles loading and managing facility data from Firestore.
 *
 * @author [Jingyao Gu]
 * @author [Aaron kim]
 * @version 1.0
 */
public class AdminFacilityFragment extends Fragment{

    private RecyclerView recyclerView;
    private AdminFacilityAdapter facilityAdapter;
    private List<Map<String, String>> facilitiesWithEvents = new ArrayList<>();
    private List<String> eventIds = new ArrayList<>();
    /**
     * Creates and initializes the fragment's user interface.
     * Sets up RecyclerView with adapter and loads facility data.
     *
     * @param inflater The layout inflater
     * @param container The parent view container
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     * @return The created fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_facility, container, false);

        recyclerView = view.findViewById(R.id.facilityRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        facilityAdapter = new AdminFacilityAdapter(facilitiesWithEvents, this::deleteEventsByFacility, getContext());
        recyclerView.setAdapter(facilityAdapter);

        loadFacilitiesFromFirestore();

        return view;
    }
    /**
     * Loads facilities and their associated events from Firestore.
     * Clears existing list and updates with fresh data.
     * Ensures each facility is only listed once even if it has multiple events.
     * Updates adapter when data is loaded.
     */
    private void loadFacilitiesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    facilitiesWithEvents.clear(); // 清空旧数据

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String facility = document.getString("facility");
                        String eventName = document.getString("name"); // get event name

                        if (facility != null && !facility.isEmpty() && eventName != null && !eventName.isEmpty()) {
                            Map<String, String> facilityEvent = new HashMap<>();
                            facilityEvent.put("facility", facility);
                            boolean alreadyExists = false;
                            for (Map<String, String> existingEvent : facilitiesWithEvents) {
                                if (facility.equals(existingEvent.get("facility"))) {
                                    alreadyExists = true;
                                    break;
                                }
                            }

                            if (!alreadyExists) {
                                facilitiesWithEvents.add(facilityEvent);
                                eventIds.add(document.getId());
                            }
                        }
                    }
                    facilityAdapter.notifyDataSetChanged(); // renew RecyclerView
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
                                // 移除该 facility 的所有记录
                                facilitiesWithEvents.removeIf(map -> map.get("facility").equals(facility));
                                facilityAdapter.notifyDataSetChanged(); // 刷新 UI
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to delete events.", Toast.LENGTH_SHORT).show();
                            });

                    for(String eventId : eventIds) {
                        Query query = db.collection("registered_events")
                                .whereEqualTo("eventId", eventId);
                        query.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();
                                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                        // Delete each document
                                        db.collection("registered_events").document(document.getId()).delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    System.out.println("Document successfully deleted!");
                                                })
                                                .addOnFailureListener(e -> {
                                                    System.err.println("Error deleting document: " + e.getMessage());
                                                });
                                    }
                                } else {
                                    System.out.println("No documents found with eventId: " + eventId);
                                }
                            } else {
                                System.err.println("Query failed: " + task.getException().getMessage());
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to find events.", Toast.LENGTH_SHORT).show();
                });
    }
}