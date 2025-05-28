package com.example.healingpath.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingpath.R;
import com.example.healingpath.models.NoteItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private final List<NoteItem> notesList;

    public NotesAdapter(List<NoteItem> notesList) {
        this.notesList = notesList;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteItem note = notesList.get(position);
        holder.noteText.setText(note.getNote());
        holder.painLevel.setText("Pain: " + note.getPain());
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(note.getTimestamp()));
        holder.timestamp.setText(date);
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteText, painLevel, timestamp;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteText = itemView.findViewById(R.id.tv_note_text);
            painLevel = itemView.findViewById(R.id.tv_pain_level);
            timestamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }
}
