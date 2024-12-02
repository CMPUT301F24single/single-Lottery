package com.example.single_lottery.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;  // For loading images (make sure to add Glide dependency)
import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;

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
        // Inflate your custom layout for each item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_user, parent, false); // Inflate item_user.xml
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

        // Set the user name in the TextView
        if(user.getName() == null || user.getName().isEmpty()){
            holder.userNameTextView.setText("(no name)");
        }
        else {
            holder.userNameTextView.setText(user.getName());
        }

        // Get the profile image URL from the user model (Firestore data)
        String profileImageUrl = user.getProfileImageUrl();  // Assuming Firestore data is being passed correctly here

        // Check if the profile image URL is valid
        if (profileImageUrl == null || profileImageUrl.isEmpty()) {
            // If profile image URL is null or empty, generate the letter avatar
            generateLetterAvatar(user.getName(), holder.userImageView);
        } else {
            // If profile image URL is available, load it using Glide
            Glide.with(context)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.ic_profile)  // Placeholder image while loading
                    .into(holder.userImageView);
        }

        // Set the click listener to open the detailed user activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminUserDetailActivity.class);
            intent.putExtra(AdminUserDetailActivity.EXTRA_USER, user); // Passing user data to the next activity
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
     * Generates a letter avatar when no profile image is set.
     * Creates circular avatar with user initials.
     *
     * @param name User's display name for initial generation
     */
    private void generateLetterAvatar(String name, ImageView profileImageView) {
        String initials = "";
        if(name == null || name.isEmpty()){
            initials += '-';
        }
        else {
            String[] nameParts = name.split("\\s+");
            if (nameParts.length > 0) {
                initials += nameParts[0].charAt(0); // First letter of first name
            }
            if (nameParts.length > 1) {
                initials += nameParts[1].charAt(0); // First letter of last name
            }
        }

        // Create a Bitmap with 100x100 size (for circular avatar)
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw a circular background
        Paint paint = new Paint();
        paint.setColor(Color.GRAY); // Background color
        canvas.drawCircle(50, 50, 50, paint); // Circle at center (50, 50) with radius 50

        // Draw initials in the center of the circle
        paint.setColor(Color.WHITE); // Text color
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER); // Center-align text
        canvas.drawText(initials, 50, 65, paint); // Draw the initials at position (50, 65)

        // Set the generated bitmap as the profile image
        profileImageView.setImageBitmap(bitmap);
    }
    /**
     * ViewHolder class for user items.
     * Holds reference to the text view that displays user name.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView userImageView;
        TextView userNameTextView;
        /**
         * Constructor for UserViewHolder.
         * Initializes text view reference from the item layout.
         *
         * @param itemView The view containing the user item layout
         */
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // Bind the ImageView and TextView
            userImageView = itemView.findViewById(R.id.userImage);  // ImageView in item_user.xml
            userNameTextView = itemView.findViewById(R.id.userName); // TextView in item_user.xml
        }
    }
}
