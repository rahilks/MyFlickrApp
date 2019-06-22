package com.example.myflickrapp;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

public class MyFlickrApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
