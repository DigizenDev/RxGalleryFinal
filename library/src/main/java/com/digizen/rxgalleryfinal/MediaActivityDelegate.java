package com.digizen.rxgalleryfinal;

import android.content.pm.PackageManager;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.finalteam.rxgalleryfinal.R;
import cn.finalteam.rxgalleryfinal.bean.MediaBean;
import cn.finalteam.rxgalleryfinal.rxbus.RxBus;
import cn.finalteam.rxgalleryfinal.rxbus.RxBusSubscriber;
import cn.finalteam.rxgalleryfinal.rxbus.event.BaseResultEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.CloseRxMediaGridPageEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.ImageMultipleResultEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.MediaCheckChangeEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.MediaViewPagerChangedEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.OpenMediaPageFragmentEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.OpenMediaPreviewFragmentEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.RequestStorageReadAccessPermissionEvent;
import cn.finalteam.rxgalleryfinal.rxjob.RxJob;
import cn.finalteam.rxgalleryfinal.ui.fragment.MediaGridFragment;
import cn.finalteam.rxgalleryfinal.ui.fragment.MediaPageFragment;
import cn.finalteam.rxgalleryfinal.ui.fragment.MediaPreviewFragment;
import cn.finalteam.rxgalleryfinal.utils.Logger;
import cn.finalteam.rxgalleryfinal.utils.ThemeUtils;
import cn.finalteam.rxgalleryfinal.view.ActivityFragmentView;
import rx.Subscription;
import rx.functions.Func1;

/**
 * author  dengyuhan
 * created 2017/4/20 11:16
 */
public abstract class MediaActivityDelegate extends BaseMediaActivityDelegate implements ActivityFragmentView {

    public static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    public static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;

    private static final String EXTRA_CHECKED_LIST = EXTRA_PREFIX + ".CheckedList";
    private static final String EXTRA_SELECTED_INDEX = EXTRA_PREFIX + ".SelectedIndex";
    private static final String EXTRA_PAGE_MEDIA_LIST = EXTRA_PREFIX + ".PageMediaList";
    private static final String EXTRA_PAGE_POSITION = EXTRA_PREFIX + ".PagePosition";
    private static final String EXTRA_PREVIEW_POSITION = EXTRA_PREFIX + ".PreviewPosition";

    MediaGridFragment mMediaGridFragment;
    MediaPageFragment mMediaPageFragment;
    MediaPreviewFragment mMediaPreviewFragment;

    private TextView mTvOverAction;

    private ArrayList<MediaBean> mCheckedList;
    private int mSelectedIndex = 0;
    private ArrayList<MediaBean> mPageMediaList;
    private int mPagePosition;
    private int mPreviewPosition;

    public MediaActivityDelegate(AppCompatActivity activity) {
        super(activity);
    }

    protected abstract TextView getOverActionView();

    @Override
    protected void onCreateOk(@Nullable Bundle savedInstanceState) {
        mMediaGridFragment = MediaGridFragment.newInstance(mConfiguration);
        mTvOverAction = getOverActionView();
        if (!mConfiguration.isRadio()) {
            mTvOverAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCheckedList != null && mCheckedList.size() > 0) {
                        BaseResultEvent event = new ImageMultipleResultEvent(mCheckedList);
                        RxBus.getDefault().post(event);
                        finish();
                    }
                }
            });
            mTvOverAction.setVisibility(View.VISIBLE);
        } else {
            mTvOverAction.setVisibility(View.GONE);
        }
        mCheckedList = new ArrayList<>();
        List<MediaBean> selectedList = mConfiguration.getSelectedList();
        if (selectedList != null && selectedList.size() > 0) {
            mCheckedList.addAll(selectedList);
        }

        showMediaGridFragment();
        subscribeEvent();
    }

    @Override
    public void showMediaGridFragment() {
        mMediaPreviewFragment = null;
        mMediaPageFragment = null;
        mSelectedIndex = 0;

        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction()
                .replace(getFragmentContainerId(), mMediaGridFragment);
        if (mMediaPreviewFragment != null) {
            ft.hide(mMediaPreviewFragment);
        }
        if (mMediaPageFragment != null) {
            ft.hide(mMediaPageFragment);
        }
        ft.show(mMediaGridFragment)
                .commit();

        if (mConfiguration.isImage()) {
            setTitle(mActivity.getString(R.string.gallery_media_grid_image_title));
        } else {
            setTitle(mActivity.getString(R.string.gallery_media_grid_video_title));
        }
    }

    protected abstract @IdRes int getFragmentContainerId();

    protected abstract void setTitle(CharSequence title);

    @Override
    public void showMediaPageFragment(ArrayList<MediaBean> list, int position) {
        mSelectedIndex = 1;
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        mMediaPageFragment = MediaPageFragment.newInstance(mConfiguration, list, position);
        ft.add(getFragmentContainerId(), mMediaPageFragment);
        mMediaPreviewFragment = null;
        ft.hide(mMediaGridFragment);
        ft.show(mMediaPageFragment);
        ft.commit();

        String title = mActivity.getString(R.string.gallery_page_title, position + 1, list.size());
        setTitle(title);
    }

    @Override
    public void showMediaPreviewFragment() {
        mSelectedIndex = 2;
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        mMediaPreviewFragment = MediaPreviewFragment.newInstance(mConfiguration, mPreviewPosition);
        ft.add(getFragmentContainerId(), mMediaPreviewFragment);
        mMediaPageFragment = null;
        ft.hide(mMediaGridFragment);
        ft.show(mMediaPreviewFragment);
        ft.commit();

        String title = mActivity.getString(R.string.gallery_page_title, mPreviewPosition, mCheckedList.size());
        setTitle(title);
    }

    private void subscribeEvent() {
        Subscription subscriptionOpenMediaPreviewEvent = RxBus.getDefault().toObservable(OpenMediaPreviewFragmentEvent.class)
                .map(new Func1<OpenMediaPreviewFragmentEvent, OpenMediaPreviewFragmentEvent>() {
                    @Override
                    public OpenMediaPreviewFragmentEvent call(OpenMediaPreviewFragmentEvent openMediaPreviewFragmentEvent) {
                        return openMediaPreviewFragmentEvent;
                    }
                })
                .subscribe(new RxBusSubscriber<OpenMediaPreviewFragmentEvent>() {
                    @Override
                    protected void onEvent(OpenMediaPreviewFragmentEvent openMediaPreviewFragmentEvent) {
                        mPreviewPosition = 0;
                        showMediaPreviewFragment();
                    }
                });

        RxBus.getDefault().add(subscriptionOpenMediaPreviewEvent);

        Subscription subscriptionMediaCheckChangeEvent = RxBus.getDefault().toObservable(MediaCheckChangeEvent.class)
                .map(new Func1<MediaCheckChangeEvent, MediaCheckChangeEvent>() {
                    @Override
                    public MediaCheckChangeEvent call(MediaCheckChangeEvent event) {
                        return event;
                    }
                })
                .subscribe(new RxBusSubscriber<MediaCheckChangeEvent>() {
                    @Override
                    protected void onEvent(MediaCheckChangeEvent mediaCheckChangeEvent) {
                        MediaBean mediaBean = mediaCheckChangeEvent.getMediaBean();
                        if (mCheckedList.contains(mediaBean)) {
                            mCheckedList.remove(mediaBean);
                        } else {
                            mCheckedList.add(mediaBean);
                        }

                        if (mCheckedList.size() > 0) {
                            String text = mActivity.getResources().getString(R.string.gallery_over_button_text_checked, mCheckedList.size(), mConfiguration.getMaxSize());
                            mTvOverAction.setText(text);
                            mTvOverAction.setEnabled(true);
                        } else {
                            mTvOverAction.setText(R.string.gallery_over_button_text);
                            mTvOverAction.setEnabled(false);
                        }
                    }
                });
        RxBus.getDefault().add(subscriptionMediaCheckChangeEvent);

        Subscription subscriptionMediaViewPagerChangedEvent = RxBus.getDefault().toObservable(MediaViewPagerChangedEvent.class)
                .map(new Func1<MediaViewPagerChangedEvent, MediaViewPagerChangedEvent>() {
                    @Override
                    public MediaViewPagerChangedEvent call(MediaViewPagerChangedEvent mediaViewPagerChangedEvent) {
                        return mediaViewPagerChangedEvent;
                    }
                })
                .subscribe(new RxBusSubscriber<MediaViewPagerChangedEvent>() {
                    @Override
                    protected void onEvent(MediaViewPagerChangedEvent mediaPreviewViewPagerChangedEvent) {
                        int curIndex = mediaPreviewViewPagerChangedEvent.getCurIndex();
                        int totalSize = mediaPreviewViewPagerChangedEvent.getTotalSize();
                        if (mediaPreviewViewPagerChangedEvent.isPreview()) {
                            mPreviewPosition = curIndex;
                        } else {
                            mPagePosition = curIndex;
                        }
                        String title = mActivity.getString(R.string.gallery_page_title, curIndex + 1, totalSize);
                        setTitle(title);
                    }
                });
        RxBus.getDefault().add(subscriptionMediaViewPagerChangedEvent);

        Subscription subscriptionCloseRxMediaGridPageEvent = RxBus.getDefault().toObservable(CloseRxMediaGridPageEvent.class)
                .subscribe(new RxBusSubscriber<CloseRxMediaGridPageEvent>() {
                    @Override
                    protected void onEvent(CloseRxMediaGridPageEvent closeRxMediaGridPageEvent) throws Exception {
                        finish();
                    }
                });
        RxBus.getDefault().add(subscriptionCloseRxMediaGridPageEvent);

        Subscription subscriptionOpenMediaPageFragmentEvent = RxBus.getDefault().toObservable(OpenMediaPageFragmentEvent.class)
                .subscribe(new RxBusSubscriber<OpenMediaPageFragmentEvent>() {
                    @Override
                    protected void onEvent(OpenMediaPageFragmentEvent openMediaPageFragmentEvent) {
                        mPageMediaList = openMediaPageFragmentEvent.getMediaBeanList();
                        mPagePosition = openMediaPageFragmentEvent.getPosition();

                        showMediaPageFragment(mPageMediaList, mPagePosition);
                    }
                });
        RxBus.getDefault().add(subscriptionOpenMediaPageFragmentEvent);
    }


    @Override
    public boolean onBackPressed() {
        if ((mMediaPreviewFragment != null && mMediaPreviewFragment.isVisible())
                || (mMediaPageFragment != null && mMediaPageFragment.isVisible())) {
            showMediaGridFragment();
            return super.onBackPressed();
        }
       return super.onBackPressed();
    }


    @Override
    public void onDestroy() {
        RxBus.getDefault().removeAllStickyEvents();
        RxBus.getDefault().clear();
        RxJob.getDefault().clearJob();
    }


    private StateListDrawable createDefaultOverButtonBgDrawable() {
        int dp12 = (int) ThemeUtils.applyDimensionDp(mActivity, 12.f);
        int dp8 = (int) ThemeUtils.applyDimensionDp(mActivity, 8.f);
        float dp4 = ThemeUtils.applyDimensionDp(mActivity, 4.f);
        float[] round = new float[]{dp4, dp4, dp4, dp4, dp4, dp4, dp4, dp4};
        ShapeDrawable pressedDrawable = new ShapeDrawable(new RoundRectShape(round, null, null));
        pressedDrawable.setPadding(dp12, dp8, dp12, dp8);
        int pressedColor = ThemeUtils.resolveColor(mActivity, R.attr.gallery_toolbar_over_button_pressed_color, R.color.gallery_default_toolbar_over_button_pressed_color);
        pressedDrawable.getPaint().setColor(pressedColor);

        int normalColor = ThemeUtils.resolveColor(mActivity, R.attr.gallery_toolbar_over_button_normal_color, R.color.gallery_default_toolbar_over_button_normal_color);
        ShapeDrawable normalDrawable = new ShapeDrawable(new RoundRectShape(round, null, null));
        normalDrawable.setPadding(dp12, dp8, dp12, dp8);
        normalDrawable.getPaint().setColor(normalColor);

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
        stateListDrawable.addState(new int[]{}, normalDrawable);

        return stateListDrawable;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Logger.i("onRequestPermissionsResult:requestCode=" + requestCode + " permissions=" + permissions[0]);
        switch (requestCode) {
            case REQUEST_STORAGE_READ_ACCESS_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    RxBus.getDefault().post(new RequestStorageReadAccessPermissionEvent(true));
                } else {
                    finish();
                }
                break;
            case REQUEST_STORAGE_WRITE_ACCESS_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    RxBus.getDefault().post(new RequestStorageReadAccessPermissionEvent(true));
                } else {
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCheckedList != null) {
            outState.putParcelableArrayList(EXTRA_CHECKED_LIST, mCheckedList);
        }
        outState.putInt(EXTRA_SELECTED_INDEX, mSelectedIndex);
        if (mPageMediaList != null) {
            outState.putParcelableArrayList(EXTRA_PAGE_MEDIA_LIST, mPageMediaList);
        }
        outState.putInt(EXTRA_PAGE_POSITION, mPagePosition);
        outState.putInt(EXTRA_PREVIEW_POSITION, mPreviewPosition);
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        List<MediaBean> list = savedInstanceState.getParcelableArrayList(EXTRA_CHECKED_LIST);
        if (list != null && list.size() > 0) {
            mCheckedList.clear();
            mCheckedList.addAll(list);
        }
        mPageMediaList = savedInstanceState.getParcelableArrayList(EXTRA_PAGE_MEDIA_LIST);
        mPagePosition = savedInstanceState.getInt(EXTRA_PAGE_POSITION);
        mPreviewPosition = savedInstanceState.getInt(EXTRA_PREVIEW_POSITION);
        mSelectedIndex = savedInstanceState.getInt(EXTRA_SELECTED_INDEX);
        if (!mConfiguration.isRadio()) {
            switch (mSelectedIndex) {
                case 1:
                    showMediaPageFragment(mPageMediaList, mPagePosition);
                    break;
                case 2:
                    showMediaPreviewFragment();
                    break;
            }
        }
    }

    public List<MediaBean> getCheckedList() {
        return mCheckedList;
    }
}
