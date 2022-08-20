package com.example.lxc.android_video.main.download;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.lxc.android_video.R;

/**
 * Created by lxc on 18-5-5.
 */

public class DownloadNoCompleteFragment extends Fragment implements View.OnClickListener {

    private RelativeLayout pause,start;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download_nocomplete, container, false);

        pause = (RelativeLayout) view.findViewById(R.id.pause);
        pause.setOnClickListener(this);

        start = (RelativeLayout) view.findViewById(R.id.start);
        start.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.pause:
                break;
            case R.id.start:
                break;
        }

    }
}
