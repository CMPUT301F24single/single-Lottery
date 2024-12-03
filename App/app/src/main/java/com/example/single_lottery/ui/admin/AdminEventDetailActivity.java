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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
/**
 * Activity for displaying and managing event details in admin view.
 * Provides functionality to view event information, delete posters, QR codes,
 * and entire events. Handles interaction with Firebase Storage and Firestore.
 *
 * @author [Jingyao Gu]
 * @author [Aaron kim]
 * @version 1.0
 */
public class AdminEventDetailActivity extends AppCompatActivity {

    private TextView textViewName;
    private TextView textViewDescription;
    private TextView textViewTime;
    private TextView textViewRegistrationDeadline;
    private TextView textViewLotteryTime;
    private TextView textViewWaitingListCount;
    private TextView textViewLotteryCount;
    private TextView textViewLocationRequirement;
    private ImageView imageViewPoster;
    private Button buttonDeletePoster;
    private Button buttonDeleteEvent;
    private ProgressDialog progressDialog;
    /**
     * Initializes the activity, sets up UI components and button listeners.
     * Loads event details if valid event ID is provided.
     *
     * @param savedInstanceState Saved instance state bundle
     */
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


    /**
     * Initializes all view references from the layout.
     * Binds UI components to their respective fields.
     */
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
        textViewLocationRequirement = findViewById(R.id.textViewLocationRequirement);
    }
    /**
     * Loads event details from Firestore database.
     * Updates UI with event information including name, description, times,
     * and poster image if available.
     *
     * @param eventId ID of the event to load
     */
    private void loadEventDetails(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Installation activity information
                        textViewName.setText(doc.getString("name"));
                        textViewDescription.setText(doc.getString("description"));
                        textViewTime.setText(doc.getString("time"));
                        textViewRegistrationDeadline.setText(doc.getString("registrationDeadline"));
                        textViewLotteryTime.setText(doc.getString("lotteryTime"));
                        if(doc.getLong("waitingListCount") == Integer.MAX_VALUE){
                            textViewWaitingListCount.setText("unlimited");
                        }
                        else {
                            textViewWaitingListCount.setText(doc.getLong("waitingListCount") + "");
                        }
                        textViewLotteryCount.setText(String.valueOf(doc.getLong("lotteryCount")));
                        if (Boolean.TRUE.equals(doc.getBoolean("requiresLocation")) == false ) {
                            textViewLocationRequirement.setText("No");
                        } else {
                            textViewLocationRequirement.setText("Yes");
                        }

                        // Loading marine images
                        String posterUrl = doc.getString("posterUrl");
                        if (posterUrl != null && !posterUrl.isEmpty()) {
                            Glide.with(this).load(posterUrl).into(imageViewPoster);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("AdminEventDetail", "Error loading event details", e));
    }
    /**
     * Deletes the event poster from storage and removes poster URL from database.
     * Shows progress dialog during deletion process.
     *
     * @param eventId ID of the event whose poster should be deleted
     */
    private void deletePoster(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Display loading conversation frame
        progressDialog.setMessage("Deleting poster...");
        progressDialog.show();

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String posterUrl = documentSnapshot.getString("posterUrl");
                    Log.d("DeletePoster", "Poster URL: " + posterUrl);

                    if (posterUrl != null && !posterUrl.isEmpty()) {
                        // Delete poster files in storage
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
    /**
     * Deletes an event and its associated data from the database.
     * Includes deletion of poster if it exists.
     * Shows confirmation dialog before deletion.
     *
     * @param eventId ID of the event to delete
     */
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
                    Query query = db.collection("registered_events")
                            .whereEqualTo("eventId", eventId);

                    query.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    // Delete each document
                                    db.collection("registered_events").document(document.getId()).delete()
                                            .addOnSuccessListener(aVoid -> {
                                                System.out.println("Document successfully deleted!");
                                            })
                                            .addOnFailureListener(e -> {
                                                System.err.println("Error deleting document: " + e.getMessage());
                                            });
                                }
                            } else {
                                System.out.println("No documents found with eventId: " + eventId);
                            }
                        } else {
                            System.err.println("Query failed: " + task.getException().getMessage());
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e("DeleteEvent", "Failed to fetch event details", e);
                    Toast.makeText(this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
                });
    }
    /**
     * Deletes the QR code associated with an event.
     * Removes QR code hash from the event document in database.
     * Shows progress dialog during deletion.
     *
     * @param eventId ID of the event whose QR code should be deleted
     */
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