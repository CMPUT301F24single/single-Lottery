package com.example.single_lottery.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;

import org.checkerframework.checker.units.qual.A;

import java.util.List;

public class AdminOrganizerAdapter extends RecyclerView.Adapter<AdminOrganizerAdapter.OrganizerViewHolder> {

    private final Context context;
    private final List<EventModel> organizerList; // 组织者的活动数据
    private final OnItemClickListener listener;

    // 定义点击事件的接口
    public interface OnItemClickListener {
        void onItemClick(EventModel organizer);
    }

    public AdminOrganizerAdapter(Context context, List<EventModel> organizerList, OnItemClickListener listener) {
        this.context = context;
        this.organizerList = organizerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrganizerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_item_organizer, parent, false);
        return new OrganizerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizerViewHolder holder, int position) {
        EventModel organizer = organizerList.get(position);
        holder.bind(organizer, listener);
    }

    @Override
    public int getItemCount() {
        return organizerList.size();
    }

    public static class OrganizerViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewOrganizerName;
        private final TextView textViewOrganizerID;

        public OrganizerViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrganizerName = itemView.findViewById(R.id.textViewOrganizerName); // 从 XML 绑定视图
            textViewOrganizerID = itemView.findViewById(R.id.textViewOrganizerID);
        }


        public void bind(EventModel organizer, OnItemClickListener listener) {
            textViewOrganizerName.setText(organizer.getName() != null ? organizer.getName() : "No Name");
            textViewOrganizerID.setText(organizer.getOrganizerDeviceID() != null ? organizer.getOrganizerDeviceID() : "No ID");

            itemView.setOnClickListener(v -> listener.onItemClick(organizer));
        }
    }
}