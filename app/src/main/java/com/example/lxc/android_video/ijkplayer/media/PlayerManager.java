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
     * ???????????????,??????????????????????????????????????????,???????????????????????????view?????????????????????????????????
     */
    public static final String SCALETYPE_FITPARENT="fitParent";
    /**
     * ???????????????,????????????????????????????????????View??????,??????View????????????????????????
     */
    public static final String SCALETYPE_FILLPARENT="fillParent";
    /**
     * ?????????????????????????????????????????????????????????view,??????????????????????????????????????????view???
     */
    public static final String SCALETYPE_WRAPCONTENT="wrapContent";
    /**
     * ?????????,????????????????????????????????????View
     */
    public static final String SCALETYPE_FITXY="fitXY";
    /**
     * ?????????,???????????????????????????16:9,??????????????????View???
     */
    public static final String SCALETYPE_16_9="16:9";
    /**
     * ?????????,???????????????????????????4:3,??????????????????View???
     */
    public static final String SCALETYPE_4_3="4:3";

    /**
     * ????????????
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
    private boolean isLive = false;//???????????????
    private boolean fullScreenOnly = false;
    private boolean portrait;
    //??????????????????
    private final int mMaxVolume;
    private int screenWidthPixels;
    private int status=STATUS_IDLE;
    private long pauseTime;
    private String url;
    //?????????????????????
    private boolean mIsReady = false;
    // ???????????????????????????
    private boolean mIsSeeking = false;
    //????????????
    private float brightness=-1;
    //????????????
    private int volume=-1;
    //????????????????????????
    private long newPosition = -1;
    //??????????????????
    private int currentPosition;
    // ????????????????????????
    private long defaultRetryTime=5000;
    // ???????????????????????????
    private long DEFAULT_HIDE_TIMEOUT=5000;
    //??????????????????
    private int MAX_VIDEO_SEEK=1000;
    // ??????????????????
    private static final int MSG_UPDATE_SEEK = 10086;

    // ???????????????????????????
    private FrameLayout mFlVideoBox;
    //????????????????????????
    private TextView mTvCurrentTime;
    //????????????
    private SeekBar mSeekBar;
    //????????????????????????
    private TextView mTvTotalTime;
    //????????????
    private ImageView mIvFullScreen;
    //????????????????????????
    private ImageView mIvPlayStatus;
    //???????????????????????????
    private ImageView mPlayStatus;
    //????????????
    private LinearLayout mLlLoading;
    //????????????
    private TextView load_speed;
    //?????????????????????,?????????????????????????????????.??????
    private LinearLayout mbottomBarControl;
    //?????????????????????,??????????????????????????????
    private LinearLayout mtopBarControl;
    //??????
    private ProgressBar mPbBattery;
    //????????????
    private TextView mTvTitle;
    //????????????
    private TextView mTvSystemTime;
    //??????
    private ImageView mIvScreenshot;
    // ?????????????????????
    private TextView mTvSettings;
    private RadioGroup mAspectRatioOptions;
    // ??????????????????
    private int mAspectOptionsHeight;
    //????????????????????????
    private ImageView mIvBack;
    // ????????????????????????
    private ImageView mWindowBack;
    // ???????????????TopBar
    private FrameLayout mWindowTopBar;
    //??????????????????
    private final TextView mTvProgress;
    //???????????????Handler
    private PlayHandler mPlayHandler = new PlayHandler();
    // ????????????????????????
    private OrientationEventListener mOrientationListener;
    //??????????????????
    private boolean mIsNetConnected;
    private NetBroadcastReceiver mNetReceiver;
    private BatteryBroadcastReceiver mBatteryReceiver;

    // ??????????????????
    private File mSaveDir;
    // ??????????????????
    private ShareDialog mShareDialog;
    // ???????????????????????????????????????
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
                StringHelper.showShortMessage(activity, "????????????????????????:" + file.getAbsolutePath());
            } catch (IOException e) {
                StringHelper.showShortMessage(activity, "??????????????????");
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
     *???????????????????????????????????????????????????
     *@param defaultRetryTime???0??????????????????????????????5000??????
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

        mFlVideoBox = (FrameLayout) activity.findViewById(R.id.fl_video_box);//??????????????????
        videoView = (IjkVideoView) activity.findViewById(R.id.video_play_IjkVideoView);
        mTvCurrentTime = (TextView) activity.findViewById(R.id.tv_current_time);//????????????
        mSeekBar = (SeekBar) activity.findViewById(R.id.seekbar);//?????????
        mTvTotalTime = (TextView) activity.findViewById(R.id.tv_total_time);//?????????
        mIvFullScreen = (ImageView) activity.findViewById(R.id.iv_play_screen);//????????????
        mIvPlayStatus = (ImageView) activity.findViewById(R.id.iv_play_status);//????????????????????????
        mPlayStatus = (ImageView) activity.findViewById(R.id.play_status);//??????????????????????????????
        mLlLoading = (LinearLayout) activity.findViewById(R.id.ll_loading);//????????????
        load_speed = (TextView) activity.findViewById(R.id.load_speed);//????????????
        mbottomBarControl = (LinearLayout) activity.findViewById(R.id.fullscreen_bottom_bar);//??????????????????,???????????????????????????
        mtopBarControl = (LinearLayout) activity.findViewById(R.id.fullscreen_top_bar);//??????????????????,???????????????????????????
        mWindowTopBar = (FrameLayout) activity.findViewById(R.id.window_top_bar); //????????????,?????????????????????
        mWindowBack = (ImageView) activity.findViewById(R.id.iv_back_window);//??????????????????
        mIvBack = (ImageView) activity.findViewById(R.id.iv_back);//??????????????????
        mTvProgress = (TextView) activity.findViewById(R.id.tv_progress);//??????????????????

        mTvTitle = (TextView) activity.findViewById(R.id.tv_title); //????????????
        mPbBattery = (ProgressBar) activity.findViewById(R.id.pb_battery);//????????????
        mTvSystemTime = (TextView) activity.findViewById(R.id.tv_system_time);//??????????????????
        mIvScreenshot = (ImageView) activity.findViewById(R.id.iv_screenshot);//??????
        mTvSettings = (TextView) activity.findViewById(R.id.tv_settings);//????????????
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
        /*---------------------------------------????????????----------------------------------------*/
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


        //?????????????????????
        videoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                Log.e("lxc","IMediaPlayer6666");
                statusChange(STATUS_COMPLETED);
                onCompleteListener.onComplete();
            }
        });
        //?????????????????????
        videoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                Log.e("lxc","IMediaPlayer1111");
                mIsReady = true;
                mPlayStatus.setEnabled(false);
                //?????????????????????????????????
                //mPlayHandler.sendEmptyMessage(Normal_Play);
            }
        });
        //?????????????????????
        videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                statusChange(STATUS_ERROR);
                Log.e("lxc","IMediaPlayerERROR");
                onErrorListener.onError(what,extra);
                return true;
            }
        });

        //????????????
        videoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START://????????????
                        Log.e("lxc","IMediaPlayer2222");
                        statusChange(STATUS_LOADING);
                        mLlLoading.setVisibility(VISIBLE);
                        mIvPlayStatus.setVisibility(View.GONE);
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END://????????????
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
                    case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH://????????????????????????
                        Log.e("lxc","IMediaPlayer4444");
                        //??????????????????
                        load_speed.setText("??????"+extra);
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START://??????????????????????????????
                        Log.e("lxc","IMediaPlayer5555");
                        Log.e("lxc","getDuration()=="+getDuration());
                        statusChange(STATUS_PLAYING);
                        mLlLoading.setVisibility(View.GONE);
                        mTvTotalTime.setText(generateTime((long) getDuration()));
                        //????????????
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
        //????????????
        gestureDetector = new GestureDetector(activity, new PlayerGestureListener());
        //??????????????????
        mFlVideoBox.setClickable(true);
        mFlVideoBox.setOnTouchListener(mPlayerTouchListener);
        // ??????????????????,??????????????????
        mOrientationListener = new OrientationEventListener(activity) {
            @Override
            public void onOrientationChanged(int orientation) {
                Log.e("lxc","mOrientationListener??????????????????");
                handleOrientation(orientation);
            }
        };
        if (true) {
            // ????????????
            mOrientationListener.disable();
        }


        if (fullScreenOnly) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        portrait=getScreenOrientation()== ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        if (!playerSupport) {
            DebugLog.e("","???????????????????????????");
        }
    }

    private void statusChange(int newStatus) {
        status = newStatus;
        if (!isLive && newStatus==STATUS_COMPLETED) {
            Log.e("lxc","videoView.getCurrentPosition()=="+videoView.getCurrentPosition()+",videoView.getDuration()==="+videoView.getDuration());
            if (videoView.getDuration() == -1 || videoView.getCurrentPosition() + 1000 < videoView.getDuration()) {//??????????????????
                mLlLoading.setVisibility(VISIBLE);
                StringHelper.showShortMessage(activity,"????????????");

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
     * ??????????????????onPause
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
             * ???power???????????????????????????????????????
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
     * ???????????????
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
     * ???????????????????????????????????????
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
     * ????????????????????????
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
        // ????????????
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        // ???????????????
        int i = (int) (index * 1.0 / mMaxVolume * 100);
        String s = i + "%";
        if (i == 0) {
            s = "off";
        }
        setVolumeInfo(s);//???????????? endGesture()??????????????????
    }

    /**
     * ????????????
     * @param percent
     */
    private void onProgressSlide(float percent) {
        //????????????
        int position = videoView.getCurrentPosition();
        //?????????
        long duration = videoView.getDuration();
        // ??????????????????????????????100?????????????????????1/2
        long deltaMax = Math.min(100 * 1000, duration/2);
        // ??????????????????
        long delta = (long) (deltaMax * percent);
        //????????????
        newPosition = delta + position;
        if (newPosition > duration) {
            newPosition = duration;
        } else if (newPosition <= 0) {
            newPosition=0;
        }
        int deltaTime = (int) ((newPosition - position) / 1000);
        String desc;
        // ??????????????????????????????????????????
        if (newPosition > position) {
            desc = generateTime(newPosition) + "/" + generateTime(duration) + "\n" + "+" + deltaTime + "???";
        } else {
            desc = generateTime(newPosition) + "/" + generateTime(duration) + "\n" + deltaTime + "???";
        }
        setProgressInfo(desc);//newPosition!=-1,?????????????????? endGesture()?????????????????????
    }

    /**
     * ??????????????????
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
        activity.getWindow().setAttributes(lpa);//???????????? endGesture()??????????????????
    }

    /**
     * ????????????????????????
     *
     * @param brightness
     */
    private void setBrightnessInfo(float brightness) {
        mTvProgress.setVisibility(VISIBLE);
        setTvProgressImg(R.mipmap.ic_brightness);
        mTvProgress.setText(Math.ceil(brightness * 100) + "%");
    }

    /**
     * ????????????????????????
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
     * ????????????????????????
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
        // ??????????????????drawabletop
        Drawable drawable = activity.getResources().getDrawable(i);
        // / ?????????????????????,??????????????????.
        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                drawable.getMinimumHeight());
        mTvProgress.setCompoundDrawables(null, drawable, null, null);
    }




    /**
     * ????????????Runnable
     */
    private Runnable mHideTouchViewRunnable = new Runnable() {
        @Override
        public void run() {
            hideTouchView();
        }
    };



    /**
     * ????????????????????????
     */
    private void hideTouchView() {
        if(mTvProgress.getVisibility() == View.VISIBLE){
            mTvProgress.setVisibility(View.GONE);
        }
    }


    /**
     * ????????????Runnable
     */
    private Runnable mHideAllViewRunnable = new Runnable() {
        @Override
        public void run() {
            hideAllView(false);
        }
    };

    /**
     * ??????????????????????????????
     */
    private void hideAllView(boolean isTouchLock) {
        mbottomBarControl.setVisibility(View.GONE);
        mWindowTopBar.setVisibility(View.GONE);
        mtopBarControl.setVisibility(View.GONE);
        showAspectRatioOptions(false);//???????????????

        mTvProgress.setVisibility(View.GONE);

    }


    /**
     * ???????????????
     * @param timeout ??????????????????
     */
    private void showControlBar(long timeout) {
        mbottomBarControl.setVisibility(VISIBLE);
        if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            mTvSystemTime.setText(StringHelper.getCurFormatTime());
            mtopBarControl.setVisibility(VISIBLE);
        }else {
            mWindowTopBar.setVisibility(VISIBLE);
        }
        // ?????????????????????????????? Runnable????????? timeout=0 ???????????????????????????
        mPlayHandler.removeCallbacks(mHideAllViewRunnable);
        if (timeout != 0) {
            mPlayHandler.postDelayed(mHideAllViewRunnable, timeout);
        }
    }



    /**
     * ??????????????????
     * @param fullScreenOnly
     */
    public void setFullScreenOnly(boolean fullScreenOnly) {
        this.fullScreenOnly = fullScreenOnly;
        tryFullScreen(fullScreenOnly);
        if (fullScreenOnly) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//??????
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//??????
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
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);//?????????????????????????????????
            } else {
                attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                activity.getWindow().setAttributes(attrs);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);//???????????????????????????
            }
        }
    }**/

    /**
     * <pre>
     *     fitParent:???????????????,??????????????????????????????????????????,???????????????????????????view?????????????????????????????????
     *     fillParent:???????????????,????????????????????????????????????View??????,??????View????????????????????????
     *     wrapContent:?????????????????????????????????????????????????????????view,??????????????????????????????????????????view???
     *     fitXY:?????????,????????????????????????????????????View
     *     16:9:?????????,???????????????????????????16:9,??????????????????View???
     *     4:3:?????????,???????????????????????????4:3,??????????????????View???
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

    public void start() {//????????????
        // ????????????
        mPlayHandler.removeMessages(MSG_UPDATE_SEEK);
        mPlayHandler.sendEmptyMessage(MSG_UPDATE_SEEK);
        mPlayStatus.setImageResource(R.mipmap.ic_video_pause);
        videoView.start();
    }

    public void pause() {//????????????
        // ????????????
        mPlayHandler.removeMessages(MSG_UPDATE_SEEK);
        mPlayStatus.setImageResource(R.mipmap.ic_video_play);
        videoView.pause();
    }

    /**
     * ????????????
     * @return
     */
    public boolean onBackPressed() {
        if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){//???????????????0
            android.util.Log.e("lxc","onBackPressed---SCREEN_ORIENTATION_LANDSCAPE");
            setFullScreenOnly(false);//??????????????????
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
            case R.id.iv_play_screen://??????
                Log.e("lxc","getScreenOrientation()==="+getScreenOrientation());
                if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){//???????????????0
                    setFullScreenOnly(false);//??????????????????
                    mIvFullScreen.setImageResource(R.mipmap.ic_fullscreen);
                    mtopBarControl.setVisibility(View.GONE);
                    showAspectRatioOptions(false);//???????????????
                    mWindowTopBar.setVisibility(VISIBLE);
                }else {
                    setFullScreenOnly(true);//???????????????
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
                setFullScreenOnly(false);//??????????????????
                mIvFullScreen.setImageResource(R.mipmap.ic_fullscreen);
                showAspectRatioOptions(false);//???????????????
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
     * ???????????????
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
     * ????????????
     *
     * @param path
     */
    private void createSaveDir(String path) {
        mSaveDir = new File(path);
        if (!mSaveDir.exists()) {
            mSaveDir.mkdirs();
        } else if (!mSaveDir.isDirectory()) {//????????????????????????
            mSaveDir.delete();
            mSaveDir.mkdirs();
        }
    }


    /**
     * ?????????????????????
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
     * ??????????????????
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
        // ???????????????????????????????????????????????????true??????????????????false???????????????
        private boolean firstTouch;
        // ??????????????????,????????????????????????true??????????????????false???????????????
        private boolean volumeControl;
        // ?????????????????????????????????????????????true??????????????????false???????????????
        private boolean toSeek;

        /**
         * ??????
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
         * ??????
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
                    if(AssistUtil.getNavigationBarHeight(activity) == 0){//???????????????
                        onProgressSlide(-deltaX / videoView.getWidth());//????????????
                    }else {
                        if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){ //?????????????????????
                            Log.e("touch","mOldX=="+mOldX+",getScreenWidth=="+AssistUtil.getScreenWidth(activity)+",getNavigationBarHeight=="+AssistUtil.getNavigationBarHeight(activity));
                            if(mOldX < AssistUtil.getScreenWidth(activity)){
                                onProgressSlide(-deltaX / videoView.getWidth());//????????????
                            }
                        }else{//??????????????????
                            onProgressSlide(-deltaX / videoView.getWidth());//????????????
                        }
                    }
                }
            } else {
                float percent = deltaY / videoView.getHeight();
                Log.e("touch","mOldY=="+mOldY+",getStateBarHeight2=="+AssistUtil.getStateBarHeight2(activity));
                if(mOldY > (float) AssistUtil.getStateBarHeight2(activity)){//?????????????????????,?????????????????????
                    if (volumeControl) {
                        onVolumeSlide(percent);//????????????
                    } else {
                        onBrightnessSlide(percent);//????????????
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
         * ????????????
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
                // ???????????? Seek ??????
                mPlayHandler.removeMessages(MSG_UPDATE_SEEK);
                mPlayHandler.sendEmptyMessage(MSG_UPDATE_SEEK);
                // ????????????????????????????????????
                mPlayHandler.postDelayed(mHideAllViewRunnable, DEFAULT_HIDE_TIMEOUT);
            } else {
                mbottomBarControl.setVisibility(View.GONE);
                mtopBarControl.setVisibility(View.GONE);
                showAspectRatioOptions(false);//???????????????
                mWindowTopBar.setVisibility(View.GONE);
            }
            return super.onSingleTapConfirmed(e);
        }

    }


    /**
     * ???????????????,????????????????????????
     *
     * @return
     */
    private int setProgressTime() {
        if (videoView == null || mIsSeeking) {
            return 0;
        }
        // ???????????????????????????
        int position = videoView.getCurrentPosition();
        // ??????????????????
        int duration = videoView.getDuration();
        if (duration > 0) {
            // ????????? Seek ??????????????????
            long pos = (long) MAX_VIDEO_SEEK * position / duration;
            mSeekBar.setProgress((int) pos);
            /**if (mIsEnableDanmaku) {
                mDanmakuPlayerSeek.setProgress((int) pos);
            }**/
        }
        // ????????????????????????????????????????????? Seek ???????????????
        int percent = videoView.getBufferPercentage();
        mSeekBar.setSecondaryProgress(percent * 10);
        /**if (mIsEnableDanmaku) {
            mDanmakuPlayerSeek.setSecondaryProgress(percent * 10);
        }**/
        mPlayHandler.removeMessages(MSG_UPDATE_SEEK);
        mPlayHandler.sendEmptyMessage(MSG_UPDATE_SEEK);
        // ??????????????????
        mTvCurrentTime.setText(generateTime(position));
        mTvTotalTime.setText(generateTime(duration));
        // ????????????????????????
        return position;
    }



    /**
     * SeekBar??????,????????????mPlayerTouchListener
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
            // ??????????????????
            newPosition = (duration * progress) / MAX_VIDEO_SEEK;
            int deltaTime = (int) ((newPosition - currentPosition) / 1000);
            String desc;
            // ??????????????????????????????????????????
            if (newPosition > currentPosition) {
                desc = generateTime(newPosition) + "/" + generateTime(duration) + "\n" + "+" + deltaTime + "???";
            } else {
                desc = generateTime(newPosition) + "/" + generateTime(duration) + "\n" + deltaTime + "???";
            }
            setProgressInfo(desc);
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            Log.e("touch","4444444");
            mIsSeeking = false;
            // ????????????
            videoView.seekTo((int)newPosition);
            newPosition = -1;
            setProgressTime();
            //????????????????????????
            hideTouchView();
        }
    };



    /**
     * ????????????
     */
    private View.OnTouchListener mPlayerTouchListener = new View.OnTouchListener() {
        // ?????????????????????????????????????????????
        private static final int NORMAL = 1;
        private static final int INVALID_POINTER = 2;
        private static final int ZOOM_AND_ROTATE = 3;
        // ????????????
        private int mode = NORMAL;
        // ???????????????
        private PointF midPoint = new PointF(0, 0);
        // ????????????
        private float degree = 0;
        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        private int fingerFlag = -1;
        // ????????????
        private float oldDist;
        // ????????????
        private float scale;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (MotionEventCompat.getActionMasked(event)) {
                //????????????
                case MotionEvent.ACTION_DOWN:
                    Log.e("touch","111111");
                    mode = NORMAL;
                    mPlayHandler.removeCallbacks(mHideAllViewRunnable);
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    /**if (event.getPointerCount() == 3 && mIsFullscreen) {
                        _hideTouchView();
                        // ??????????????????????????????????????????????????????
                        mode = ZOOM_AND_ROTATE;
                        MotionEventUtils.midPoint(midPoint, event);
                        fingerFlag = MotionEventUtils.calcFingerFlag(event);
                        degree = MotionEventUtils.rotation(event, fingerFlag);
                        oldDist = MotionEventUtils.calcSpacing(event, fingerFlag);
                        // ??????????????? Matrix
                        mSaveMatrix = mVideoView.getVideoTransform();
                    } else {
                        mode = INVALID_POINTER;
                    }**/
                    mode = INVALID_POINTER;
                    break;

                case MotionEvent.ACTION_MOVE:
                    /**if (mode == ZOOM_AND_ROTATE) {
                        // ????????????
                        float newRotate = MotionEventUtils.rotation(event, fingerFlag);
                        mVideoView.setVideoRotation((int) (newRotate - degree));
                        // ????????????
                        mVideoMatrix.set(mSaveMatrix);
                        float newDist = MotionEventUtils.calcSpacing(event, fingerFlag);
                        scale = newDist / oldDist;
                        mVideoMatrix.postScale(scale, scale, midPoint.x, midPoint.y);
                        mVideoView.setVideoTransform(mVideoMatrix);
                    }**/
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    /**if (mode == ZOOM_AND_ROTATE) {
                        // ???????????????????????????????????????????????????
                        mIsNeedRecoverScreen = mVideoView.adjustVideoView(scale);
                        if (mIsNeedRecoverScreen && mIsShowBar) {
                            mTvRecoverScreen.setVisibility(VISIBLE);
                        }
                    }**/
                    mode = INVALID_POINTER;
                    break;
            }
            // ??????????????????
            if (mode == NORMAL) {
                if (gestureDetector.onTouchEvent(event)) {
                    Log.e("touch","22222");
                    android.util.Log.e("lxc","gestureDetector.onTouchEvent---");
                    return true;
                }
                //????????????
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
     * ??????????????????
     */
    private void endGesture() {
        if (newPosition >= 0 && newPosition != videoView.getCurrentPosition()) {
            android.util.Log.e("lxc","endGesture22222---");
            // ????????????
            videoView.seekTo((int)newPosition);
            newPosition = -1;
            setProgressTime();

        }
        //????????????????????????
        hideTouchView();
        brightness = -1;
        volume = -1;
    }


    /**
     * is player support this device,??????????????????????????????
     * @return
     */
    public boolean isPlayerSupport() {
        return playerSupport;
    }

    /**
     * ??????????????????
     * @return
     */
    public boolean isPlaying() {
        return videoView!=null?videoView.isPlaying():false;
    }

    /**
     * ????????????
     */
    public void stop(){
        videoView.stopPlayback();
    }

    /**
     * ??????????????????
     * @return
     */
    public int getCurrentPosition(){
        return videoView.getCurrentPosition();
    }

    /**
     * ?????????????????????
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
     * get video duration,????????????????????????
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
     * ??????????????????
     * @return
     */
    public PlayerManager toggleAspectRatio(){
        if (videoView != null) {
            videoView.toggleAspectRatio();
        }
        return this;
    }



    /**
     * ????????????
     */
    public void reload(String url) {
        this.url = url;
        mSeekBar.setProgress(0);
        mSeekBar.setSecondaryProgress(0);
        if (mIsReady) {
            // ?????????????????????
            if (NetWorkUtils.isNetworkAvailable(activity)) {
                Log.e("lxc","initPlayer333");
                mIvPlayStatus.setVisibility(View.GONE);
                videoView.setRender(IjkVideoView.RENDER_SURFACE_VIEW);//????????????????????????????????????????????????????????????????????????????????????
                videoView.seekTo(0);
                play(url);

            }else {
                StringHelper.showShortMessage(activity,"?????????????????????~");
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
                    final int pos = setProgressTime();//????????????
                    if (!mIsSeeking && videoView.isPlaying()) {
                        // ?????????????????????MSG???????????????????????? Seek ?????????
                        msg = obtainMessage(MSG_UPDATE_SEEK);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
                }

           }
    }

    /**
     * ??????????????????
     * @param orientation
     */
    private void handleOrientation(int orientation) {
       /** if (mIsNeverPlay) {
            return;
        }
        if (mIsFullscreen && !mIsAlwaysFullScreen) {
            // ???????????????????????????????????????????????????????????????????????????
            if (orientation >= 0 && orientation <= 30 || orientation >= 330) {
                // ??????????????????
                mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            // ??????????????????????????????
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
     * ???????????????????????????????????????
     */
    private void initReceiver() {

        if (AssistUtil.CheckSdCard2(activity)) {
            createSaveDir(AssistUtil.getSDpath() + File.separator + "screenshot");
        }else {
            createSaveDir(AssistUtil.getInternalpath() + File.separator + "screenshot");
        }

        mNetReceiver = new NetBroadcastReceiver();
        mBatteryReceiver = new BatteryBroadcastReceiver();
        //??????????????????
        activity.registerReceiver(mNetReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        activity.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    public class NetBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // ??????????????????????????????????????????????????????
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                mIsNetConnected = NetWorkUtils.isNetworkAvailable(activity);
                if(mIsNetConnected){//???????????????
                        videoView.seekTo(getCurrentPosition());
                        if((mPlayStatus.getDrawable().getCurrent().getConstantState()).equals(ContextCompat.getDrawable(activity,R.mipmap.ic_video_pause).getConstantState())) {
                            start();
                    }
                }else {

                    StringHelper.showShortMessage(activity,"???????????????~");
                }
            }
        }
    }

    /**
     * ????????????????????????
     */
    class BatteryBroadcastReceiver extends BroadcastReceiver {

        // ??????????????????
        private static final int BATTERY_LOW_LEVEL = 15;

        @Override
        public void onReceive(Context context, Intent intent) {
            // ????????????????????????
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                // ???????????????
                int curPower = level * 100 / scale;
                int status = intent.getIntExtra("status", BatteryManager.BATTERY_HEALTH_UNKNOWN);
                // SecondaryProgress ????????????????????????Progress ????????????????????????
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {//??????
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