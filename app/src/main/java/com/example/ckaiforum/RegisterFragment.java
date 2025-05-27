package com.example.ckaiforum;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.appwrite.Client;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.InputFile;
import io.appwrite.models.User;
import io.appwrite.services.Account;
import io.appwrite.services.Databases;
import io.appwrite.services.Storage;

public class RegisterFragment extends Fragment {

    private EditText usernameEditText, emailEditText, passwordEditText;

    private Button registerButton;

    Client client;

    private ImageView avatarImageView;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    NavController navController;
    Uri mediaUri;
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

        avatarImageView = view.findViewById(R.id.avatarImageView);

        avatarImageView.setOnClickListener(v -> openImageChooser());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        mediaUri = result.getData().getData();
                        Glide.with(requireContext())
                                .load(mediaUri)
                                .error(R.drawable.camera)
                                .into(avatarImageView);
                    }
                });


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
        Storage storage = new Storage(client);
        Databases databases = new Databases(client);

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
                            Snackbar.make(requireView(), "Error: " + error.toString(), Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        System.out.println(result);
                        account.createEmailPasswordSession(
                                emailEditText.getText().toString(),
                                passwordEditText.getText().toString(),
                                new CoroutineCallback<>((result2, error2) -> {
                                    if (error2 != null){
                                        Snackbar.make(requireView(), "Error: " + error2, Snackbar.LENGTH_LONG).show();
                                        return;
                                    }
                                    System.out.println("Sesión creada para el usuario:" + result2);
                                    File tempFile;
                                    try {
                                        tempFile = getFileFromUri(requireContext(), mediaUri, result2.getId());
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                    storage.createFile(
                                            getString(R.string.APPWRITE_STORAGE_BUCKET_ID), // bucketId
                                            "unique()", // fileId
                                            InputFile.Companion.fromFile(tempFile), // file
                                            new ArrayList<>(), // permissions (optional)
                                            new CoroutineCallback<>((result3, error3) -> {
                                                if (error3 != null) {
                                                    System.err.println("Error subiendo el archivo:" +
                                                            error3.getMessage() );
                                                    return;
                                                }
                                                assert result3 != null;
                                                String downloadUrl =
                                                        "https://cloud.appwrite.io/v1/storage/buckets/" +
                                                                getString(R.string.APPWRITE_STORAGE_BUCKET_ID) + "/files/" + result3.getId() +
                                                                "/view?project=" + getString(R.string.APPWRITE_PROJECT_ID) + "&project=" +
                                                                getString(R.string.APPWRITE_PROJECT_ID) + "&mode=admin";
                                            })
                                    );
                                    mainHandler.post(this::actualizarUI);
                                })
                        );
                    })
            );
        } catch (AppwriteException e){
            throw new RuntimeException(e);
        }
    }

    public File getFileFromUri(Context context, Uri uri, String customFileName) throws Exception
    {
        InputStream inputStream =
                context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new FileNotFoundException("No se pudo abrir el URI: " + uri);
        }
        String fileName = (customFileName != null && !customFileName.isEmpty())
                ? getValidFileName(customFileName, uri)
                : getFileName(context, uri);
        File tempFile = new File(context.getCacheDir(), fileName);
        try (inputStream;
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[8192]; // 8KB 缓冲区
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    // 处理自定义文件名的合法性
    private String getValidFileName(String customName, Uri uri) {
        // 添加时间戳防止重名
        String extension = getFileExtension(uri); // 获取文件扩展名

        if (!customName.endsWith(extension)) {
            customName += extension;
        }

        return customName;
    }

    // 从 URI 中提取文件扩展名
    private String getFileExtension(Uri uri) {
        String mimeType = requireContext().getContentResolver().getType(uri);
        if (mimeType != null) {
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        }
        return ""; // 默认无扩展名
    }
    private String getFileName(Context context, Uri uri)
    {
        String fileName = "temp_file";
        try (Cursor cursor = context.getContentResolver().query(uri, null, null,
                null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex =
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        }
        return fileName;
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

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }


}