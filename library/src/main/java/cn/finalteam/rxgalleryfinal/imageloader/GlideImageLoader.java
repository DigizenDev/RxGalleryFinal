package cn.finalteam.rxgalleryfinal.imageloader;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.ViewPropertyAnimation;

import java.io.File;

import cn.finalteam.rxgalleryfinal.ui.widget.FixImageView;

/**
 * Created by pengjianbo on 2016/8/13 0013.
 */
public class GlideImageLoader implements AbsImageLoader {

    @Override
    public void displayImage(Object context, String path, FixImageView imageView, Drawable defaultDrawable, Bitmap.Config config, boolean resize, int width, int height, int rotate) {
        Context ctx = (Context) context;
        BitmapRequestBuilder<File, Bitmap> builder = null;
        if (path != null) {
            builder = Glide.with(ctx)
                    .load(new File(path))
                    .asBitmap()
                    .placeholder(defaultDrawable);

        } else {
            builder = Glide.with(ctx)
                    .load(new File("/sdcard"))
                    .asBitmap()
                    .placeholder(defaultDrawable);
        }
        if (resize) {
            builder = builder.override(width, height);
        }
        builder.animate(new DrawableCrossFadeAnimator())
                //.transform(new RotateTransformation(ctx, rotate))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);
    }

    public static class DrawableCrossFadeAnimator implements ViewPropertyAnimation.Animator {

        private final int duration;
        private static final int DEFAULT_DURATION_MS = 300;



        public DrawableCrossFadeAnimator() {
            this(DEFAULT_DURATION_MS);
        }

        public DrawableCrossFadeAnimator(int duration) {
            this.duration=duration;
        }

        @Override
        public void animate(View view) {
            ObjectAnimator.ofFloat(view,"alpha",0f,1f).setDuration(duration).start();
        }
    }

}
