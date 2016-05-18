package com.stakkfactory.cachedemo.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Daniel on 31/3/2016.
 */
public abstract class BaseFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        beforeOnCreate();
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(layoutId(), container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        connectViews();
        setListeners();
        initViews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    View findViewById(int id) {
        return getView().findViewById(id);
    }

    abstract protected void beforeOnCreate();

    abstract protected int layoutId();

    abstract protected void connectViews();

    abstract protected void setListeners();

    abstract protected void initViews();
}