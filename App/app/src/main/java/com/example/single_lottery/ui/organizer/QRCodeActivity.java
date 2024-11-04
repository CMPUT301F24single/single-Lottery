// QRCodeActivity.java
package com.example.single_lottery.ui.organizer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.single_lottery.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class QRCodeActivity extends AppCompatActivity {

    private ImageView imageViewQRCode;
    private Button buttonback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        imageViewQRCode = findViewById(R.id.imageViewQRCode);
        Button buttonback = findViewById(R.id.buttonback);
        buttonback.setOnClickListener(v -> finish());

        String eventId = getIntent().getStringExtra("event_id");
        generateQRCode(eventId);

    }

    private void generateQRCode(String eventId) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(eventId, BarcodeFormat.QR_CODE, 400, 400);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            imageViewQRCode.setImageBitmap(bitmap);
            uploadQRCode(eventId, bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void uploadQRCode(String eventId, Bitmap bitmap) {
        try {
            String uniqueFileName = eventId + "_qr_" + System.currentTimeMillis() + ".png";
            File file = new File(getExternalFilesDir(null), eventId + "_qr.png");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            Uri uri = Uri.fromFile(file);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("qr_codes/" + eventId + uniqueFileName);

            storageRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                    saveQRCodeUrlToFirestore(eventId, downloadUrl.toString());
                });
                Toast.makeText(this, "QR Code uploaded successfully!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to upload QR Code.", Toast.LENGTH_SHORT).show();
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save QR Code.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQRCodeUrlToFirestore(String eventId, String qrCodeUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).update("qrCodeUrl", qrCodeUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "QR Code URL saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save QR Code URL.", Toast.LENGTH_SHORT).show();
                });
    }
}
