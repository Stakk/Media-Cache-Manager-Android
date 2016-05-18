package com.stakkfactory.cachedemo.ui.activites;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Daniel on 31/3/2016.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        beforeOnCreate();
        super.onCreate(savedInstanceState);

        setContentView(layoutId());
        connectViews();
        setListeners();
        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        
    }

    abstract protected void beforeOnCreate();

    abstract protected int layoutId();

    abstract protected void connectViews();

    abstract protected void setListeners();

    abstract protected void initViews();

    protected Activity getActivity() {
        return this;
    }

}
