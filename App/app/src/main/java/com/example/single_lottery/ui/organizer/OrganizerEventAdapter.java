package com.example.single_lottery.ui.organizer;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.R;

import java.util.List;

public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.OrganizerEventViewHolder> {
    private List<OrganizerHomeEventModel> eventList;

    public OrganizerEventAdapter(List<OrganizerHomeEventModel> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public OrganizerEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.organizer_home_item_event, parent, false);
        return new OrganizerEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizerEventViewHolder holder, int position) {
        OrganizerHomeEventModel event = eventList.get(position);
        holder.eventNameTextView.setText(event.getName());  // 假设有 `getName()` 方法

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
