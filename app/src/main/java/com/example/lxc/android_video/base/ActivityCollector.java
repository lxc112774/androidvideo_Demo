package com.example.lxc.android_video.base;

/**
 * Created by lxc on 18-4-12.
 */

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 类活动管理器
 */
public class ActivityCollector {
    public static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity){
        activities.add(activity);
    }

    public static void removeActivity(Activity activity){
        activities.remove(activity);
    }

    /**
     * 关闭整个应用
     */
    public static void finishAll(){
        for(Activity activity:activities){
            if(!activity.isFinishing()){
                activity.finish();
            }
        }
        activities.clear();
        //关闭应用程序
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
