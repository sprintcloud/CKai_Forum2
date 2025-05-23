package com.example.ckaiforum.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ckaiforum.R;
import com.example.ckaiforum.ViewModel.AppViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.appwrite.Client;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.DocumentList;
import io.appwrite.services.Databases;
import androidx.navigation.NavController;


public class PostAdapter extends RecyclerView.Adapter<PostViewHolder>{
    private final Context context;
    private final AppViewModel appViewModel;
    private final NavController navController;
    private final String userId;
    private final Client client;
    private DocumentList<Map<String, Object>> list;

    public PostAdapter(Context context, AppViewModel appViewModel, NavController navController, String userId, Client client) {
        this.context = context;
        this.appViewModel = appViewModel;
        this.navController = navController;
        this.userId = userId;
        this.client = client;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        return new
                PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position)
    {
        Map<String, Object> post = list.getDocuments().get(position).getData();

        // 处理媒体
        handleMedia(post, holder);

        // 处理作者信息
        handleAuthorInfo(post, holder);

        // 处理点赞逻辑
        handleLikes(post, holder);
    }

    private void handleMedia(Map<String, Object> post, PostViewHolder holder) {
        if (post.get("mediaUrl") != null) {
            holder.mediaImageView.setVisibility(View.VISIBLE);
            if ("audio".equals(post.get("mediatype"))) {
                Glide.with(context).load(R.drawable.audio).into(holder.mediaImageView);
            } else {
                Glide.with(context).load(post.get("mediaUrl")).into(holder.mediaImageView);
            }
            holder.mediaImageView.setOnClickListener(v -> navigateToMediaFragment(post));
        } else {
            holder.mediaImageView.setVisibility(View.GONE);
        }
    }

    private void handleAuthorInfo(Map<String, Object> post, PostViewHolder holder) {
        if (post.get("authorPhotoUrl") == null) {
            holder.authorPhotoImageView.setImageResource(R.drawable.user);
        } else {
            Glide.with(context)
                    .load(post.get("authorPhotoUrl"))
                    .circleCrop()
                    .into(holder.authorPhotoImageView);
        }
        holder.authorTextView.setText(Objects.requireNonNull(post.get("author")).toString());
        holder.contentTextView.setText(Objects.requireNonNull(post.get("content")).toString());
    }

    private void handleLikes(Map<String, Object> post, PostViewHolder holder) {
        List<String> likes = (List<String>) post.get("likes");
        assert likes != null;
        updateLikeUI(likes, holder);

        holder.likeImageView.setOnClickListener(v -> updateLikesInDatabase(post, likes, holder));
    }

    private void updateLikeUI(List<String> likes, PostViewHolder holder) {
        holder.likeImageView.setImageResource(
                likes.contains(userId) ? R.drawable.like_on : R.drawable.like_off
        );
        holder.numLikesTextView.setText(String.valueOf(likes.size()));
    }

    private void updateLikesInDatabase(
            Map<String, Object> post,
            List<String> likes,
            PostViewHolder holder
    ) {
        List<String> newLikes = new ArrayList<>(likes);
        if (newLikes.contains(userId)) {
            newLikes.remove(userId);
        } else {
            newLikes.add(userId);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("likes", newLikes);

        try {
            new Databases(client).updateDocument(
                    context.getString(R.string.APPWRITE_DATABASE_ID),
                    context.getString(R.string.APPWRITE_POSTS_COLLECTION_ID),
                    Objects.requireNonNull(post.get("$id")).toString(),
                    data,
                    new ArrayList<>(),
                    new CoroutineCallback<>((result, error) -> {
                        if (error != null) {
                            throw new RuntimeException(error);
                        }
                        holder.numLikesTextView.setText(String.valueOf(newLikes.size()));
                        // 刷新列表
                        if (list != null) {
                            list.getDocuments().get(holder.getAdapterPosition()).getData()
                                    .put("likes", newLikes);
                            notifyItemChanged(holder.getAdapterPosition());
                        }
                    })
            );
        } catch (AppwriteException e) {
            throw new RuntimeException(e);
        }
    }

    private void navigateToMediaFragment(Map<String, Object> post) {
        appViewModel.postDeselection.setValue(post);
        navController.navigate(R.id.mediaFragment);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.getDocuments().size();
    }
    public void setList(DocumentList<Map<String, Object>> posts) {
        this.list = posts;
        notifyDataSetChanged();
    }
}