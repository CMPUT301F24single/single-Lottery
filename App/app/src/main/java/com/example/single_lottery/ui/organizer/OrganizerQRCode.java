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
/**
 * Activity for managing QR code generation and display for events in the Single Lottery application.
 * Handles QR code creation, storage in Firebase, regeneration, and display functionality.
 * QR codes are used for event signup and include the event ID.
 *
 * Outstanding Issues:
 * - QR code sizing could be improved for different screen sizes
 * - Need to add caching to reduce Firebase reads
 * - Consider adding QR code expiration functionality
 *
 * @author [Haorui Gao]
 * @version 1.0
 */
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
    /**
     * Checks for existing QR code in Firestore and displays it if found.
     * Generates new QR code if none exists.
     *
     * @param eventId ID of event to check/display QR code for
     */
    private void checkAndDisplayExistingQRCode(String eventId) {
        // Query Firestore for existing QR code with matching event ID
        FirebaseFirestore.getInstance().collection("QRCode")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("type", "Signup")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // If QR code exists, display it
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
    /**
     * Generates new QR code for an event.
     * Creates QR code bitmap and saves to Firebase.
     *
     * @param eventId ID of event to generate QR code for
     */
    private void generateQRCode(String eventId) {
        // Generate unique ID for QR code
        String qrCodeId = UUID.randomUUID().toString();
        try {
            // Create QR code bitmap using ZXing library
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            String qrCodeContent = eventId;
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeContent, BarcodeFormat.QR_CODE, 600, 600);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            // Display and save the generated QR code
            qrCodeImageView.setImageBitmap(bitmap);
            saveQRCodeToFirebase(qrCodeId, eventId, bitmap);
            Toast.makeText(this, "QR Code generated successfully.", Toast.LENGTH_SHORT).show();
        } catch (WriterException e) {
            Log.e("OrganizerQRCode", "Error generating QR code: ", e);
        }
    }
    /**
     * Saves generated QR code bitmap to Firebase Storage.
     *
     * @param qrCodeId Unique identifier for QR code
     * @param eventId Associated event ID
     * @param bitmap QR code bitmap to save
     */
    private void saveQRCodeToFirebase(String qrCodeId, String eventId, Bitmap bitmap) {
        // Convert bitmap to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // Upload QR code image to Firebase Storage
        String path = "qr_codes/" + qrCodeId + ".png";
        StorageReference qrCodeRef = FirebaseStorage.getInstance().getReference(path);
        qrCodeRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> qrCodeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    qrCodeUrl = uri.toString();
                    // Save QR code details to Firestore after successful upload
                    saveQrCodeDetailsToFirestore(qrCodeId, eventId, qrCodeUrl);
                }))
                .addOnFailureListener(e -> Log.e("OrganizerQRCode", "Error saving QR code to Firebase Storage: ", e));
    }
    /**
     * Saves QR code metadata to Firestore.
     * Stores event ID, type and storage URL.
     *
     * @param qrCodeId Unique identifier for QR code
     * @param eventId Associated event ID
     * @param qrCodeUrl Firebase Storage URL of QR code image
     */
    private void saveQrCodeDetailsToFirestore(String qrCodeId, String eventId, String qrCodeUrl) {
        // Create map of QR code details
        Map<String, Object> qrCodeDetails = new HashMap<>();
        qrCodeDetails.put("eventId", eventId);
        qrCodeDetails.put("type", "Signup");
        qrCodeDetails.put("qrCodeUrl", qrCodeUrl);
        // Save details to Firestore
        FirebaseFirestore.getInstance().collection("QRCode").document(qrCodeId)
                .set(qrCodeDetails)
                .addOnSuccessListener(aVoid -> Log.d("OrganizerQRCode", "QR code details saved successfully"))
                .addOnFailureListener(e -> Log.e("OrganizerQRCode", "Error saving QR code details to Firestore", e));
    }
    /**
     * Deletes existing QR code from Firebase Storage and Firestore.
     * Executes callback on successful deletion.
     *
     * @param eventId ID of event whose QR code should be deleted
     * @param onSuccess Callback to execute after successful deletion
     */
    private void deleteExistingQRCode(String eventId, Runnable onSuccess) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Query for existing QR code
        db.collection("QRCode")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("type", "Signup")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get document ID and URL of existing QR code
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        String qrCodeUrlToDelete = queryDocumentSnapshots.getDocuments().get(0).getString("qrCodeUrl");

                        // Delete from Firestore and Storage
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
    /**
     * Extracts file path from Firebase Storage URL.
     * Used for deleting files from Storage.
     *
     * @param qrCodeUrl Full Firebase Storage URL
     * @return Storage path of file, or null if URL invalid
     */
    private String extractFilePathFromUrl(String qrCodeUrl) {
        if (qrCodeUrl == null || qrCodeUrl.isEmpty()) {
            return null;
        }
        try {
            // Parse URL and extract file path
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
    /**
     * Displays QR code image from URL using Glide library.
     *
     * @param imageUrl URL of QR code image to display
     */
    private void displayQRCodeImageByUrl(String imageUrl) {
        // Load and display image using Glide
        Glide.with(this)
                .load(imageUrl)
                .into(qrCodeImageView);
    }
}
