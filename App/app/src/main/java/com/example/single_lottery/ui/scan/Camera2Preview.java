package com.example.single_lottery.ui.scan;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;

public class Camera2Preview extends TextureView implements TextureView.SurfaceTextureListener {
    private Camera2Helper camera2Helper;

    public Camera2Preview(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSurfaceTextureListener(this); 
        camera2Helper = new Camera2Helper(context); 
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        camera2Helper.openCamera(); 
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        camera2Helper.closeCamera(); 
        return true; 
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        
    }
}
