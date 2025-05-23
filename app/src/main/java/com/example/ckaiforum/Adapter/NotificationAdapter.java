package com.example.ckaiforum.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ckaiforum.R;

import java.util.Map;
import java.util.Objects;

import io.appwrite.models.DocumentList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder> {
    private DocumentList<Map<String, Object>> list;

    public NotificationAdapter(DocumentList<Map<String, Object>> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        return new
                NotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Map<String, Object> notification = list.getDocuments().get(position).getData();

        holder.titleView.setText(Objects.requireNonNull(notification.get("title")).toString());
        holder.contentView.setText(Objects.requireNonNull(notification.get("content")).toString());

    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.getDocuments().size();
    }

    public void setList(DocumentList<Map<String, Object>> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}
