package com.example.ckaiforum;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowMetrics;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ckaiforum.Adapter.BottomSheetPostAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import io.appwrite.Client;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.Document;
import io.appwrite.services.Account;
import io.appwrite.services.Databases;

public class MyBottomSheetDialogFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_bottom_sheet_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.subPostRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        BottomSheetPostAdapter adapter = new BottomSheetPostAdapter(getContext());
        recyclerView.setAdapter(adapter);

        Client client = new Client(requireContext())
                    .setProject(getString(R.string.APPWRITE_PROJECT_ID));
        try {
            new Databases(client).listDocuments(
                    getString(R.string.APPWRITE_DATABASE_ID),
                    getString(R.string.APPWRITE_SUB_POSTS_COLLECTION_ID),
                    new ArrayList<>(),
                    new CoroutineCallback<>((result, error) -> {
                        if (error != null){
                            error.printStackTrace();
                            return;
                        }
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> adapter.setList(result));
                    })
            );
        } catch (AppwriteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 设置背景透明
            dialog.getWindow().setDimAmount(0.4f); // 设置背景遮罩透明度（0 = 完全透明，1 = 不透明）
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (!(dialog instanceof BottomSheetDialog)) {
            return;
        }

        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet == null) {
            return;
        }

        int screenHeight;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = requireActivity().getWindowManager().getCurrentWindowMetrics();
            screenHeight = windowMetrics.getBounds().height();
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            screenHeight = displayMetrics.heightPixels;
        }

        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setPeekHeight((int) (screenHeight * 0.7)); // 初始折叠高度

        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
        layoutParams.height = (int) (screenHeight * 0.8); // 最大高度
        bottomSheet.setLayoutParams(layoutParams);
    }

}