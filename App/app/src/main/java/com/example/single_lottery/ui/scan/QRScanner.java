package com.example.single_lottery.ui.scan;

import com.journeyapps.barcodescanner.BarcodeResult;

public interface QRScanner {
    void onBarcodeResult(BarcodeResult result);
}
