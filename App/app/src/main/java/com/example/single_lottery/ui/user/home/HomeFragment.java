package com.example.single_lottery.ui.user.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.R;
import com.example.single_lottery.EventModel;
import com.example.single_lottery.ui.scan.QRScannerActivity;
import com.example.single_lottery.ui.user.home.UserHomeDetailActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import androidx.activity.result.ActivityResultLauncher;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying available lottery events for users.
 * Shows list of all events from Firestore database.
 *
 * @version 1.0
 */
public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewEvents;
    private UserHomeAdapter userHomeAdapter;
    private List<EventModel> eventList;
    private boolean processingQr = false;

    private final ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null && !processingQr) {
            processingQr = true;
            String scannedContent = result.getContents();
            Log.d("HomeFragment", "Scanned QR Code: " + scannedContent);

            if (scannedContent.charAt(0) == '0' && scannedContent.charAt(1) == '.') {
                handleCheckInQR(scannedContent.substring(2));
            } else if (scannedContent.charAt(0) == '1' && scannedContent.charAt(1) == '.') {
                handleEventDescQR(scannedContent.substring(2));
            } else {
                handleCheckInQR(scannedContent);
            }
        }
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_homepage, container, false);

        recyclerViewEvents = view.findViewById(R.id.recyclerViewEvents);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();
        userHomeAdapter = new UserHomeAdapter(getContext(), eventList);
        recyclerViewEvents.setAdapter(userHomeAdapter);

        loadEventsFromDatabase();

        FloatingActionButton fabCamera = view.findViewById(R.id.fab_camera);
        fabCamera.setOnClickListener(v -> scanCode());

        return view;
    }

    /**
     * Loads all events from Firestore database.
     * Updates RecyclerView adapter with loaded events.
     */
    private void loadEventsFromDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null) {
                eventList.clear(); // Clear previous data to avoid duplicates
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    EventModel event = document.toObject(EventModel.class);
                    if (event != null) {
                        event.setEventId(document.getId());
                        eventList.add(event);
                    }
                }
                userHomeAdapter.notifyDataSetChanged(); // Notify the adapter that data has changed
                Log.d("HomeFragment", "Events loaded successfully.");
            }
        }).addOnFailureListener(e -> {
            Log.e("HomeFragment", "Failed to load events from database", e);
        });
    }

    /**
     * Initiates QR Code scanning.
     */
    private void scanCode() {
        processingQr = false;
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setPrompt("Scan the QR code");
        scanOptions.setBeepEnabled(true);
        scanOptions.setOrientationLocked(true);
        scanOptions.setCaptureActivity(QRScannerActivity.class);
        barLauncher.launch(scanOptions);
    }

    /**
     * Handles the QR code scan result for event descriptions.
     *
     * @param eventUID The event unique identifier.
     */
    private void handleEventDescQR(String eventUID) {
        Log.d("HomeFragment", "Navigating to UserHomeDetailActivity with eventID: " + eventUID);
        Intent intent = new Intent(getActivity(), UserHomeDetailActivity.class);
        intent.putExtra("event_id", eventUID);
        startActivity(intent);
    }

    /**
     * Handles the QR code scan result for check-in.
     *
     * @param qrCodeId The QR code identifier.
     */
    private void handleCheckInQR(String qrCodeId) {
        Log.d("HomeFragment", "Navigating to UserHomeDetailActivity for check-in with eventID: " + qrCodeId);
        Intent intent = new Intent(getActivity(), UserHomeDetailActivity.class);
        intent.putExtra("event_id", qrCodeId);
        startActivity(intent);
    }
}
