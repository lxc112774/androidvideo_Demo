package com.example.lxc.android_video.main.download;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.lxc.android_video.R;
import com.example.lxc.android_video.help.AssistUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lxc on 18-4-27.
 */

public class DownloadFragment extends Fragment {

    private ViewPager viewPager;

    private FragmentPagerAdapter mAdapter;

    private List<Fragment> fragmentArrayList;

    private RelativeLayout completeLayout,NocompleteLayout;

    //引导线
    private ImageView tab_line;

    private TextView complete_txt,nocomplete_txt;

    //屏幕的宽度
    private int screenWidth;

    /***
     * 作用；
     * 这个方法是用来返回一个视图用于显示在fragment上
     * inflater:用于将一个XML文件实例化成一个View队形
     * savedInstanceState: 用于保存一些信息之用；
     * ***/
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download, container, false);

        viewPager = (ViewPager)view.findViewById(R.id.download_viewpager);



        completeLayout = (RelativeLayout) view.findViewById(R.id.download_complete);

        completeLayout.setOnClickListener(new TabOnClickListener(0));

        NocompleteLayout = (RelativeLayout) view.findViewById(R.id.download_nocomplete);

        NocompleteLayout.setOnClickListener(new TabOnClickListener(1));


        complete_txt = (TextView) view.findViewById(R.id.complete_txt);

        nocomplete_txt = (TextView) view.findViewById(R.id.no_complete_txt);

        tab_line = (ImageView) view.findViewById(R.id.tab_line);

        //页面初始化
        initFragment();


        //Fragment页面适配器
        mAdapter = new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public int getCount() {
                return fragmentArrayList.size();
            }
            @Override
            public Fragment getItem(int fragment) {
                return fragmentArrayList.get(fragment);
            }
        };

        viewPager.setAdapter(mAdapter);

        viewPager.setOnPageChangeListener(new TabOnPageChangeListener());

        //初始化引导线长度
        initTabLine();


        return view;
    }

    private void initFragment(){
        fragmentArrayList = new ArrayList<Fragment>();
        fragmentArrayList.add(new DownloadCompleteFragment());
        fragmentArrayList.add(new DownloadNoCompleteFragment());
    }

    /**
     * 根据屏幕的宽度，初始化引导线的宽度
     */
    private void initTabLine() {
        //获取屏幕的宽度
        screenWidth= AssistUtil.getScreenWidth(getActivity());

        //获取控件的LayoutParams参数(注意：一定要用父控件的LayoutParams写LinearLayout.LayoutParams)
        RelativeLayout.LayoutParams lp=(RelativeLayout.LayoutParams) tab_line.getLayoutParams();
        lp.width= (screenWidth/2)-100;//设置该控件的layoutParams参数
        tab_line.setLayoutParams(lp);//将修改好的layoutParams设置为该控件的layoutParams

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
            viewPager.setCurrentItem(index);//选择某一页
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
            lp.leftMargin= (int) ((positionOffset+position)*(screenWidth/2)+50);
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
                    complete_txt.setTextColor(color);
                    break;
                case 1:
                    nocomplete_txt.setTextColor(color);
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
        complete_txt.setTextColor(getResources().getColor(R.color.main_bottom_default));
        nocomplete_txt.setTextColor(getResources().getColor(R.color.main_bottom_default));
    }





}
