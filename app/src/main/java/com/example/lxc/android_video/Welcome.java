package com.example.lxc.android_video;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.example.lxc.android_video.base.BaseActivity;
import com.example.lxc.android_video.guide.GuideActivity;
import com.example.lxc.android_video.main.MainActivity;

public class Welcome extends BaseActivity {
    //是否是第一次进入程序
    private boolean isFirstIn = false;

    private static final int TIME = 1000;
    private static final int GO_HOME = 1000;
    private static final int GO_GUIDE = 1001;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GO_GUIDE:
                    goGuide();
                    break;
                case GO_HOME:
                    goHome();
                    break;
            }

        }
    };

    private RelativeLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_welcome);


        //渐显动画
        rootLayout = (RelativeLayout) findViewById(R.id.all_welcome);
        AlphaAnimation animation = new AlphaAnimation(1.0f, 1.0f);
        animation.setDuration(3000);
        rootLayout.startAnimation(animation);
        //动画完成的监听
        animation.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                init();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    /**
     * 进行是否第一次进入应用的判断
     **/
    private void init() {
        SharedPreferences sharedPreferences = getSharedPreferences("Welcome", MODE_PRIVATE);
        //查询值，不存在赋值为true;
        isFirstIn = sharedPreferences.getBoolean("isFirstIn", true);
        if (isFirstIn) {
            mHandler.sendEmptyMessageDelayed(GO_GUIDE, TIME);//延时发送
            sharedPreferences.edit().putBoolean("isFirstIn", false).commit();//提交数据
        } else {
            mHandler.sendEmptyMessageDelayed(GO_HOME, TIME);
        }
    }


    /**
     *进入主界面
     */
    private void goHome(){
        startActivity(new Intent(Welcome.this,MainActivity.class));
        overridePendingTransition(R.anim.slide_left_in,R.anim.activity_stay);
        finish();
    }


    /**
     *进入引导界面
     */
    private void goGuide(){
        startActivity(new Intent(Welcome.this,GuideActivity.class));
        finish();
    }
}
