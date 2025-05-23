package com.example.ckaiforum.ViewModel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.Map;

public class AppViewModel extends AndroidViewModel
{
    public static class Media {
        public Uri uri;
        public String type;
        public Media(Uri uri, String type) {
            this.uri = uri;
            this.type = type;
        }
    }
    public MutableLiveData<Map<String,Object>> postDeselection = new
            MutableLiveData<>();
    public MutableLiveData<Media> mediaDeselection = new
            MutableLiveData<>();
    public AppViewModel(@NonNull Application application) {
        super(application);
    }
    public void setMediaDeselection(Uri uri, String type) {
        mediaDeselection.setValue(new Media(uri, type));
    }
}