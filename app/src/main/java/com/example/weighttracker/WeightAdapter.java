package com.example.weighttracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.WeightVH> {

    public interface OnDeleteClicked {
        void onDelete(long id);
    }

    private final List<WeightEntry> items;
    private final OnDeleteClicked onDeleteClicked;

    public WeightAdapter(List<WeightEntry> items, OnDeleteClicked onDeleteClicked) {
        this.items = items;
        this.onDeleteClicked = onDeleteClicked;
    }

    @NonNull
    @Override
    public WeightVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weight_row, parent, false);
        return new WeightVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightVH holder, int position) {
        WeightEntry e = items.get(position);
        holder.textDate.setText(e.dateText);
        holder.textWeight.setText(String.valueOf(e.weight));
        holder.btnDelete.setOnClickListener(v -> onDeleteClicked.onDelete(e.id));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class WeightVH extends RecyclerView.ViewHolder {
        TextView textDate;
        TextView textWeight;
        Button btnDelete;

        WeightVH(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textDate);
            textWeight = itemView.findViewById(R.id.textWeight);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
