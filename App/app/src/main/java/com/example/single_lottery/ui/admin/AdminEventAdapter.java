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

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.AdminEventViewHolder> {

    private Context context;
    private List<EventModel> eventList;

    public AdminEventAdapter(Context context, List<EventModel> eventList) {
        this.context = context;
        this.eventList = eventList;
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
        holder.textViewOrganizerName.setText("Organizer: " + event.getOrganizerDeviceID());

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
            textViewEventName = itemView.findViewById(R.id.text_event_name);
            textViewOrganizerName = itemView.findViewById(R.id.text_organizer_name);
            buttonViewEvent = itemView.findViewById(R.id.admin_event_button_view); // Bind the "View" button
        }
    }
}
