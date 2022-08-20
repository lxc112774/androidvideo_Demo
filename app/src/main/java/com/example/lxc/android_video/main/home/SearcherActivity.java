package com.example.lxc.android_video.main.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lxc.android_video.R;
import com.example.lxc.android_video.base.BaseActivity;
import com.example.lxc.android_video.help.SoftInputUtils;

/**
 * Created by lxc on 18-5-4.
 */

public class SearcherActivity extends BaseActivity implements View.OnClickListener{

    private TextView cancle_txt;

    private EditText editText;

    private ImageView delete_img;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        cancle_txt = (TextView) findViewById(R.id.cancle_txt);
        cancle_txt.setOnClickListener(this);

        editText = (EditText) findViewById(R.id.search_edit);

        delete_img = (ImageView) findViewById(R.id.delete_img);
        delete_img.setOnClickListener(this);

        //搜索框监听
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    delete_img.setVisibility(View.GONE);
                } else {
                    delete_img.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.delete_img:
                if(!editText.getText().equals("") || editText.getText()!=null){
                    editText.setText("");
                }
                break;

            case R.id.cancle_txt:
                SoftInputUtils.closeSoftInput(this);
                finish();
                overridePendingTransition(0, R.anim.slide_top_out);//动画效果
                break;
        }
    }




}
