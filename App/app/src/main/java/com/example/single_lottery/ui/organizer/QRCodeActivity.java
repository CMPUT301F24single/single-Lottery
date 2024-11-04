// QRCodeActivity.java
package com.example.single_lottery.ui.organizer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.single_lottery.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        String hash = hashData(eventId);
        saveHashToFirestore(eventId, hash);
        generateQRCode(hash);
    }

    private void generateQRCode(String hash) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(hash, BarcodeFormat.QR_CODE, 400, 400);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            imageViewQRCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate QR Code.", Toast.LENGTH_SHORT).show();
        }
    }

    private String hashData(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveHashToFirestore(String eventId, String hash) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).update("qrCodeHash", hash)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "QR Code hash saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save QR Code hash.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadQRCodeFromFirestore(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String hash = documentSnapshot.getString("qrCodeHash");
                        generateQRCode(hash); // 用哈希值生成二维码
                    } else {
                        Toast.makeText(this, "No QR Code hash found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load QR Code hash.", Toast.LENGTH_SHORT).show();
                });
    }
    
}
