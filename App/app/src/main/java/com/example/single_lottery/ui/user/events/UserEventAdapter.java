package com.example.single_lottery.ui.user.events;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
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
import com.example.single_lottery.ui.user.profile.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
    private FirebaseFirestore db;

    /**
     * Creates new adapter instance with event list.
     *
     * @param context Context for launching activities
     * @param eventList List of events to display
     */
    public UserEventAdapter(Context context, List<EventModel> eventList) {
        this.context = context;
        this.eventList = eventList;
        this.db = FirebaseFirestore.getInstance();
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

        ContentResolver contentResolver = context.getContentResolver();

        TextView lotteryDateTextView = holder.itemView.findViewById(R.id.eventLotteryDate);
        TextView facilityTextView = holder.itemView.findViewById(R.id.eventFacilityTextView);
        TextView timeTextView = holder.itemView.findViewById(R.id.eventTimeTextView);
        TextView statusTextView = holder.itemView.findViewById(R.id.eventStatusTextView);

        String eventId = event.getEventId();
        String userId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        setUserStatus(userId, eventId, statusTextView);


        facilityTextView.setText(event.getFacility());
        timeTextView.setText(event.getTime());
        lotteryDateTextView.setText(event.getLotteryTime());


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
    /**
     * Checks user status for the given event.
     *
     * @param userId User's unique ID
     * @param eventId Event's unique ID
     * @param statusTextView TextView to display the user's status
     */
    private void setUserStatus(String userId, String eventId, TextView statusTextView) {
        // Query the Firestore collection to find the registration for the event
        db.collection("registered_events")
                .whereEqualTo("userId", userId)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            String status = document.getString("status");  // Assuming "status" is the field in Firestore
                            statusTextView.setText(status);  // Set the status to the TextView
                        } else {
                            statusTextView.setText("Not Registered");  // No matching document found
                        }
                    } else {
                        statusTextView.setText("Error fetching status");
                    }
                });
    }
}
