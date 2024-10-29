package com.example.single_lottery.ui.role;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.single_lottery.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class RoleFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_role, container, false);

//        Button entrantButton = view.findViewById(R.id.button_entrant);
//        Button organizerButton = view.findViewById(R.id.button_organizer);

//        entrantButton.setOnClickListener(v -> {
//            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
//            navController.navigate(R.id.EntrantFragment);
//        });
//
//        organizerButton.setOnClickListener(v -> {
//            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
//            navController.navigate(R.id.OrganizerFragment);
//        });

        return view;
    }
}
