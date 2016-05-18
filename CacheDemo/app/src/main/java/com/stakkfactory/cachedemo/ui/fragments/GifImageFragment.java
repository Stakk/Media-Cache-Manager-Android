package com.stakkfactory.cachedemo.ui.fragments;

import android.widget.TextView;

import com.felipecsl.gifimageview.library.GifImageView;
import com.stakkfactory.cachedemo.R;
import com.stakkfactory.cachedemo.presenter.GifImagePresenter;
import com.stakkfactory.cachedemo.presenter.interfaceView.IGifImageView;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by danielchung on 5/5/16.
 */
public class GifImageFragment extends ImageFragment implements IGifImageView {

    GifImageView gifImageView;

    GifImagePresenter mGifImagePresenter;

    public static BaseFragment newInstance() {
        GifImageFragment fragment = new GifImageFragment();

        return fragment;
    }

    @Override
    protected void beforeOnCreate() {
        mGifImagePresenter = new GifImagePresenter(this);
    }

    @Override
    protected int layoutId() {
        return R.layout.fragment_gif_image;
    }

    @Override
    protected void connectViews() {
        gifImageView = (GifImageView) findViewById(R.id.gifImageView);
        loadingTimeTextView = (TextView) findViewById(R.id.loadingTime);
        fromSourceTextView = (TextView) findViewById(R.id.fromSource);
    }

    @Override
    protected void initViews() {
        mGifImagePresenter.initDownloadGifImage();
    }

    @Override
    public void setGifImage(GifDrawable gifDrawable) {
        gifImageView.setImageDrawable(gifDrawable);
    }
}
