package com.example.single_lottery.ui.scan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.single_lottery.R;
import com.example.single_lottery.ui.organizer.OrganizerHomeViewEventActivity;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Activity for scanning event QR codes.
 * Handles camera initialization, QR code scanning and processing.
 * Uses Camera2 API for camera preview and ZXing for QR code decoding.
 *
 * @author [Haorui Gao]
 * @version 1.0
 */
public class QRScannerActivity extends AppCompatActivity {
    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        textureView = findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(textureListener);
    }

    /**
     * Checks and requests camera permissions if needed.
     * Initiates camera preview if permission granted.
     */
    private void checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
    }

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            checkCameraPermission();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            closeCamera();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
    };

    private void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    /**
     * Creates camera preview session and configures capture request.
     *
     * @throws CameraAccessException if camera access fails
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
            Surface surface = new Surface(texture);

            CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    try {
                        session.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(QRScannerActivity.this, "Camera Configuration Failed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes camera image to decode QR code.
     * Converts YUV to RGB and uses ZXing for decoding.
     *
     * @param image Camera image to process
     */
    private void processImageForQRCode(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
    
        int[] rgb = new int[image.getWidth() * image.getHeight()];
        for (int i = 0; i < rgb.length; i++) {
            int yIndex = i; 
            int uIndex = (i / image.getWidth() / 2) * image.getWidth() / 2 + (i % (image.getWidth() / 2));
            int vIndex = uIndex; 
    
            int Y = data[yIndex] & 0xFF; 
            int U = data[uIndex] & 0xFF; 
            int V = data[vIndex] & 0xFF; 
    
            int r = Y + (int)(1.402 * (V - 128));
            int g = Y - (int)(0.344136 * (U - 128)) - (int)(0.714136 * (V - 128));
            int b = Y + (int)(1.772 * (U - 128));
    
            r = Math.max(0, Math.min(255, r));
            g = Math.max(0, Math.min(255, g));
            b = Math.max(0, Math.min(255, b));
    
            rgb[i] = (0xFF << 24) | (r << 16) | (g << 8) | b; 
        }
    
        LuminanceSource source = new RGBLuminanceSource(image.getWidth(), image.getHeight(), rgb);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
    
        try {
            // Decode the QR code
            Result result = new MultiFormatReader().decode(bitmap);
            String scannedContent = result.getText();
            handleQRCodeScan(scannedContent); 
        } catch (Exception e) {
            Log.e("QRCodeScanner", "Error decoding QR code", e);
        }
    }


    /**
     * Handles decoded QR code content.
     * Launches event view activity with scanned event ID.
     *
     * @param scannedContent Decoded QR code content
     */
    private void handleQRCodeScan(String scannedContent) {
        Intent intent = new Intent(this, OrganizerHomeViewEventActivity.class);
        intent.putExtra("event_id", scannedContent); 
        startActivity(intent); 
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }
}
