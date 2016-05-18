package com.stakkfactory.cachedemo.presenter;

import android.support.v4.app.FragmentManager;

import com.stakkfactory.cachedemo.presenter.interfaceView.IMainView;
import com.stakkfactory.cachedemo.views.adapters.SubPageAdapter;

/**
 * Created by danielchung on 29/4/16.
 */
public class MainPresenter extends BasePresenter<IMainView> {

    public MainPresenter(IMainView view) {
        super(view);
    }

    public void intiPagerAdapter(FragmentManager fm) {

        view.setViewPagerAdapter(new SubPageAdapter(fm));
    }
}
