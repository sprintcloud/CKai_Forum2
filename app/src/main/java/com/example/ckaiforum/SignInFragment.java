package com.example.ckaiforum;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;

import io.appwrite.Client;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.services.Account;

public class SignInFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private LinearLayout signInForm;
    private ProgressBar signInProgressBar;
    Client client;
    Account account;
    NavController navController;
    public SignInFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle
            savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        view.findViewById(R.id.gotoCreateAccountTextView)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view){
                        navController.navigate(R.id.registerFragment);
                    }
                });

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        Button emailSignInButton = view.findViewById(R.id.emailSignInButton);
        signInForm = view.findViewById(R.id.signInForm);
        signInProgressBar = view.findViewById(R.id.signInProgressBar);

        Handler mainHandler = new Handler(Looper.getMainLooper());

        client = new Client(requireActivity().getApplicationContext());
        client.setProject(getString(R.string.APPWRITE_PROJECT_ID));

        account = new Account(client);

        account.getSession(
                "current",
                new CoroutineCallback<>((result, error) -> {
                    if (error != null){
                        throw new RuntimeException(error);
                    }

                    if (result != null){
                        mainHandler.post(() -> actualizarUI());
                    }
                })
        );

        emailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accederConEmail();
            }
        });
    }

    private void accederConEmail(){
        signInForm.setVisibility(View.GONE);
        signInProgressBar.setVisibility(View.VISIBLE);

        Account account = new Account(client);

        Handler mainHandler = new Handler(Looper.getMainLooper());

        account.createEmailPasswordSession(
                emailEditText.getText().toString(),
                passwordEditText.getText().toString(),
                new CoroutineCallback<>((result, error) -> {
                    if (error != null){
                        Snackbar.make(requireView(), "Error: " + error.toString(),
                                Snackbar.LENGTH_LONG).show();
                    }
                    else {
                        System.out.println("Sesión creada para el usuario:" +result);
                        mainHandler.post(() -> actualizarUI());
                        mainHandler.post(() -> {
                            signInForm.setVisibility(View.VISIBLE);
                            signInProgressBar.setVisibility(View.GONE);
                        });
                    }
                })
        );
    };
    private void actualizarUI(){
        navController.navigate(R.id.homeFragment);
    }
}