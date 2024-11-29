package com.example.single_lottery.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.R;

import org.checkerframework.checker.units.qual.A;

import java.util.List;
import java.util.Map;

public class AdminFacilityAdapter extends RecyclerView.Adapter<AdminFacilityAdapter.FacilityViewHolder> {

    private List<Map<String, String>> facilitiesWithEvents;
    private OnFacilityDeleteListener deleteListener;

    public AdminFacilityAdapter(List<Map<String, String>> facilitiesWithEvents, OnFacilityDeleteListener deleteListener) {
        this.facilitiesWithEvents = facilitiesWithEvents;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public FacilityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_facility, parent, false);
        return new FacilityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FacilityViewHolder holder, int position) {
        Map<String, String> facilityEvent = facilitiesWithEvents.get(position);
        String facility = facilityEvent.get("facility");
        String eventName = facilityEvent.get("eventName");

        holder.facilityTextView.setText(facility);
        holder.eventNameTextView.setText("Event: " + eventName);

        // 设置删除按钮点击事件
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onFacilityDelete(facility);
            }
        });
    }

    @Override
    public int getItemCount() {
        return facilitiesWithEvents.size();
    }

    public static class FacilityViewHolder extends RecyclerView.ViewHolder {

        TextView facilityTextView;
        TextView eventNameTextView;
        Button deleteButton;

        public FacilityViewHolder(@NonNull View itemView) {
            super(itemView);
            facilityTextView = itemView.findViewById(R.id.facilityTextView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    public interface OnFacilityDeleteListener {
        void onFacilityDelete(String facility);
    }
}