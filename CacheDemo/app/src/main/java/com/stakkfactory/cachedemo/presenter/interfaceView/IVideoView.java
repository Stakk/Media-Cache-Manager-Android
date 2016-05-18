package com.stakkfactory.cachedemo.presenter.interfaceView;

/**
 * Created by danielchung on 4/5/16.
 */
public interface IVideoView {

    void startDownloadTimer();

    void endDownloadTimer();

    void setVideo(String videoPath);

    void setFromSource(String source);
}
