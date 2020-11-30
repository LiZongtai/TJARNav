package com.example.tjarnav.ar.mapbox;

import android.app.Application;

import com.example.tjarnav.R;
import com.mapbox.vision.VisionManager;


public class VisionApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VisionManager.init(this, getResources().getString(R.string.mapbox_access_token));
    }
}

