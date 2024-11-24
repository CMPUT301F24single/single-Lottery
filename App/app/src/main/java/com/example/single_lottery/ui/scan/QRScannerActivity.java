package com.example.single_lottery.ui.scan;

import android.content.Intent;
import android.os.Bundle;

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
        Intent intent = new Intent(QRScannerActivity.this, UserHomeDetailActivity.class);
        intent.putExtra("event_id", scannedData); 
        startActivity(intent);
        finish(); 
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish(); 
    }
}
