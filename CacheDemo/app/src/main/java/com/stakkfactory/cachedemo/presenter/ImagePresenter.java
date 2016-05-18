package com.stakkfactory.cachedemo.presenter;

import android.graphics.Bitmap;
import android.util.Log;

import com.stakkfactory.cachedemo.CacheDemoApp;
import com.stakkfactory.cachedemo.network.api.ApiGetImage;
import com.stakkfactory.cachedemo.presenter.interfaceView.IImageView;
import com.stakkfactory.mediacache.DiskCache;
import com.stakkfactory.mediacache.MediaCacheManager.MediaCache;
import com.stakkfactory.mediacache.MediaCacheManager.Metadata;

import java.io.IOException;
import java.util.Random;

/**
 * Created by danielchung on 3/5/16.
 */
public class ImagePresenter extends BasePresenter<IImageView> implements ApiGetImage.OnGetImageListener {

    public final static String TAG = ImagePresenter.class.getSimpleName();

    static final String[] IMAGES = {
            "https://d2v9y0dukr6mq2.cloudfront.net/video/thumbnail/alpha-channel-flames-from-center_bjubppugs__M0000.jpg"
    };

    public ImagePresenter(IImageView view) {
        super(view);
    }

    public void initDownloadImage() {
        view.startDownloadTimer();
        view.setImageBitmap(null);

        final Random rand = new Random();
        int randPos = rand.nextInt(IMAGES.length);

        String url = IMAGES[randPos];

        Log.d(TAG, "initDownloadImage url: " + url);

        DiskCache mediaCache = CacheDemoApp.app().mediaCache();

        if (mediaCache.contains(url)) {
            Log.d(TAG, "mediaCache.contains bitmap");

            Bitmap image = mediaCache.getBitmap(url);

            setImageToView(image);
            view.setFromSource("Cache");
        } else {
            apiGetImage(url);
            view.setFromSource("Server");
        }
    }

    void apiGetImage(String url) {
        Log.d(TAG, "apiGetImage");
        new ApiGetImage(url).setListener(this).execute();
    }

    @Override
    public void onGetImage(String url, Bitmap image) {
        if (image != null) {
            setImageToView(image);

            DiskCache mediaCache = CacheDemoApp.app().mediaCache();

            Metadata metadata = new Metadata(url, MediaCache.MEDIA_TYPE_IMAGE, url.substring(url.lastIndexOf(".")));
            MediaCache<Bitmap> bitmapCache = new MediaCache<>(image, metadata);

            try {
                mediaCache.put(bitmapCache);
                Log.d(TAG, "mediaCache.put bitmap");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFailGetImage(String url) {
        Log.d(TAG, "failGetImage");
    }

    void setImageToView(Bitmap bitmap) {
        if (bitmap != null) {
            view.setImageBitmap(bitmap);
        }
        view.endDownloadTimer();
    }
}
