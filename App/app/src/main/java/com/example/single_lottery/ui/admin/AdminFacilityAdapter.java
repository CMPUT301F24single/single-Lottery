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
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.single_lottery.R;

import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying facility items in the admin view.
 * Handles displaying facilities and their associated events in a RecyclerView.
 * Provides functionality for facility deletion.
 *
 * @author Jingyao Gu
 * @version 1.0
 */
public class AdminFacilityAdapter extends RecyclerView.Adapter<AdminFacilityAdapter.FacilityViewHolder> {

    private List<Map<String, String>> facilitiesWithEvents;
    private OnFacilityDeleteListener deleteListener;
    private Context context;

    /**
     * Constructor for AdminFacilityAdapter.
     *
     * @param facilitiesWithEvents List of maps containing facility and event data
     * @param deleteListener Listener for handling facility deletion events
     */
    public AdminFacilityAdapter(List<Map<String, String>> facilitiesWithEvents, OnFacilityDeleteListener deleteListener, Context context) {
        this.facilitiesWithEvents = facilitiesWithEvents;
        this.deleteListener = deleteListener;
        this.context = context;
    }

    /**
     * Creates new ViewHolder instances for facility items.
     *
     * @param parent The parent ViewGroup
     * @param viewType The type of view
     * @return A new FacilityViewHolder instance
     */
    @NonNull
    @Override
    public FacilityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_facility, parent, false);
        return new FacilityViewHolder(view);
    }

    /**
     * Binds facility data to the ViewHolder.
     * Sets facility name, event name, and delete button click listener.
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the list
     */
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

        holder.eventNameTextView.setText("Event: " + eventName);

        // Set the delete button click event
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

    /**
     * Gets the total number of facilities in the list.
     *
     * @return The total number of facilities
     */
    @Override
    public int getItemCount() {
        return facilitiesWithEvents.size();
    }

    /**
     * ViewHolder class for facility items.
     * Holds references to the views within each facility item layout.
     */
    public static class FacilityViewHolder extends RecyclerView.ViewHolder {

        TextView facilityTextView;
        Button deleteButton;

        /**
         * Constructor for FacilityViewHolder.
         * Initializes view references from the item layout.
         *
         * @param itemView The view containing the facility item layout
         */
        public FacilityViewHolder(@NonNull View itemView) {
            super(itemView);
            facilityTextView = itemView.findViewById(R.id.facilityTextView);
            eventsButton = itemView.findViewById(R.id.viewEventsButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
    /**
     * Interface for handling facility deletion events.
     */
    public interface OnFacilityDeleteListener {
        /**
         * Called when a facility needs to be deleted.
         *
         * @param facility The name of the facility to delete
         */
        void onFacilityDelete(String facility);
    }

}