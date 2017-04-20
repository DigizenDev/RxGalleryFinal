package com.dyhdyh.rxgalleryfinal;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import cn.finalteam.rxgalleryfinal.BuildConfig;
import cn.finalteam.rxgalleryfinal.Configuration;

/**
 * author  dengyuhan
 * created 2017/4/20 11:10
 */
public abstract class BaseMediaActivityDelegate {
    public static final String EXTRA_PREFIX = BuildConfig.APPLICATION_ID;
    public static final String EXTRA_CONFIGURATION = EXTRA_PREFIX + ".Configuration";

    protected Configuration mConfiguration;
    protected AppCompatActivity mActivity;

    public BaseMediaActivityDelegate(AppCompatActivity activity) {
        this.mActivity = activity;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        Intent intent = mActivity.getIntent();
        Bundle bundle = null;
        if (intent != null) {
            bundle = intent.getExtras();
        }

        if (savedInstanceState != null) {
            mConfiguration = savedInstanceState.getParcelable(EXTRA_CONFIGURATION);
        }
        if (mConfiguration == null && bundle != null) {
            mConfiguration = bundle.getParcelable(EXTRA_CONFIGURATION);
        }

        if (mConfiguration == null) {
            return;
        } else {
            if (bundle == null) {
                bundle = savedInstanceState;
            }
            setContentView();
            findViews();
            onCreateOk(bundle);
        }
    }

    protected abstract void onCreateOk(@Nullable Bundle savedInstanceState);

    protected abstract void findViews();

    protected abstract void setContentView();


    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(EXTRA_CONFIGURATION, mConfiguration);
    }


    public boolean finish() {
        return true;
    }


    public boolean onBackPressed() {
        return true;
    }


    public void onDestroy() {

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mConfiguration = savedInstanceState.getParcelable(EXTRA_CONFIGURATION);
    }


    public void setTheme() {

    }

    public AppCompatActivity getActivity() {
        return mActivity;
    }
}