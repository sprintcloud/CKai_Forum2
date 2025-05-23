package com.example.ckaiforum;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.ckaiforum.Adapter.PostAdapter;
import com.example.ckaiforum.ViewModel.AppViewModel;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import io.appwrite.Client;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.services.Account;
import io.appwrite.services.Databases;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private NavController navController;
    private ImageView photoImageView;
    private TextView displayNameTextView, emailTextView;
    private Client client;
    private Account account;
    private PostAdapter adapter;
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


        initViews(view);
        initClient();
        setupNavigationHeader(view);

        fetchPosts();
    }

    private void initViews(View view) {
        RecyclerView postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostAdapter(
                requireContext(),
                new ViewModelProvider(requireActivity()).get(AppViewModel.class),
                Navigation.findNavController(view),
                getUserIdSafely(),
                client
        );
        postsRecyclerView.setAdapter(adapter);
        navController = Navigation.findNavController(view);
    }

    private String getUserIdSafely() {
        return userId != null ? userId : "";
    }

    private void initClient() {
        client = new Client(requireContext())
                .setProject(getString(R.string.APPWRITE_PROJECT_ID));
        account = new Account(client);
        AppViewModel appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
    }

    private void setupNavigationHeader(View view) {
        NavigationView navView = requireActivity().findViewById(R.id.nav_view);
        View header = navView.getHeaderView(0);

        photoImageView = header.findViewById(R.id.imageView);
        displayNameTextView = header.findViewById(R.id.displayNameTextView);
        emailTextView = header.findViewById(R.id.emailTextView);
        ImageView notificationImageView = header.findViewById(R.id.notificationBell);

        view.findViewById(R.id.gotoNewPostFragmentButton).setOnClickListener(v ->
                navController.navigate(R.id.NewPostFragment));

        notificationImageView.setOnClickListener(v -> navController.navigate(R.id.notificationsFragment));
    }

    private void fetchPosts() {
            try {
                account.get(new CoroutineCallback<>((result, error) -> {
                    if (error != null) return;

                    requireActivity().runOnUiThread(() -> {
                        assert result != null;
                        displayNameTextView.setText(result.getName());
                        emailTextView.setText(result.getEmail());
                        Glide.with(requireContext()).load(R.drawable.user).into(photoImageView);
                        userId = result.getId();
                    });

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


                                    requireActivity().runOnUiThread(() ->
                                            adapter.setList(result1)
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