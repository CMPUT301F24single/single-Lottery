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

public class AdminEventFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminEventAdapter eventAdapter;
    private List<EventModel> eventList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_event, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();
        eventAdapter = new AdminEventAdapter(getContext(), eventList);
        recyclerView.setAdapter(eventAdapter);

        // Add DividerItemDecoration (for dividing list of events)
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(),
                LinearLayoutManager.VERTICAL
        );
        recyclerView.addItemDecoration(dividerItemDecoration);

        loadEventData();

        return view;
    }

    private void loadEventData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").get().addOnSuccessListener(queryDocumentSnapshots -> {
            eventList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                EventModel event = doc.toObject(EventModel.class);
                event.setEventId(doc.getId()); // 将文档ID设置为eventId
                eventList.add(event);
            }
            eventAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load events.", Toast.LENGTH_SHORT).show();
        });
    }
}
