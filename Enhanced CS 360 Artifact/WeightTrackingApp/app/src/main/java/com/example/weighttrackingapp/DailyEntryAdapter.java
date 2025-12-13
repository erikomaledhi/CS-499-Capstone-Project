package com.example.weighttrackingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DailyEntryAdapter extends RecyclerView.Adapter<DailyEntryAdapter.ViewHolder> {

    private Context context;
    private List<WeightEntry> entries;
    private OnDeleteClickListener deleteClickListener;
    private OnEditClickListener editClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    public DailyEntryAdapter(Context context, List<WeightEntry> entries) {
        this.context = context;
        this.entries = entries;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editClickListener = listener;
    }

    public void updateEntries(List<WeightEntry> newEntries) {
        this.entries.clear();
        this.entries.addAll(newEntries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_daily_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeightEntry entry = entries.get(position);

        // Set date (extract day from date string)
        holder.tvDay.setText(extractDay(entry.getDate()));

        // Set full date and time
        holder.tvDate.setText(entry.getDisplayDate());
        holder.tvTime.setText(entry.getDisplayTime());

        // Set weight (always in kg)
        holder.tvWeight.setText(String.format("%.1f", entry.getWeight()));

        // Set edit button click listener
        if (holder.btnEdit != null) {
            holder.btnEdit.setOnClickListener(v -> {
                if (editClickListener != null) {
                    editClickListener.onEditClick(holder.getAdapterPosition());
                }
            });
        }

        // Set delete button click listener
        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    private String extractDay(String date) {
        try {
            // Handle database format (yyyy-MM-dd)
            if (date != null && date.contains("-")) {
                String[] parts = date.split("-");
                if (parts.length == 3) {
                    return parts[2]; // Return the day part (dd)
                }
            }

            // Handle display format
            if (date != null && date.contains(",")) {
                String[] parts = date.split(",");
                if (parts.length > 1) {
                    String dayPart = parts[1].trim();
                    String[] dayWords = dayPart.split(" ");
                    if (dayWords.length > 1) {
                        return dayWords[dayWords.length - 1]; // Return the last part (day number)
                    }
                }
            }

            // If it's just "Yesterday" or "Today", extract current day
            if (date != null && (date.equals("Yesterday") || date.equals("Today"))) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                if (date.equals("Yesterday")) {
                    cal.add(java.util.Calendar.DAY_OF_MONTH, -1);
                }
                return String.valueOf(cal.get(java.util.Calendar.DAY_OF_MONTH));
            }

        } catch (Exception e) {
            // If anything goes wrong, return a default
        }

        return "1"; // Default
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvDate, tvTime, tvWeight;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}