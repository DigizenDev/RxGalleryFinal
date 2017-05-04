package cn.finalteam.rxgalleryfinal.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * author  dengyuhan
 * created 2017/5/4 16:37
 */
public class VideoUtils {
    private static DateFormat durationFormat=new SimpleDateFormat("mm:ss");

    public static String formatDuration(long duration){
        return durationFormat.format(duration);
    }
}
