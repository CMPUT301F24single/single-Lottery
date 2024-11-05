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

public class OrganizerHomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private OrganizerEventAdapter eventAdapter;
    private List<EventModel> eventList;

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

    @Override
    public void onResume() {
        super.onResume();
        loadEvents();  // 每次返回页面时重新加载活动数据
    }

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
