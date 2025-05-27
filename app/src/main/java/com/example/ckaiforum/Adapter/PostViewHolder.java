package com.example.ckaiforum.Adapter;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ckaiforum.R;
import com.google.android.material.chip.ChipGroup;

public class PostViewHolder extends RecyclerView.ViewHolder
{
    public ImageView mediaImageView, likeImageView, authorPhotoImageView, shareImageView;
    public TextView authorTextView, contentTextView, numLikesTextView, numCommentTextView, tagsTitle;
    public LinearLayout likeLinearLayout, commentLinearLayout;
    public Button deletePost, actionViewPost;
    public ChipGroup tagsGroup;

    public PostViewHolder(@NonNull View itemView) {
        super(itemView);

        authorPhotoImageView = itemView.findViewById(R.id.authorPhotoImageView);
        likeImageView = itemView.findViewById(R.id.likeImageView);
        mediaImageView = itemView.findViewById(R.id.mediaImage);
        authorTextView = itemView.findViewById(R.id.authorTextView);
        contentTextView = itemView.findViewById(R.id.contentTextView);
        numLikesTextView = itemView.findViewById(R.id.numLikesTextView);
        likeLinearLayout = itemView.findViewById(R.id.likeLinearLayout);
        deletePost = itemView.findViewById(R.id.actionDelete);
        commentLinearLayout = itemView.findViewById(R.id.commentLinearLayout);
        numCommentTextView = itemView.findViewById(R.id.numCommentTextView);
        actionViewPost = itemView.findViewById(R.id.actionViewPost);
        shareImageView = itemView.findViewById(R.id.shareImageView);
        tagsGroup = itemView.findViewById(R.id.tagsChipGroup);
        tagsTitle = itemView.findViewById(R.id.tagsContainer);

    }
}