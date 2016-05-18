package com.stakkfactory.cachedemo.presenter.interfaceView;

import android.graphics.Bitmap;

/**
 * Created by danielchung on 3/5/16.
 */
public interface IImageView {

    void startDownloadTimer();

    void endDownloadTimer();

    void setImageBitmap(Bitmap bitmap);

    void setFromSource(String source);
}
