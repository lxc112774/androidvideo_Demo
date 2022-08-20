package com.example.lxc.android_video.help;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by lxc on 18-11-5.
 */
//图片缓存
// 类中参数为3种泛型类型
// 整体作用：控制AsyncTask子类执行线程任务时各个阶段的返回类型 
// 具体说明：
     //  a. Params：开始异步任务执行时传入的参数类型，对应excute（）中传递的参数, 也就是doInBackground()中String... urls
     //  b. Progress：异步任务执行过程中，返回下载进度值的类型
    // c. Result：异步任务执行完成后，返回的结果类型，与doInBackground()的返回值类型保持一致

// 注： // a. 使用时并不是所有类型都被使用 // b. 若无被使用，可用java.lang.Void类型代替 // c. 若有不同业务，需额外再写1个AsyncTask的子类
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    /**
     * 后台耗时操作,存在于子线程中
     * @param urls
     * @return
     */
    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    /**
     * 耗时方法结束后执行该方法,主线程中
     * @param result
     */
    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }


    /**
     * 更新进度,在主线程中
     * @param values
     */
    @Override
    protected void onProgressUpdate(Void[] values) {
        super.onProgressUpdate(values);
    }
}
