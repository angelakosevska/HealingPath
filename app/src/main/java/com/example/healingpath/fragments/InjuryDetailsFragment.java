package com.example.healingpath.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.healingpath.R;
import com.google.android.material.button.MaterialButtonToggleGroup;


public class InjuryDetailsFragment extends Fragment {

    private static final String ARG_INJURY_ID = "injury_id";
    private String injuryId;

    public InjuryDetailsFragment() {

    }

    public static InjuryDetailsFragment newInstance(String injuryId) {
        InjuryDetailsFragment fragment = new InjuryDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INJURY_ID, injuryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            injuryId = getArguments().getString("injury_id");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_injury_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switchToTab(InjuryInfoFragment.newInstance(injuryId));

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.top_nav_toggle);
        if (toggleGroup != null) {
            toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (!isChecked) return;

                Fragment selected = null;
                if (checkedId == R.id.btn_notes) {
                    selected = NotesFragment.newInstance(injuryId);
                } else if (checkedId == R.id.btn_files) {
                    selected = FilesFragment.newInstance(injuryId);
                } else if (checkedId == R.id.btn_reminders) {
                    selected = RemindersFragment.newInstance(injuryId);
                }

                if (selected != null) {
                    switchToTab(selected);
                }
            });


        }
    }

    private void switchToTab(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.injury_fragment_container, fragment)
                .addToBackStack(null) //!?
                .commit();
    }
}
