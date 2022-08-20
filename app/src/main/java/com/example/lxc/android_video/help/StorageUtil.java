package com.example.lxc.android_video.help;

import android.app.Activity;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by lxc on 18-11-12.
 */

/**
 * 反射获取手机内存路径和SD卡路径
 */

public class StorageUtil {

    private Activity mActivity;
    private StorageManager mStorageManager;

    public String path1 = null;
    public String path2 = null;

    public StorageUtil(Activity activity) {
        mActivity = activity;
        if (mActivity != null) {
            mStorageManager = (StorageManager) mActivity.getSystemService(Activity.STORAGE_SERVICE);
        }
    }


    public boolean isSDMounted(){
        boolean isMounted = false;
        try {
            Method getVolumList = StorageManager.class.getMethod("getVolumeList",  new Class[0]);
            getVolumList.setAccessible(true);
            Object[] results = (Object[])getVolumList.invoke(mStorageManager, new Object[]{});
            if (results != null) {
                for (Object result : results) {
                    Method mRemoveable = result.getClass().getMethod("isRemovable", new Class[0]);
                    Boolean isRemovable = (Boolean) mRemoveable.invoke(result, new Object[]{});//判断该内存卡是否可以移除
                    if(!isRemovable) {
                        Method getPath1 = result.getClass().getMethod("getPath", new Class[0]);
                        path1 = (String) getPath1.invoke(result, new Object[]{});//手机内存路径
                        Log.e("lxc","path1==="+path1);
                    }else if (isRemovable) {
                        Method getPath2 = result.getClass().getMethod("getPath", new Class[0]);
                        path2 = (String) getPath2.invoke(result, new Object[]{});//SD卡路径

                        Method getState = mStorageManager.getClass().getMethod("getVolumeState", String.class);
                        String state = (String) getState.invoke(mStorageManager, path2);
                        Log.e("lxc","path2==="+path2+",state==="+state);
                        if (state.equals(Environment.MEDIA_MOUNTED)) {
                            isMounted = true;
                            break;
                        }
                    }
                }
            }
        } catch (NoSuchMethodException e){
            e.printStackTrace();
        } catch (IllegalAccessException e){
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return isMounted;

    }
}
