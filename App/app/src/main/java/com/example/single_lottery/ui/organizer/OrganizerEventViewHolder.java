package com.example.single_lottery.ui.organizer;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.R;

/**
 * ViewHolder class for displaying individual event items in the organizer's event list.
 * Responsible for holding references to the UI components of each event item view.
 * Used by OrganizerEventAdapter to efficiently manage event list item views.
 *
 * The ViewHolder contains:
 * - Event name display
 * - View button for detailed event viewing
 * - Edit button for event modification
 *
 * @author [Jingyao Gu]
 * @version 1.0
 * @see RecyclerView.ViewHolder
 * @see OrganizerEventAdapter
 * @since 1.0
 */

public class OrganizerEventViewHolder extends RecyclerView.ViewHolder {
    TextView eventNameTextView;
    Button viewButton;
    Button editButton;

    /**
     * Constructs a new OrganizerEventViewHolder and initializes view references.
     * Finds and assigns all UI component references from the provided item view.
     *
     * @param itemView The view containing the event item layout components
     * @see RecyclerView.ViewHolder
     */

    public OrganizerEventViewHolder(View itemView) {
        super(itemView); // Make sure to call the parent class constructor
        eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
        viewButton = itemView.findViewById(R.id.viewButton); // New View button
        editButton = itemView.findViewById(R.id.editButton); // New Edit button
    }
}
