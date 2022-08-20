package com.example.lxc.android_video.main.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lxc.android_video.R;
import com.example.lxc.android_video.entity.VideoPlayData;
import com.example.lxc.android_video.help.BroadCastManager;

import java.util.List;

/**
 * Created by lxc on 17-9-8.
 */

public class VideoPlayIntroductionDataAdapter extends RecyclerView.Adapter<VideoPlayIntroductionDataAdapter.ViewHolder> {

    private List<VideoPlayData> mVideoList;

    private Context context;

    private int row_index = 0;

    private int data_id = 0 ;//每集的id

    private String url = "http://flv2.bn.netease.com/videolib3/1611/28/GbgsL3639/SD/movie_index.m3u8";//测试视频

    static class ViewHolder extends RecyclerView.ViewHolder{
        View View;
        TextView play_txt;
        public ViewHolder(View itemView) {
            super(itemView);
            View=itemView;
            play_txt = (TextView) itemView.findViewById(R.id.play_txt);

        }
    }

    public VideoPlayIntroductionDataAdapter(Context context,List<VideoPlayData> mVideoList) {
        this.context = context;
        this.mVideoList = mVideoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_play_item,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        holder.View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                row_index = position;
                VideoPlayData videoPlayData = mVideoList.get(position);
                Log.e("lxc","video.getVideo_name()=="+videoPlayData.getVideo_name());
                notifyDataSetChanged();

            }
        });


        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideoPlayData video = mVideoList.get(position);
        //集数
        holder.play_txt.setText(video.getVideo_name()+"");
        //正在播放的集数边框为红色
        if(row_index==position){
            holder.View.setBackgroundResource(R.drawable.ripple_video_play_selection);
            url = video.getVideo_url();
            data_id = video.getData_id();
            sendURL();
        } else {
            holder.View.setBackgroundResource(R.drawable.ripple_video_play_noselection);
        }
    }

    @Override
    public int getItemCount() {
        return mVideoList.size();
    }

    /**
     * 发送播放地址
     */
    private void sendURL(){
        //发送广播
        Intent intent = new Intent();
        intent.putExtra("url", url);
        intent.putExtra("data_id",data_id);
        Log.e("lxc","url_1==="+url);
        Log.e("lxc","data_id==="+data_id);
        intent.setAction("fragment_play_url");
        BroadCastManager.getInstance().sendBroadCast((Activity) context, intent);
    }



}
