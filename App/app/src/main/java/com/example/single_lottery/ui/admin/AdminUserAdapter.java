package com.example.single_lottery.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.EventModel;

import java.util.List;
/**
 * Adapter for displaying user items in admin view.
 * Handles the display and interaction of user data in a RecyclerView.
 *
 * @author Jingyao Gu
 * @version 1.0
 */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {
    private List<EventModel> userList;
    private Context context;
    /**
     * Constructor for AdminUserAdapter.
     *
     * @param context Context used to inflate layouts
     * @param userList List of users to display
     */
    public AdminUserAdapter(Context context, List<EventModel> userList) {
        this.context = context;
        this.userList = userList;
    }

    /**
     * Creates new ViewHolder instances for user items.
     *
     * @param parent The parent ViewGroup
     * @param viewType The view type
     * @return A new UserViewHolder instance
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new UserViewHolder(view);
    }
    /**
     * Binds user data to the ViewHolder.
     * Sets user name and click listener for navigation to user details.
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position in the list
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        EventModel user = userList.get(position);
        holder.textView.setText(user.getName());

        // Set up click events
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminUserDetailActivity.class);
            intent.putExtra(AdminUserDetailActivity.EXTRA_USER, user); // Passing Serializable Objects
            context.startActivity(intent);
        });

    }
    /**
     * Gets the total number of users in the list.
     *
     * @return The total number of users
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }
    /**
     * ViewHolder class for user items.
     * Holds reference to the text view that displays user name.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        /**
         * Constructor for UserViewHolder.
         * Initializes text view reference from the item layout.
         *
         * @param itemView The view containing the user item layout
         */
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}