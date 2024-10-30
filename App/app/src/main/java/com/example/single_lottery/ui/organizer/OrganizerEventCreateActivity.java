package com.example.single_lottery.ui.organizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.single_lottery.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class OrganizerEventCreateActivity extends AppCompatActivity {

    private ImageView eventPosterImageView;
    private EditText eventNameEditText, eventDescriptionEditText, eventTimeEditText,
            registrationDeadlineEditText, waitingListCountEditText, lotteryCountEditText, lotteryTimeEditText;
    private Uri posterUri;

    private FirebaseFirestore db;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_create_fragment);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//
//        if (toolbar != null) {
//            // 设置返回图标
//            toolbar.setNavigationIcon(R.drawable.ic_back); // 替换为你的返回图标资源ID
//            // 为 Toolbar 添加返回点击事件
//            toolbar.setNavigationOnClickListener(v -> {
//                finish(); // 返回上一个 Activity
//            });
//        }

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // 初始化 Firebase 实例
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference().child("event_posters");

        // 绑定视图
        eventPosterImageView = findViewById(R.id.eventPosterImageView);
        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
        eventTimeEditText = findViewById(R.id.eventTimeEditText);
        registrationDeadlineEditText = findViewById(R.id.registrationDeadlineEditText);
        waitingListCountEditText = findViewById(R.id.waitingListCountEditText);
        lotteryCountEditText = findViewById(R.id.lotteryCountEditText);
        lotteryTimeEditText = findViewById(R.id.lotteryTimeEditText);

        Button uploadPosterButton = findViewById(R.id.uploadPosterButton);
        Button createEventButton = findViewById(R.id.createEventButton);

        uploadPosterButton.setOnClickListener(v -> openImagePicker());
        createEventButton.setOnClickListener(v -> uploadEventToFirebase());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            posterUri = data.getData();
            eventPosterImageView.setImageURI(posterUri);
        }
    }

    private void uploadEventToFirebase() {
        // 检查必填字段是否为空
        Log.d("OrganizerEventCreateActivity", "uploadEventToFirebase() called");

        String eventName = eventNameEditText.getText().toString().trim();
        String eventTime = eventTimeEditText.getText().toString().trim();
        String registrationDeadline = registrationDeadlineEditText.getText().toString().trim();
        String lotteryTime = lotteryTimeEditText.getText().toString().trim();


        // 获取设备码
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String organizerDeviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        // 检查必填字段
        if (eventName.isEmpty() || eventTime.isEmpty() || registrationDeadline.isEmpty() || lotteryTime.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            Log.d("OrganizerEventCreateActivity", "Required fields are missing");
            return;
        }

        int waitingListCount;
        int lotteryCount;
        try {
            String waitingListStr = waitingListCountEditText.getText().toString().trim();
            String lotteryCountStr = lotteryCountEditText.getText().toString().trim();

            // 设置等待列表和彩票数量的默认值
            waitingListCount = waitingListStr.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(waitingListStr);
            lotteryCount = lotteryCountStr.isEmpty() ? waitingListCount : Integer.parseInt(lotteryCountStr);

            // 检查彩票数量是否小于或等于等待列表数量
            if (lotteryCount > waitingListCount) {
                Toast.makeText(this, "Lottery count cannot exceed waiting list count", Toast.LENGTH_SHORT).show();
                Log.d("OrganizerEventCreateActivity", "Lottery count exceeds waiting list count");
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for waiting list and lottery count", Toast.LENGTH_SHORT).show();
            Log.d("OrganizerEventCreateActivity", "Invalid number format for waiting list or lottery count");
            return;
        }

        // 检查海报上传
        if (posterUri != null) {
            StorageReference posterRef = storageRef.child(System.currentTimeMillis() + ".jpg");
            Log.d("OrganizerEventCreateActivity", "Uploading poster to Firebase Storage");
            posterRef.putFile(posterUri).addOnSuccessListener(taskSnapshot ->
                    posterRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d("OrganizerEventCreateActivity", "Poster uploaded, URL: " + uri.toString());
                        saveEventData(uri.toString(), eventName, eventTime, registrationDeadline, lotteryTime, waitingListCount, lotteryCount, organizerDeviceID);
                    })
            ).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to upload poster", Toast.LENGTH_SHORT).show();
                Log.d("OrganizerEventCreateActivity", "Poster upload failed: " + e.getMessage());
            });
        } else {
            Log.d("OrganizerEventCreateActivity", "No poster, saving event data directly");
            saveEventData(null, eventName, eventTime, registrationDeadline, lotteryTime, waitingListCount, lotteryCount,organizerDeviceID);
        }
    }

    private void saveEventData(String posterUrl, String eventName, String eventTime,
                               String registrationDeadline, String lotteryTime,
                               int waitingListCount, int lotteryCount,String organizerDeviceID) {


        Map<String, Object> event = new HashMap<>();
        event.put("name", eventName);
        event.put("time", eventTime);
        event.put("registrationDeadline", registrationDeadline);
        event.put("lotteryTime", lotteryTime);
        event.put("waitingListCount", waitingListCount);
        event.put("lotteryCount", lotteryCount);
        event.put("posterUrl", posterUrl);
        event.put("organizerDeviceID", organizerDeviceID); // 添加 organizerDeviceID 字段

        db.collection("events").add(event).addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Event created successfully", Toast.LENGTH_SHORT).show();
                }
        ).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create event", Toast.LENGTH_SHORT).show();
                }
        );
    }



    private boolean validateInputs() {
        if (eventNameEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter event name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (eventDescriptionEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter event description", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (eventTimeEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter event time", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (registrationDeadlineEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter registration deadline", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (waitingListCountEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter waiting list count", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (lotteryCountEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter lottery count", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (lotteryTimeEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter lottery time", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
