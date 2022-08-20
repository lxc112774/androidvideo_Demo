package com.example.lxc.android_video.help.download_img;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.example.lxc.android_video.help.StringHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by lxc on 18-11-23.
 */
/**
 * 图片本地sd卡缓存
 */
public class LocalCacheUtils {

    public static final String CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/myvideo";


    /**
     * 从本地读取图片
     * @param url
     * @return
     */
    public Bitmap getBitmapFromLocal(String url){
        String fileName = null;//把图片的url当做文件名，并进行MD5加密
        try {
            fileName = StringHelper.md5(url);//这里加不加密无所谓
            File file = new File(CACHE_PATH,fileName+".jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            return bitmap;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从网络获取图片后,保存至本地缓存
     * @param url
     * @param bitmap
     */
    public void setBitmapToLocal(String url,Bitmap bitmap){
        try{

            String fileName = StringHelper.md5(url);
            File file = new File(CACHE_PATH,fileName+".jpg");

            //通过得到文件的父文件，判断父文件是否存在
            File parentFile = file.getParentFile();
            if (!parentFile.exists()){
                parentFile.mkdirs();
            }

            //把图片保存到本地
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,new FileOutputStream(file));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
