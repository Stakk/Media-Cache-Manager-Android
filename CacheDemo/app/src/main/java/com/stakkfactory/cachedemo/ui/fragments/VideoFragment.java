package com.stakkfactory.cachedemo.ui.fragments;

import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;
import android.widget.VideoView;

import com.stakkfactory.cachedemo.R;
import com.stakkfactory.cachedemo.presenter.VideoPresenter;
import com.stakkfactory.cachedemo.presenter.interfaceView.IVideoView;

/**
 * Created by danielchung on 29/4/16.
 */
public class VideoFragment extends BaseFragment implements IVideoView, MediaPlayer.OnPreparedListener {

    public final static String TAG = VideoFragment.class.getSimpleName();

    VideoView videoView;
    TextView loadingTimeTextView;
    TextView fromSourceTextView;

    VideoPresenter mVideoPresenter;

    long startLoadingTime = 0;
    long endLoadingTime = 0;

    public static BaseFragment newInstance() {
        VideoFragment fragment = new VideoFragment();

        return fragment;
    }

    @Override
    protected void beforeOnCreate() {
        mVideoPresenter = new VideoPresenter(this);
    }

    @Override
    protected int layoutId() {
        return R.layout.fragment_video;
    }

    @Override
    protected void connectViews() {
        videoView = (VideoView) findViewById(R.id.videoView);
        loadingTimeTextView = (TextView) findViewById(R.id.loadingTime);
        fromSourceTextView = (TextView) findViewById(R.id.fromSource);
    }

    @Override
    protected void setListeners() {
        videoView.setOnPreparedListener(this);
    }

    @Override
    protected void initViews() {
        mVideoPresenter.initDownloadVideo();
    }

    @Override
    public void startDownloadTimer() {
        startLoadingTime = System.currentTimeMillis();
    }

    @Override
    public void endDownloadTimer() {
        endLoadingTime = System.currentTimeMillis();
        loadingTimeTextView.setText("Time: " + (endLoadingTime - startLoadingTime) / 1000f + "s");
    }

    @Override
    public void setVideo(String videoPath) {
        videoView.setVideoURI(Uri.parse(videoPath));
        videoView.start();
    }

    @Override
    public void setFromSource(String source) {
        fromSourceTextView.setText(source);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared");
        mp.setLooping(true);
        mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
    }
}
