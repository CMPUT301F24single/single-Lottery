package com.example.single_lottery.ui.scan;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

/**
 * Helper class for managing Camera2 API functionality.
 * Handles camera initialization, preview creation and resource cleanup.
 *
 * @author [Haorui Gao]
 * @version 1.0
 */
public class Camera2Helper {
    private Context context;
    private CameraDevice cameraDevice;
    private TextureView textureView;

    public Camera2Helper(Context context) {
        this.context = context;
    }


    /**
     * Opens camera and initializes preview session.
     * Requires CAMERA permission to be granted.
     *
     * @throws SecurityException if camera permission not granted
     * @throws CameraAccessException if camera cannot be accessed
     */
    public void openCamera() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];

            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles camera device state changes and preview session creation.
     */
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreviewSession() {
        // Logic to support camera preview
    }

    /**
     * Releases camera resources and closes active sessions.
     */
    public void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }
}
