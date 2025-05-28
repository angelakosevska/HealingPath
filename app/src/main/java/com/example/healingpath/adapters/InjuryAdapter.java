package com.example.healingpath.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingpath.R;
import com.example.healingpath.models.Injury;
import com.example.healingpath.activities.InjuryDetailActivity;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InjuryAdapter extends RecyclerView.Adapter<InjuryAdapter.InjuryViewHolder> {

    private List<Injury> injuryList;
    private Context context;

    public InjuryAdapter(Context context, List<Injury> injuryList) {
        this.context = context;
        this.injuryList = injuryList;
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
            Intent intent = new Intent(context, InjuryDetailActivity.class);
            intent.putExtra("injury_id", injury.getId());
            context.startActivity(intent);
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
