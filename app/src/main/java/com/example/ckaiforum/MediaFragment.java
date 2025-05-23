package com.example.ckaiforum;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.ckaiforum.ViewModel.AppViewModel;

import java.util.Objects;

public class MediaFragment extends Fragment
{
    VideoView videoView;
    ImageView imageView;
    public AppViewModel appViewModel;
    public MediaFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup
            container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_media, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle
            savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        appViewModel = new
                ViewModelProvider(requireActivity()).get(AppViewModel.class);
        imageView = view.findViewById(R.id.imageView);
        videoView = view.findViewById(R.id.videoView);
        appViewModel.postDeselection.observe(getViewLifecycleOwner(), post
                -> {
            String mediaType = Objects.requireNonNull(post.get("mediatype")).toString();
            String mediaUrl = Objects.requireNonNull(post.get("mediaUrl")).toString();
            if ("video".equals(mediaType) ||
                    "audio".equals(mediaType)) {
                MediaController mc = new MediaController(requireContext());
                mc.setAnchorView(videoView);
                videoView.setMediaController(mc);
                videoView.setVideoPath(Objects.requireNonNull(post.get("mediaUrl")).toString());
                videoView.start();
            } else if ("image".equals(mediaType)) {
                Glide.with(requireView()).load(mediaUrl).into(imageView);
            }
        });
    }
}