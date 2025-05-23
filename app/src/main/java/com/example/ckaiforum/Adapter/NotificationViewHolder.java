package com.example.ckaiforum.Adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ckaiforum.R;

public class NotificationViewHolder extends RecyclerView.ViewHolder{

    public TextView titleView, contentView;

    public NotificationViewHolder(@NonNull View itemView) {
        super(itemView);
        titleView = itemView.findViewById(R.id.item_notifications_title);
        contentView = itemView.findViewById(R.id.item_notifications_content);
    }
}