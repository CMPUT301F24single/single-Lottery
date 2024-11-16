package com.example.single_lottery.ui.scan;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.single_lottery.R;
import com.example.single_lottery.ui.organizer.OrganizerQRCode;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class QRScannerActivity extends AppCompatActivity {

    private static final String TAG = "QRScannerActivity";
    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        backButton = findViewById(R.id.buttonback);
        backButton.setOnClickListener(v -> finish());

        textureView = findViewById(R.id.scanner_view);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                closeCamera();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
            }
        });
    }

    private void openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            return;
        }

        try {
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String cameraId = manager.getCameraIdList()[0];
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    createCameraPreviewSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    closeCamera();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    closeCamera();
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access error", e);
        }
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;

            texture.setDefaultBufferSize(1080, 1920);
            Surface surface = new Surface(texture);

            imageReader = ImageReader.newInstance(1080, 1920, ImageFormat.YUV_420_888, 2);
            imageReader.setOnImageAvailableListener(this::onImageAvailable, null);

            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);
            builder.addTarget(imageReader.getSurface());

            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    try {
                        session.setRepeatingRequest(builder.build(), null, null);
                    } catch (CameraAccessException e) {
                        Log.e(TAG, "Failed to set repeat request.", e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(QRScannerActivity.this, "Camera configuration failed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access error", e);
        }
    }

    private void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireLatestImage();
        if (image != null) {
            processImageForQRCode(image);
            image.close();
        }
    }

    private void processImageForQRCode(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        LuminanceSource source = new YUVLuminanceSource(data, image.getWidth(), image.getHeight());

        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = new MultiFormatReader().decode(bitmap);
            String scannedContent = result.getText();
            handleQRCodeScan(scannedContent);
        } catch (Exception e) {
            Log.e(TAG, "Error decoding QR code", e);
        }
    }

    private void handleQRCodeScan(String scannedContent) {
        Intent intent = new Intent(this, OrganizerQRCode.class);
        intent.putExtra("event_id", scannedContent);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    private void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private class YUVLuminanceSource extends LuminanceSource {
        private final byte[] data;

        public YUVLuminanceSource(byte[] data, int width, int height) {
            super(width, height);
            this.data = data;
        }

        @Override
        public byte[] getMatrix() {
            return data;
        }

        @Override
        public byte[] getRow(int y, byte[] row) {
            if (row == null || row.length < getWidth()) {
                row = new byte[getWidth()];
            }
            System.arraycopy(data, y * getWidth(), row, 0, getWidth());
            return row;
        }
    }
}
