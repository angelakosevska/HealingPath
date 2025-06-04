package com.example.healingpath.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingpath.R;

import java.util.ArrayList;
import java.util.List;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private TextView tvSelectedDate;
    private RecyclerView recyclerNotes;
    private NotesAdapter notesAdapter;
    private List<String> notesList = new ArrayList<>(); // This will hold notes for selected date

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendar_view);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        recyclerNotes = view.findViewById(R.id.recycler_notes);

        // Setup RecyclerView
        recyclerNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        notesAdapter = new NotesAdapter(notesList);
        recyclerNotes.setAdapter(notesAdapter);

        // Set initial selected date text
        long today = calendarView.getDate();
        updateSelectedDateText(today);

        // Set listener for date changes
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            tvSelectedDate.setText(selectedDate);

            // Load notes from Firestore for this date and update RecyclerView
            loadNotesForDate(selectedDate);
        });

        // Load notes for today initially
        loadNotesForDate(tvSelectedDate.getText().toString());
    }

    private void updateSelectedDateText(long dateMillis) {
        // Convert millis to date string (day/month/year)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(dateMillis);

        int day = cal.get(java.util.Calendar.DAY_OF_MONTH);
        int month = cal.get(java.util.Calendar.MONTH) + 1;
        int year = cal.get(java.util.Calendar.YEAR);

        String dateString = day + "/" + month + "/" + year;
        tvSelectedDate.setText(dateString);
    }

    private void loadNotesForDate(String date) {
        // TODO: Replace this with Firestore query filtered by the selected date and current user & injury
        // For now, just clear and add dummy data

        notesList.clear();

        // Example dummy data - in real app, load notes from Firestore based on date
        notesList.add("Note 1 for " + date);
        notesList.add("Note 2 for " + date);
        notesList.add("Note 3 for " + date);

        notesAdapter.notifyDataSetChanged();
    }

    // Simple RecyclerView.Adapter for showing notes as strings
    private static class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

        private final List<String> notes;

        NotesAdapter(List<String> notes) {
            this.notes = notes;
        }

        @NonNull
        @Override
        public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setPadding(16, 16, 16, 16);
            tv.setTextSize(16f);
            return new NoteViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
            String note = notes.get(position);
            holder.textView.setText(note);
        }

        @Override
        public int getItemCount() {
            return notes.size();
        }

        static class NoteViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            NoteViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = (TextView) itemView;
            }
        }
    }
}
