package com.example.single_lottery.ui.scan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.example.single_lottery.R;

/**
 * Fragment for handling camera preview display.
 * Uses Camera2Preview to show camera feed.
 *
 * @author [Haorui Gao]
 * @version 1.0
 */
public class CameraFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        return view;
    }
}
