package com.example.lxc.android_video.ijkplayer.media;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.lxc.android_video.R;
import com.example.lxc.android_video.help.AssistUtil;
import com.example.lxc.android_video.help.NetWorkUtils;
import com.example.lxc.android_video.help.ShareDialog;
import com.example.lxc.android_video.help.StorageUtil;
import com.example.lxc.android_video.help.StringHelper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.pragma.DebugLog;

import static android.view.View.VISIBLE;

/**
 * Created by yilv on 2018/3/29.
 */

public class PlayerManager implements View.OnClickListener{
    /**
     * 可能会剪裁,保持原视频的大小，显示在中心,当原视频的大小超过view的大小超过部分裁剪处理
     */
    public static final String SCALETYPE_FITPARENT="fitParent";
    /**
     * 可能会剪裁,等比例放大视频，直到填满View为止,超过View的部分作裁剪处理
     */
    public static final String SCALETYPE_FILLPARENT="fillParent";
    /**
     * 将视频的内容完整居中显示，如果视频大于view,则按比例缩视频直到完全显示在view中
     */
    public static final String SCALETYPE_WRAPCONTENT="wrapContent";
    /**
     * 不剪裁,非等比例拉伸画面填满整个View
     */
    public static final String SCALETYPE_FITXY="fitXY";
    /**
     * 不剪裁,非等比例拉伸画面到16:9,并完全显示在View中
     */
    public static final String SCALETYPE_16_9="16:9";
    /**
     * 不剪裁,非等比例拉伸画面到4:3,并完全显示在View中
     */
    public static final String SCALETYPE_4_3="4:3";

    /**
     * 状态常量
     */
    private final int STATUS_ERROR=-1;
    private final int STATUS_IDLE=0;
    private final int STATUS_LOADING=1;
    private final int STATUS_PLAYING=2;
    private final int STATUS_PAUSE=3;
    private final int STATUS_COMPLETED=4;

    private final AppCompatActivity activity;
    private final IjkVideoView videoView;
    private final AudioManager audioManager;
    public GestureDetector gestureDetector;

    private boolean playerSupport;
    private boolean isLive = false;//是否为直播
    private boolean fullScreenOnly = false;
    private boolean portrait;
    //手机最大音量
    private final int mMaxVolume;
    private int screenWidthPixels;
    private int status=STATUS_IDLE;
    private long pauseTime;
    private String url;
    //是否准备好播放
    private boolean mIsReady = false;
    // 是否正在拖拽进度条
    private boolean mIsSeeking = false;
    //当前亮度
    private float brightness=-1;
    //当前声音
    private int volume=-1;
    //快进快退目标位置
    private long newPosition = -1;
    //当前播放位置
    private int currentPosition;
    // 默认出错尝试时间
    private long defaultRetryTime=5000;
    // 默认隐藏控制栏时间
    private long DEFAULT_HIDE_TIMEOUT=5000;
    //进度条最大值
    private int MAX_VIDEO_SEEK=1000;
    // 更新进度消息
    private static final int MSG_UPDATE_SEEK = 10086;

    // 整个视频框架的布局
    private FrameLayout mFlVideoBox;
    //当前播放时间显示
    private TextView mTvCurrentTime;
    //进度控制
    private SeekBar mSeekBar;
    //总共播放时间显示
    private TextView mTvTotalTime;
    //全屏按钮
    private ImageView mIvFullScreen;
    //屏幕中央重播图片
    private ImageView mIvPlayStatus;
    //进度条上的播放状态
    private ImageView mPlayStatus;
    //正在缓冲
    private LinearLayout mLlLoading;
    //缓冲速度
    private TextView load_speed;
    //横屏控制栏界面,显示和隐藏下面的进度条.按钮
    private LinearLayout mbottomBarControl;
    //横屏控制栏界面,显示和隐藏上面的布局
    private LinearLayout mtopBarControl;
    //电量
    private ProgressBar mPbBattery;
    //滚动标题
    private TextView mTvTitle;
    //手机时间
    private TextView mTvSystemTime;
    //截图
    private ImageView mIvScreenshot;
    // 视频宽高比选项
    private TextView mTvSettings;
    private RadioGroup mAspectRatioOptions;
    // 选项列表高度
    private int mAspectOptionsHeight;
    //横屏模式的后退键
    private ImageView mIvBack;
    // 竖屏模式的后退键
    private ImageView mWindowBack;
    // 竖屏模式的TopBar
    private FrameLayout mWindowTopBar;
    //快进快退显示
    private final TextView mTvProgress;
    //进度变化的Handler
    private PlayHandler mPlayHandler = new PlayHandler();
    // 屏幕旋转角度监听
    private OrientationEventListener mOrientationListener;
    //网络是否可用
    private boolean mIsNetConnected;
    private NetBroadcastReceiver mNetReceiver;
    private BatteryBroadcastReceiver mBatteryReceiver;

    // 截图保存路径
    private File mSaveDir;
    // 截图分享弹框
    private ShareDialog mShareDialog;
    // 对话框点击监听，内部和外部
    private ShareDialog.OnDialogClickListener mDialogClickListener;
    private ShareDialog.OnDialogClickListener mInsideDialogClickListener = new ShareDialog.OnDialogClickListener() {
        @Override
        public void onShare(Bitmap bitmap, Uri uri) {
            if (mDialogClickListener != null) {
                mDialogClickListener.onShare(bitmap, Uri.parse(url));
            }
            File file = new File(mSaveDir, System.currentTimeMillis() + ".jpg");
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
                StringHelper.showShortMessage(activity, "保存成功，路径为:" + file.getAbsolutePath());
            } catch (IOException e) {
                StringHelper.showShortMessage(activity, "保存本地失败");
            }

        }
    };
    private ShareDialog.OnDialogDismissListener mDialogDismissListener = new ShareDialog.OnDialogDismissListener() {
        @Override
        public void onDismiss() {
            start();
        }
    };


    private OrientationEventListener orientationEventListener;
    private PlayerStateListener playerStateListener;

    public void setPlayerStateListener(PlayerStateListener playerStateListener) {
        this.playerStateListener = playerStateListener;
    }

    private OnErrorListener onErrorListener=new OnErrorListener() {
        @Override
        public void onError(int what, int extra) {
        }
    };

    private OnCompleteListener onCompleteListener=new OnCompleteListener() {
        @Override
        public void onComplete() {
        }
    };

    private OnInfoListener onInfoListener=new OnInfoListener(){
        @Override
        public void onInfo(int what, int extra) {

        }
    };
    private OnControlPanelVisibilityChangeListener onControlPanelVisibilityChangeListener=new OnControlPanelVisibilityChangeListener() {
        @Override
        public void change(boolean isShowing) {
        }
    };

    /**
     * try to play when error(only for live video)
     * @param defaultRetryTime millisecond,0 will stop retry,default is 5000 millisecond
     */
    /**
     *出错时尝试播放（仅适用于实况视频）
     *@param defaultRetryTime，0将停止重试，默认值为5000毫秒
     */
    public void setDefaultRetryTime(long defaultRetryTime) {
        this.defaultRetryTime = defaultRetryTime;
    }

    public PlayerManager(final AppCompatActivity activity) {
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
            playerSupport=true;
        } catch (Throwable e) {
            Log.e("GiraffePlayer", "loadLibraries error", e);
        }
        this.activity=activity;
        screenWidthPixels = activity.getResources().getDisplayMetrics().widthPixels;

        mFlVideoBox = (FrameLayout) activity.findViewById(R.id.fl_video_box);//视频框架布局
        videoView = (IjkVideoView) activity.findViewById(R.id.video_play_IjkVideoView);
        mTvCurrentTime = (TextView) activity.findViewById(R.id.tv_current_time);//当前时间
        mSeekBar = (SeekBar) activity.findViewById(R.id.seekbar);//进度条
        mTvTotalTime = (TextView) activity.findViewById(R.id.tv_total_time);//总时间
        mIvFullScreen = (ImageView) activity.findViewById(R.id.iv_play_screen);//全屏按钮
        mIvPlayStatus = (ImageView) activity.findViewById(R.id.iv_play_status);//屏幕中央重播图片
        mPlayStatus = (ImageView) activity.findViewById(R.id.play_status);//进度条的暂停播放图片
        mLlLoading = (LinearLayout) activity.findViewById(R.id.ll_loading);//正在缓冲
        load_speed = (TextView) activity.findViewById(R.id.load_speed);//缓冲速度
        mbottomBarControl = (LinearLayout) activity.findViewById(R.id.fullscreen_bottom_bar);//横屏控制界面,控制下面的所有显示
        mtopBarControl = (LinearLayout) activity.findViewById(R.id.fullscreen_top_bar);//横屏控制界面,控制上面的所有显示
        mWindowTopBar = (FrameLayout) activity.findViewById(R.id.window_top_bar); //竖屏模式,控制上面的显示
        mWindowBack = (ImageView) activity.findViewById(R.id.iv_back_window);//竖屏的返回键
        mIvBack = (ImageView) activity.findViewById(R.id.iv_back);//横屏的返回键
        mTvProgress = (TextView) activity.findViewById(R.id.tv_progress);//滑动快进显示

        mTvTitle = (TextView) activity.findViewById(R.id.tv_title); //滚动标题
        mPbBattery = (ProgressBar) activity.findViewById(R.id.pb_battery);//电量进度
        mTvSystemTime = (TextView) activity.findViewById(R.id.tv_system_time);//显示手机时间
        mIvScreenshot = (ImageView) activity.findViewById(R.id.iv_screenshot);//截图
        mTvSettings = (TextView) activity.findViewById(R.id.tv_settings);//视频比例
        mAspectRatioOptions = (RadioGroup) activity.findViewById(R.id.aspect_ratio_group);
        mAspectOptionsHeight = activity.getResources().getDimensionPixelSize(R.dimen.aspect_btn_size) * 5;
        mAspectRatioOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.aspect_fit_parent) {
                    setScaleType(SCALETYPE_FITPARENT);
                } else if (checkedId == R.id.aspect_fill_parent) {
                    setScaleType(SCALETYPE_FILLPARENT);
                } else if (checkedId == R.id.aspect_fitxy) {
                    setScaleType(SCALETYPE_FITXY);
                } else if (checkedId == R.id.aspect_16_and_9) {
                    setScaleType(SCALETYPE_16_9);
                } else if (checkedId == R.id.aspect_4_and_3) {
                    setScaleType(SCALETYPE_4_3);
                }
                doClipViewHeight(mAspectRatioOptions, mAspectOptionsHeight, 0, 150);
            }
        });
        /*---------------------------------------点击事件----------------------------------------*/
        initReceiver();

        mSeekBar.setMax(MAX_VIDEO_SEEK);
        mSeekBar.setOnSeekBarChangeListener(mSeekListener);
        mIvFullScreen.setOnClickListener(this);
        mIvPlayStatus.setOnClickListener(this);
        mPlayStatus.setOnClickListener(this);
        mWindowBack.setOnClickListener(this);
        mIvBack.setOnClickListener(this);
        mIvScreenshot.setOnClickListener(this);
        mTvSettings.setOnClickListener(this);

        mTvSystemTime.setText(StringHelper.getCurFormatTime());


        //播放完成的监听
        videoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                Log.e("lxc","IMediaPlayer6666");
                statusChange(STATUS_COMPLETED);
                onCompleteListener.onComplete();
            }
        });
        //播放开始的监听
        videoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                Log.e("lxc","IMediaPlayer1111");
                mIsReady = true;
                mPlayStatus.setEnabled(false);
                //检测播放进度和缓冲进度
                //mPlayHandler.sendEmptyMessage(Normal_Play);
            }
        });
        //播放错误的监听
        videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                statusChange(STATUS_ERROR);
                Log.e("lxc","IMediaPlayerERROR");
                onErrorListener.onError(what,extra);
                return true;
            }
        });

        //缓冲监听
        videoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START://开始缓冲
                        Log.e("lxc","IMediaPlayer2222");
                        statusChange(STATUS_LOADING);
                        mLlLoading.setVisibility(VISIBLE);
                        mIvPlayStatus.setVisibility(View.GONE);
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END://缓冲完成
                        Log.e("lxc","IMediaPlayer3333");
                        statusChange(STATUS_PLAYING);
                        mLlLoading.setVisibility(View.GONE);
                        Log.e("lxc","isplaying()==="+isPlaying());
                        if(isPlaying()){
                            start();
                        }else {
                            pause();
                        }

                        //mPlayHandler.sendEmptyMessage(Pause_Play);
                        break;
                    case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH://媒体信息网络带宽
                        Log.e("lxc","IMediaPlayer4444");
                        //显示下载速度
                        load_speed.setText("加载"+extra);
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START://媒体信息视频开始呈现
                        Log.e("lxc","IMediaPlayer5555");
                        Log.e("lxc","getDuration()=="+getDuration());
                        statusChange(STATUS_PLAYING);
                        mLlLoading.setVisibility(View.GONE);
                        mTvTotalTime.setText(generateTime((long) getDuration()));
                        //更新进度
                        start();
                        mPlayStatus.setEnabled(true);
                        break;
                }
                onInfoListener.onInfo(what,extra);
                return false;
            }
        });

        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //手势监听
        gestureDetector = new GestureDetector(activity, new PlayerGestureListener());
        //屏幕触摸监听
        mFlVideoBox.setClickable(true);
        mFlVideoBox.setOnTouchListener(mPlayerTouchListener);
        // 屏幕翻转控制,重写重力旋转
        mOrientationListener = new OrientationEventListener(activity) {
            @Override
            public void onOrientationChanged(int orientation) {
                Log.e("lxc","mOrientationListener重力旋转监听");
                handleOrientation(orientation);
            }
        };
        if (true) {
            // 禁止翻转
            mOrientationListener.disable();
        }


        if (fullScreenOnly) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        portrait=getScreenOrientation()== ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        if (!playerSupport) {
            DebugLog.e("","播放器不支持此设备");
        }
    }

    private void statusChange(int newStatus) {
        status = newStatus;
        if (!isLive && newStatus==STATUS_COMPLETED) {
            Log.e("lxc","videoView.getCurrentPosition()=="+videoView.getCurrentPosition()+",videoView.getDuration()==="+videoView.getDuration());
            if (videoView.getDuration() == -1 || videoView.getCurrentPosition() + 1000 < videoView.getDuration()) {//允许误差一秒
                mLlLoading.setVisibility(VISIBLE);
                StringHelper.showShortMessage(activity,"网络异常");

            }else {
                pause();
                mIvPlayStatus.setVisibility(VISIBLE);
            }

            DebugLog.d("","statusChange STATUS_COMPLETED...");
            if (playerStateListener != null){
                playerStateListener.onComplete();
            }
        }else if (newStatus == STATUS_ERROR) {
            DebugLog.d("","statusChange STATUS_ERROR...");
            if (playerStateListener != null){
                playerStateListener.onError();
            }
        } else if(newStatus==STATUS_LOADING){
//            $.id(R.id.app_video_loading).visible();
            if (playerStateListener != null){
                playerStateListener.onLoading();
            }
            DebugLog.d("","statusChange STATUS_LOADING...");
        } else if (newStatus == STATUS_PLAYING) {
            DebugLog.d("","statusChange STATUS_PLAYING...");
            if (playerStateListener != null){
                playerStateListener.onPlay();
            }
        }
    }


    /**
     * 生命周期中的onPause
     */
    public void onPause() {
        Log.e("lxc","onPause()pause()");
        pauseTime= System.currentTimeMillis();
        if (status==STATUS_PLAYING) {
            videoView.pause();
            if (!isLive) {
                android.util.Log.e("lxc","videoView.getCurrentPosition()==="+generateTime(videoView.getCurrentPosition()));
                currentPosition = videoView.getCurrentPosition();
            }
        }
    }

    public void onResume() {
        pauseTime=0;
        if (status==STATUS_PLAYING) {
            if (isLive) {
                videoView.seekTo(0);
            } else {
                if (currentPosition>0) {
                    videoView.seekTo(currentPosition);
                }
            }

            /**
             * 按power键黑屏亮屏会重新出来虚拟键
             */
            /**if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                AssistUtil.hideBottomUIMenu(activity);
            }**/

            if((mPlayStatus.getDrawable().getCurrent().getConstantState()).equals(ContextCompat.getDrawable(activity,R.mipmap.ic_video_pause).getConstantState())) {
                Log.e("lxc","onResume()start()");
                start();
            }

        }
    }

    public void onDestroy() {
        Log.e("lxc","onDestroy");
        if(orientationEventListener != null) {
            orientationEventListener.disable();
        }
        videoView.stopPlayback();
        activity.unregisterReceiver(mNetReceiver);
        activity.unregisterReceiver(mBatteryReceiver);
    }

    public void play(String url) {
        this.url = url;
        if (playerSupport) {
            videoView.setVideoPath(url);
            videoView.start();
        }
    }

    /**
     * 格式化时间
     * @param time
     * @return
     */
    private String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = totalSeconds / 60;
//        int minutes = (totalSeconds / 60) % 60;
//        int hours = totalSeconds / 3600;
        return minutes > 99 ? String.format("%d:%02d", minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * 获取当前屏幕是横屏还是竖屏
     * @return
     */
    private int getScreenOrientation() {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }
        return orientation;
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (volume == -1) {
            volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volume < 0)
                volume = 0;
        }
        int index = (int) (percent * mMaxVolume) + volume;
        if (index > mMaxVolume) {
            index = mMaxVolume;
        } else if (index < 0){
            index = 0;
        }
        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        // 变更进度条
        int i = (int) (index * 1.0 / mMaxVolume * 100);
        String s = i + "%";
        if (i == 0) {
            s = "off";
        }
        setVolumeInfo(s);//后面通过 endGesture()方法隐藏布局
    }

    /**
     * 快进快退
     * @param percent
     */
    private void onProgressSlide(float percent) {
        //当前时间
        int position = videoView.getCurrentPosition();
        //总时间
        long duration = videoView.getDuration();
        // 单次拖拽最大时间差为100秒或播放时长的1/2
        long deltaMax = Math.min(100 * 1000, duration/2);
        // 计算滑动时间
        long delta = (long) (deltaMax * percent);
        //目标位置
        newPosition = delta + position;
        if (newPosition > duration) {
            newPosition = duration;
        } else if (newPosition <= 0) {
            newPosition=0;
        }
        int deltaTime = (int) ((newPosition - position) / 1000);
        String desc;
        // 对比当前位置来显示快进或后退
        if (newPosition > position) {
            desc = generateTime(newPosition) + "/" + generateTime(duration) + "\n" + "+" + deltaTime + "秒";
        } else {
            desc = generateTime(newPosition) + "/" + generateTime(duration) + "\n" + deltaTime + "秒";
        }
        setProgressInfo(desc);//newPosition!=-1,所以后面通过 endGesture()方法改变进度条
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (brightness < 0) {
            brightness = activity.getWindow().getAttributes().screenBrightness;
            if (brightness < 0.0f){
                brightness = 0.5f;
            }else if (brightness < 0.01f){
                brightness = 0.01f;
            }
        }
        DebugLog.d("","brightness:"+brightness+",percent:"+ percent);
        WindowManager.LayoutParams lpa = activity.getWindow().getAttributes();
        lpa.screenBrightness = brightness + percent;
        if (lpa.screenBrightness > 1.0f){
            lpa.screenBrightness = 1.0f;
        }else if (lpa.screenBrightness < 0.01f){
            lpa.screenBrightness = 0.01f;
        }
        setBrightnessInfo(lpa.screenBrightness);
        activity.getWindow().setAttributes(lpa);//后面通过 endGesture()方法隐藏布局
    }

    /**
     * 设置亮度控制显示
     *
     * @param brightness
     */
    private void setBrightnessInfo(float brightness) {
        mTvProgress.setVisibility(VISIBLE);
        setTvProgressImg(R.mipmap.ic_brightness);
        mTvProgress.setText(Math.ceil(brightness * 100) + "%");
    }

    /**
     * 设置声音控制显示
     */
    private void setVolumeInfo(String i){
        mTvProgress.setVisibility(VISIBLE);
        setTvProgressImg(R.mipmap.ic_volume_on);
        if(i.equals("off")){
            setTvProgressImg(R.mipmap.ic_volume_off);
        }
        mTvProgress.setText(i);
    }

    /**
     * 设置进度控制显示
     */
    private void setProgressInfo(String i){
        mTvProgress.setVisibility(VISIBLE);
        if(i.contains("+")){
            setTvProgressImg(R.mipmap.ic_fast_forward);
        }else {
            setTvProgressImg(R.mipmap.ic_fast_rewind);
        }
        mTvProgress.setText(i);
    }


    private void setTvProgressImg(int i){
        // 使用代码设置drawabletop
        Drawable drawable = activity.getResources().getDrawable(i);
        // / 这一步必须要做,否则不会显示.
        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                drawable.getMinimumHeight());
        mTvProgress.setCompoundDrawables(null, drawable, null, null);
    }




    /**
     * 隐藏视图Runnable
     */
    private Runnable mHideTouchViewRunnable = new Runnable() {
        @Override
        public void run() {
            hideTouchView();
        }
    };



    /**
     * 隐藏触摸进度视图
     */
    private void hideTouchView() {
        if(mTvProgress.getVisibility() == View.VISIBLE){
            mTvProgress.setVisibility(View.GONE);
        }
    }


    /**
     * 隐藏视图Runnable
     */
    private Runnable mHideAllViewRunnable = new Runnable() {
        @Override
        public void run() {
            hideAllView(false);
        }
    };

    /**
     * 隐藏除视频外所有视图
     */
    private void hideAllView(boolean isTouchLock) {
        mbottomBarControl.setVisibility(View.GONE);
        mWindowTopBar.setVisibility(View.GONE);
        mtopBarControl.setVisibility(View.GONE);
        showAspectRatioOptions(false);//隐藏宽高比

        mTvProgress.setVisibility(View.GONE);

    }


    /**
     * 显示控制栏
     * @param timeout 延迟隐藏时间
     */
    private void showControlBar(long timeout) {
        mbottomBarControl.setVisibility(VISIBLE);
        if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            mTvSystemTime.setText(StringHelper.getCurFormatTime());
            mtopBarControl.setVisibility(VISIBLE);
        }else {
            mWindowTopBar.setVisibility(VISIBLE);
        }
        // 先移除延迟隐藏控制栏 Runnable，如果 timeout=0 则不做延迟隐藏操作
        mPlayHandler.removeCallbacks(mHideAllViewRunnable);
        if (timeout != 0) {
            mPlayHandler.postDelayed(mHideAllViewRunnable, timeout);
        }
    }



    /**
     * 设置是否全屏
     * @param fullScreenOnly
     */
    public void setFullScreenOnly(boolean fullScreenOnly) {
        this.fullScreenOnly = fullScreenOnly;
        tryFullScreen(fullScreenOnly);
        if (fullScreenOnly) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        }
    }

    private void tryFullScreen(boolean fullScreen) {
        if (activity instanceof AppCompatActivity) {
            ActionBar supportActionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (supportActionBar != null) {
                if (fullScreen) {
                    supportActionBar.hide();
                } else {
                    supportActionBar.show();
                }
            }
        }
        //setFullScreen(fullScreen);
    }

    /*private void setFullScreen(boolean fullScreen) {
        if (activity != null) {
            WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
            if (fullScreen) {
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                activity.getWindow().setAttributes(attrs);
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);//行了某个操作而使之全屏
            } else {
                attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                activity.getWindow().setAttributes(attrs);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);//然后还需要退出全屏
            }
        }
    }**/

    /**
     * <pre>
     *     fitParent:可能会剪裁,保持原视频的大小，显示在中心,当原视频的大小超过view的大小超过部分裁剪处理
     *     fillParent:可能会剪裁,等比例放大视频，直到填满View为止,超过View的部分作裁剪处理
     *     wrapContent:将视频的内容完整居中显示，如果视频大于view,则按比例缩视频直到完全显示在view中
     *     fitXY:不剪裁,非等比例拉伸画面填满整个View
     *     16:9:不剪裁,非等比例拉伸画面到16:9,并完全显示在View中
     *     4:3:不剪裁,非等比例拉伸画面到4:3,并完全显示在View中
     * </pre>
     * @param scaleType
     */
    public void setScaleType(String scaleType) {
        if (SCALETYPE_FITPARENT.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
        }else if (SCALETYPE_FILLPARENT.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_ASPECT_FILL_PARENT);
        }else if (SCALETYPE_WRAPCONTENT.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_ASPECT_WRAP_CONTENT);
        }else if (SCALETYPE_FITXY.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_MATCH_PARENT);
        }else if (SCALETYPE_16_9.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_16_9_FIT_PARENT);
        }else if (SCALETYPE_4_3.equals(scaleType)) {
            videoView.setAspectRatio(IRenderView.AR_4_3_FIT_PARENT);
        }
    }

    public void start() {//播放开始
        // 更新进度
        mPlayHandler.removeMessages(MSG_UPDATE_SEEK);
        mPlayHandler.sendEmptyMessage(MSG_UPDATE_SEEK);
        mPlayStatus.setImageResource(R.mipmap.ic_video_pause);
        videoView.start();
    }

    public void pause() {//播放暂停
        // 暂停进度
        mPlayHandler.removeMessages(MSG_UPDATE_SEEK);
        mPlayStatus.setImageResource(R.mipmap.ic_video_play);
        videoView.pause();
    }

    /**
     * 返回按键
     * @return
     */
    public boolean onBackPressed() {
        if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){//当时是横屏0
            android.util.Log.e("lxc","onBackPressed---SCREEN_ORIENTATION_LANDSCAPE");
            setFullScreenOnly(false);//改为不是全屏
            mIvFullScreen.setImageResource(R.mipmap.ic_fullscreen);
            mtopBarControl.setVisibility(View.GONE);
            showAspectRatioOptions(false);
            mWindowTopBar.setVisibility(VISIBLE);
            hideAllView(false);
            return true;
        }else {
            android.util.Log.e("lxc","onBackPressed---finish");
            activity.finish();
            activity.overridePendingTransition(0,R.anim.slide_right_out);
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play_screen://全屏
                Log.e("lxc","getScreenOrientation()==="+getScreenOrientation());
                if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){//当时是横屏0
                    setFullScreenOnly(false);//改为不是全屏
                    mIvFullScreen.setImageResource(R.mipmap.ic_fullscreen);
                    mtopBarControl.setVisibility(View.GONE);
                    showAspectRatioOptions(false);//隐藏宽高比
                    mWindowTopBar.setVisibility(VISIBLE);
                }else {
                    setFullScreenOnly(true);//改为是全屏
                    mIvFullScreen.setImageResource(R.mipmap.ic_fullscreen_exit);
                    mtopBarControl.setVisibility(VISIBLE);
                    mTvSystemTime.setText(StringHelper.getCurFormatTime());
                    mWindowTopBar.setVisibility(View.GONE);
                }
                break;
            case R.id.play_status:
                if(isPlaying()){
                    pause();
                }else {
                    start();
                }
                break;
            case R.id.iv_play_status:
                reload(url);
                break;
            case R.id.iv_back_window:
                activity.finish();
                activity.overridePendingTransition(0,R.anim.slide_right_out);
                break;
            case R.id.iv_back:
                setFullScreenOnly(false);//改为不是全屏
                mIvFullScreen.setImageResource(R.mipmap.ic_fullscreen);
                showAspectRatioOptions(false);//隐藏宽高比
                mtopBarControl.setVisibility(View.GONE);
                mWindowTopBar.setVisibility(VISIBLE);
                break;
            case R.id.iv_screenshot:
                setScreenshot();
                break;
            case R.id.tv_settings:
                showAspectRatioOptions(true);
                break;
        }

    }


    private void setScreenshot(){
        pause();
        //hideAllView(false);
        showShareDialog(videoView.getShortcut());
    }

    /**
     * 显示对话框
     * @param bitmap
     */
    private void showShareDialog(Bitmap bitmap) {
        if (mShareDialog == null) {
            mShareDialog = new ShareDialog();
            mShareDialog.setClickListener(mInsideDialogClickListener);
            mShareDialog.setDismissListener(mDialogDismissListener);
            if (mDialogClickListener != null) {
                mShareDialog.setShareMode(true);
            }
        }
        mShareDialog.setScreenshotPhoto(bitmap);
        mShareDialog.show(activity.getSupportFragmentManager(),"share");

    }





    /**
     * 创建目录
     *
     * @param path
     */
    private void createSaveDir(String path) {
        mSaveDir = new File(path);
        if (!mSaveDir.exists()) {
            mSaveDir.mkdirs();
        } else if (!mSaveDir.isDirectory()) {//对象是否是文件夹
            mSaveDir.delete();
            mSaveDir.mkdirs();
        }
    }


    /**
     * 显示宽高比设置
     *
     * @param isShow
     */
    private void showAspectRatioOptions(boolean isShow) {
        if (isShow) {
            doClipViewHeight(mAspectRatioOptions, 0, mAspectOptionsHeight, 150);
        } else {
            ViewGroup.LayoutParams layoutParams = mAspectRatioOptions.getLayoutParams();
            layoutParams.height = 0;
        }
    }


    /**
     * 裁剪视图高度
     * @param view
     * @param srcHeight
     * @param endHeight
     * @param duration
     */
    public static void doClipViewHeight(final View view, int srcHeight, int endHeight, int duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(srcHeight, endHeight).setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int width = (int) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = width;
                view.setLayoutParams(layoutParams);
            }
        });
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.start();
    }



    class Query {
        private final Activity activity;
        private View view;

        public Query(Activity activity) {
            this.activity=activity;
        }

        public Query id(int id) {
            view = activity.findViewById(id);
            return this;
        }

        public Query image(int resId) {
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(resId);
            }
            return this;
        }

        public Query visible() {
            if (view != null) {
                view.setVisibility(VISIBLE);
            }
            return this;
        }

        public Query gone() {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
            return this;
        }

        public Query invisible() {
            if (view != null) {
                view.setVisibility(View.INVISIBLE);
            }
            return this;
        }

        public Query clicked(View.OnClickListener handler) {
            if (view != null) {
                view.setOnClickListener(handler);
            }
            return this;
        }

        public Query text(CharSequence text) {
            if (view!=null && view instanceof TextView) {
                ((TextView) view).setText(text);
            }
            return this;
        }

        public Query visibility(int visible) {
            if (view != null) {
                view.setVisibility(visible);
            }
            return this;
        }

        private void size(boolean width, int n, boolean dip){
            if(view != null){
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                if(n > 0 && dip){
                    n = dip2pixel(activity, n);
                }
                if(width){
                    lp.width = n;
                }else{
                    lp.height = n;
                }
                view.setLayoutParams(lp);
            }
        }

        public void height(int height, boolean dip) {
            size(false,height,dip);
        }

        public int dip2pixel(Context context, float n){
            int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, n, context.getResources().getDisplayMetrics());
            return value;
        }

        public float pixel2dip(Context context, float n){
            Resources resources = context.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float dp = n / (metrics.densityDpi / 160f);
            return dp;
        }
    }



    public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        // 是否是按下的标识，默认为其他动作，true为按下标识，false为其他动作
        private boolean firstTouch;
        // 是否声音控制,默认为亮度控制，true为声音控制，false为亮度控制
        private boolean volumeControl;
        // 是否横向滑动，默认为纵向滑动，true为横向滑动，false为纵向滑动
        private boolean toSeek;

        /**
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //videoView.toggleAspectRatio();
            if(isPlaying()){
                pause();
            }else {
                start();
            }
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            firstTouch = true;
            return super.onDown(e);
        }

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            float deltaY = mOldY - e2.getY();
            float deltaX = mOldX - e2.getX();
            if (firstTouch) {
                toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
                volumeControl=mOldX > screenWidthPixels * 0.5f;
                firstTouch = false;
            }

            if (toSeek) {
                if (!isLive) {
                    if(AssistUtil.getNavigationBarHeight(activity) == 0){//无虚拟按键
                        onProgressSlide(-deltaX / videoView.getWidth());//快进快退
                    }else {
                        if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){ //有虚拟键且横屏
                            Log.e("touch","mOldX=="+mOldX+",getScreenWidth=="+AssistUtil.getScreenWidth(activity)+",getNavigationBarHeight=="+AssistUtil.getNavigationBarHeight(activity));
                            if(mOldX < AssistUtil.getScreenWidth(activity)){
                                onProgressSlide(-deltaX / videoView.getWidth());//快进快退
                            }
                        }else{//有虚拟键竖屏
                            onProgressSlide(-deltaX / videoView.getWidth());//快进快退
                        }
                    }
                }
            } else {
                float percent = deltaY / videoView.getHeight();
                Log.e("touch","mOldY=="+mOldY+",getStateBarHeight2=="+AssistUtil.getStateBarHeight2(activity));
                if(mOldY > (float) AssistUtil.getStateBarHeight2(activity)){//下拉手机状态栏,不改变音量亮度
                    if (volumeControl) {
                        onVolumeSlide(percent);//改变声音
                    } else {
                        onBrightnessSlide(percent);//改变亮度
                    }
                }
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        /**
         * 单击确认
         * @param e
         * @return
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mbottomBarControl.getVisibility() == View.GONE ) {
                mbottomBarControl.setVisibility(VISIBLE);
                if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                    mtopBarControl.setVisibility(VISIBLE);
                    mTvSystemTime.setText(StringHelper.getCurFormatTime());
                }else {
                    mWindowTopBar.setVisibility(VISIBLE);
                }
                // 发送更新 Seek 消息
                mPlayHandler.removeMessages(MSG_UPDATE_SEEK);
                mPlayHandler.sendEmptyMessage(MSG_UPDATE_SEEK);
                // 发送延迟隐藏控制栏的操作
                mPlayHandler.postDelayed(mHideAllViewRunnable, DEFAULT_HIDE_TIMEOUT);
            } else {
                mbottomBarControl.setVisibility(View.GONE);
                mtopBarControl.setVisibility(View.GONE);
                showAspectRatioOptions(false);//隐藏宽高比
                mWindowTopBar.setVisibility(View.GONE);
            }
            return super.onSingleTapConfirmed(e);
        }

    }


    /**
     * 更新进度条,当前播放时间显示
     *
     * @return
     */
    private int setProgressTime() {
        if (videoView == null || mIsSeeking) {
            return 0;
        }
        // 视频播放的当前进度
        int position = videoView.getCurrentPosition();
        // 视频总的时长
        int duration = videoView.getDuration();
        if (duration > 0) {
            // 转换为 Seek 显示的进度值
            long pos = (long) MAX_VIDEO_SEEK * position / duration;
            mSeekBar.setProgress((int) pos);
            /**if (mIsEnableDanmaku) {
                mDanmakuPlayerSeek.setProgress((int) pos);
            }**/
        }
        // 获取缓冲的进度百分比，并显示在 Seek 的缓存进度
        int percent = videoView.getBufferPercentage();
        mSeekBar.setSecondaryProgress(percent * 10);
        /**if (mIsEnableDanmaku) {
            mDanmakuPlayerSeek.setSecondaryProgress(percent * 10);
        }**/
        mPlayHandler.removeMessages(MSG_UPDATE_SEEK);
        mPlayHandler.sendEmptyMessage(MSG_UPDATE_SEEK);
        // 更新播放时间
        mTvCurrentTime.setText(generateTime(position));
        mTvTotalTime.setText(generateTime(duration));
        // 返回当前播放进度
        return position;
    }



    /**
     * SeekBar监听,不会执行mPlayerTouchListener
     */
    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {


        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            Log.e("touch","3333333");
            showControlBar(DEFAULT_HIDE_TIMEOUT);
            mIsSeeking = true;
            currentPosition = videoView.getCurrentPosition();
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
            if (!fromUser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }
            long duration = videoView.getDuration();
            // 计算目标位置
            newPosition = (duration * progress) / MAX_VIDEO_SEEK;
            int deltaTime = (int) ((newPosition - currentPosition) / 1000);
            String desc;
            // 对比当前位置来显示快进或后退
            if (newPosition > currentPosition) {
                desc = generateTime(newPosition) + "/" + generateTime(duration) + "\n" + "+" + deltaTime + "秒";
            } else {
                desc = generateTime(newPosition) + "/" + generateTime(duration) + "\n" + deltaTime + "秒";
            }
            setProgressInfo(desc);
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            Log.e("touch","4444444");
            mIsSeeking = false;
            // 视频跳转
            videoView.seekTo((int)newPosition);
            newPosition = -1;
            setProgressTime();
            //隐藏触摸进度视图
            hideTouchView();
        }
    };



    /**
     * 触摸监听
     */
    private View.OnTouchListener mPlayerTouchListener = new View.OnTouchListener() {
        // 触摸模式：正常、无效、缩放旋转
        private static final int NORMAL = 1;
        private static final int INVALID_POINTER = 2;
        private static final int ZOOM_AND_ROTATE = 3;
        // 触摸模式
        private int mode = NORMAL;
        // 缩放的中点
        private PointF midPoint = new PointF(0, 0);
        // 旋转角度
        private float degree = 0;
        // 用来标识哪两个手指靠得最近，我的做法是取最近的两指中点和余下一指来控制旋转缩放
        private int fingerFlag = -1;
        // 初始间距
        private float oldDist;
        // 缩放比例
        private float scale;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (MotionEventCompat.getActionMasked(event)) {
                //手指按下
                case MotionEvent.ACTION_DOWN:
                    Log.e("touch","111111");
                    mode = NORMAL;
                    mPlayHandler.removeCallbacks(mHideAllViewRunnable);
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    /**if (event.getPointerCount() == 3 && mIsFullscreen) {
                        _hideTouchView();
                        // 进入三指旋转缩放模式，进行相关初始化
                        mode = ZOOM_AND_ROTATE;
                        MotionEventUtils.midPoint(midPoint, event);
                        fingerFlag = MotionEventUtils.calcFingerFlag(event);
                        degree = MotionEventUtils.rotation(event, fingerFlag);
                        oldDist = MotionEventUtils.calcSpacing(event, fingerFlag);
                        // 获取视频的 Matrix
                        mSaveMatrix = mVideoView.getVideoTransform();
                    } else {
                        mode = INVALID_POINTER;
                    }**/
                    mode = INVALID_POINTER;
                    break;

                case MotionEvent.ACTION_MOVE:
                    /**if (mode == ZOOM_AND_ROTATE) {
                        // 处理旋转
                        float newRotate = MotionEventUtils.rotation(event, fingerFlag);
                        mVideoView.setVideoRotation((int) (newRotate - degree));
                        // 处理缩放
                        mVideoMatrix.set(mSaveMatrix);
                        float newDist = MotionEventUtils.calcSpacing(event, fingerFlag);
                        scale = newDist / oldDist;
                        mVideoMatrix.postScale(scale, scale, midPoint.x, midPoint.y);
                        mVideoView.setVideoTransform(mVideoMatrix);
                    }**/
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    /**if (mode == ZOOM_AND_ROTATE) {
                        // 调整视频界面，让界面居中显示在屏幕
                        mIsNeedRecoverScreen = mVideoView.adjustVideoView(scale);
                        if (mIsNeedRecoverScreen && mIsShowBar) {
                            mTvRecoverScreen.setVisibility(VISIBLE);
                        }
                    }**/
                    mode = INVALID_POINTER;
                    break;
            }
            // 触屏手势处理
            if (mode == NORMAL) {
                if (gestureDetector.onTouchEvent(event)) {
                    Log.e("touch","22222");
                    android.util.Log.e("lxc","gestureDetector.onTouchEvent---");
                    return true;
                }
                //手指弹起
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_UP) {
                    Log.e("touch","33333");
                    android.util.Log.e("lxc","endGesture---");
                    endGesture();
                }
            }
            return false;
        }
    };

    /**
     * 手势结束调用
     */
    private void endGesture() {
        if (newPosition >= 0 && newPosition != videoView.getCurrentPosition()) {
            android.util.Log.e("lxc","endGesture22222---");
            // 视频跳转
            videoView.seekTo((int)newPosition);
            newPosition = -1;
            setProgressTime();

        }
        //隐藏触摸进度视图
        hideTouchView();
        brightness = -1;
        volume = -1;
    }


    /**
     * is player support this device,播放器支持这个设备吗
     * @return
     */
    public boolean isPlayerSupport() {
        return playerSupport;
    }

    /**
     * 是否正在播放
     * @return
     */
    public boolean isPlaying() {
        return videoView!=null?videoView.isPlaying():false;
    }

    /**
     * 停止播放
     */
    public void stop(){
        videoView.stopPlayback();
    }

    /**
     * 当前播放位置
     * @return
     */
    public int getCurrentPosition(){
        return videoView.getCurrentPosition();
    }

    /**
     * 获取缓冲百分比
     * @return
     */
    public int getBufferPercentage(){
        return videoView.getBufferPercentage();
    }

    public IjkVideoView getVideoView(){
        if (videoView != null){
            return videoView;
        }
        return null;
    }

    /**
     * get video duration,获取视频持续时间
     * @return
     */
    public int getDuration(){
        return videoView.getDuration();
    }

    public PlayerManager playInFullScreen(boolean fullScreen){
        if (fullScreen) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        return this;
    }

    public PlayerManager onError(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
        return this;
    }

    public PlayerManager onComplete(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
        return this;
    }

    public PlayerManager onInfo(OnInfoListener onInfoListener) {
        this.onInfoListener = onInfoListener;
        return this;
    }

    public PlayerManager onControlPanelVisibilityChange(OnControlPanelVisibilityChangeListener listener){
        this.onControlPanelVisibilityChangeListener = listener;
        return this;
    }

    /**
     * set is live (can't seek forward)
     * @param isLive
     * @return
     */
    public PlayerManager live(boolean isLive) {
        this.isLive = isLive;
        return this;
    }

    /**
     * 改变视频比例
     * @return
     */
    public PlayerManager toggleAspectRatio(){
        if (videoView != null) {
            videoView.toggleAspectRatio();
        }
        return this;
    }



    /**
     * 重新开始
     */
    public void reload(String url) {
        this.url = url;
        mSeekBar.setProgress(0);
        mSeekBar.setSecondaryProgress(0);
        if (mIsReady) {
            // 确保网络正常时
            if (NetWorkUtils.isNetworkAvailable(activity)) {
                Log.e("lxc","initPlayer333");
                mIvPlayStatus.setVisibility(View.GONE);
                videoView.setRender(IjkVideoView.RENDER_SURFACE_VIEW);//防止切换到下一个视频时，中间会停留上一个视频的残存画面。
                videoView.seekTo(0);
                play(url);

            }else {
                StringHelper.showShortMessage(activity,"请检查网络连接~");
            }
        } else {
            Log.e("lxc","initPlayer444");
            videoView.release(false);
            videoView.setRender(IjkVideoView.RENDER_TEXTURE_VIEW);
            play(url);
        }
    }


    public interface PlayerStateListener{
        void onComplete();
        void onError();
        void onLoading();
        void onPlay();
    }

    public interface OnErrorListener{
        void onError(int what, int extra);
    }

    public interface OnCompleteListener{
        void onComplete();
    }

    public interface OnControlPanelVisibilityChangeListener{
        void change(boolean isShowing);
    }

    public interface OnInfoListener{
        void onInfo(int what, int extra);
    }


    private class PlayHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case MSG_UPDATE_SEEK:
                    final int pos = setProgressTime();//更新进度
                    if (!mIsSeeking && videoView.isPlaying()) {
                        // 这里会重复发送MSG，已达到实时更新 Seek 的效果
                        msg = obtainMessage(MSG_UPDATE_SEEK);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
                }

           }
    }

    /**
     * 处理屏幕翻转
     * @param orientation
     */
    private void handleOrientation(int orientation) {
       /** if (mIsNeverPlay) {
            return;
        }
        if (mIsFullscreen && !mIsAlwaysFullScreen) {
            // 根据角度进行竖屏切换，如果为固定全屏则只能横屏切换
            if (orientation >= 0 && orientation <= 30 || orientation >= 330) {
                // 请求屏幕翻转
                mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            // 根据角度进行横屏切换
            if (orientation >= 60 && orientation <= 120) {
                mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            } else if (orientation >= 240 && orientation <= 300) {
                mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }**/
        Log.e("lxc","ggggggggggggggggg");
        if(fullScreenOnly){
            Log.e("lxc","mmmmmmmmmmmmmmmm");
            if (orientation >= 60 && orientation <= 120) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            } else if (orientation >= 240 && orientation <= 300) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }else {
            Log.e("lxc","nnnnnnnnnnnnnnnnnn");
            return;
        }
    }




    /**
     * 初始化电量、锁屏、时间处理
     */
    private void initReceiver() {

        if (AssistUtil.CheckSdCard2(activity)) {
            createSaveDir(AssistUtil.getSDpath() + File.separator + "screenshot");
        }else {
            createSaveDir(AssistUtil.getInternalpath() + File.separator + "screenshot");
        }

        mNetReceiver = new NetBroadcastReceiver();
        mBatteryReceiver = new BatteryBroadcastReceiver();
        //注册接受广播
        activity.registerReceiver(mNetReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        activity.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    public class NetBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 如果相等的话就说明网络状态发生了变化
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                mIsNetConnected = NetWorkUtils.isNetworkAvailable(activity);
                if(mIsNetConnected){//网络已连接
                        videoView.seekTo(getCurrentPosition());
                        if((mPlayStatus.getDrawable().getCurrent().getConstantState()).equals(ContextCompat.getDrawable(activity,R.mipmap.ic_video_pause).getConstantState())) {
                            start();
                    }
                }else {

                    StringHelper.showShortMessage(activity,"网络已断开~");
                }
            }
        }
    }

    /**
     * 接受电量改变广播
     */
    class BatteryBroadcastReceiver extends BroadcastReceiver {

        // 低电量临界值
        private static final int BATTERY_LOW_LEVEL = 15;

        @Override
        public void onReceive(Context context, Intent intent) {
            // 接收电量变化信息
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                // 电量百分比
                int curPower = level * 100 / scale;
                int status = intent.getIntExtra("status", BatteryManager.BATTERY_HEALTH_UNKNOWN);
                // SecondaryProgress 用来展示低电量，Progress 用来展示正常电量
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {//充电
                    mPbBattery.setSecondaryProgress(0);
                    mPbBattery.setProgress(0);
                    mPbBattery.setBackgroundResource(R.mipmap.ic_battery_charging);
                } else if (curPower < BATTERY_LOW_LEVEL) {
                    mPbBattery.setProgress(0);
                    mPbBattery.setSecondaryProgress(curPower);
                    mPbBattery.setBackgroundResource(R.mipmap.ic_battery);
                } else {
                    mPbBattery.setSecondaryProgress(0);
                    mPbBattery.setProgress(curPower);
                    mPbBattery.setBackgroundResource(R.mipmap.ic_battery);
                }
            }
        }
    }

}