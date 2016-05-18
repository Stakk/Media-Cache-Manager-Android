package com.stakkfactory.cachedemo.ui.fragments;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

import com.stakkfactory.cachedemo.R;
import com.stakkfactory.cachedemo.presenter.ImagePresenter;
import com.stakkfactory.cachedemo.presenter.interfaceView.IImageView;

/**
 * Created by danielchung on 29/4/16.
 */
public class ImageFragment extends BaseFragment implements IImageView {

    public final static String TAG = ImageFragment.class.getSimpleName();

    ImageView imageView;
    TextView loadingTimeTextView;
    TextView fromSourceTextView;

    ImagePresenter mImagePresenter;


    long startLoadingTime = 0;
    long endLoadingTime = 0;

    public static BaseFragment newInstance() {
        ImageFragment fragment = new ImageFragment();

        return fragment;
    }

    @Override
    protected void beforeOnCreate() {
        mImagePresenter = new ImagePresenter(this);
    }

    @Override
    protected int layoutId() {
        return R.layout.fragment_image;
    }

    @Override
    protected void connectViews() {
        imageView = (ImageView) findViewById(R.id.imageView);
        loadingTimeTextView = (TextView) findViewById(R.id.loadingTime);
        fromSourceTextView = (TextView) findViewById(R.id.fromSource);
    }

    @Override
    protected void setListeners() {

    }

    @Override
    protected void initViews() {
        mImagePresenter.initDownloadImage();
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
    public void setImageBitmap(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void setFromSource(String source) {
        fromSourceTextView.setText(source);
    }
}
