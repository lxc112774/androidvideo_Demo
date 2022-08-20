package com.example.lxc.android_video.help;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by lxc on 18-10-25.
 */

/**
 * 圆弧图片
 */
public class YuanHuImageView extends ImageView {

    //圆角弧度
    private float[] rids = {10.0f,10.0f,10.0f,10.0f,0.0f,0.0f,0.0f,0.0f,};
    private float[] headrids = {10.0f,10.0f,10.0f,10.0f,10.0f,10.0f,10.0f,10.0f,};

    private boolean ishead = false;

    public YuanHuImageView(Context context) {
        super(context);
    }

    public YuanHuImageView(Context context,boolean ishead) {
        super(context);
        this.ishead=ishead;
    }


    public YuanHuImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public YuanHuImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 绘制圆弧图片
     *
     */
    protected void onDraw(Canvas canvas) {
        Path path = new Path();
        int w = this.getWidth();
        int h = this.getHeight();
        if(ishead){
            //绘制圆角imageview
            path.addRoundRect(new RectF(0, 0, w, h), headrids, Path.Direction.CW);
        }else {
            //绘制圆角imageview
            path.addRoundRect(new RectF(0, 0, w, h), rids, Path.Direction.CW);
        }
        canvas.clipPath(path);
        super.onDraw(canvas);
    }

}
