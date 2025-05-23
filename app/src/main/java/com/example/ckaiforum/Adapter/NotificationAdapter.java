package com.example.ckaiforum.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ckaiforum.R;

import java.util.Map;

import io.appwrite.models.DocumentList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder> {
    DocumentList<Map<String, Object>> list = null;


    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        return new
                NotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.getDocuments().size();
    }
}
