package com.stakkfactory.cachedemo.presenter.interfaceView;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by danielchung on 3/5/16.
 */
public interface IGifImageView {

    void startDownloadTimer();

    void endDownloadTimer();

    void setGifImage(GifDrawable gifDrawable);

    void setFromSource(String source);
}
