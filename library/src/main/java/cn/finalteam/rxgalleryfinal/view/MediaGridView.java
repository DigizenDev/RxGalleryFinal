package cn.finalteam.rxgalleryfinal.view;

import java.util.List;

import cn.finalteam.rxgalleryfinal.bean.MediaBean;

/**
 * Desction:
 * Author:pengjianbo
 * Date:16/5/14 上午11:00
 */
public interface MediaGridView {
    //显示loading view
    void showProgress();
    //显示empty
    void showEmptyView();
    void onRequestMediaCallback(List<MediaBean> list);
}