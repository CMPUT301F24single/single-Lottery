package com.example.single_lottery.ui.admin;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.single_lottery.EventModel;
import com.example.single_lottery.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
/**
 * Fragment for managing users in admin view.
 * Displays list of users and handles loading user data from Firestore.
 * Manages loading and displaying user data in a RecyclerView.
 *
 * @author [Jingyao Gu]
 * @author [Aaron kim]
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
     * @param savedInstanceState Bundle containing the fragment's previously saved state
     * @return The created fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_user, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Change to LinearLayoutManager for vertical list

        userList = new ArrayList<>();
        userAdapter = new AdminUserAdapter(requireContext(), userList);
        recyclerView.setAdapter(userAdapter);

        db = FirebaseFirestore.getInstance();
        loadUsers();

        return view;
    }
    /**
     * Loads user data from Firestore database.
     * Retrieves user information and updates adapter with fresh data.
     * Clears existing list before adding new user data.
     * Handles errors during data loading with user feedback.
     */
    private void loadUsers() {
        CollectionReference usersRef = db.collection("users");
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                if (snapshot != null) {
                    userList.clear();
                    for (var document : snapshot) {
                        EventModel user = new EventModel();
                        user.setEventName(document.getString("name"));
                        user.setEmail(document.getString("email"));
                        user.setPhone(document.getString("phone"));
                        user.setProfileImageUrl(document.getString("profileImageUrl")); // Extract the profile image URL
                        user.setEventId(document.getId()); // Use Firestore document ID

                        userList.add(user);
                    }
                    userAdapter.notifyDataSetChanged();
                }
            } else {
                // Handle failure gracefully
                Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
                task.getException().printStackTrace();
            }
        });
    }
}
