package com.example.ckaiforum.Adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

        handleMedia(post, holder);

        handleAuthorInfo(post, holder);

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
        holder.deletePost.setVisibility(userId.equals(post.get("uid")) ? View.VISIBLE : View.GONE);
    }

    private void handleLikes(Map<String, Object> post, PostViewHolder holder) {
        List<String> likes = (List<String>) post.get("likes");
        int comments = Integer.parseInt(Objects.requireNonNull(post.get("comments")).toString());
        assert likes != null;
        updateLikeUI(likes, comments, holder);

        holder.commentLinearLayout.setOnClickListener(v -> showCommentDialog(post, holder, comments));
        holder.likeLinearLayout.setOnClickListener(v -> updateLikesInDatabase(post, likes, holder));
        holder.deletePost.setOnClickListener(v -> delePost(post, holder));
    }

    private void showCommentDialog(final Map<String, Object> post, final PostViewHolder holder, int comments) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Post a comment");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Publish", (dialog, which) -> {
            String comment = input.getText().toString().trim();
            if (!comment.isEmpty()) {
                Toast.makeText(context, "Published successfully", Toast.LENGTH_SHORT).show();

                int newComments = comments + 1;
                Map<String, Object> data = new HashMap<>();
                data.put("comments",newComments);

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

                                if (list != null) {
                                    Handler handler = new Handler(Looper.getMainLooper());
                                    handler.post(() ->
                                    {
                                        holder.numCommentTextView.setText(String.valueOf(comments));
                                        list.getDocuments().get(holder.getAdapterPosition()).getData()
                                                .put("comments", newComments);
                                        setCommentsNotification(post, comments);
                                        notifyItemChanged(holder.getAdapterPosition());
                                    });


                                }
                            })
                    );
                } catch (AppwriteException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Toast.makeText(context, "Content cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void delePost(Map<String, Object> post, PostViewHolder holder) {
        Databases databases = new Databases(client);
        databases.deleteDocument(context.getString(R.string.APPWRITE_DATABASE_ID),
                context.getString(R.string.APPWRITE_POSTS_COLLECTION_ID),
                Objects.requireNonNull(post.get("$id")).toString(),
                new CoroutineCallback<>((result, error) -> {
                    if (error != null){
                        error.printStackTrace();
                        return;
                    }
                    Handler handler = new Handler(Looper.getMainLooper());

                        try {
                            databases.listDocuments(context.getString(R.string.APPWRITE_DATABASE_ID),
                                    context.getString(R.string.APPWRITE_POSTS_COLLECTION_ID),
                                    new CoroutineCallback<>((result1, error1) -> {
                                        handler.post(() ->
                                        {
                                            setList(result1);
                                            notifyDataSetChanged();
                                        });
                                    }));
                        } catch (AppwriteException e) {
                            throw new RuntimeException(e);
                        }
                    handler.post(() -> notifyItemChanged(holder.getAdapterPosition()));
                }));
    }

    private void updateLikeUI(List<String> likes, int comments, PostViewHolder holder) {
        holder.likeImageView.setImageResource(
                likes.contains(userId) ? R.drawable.like_on : R.drawable.like_off
        );
        holder.numLikesTextView.setText(String.valueOf(likes.size()));
        holder.numCommentTextView.setText(String.valueOf(comments));
    }

    private void setLikesNotification(Map<String, Object> post,
                                 List<String> newLikes){
        Databases databases = new Databases(client);

        Map<String, Object> data = new HashMap<>();

        data.put("title", "System Notifications");
        data.put("content", "Your post has new likes, totaling " + newLikes.size());
        data.put("uid",post.get("uid"));
        try{
            databases.createDocument(context.getString(R.string.APPWRITE_DATABASE_ID),
            context.getString(R.string.APPWRITE_NOTIFICATIONS_COLLECTION_ID),
                    "unique()",
                    data,
                    new CoroutineCallback<>((result, error) -> {
                        if (error != null){
                            error.printStackTrace();
                            return;
                        }
                    }));
        }catch (AppwriteException e){
            throw new RuntimeException(e);
        }
    }

    private void setCommentsNotification(Map<String, Object> post,
                                      int comments){
        Databases databases = new Databases(client);

        Map<String, Object> data = new HashMap<>();

        data.put("title", "System Notifications");
        data.put("content", "Your post has new comment, totaling " + comments);
        data.put("uid",post.get("uid"));
        try{
            databases.createDocument(context.getString(R.string.APPWRITE_DATABASE_ID),
                    context.getString(R.string.APPWRITE_NOTIFICATIONS_COLLECTION_ID),
                    "unique()",
                    data,
                    new CoroutineCallback<>((result, error) -> {
                        if (error != null){
                            error.printStackTrace();
                            return;
                        }
                    }));
        }catch (AppwriteException e){
            throw new RuntimeException(e);
        }
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

                        if (list != null) {

                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(() ->
                            {
                                holder.numLikesTextView.setText(String.valueOf(newLikes.size()));
                                list.getDocuments().get(holder.getAdapterPosition()).getData()
                                        .put("likes", newLikes);
                                setLikesNotification(post, newLikes);
                                notifyItemChanged(holder.getAdapterPosition());
                            });
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