package cn.finalteam.rxgalleryfinal.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.dyhdyh.rxgalleryfinal.sample.CustomActivity;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.List;

import cn.finalteam.rxgalleryfinal.OnCheckMediaListener;
import cn.finalteam.rxgalleryfinal.RxGalleryFinal;
import cn.finalteam.rxgalleryfinal.RxGalleryFinalApi;
import cn.finalteam.rxgalleryfinal.bean.MediaBean;
import cn.finalteam.rxgalleryfinal.imageloader.ImageLoaderType;
import cn.finalteam.rxgalleryfinal.rxbus.RxBusResultSubscriber;
import cn.finalteam.rxgalleryfinal.rxbus.event.BaseResultEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.ImageMultipleResultEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.ImageRadioResultEvent;
import cn.finalteam.rxgalleryfinal.ui.RxGalleryListener;
import cn.finalteam.rxgalleryfinal.ui.base.IMultiImageCheckedListener;
import cn.finalteam.rxgalleryfinal.utils.Logger;
import cn.finalteam.rxgalleryfinal.utils.MediaScanner;
import cn.finalteam.rxgalleryfinal.utils.MediaType;
import cn.finalteam.rxgalleryfinal.utils.ModelUtils;

/**
 * 示例
 *
 * @author KARL-dujinyang
 */
public class MainActivity extends AppCompatActivity {

    RadioButton mRbRadioIMG, mRbMutiIMG, mRbOpenC, mRbRadioVD, mRbMutiVD, mRbCropZD, mRbCropZVD;
    Button mBtnOpenMultiActivity, mBtnOpenSetActivity, mBtnOpenSetDir, mBtnOpenDefRadio, mBtnOpenDefMulti, mBtnOpenIMG, mBtnOpenVD, mBtnOpenCrop;
    boolean mFlagOpenCrop = false; //是否拍照并裁剪

    //ID
    private void initView() {
        mBtnOpenSetActivity = (Button) findViewById(R.id.btn_open_set_activity);
        mBtnOpenMultiActivity = (Button) findViewById(R.id.btn_open_multi_activity);
        mBtnOpenSetDir = (Button) findViewById(R.id.btn_open_set_path);
        mBtnOpenIMG = (Button) findViewById(R.id.btn_open_img);
        mBtnOpenVD = (Button) findViewById(R.id.btn_open_vd);
        mBtnOpenDefRadio = (Button) findViewById(R.id.btn_open_def_radio);
        mBtnOpenDefMulti = (Button) findViewById(R.id.btn_open_def_multi);
        mBtnOpenCrop = (Button) findViewById(R.id.btn_open_crop);
        mRbRadioIMG = (RadioButton) findViewById(R.id.rb_radio_img);
        mRbMutiIMG = (RadioButton) findViewById(R.id.rb_muti_img);
        mRbRadioVD = (RadioButton) findViewById(R.id.rb_radio_vd);
        mRbMutiVD = (RadioButton) findViewById(R.id.rb_muti_vd);
        mRbOpenC = (RadioButton) findViewById(R.id.rb_openC);
        mRbCropZD = (RadioButton) findViewById(R.id.rb_radio_crop_z);
        mRbCropZVD = (RadioButton) findViewById(R.id.rb_radio_crop_vz);
    }

    //ImageLoaderConfiguration
    private void initImageLoader() {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(this);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        ImageLoader.getInstance().init(config.build());
    }

    //Fresco
    private void initFresco() {
        Fresco.initialize(this);
    }

    //*************************************************************************//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //手动打开日志。
        ModelUtils.setDebugModel(true);
        initView();
        initImageLoader();
        initFresco();

        //自定义使用
        onClickZDListener();
        //调用图片选择器Api
        onClickSelImgListener();
        //调用视频选择器Api
        onClickSelVDListener();
        //调用裁剪
        onClickImgCropListener();
        //多选事件的回调
        getMultiListener();
    }

    //***********************************************************
    //**  以下为调用方法 onClick *
    //***********************************************************

    /**
     * 调用裁剪
     */
    private void onClickImgCropListener() {
        mBtnOpenCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRbCropZD.isChecked()) {
                    //直接裁剪
                    String inputImg = "";
                    Toast.makeText(MainActivity.this, "没有图片演示，请选择‘拍照裁剪’功能", Toast.LENGTH_SHORT).show();
                    //  RxGalleryFinalApi.cropScannerForResult(MainActivity.this, RxGalleryFinalApi.getModelPath(), inputImg);//调用裁剪.RxGalleryFinalApi.getModelPath()为模拟的输出路径
                } else {
                    //拍照并裁剪
                    mFlagOpenCrop = true;
                    //然后直接打开相机 - onActivityResult 回调里面处理裁剪
                    RxGalleryFinalApi.openZKCamera(MainActivity.this);
                }
            }
        });
    }

    /**
     * 调用视频选择器Api
     */
    private void onClickSelVDListener() {
        mBtnOpenVD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRbRadioVD.isChecked()) {
                    RxGalleryFinalApi.getInstance(MainActivity.this)
                            .setType(RxGalleryFinalApi.SelectRXType.TYPE_VIDEO, RxGalleryFinalApi.SelectRXType.TYPE_SELECT_RADIO)
                            .setVDRadioResultEvent(new RxBusResultSubscriber<ImageRadioResultEvent>() {
                                @Override
                                protected void onEvent(ImageRadioResultEvent imageRadioResultEvent) throws Exception {
                                    //回调还没
                                }
                            })
                            .open();


                } else if (mRbMutiVD.isChecked()) {
                    //多选图片的方式
                    //1.使用默认的参数
              /*  RxGalleryFinalApi.getInstance(this).setVDMultipleResultEvent(new RxBusResultSubscriber<ImageMultipleResultEvent>() {
                    @Override
                    protected void onEvent(ImageMultipleResultEvent imageMultipleResultEvent) throws Exception {
                        Logger.i("多选视频的回调");
                    }
                }).open();
*/
                    //2.使用自定义的参数
                    RxGalleryFinalApi.getInstance(MainActivity.this)
                            .setType(RxGalleryFinalApi.SelectRXType.TYPE_VIDEO, RxGalleryFinalApi.SelectRXType.TYPE_SELECT_MULTI)
                            .setVDMultipleResultEvent(new RxBusResultSubscriber<ImageMultipleResultEvent>() {
                                @Override
                                protected void onEvent(ImageMultipleResultEvent imageMultipleResultEvent) throws Exception {
                                    Logger.i("多选视频的回调");
                                }
                            }).open();
                    ;

                    //3.直接打开
            /*    RxGalleryFinalApi.openMultiSelectVD(this, new RxBusResultSubscriber() {
                    @Override
                    protected void onEvent(Object o) throws Exception {
                        Logger.i("多选视频的回调");
                    }
                });*/
                }
            }
        });
    }

    /**
     * 调用图片选择器Api
     */
    private void onClickSelImgListener() {
        mBtnOpenIMG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRbRadioIMG.isChecked()) {
                    //以下方式 -- 可选：
                    //1.打开单选图片，默认参数
                    RxGalleryFinalApi.getInstance(MainActivity.this).setImageRadioResultEvent(new RxBusResultSubscriber<ImageRadioResultEvent>() {
                        @Override
                        protected void onEvent(ImageRadioResultEvent imageRadioResultEvent) throws Exception {
                            Logger.i("单选图片的回调");
                        }
                    }).open();

                    //2.设置自定义的参数
                    RxGalleryFinalApi.getInstance(MainActivity.this).setType(RxGalleryFinalApi.SelectRXType.TYPE_IMAGE, RxGalleryFinalApi.SelectRXType.TYPE_SELECT_RADIO)
                            .setImageRadioResultEvent(new RxBusResultSubscriber<ImageRadioResultEvent>() {
                                @Override
                                protected void onEvent(ImageRadioResultEvent imageRadioResultEvent) throws Exception {
                                    Logger.i("单选图片的回调");
                                }
                            }).open();

                    //3.打开单选图片
                    RxGalleryFinalApi.openRadioSelectImage(MainActivity.this, new RxBusResultSubscriber() {
                        @Override
                        protected void onEvent(Object o) throws Exception {

                        }
                    });

                } else if (mRbMutiIMG.isChecked()) {
                    //多选图片的方式
                    //1.使用默认的参数
                    RxGalleryFinalApi.getInstance(MainActivity.this).setImageMultipleResultEvent(new RxBusResultSubscriber<ImageMultipleResultEvent>() {
                        @Override
                        protected void onEvent(ImageMultipleResultEvent imageMultipleResultEvent) throws Exception {
                            Logger.i("多选图片的回调");
                        }
                    }).open();

                    //2.使用自定义的参数
                    RxGalleryFinalApi.getInstance(MainActivity.this)
                            .setType(RxGalleryFinalApi.SelectRXType.TYPE_IMAGE, RxGalleryFinalApi.SelectRXType.TYPE_SELECT_MULTI)
                            .setImageMultipleResultEvent(new RxBusResultSubscriber<ImageMultipleResultEvent>() {
                                @Override
                                protected void onEvent(ImageMultipleResultEvent imageMultipleResultEvent) throws Exception {
                                    Logger.i("多选图片的回调");
                                }
                            }).open();
                    ;

                    //3.直接打开
                    RxGalleryFinalApi.openMultiSelectImage(MainActivity.this, new RxBusResultSubscriber() {
                        @Override
                        protected void onEvent(Object o) throws Exception {
                            Logger.i("多选图片的回调");
                        }
                    });
                } else {
                    mFlagOpenCrop = false;
                    //直接打开相机
                    RxGalleryFinalApi.openZKCamera(MainActivity.this);
                }
            }
        });
    }


    /**
     * 如果不使用api定义好的，则自己定义使用
     * ImageLoaderType :自己选择使用
     */
    private void onClickZDListener() {
        mBtnOpenMultiActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxGalleryFinal
                        .with(MainActivity.this)
                        .setActivityClassName(CustomActivity.class)
                        .all()
                        .imageLoader(ImageLoaderType.GLIDE)
                        .hideCamera()
                        .multiple()
                        .maxSize(5)//多选上限
                        .setOnCheckMediaListener(new OnCheckMediaListener() {
                            private int imageCount;
                            private int videoCount;

                            @Override
                            public boolean onChecked(MediaBean media, boolean checked) {
                                Log.d("------------->", checked + " " + imageCount);
                                //如果是选中
                                if (MediaType.ofImage().contains(MediaType.fromValue(media.getMimeType()))) {
                                    //如果已经选了视频了
                                    if (videoCount > 0) {
                                        Toast.makeText(MainActivity.this, "不能同时选择照片和视频", Toast.LENGTH_SHORT).show();
                                        return false;
                                    }
                                    //如果选的是图片
                                    if (checked) {
                                        imageCount++;
                                    } else {
                                        imageCount--;
                                    }
                                    return true;
                                } else if (MediaType.ofCommonVideo().contains(MediaType.fromValue(media.getMimeType()))) {
                                    //如果是视频
                                    //如果已经选了图片了
                                    if (imageCount > 0) {
                                        Toast.makeText(MainActivity.this, "不能同时选择照片和视频", Toast.LENGTH_SHORT).show();
                                        return false;
                                    }
                                    if (checked) {
                                        //视频数量限制
                                        if (videoCount >= 1) {
                                            Toast.makeText(MainActivity.this, "最多只能选择1个视频", Toast.LENGTH_SHORT).show();
                                            return false;
                                        }
                                        videoCount++;
                                    } else {
                                        videoCount--;
                                    }
                                    return true;
                                }
                                return true;
                            }

                            @Override
                            public boolean onFinish(MediaBean media) {
                                return false;
                            }
                        })
                        .subscribe(new RxBusResultSubscriber<BaseResultEvent>() {
                            @Override
                            protected void onEvent(BaseResultEvent event) throws Exception {
                                if (event instanceof ImageMultipleResultEvent) {
                                    List<MediaBean> result = ((ImageMultipleResultEvent) event).getResult();

                                    Toast.makeText(getApplicationContext(), result.size() + "个", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).openGallery();
            }
        });

        mBtnOpenSetActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxGalleryFinal
                        .with(MainActivity.this)
                        .setActivityClassName(CustomActivity.class)
                        .all()
                        .radio()
                        .imageLoader(ImageLoaderType.GLIDE)
                        .hideCamera()
                        .subscribe(new RxBusResultSubscriber<ImageRadioResultEvent>() {
                            @Override
                            protected void onEvent(ImageRadioResultEvent imageRadioResultEvent) throws Exception {
                                Toast.makeText(getBaseContext(), imageRadioResultEvent.getResult().getOriginalPath(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setOnCheckMediaListener(new OnCheckMediaListener() {
                            @Override
                            public boolean onChecked(MediaBean media, boolean checked) {
                                if (media.getMimeType().equals(MediaType.GIF.toString())) {
                                    Toast.makeText(MainActivity.this, "不能选择gif", Toast.LENGTH_SHORT).show();
                                    return false;
                                } else {
                                    return true;
                                }
                            }

                            @Override
                            public boolean onFinish(MediaBean media) {
                                return false;
                            }

                        })
                        .openGallery();
            }
        });

        mBtnOpenSetDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxGalleryFinalApi.setImgSaveRxSDCard("dujinyang");//图片自动会存储到下面，裁剪会自动生成路径；也可以手动设置裁剪的路径；
                RxGalleryFinalApi.setImgSaveRxCropSDCard("dujinyang/crop");
            }
        });

        mBtnOpenDefRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //单选图片
                RxGalleryFinal
                        .with(MainActivity.this)
                        .image()
                        .radio()
                        .crop()
                        .imageLoader(ImageLoaderType.FRESCO)
                        .subscribe(new RxBusResultSubscriber<ImageRadioResultEvent>() {
                            @Override
                            protected void onEvent(ImageRadioResultEvent imageRadioResultEvent) throws Exception {
                                Toast.makeText(getBaseContext(), imageRadioResultEvent.getResult().getOriginalPath(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .openGallery();
            }
        });

        mBtnOpenDefMulti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //多选图片
                RxGalleryFinal
                        .with(MainActivity.this)
                        .image()
                        .multiple()
                        .maxSize(8)
                        .imageLoader(ImageLoaderType.UNIVERSAL)
                        .subscribe(new RxBusResultSubscriber<ImageMultipleResultEvent>() {

                            @Override
                            protected void onEvent(ImageMultipleResultEvent imageMultipleResultEvent) throws Exception {
                                Toast.makeText(getBaseContext(), "已选择" + imageMultipleResultEvent.getResult().size() + "张图片", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCompleted() {
                                super.onCompleted();
                                Toast.makeText(getBaseContext(), "OVER", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .openGallery();

            }
        });
    }


    /**
     * 多选事件都会在这里执行
     */
    public void getMultiListener() {
        //得到多选的事件
        RxGalleryListener.getInstance().setMultiImageCheckedListener(new IMultiImageCheckedListener() {
            @Override
            public void selectedImg(Object t, boolean isChecked) {
                //这个主要点击或者按到就会触发，所以不建议在这里进行Toast
            }

            @Override
            public void selectedImgMax(Object t, boolean isChecked, int maxSize) {
                Toast.makeText(getBaseContext(), "你最多只能选择" + maxSize + "张图片", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.i("onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode + " data:" + data);
        if (requestCode == RxGalleryFinalApi.TAKE_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Logger.i("拍照OK，图片路径:" + RxGalleryFinalApi.fileImagePath.getPath().toString());
            //刷新相册数据库
            RxGalleryFinalApi.openZKCameraForResult(MainActivity.this, new MediaScanner.ScanCallback() {
                @Override
                public void onScanCompleted(String[] strings) {
                    Logger.i(String.format("拍照成功,图片存储路径:%s", strings[0]));
                    if (mFlagOpenCrop) {
                        Logger.d("演示拍照后进行图片裁剪，根据实际开发需求可去掉上面的判断");
                        RxGalleryFinalApi.cropScannerForResult(MainActivity.this, RxGalleryFinalApi.getModelPath(), strings[0]);//调用裁剪.RxGalleryFinalApi.getModelPath()为默认的输出路径
                    }
                }
            });
        } else {
            Logger.i("失敗");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //选择调用：裁剪图片的回调
        RxGalleryFinalApi.cropActivityForResult(this, new MediaScanner.ScanCallback() {
            @Override
            public void onScanCompleted(String[] images) {
                Logger.i(String.format("裁剪图片成功,图片裁剪后存储路径:%s", images[0]));
            }
        });
    }


    @Override
    protected void onRestart() {
        super.onRestart();
    }


}