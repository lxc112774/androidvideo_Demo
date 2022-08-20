package com.example.lxc.android_video.help;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.lxc.android_video.R;


/**
 * Created by long on 2016/11/17.
 */

public class ShareDialog extends DialogFragment {

    private OnDialogClickListener mClickListener;
    private OnDialogDismissListener mDismissListener;
    private Bitmap mBitmap;
    private boolean mIsShareMode = false;

    public Window window;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        window = getDialog().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        fullScreenImmersive(window.getDecorView());
        window.setWindowAnimations(R.style.AnimateDialog);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View view = inflater.inflate(R.layout.dialog_share, container);
        final ImageView photo = (ImageView) view.findViewById(R.id.iv_screenshot_photo);
        ViewGroup.LayoutParams layoutParams = photo.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels * 7 / 10;
        layoutParams.height = getResources().getDisplayMetrics().heightPixels * 7 / 10;
        photo.setLayoutParams(layoutParams);
        if (mBitmap != null) {
            photo.setImageBitmap(mBitmap);
        }
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        TextView tvShare = (TextView) view.findViewById(R.id.btn_share);
        if (mIsShareMode) {
            tvShare.setText("分享");
        }
        tvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onShare(mBitmap, null);
                }
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mDismissListener != null) {
            mDismissListener.onDismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        // 点击外部回调这里
        if (mDismissListener != null) {
            mDismissListener.onDismiss();
        }
    }

    public void setScreenshotPhoto(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public void setClickListener(OnDialogClickListener clickListener) {
        mClickListener = clickListener;
    }

    public void setDismissListener(OnDialogDismissListener dismissListener) {
        mDismissListener = dismissListener;
    }

    public void setShareMode(boolean shareMode) {
        mIsShareMode = shareMode;
    }

    public interface OnDialogClickListener {
        void onShare(Bitmap bitmap, Uri uri);
    }

    public interface OnDialogDismissListener {
        void onDismiss();
    }

    private void fullScreenImmersive(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            view.setSystemUiVisibility(uiOptions);
        }
    }

}
