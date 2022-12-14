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

    private static final int REQUEST_PERMISSION_REQUEST_CODE = 1;// ??????????????????????????????

    private ArrayList<Fragment> fragmentArrayList;

    private Fragment mCurrentFrgment;

    private RelativeLayout homelayout,kindlayout,downloadlayout;

    private ImageView home_img,kind_img,download_img,user_img;
    private TextView home_txt,kind_txt,download_txt,user_name;

    private SwipeRefreshLayout swipeRefreshLayout;

    private Switch night_switch;

    private boolean switch_checkout_state;//??????????????????

    //??????????????????
    private  LinearLayout left_ll;

    //????????????
    private int ScreenWidth;

    //????????????id
    private int currentIndex = -1;

    // ??????????????????????????????????????????
    private long exitTime = 0;

    private final static int REQUESTCODE = 1; // ????????????????????????
    private final static int REQUECUSTOM = 0; // ??????????????????????????????

    private int user_id;//??????id

    private String user_img_url,user_nick_name;//????????????,??????


    //??????
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

        //??????????????????????????????Android7.0???????????????
        if(Build.VERSION.SDK_INT >= 24) {
            Log.e("lxc","Build.VERSION.SDK_INT==="+Build.VERSION.SDK_INT);
            initRequestCompetence();
        }

    }

    /**
     * ??????RelativeLayout??????????????????
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
        //???????????????LayoutParams??????(?????????????????????????????????LayoutParams???LinearLayout.LayoutParams)
        DrawerLayout.LayoutParams lp=(DrawerLayout.LayoutParams) left_ll.getLayoutParams();
        lp.width= ScreenWidth*2/3;//??????????????????layoutParams??????
        left_ll.setLayoutParams(lp);


        //???????????????USER??????
        SystemProperty.getInstance(MainActivity.this).setUserNull();
        user_img = (ImageView) findViewById(R.id.user_img);
        user_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SystemProperty.isLogin(MainActivity.this)){
                    //??????????????????
                    Intent intent = new Intent(MainActivity.this, UserSettingActivity.class);
                    startActivityForResult(intent, REQUECUSTOM);//?????????????????????
                }else {
                    //??????????????????
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent, REQUESTCODE);//?????????????????????
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
        //recreate();//??????recreate????????????

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
                // Base64????????????
                byte[] imageByteArray = Base64.decode(user_img_url , 0);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                if(bitmap !=null) user_img.setImageBitmap(bitmap);

            }
        }

    }


    /**
     * ???????????????
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
     * ?????????????????????TAB??????
     */
    @Override
    public void onClick(View v) {
        changeTab((Integer) v.getTag());
    }

    /**
     * ?????????Fragment??????????????????
     */
    private void changeTab(int index) {
        homelayout.setSelected(index == 0);
        kindlayout.setSelected(index == 1);
        downloadlayout.setSelected(index == 2);

        //???????????????????????????
        clearSelection();
        if(index == 0) {
            if (currentIndex != -1) {
                if (index == currentIndex) {
                    //????????????????????????????????????????????????????????????????????????
                    Log.e("lxc","?????????????????????");
                    final HomeFragment fragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(fragmentArrayList.get(currentIndex).getClass().getName());
                    swipeRefreshLayout = (SwipeRefreshLayout) fragment.getView().findViewById(R.id.home_swipe_refresh);//fragment????????????
                    swipeRefreshLayout.setRefreshing(true);//???????????????
                    fragment.refreshData();//????????????????????????
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
            showShortMessage(this,"????????????");
        }

        currentIndex = index;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //???????????????Fragment?????????????????????????????????
        if (null != mCurrentFrgment) {
            ft.hide(mCurrentFrgment);
        }
        //?????????Tag???FragmentTransaction???????????????????????????Fragment
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentArrayList.get(currentIndex).getClass().getName());

        if (null == fragment) {
            //???fragment??????????????????????????????Fragment?????????????????????
            fragment = fragmentArrayList.get(index);
        }
        mCurrentFrgment = fragment;

        //?????????Fragment?????????????????????FragmentTransaction?????????
        if (!fragment.isAdded()) {
            ft.add(R.id.main_content, fragment, fragment.getClass().getName());
        } else {
            ft.show(fragment);
        }
        ft.commit();
    }


    /**
     * ??????????????????????????????
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


    //????????????????????????
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                showShortMessage(this,"??????????????????~");
                exitTime = System.currentTimeMillis();
            } else {
                ActivityCollector.finishAll();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * ????????????????????????
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RESULT_OK?????????????????????activity?????????????????????????????????Standard activity result:
        // operation succeeded. ????????????-1
        if(resultCode == 2){//ok
            if(requestCode == REQUESTCODE || requestCode == REQUECUSTOM){
                user_id = data.getIntExtra("user_id",0);
                user_img_url = data.getStringExtra("user_img");//????????????
                Log.e("lxc","user_img_url=="+user_img_url);
                user_nick_name = data.getStringExtra("user_name");

                if(SystemProperty.isLogin(MainActivity.this)) {
                    user_name.setText(user_nick_name);
                    if (user_img_url == null || user_img_url.equals("")) {
                        user_img.setImageResource(R.drawable.lxc);
                    } else {
                        // Base64????????????
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


    //????????????
    private void initRequestCompetence(){
        needPermission = new ArrayList<String>();
        for (String permission : permissions) {
            if (this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { //????????????????????????
                needPermission.add(permission); //??????????????????
            }
        }

        if(needPermission.toArray(new String[needPermission.size()]).length > 0){
            requestPermissions(needPermission.toArray(new String[needPermission.size()]), REQUEST_PERMISSION_REQUEST_CODE);
        }
    }


    //??????????????????,??????????????????,?????????????????????grantResults
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (PackageManager.PERMISSION_GRANTED != result) {
                    showShortMessage(this,"?????????????????????");
                    return;
                }
            }
        }
    }


}
