package com.stakkfactory.cachedemo.presenter;

/**
 * Created by Daniel on 31/3/2016.
 */
public abstract class BasePresenter<V> {

    V view;

    public BasePresenter(V view) {
        this.view = view;
    }
}