package com.example.single_lottery.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;

import java.util.List;

public class AdminOrganizerAdapter extends RecyclerView.Adapter<AdminOrganizerAdapter.OrganizerViewHolder> {

    private final Context context;
    private final List<EventModel> organizerList; // List of organizers' event data
    private final OnItemClickListener listener;

    // Define an interface for the item click listener
    public interface OnItemClickListener {
        void onItemClick(EventModel organizer);
    }

    public AdminOrganizerAdapter(Context context, List<EventModel> organizerList, OnItemClickListener listener) {
        this.context = context;
        this.organizerList = organizerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrganizerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for the organizer
        View view = LayoutInflater.from(context).inflate(R.layout.admin_item_organizer, parent, false);
        return new OrganizerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizerViewHolder holder, int position) {
        EventModel organizer = organizerList.get(position);
        holder.bind(organizer, listener);
    }

    @Override
    public int getItemCount() {
        return organizerList.size();
    }

    public static class OrganizerViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewOrganizerName;
        private final TextView textViewOrganizerID;
        private final Button buttonViewDetails; // Button to view organizer details

        public OrganizerViewHolder(@NonNull View itemView) {
            super(itemView);
            // Bind views from the layout
            textViewOrganizerName = itemView.findViewById(R.id.textViewOrganizerName);
            textViewOrganizerID = itemView.findViewById(R.id.textViewOrganizerID);
            buttonViewDetails = itemView.findViewById(R.id.admin_organizer_button_view); // Bind the "View Details" button
        }

        public void bind(EventModel organizer, OnItemClickListener listener) {
            // Set the organizer name and ID
            textViewOrganizerName.setText(organizer.getName() != null ? organizer.getName() : "No Name");
            textViewOrganizerID.setText(organizer.getOrganizerDeviceID() != null ? organizer.getOrganizerDeviceID() : "No ID");


            // Set the click listener for the "View Details" button
            buttonViewDetails.setOnClickListener(v -> {
                // Open the organizer's detail activity
                Intent intent = new Intent(v.getContext(), AdminOrganizerDetailActivity.class);
                intent.putExtra("organizerId", organizer.getOrganizerDeviceID()); // Pass the organizer's ID to the detail activity
                v.getContext().startActivity(intent);
            });
        }
    }
}
