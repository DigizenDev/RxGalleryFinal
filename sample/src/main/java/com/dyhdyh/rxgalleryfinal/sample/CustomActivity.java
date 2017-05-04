package com.dyhdyh.rxgalleryfinal.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.digizen.rxgalleryfinal.IMediaActivityDelegate;
import com.digizen.rxgalleryfinal.MediaActivityDelegate;


/**
 * author  dengyuhan
 * created 2017/4/20 11:33
 */
public class CustomActivity extends AppCompatActivity implements IMediaActivityDelegate {
    private CustomActivityDelegate mDelegate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDelegate = new CustomActivityDelegate(this);
        mDelegate.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mDelegate.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mDelegate.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDelegate.onDestroy();
    }

    @Override
    public void onBackPressed() {
        boolean onBackPressed = mDelegate.onBackPressed();
        if (onBackPressed){
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public MediaActivityDelegate getMediaActivityDelegate() {
        return mDelegate;
    }
}
