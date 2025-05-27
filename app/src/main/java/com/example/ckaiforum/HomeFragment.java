package com.example.ckaiforum;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.ckaiforum.Adapter.NotificationAdapter;
import com.example.ckaiforum.Adapter.PostAdapter;
import com.example.ckaiforum.ViewModel.AppViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import io.appwrite.Client;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.Document;
import io.appwrite.models.DocumentList;
import io.appwrite.models.File;
import io.appwrite.services.Account;
import io.appwrite.services.Databases;
import io.appwrite.services.Storage;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private NavController navController;
    private DrawerLayout drawer;
    private Toolbar activityToolbar;
    private ImageView photoImageView, notificationImageView;
    private TextView displayNameTextView, emailTextView;
    private FloatingActionButton newPostButton;
    private Client client;
    private Account account;
    private Storage storage;
    private PostAdapter postAdapter;
    private String userId;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newPostButton = view.findViewById(R.id.gotoNewPostFragmentButton);
        activityToolbar = requireActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(activityToolbar);

        navController = Navigation.findNavController(view);
        drawer = requireActivity().findViewById(R.id.main);


        initClient(view);
        setupNavigationHeader(view);
        setupNavigation();
        fetchPosts();
    }

    private void setupNavigation() {
        navController = Navigation.findNavController(requireView());
        drawer = requireActivity().findViewById(R.id.main);

        activityToolbar.setNavigationOnClickListener(v -> {
            int currentDestId = navController.getCurrentDestination().getId();
            if (currentDestId == R.id.homeFragment) {
                if (!drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.openDrawer(GravityCompat.START);
                    Databases databases = new Databases(client);

                    try {
                        databases.listDocuments(getString(R.string.APPWRITE_DATABASE_ID),
                                getString(R.string.APPWRITE_NOTIFICATIONS_COLLECTION_ID),
                                new CoroutineCallback<>((result, error) ->{
                                    if (error != null){
                                        error.printStackTrace();
                                        return;
                                    }

                                    for (Document<Map<String, Object>> doc : result.getDocuments()) {
                                        if (doc.getData().get("uid").equals(userId)){
                                            requireActivity().runOnUiThread(() -> Glide.with(requireContext()).load(R.drawable.ic_notification_bell_red).into(notificationImageView));
                                        }else {
                                            requireActivity().runOnUiThread(() -> Glide.with(requireContext()).load(R.drawable.ic_notification_bell).into(notificationImageView));

                                        }
                                    }
                                }));
                    } catch (AppwriteException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    drawer.closeDrawer(GravityCompat.START);
                }
            } else if (currentDestId == R.id.signInFragment) {
                // 登录页逻辑（如果需要）
            } else {
                navController.navigateUp();
            }
        });
    }

    private String getUserIdSafely() {
        return userId != null ? userId : "";
    }

    private void initClient(View view) {
        client = new Client(requireActivity())
                .setProject(getString(R.string.APPWRITE_PROJECT_ID));
        account = new Account(client);
        storage = new Storage(client);
        AppViewModel appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        try {
            account.get(new CoroutineCallback<>((result, error) -> {
            if (error != null){
                Snackbar.make(requireView(),error.toString(),Snackbar.LENGTH_LONG).show();
            }
                requireActivity().runOnUiThread(() -> {
                    userId = result.getId();

                    RecyclerView postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
                    postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    postAdapter = new PostAdapter(
                            requireContext(),
                            new ViewModelProvider(requireActivity()).get(AppViewModel.class),
                            Navigation.findNavController(view),
                            getUserIdSafely(),
                            client,
                            result.getName(),
                            getChildFragmentManager()
                    );
                    postsRecyclerView.setAdapter(postAdapter);

                    fetchPosts();
                });
        }));
    }catch (AppwriteException e){
            throw new RuntimeException(e);
        }
    }

    private void setupNavigationHeader(View view) {
        NavigationView navView = requireActivity().findViewById(R.id.nav_view);
        View header = navView.getHeaderView(0);
        photoImageView = header.findViewById(R.id.imageView);
        displayNameTextView = header.findViewById(R.id.displayNameTextView);
        emailTextView = header.findViewById(R.id.emailTextView);
        notificationImageView = header.findViewById(R.id.notificationBell);

        DrawerLayout drawer = requireActivity().findViewById(R.id.main);


        newPostButton.setOnClickListener(v -> navController.navigate(R.id.NewPostFragment));

        notificationImageView.setOnClickListener(v -> {
            drawer.closeDrawers();
            navController.navigate(R.id.notificationsFragment);
        });
    }

    private void fetchPosts() {
        try {
            account.get(new CoroutineCallback<>((result, error) -> {
                if (error != null) return;

                requireActivity().runOnUiThread(() -> {
                    assert result != null;
                    displayNameTextView.setText(result.getName());
                    emailTextView.setText(result.getEmail());
                    userId = result.getId();
                });

                storage.listFiles(getString(R.string.APPWRITE_STORAGE_BUCKET_ID),
                        new CoroutineCallback<>((result2, error2) -> {
                            if (error2 != null) {
                                Snackbar.make(requireView(), "Error " + error2.getMessage(), Snackbar.LENGTH_LONG).show();
                                return;
                            }
                            for (File files : result2.getFiles()) {
                                if (files.getName().contains(userId)) {
                                    Uri uri = Uri.parse("https://cloud.appwrite.io/v1/storage/buckets/" +
                                            getString(R.string.APPWRITE_STORAGE_BUCKET_ID) + "/files/" + files.getId() +
                                            "/view?project=" + getString(R.string.APPWRITE_PROJECT_ID) + "&project=" +
                                            getString(R.string.APPWRITE_PROJECT_ID) + "&mode=admin");
                                    requireActivity().runOnUiThread(() -> Glide.with(requireView()).load(uri).into(photoImageView));
                                    return;
                                }
                            }
                            requireActivity().runOnUiThread(() -> Glide.with(requireView()).load(R.drawable.user).into(photoImageView));
                        }));

                Databases databases = new Databases(client);

                try {
                    databases.listDocuments(
                            getString(R.string.APPWRITE_DATABASE_ID), // databaseId
                            getString(R.string.APPWRITE_POSTS_COLLECTION_ID), // collectionId
                            new ArrayList<>(), // queries (optional)
                            new CoroutineCallback<>((result1, error1) -> {
                                if (error1 != null) {
                                    Snackbar.make(requireView(), "Error al obtener los posts: "
                                            + error1, Snackbar.LENGTH_LONG).show();
                                    return;
                                }

                                requireActivity().runOnUiThread(() -> {
                                            postAdapter.setList(result1);
                                        }
                                );
                            })
                    );
                } catch (AppwriteException e) {
                    throw new RuntimeException(e);
                }
            }));
        } catch (AppwriteException e) {
            throw new RuntimeException(e);
        }
    }
}