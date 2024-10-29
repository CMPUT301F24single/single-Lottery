package com.example.single_lottery.ui.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.R;

public class OrganizerHomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 将 fragment_organizer_home.xml 作为此 Fragment 的布局
        return inflater.inflate(R.layout.organizer_homepage_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        // 在此初始化 UI 元素或绑定数据，例如 RecyclerView
//        RecyclerView eventList = view.findViewById(R.id.event_list);
        // 设置 RecyclerView 的布局管理器和适配器
//        eventList.setLayoutManager(new LinearLayoutManager(getContext()));
//        // eventList.setAdapter(你的适配器);
    }
}