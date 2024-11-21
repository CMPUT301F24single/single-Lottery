package com.example.single_lottery.ui.user.events;

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

import com.example.single_lottery.ui.user.home.UserHomeDetailActivity;
import com.example.single_lottery.ui.user.home.UserHomeAdapter;

import java.util.List;

/**
 * RecyclerView adapter for user's registered events list.
 * Handles event item display and click actions.
 *
 * @author [Jingyao Gu]
 * @version 1.0
 */
public class UserEventAdapter extends RecyclerView.Adapter<UserEventAdapter.UserEventViewHolder> {

    private List<EventModel> eventList;
    private Context context;

    /**
     * Creates new adapter instance with event list.
     *
     * @param context Context for launching activities
     * @param eventList List of events to display
     */
    public UserEventAdapter(Context context, List<EventModel> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public UserEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_event_item_event, parent, false);
        return new UserEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserEventViewHolder holder, int position) {
        EventModel event = eventList.get(position);
        holder.eventNameTextView.setText(event.getName());

        TextView lotteryDateTextView = holder.itemView.findViewById(R.id.lotteryDate);
        lotteryDateTextView.setText("Lottery Date: " + event.getLotteryTime());

        holder.viewButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserEventDetailActivity.class);
            intent.putExtra("event_id", event.getEventId()); // 确保 eventId 非空
            context.startActivity(intent);
        });
    }






    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ViewHolder class for event list items.
     * Contains event name and view button.
     */
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
