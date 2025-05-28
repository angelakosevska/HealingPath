package com.example.healingpath.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingpath.R;
import com.example.healingpath.models.Injury;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InjuryAdapter extends RecyclerView.Adapter<InjuryAdapter.InjuryViewHolder> {

    private final List<Injury> injuryList;
    private final Context context;
    private final OnInjuryClickListener listener;

    // Interface to be implemented by the Fragment
    public interface OnInjuryClickListener {
        void onInjuryClick(Injury injury);
    }

    public InjuryAdapter(Context context, ArrayList<Injury> injuryList, OnInjuryClickListener listener) {
        this.context = context;
        this.injuryList = injuryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InjuryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_injury, parent, false);
        return new InjuryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InjuryViewHolder holder, int position) {
        Injury injury = injuryList.get(position);
        holder.tvTitle.setText(injury.getTitle());
        holder.tvDesc.setText(injury.getDescription());
        holder.tvDate.setText(formatDate(injury.getTimestamp()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onInjuryClick(injury);
            }
        });
    }

    @Override
    public int getItemCount() {
        return injuryList.size();
    }

    public static class InjuryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvDate;

        public InjuryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_injury_title);
            tvDesc = itemView.findViewById(R.id.tv_injury_desc);
            tvDate = itemView.findViewById(R.id.tv_injury_date);
        }
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
