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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import io.appwrite.Client;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.services.Account;

public class ProfileFragment extends Fragment {

    NavController navController;
    ImageView photoImageView;
    TextView displayNameTextView, emailTextView;

    public ProfileFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle
            savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        photoImageView = view.findViewById(R.id.photoImageView);
        displayNameTextView = view.findViewById(R.id.displayNameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);

        Client client = new Client(requireContext())
                .setProject(getString(R.string.APPWRITE_PROJECT_ID));

        Account account = new Account(client);

        try{
            account.get(new CoroutineCallback<>((result, error) -> {
                if (error != null){
                    throw new RuntimeException(error);
                }

                assert result != null;
                requireActivity().runOnUiThread(() -> {
                    displayNameTextView.setText(result.getName());
                    emailTextView.setText(result.getEmail());
                    Glide.with(requireView()).load(R.drawable.user).into(photoImageView);
                });


            }));
        }catch (AppwriteException e){
            throw new RuntimeException(e);
        }
    }
}