package com.stakkfactory.cachedemo;

import android.app.Application;
import android.util.Log;

import com.stakkfactory.mediacache.DiskCache;
import com.stakkfactory.mediacache.MediaCacheManager;

import java.io.IOException;

/**
 * Created by danielchung on 3/5/16.
 */
public class CacheDemoApp extends Application {

    public final static String TAG = CacheDemoApp.class.getSimpleName();

    /**
     * A singleton instance of the application class for easy access in other
     * places
     */
    private static CacheDemoApp sInstance;

    private DiskCache mMediaCache;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        // initialize the singleton
        sInstance = this;
    }

    @Override
    public void onTerminate() {
        Log.d(TAG, "onTerminate");

        sInstance = null;
        super.onTerminate();
    }

    /**
     * @return Application singleton instance
     */
    public static synchronized CacheDemoApp app() {

        return sInstance;
    }

    public synchronized DiskCache mediaCache() {
        if (mMediaCache == null) {
            try {
                // Open max 150 MB as dir name MediasCache
                mMediaCache = MediaCacheManager.openCache(this, "MediasCache", 1024 * 1024 * 8 * 150);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return mMediaCache;
    }
}