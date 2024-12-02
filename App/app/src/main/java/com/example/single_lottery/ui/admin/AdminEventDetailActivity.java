package com.example.single_lottery.ui.admin;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AdminEventDetailActivity extends AppCompatActivity {

    private TextView textViewName;
    private TextView textViewDescription;
    private TextView textViewTime;
    private TextView textViewRegistrationDeadline;
    private TextView textViewLotteryTime;
    private TextView textViewWaitingListCount;
    private TextView textViewLotteryCount;
    private ImageView imageViewPoster;
    private Button buttonDeletePoster;
    private Button buttonDeleteEvent;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_detail);
        setTitle("Event Details");

        // Remove the default back button in the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false); // Disable the default back button
        }

        // Initialize views
        initViews();

        // Initialize the loading dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Get the eventId passed from the previous activity
        String eventId = getIntent().getStringExtra("eventId");
        if (eventId != null) {
            loadEventDetails(eventId);
        } else {
            Log.e("AdminEventDetail", "Event ID is missing in intent.");
        }

        // Set up the custom back button's click listener
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // Finish the activity and go back to the previous screen
            onBackPressed();
        });

        // Set up the delete poster button click listener
        buttonDeletePoster.setOnClickListener(v -> {
            deletePoster(eventId);
        });

        // Set up the delete event button click listener
        buttonDeleteEvent.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
                    .setPositiveButton("Yes", (dialog, which) -> deleteEvent(eventId))
                    .setNegativeButton("No", null)
                    .show();
        });

        // Set up the delete QR code button click listener
        Button buttonDeleteQRCode = findViewById(R.id.buttonDeleteQRCode);
        buttonDeleteQRCode.setOnClickListener(v -> {
            deleteQRCode(eventId);
        });
    }



    private void initViews() {
        textViewName = findViewById(R.id.textViewEventName);
        textViewDescription = findViewById(R.id.textViewEventDescription);
        textViewTime = findViewById(R.id.textViewEventTime);
        textViewRegistrationDeadline = findViewById(R.id.textViewRegistrationDeadline);
        textViewLotteryTime = findViewById(R.id.textViewLotteryTime);
        textViewWaitingListCount = findViewById(R.id.textViewWaitingListCount);
        textViewLotteryCount = findViewById(R.id.textViewLotteryCount);
        imageViewPoster = findViewById(R.id.imageViewPoster);
        buttonDeletePoster = findViewById(R.id.buttonDeletePoster);
        buttonDeleteEvent = findViewById(R.id.buttonDeleteEvent);
    }

    private void loadEventDetails(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // 设置活动信息
                        textViewName.setText(doc.getString("name"));
                        textViewDescription.setText(doc.getString("description"));
                        textViewTime.setText(doc.getString("time"));
                        textViewRegistrationDeadline.setText(doc.getString("registrationDeadline"));
                        textViewLotteryTime.setText(doc.getString("lotteryTime"));
                        textViewWaitingListCount.setText(doc.getLong("waitingListCount") + "/100");
                        textViewLotteryCount.setText(String.valueOf(doc.getLong("lotteryCount")));

                        // 加载海报图片
                        String posterUrl = doc.getString("posterUrl");
                        if (posterUrl != null && !posterUrl.isEmpty()) {
                            Glide.with(this).load(posterUrl).into(imageViewPoster);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("AdminEventDetail", "Error loading event details", e));
    }

    private void deletePoster(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // 显示加载对话框
        progressDialog.setMessage("Deleting poster...");
        progressDialog.show();

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String posterUrl = documentSnapshot.getString("posterUrl");
                    Log.d("DeletePoster", "Poster URL: " + posterUrl);

                    if (posterUrl != null && !posterUrl.isEmpty()) {
                        // 删除存储中的海报文件
                        StorageReference posterRef = storage.getReferenceFromUrl(posterUrl);
                        posterRef.delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("DeletePoster", "Poster deleted successfully from storage");
                                    // 更新数据库，移除海报 URL
                                    db.collection("events").document(eventId)
                                            .update("posterUrl", null)
                                            .addOnSuccessListener(unused -> {
                                                Log.d("DeletePoster", "Poster URL removed from Firestore");
                                                imageViewPoster.setImageResource(android.R.color.transparent);
                                                Toast.makeText(this, "Poster deleted successfully", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("DeletePoster", "Failed to update Firestore", e);
                                                Toast.makeText(this, "Failed to update database", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("DeletePoster", "Failed to delete poster from storage", e);
                                    Toast.makeText(this, "Failed to delete poster from storage", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                });
                    } else {
                        Toast.makeText(this, "No poster URL found", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DeletePoster", "Error fetching event details", e);
                    Toast.makeText(this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
    }

    private void deleteEvent(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // 显示加载对话框
        progressDialog.setMessage("Deleting event...");
        progressDialog.show();

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // 删除关联的海报
                    String posterUrl = documentSnapshot.getString("posterUrl");
                    if (posterUrl != null && !posterUrl.isEmpty()) {
                        StorageReference posterRef = storage.getReferenceFromUrl(posterUrl);
                        posterRef.delete()
                                .addOnSuccessListener(aVoid -> Log.d("DeleteEvent", "Poster deleted successfully"))
                                .addOnFailureListener(e -> Log.e("DeleteEvent", "Failed to delete poster from storage", e));
                    }

                    // 删除活动文档
                    db.collection("events").document(eventId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                                finish(); // 关闭当前页面
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Log.e("DeleteEvent", "Failed to delete event document", e);
                                Toast.makeText(this, "Failed to delete event", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e("DeleteEvent", "Failed to fetch event details", e);
                    Toast.makeText(this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteQRCode(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 显示加载进度对话框
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting QR Code...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 检查是否存在 QR Code
                        String qrCodeHash = documentSnapshot.getString("qrCodeHash");
                        if (qrCodeHash != null && !qrCodeHash.isEmpty()) {
                            // 删除 QR Code 字段
                            db.collection("events").document(eventId)
                                    .update("qrCodeHash", null)
                                    .addOnSuccessListener(unused -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(this, "QR Code deleted successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Log.e("DeleteQRCode", "Error deleting QR Code from Firestore", e);
                                        Toast.makeText(this, "Failed to delete QR Code", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(this, "No QR Code found for this event", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e("DeleteQRCode", "Error fetching event details", e);
                    Toast.makeText(this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
                });
    }
}