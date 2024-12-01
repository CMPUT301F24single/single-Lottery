package com.example.single_lottery.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.AdminEventViewHolder> {

    private Context context;
    private List<EventModel> eventList;
    private FirebaseFirestore db;

    public AdminEventAdapter(Context context, List<EventModel> eventList) {
        this.context = context;
        this.eventList = eventList;
        this.db = FirebaseFirestore.getInstance();  // Initialize Firestore instance
    }

    @NonNull
    @Override
    public AdminEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each event
        View view = LayoutInflater.from(context).inflate(R.layout.admin_item_event, parent, false);
        return new AdminEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminEventViewHolder holder, int position) {
        // Get the event data for the current position
        EventModel event = eventList.get(position);
        holder.textViewEventName.setText(event.getName());

        // set location and tiem
        TextView textViewLocation = holder.itemView.findViewById(R.id.adminLocationTextView);
        TextView textViewTime = holder.itemView.findViewById(R.id.adminTimeTextView);
        textViewLocation.setText(event.getFacility());
        textViewTime.setText(event.getTime());

        String organizerId = event.getOrganizerDeviceID();

        // obtain an set the organizer's name
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Iterate through all documents in the "users" collection
                        for (DocumentSnapshot userDoc : task.getResult()) {
                            // Check if the uid matches the organizerId
                            String userUid = userDoc.getString("uid");
                            if (userUid != null && userUid.equals(organizerId)) {
                                // If the UID matches, get the organizer's name
                                String organizerName = userDoc.getString("name");  // Assuming "name" field exists
                                if (organizerName != null) {
                                    holder.textViewOrganizerName.setText(organizerName);  // Set the name in the UI
                                } else {
                                    holder.textViewOrganizerName.setText("Organizer name not found");
                                }
                                return;  // Exit after finding the first match
                            }
                        }
                        // If no matching organizerId found
                        holder.textViewOrganizerName.setText("Organizer not found");
                    } else {
                        Log.d("Firestore", "Error fetching users or no users found: " + task.getException());
                        holder.textViewOrganizerName.setText("Error fetching organizer");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("Firestore", "Error fetching users: " + e.getMessage());
                    holder.textViewOrganizerName.setText("Error fetching organizer");
                });

        // Set the click listener for the "View" button
        holder.buttonViewEvent.setOnClickListener(v -> {
            // Open the event detail page when the "View" button is clicked
            Intent intent = new Intent(context, AdminEventDetailActivity.class);
            intent.putExtra("eventId", event.getEventId()); // Pass event ID to the detail activity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        // Return the total number of events
        return eventList.size();
    }

    public static class AdminEventViewHolder extends RecyclerView.ViewHolder {
        // Declare views for event name, organizer name, and "View" button
        TextView textViewEventName, textViewOrganizerName;
        Button buttonViewEvent; // Reference to the "View" button

        public AdminEventViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the views
            textViewEventName = itemView.findViewById(R.id.adminEventName);
            textViewOrganizerName = itemView.findViewById(R.id.adminOrganizerTextView);
            buttonViewEvent = itemView.findViewById(R.id.adminEventsViewButton); // Bind the "View" button
        }
    }
}
