package com.dyhdyh.rxgalleryfinal.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.dyhdyh.rxgalleryfinal.MediaActivityDelegate;

import cn.finalteam.rxgalleryfinal.sample.R;


/**
 * author  dengyuhan
 * created 2017/4/20 11:49
 */
public class CustomActivityDelegate extends MediaActivityDelegate {
    private TextView mTitleView;

    public CustomActivityDelegate(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    protected void onCreateOk(@Nullable Bundle savedInstanceState) {
        super.onCreateOk(savedInstanceState);
    }

    @Override
    protected TextView getOverActionView() {
        return (TextView) mActivity.findViewById(R.id.tv_over_action);
    }

    @Override
    protected int getFragmentContainerId() {
        return R.id.fragment_container;
    }

    @Override
    protected void setTitle(CharSequence title) {
        mTitleView.setText(title);
    }

    @Override
    protected void findViews() {
        mTitleView = (TextView) mActivity.findViewById(R.id.tv_toolbar_title);
    }

    @Override
    protected void setContentView() {
        mActivity.setContentView(R.layout.activity_custom);
    }

}
