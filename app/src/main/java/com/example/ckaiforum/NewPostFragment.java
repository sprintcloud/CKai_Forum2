package com.example.ckaiforum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.ckaiforum.ViewModel.AppViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class NewPostFragment extends Fragment {

    Button publishButton;
    EditText postContentEditText;
    NavController navController;
    Client client;
    Account account;
    AppViewModel appViewModel;
    Uri mediaUri;
    String mediaType;

    public NewPostFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle
            savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        client = new Client(requireContext())
                .setProject(getString(R.string.APPWRITE_PROJECT_ID));
        publishButton = view.findViewById(R.id.publishButton);
        postContentEditText = view.findViewById(R.id.postContentEditText);

        publishButton.setOnClickListener(v -> publican());

        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);

        view.findViewById(R.id.camara_fotos).setOnClickListener(v -> tomarFoto());

        view.findViewById(R.id.camara_video).setOnClickListener(v ->
                tomarVideo());
        view.findViewById(R.id.grabar_audio).setOnClickListener(v ->
                recordAudio());
        view.findViewById(R.id.imagen_galeria).setOnClickListener(v ->
                selectionImage());
        view.findViewById(R.id.video_galeria).setOnClickListener(v ->
                selectionVideo());
        view.findViewById(R.id.audio_galeria).setOnClickListener(v ->
                selectionAudio());
        appViewModel.mediaDeselection.observe(getViewLifecycleOwner(), media
                ->
        {
            this.mediaUri = media.uri;
            this.mediaType = media.type;
            Glide.with(this).load(media.uri).into((ImageView)
                    view.findViewById(R.id.previsualizacion));
        });
    }

    private void publican() {
        String postContent = postContentEditText.getText().toString();

        if (TextUtils.isEmpty(postContent)){
            postContentEditText.setError("Required");
            return;
        }

        publishButton.setEnabled(false);


        account = new Account(client);

        try {
            account.get(new CoroutineCallback<>((result, error) -> {
                if (error != null) {
                    throw new RuntimeException(error);
                }

                if (mediaType == null && result != null){
                    saveEnAppWrite(result, postContent, null);
                }else {
                    uploadAndSaveInAppWrite(result,postContent);
                }
            }));
        }catch (AppwriteException e){
            throw new RuntimeException(e);
        }
    }

    void saveEnAppWrite(User<Map<String, Object>> user, String content, String mediaUrl){

        Handler mainHandler = new Handler(Looper.getMainLooper());

        Databases databases = new Databases(client);

        Storage storage = new Storage(client);

        try{
            account.get(new CoroutineCallback<>((result, error) -> {
                if (error != null){
                    throw new RuntimeException(error);
                }

                assert result != null;
                requireActivity().runOnUiThread(() -> {
                    storage.listFiles(getString(R.string.APPWRITE_STORAGE_BUCKET_ID),
                            new CoroutineCallback<>((result2, error2) -> {
                                String authorPhotoUrl = null;
                                if (error2 != null) {
                                    Snackbar.make(requireView(), "Error " + error2.getMessage(), Snackbar.LENGTH_LONG).show();
                                    return;
                                }
                                for (io.appwrite.models.File files : result2.getFiles()) {
                                    if (files.getName().contains(result.getId())) {
                                        authorPhotoUrl = "https://cloud.appwrite.io/v1/storage/buckets/" +
                                                getString(R.string.APPWRITE_STORAGE_BUCKET_ID) + "/files/" + files.getId() +
                                                "/view?project=" + getString(R.string.APPWRITE_PROJECT_ID) + "&project=" +
                                                getString(R.string.APPWRITE_PROJECT_ID) + "&mode=admin";
                                    }
                                }
                                Map<String, Object> data = new HashMap<>();
                                data.put("uid", user.getId());
                                data.put("author", user.getName());
                                data.put("authorPhotoUrl", authorPhotoUrl);
                                data.put("content", content);
                                data.put("mediatype", mediaType);
                                data.put("mediaUrl", mediaUrl);
                                try {
                                    databases.createDocument(
                                            getString(R.string.APPWRITE_DATABASE_ID),
                                            getString(R.string.APPWRITE_POSTS_COLLECTION_ID),
                                            "unique()",
                                            data,
                                            new ArrayList<>(),
                                            new CoroutineCallback<>((result3, error3) -> {
                                                if (error3 != null) {
                                                    Snackbar.make(requireView(), "Error: " +
                                                            error3, Snackbar.LENGTH_LONG).show();
                                                } else {
                                                    System.out.println("Post created: " +
                                                            mainHandler.post(() -> navController.popBackStack()));
                                                }
                                            })
                                    );
                                } catch (AppwriteException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                    );
                });
            }));
        }catch (AppwriteException e) {
            throw new RuntimeException(e);
        }
        }


    private final ActivityResultLauncher<String> galleria =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> appViewModel.setMediaDeselection(uri, mediaType));
    private final ActivityResultLauncher<Uri> camaraPhoto =
            registerForActivityResult(new ActivityResultContracts.TakePicture(),
                    isSuccess -> appViewModel.setMediaDeselection(mediaUri, "image"));
    private final ActivityResultLauncher<Uri> camaraVideos =
            registerForActivityResult(new ActivityResultContracts.TakeVideo(),
                    isSuccess -> appViewModel.setMediaDeselection(mediaUri, "video"));
    private final ActivityResultLauncher<Intent> recordAudio =
            registerForActivityResult(new
                    ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    assert result.getData() != null;
                    appViewModel.setMediaDeselection(result.getData().getData(),
                            "audio");
                }
            });
    private void selectionImage() {
        mediaType = "image";
        galleria.launch("image/*");
    }
    private void selectionVideo() {
        mediaType = "video";
        galleria.launch("video/*");
    }
    private void selectionAudio() {
        mediaType = "audio";
        galleria.launch("audio/*");
    }
    private void tomarFoto() {
        try {
            mediaUri = FileProvider.getUriForFile(requireContext(),
                    "com.example.socialappwrite" + ".fileprovider",
                    File.createTempFile("img", ".jpg",
                            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES))
            );
            camaraPhoto.launch(mediaUri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void tomarVideo() {
        try {
            mediaUri = FileProvider.getUriForFile(requireContext(),
                    "com.example.socialappwrite" + ".fileprovider",
                    File.createTempFile("vid", ".mp4",
                            requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES)));
            camaraVideos.launch(mediaUri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void recordAudio() {
        recordAudio.launch(new
                Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION));
    }

    private void uploadAndSaveInAppWrite(User<Map<String, Object>> user, final String
            postText)
    {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Storage storage = new Storage(client);
        File tempFile;
        try {
            tempFile = getFileFromUri(requireContext(), mediaUri);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        storage.createFile(
                getString(R.string.APPWRITE_STORAGE_BUCKET_ID), // bucketId
                "unique()", // fileId
                InputFile.Companion.fromFile(tempFile), // file
                new ArrayList<>(), // permissions (optional)
                new CoroutineCallback<>((result, error) -> {
                    if (error != null) {
                        System.err.println("Error subiendo el archivo:" +
                                error.getMessage() );
                        return;
                    }
                    assert result != null;
                    String downloadUrl =
                            "https://cloud.appwrite.io/v1/storage/buckets/" +
                                    getString(R.string.APPWRITE_STORAGE_BUCKET_ID) + "/files/" + result.getId() +
                                    "/view?project=" + getString(R.string.APPWRITE_PROJECT_ID) + "&project=" +
                                    getString(R.string.APPWRITE_PROJECT_ID) + "&mode=admin";
                    mainHandler.post(() ->
                            saveEnAppWrite(user, postText, downloadUrl));
                })
        );
    }

    public File getFileFromUri(Context context, Uri uri) throws Exception
    {
        InputStream inputStream =
                context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new FileNotFoundException("No se pudo abrir el URI: " + uri);
        }
        String fileName = getFileName(context, uri);
        File tempFile = new File(context.getCacheDir(), fileName);
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
        inputStream.close();
        return tempFile;
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

    public ActivityResultLauncher<Uri> getCamaraPhoto() {
        return camaraPhoto;
    }
}