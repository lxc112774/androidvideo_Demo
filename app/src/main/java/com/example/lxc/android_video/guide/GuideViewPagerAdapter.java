package com.example.lxc.android_video.guide;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by lxc on 18-4-12.
 */

public class GuideViewPagerAdapter extends PagerAdapter {


    private List<View> views;
    public GuideViewPagerAdapter(List<View> views) {
        this.views=views;
    }

    // 销毁arg1位置的界面
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager)container).removeView(views.get(position));
    }

    @Override
    public void finishUpdate(ViewGroup container) {
    }

    // 获得当前界面数
    @Override
    public int getCount(){
        if (views != null){
            return views.size();
        }
        return 0;
    }

    /**
     * 初始化position位置的界面
     * @param container
     * @param position
     * @return
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views.get(position));
        return views.get(position);
    }

    // 判断是否由对象生成界面
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return (arg0 == arg1);
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1){
    }

    @Override
    public Parcelable saveState(){
        return null;
    }

    @Override
    public void startUpdate(ViewGroup arg0){
    }
}
