package com.example.single_lottery.ui.user;

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
import com.example.single_lottery.ui.user.home.UserEventDetailActivity;
import com.example.single_lottery.ui.user.home.UserEventViewHolder;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.UserEventViewHolder> {

    private List<EventModel> eventList;
    private Context context;

    public EventAdapter(Context context, List<EventModel> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public UserEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_home_item_event, parent, false);
        return new UserEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserEventViewHolder holder, int position) {
        EventModel event = eventList.get(position);
        holder.eventNameTextView.setText(event.getName());

        holder.viewButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserEventDetailActivity.class);
            intent.putExtra("event_id", event.getEventId()); // 确保eventId非空
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class UserEventViewHolder extends RecyclerView.ViewHolder {
        public TextView eventNameTextView;
        public Button viewButton;

        public UserEventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            viewButton = itemView.findViewById(R.id.viewButton);
        }
    }
}
