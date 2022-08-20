package com.example.lxc.android_video.main.download;

import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.example.lxc.android_video.R;
import com.example.lxc.android_video.help.AssistUtil;

import static com.example.lxc.android_video.help.AssistUtil.CheckSdCard;
import static com.example.lxc.android_video.help.AssistUtil.CheckSdCard2;
import static com.example.lxc.android_video.help.AssistUtil.getAvailableInternalMemorySize;
import static com.example.lxc.android_video.help.AssistUtil.getSDAllSize;
import static com.example.lxc.android_video.help.AssistUtil.getSDFreeSize;
import static com.example.lxc.android_video.help.AssistUtil.getScreenWidth;
import static com.example.lxc.android_video.help.AssistUtil.getTotalInternalMemorySize;


/**
 * Created by lxc on 18-5-5.
 */

public class DownloadCompleteFragment extends Fragment{


    private TextView total_space_number_txt,spare_space_number_txt;

    private View red_view,dark_view;

    //屏幕宽度
    private int ScreenWidth;

    //总空间和剩余空间
    private long total_space,spare_space;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download_complete, container, false);

        total_space_number_txt = (TextView) view.findViewById(R.id.total_space_number);

        spare_space_number_txt = (TextView) view.findViewById(R.id.spare_space_number);

        total();

        spare();


        red_view = (View) view.findViewById(R.id.red_view);

        dark_view = (View) view.findViewById(R.id.dark_view);

        setColor();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 查询空间总容量
     */
    private void total(){
        Log.e("lxc","CheckSdCard2=="+CheckSdCard2(getActivity()));
        if(CheckSdCard2(getActivity())){
            Log.e("lxc","total_space1=="+getTotalInternalMemorySize()+","+getSDAllSize());
            total_space = getTotalInternalMemorySize() + getSDAllSize();
        }else {
            Log.e("lxc","total_space2=="+getTotalInternalMemorySize());
            total_space = getTotalInternalMemorySize();
        }
        Log.e("lxc","total_space3=="+total_space);
        total_space_number_txt.setText(computationalSpace(total_space));
    }


    /**
     * 查询空间剩余容量
     */
    private void spare(){
        if(CheckSdCard2(getActivity())){
            Log.e("lxc","spare_space1=="+getAvailableInternalMemorySize()+","+getSDFreeSize());
            spare_space = getAvailableInternalMemorySize() + getSDFreeSize();
        }else {
            Log.e("lxc","spare_space2=="+getAvailableInternalMemorySize());
            spare_space = getAvailableInternalMemorySize();
        }
        Log.e("lxc","spare_space3=="+spare_space);
        spare_space_number_txt.setText(computationalSpace(spare_space));
    }

    /**
     * 空间容量数字格式
     */
    private String computationalSpace(long spaced){
        double space = spaced;
        if(space/(1000*1000*1000)>1){
            Log.e("lxc","space=="+ space/(1000*1000*1000));
            String numberStr = String.valueOf(space/(1000*1000*1000));
            int index = numberStr.indexOf(".");
            String number = numberStr.substring(0,index+2);
            return number+"GB";
        }else if(space/(1000*1000)>1){
            String numberStr = String.valueOf(space/(1000*1000));
            int index = numberStr.indexOf(".");
            String number = numberStr.substring(0,index+2);
            return number+"MB";
        }else if(space/1000 >1){
            String numberStr = String.valueOf(space/1000);
            int index = numberStr.indexOf(".");
            String number = numberStr.substring(0,index+2);
            return number+"KB";
        }else {
            return "";
        }
    }


    /**
     * 设置总空间和剩余空间颜色比重
     */
    private void setColor(){
        ScreenWidth = getScreenWidth(getActivity());
        //获取控件的LayoutParams参数(注意：一定要用父控件的LayoutParams写LinearLayout.LayoutParams)
        RelativeLayout.LayoutParams lp=(RelativeLayout.LayoutParams) red_view.getLayoutParams();
        long used = total_space - spare_space;

        double i = used / (double) total_space * 100;
        int progress = (int) Math.ceil(i);
        Log.e("lxc","i=="+i);
        Log.e("lxc","progress=="+progress);

        lp.width= ScreenWidth*progress/100;//设置该控件的layoutParams参数
        red_view.setLayoutParams(lp);
    }
}
