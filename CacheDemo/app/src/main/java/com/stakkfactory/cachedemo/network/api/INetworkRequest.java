package com.stakkfactory.cachedemo.network.api;

/**
 * Created by danielchung on 4/5/16.
 */
public interface INetworkRequest<T> {

    String url();

    void execute();

    boolean isDemoDataEnabled();

    T demoData();


}
