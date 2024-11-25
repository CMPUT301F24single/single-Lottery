package com.example.single_lottery.ui.scan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.single_lottery.ui.user.home.UserHomeDetailActivity;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.BarcodeResult;

public class QRScannerActivity extends CaptureActivity implements QRScanner {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBarcodeResult(BarcodeResult result) {
        String scannedData = result.getText();
        Log.d("QRScannerActivity", "Barcode scanned: " + scannedData);
        if (scannedData != null && !scannedData.isEmpty()) {
            Intent intent = new Intent(QRScannerActivity.this, UserHomeDetailActivity.class);
            intent.putExtra("event_id", scannedData); 
            Log.d("QRScannerActivity", "Starting UserHomeDetailActivity with event_id: " + scannedData);
            startActivity(intent);
        } else {
            Log.e("QRScannerActivity", "Scanned data is empty or null");
        }
        finish(); 
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("QRScannerActivity", "Back pressed, finishing QRScannerActivity");
        finish(); 
    }
}
