package com.example.single_lottery.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.R;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdminFacilityFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminFacilityAdapter facilityAdapter;
    private List<Map<String, String>> facilitiesWithEvents = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_facility, container, false);

        recyclerView = view.findViewById(R.id.facilityRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        facilityAdapter = new AdminFacilityAdapter(facilitiesWithEvents, this::deleteEventsByFacility);
        recyclerView.setAdapter(facilityAdapter);

        loadFacilitiesFromFirestore();

        return view;
    }

    private void loadFacilitiesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    facilitiesWithEvents.clear(); // 清空旧数据

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String facility = document.getString("facility");
                        String eventName = document.getString("name"); // 获取 event name

                        if (facility != null && !facility.isEmpty() && eventName != null && !eventName.isEmpty()) {
                            Map<String, String> facilityEvent = new HashMap<>();
                            facilityEvent.put("facility", facility);
                            facilityEvent.put("eventName", eventName);
                            facilitiesWithEvents.add(facilityEvent); // 添加到列表
                        }
                    }

                    facilityAdapter.notifyDataSetChanged(); // 更新 RecyclerView
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load facilities.", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteEventsByFacility(String facility) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .whereEqualTo("facility", facility)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        batch.delete(document.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "All events with facility deleted successfully.", Toast.LENGTH_SHORT).show();
                                // 移除该 facility 的所有记录
                                facilitiesWithEvents.removeIf(map -> map.get("facility").equals(facility));
                                facilityAdapter.notifyDataSetChanged(); // 刷新 UI
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to delete events.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to find events.", Toast.LENGTH_SHORT).show();
                });
    }
}