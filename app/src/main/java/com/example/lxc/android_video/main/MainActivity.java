package com.example.lxc.android_video.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lxc.android_video.R;
import com.example.lxc.android_video.base.ActivityCollector;
import com.example.lxc.android_video.base.BaseActivity;
import com.example.lxc.android_video.base.SystemProperty;
import com.example.lxc.android_video.help.AssistUtil;
import com.example.lxc.android_video.help.DownloadImageTask;
import com.example.lxc.android_video.help.download_img.BitmapUtils;
import com.example.lxc.android_video.main.LoginRegister.LoginActivity;
import com.example.lxc.android_video.main.LoginRegister.UserBean;
import com.example.lxc.android_video.main.LoginRegister.UserSettingActivity;
import com.example.lxc.android_video.main.download.DownloadFragment;
import com.example.lxc.android_video.main.home.HomeFragment;
import com.example.lxc.android_video.main.kind.KindFragment;

import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by lxc on 18-4-12.
 */

public class MainActivity extends BaseActivity implements View.OnClickListener{

    private static final String LOG_TAG = "MainActivity";

    private static final int REQUEST_PERMISSION_REQUEST_CODE = 1;// 申请权限返回的结果码

    private ArrayList<Fragment> fragmentArrayList;

    private Fragment mCurrentFrgment;

    private RelativeLayout homelayout,kindlayout,downloadlayout;

    private ImageView home_img,kind_img,download_img,user_img;
    private TextView home_txt,kind_txt,download_txt,user_name;

    private SwipeRefreshLayout swipeRefreshLayout;

    private Switch night_switch;

    private boolean switch_checkout_state;//是否夜间模式

    //滑动左侧布局
    private  LinearLayout left_ll;

    //屏幕宽度
    private int ScreenWidth;

    //当前页面id
    private int currentIndex = -1;

    // 定义一个变量，来标识是否退出
    private long exitTime = 0;

    private final static int REQUESTCODE = 1; // 登录返回的结果码
    private final static int REQUECUSTOM = 0; // 用户设置返回的结果码

    private int user_id;//用户id

    private String user_img_url,user_nick_name;//用户昵称,头像


    //权限
    public static final String STORAGE_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String STORAGE_READ = Manifest.permission.READ_EXTERNAL_STORAGE;
    private String[] permissions = new String[]{STORAGE_WRITE,STORAGE_READ};

    private ArrayList<String> needPermission;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initFragment();

        //判断系统版本是否高于Android7.0，申请权限
        if(Build.VERSION.SDK_INT >= 24) {
            Log.e("lxc","Build.VERSION.SDK_INT==="+Build.VERSION.SDK_INT);
            initRequestCompetence();
        }

    }

    /**
     * 三个RelativeLayout添加点击事件
     */
    private void initView() {
        homelayout = (RelativeLayout) findViewById(R.id.home_page);
        kindlayout = (RelativeLayout) findViewById(R.id.kind_page);
        downloadlayout = (RelativeLayout) findViewById(R.id.download_page);

        homelayout.setOnClickListener(this);
        homelayout.setTag(0);

        kindlayout.setOnClickListener(this);
        kindlayout.setTag(1);

        downloadlayout.setOnClickListener(this);
        downloadlayout.setTag(2);

        home_img = (ImageView) findViewById(R.id.home_img);
        kind_img = (ImageView) findViewById(R.id.kind_img);
        download_img = (ImageView) findViewById(R.id.download_img);

        home_txt = (TextView) findViewById(R.id.home_txt);
        kind_txt = (TextView) findViewById(R.id.kind_txt);
        download_txt = (TextView) findViewById(R.id.download_txt);

        left_ll = (LinearLayout) findViewById(R.id.left_menu_layout);
        ScreenWidth = AssistUtil.getScreenWidth(getApplicationContext());
        //获取控件的LayoutParams参数(注意：一定要用父控件的LayoutParams写LinearLayout.LayoutParams)
        DrawerLayout.LayoutParams lp=(DrawerLayout.LayoutParams) left_ll.getLayoutParams();
        lp.width= ScreenWidth*2/3;//设置该控件的layoutParams参数
        left_ll.setLayoutParams(lp);


        //清除之前的USER信息
        SystemProperty.getInstance(MainActivity.this).setUserNull();
        user_img = (ImageView) findViewById(R.id.user_img);
        user_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SystemProperty.isLogin(MainActivity.this)){
                    //跳转用户界面
                    Intent intent = new Intent(MainActivity.this, UserSettingActivity.class);
                    startActivityForResult(intent, REQUECUSTOM);//有返回值的跳转
                }else {
                    //跳转登录界面
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent, REQUESTCODE);//有返回值的跳转
                }
            }
        });

        SharedPreferences preferences = getSharedPreferences(LOG_TAG,Context.MODE_PRIVATE);
        switch_checkout_state = preferences.getBoolean("switch_checkout_state",false);

        user_name = (TextView) findViewById(R.id.user_name);
        night_switch = (Switch) findViewById(R.id.night_switch);
        night_switch.setChecked(switch_checkout_state);
        /**night_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        //recreate();//需要recreate才能生效

        SharedPreferences sharedPreferences = getSharedPreferences(LOG_TAG,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("switch_checkout_state",isChecked);
        editor.commit();


        startActivity(new Intent(MainActivity.this,MainActivity.class));
        finish();
        overridePendingTransition(0,R.anim.activity_stay);


        }
        });**/

        if(SystemProperty.isLogin(MainActivity.this)){
            UserBean userdatas= SystemProperty.getInstance(this).getUser(MainActivity.this);
            android.util.Log.e("lxc","ssss==="+userdatas.getUsername());
            String username = userdatas.getUsername();
            user_img_url = userdatas.getUserimg();
            user_name.setText(username);

            if(user_img_url == null || user_img_url.equals("")){
                user_img.setImageResource(R.drawable.lxc);
            }else{
                // Base64解码图片
                byte[] imageByteArray = Base64.decode(user_img_url , 0);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                if(bitmap !=null) user_img.setImageBitmap(bitmap);

            }
        }

    }


    /**
     * 实例化布局
     */
    private void initFragment() {
        fragmentArrayList = new ArrayList<Fragment>(3);
        fragmentArrayList.add(new HomeFragment());
        fragmentArrayList.add(new KindFragment());
        fragmentArrayList.add(new DownloadFragment());

        homelayout.setSelected(true);
        changeTab(0);
    }

    /**
     * 功能：点击主页TAB事件
     */
    @Override
    public void onClick(View v) {
        changeTab((Integer) v.getTag());
    }

    /**
     * 功能：Fragment页面改变事件
     */
    private void changeTab(int index) {
        homelayout.setSelected(index == 0);
        kindlayout.setSelected(index == 1);
        downloadlayout.setSelected(index == 2);

        //更改字体颜色和图片
        clearSelection();
        if(index == 0) {
            if (currentIndex != -1) {
                if (index == currentIndex) {
                    //这里大家可以加上底栏二次点击你想要实现相应的操作
                    Log.e("lxc","第二次点击刷新");
                    final HomeFragment fragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(fragmentArrayList.get(currentIndex).getClass().getName());
                    swipeRefreshLayout = (SwipeRefreshLayout) fragment.getView().findViewById(R.id.home_swipe_refresh);//fragment中的控件
                    swipeRefreshLayout.setRefreshing(true);//显示进度圈
                    fragment.refreshData();//延迟先显示进度圈
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            //execute the task
                        }
                    }, 400);
                }
            }
            home_img.setImageResource(R.drawable.home_img_select);
            home_txt.setTextColor(getResources().getColor(R.color.main_bottom_select));
        }else if(index == 1) {
            kind_img.setImageResource(R.drawable.group_img_select);
            kind_txt.setTextColor(getResources().getColor(R.color.main_bottom_select));
        }else if (index == 2) {
            download_img.setImageResource(R.drawable.download_img_select);
            download_txt.setTextColor(getResources().getColor(R.color.main_bottom_select));
        }else {
            showShortMessage(this,"程序出错");
        }

        currentIndex = index;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //判断当前的Fragment是否为空，不为空则隐藏
        if (null != mCurrentFrgment) {
            ft.hide(mCurrentFrgment);
        }
        //先根据Tag从FragmentTransaction事物获取之前添加的Fragment
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentArrayList.get(currentIndex).getClass().getName());

        if (null == fragment) {
            //如fragment为空，则之前未添加此Fragment便从集合中取出
            fragment = fragmentArrayList.get(index);
        }
        mCurrentFrgment = fragment;

        //判断此Fragment是否已经添加到FragmentTransaction事物中
        if (!fragment.isAdded()) {
            ft.add(R.id.main_content, fragment, fragment.getClass().getName());
        } else {
            ft.show(fragment);
        }
        ft.commit();
    }


    /**
     * 清除掉所有的选中状态
     */
    private void clearSelection() {
        home_img.setImageResource(R.drawable.home_img);
        home_txt.setTextColor(getResources().getColor(R.color.main_bottom_default));
        kind_img.setImageResource(R.drawable.group_img);
        kind_txt.setTextColor(getResources().getColor(R.color.main_bottom_default));
        download_img.setImageResource(R.drawable.download_img);
        download_txt.setTextColor(getResources().getColor(R.color.main_bottom_default));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    //连按两次返回退出
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                showShortMessage(this,"再按一次退出~");
                exitTime = System.currentTimeMillis();
            } else {
                ActivityCollector.finishAll();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 登录界面返回结果
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RESULT_OK，判断另外一个activity已经结束数据输入功能，Standard activity result:
        // operation succeeded. 默认值是-1
        if(resultCode == 2){//ok
            if(requestCode == REQUESTCODE || requestCode == REQUECUSTOM){
                user_id = data.getIntExtra("user_id",0);
                user_img_url = data.getStringExtra("user_img");//设置显示
                Log.e("lxc","user_img_url=="+user_img_url);
                user_nick_name = data.getStringExtra("user_name");

                if(SystemProperty.isLogin(MainActivity.this)) {
                    user_name.setText(user_nick_name);
                    if (user_img_url == null || user_img_url.equals("")) {
                        user_img.setImageResource(R.drawable.lxc);
                    } else {
                        // Base64解码图片
                        byte[] imageByteArray = Base64.decode(user_img_url, 0);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                        if (bitmap != null) user_img.setImageBitmap(bitmap);

                    }
                }else {
                    user_name.setText(R.string.user_name);
                    user_img.setImageResource(R.drawable.user_header);
                }
            }
        }
    }


    //申请权限
    private void initRequestCompetence(){
        needPermission = new ArrayList<String>();
        for (String permission : permissions) {
            if (this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { //检查是否授予权限
                needPermission.add(permission); //未授予的添加
            }
        }

        if(needPermission.toArray(new String[needPermission.size()]).length > 0){
            requestPermissions(needPermission.toArray(new String[needPermission.size()]), REQUEST_PERMISSION_REQUEST_CODE);
        }
    }


    //不论哪种结果,回调到该方法,授权结果封装到grantResults
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (PackageManager.PERMISSION_GRANTED != result) {
                    showShortMessage(this,"你需要打开权限");
                    return;
                }
            }
        }
    }


}
