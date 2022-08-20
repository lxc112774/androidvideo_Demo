package com.example.lxc.android_video.main.home;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.example.lxc.android_video.R;
import com.example.lxc.android_video.base.BaseActivity;

/**
 * Created by lxc on 18-5-5.
 */

public class HistoryActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
