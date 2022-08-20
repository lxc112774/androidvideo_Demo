package com.example.lxc.android_video.main.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lxc.android_video.R;
import com.example.lxc.android_video.entity.VideoPlayComment;
import com.example.lxc.android_video.entity.VideoPlayData;

import java.util.List;

/**
 * Created by lxc on 17-9-8.
 */

public class VideoPlayCommentAdapter extends RecyclerView.Adapter<VideoPlayCommentAdapter.ViewHolder> {

    private List<VideoPlayComment> mVideoList;


    static class ViewHolder extends RecyclerView.ViewHolder{
        View View;
        ImageView user_img,zan,no_zan,btn_share,comment_setting;
        TextView user_name,comment_time,comment_content;
        public ViewHolder(View itemView) {
            super(itemView);
            View=itemView;
            user_img = (ImageView) itemView.findViewById(R.id.user_img);
            zan = (ImageView) itemView.findViewById(R.id.zan);
            no_zan = (ImageView) itemView.findViewById(R.id.no_zan);
            btn_share = (ImageView) itemView.findViewById(R.id.btn_share);
            comment_setting = (ImageView) itemView.findViewById(R.id.comment_setting);
            user_name = (TextView) itemView.findViewById(R.id.user_name);
            comment_time = (TextView) itemView.findViewById(R.id.comment_time);
            comment_content = (TextView) itemView.findViewById(R.id.comment_content);
        }
    }

    public VideoPlayCommentAdapter(List<VideoPlayComment> mVideoList) {
        this.mVideoList = mVideoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_play_comment_item,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        holder.View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                notifyDataSetChanged();

            }
        });


        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideoPlayComment comment = mVideoList.get(position);
        holder.user_name.setText(comment.getUsername());
        holder.comment_time.setText(comment.getCommentTime());
        holder.comment_content.setText(comment.getComment_content());
        if(comment.getUserimg()==null || comment.getUserimg().equals("")){
            holder.user_img.setImageResource(R.drawable.lxc);
        }else {
            // Base64解码图片
            byte[] imageByteArray = Base64.decode(comment.getUserimg() , 0);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            holder.user_img.setImageBitmap(bitmap);
        }
    }

    @Override
    public int getItemCount() {
        return mVideoList.size();
    }




}
