package com.example.lxc.android_video.base;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Toast;

import com.example.lxc.android_video.R;
import com.example.lxc.android_video.help.AssistUtil;


/**
 * Created by lxc on 18-4-12.
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //强制设置成竖屏，禁止横屏展示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //指定无标题头，标题自己定义
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        //产生的activity添加到类管理器中
        ActivityCollector.addActivity(this);

        try {
            //系统信息初始化
            SystemProperty.init(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }


    /**
     * 吐司显示提示信息
     * @param msg
     */
    public void showShortMessage(Context context, String msg){
        Toast toast = null;
        if (toast==null){
            toast = Toast.makeText(context,msg,Toast.LENGTH_SHORT);
        }else{
            toast.setText(msg);
        }
        toast.show();
    }

}
