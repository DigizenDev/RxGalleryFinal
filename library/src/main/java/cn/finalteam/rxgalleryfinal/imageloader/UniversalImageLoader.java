package cn.finalteam.rxgalleryfinal.imageloader;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

/**
 * Created by pengjianbo on 2016/8/13 0013.
 */
public class UniversalImageLoader implements AbsImageLoader {

    @Override
    public void displayImage(Object context, String path, ImageView imageView, Drawable defaultDrawable, Bitmap.Config config, boolean resize, int width, int height, int rotate) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(false)
                .bitmapConfig(config)
                .showImageOnFail(defaultDrawable)
                .showImageOnLoading(defaultDrawable)
                .showImageForEmptyUri(defaultDrawable)
                .build();
        ImageSize imageSize = null;
        if(resize) {
            imageSize = new ImageSize(width, height);
        }
        ImageLoader.getInstance().displayImage("file://" + path, new ImageViewAware(imageView), options, imageSize, null, null);
    }
}
