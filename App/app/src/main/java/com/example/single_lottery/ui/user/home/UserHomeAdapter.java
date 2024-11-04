package com.example.single_lottery.ui.user.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.R;
import com.example.single_lottery.EventModel;

import java.util.List;

public class UserHomeAdapter extends RecyclerView.Adapter<UserHomeAdapter.UserHomeViewHolder> {

    private List<EventModel> eventList;
    private Context context;

    public UserHomeAdapter(Context context, List<EventModel> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public UserHomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_home_item_event, parent, false);
        return new UserHomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHomeViewHolder holder, int position) {
        EventModel event = eventList.get(position);
        holder.eventNameTextView.setText(event.getName());

        holder.viewButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserHomeDetailActivity.class);
            intent.putExtra("event_id", event.getEventId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class UserHomeViewHolder extends RecyclerView.ViewHolder {
        public TextView eventNameTextView;
        public Button viewButton;

        public UserHomeViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            viewButton = itemView.findViewById(R.id.viewButton);
        }
    }
}