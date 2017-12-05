package com.zy.sharebook.application;

import android.app.Application;
import android.content.Context;

import com.zy.sharebook.database.DatabaseHelper;
import com.zy.sharebook.util.PreferenceManager;

/**
 * Created by ZY on 2017/11/21.
 */

public class MyApplication extends Application {
    private Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        /*初始化缓存*/
        PreferenceManager.init(mContext);

        /*初始化数据库*/
        DatabaseHelper.init(mContext);
    }
}
