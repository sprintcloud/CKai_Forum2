package com.example.ckaiforum.Adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ckaiforum.R;

public class PostViewHolder extends RecyclerView.ViewHolder
{
    public ImageView authorPhotoImageView;
    public ImageView likeImageView;
    public ImageView mediaImageView;
    public TextView authorTextView, contentTextView, numLikesTextView;

    public PostViewHolder(@NonNull View itemView) {
        super(itemView);

        authorPhotoImageView = itemView.findViewById(R.id.authorPhotoImageView);
        likeImageView = itemView.findViewById(R.id.likeImageView);
        mediaImageView = itemView.findViewById(R.id.mediaImage);
        authorTextView = itemView.findViewById(R.id.authorTextView);
        contentTextView = itemView.findViewById(R.id.contentTextView);
        numLikesTextView = itemView.findViewById(R.id.numLikesTextView);
    }
}