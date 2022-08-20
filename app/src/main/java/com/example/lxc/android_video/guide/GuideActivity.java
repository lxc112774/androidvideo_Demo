package com.example.lxc.android_video.guide;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;


import com.example.lxc.android_video.main.MainActivity;
import com.example.lxc.android_video.R;
import com.example.lxc.android_video.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lxc on 18-4-12.
 */

/**
 * 引导页面
 */
public class GuideActivity extends BaseActivity{

    //ViewPager适配器
    private GuideViewPagerAdapter mAdapter;

    private ViewPager mViewPager;

    //图片信息
    private int[] imageDatas = new int[]{R.mipmap.boot1,R.mipmap.boot2,R.mipmap.boot3};

    // 页面集合
    private List<View> views;

    //小圆点集合
    private ImageView[] dotViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        //初始化ViewPager
        mViewPager = (ViewPager) findViewById(R.id.guide_viewPage);

        //初始化展示的视图
        views = new ArrayList<View>();

        LayoutInflater inflater= LayoutInflater.from(this);

        //初始化View
        for (int i=0;i<imageDatas.length;i++){
            //视图
            View mView = inflater.inflate(R.layout.activity_guide_main,null);
            //图片控件
            ImageView guide_img = (ImageView)mView.findViewById(R.id.guide_img);
            //设置图片
            guide_img.setImageResource(imageDatas[i]);

            //设置小圆点颜色
            LinearLayout layout = (LinearLayout) mView.findViewById(R.id.ll_dot);
            layout.getChildAt(i).setBackgroundResource(R.drawable.guide_dot_select);

            //设置点击事件
            if (i == imageDatas.length-1){
                //显示点击的按钮
                Button btn = (Button)mView.findViewById(R.id.guide_btn);
                btn.setVisibility(View.VISIBLE);

                //设置点击事件
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(GuideActivity.this, MainActivity.class);
                        GuideActivity.this.startActivity(intent);
                        GuideActivity.this.finish();
                    }
                });
            }


            views.add(mView);
        }

        mAdapter=new GuideViewPagerAdapter(views);
        mViewPager.setAdapter(mAdapter);
    }


    /**
     * 根据引导页的数量，动态生成相应数量的导航小圆点，并添加到LinearLayout中显示。
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    /**private void initDots() {
        LinearLayout layout = (LinearLayout) mView.findViewById(R.id.ll_dot);
        LayoutParams mParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mParams.setMargins(10, 0, 10, 0);//设置小圆点左右之间的间隔 //left,top,right,bottom

        dotViews = new ImageView[imageDatas.length];

        for (int j = 0; j < imageDatas.length; j++) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(mParams);
            imageView.setBackgroundResource(R.drawable.guide_dot);
            imageView.setEnabled(true);
            dotViews[i] = imageView;//得到每个小圆点的引用，用于滑动页面时，（onPageSelected方法中）更改它们的状态。
            layout.addView(imageView);//添加到布局里面显示
        }
        layout.getChildAt(0).setEnabled(true);
    }**/

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
