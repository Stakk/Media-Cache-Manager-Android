package com.stakkfactory.cachedemo.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.stakkfactory.cachedemo.ui.fragments.GifImageFragment;
import com.stakkfactory.cachedemo.ui.fragments.ImageFragment;
import com.stakkfactory.cachedemo.ui.fragments.VideoFragment;

/**
 * Created by danielchung on 29/4/16.
 */
public class SubPageAdapter extends FragmentStatePagerAdapter {

    public final static String TAG = SubPageAdapter.class.getSimpleName();

    public SubPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(int position) {

        Log.d(TAG, "getItem: " + position);

        switch (position % 3) {
            case 0:
                return ImageFragment.newInstance();

            case 1:
                return GifImageFragment.newInstance();

            case 2:
                return VideoFragment.newInstance();

        }

        return null;
    }

}
