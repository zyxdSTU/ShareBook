package com.zy.sharebook.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/4/9.
 */

public class FragmentAdapter extends FragmentPagerAdapter {

    //碎片数据
    private ArrayList<Fragment> mList;

    public FragmentAdapter(FragmentManager fragmentManager,ArrayList<Fragment>mList) {
        super(fragmentManager);
        this.mList = mList;
    }

    @Override
    public Fragment getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

}
