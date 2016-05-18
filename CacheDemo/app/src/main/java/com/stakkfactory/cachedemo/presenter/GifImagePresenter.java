package com.stakkfactory.cachedemo.presenter;

import android.util.Log;

import com.stakkfactory.cachedemo.CacheDemoApp;
import com.stakkfactory.cachedemo.network.api.ApiGetGifImage;
import com.stakkfactory.cachedemo.presenter.interfaceView.IGifImageView;
import com.stakkfactory.mediacache.DiskCache;
import com.stakkfactory.mediacache.MediaCacheManager.MediaCache;
import com.stakkfactory.mediacache.MediaCacheManager.Metadata;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by danielchung on 3/5/16.
 */
public class GifImagePresenter extends BasePresenter<IGifImageView> implements ApiGetGifImage.OnGetGifImageListener {

    public final static String TAG = GifImagePresenter.class.getSimpleName();

    static final String[] IMAGES = {
            "http://i.makeagif.com/media/5-13-2016/M7USck.gif"
    };

    public GifImagePresenter(IGifImageView view) {
        super(view);
    }

    public void initDownloadGifImage() {
        view.startDownloadTimer();

        final Random rand = new Random();
        int randPos = rand.nextInt(IMAGES.length);

        String url = IMAGES[randPos];

        Log.d(TAG, "initDownloadGifImage url: " + url);

        DiskCache mediaCache = CacheDemoApp.app().mediaCache();


        if (mediaCache.contains(url)) {
            Log.d(TAG, "mediaCache.contains gif");

            GifDrawable gif = toGifDrawable(mediaCache.getInputStream(url));
            setImageToView(gif);
            view.setFromSource("Cache");
        } else {
            apiGetGifImage(url);
            view.setFromSource("Server");
        }
    }

    GifDrawable toGifDrawable(InputStream is) {
        GifDrawable gifDrawable = null;

        if (is != null) {
            try {
                gifDrawable = new GifDrawable(new BufferedInputStream(is));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return gifDrawable;
    }

    void apiGetGifImage(String url) {
        Log.d(TAG, "apiGetGifImage");
        new ApiGetGifImage(url).setListener(this).execute();
    }

    @Override
    public void onGetGitImage(String url, String gifPath) {
        GifDrawable gifDrawable;
        try {
            gifDrawable = new GifDrawable(gifPath);
            setImageToView(gifDrawable);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileInputStream is = null;
        try {
            is = new FileInputStream(gifPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (is != null) {
            DiskCache mediaCache = CacheDemoApp.app().mediaCache();

            Metadata metadata = new Metadata(url, MediaCache.MEDIA_TYPE_GIF, url.substring(url.lastIndexOf(".")));
            MediaCache<InputStream> gifCache = new MediaCache<InputStream>(is, metadata);

            try {
                mediaCache.put(gifCache);
                Log.d(TAG, "mediaCache.put gif");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFailGetGitImage(String url) {
        Log.e(TAG, "onFailGetGitImage");
    }

    void setImageToView(GifDrawable gifDrawable) {
        if (gifDrawable != null) {
            view.setGifImage(gifDrawable);
            gifDrawable.start();
        }
        view.endDownloadTimer();
    }
}
