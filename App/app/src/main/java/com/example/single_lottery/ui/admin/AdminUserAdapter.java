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
import com.example.single_lottery.R;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private final Context context;
    private final List<EventModel> userList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(EventModel user);
    }

    public AdminUserAdapter(Context context, List<EventModel> userList, OnItemClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        EventModel user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewUserrName;
        private final TextView textViewUserID;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserrName = itemView.findViewById(R.id.textViewUserName);
            textViewUserID = itemView.findViewById(R.id.textViewUserID);
        }

        public void bind(EventModel user, OnItemClickListener listener) {
            textViewUserrName.setText(user.getName());
            textViewUserID.setText(user.getUserDeviceID());
            itemView.setOnClickListener(v -> listener.onItemClick(user));
        }
    }
}