package com.example.ckaiforum.Adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ckaiforum.R;

public class BottomSheetPostHolder extends RecyclerView.ViewHolder {
    public TextView authorTextView, contentTextView;
    public ImageView authorPhotoImageView, shareImageView;

    public BottomSheetPostHolder(@NonNull View itemView) {
        super(itemView);
        authorPhotoImageView = itemView.findViewById(R.id.authorPhotoImageView);
        authorTextView = itemView.findViewById(R.id.authorTextView);
        contentTextView = itemView.findViewById(R.id.contentTextView);
        shareImageView = itemView.findViewById(R.id.shareImageView);
    }
}
