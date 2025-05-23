package com.example.ckaiforum;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;

import io.appwrite.Client;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.services.Account;

public class RegisterFragment extends Fragment {

    private EditText usernameEditText, emailEditText, passwordEditText;

    private Button registerButton;

    Client client;

    NavController navController;
    public RegisterFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle
            savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        usernameEditText = view.findViewById(R.id.usernameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);

        registerButton = view.findViewById(R.id.registerButton);

        registerButton.setOnClickListener(view1 -> crearCuenta());
    }

    private void crearCuenta(){
        if (!validFormulary()){
            return;
        }

        registerButton.setEnabled(false);

        client = new Client(requireActivity().getApplicationContext());
        client.setProject(getString(R.string.APPWRITE_PROJECT_ID));

        Account account = new Account(client);

        Handler mainHandler = new Handler(Looper.getMainLooper());

        try{
            account.create(
                    "unique()",
                    emailEditText.getText().toString(),
                    passwordEditText.getText().toString(),
                    usernameEditText.getText().toString(),
                    new CoroutineCallback<>((result, error) -> {
                        mainHandler.post(() -> registerButton.setEnabled(true));

                        if (error != null){
                            Snackbar.make(requireView(), "Error: " + error, Snackbar.LENGTH_LONG).show();
                            return;
                        }

                        account.createEmailPasswordSession(
                                emailEditText.getText().toString(),
                                passwordEditText.getText().toString(),

                                new CoroutineCallback<>((result2, error2) -> {
                                    if (error2 != null){
                                        Snackbar.make(requireView(), "Error: " + error2, Snackbar.LENGTH_LONG).show();
                                    }
                                    else{
                                        System.out.println("Sesión creada para el usuario:" + result2);
                                        mainHandler.post(this::actualizarUI);
                                    }
                                })
                        );
                    })
            );
        } catch (AppwriteException e){
            throw new RuntimeException(e);
        }
    }

    private void actualizarUI(){
        navController.navigate(R.id.signInFragment);
    }

    private boolean validFormulary(){
        boolean valid = true;

        if (TextUtils.isEmpty(emailEditText.getText().toString())){
            emailEditText.setError("Required.");
        } else {
            emailEditText.setError(null);
        }

        if (TextUtils.isEmpty(passwordEditText.getText().toString())){
            passwordEditText.setError("Required.");
        }else {
            passwordEditText.setError(null);
        }

        return valid;
    }
}