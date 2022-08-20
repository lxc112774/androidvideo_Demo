package com.example.lxc.android_video.main.home;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lxc.android_video.R;

/**
 * Created by lxc on 19-8-6.
 */

public class SendCommentDialog extends Dialog {

    private int data_id, user_id;

    private Context context;

    private EditText editText;

    private  LinearLayout comment_dialog;

    public SendCommentDialog(@NonNull Context context, int data_id, int user_id) {
        super(context,R.style.inputDialog);
        this.context = context;
        this.data_id = data_id;
        this.user_id = user_id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_comment_dialog);

        initView();
    }


    /**
     * 初始化界面控件
     */
    private void initView() {
        editText = (EditText) findViewById(R.id.comment_content_edit);
        comment_dialog = (LinearLayout) findViewById(R.id.comment_dialog);

        Window dialogWindow = getWindow();
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels); // 宽度设置为全屏
        lp.y = 0;//设置Dialog距离底部的距离
        dialogWindow.setAttributes(lp);
    }


    /**
     * 输入框隐藏后隐藏软键盘
     */
    //通过dispatchTouchEvent每次ACTION_DOWN事件中动态判断非EditText本身区域的点击事件，然后在事件中进行屏蔽.
    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();//获取当前焦点所在的view
            //判断dialog位置
            android.util.Log.e("lxc","v=="+v);
            android.util.Log.e("lxc","isShouldHideInput(v, ev)=="+isShouldHideInput(v, ev));
            if (isShouldHideInput(v, ev)) {

                VideoPlayCommentFragment.changComent();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }

            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    public  boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            //获取当前dialog的location位置(左上角)
            comment_dialog.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + comment_dialog.getHeight();
            int right = left + comment_dialog.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是dialog区域，保留点击EditText的事件
                android.util.Log.e("lxc","ssssss==");
                return false;
            } else {
                return true;
            }
        }
        return false;
    }


}
