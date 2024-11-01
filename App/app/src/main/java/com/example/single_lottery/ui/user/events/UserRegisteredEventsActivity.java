package com.example.single_lottery.ui.user.events;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;
import com.example.single_lottery.ui.user.EventAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserRegisteredEventsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRegisteredEvents;
    private EventAdapter eventAdapter;
    private List<EventModel> eventList;
    private String currentUserId; // 当前用户ID，假设你已在其他地方获取

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_events_registered);

        // 初始化视图
        recyclerViewRegisteredEvents = findViewById(R.id.recyclerViewRegisteredEvents);
        recyclerViewRegisteredEvents.setLayoutManager(new LinearLayoutManager(this));

        // 初始化数据源和适配器
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(this, eventList);
        recyclerViewRegisteredEvents.setAdapter(eventAdapter);

        // 获取当前用户ID
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 从数据库加载活动数据
        loadEventsFromDatabase();
    }

    private void loadEventsFromDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 查询数据库，获取当前用户注册的活动
        db.collection("registered_events").whereEqualTo("userId", currentUserId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear(); // 清空现有列表以避免重复
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        EventModel event = document.toObject(EventModel.class);
                        if (event != null) {
                            eventList.add(event);
                            Log.d("Events", "Event added: " + event.getName());
                        }
                    }
                    eventAdapter.notifyDataSetChanged(); // 刷新 RecyclerView
                })
                .addOnFailureListener(e -> {
                    Log.e("Events", "Failed to load events", e);
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }
}
