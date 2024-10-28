package com.example.single_lottery.ui.organizer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.single_lottery.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
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
        setContentView(R.layout.organizer_event_create);

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
        if (!validateInputs()) return;

        if (posterUri != null) {
            // 上传海报到 Firebase Storage
            StorageReference posterRef = storageRef.child(System.currentTimeMillis() + ".jpg");
            posterRef.putFile(posterUri).addOnSuccessListener(taskSnapshot ->
                    posterRef.getDownloadUrl().addOnSuccessListener(uri ->
                            saveEventData(uri.toString())
                    )
            ).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to upload poster", Toast.LENGTH_SHORT).show()
            );
        } else {
            saveEventData(null);
        }
    }

    private void saveEventData(String posterUrl) {
        String eventName = eventNameEditText.getText().toString();
        String eventDescription = eventDescriptionEditText.getText().toString();
        String eventTime = eventTimeEditText.getText().toString();
        String registrationDeadline = registrationDeadlineEditText.getText().toString();

        int waitingListCount;
        int lotteryCount;

        try {
            waitingListCount = Integer.parseInt(waitingListCountEditText.getText().toString());
            lotteryCount = Integer.parseInt(lotteryCountEditText.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for waiting list and lottery count", Toast.LENGTH_SHORT).show();
            return;
        }

        String lotteryTime = lotteryTimeEditText.getText().toString();

        Map<String, Object> event = new HashMap<>();
        event.put("name", eventName);
        event.put("description", eventDescription);
        event.put("time", eventTime);
        event.put("registrationDeadline", registrationDeadline);
        event.put("waitingListCount", waitingListCount);
        event.put("lotteryCount", lotteryCount);
        event.put("lotteryTime", lotteryTime);
        event.put("posterUrl", posterUrl);

        db.collection("events").add(event).addOnSuccessListener(documentReference ->
                Toast.makeText(this, "Event created successfully", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to create event", Toast.LENGTH_SHORT).show()
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
