package com.example.single_lottery.ui.user.home;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.R;


public class UserEventViewHolder extends RecyclerView.ViewHolder {

    TextView eventNameTextView;
    Button viewButton;


    public UserEventViewHolder(@NonNull View itemView) {
        super(itemView);
        eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
        viewButton = itemView.findViewById(R.id.viewButton);


    }
}
