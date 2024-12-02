package com.example.single_lottery.ui.scan;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.view.View;
import android.widget.FrameLayout;

import com.journeyapps.barcodescanner.CaptureActivity;
/**
 * Activity for scanning QR codes using device camera.
 * Extends CaptureActivity to provide QR code scanning functionality
 * with custom back navigation.
 *
 * @author [Haorui Gao]
 * @version 1.0
 */
public class QRScannerActivity extends CaptureActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add return button
        ImageButton backButton = new ImageButton(this);
        backButton.setImageResource(android.R.drawable.ic_menu_revert); // Use a system drawable or custom drawable
        backButton.setBackgroundColor(0); // Transparent background

        // Set layout parameters for the button
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_START);
        params.setMargins(16, 16, 0, 0); // Add margins if needed
        backButton.setLayoutParams(params);

        // Add functionality to the button
        backButton.setOnClickListener(v -> finish()); // Close activity on click

        // Add button to the current layout
        FrameLayout content = findViewById(android.R.id.content);
        content.addView(backButton);
    }
}
