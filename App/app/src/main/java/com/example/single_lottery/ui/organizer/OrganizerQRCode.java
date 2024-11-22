package com.example.single_lottery.ui.organizer;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.single_lottery.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrganizerQRCode extends AppCompatActivity {

    private ImageView qrCodeImageView;
    private String eventId;
    private String qrCodeUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode); 

        qrCodeImageView = findViewById(R.id.imageViewQRCode);
        eventId = getIntent().getStringExtra("event_id"); 

        Log.d("OrganizerQRCode", "event_id: " + eventId);

        Button buttonback = findViewById(R.id.buttonback);
        buttonback.setOnClickListener(v -> {
            finish();
        });

        Button regenerateQRCodeButton = findViewById(R.id.btn_regenerateQRCode); 
        regenerateQRCodeButton.setOnClickListener(v -> {
            deleteExistingQRCode(eventId, () -> generateQRCode(eventId));
        });

        checkAndDisplayExistingQRCode(eventId);
    }

    private void checkAndDisplayExistingQRCode(String eventId) {
        FirebaseFirestore.getInstance().collection("QRCode")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("type", "Signup")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String existingQRCodeUrl = queryDocumentSnapshots.getDocuments().get(0).getString("qrCodeUrl");
                        if (existingQRCodeUrl != null) {
                            qrCodeUrl = existingQRCodeUrl;
                            displayQRCodeImageByUrl(existingQRCodeUrl);
                        }
                    } else {
                        generateQRCode(eventId);
                    }
                })
                .addOnFailureListener(e -> Log.e("OrganizerQRCode", "Error checking for existing QR code", e));
    }

    private void generateQRCode(String eventId) {
        String qrCodeId = UUID.randomUUID().toString();
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            String qrCodeContent = "{ \"eventId\": \"" + eventId + "\", \"qrCodeId\": \"" + qrCodeId + "\", \"type\": \"Signup\" }";
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeContent, BarcodeFormat.QR_CODE, 600, 600);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qrCodeImageView.setImageBitmap(bitmap);
            saveQRCodeToFirebase(qrCodeId, eventId, bitmap);
            Toast.makeText(this, "QR Code generated successfully.", Toast.LENGTH_SHORT).show();
        } catch (WriterException e) {
            Log.e("OrganizerQRCode", "Error generating QR code: ", e);
        }
    }

    private void saveQRCodeToFirebase(String qrCodeId, String eventId, Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        
        String path = "qr_codes/" + qrCodeId + ".png";
        StorageReference qrCodeRef = FirebaseStorage.getInstance().getReference(path);
        qrCodeRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> qrCodeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    qrCodeUrl = uri.toString();
                    saveQrCodeDetailsToFirestore(qrCodeId, eventId, qrCodeUrl);
                }))
                .addOnFailureListener(e -> Log.e("OrganizerQRCode", "Error saving QR code to Firebase Storage: ", e));
    }

    private void saveQrCodeDetailsToFirestore(String qrCodeId, String eventId, String qrCodeUrl) {
        Map<String, Object> qrCodeDetails = new HashMap<>();
        qrCodeDetails.put("eventId", eventId);
        qrCodeDetails.put("type", "Signup");
        qrCodeDetails.put("qrCodeUrl", qrCodeUrl);
        
        FirebaseFirestore.getInstance().collection("QRCode").document(qrCodeId)
                .set(qrCodeDetails)
                .addOnSuccessListener(aVoid -> Log.d("OrganizerQRCode", "QR code details saved successfully"))
                .addOnFailureListener(e -> Log.e("OrganizerQRCode", "Error saving QR code details to Firestore", e));
    }

    private void deleteExistingQRCode(String eventId, Runnable onSuccess) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("QRCode")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("type", "Signup")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        String qrCodeUrlToDelete = queryDocumentSnapshots.getDocuments().get(0).getString("qrCodeUrl");

                        db.collection("QRCode").document(docId).delete();
                        String filePath = extractFilePathFromUrl(qrCodeUrlToDelete);
                        if (filePath != null) {
                            FirebaseStorage.getInstance().getReference().child(filePath).delete()
                                    .addOnSuccessListener(aVoid -> onSuccess.run())
                                    .addOnFailureListener(e -> Log.e("OrganizerQRCode", "Error deleting QR code from Firebase Storage", e));
                        } else {
                            onSuccess.run();
                        }
                    } else {
                        onSuccess.run();
                    }
                })
                .addOnFailureListener(e -> Log.e("OrganizerQRCode", "Error finding existing QR code to delete", e));
    }

    private String extractFilePathFromUrl(String qrCodeUrl) {
        if (qrCodeUrl == null || qrCodeUrl.isEmpty()) {
            return null;
        }
        try {
            Uri uri = Uri.parse(qrCodeUrl);
            String path = uri.getPath();
            if (path != null) {
                int startIndex = path.indexOf("/o/") + 3;
                int endIndex = path.indexOf("?alt=media");
                if (startIndex != -1 && endIndex != -1) {
                    return path.substring(startIndex, endIndex).replace("%2F", "/");
                }
            }
        } catch (Exception e) {
            Log.e("OrganizerQRCode", "Error extracting file path from URL", e);
        }
        return null;
    }

    private void displayQRCodeImageByUrl(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .into(qrCodeImageView);
    }
}
