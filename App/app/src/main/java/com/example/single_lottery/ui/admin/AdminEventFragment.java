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

import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
/**
 * Fragment for displaying the list of events in admin view.
 * Shows all events or events filtered by facility name in a RecyclerView.
 * Handles loading event data from Firestore and updating the display.
 *
 * @author [Jingyao Gu]
 * @author [Aaron kim]
 * @version 1.0
 */
public class AdminEventFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminEventAdapter eventAdapter;
    private List<EventModel> eventList;
    private String facilityName;
    /**
     * Initializes the fragment and retrieves any passed arguments.
     * Extracts facility name from arguments if present for filtering events.
     *
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            facilityName = getArguments().getString("facility_name");
        }
    }
    /**
     * Creates and initializes the fragment's user interface.
     * Sets up RecyclerView with adapter and loads event data.
     *
     * @param inflater The layout inflater
     * @param container The parent view container
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     * @return The created fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_event, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();
        eventAdapter = new AdminEventAdapter(getContext(), eventList);
        recyclerView.setAdapter(eventAdapter);

        loadEventData();

        return view;
    }
    /**
     * Loads event data from Firestore database.
     * If facilityName is provided, filters events by facility.
     * Otherwise loads all events.
     * Updates the RecyclerView adapter with the loaded data.
     */
    private void loadEventData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").get().addOnSuccessListener(queryDocumentSnapshots -> {
            eventList.clear();
            if (facilityName == null) {
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    EventModel event = doc.toObject(EventModel.class);
                    event.setEventId(doc.getId()); // 将文档ID设置为eventId
                    eventList.add(event);
                }
            }
            else {
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    EventModel event = doc.toObject(EventModel.class);
                    event.setEventId(doc.getId());
                    if(event.getFacility() != null && event.getFacility().equals(facilityName)) {
                        eventList.add(event);
                    }
                }
            }
            eventAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load events.", Toast.LENGTH_SHORT).show();
        });
    }
}
