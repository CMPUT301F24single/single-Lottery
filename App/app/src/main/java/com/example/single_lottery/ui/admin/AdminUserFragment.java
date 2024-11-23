package com.example.single_lottery.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminUserFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private final List<EventModel> userList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_user, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AdminUserAdapter(getContext(), userList, user -> {
            // 点击用户跳转到详情页面
            Intent intent = new Intent(getContext(), AdminUserDetailActivity.class);
            intent.putExtra("userId", user.getUserDeviceID());
            intent.putExtra("userName", user.getName());
            intent.putExtra("userEmail", user.getEmail());
            intent.putExtra("userPhone", user.getPhone());
            intent.putExtra("profileImageUrl", user.getProfileImageUrl());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        loadUsers();

        return view;
    }

    private void loadUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get()
                .addOnSuccessListener(querySnapshot -> {
                    userList.clear(); // 清空当前用户列表
                    for (DocumentSnapshot document : querySnapshot) {
                        EventModel user = document.toObject(EventModel.class);
                        if (user != null) {
                            user.setUserDeviceID(document.getId()); // 设置文档 ID 为 userDeviceID
                            userList.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged(); // 刷新适配器
                })
                .addOnFailureListener(e -> Log.e("AdminUserFragment", "Error loading organizers", e));
    }
}