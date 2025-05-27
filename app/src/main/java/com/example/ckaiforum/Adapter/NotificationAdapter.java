package com.example.ckaiforum.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ckaiforum.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.Document;
import io.appwrite.models.DocumentList;
import io.appwrite.services.Databases;
import kotlin.coroutines.Continuation;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder> {
    private String userId;
    private List<Map<String, Object>> filteredList = new ArrayList<>();


    public NotificationAdapter(DocumentList<Map<String, Object>> list, String userId) {
        this.userId = userId;

        for (Document<Map<String, Object>> doc : list.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (userId.equals(data.get("uid"))) {
                filteredList.add(data);
            }
        }
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        return new
                NotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Map<String, Object> notification = filteredList.get(position);
        holder.titleView.setText(notification.get("title").toString());
        holder.contentView.setText(notification.get("content").toString());
    }

    @Override
    public int getItemCount() {
        return filteredList.size();

    }

    public void setList(DocumentList<Map<String, Object>> newList) {
        filteredList.clear();
        for (Document<Map<String, Object>> doc : newList.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (userId.equals(data.get("userId"))) {
                filteredList.add(data);
            }
        }
        notifyDataSetChanged();
    }

    public void actReadAllData(Databases databases, String databaseId, String collectionId,
                                  Context context){
        for (Map<String, Object> documentData : filteredList) {
            String docId  = (String) documentData.get("$id");
            assert docId  != null;
            databases.deleteDocument(databaseId, collectionId, docId,
                    new CoroutineCallback<>((result, error)->{
                        if (error != null){
                            System.out.println(error);
                        }else{
                            if (context instanceof Activity) {
                                ((Activity) context).runOnUiThread(() -> {
                                    filteredList.clear();
                                    notifyDataSetChanged();
                                    Snackbar.make(
                                            ((Activity) context).findViewById(android.R.id.content),
                                            "Todo leidos",
                                            Snackbar.LENGTH_SHORT
                                    ).show();
                                });
                            }
                        }
            }));
        }
    }
}
