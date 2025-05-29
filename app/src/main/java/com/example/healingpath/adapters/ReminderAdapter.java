package com.example.healingpath.adapters;

import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingpath.R;
import com.example.healingpath.models.ReminderModel;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<ReminderModel> reminderList;
    private OnReminderLongClickListener longClickListener;

    public interface OnReminderLongClickListener {
        void onReminderLongClick(ReminderModel reminder, int position);
    }

    public ReminderAdapter(List<ReminderModel> reminderList) {
        this.reminderList = reminderList;
    }

    public void setOnReminderLongClickListener(OnReminderLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        ReminderModel reminder = reminderList.get(position);
        holder.bind(reminder);
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView noteText, timeText;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            noteText = itemView.findViewById(R.id.text_note_reminder);
            timeText = itemView.findViewById(R.id.text_time_reminder);

            itemView.setOnLongClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (longClickListener != null && pos != RecyclerView.NO_POSITION) {
                    longClickListener.onReminderLongClick(reminderList.get(pos), pos);
                }
                return true;
            });
        }

        public void bind(ReminderModel reminder) {
            noteText.setText(reminder.getNote());
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault());
            timeText.setText(sdf.format(new Date(reminder.getTimestamp())));
        }
    }
}
