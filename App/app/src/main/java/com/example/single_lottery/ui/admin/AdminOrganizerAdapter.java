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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
/**
 * Adapter for displaying organizer items in admin view.
 * Handles the display and interaction of organizer data in a RecyclerView.
 *
 * @author Jingyao Gu
 * @version 1.0
 */
public class AdminOrganizerAdapter extends RecyclerView.Adapter<AdminOrganizerAdapter.OrganizerViewHolder> {

    private final Context context;
    private final List<EventModel> organizerList; // List of organizers' event data
    private final OnItemClickListener listener;

    /**
     * Interface for handling organizer item click events.
     */
    public interface OnItemClickListener {
        /**
         * Called when an organizer item is clicked.
         *
         * @param organizer The selected organizer model
         */
        void onItemClick(EventModel organizer);
    }

    /**
     * Constructor for AdminOrganizerAdapter.
     *
     * @param context Context used to inflate layouts
     * @param organizerList List of organizer data to display
     * @param listener Listener for item click events
     */
    public AdminOrganizerAdapter(Context context, List<EventModel> organizerList, OnItemClickListener listener) {
        this.context = context;
        this.organizerList = organizerList;
        this.listener = listener;
    }
    /**
     * Creates new ViewHolder instances for organizer items.
     *
     * @param parent The parent ViewGroup
     * @param viewType The view type
     * @return A new OrganizerViewHolder instance
     */
    @NonNull
    @Override
    public OrganizerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for the organizer
        View view = LayoutInflater.from(context).inflate(R.layout.admin_item_organizer, parent, false);
        return new OrganizerViewHolder(view);
    }
    /**
     * Binds organizer data to the ViewHolder.
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position in the list
     */
    @Override
    public void onBindViewHolder(@NonNull OrganizerViewHolder holder, int position) {
        EventModel organizer = organizerList.get(position);
        holder.bind(organizer, listener);
    }
    /**
     * Gets the total number of organizers in the list.
     *
     * @return The total number of organizers
     */
    @Override
    public int getItemCount() {
        return organizerList.size();
    }
    /**
     * ViewHolder class for organizer items.
     * Holds references to views within each organizer item layout.
     */
    public static class OrganizerViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewOrganizerName;
        private final TextView textViewOrganizerID;
        private final Button buttonViewDetails; // Button to view organizer details
        /**
         * Constructor for OrganizerViewHolder.
         * Initializes view references from the item layout.
         *
         * @param itemView The view containing the organizer item layout
         */
        public OrganizerViewHolder(@NonNull View itemView) {
            super(itemView);
            // Bind views from the layout
            textViewOrganizerName = itemView.findViewById(R.id.adminOrganizerName);
            textViewOrganizerID = itemView.findViewById(R.id.adminOrganizerIdTextView);
            buttonViewDetails = itemView.findViewById(R.id.adminEventsViewButton); // Bind the "View Details" button
        }
        /**
         * Binds organizer data to the ViewHolder views.
         * Sets up name, ID and click listeners.
         *
         * @param organizer The organizer data to bind
         * @param listener Listener for click events
         */
        public void bind(EventModel organizer, OnItemClickListener listener) {
            // Set organizer name and ID
            textViewOrganizerName.setText(organizer.getName() != null ? organizer.getName() : "No Name");
            textViewOrganizerID.setText(organizer.getOrganizerDeviceID() != null ? organizer.getOrganizerDeviceID() : "No ID");

            // Firebase Firestore reference
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            TextView textViewEventCount = itemView.findViewById(R.id.adminEventCount);

            // Query the events collection to count matching events
            db.collection("events")
                    .whereEqualTo("organizerId", organizer.getOrganizerDeviceID())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int eventCount = queryDocumentSnapshots.size(); // Count matching events
                        textViewEventCount.setText(String.format("%d Events", eventCount));
                    })
                    .addOnFailureListener(e -> {
                        textViewEventCount.setText("Error"); // Handle query failure
                    });

            // Set the click listener for the "View Details" button
            buttonViewDetails.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), AdminOrganizerDetailActivity.class);
                intent.putExtra("organizerId", organizer.getOrganizerDeviceID());
                v.getContext().startActivity(intent);
            });
        }


    }
}
