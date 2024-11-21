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

/**
 * Adapter for displaying event list in user's home screen.
 * Handles event item display and navigation to event details.
 *
 * @author [Jingyao Gu]
 * @version 1.0
 */
public class UserHomeAdapter extends RecyclerView.Adapter<UserHomeAdapter.UserHomeViewHolder> {

    private List<EventModel> eventList;
    private Context context;

    /**
     * Creates new adapter instance for event list.
     *
     * @param context Context for launching activities
     * @param eventList List of events to display
     */
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

        TextView lotteryDateTextView = holder.itemView.findViewById(R.id.lotteryDate);
        lotteryDateTextView.setText("Lottery Date: " + event.getLotteryTime()); // Ensure getLotteryDate() returns the date

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

    /**
     * ViewHolder for home screen event items.
     * Contains event name and view button.
     */
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
