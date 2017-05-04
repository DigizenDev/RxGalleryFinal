package cn.finalteam.rxgalleryfinal.interactor.impl;

import android.content.Context;

import java.util.List;

import cn.finalteam.rxgalleryfinal.Configuration;
import cn.finalteam.rxgalleryfinal.Configuration.MediaType;
import cn.finalteam.rxgalleryfinal.bean.BucketBean;
import cn.finalteam.rxgalleryfinal.interactor.MediaBucketFactoryInteractor;
import cn.finalteam.rxgalleryfinal.utils.MediaUtils;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Desction:
 * Author:pengjianbo
 * Date:16/7/4 下午8:29
 */
public class MediaBucketFactoryInteractorImpl implements MediaBucketFactoryInteractor {

    private Context context;
    private Configuration.MediaType mediaType;
    private OnGenerateBucketListener onGenerateBucketListener;

    public MediaBucketFactoryInteractorImpl(Context context, Configuration.MediaType mediaType, OnGenerateBucketListener onGenerateBucketListener) {
        this.context = context;
        this.mediaType = mediaType;
        this.onGenerateBucketListener = onGenerateBucketListener;
    }

    @Override
    public void generateBuckets() {
        Observable.create(new Observable.OnSubscribe<List<BucketBean>>(){
            @Override
            public void call(Subscriber<? super List<BucketBean>> subscriber) {
                List<BucketBean> bucketBeanList = null;
                if (MediaType.IMAGE == mediaType) {
                    bucketBeanList = MediaUtils.getAllBucketByImage(context);
                } else if (MediaType.VIDEO == mediaType) {
                    bucketBeanList = MediaUtils.getAllBucketByVideo(context);
                } else {
                    bucketBeanList = MediaUtils.getAllBucket(context, mediaType);
                }
                subscriber.onNext(bucketBeanList);
                subscriber.onCompleted();
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<List<BucketBean>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                onGenerateBucketListener.onFinished(null);
            }

            @Override
            public void onNext(List<BucketBean> bucketBeanList) {
                onGenerateBucketListener.onFinished(bucketBeanList);
            }
        });
    }
}
