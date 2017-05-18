package cn.finalteam.rxgalleryfinal;

import cn.finalteam.rxgalleryfinal.bean.MediaBean;

/**
 * author  dengyuhan
 * created 2017/5/5 14:14
 */
public interface OnCheckMediaListener{

    boolean onChecked(MediaBean media, boolean checked);

    /**
     *
     * @param media
     * @return true会finish,false什么也不做
     */
    boolean onFinish(MediaBean media);
}
