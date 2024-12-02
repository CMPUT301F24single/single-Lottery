package com.example.single_lottery.ui.organizer;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.R;
import com.example.single_lottery.EventModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;

import java.util.List;

/**
 * Adapter class for managing and displaying event items in the organizer's event list.
 * Handles the display and interaction of events including view and edit operations.
 * Works with RecyclerView to efficiently display scrollable lists of events.
 *
 * @author [Jingyao Gu]
 * @version 1.0
 * @see RecyclerView.Adapter
 * @see EventModel
 * @see OrganizerEventViewHolder
 * @since 1.0
 */

public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.OrganizerEventViewHolder> {
    private List<EventModel> eventList;
    private String installationId;
    private String facilityName = "None";

    /**
     * Constructs an OrganizerEventAdapter with a list of events.
     *
     * @param eventList List of EventModel objects to be displayed
     */

    public OrganizerEventAdapter(List<EventModel> eventList) {
        this.eventList = eventList;
    }

    /**
     * Creates new ViewHolder instances for event items.
     * Inflates the event item layout and creates a new OrganizerEventViewHolder.
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new OrganizerEventViewHolder that holds a View of the event item layout
     */

    @NonNull
    @Override
    public OrganizerEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.organizer_home_item_event, parent, false);
        return new OrganizerEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizerEventViewHolder holder, int position) {
        TextView facilityTextView = holder.itemView.findViewById(R.id.organizerFacilityTextView);
        TextView timeTextView = holder.itemView.findViewById(R.id.organizerTimeTextView);
        TextView lotteryDateTextView = holder.itemView.findViewById(R.id.organizerLotteryDate);

        EventModel event = eventList.get(position);
        holder.eventNameTextView.setText(event.getName());  // Assume there is a `getName()` method

        facilityTextView.setText(event.getFacility()); // Ensure getFacility() returns the facility
        timeTextView.setText(event.getTime()); // Ensure getTime() returns the time
        lotteryDateTextView.setText(event.getLotteryTime()); // Ensure getLotteryDate() returns the date

        // View button click listener
        holder.viewButton.setOnClickListener(v -> {
            Intent viewIntent = new Intent(v.getContext(), OrganizerHomeViewEventActivity.class);
            viewIntent.putExtra("event_id", event.getEventId());
            v.getContext().startActivity(viewIntent);
        });

        // Edit button click listener
        holder.editButton.setOnClickListener(v -> {
            Intent editIntent = new Intent(v.getContext(), OrganizerHomeEditEventActivity.class);
            editIntent.putExtra("event_id", event.getEventId());
            v.getContext().startActivity(editIntent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class OrganizerEventViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameTextView;
        Button viewButton;
        Button editButton;

        public OrganizerEventViewHolder(View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            viewButton = itemView.findViewById(R.id.viewButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }
}
