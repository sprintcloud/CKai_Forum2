package com.example.ckaiforum.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ckaiforum.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.appwrite.models.Document;
import io.appwrite.models.DocumentList;

public class BottomSheetPostAdapter extends RecyclerView.Adapter<BottomSheetPostHolder> {
    private List<Map<String, Object>> filteredList = new ArrayList<>();
    private Context context;

    @NonNull
    @Override
    public BottomSheetPostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new
                BottomSheetPostHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.subviewholder_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BottomSheetPostHolder holder, int position) {
        Map<String, Object> list = filteredList.get(position);
        holder.contentTextView.setText(list.get("content").toString());
        holder.authorTextView.setText(list.get("author").toString());
        Glide.with(context).load(list.get("authorPhotoUrl")).into(holder.authorPhotoImageView);
        holder.shareImageView.setOnClickListener(v -> {
            String shareText = "Author: " + list.get("author") + "\n\r"
                    + "Content: " + list.get("content");
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            context.startActivity(Intent.createChooser(shareIntent, "share text for"));
        });
    }

    public BottomSheetPostAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void setList(DocumentList<Map<String, Object>> list) {
        for (Document<Map<String, Object>> doc : list.getDocuments()) {
            Map<String, Object> data = doc.getData();
            filteredList.add(data);
        }
        notifyDataSetChanged();
    }

    private void shareWithImageAndText() {


    }
}
