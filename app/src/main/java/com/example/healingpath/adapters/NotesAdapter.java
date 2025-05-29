package com.example.healingpath.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingpath.R;
import com.example.healingpath.models.NoteItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private final List<NoteItem> notesList;


//    private final int[] painColors = new int[]{
//            0xFFE0F7FA, // 0
//            0xFFB2EBF2, // 1
//            0xFF80DEEA, // 2
//            0xFF4DD0E1, // 3
//            0xFF26C6DA, // 4
//            0xFF00BCD4, // 5
//            0xFF00ACC1, // 6
//            0xFF0097A7, // 7
//            0xFF00838F, // 8
//            0xFF006064, // 9
//            0xFF004D40  // 10
//    };

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
        String moodEmoji = note.getMood() != null ? note.getMood() : "😐";
        holder.mood.setText("Mood: " + moodEmoji);

        // Get pain level (ensure it's between 1 and 10)
        int pain = note.getPain();
        int index = Math.max(0, Math.min(9, pain - 1));

        // Get array of 10 colors
        int[] painColors = getPainColors(holder.itemView.getContext());

        // Create a drawable with the selected color and rounded corners
        GradientDrawable background = new GradientDrawable();
        background.setColor(painColors[index]);
        background.setCornerRadius(24f); // You can change the radius

        // Apply the background to the layout
        holder.noteLayout.setBackground(background);
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteText, painLevel, timestamp, mood;
        View noteLayout;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteText = itemView.findViewById(R.id.tv_note_text);
            painLevel = itemView.findViewById(R.id.tv_pain_level);
            timestamp = itemView.findViewById(R.id.tv_timestamp);
            mood = itemView.findViewById(R.id.tv_mood);
            noteLayout = itemView.findViewById(R.id.note_layout); // root layout in item_note.xml
        }
    }

    private int[] getPainColors(Context context) {
        int[] colors = new int[10];
        int start = ContextCompat.getColor(context, R.color.colorStart); // Low pain
        int middle = ContextCompat.getColor(context, R.color.colorMid); // Medium pain
        int end = ContextCompat.getColor(context, R.color.colorEnd); // High pain

        for (int i = 0; i < 10; i++) {
            float fraction = i / 9f;

            if (fraction < 0.5f) {
                // Interpolate between start and middle
                float localFraction = fraction * 2f;
                colors[i] = blendColors(start, middle, localFraction);
            } else {
                // Interpolate between middle and end
                float localFraction = (fraction - 0.5f) * 2f;
                colors[i] = blendColors(middle, end, localFraction);
            }
        }
        return colors;
    }

    private int blendColors(int colorFrom, int colorTo, float ratio) {
        final float inverseRatio = 1f - ratio;

        float r = Color.red(colorFrom) * inverseRatio + Color.red(colorTo) * ratio;
        float g = Color.green(colorFrom) * inverseRatio + Color.green(colorTo) * ratio;
        float b = Color.blue(colorFrom) * inverseRatio + Color.blue(colorTo) * ratio;

        return Color.rgb((int) r, (int) g, (int) b);
    }

}
