package com.stakkfactory.cachedemo.presenter;

import android.util.Log;

import com.stakkfactory.cachedemo.CacheDemoApp;
import com.stakkfactory.cachedemo.network.api.ApiGetVideo;
import com.stakkfactory.cachedemo.presenter.interfaceView.IVideoView;
import com.stakkfactory.mediacache.DiskCache;
import com.stakkfactory.mediacache.MediaCacheManager.MediaCache;
import com.stakkfactory.mediacache.MediaCacheManager.Metadata;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Created by danielchung on 4/5/16.
 */
public class VideoPresenter extends BasePresenter<IVideoView> implements ApiGetVideo.OnGetVideoListener {

    public final static String TAG = VideoPresenter.class.getSimpleName();

    static final String[] VIDEOS = {
            "https://d2v9y0dukr6mq2.cloudfront.net/video/preview/alpha-channel-flames-from-center_bjubppugs__PM.mp4"
    };

    public VideoPresenter(IVideoView view) {
        super(view);
    }

    public void initDownloadVideo() {
        view.startDownloadTimer();

        final Random rand = new Random();
        int randPos = rand.nextInt(VIDEOS.length);

        String url = VIDEOS[randPos];

        Log.d(TAG, "initDownloadVideo url: " + url);

        DiskCache mediaCache = CacheDemoApp.app().mediaCache();

        if (mediaCache.contains(url)) {
            Log.d(TAG, "mediaCache.contains video");

            String path = mediaCache.getTempFilePath(url);
            setVideoPathToView(path);
            view.setFromSource("Cache");
        } else {
            apiGetVideo(url);
            view.setFromSource("Server");
        }
    }

    void apiGetVideo(String url) {
        Log.d(TAG, "apiGetVideo: " + url);

        new ApiGetVideo(url).setListener(this).execute();
    }

    @Override
    public void onGetVideo(String url, String videoPath) {
        Log.d(TAG, "onGetVideo: " + videoPath);

        setVideoPathToView(videoPath);

        FileInputStream is = null;
        try {
            is = new FileInputStream(videoPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (is != null) {
            DiskCache mediaCache = CacheDemoApp.app().mediaCache();

            Metadata metadata = new Metadata(url, MediaCache.MEDIA_TYPE_VIDEO, url.substring(url.lastIndexOf(".")));
            MediaCache<InputStream> videoCache = new MediaCache<InputStream>(is, metadata);

            try {
                mediaCache.put(videoCache);
                Log.d(TAG, "mediaCache.put video");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFailGetVideo(String url) {
        Log.e(TAG, "onFailGetVideo");
    }

    void setVideoPathToView(String videoPath) {
        Log.d(TAG, "setVideoPathToView: " + videoPath);
        if (videoPath != null) {
            view.setVideo(videoPath);
        }
        view.endDownloadTimer();
    }
}
