package com.example.single_lottery.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
/**
 * Fragment for managing users in admin view.
 * Displays list of users and handles loading user data from Firestore.
 *
 * @author Jingyao Gu
 * @version 1.0
 */
public class AdminUserFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminUserAdapter userAdapter;
    private List<EventModel> userList;
    private FirebaseFirestore db;
    /**
     * Creates and initializes the fragment's user interface.
     * Sets up RecyclerView with adapter and loads user data.
     *
     * @param inflater The layout inflater
     * @param container The parent view container
     * @param savedInstanceState Saved instance state bundle
     * @return The created fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_user, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        userAdapter = new AdminUserAdapter(requireContext(), userList);
        recyclerView.setAdapter(userAdapter);

        // Add DividerItemDecoration (for dividing list of events)
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(),
                LinearLayoutManager.VERTICAL
        );
        recyclerView.addItemDecoration(dividerItemDecoration);


        db = FirebaseFirestore.getInstance();
        loadUsers();

        return view;
    }
    /**
     * Loads user data from Firestore database.
     * Retrieves user information and updates adapter with fresh data.
     * Clears existing list before adding new user data.
     */
    private void loadUsers() {
        CollectionReference usersRef = db.collection("users");
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Clear existing list before loading new data
                QuerySnapshot snapshot = task.getResult();
                if (snapshot != null) {
                    userList.clear();
                    snapshot.forEach(document -> {
                        // Create new user model for each document
                        EventModel user = new EventModel();
                        // Map document fields to user model properties
                        user.setEventName(document.getString("name"));
                        user.setEmail(document.getString("email"));
                        user.setPhone(document.getString("phone"));
                        user.setProfileImageUrl(document.getString("profileImageUrl"));

                        // Setting eventId using the Firestore document ID
                        user.setEventId(document.getId());

                        userList.add(user);
                    });
                    userAdapter.notifyDataSetChanged();
                }
            } else {
                task.getException().printStackTrace();
            }
        });
    }


}