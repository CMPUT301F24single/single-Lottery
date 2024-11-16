package com.example.single_lottery.ui.user.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.R;
import com.example.single_lottery.EventModel;
import com.example.single_lottery.ui.scan.QRScannerActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying available lottery events for users.
 * Shows list of all events from Firestore database.
 *
 * @author [Jingyao Gu]
 * @version 1.0
 */
public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewEvents;
    private UserHomeAdapter userHomeAdapter;
    private List<EventModel> eventList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_homepage, container, false);

        recyclerViewEvents = view.findViewById(R.id.recyclerViewEvents);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        // Add DividerItemDecoration (for dividing list of events)
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerViewEvents.getContext(),
                LinearLayoutManager.VERTICAL
        );
        recyclerViewEvents.addItemDecoration(dividerItemDecoration);

        eventList = new ArrayList<>();
        userHomeAdapter = new UserHomeAdapter(getContext(), eventList);
        recyclerViewEvents.setAdapter(userHomeAdapter);

        loadEventsFromDatabase();

        FloatingActionButton fabCamera = view.findViewById(R.id.fab_camera);
        fabCamera.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), QRScannerActivity.class);
            startActivity(intent);
        });

        return view;
    }

    /**
     * Loads all events from Firestore database.
     * Updates RecyclerView adapter with loaded events.
     */
    private void loadEventsFromDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                EventModel event = document.toObject(EventModel.class);
                event.setEventId(document.getId());
                eventList.add(event);
            }
            userHomeAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
        });
    }
}
