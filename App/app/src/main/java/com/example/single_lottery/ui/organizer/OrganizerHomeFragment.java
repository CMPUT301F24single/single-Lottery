package com.example.single_lottery.ui.organizer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.R;
import com.example.single_lottery.EventModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.List;


/**
 * Fragment that displays the organizer's home page with a list of their created events.
 * Manages the display and loading of events associated with the current organizer.
 * Uses RecyclerView to efficiently display scrollable list of events.
 *
 * Features:
 * - Displays list of events created by the current organizer
 * - Automatically refreshes event list on resume
 * - Loads events from Firebase Firestore based on organizer's device ID
 * - Handles event data display and updates
 *
 * @author [Jingyao Gu]
 * @version 1.0
 * @see Fragment
 * @see OrganizerEventAdapter
 * @see EventModel
 * @since 1.0
 */
public class OrganizerHomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private OrganizerEventAdapter eventAdapter;
    private List<EventModel> eventList;

    /**
     * Creates and initializes the fragment's user interface.
     * Sets up:
     * - RecyclerView with LinearLayoutManager
     * - Event adapter with empty event list
     * - List dividers for visual separation
     * - Initial event data loading
     *
     * @param inflater The LayoutInflater object for inflating views
     * @param container Parent view that the fragment's UI should be attached to
     * @param savedInstanceState Previous state of the fragment, if available
     * @return The View for the fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_homepage_fragment, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();
        eventAdapter = new OrganizerEventAdapter(eventList);
        recyclerView.setAdapter(eventAdapter);

        // Add DividerItemDecoration (for dividing list of events)
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(),
                LinearLayoutManager.VERTICAL
        );
        recyclerView.addItemDecoration(dividerItemDecoration);

        loadEvents();

        return view;
    }

    /**
     * Called when the fragment becomes visible to the user.
     * Reloads event data to ensure displayed information is current.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadEvents();  // Reload activity data every time you return to the page
    }

    /**
     * Loads events from Firestore that belong to the current organizer.
     * Filters events based on organizer's device ID and updates the RecyclerView.
     *
     * @throws SecurityException if unable to access device ID
     * @SuppressLint("NotifyDataSetChanged") Suppresses the lint warning for notifyDataSetChanged
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String deviceID = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        db.collection("events")
                .whereEqualTo("organizerDeviceID", deviceID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        EventModel event = document.toObject(EventModel.class);
                        Log.d("OrganizerHomeFragment", "Event loaded: " + event.getName());

                        if (event != null) {
                            Log.d("OrganizerHomeFragment", "Event loaded: " + event.getName());
                            event.setEventId(document.getId());
                            eventList.add(event);
                        }
                    }
                    eventAdapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {
                    Log.e("OrganizerHomeFragment", "Failed to load events: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }



}
