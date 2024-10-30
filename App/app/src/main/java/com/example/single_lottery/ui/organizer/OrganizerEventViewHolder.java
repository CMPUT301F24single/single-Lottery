package com.example.single_lottery.ui.organizer;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.R;

public class OrganizerEventViewHolder extends RecyclerView.ViewHolder {
    TextView eventNameTextView;
    Button viewButton;
    Button editButton;

    public OrganizerEventViewHolder(View itemView) {
        super(itemView); // 确保调用父类构造函数
        eventNameTextView = itemView.findViewById(R.id.eventNameTextView); // 已有的TextView
        viewButton = itemView.findViewById(R.id.viewButton); // 新增的View按钮
        editButton = itemView.findViewById(R.id.editButton); // 新增的Edit按钮
    }
}
