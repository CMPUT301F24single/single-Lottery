package com.example.single_lottery.ui.user.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.R;
import com.example.single_lottery.EventModel;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

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

        eventList = new ArrayList<>();
        userHomeAdapter = new UserHomeAdapter(getContext(), eventList);
        recyclerViewEvents.setAdapter(userHomeAdapter);

        loadEventsFromDatabase();

        return view;
    }

    private void loadEventsFromDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                EventModel event = document.toObject(EventModel.class);
                event.setEventId(document.getId());  // 设置 document ID 为 eventId
                eventList.add(event);
            }
            userHomeAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            // 错误处理
        });
    }
}
