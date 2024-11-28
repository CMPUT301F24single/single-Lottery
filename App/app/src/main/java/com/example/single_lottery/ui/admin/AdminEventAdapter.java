package com.example.single_lottery.ui.admin;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;

import java.util.List;

import android.content.Context;

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
        View view = LayoutInflater.from(context).inflate(R.layout.admin_item_event, parent, false);
        return new AdminEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminEventViewHolder holder, int position) {
        EventModel event = eventList.get(position);
        holder.textViewEventName.setText(event.getName());
        holder.textViewOrganizerName.setText("Organizer: " + event.getOrganizerDeviceID());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminEventDetailActivity.class);
            intent.putExtra("eventId", event.getEventId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class AdminEventViewHolder extends RecyclerView.ViewHolder {
        TextView textViewEventName, textViewOrganizerName;

        public AdminEventViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewEventName = itemView.findViewById(R.id.text_event_name);
            textViewOrganizerName = itemView.findViewById(R.id.text_organizer_name);
        }
    }


}
