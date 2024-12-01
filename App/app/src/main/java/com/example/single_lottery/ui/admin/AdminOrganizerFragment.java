package com.example.single_lottery.ui.admin;

import android.content.Intent;
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

import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
/**
 * Fragment for managing organizers in admin view.
 * Displays list of organizers and handles navigation to organizer details.
 *
 * @author Jingyao Gu
 * @version 1.0
 */
public class AdminOrganizerFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminOrganizerAdapter adapter;
    private final List<EventModel> organizerList = new ArrayList<>();
    /**
     * Creates and initializes the fragment's user interface.
     * Sets up RecyclerView with adapter and loads organizer data.
     *
     * @param inflater The layout inflater
     * @param container The parent view container
     * @param savedInstanceState Saved instance state bundle
     * @return The created fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_organizer, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewOrganizers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Add DividerItemDecoration (for dividing list of events)
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(),
                LinearLayoutManager.VERTICAL
        );
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter = new AdminOrganizerAdapter(getContext(), organizerList, organizer -> {
            Intent intent = new Intent(getContext(), AdminOrganizerDetailActivity.class);
            intent.putExtra("organizerId", organizer.getOrganizerDeviceID());
            intent.putExtra("organizerName", organizer.getName());
            intent.putExtra("organizerEmail", organizer.getEmail());
            intent.putExtra("organizerPhone", organizer.getPhone());
            intent.putExtra("organizerInfo", organizer.getInfo());
            intent.putExtra("organizerProfileImageUrl", organizer.getProfileImageUrl());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
        loadOrganizers(); // Loading Organizer Data
        return view;
    }
    /**
     * Loads organizer data from Firestore database.
     * Clears existing organizer list and updates with fresh data.
     * Updates adapter when data is loaded.
     */
    private void loadOrganizers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("organizers").get()
                .addOnSuccessListener(querySnapshot -> {
                    organizerList.clear();
                    for (DocumentSnapshot document : querySnapshot) {
                        EventModel organizer = document.toObject(EventModel.class);
                        if (organizer != null) {
                            // Bind document ID to model
                            organizer.setOrganizerDeviceID(document.getId());
                            organizerList.add(organizer);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("AdminOrganizerFragment", "Error loading organizers", e));
    }
}