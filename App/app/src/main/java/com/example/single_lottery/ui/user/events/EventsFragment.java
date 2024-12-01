package com.example.single_lottery.ui.user.events;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying user's registered events list.
 * Shows events user has registered for and handles empty state display.
 *
 * @author [Jingyao Gu]
 * @version 1.0
 */
public class EventsFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserEventAdapter eventAdapter;
    private List<EventModel> eventList;
    private TextView noEventsTextView;

    /**
     * Initializes the fragment view and sets up RecyclerView with event list.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewRegisteredEvents);
        noEventsTextView = view.findViewById(R.id.noEventsTextView);

        eventList = new ArrayList<>();
        eventAdapter = new UserEventAdapter(getContext(), eventList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(eventAdapter);

        loadRegisteredEvents();

        return view;
    }

    /**
     * Loads user's registered events from Firestore.
     * Handles empty state visibility and updates event list display.
     * Uses device ID as user identifier.
     */
    private void loadRegisteredEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        db.collection("registered_events")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        noEventsTextView.setVisibility(View.VISIBLE); // 显示“无活动”提示
                        eventList.clear();
                        eventAdapter.notifyDataSetChanged();
                    } else {
                        noEventsTextView.setVisibility(View.GONE); // 隐藏提示
                        eventList.clear();

                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String eventId = document.getString("eventId");

                            // 查找 events 集合中的详细信息
                            db.collection("events").document(eventId)
                                    .get()
                                    .addOnSuccessListener(eventSnapshot -> {
                                        if (eventSnapshot.exists()) {
                                            EventModel event = eventSnapshot.toObject(EventModel.class);
                                            event.setEventId(eventId); // 设置 eventId
                                            eventList.add(event);
                                            eventAdapter.notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("EventsFragment", "Error loading event details", e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("EventsFragment", "Error loading registered events", e));
    }



}
