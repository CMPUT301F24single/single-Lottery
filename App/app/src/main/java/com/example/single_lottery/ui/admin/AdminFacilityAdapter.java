package com.example.single_lottery.ui.admin;

import static android.app.PendingIntent.getActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.R;

import org.checkerframework.checker.units.qual.A;

import java.util.List;
import java.util.Map;

public class AdminFacilityAdapter extends RecyclerView.Adapter<AdminFacilityAdapter.FacilityViewHolder> {

    private List<Map<String, String>> facilitiesWithEvents;
    private OnFacilityDeleteListener deleteListener;
    private Context context;

    public AdminFacilityAdapter(List<Map<String, String>> facilitiesWithEvents, OnFacilityDeleteListener deleteListener, Context context) {
        this.facilitiesWithEvents = facilitiesWithEvents;
        this.deleteListener = deleteListener;
        this.context = context;
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

        holder.facilityTextView.setText(facility);

        holder.eventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminFacilityEventActivity.class);
            intent.putExtra(AdminFacilityEventActivity.facility_name, facility);
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setMessage("Are you sure you want to delete this facility? You will delete every event at this facility.")
                        .setCancelable(false)  // Disable cancel by tapping outside the dialog
                        .setPositiveButton("Yes", (dialog, which) -> {
                            if (deleteListener != null) {
                                deleteListener.onFacilityDelete(facility);
                            }
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return facilitiesWithEvents.size();
    }

    public static class FacilityViewHolder extends RecyclerView.ViewHolder {

        TextView facilityTextView;
        Button deleteButton;
        Button eventsButton;

        public FacilityViewHolder(@NonNull View itemView) {
            super(itemView);
            facilityTextView = itemView.findViewById(R.id.facilityTextView);
            eventsButton = itemView.findViewById(R.id.viewEventsButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    public interface OnFacilityDeleteListener {
        void onFacilityDelete(String facility);
    }

}