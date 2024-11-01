package com.example.single_lottery.ui.user.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;
import com.example.single_lottery.ui.user.EventAdapter;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<EventModel> eventList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_events_registered, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewRegisteredEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 初始化 Adapter
        eventAdapter = new EventAdapter(getContext(), eventList);

        recyclerView.setAdapter(eventAdapter);

        loadRegisteredEvents(); // 加载用户注册的活动数据
        return view;
    }

    private void loadRegisteredEvents() {
        // 从数据库加载数据并填充 eventList，然后通知 Adapter 刷新数据
    }
}