package com.example.ckaiforum;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ckaiforum.Adapter.NotificationAdapter;
import com.example.ckaiforum.ViewModel.AppViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.appwrite.Client;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.services.Account;
import io.appwrite.services.Databases;

public class NotificationsFragment extends Fragment {

    Account account;
    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private Client client;
    private Databases databases;
    AppViewModel appViewModel;
    String userId;


    public NotificationsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle
            savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        initClient();
        fetchNotifications();
    }

    private void initClient() {
        client = new Client(requireContext())
                .setProject(getString(R.string.APPWRITE_PROJECT_ID));
        databases = new Databases(client);
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
    }

    private void fetchNotifications() {
        account = new Account(client);
        try {
            account.get(new CoroutineCallback<>((result, error) -> {
                if (error != null) {
                    throw new RuntimeException(error);
                }
                assert result != null;
                userId = result.getId();
            }));
        } catch (AppwriteException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> queries = new HashMap<>();
        queries.put("userId", userId);
        try {
            databases.listDocuments(
                    getString(R.string.APPWRITE_DATABASE_ID),
                    getString(R.string.APPWRITE_NOTIFICATIONS_COLLECTION_ID),
                    new ArrayList<>(Integer.parseInt(userId)),
                    new CoroutineCallback<>((result, error)->{
                        if (error != null){
                            Snackbar.make(requireView(), "Error al obtener los notifications: "
                                    + error, Snackbar.LENGTH_LONG).show();
                            System.out.println(error);
                        }
                        System.out.println(result);
                        requireActivity().runOnUiThread(() -> {
                            Map<String,Object> notification = result.getDocuments().get(1).getData();

//                            if (notification.get("$id")
                            notificationAdapter = new NotificationAdapter(result);
                            notificationsRecyclerView.setAdapter(notificationAdapter);
                        });
                    })
            );
        } catch (AppwriteException e) {
            throw new RuntimeException(e);
        }
    }
}