package com.example.lxc.android_video.main.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.lxc.android_video.R;
import com.example.lxc.android_video.base.BaseActivity;
import com.example.lxc.android_video.help.AssistUtil;
import com.example.lxc.android_video.help.BroadCastManager;
import com.example.lxc.android_video.ijkplayer.media.PlayerManager;
import com.scwang.smartrefresh.layout.util.DensityUtil;


import java.util.ArrayList;
import java.util.List;


/**
 * Created by lxc on 18-11-5.
 */

public class VideoPlayMainActivity extends BaseActivity implements PlayerManager.PlayerStateListener{

    private ViewPager videoViewPager;

    private FragmentPagerAdapter mAdapter;

    private List<Fragment> fragmentArrayList;

    private RelativeLayout introductionLayout,commentLayout;

    private View play_view;

    //引导线
    private ImageView tab_line;

    private TextView introduction_txt,video_comment_txt,video_comment_number;

    //屏幕的宽度
    private int screenWidth;

    // 屏幕UI可见性
    private int mScreenUiVisibility;

    public int video_id;

    public String introduction_content,video_name,video_img;

    //实例化当前类(接口)
    public static VideoPlayMainActivity instance;

    //返回当前类
    public static VideoPlayMainActivity getInstance(){return instance;}


    private PlayerManager player;

    //广播接收者
    private LocalReceiver mReceiver;

    private String url ;//播放该集的id,url

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        //获取传送过来的数据
        instance = this;
        Intent intent = this.getIntent();
        video_id = intent.getIntExtra("video_introduction_id",0);
        introduction_content = intent.getStringExtra("introduction_content");
        video_name = intent.getStringExtra("video_name");
        video_img = intent.getStringExtra("video_img");

        AssistUtil.setStatusBarColor(this,getResources().getColor(R.color.dark));

        introductionLayout = (RelativeLayout) findViewById(R.id.video_introduction);
        introductionLayout.setOnClickListener(new TabOnClickListener(0));
        commentLayout = (RelativeLayout) findViewById(R.id.video_comment);
        commentLayout.setOnClickListener(new TabOnClickListener(1));

        tab_line = (ImageView) findViewById(R.id.video_tab_line);

        introduction_txt = (TextView) findViewById(R.id.introduction_txt);
        video_comment_txt = (TextView) findViewById(R.id.video_comment_txt);
        video_comment_number = (TextView) findViewById(R.id.video_comment_number);

        play_view = findViewById(R.id.play_view);

        initTabLine();

        fragmentArrayList = new ArrayList<Fragment>();
        fragmentArrayList.add(new VideoPlayIntroductionFragment());
        fragmentArrayList.add(new VideoPlayCommentFragment());

        videoViewPager = (ViewPager) findViewById(R.id.video_viewpager);
        //Fragment页面适配器
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return fragmentArrayList.size();
            }
            @Override
            public Fragment getItem(int fragment) {
                return fragmentArrayList.get(fragment);
            }
        };

        videoViewPager.setAdapter(mAdapter);

        videoViewPager.setOnPageChangeListener(new TabOnPageChangeListener());

        //接收url广播
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction("fragment_play_url");
            mReceiver = new LocalReceiver();
            BroadCastManager.getInstance().registerReceiver(this,
                    mReceiver, filter);//注册广播接收者
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放视频
     * @param url
     */
    private void initPlayer(String url) {
        if(player == null) {
            Log.e("lxc","initPlayer111");
            player = new PlayerManager(this);
            player.setFullScreenOnly(false);
            player.setScaleType(PlayerManager.SCALETYPE_FITPARENT);
            player.playInFullScreen(false);
            player.setPlayerStateListener(this);
            player.play(url);
        }else {
            //切换视频
            Log.e("lxc","initPlayer222");
            player.reload(url);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (player !=null && player.gestureDetector != null && player.gestureDetector.onTouchEvent(event))
            return true;
        return super.onTouchEvent(event);
    }


    @Override
    public void onComplete() {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onLoading() {

    }

    @Override
    public void onPlay() {

    }

    /**
     * 功能：点击主页TAB事件
     */
    public class TabOnClickListener implements View.OnClickListener{
        private int index=0;
        public TabOnClickListener(int i){
            index=i;
        }
        public void onClick(View v) {
            videoViewPager.setCurrentItem(index);//选择某一页
        }

    }


    /**
     * 功能：Fragment页面改变事件
     */
    public class TabOnPageChangeListener implements ViewPager.OnPageChangeListener{

        //当前页面被滑动时调用,设置引导线滑动
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            RelativeLayout.LayoutParams lp=(android.widget.RelativeLayout.LayoutParams) tab_line.getLayoutParams();
            //返回组件距离左侧组件的距离
            lp.leftMargin= (int) ((positionOffset+position)*(screenWidth/2)+(screenWidth/4-lp.width/2));
            tab_line.setLayoutParams(lp);
        }

        //当新的页面被选中时调用
        @Override
        public void onPageSelected(int position) {
            //重置所有TextView的字体颜色
            resetTextView();
            int color = getResources().getColor(R.color.main_bottom_select);
            switch (position) {
                case 0:
                    introduction_txt.setTextColor(color);

                    RelativeLayout.LayoutParams lp=(android.widget.RelativeLayout.LayoutParams) tab_line.getLayoutParams();
                    introduction_txt.measure( 0,  0);
                    int measuredWidth = introduction_txt.getMeasuredWidth();
                    lp.width = measuredWidth;
                    break;
                case 1:
                    video_comment_txt.setTextColor(color);
                    video_comment_number.setTextColor(color);

                    RelativeLayout.LayoutParams lp2=(android.widget.RelativeLayout.LayoutParams) tab_line.getLayoutParams();
                    video_comment_number.measure(0,0);
                    video_comment_txt.measure(0,0);
                    int measuredWidth1 = video_comment_txt.getMeasuredWidth();
                    int measuredWidth2 = video_comment_number.getMeasuredWidth();
                    lp2.width = measuredWidth1+measuredWidth2;
                    break;
            }
        }

        //当滑动状态改变时调用
        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }

    /**
     * 重置字体颜色
     */
    private void resetTextView() {
        introduction_txt.setTextColor(getResources().getColor(R.color.main_bottom_default));
        video_comment_txt.setTextColor(getResources().getColor(R.color.main_bottom_default));
        video_comment_number.setTextColor(getResources().getColor(R.color.main_bottom_default));
    }


    /**
     * 根据屏幕的宽度，初始化引导线的宽度
     */
    private void initTabLine() {
        //获取屏幕的宽度
        screenWidth= AssistUtil.getScreenWidth(this);

        introduction_txt.measure( 0,  0);
        int measuredWidth = introduction_txt.getMeasuredWidth();

        //获取控件的LayoutParams参数(注意：一定要用父控件的LayoutParams写LinearLayout.LayoutParams)
        RelativeLayout.LayoutParams lp=(RelativeLayout.LayoutParams) tab_line.getLayoutParams();
        //lp.width= (screenWidth/2)-100;//设置该控件的layoutParams参数
        lp.width = measuredWidth;
        tab_line.setLayoutParams(lp);//将修改好的layoutParams设置为该控件的layoutParams

    }


    /**
     * 接收广播后的操作
     */
    class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //收到广播后的处理
            String adapter_url = intent.getStringExtra("url");
            Log.e("lxc","url_2==="+adapter_url);
            if(!adapter_url.isEmpty()) {
                Log.e("lxc","initPlayer开始");
                initPlayer(adapter_url);
            }
        }
    }

    @Override
    protected void onPause() {
        if(player!=null) {
            player.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(player!=null) {
            player.onResume();
        }
        super.onResume();
    }

    /**
     * 注销url广播接收者
     */
    @Override
    protected void onDestroy() {
        BroadCastManager.getInstance().unregisterReceiver(this,mReceiver);
        if(player!=null) {
            Log.e("lxc","player.onDestroy();");
            player.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (player != null && player.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    /**
     * SYSTEM_UI_FLAG_LAYOUT_STABLE：维持一个稳定的布局
     * SYSTEM_UI_FLAG_FULLSCREEN：Activity全屏显示，且状态栏被隐藏覆盖掉
     * SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN：Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住
     * SYSTEM_UI_FLAG_HIDE_NAVIGATION：隐藏虚拟按键(导航栏)
     * SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION：效果同View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
     * SYSTEM_UI_FLAG_IMMERSIVE：沉浸式，从顶部下滑出现状态栏和导航栏会固定住
     * SYSTEM_UI_FLAG_IMMERSIVE_STICKY：黏性沉浸式，从顶部下滑出现状态栏和导航栏过几秒后会缩回去
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e("lxc","bbbb11111111");
        //1竖屏   0横屏
         LinearLayout.LayoutParams params;
         if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
             Log.e("lxc", "bbbb11111111");
             //竖屏 设置高度为200dp
             // 还原
             View decorView = this.getWindow().getDecorView();
             decorView.setSystemUiVisibility(mScreenUiVisibility);
             params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DensityUtil.dp2px(200));
             play_view.setLayoutParams(params);
             getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

         } else {
             Log.e("lxc", "bbb222222222");
             //横屏 设置全屏
             // 沉浸式只能在SDK19以上实现
             if (Build.VERSION.SDK_INT >= 14) {
                 if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                     // 获取关联 Activity 的 DecorView
                     View decorView = this.getWindow().getDecorView();
                     // 保存旧的配置
                     mScreenUiVisibility = decorView.getSystemUiVisibility();
                     // 沉浸式使用这些Flag
                     decorView.setSystemUiVisibility(
                             View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                             View.SYSTEM_UI_FLAG_FULLSCREEN |
                             View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                             View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                             View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                             View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                     );
                     play_view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                     this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                 }
             }
         }

    }

}


