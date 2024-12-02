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

public class AdminOrganizerFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminOrganizerAdapter adapter;
    private final List<EventModel> organizerList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_organizer, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewOrganizers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
        loadOrganizers(); // 加载组织者数据
        return view;
    }

    private void loadOrganizers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("organizers").get()
                .addOnSuccessListener(querySnapshot -> {
                    organizerList.clear();
                    for (DocumentSnapshot document : querySnapshot) {
                        EventModel organizer = document.toObject(EventModel.class);
                        if (organizer != null) {
                            // 绑定文档 ID 到 model
                            organizer.setOrganizerDeviceID(document.getId());
                            organizerList.add(organizer);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("AdminOrganizerFragment", "Error loading organizers", e));
    }
}