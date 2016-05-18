package com.stakkfactory.cachedemo.ui.activites;

import android.support.v4.view.ViewPager;

import com.stakkfactory.cachedemo.R;
import com.stakkfactory.cachedemo.presenter.MainPresenter;
import com.stakkfactory.cachedemo.presenter.interfaceView.IMainView;
import com.stakkfactory.cachedemo.views.adapters.SubPageAdapter;

public class MainActivity extends BaseActivity implements IMainView {

    ViewPager viewpager;

    MainPresenter mMainPresenter;

    @Override
    protected void beforeOnCreate() {
        mMainPresenter = new MainPresenter(this);
    }

    @Override
    protected int layoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void connectViews() {
        viewpager = (ViewPager) findViewById(R.id.viewPager);
    }

    @Override
    protected void setListeners() {

    }

    @Override
    protected void initViews() {
        mMainPresenter.intiPagerAdapter(getSupportFragmentManager());
    }

    @Override
    public void setViewPagerAdapter(SubPageAdapter pagerAdapter) {
        viewpager.setAdapter(pagerAdapter);
    }
}
