package com.example.lxc.android_video.main.home;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lxc.android_video.R;
import com.example.lxc.android_video.entity.HeadMessageBase;
import com.example.lxc.android_video.entity.HeadMessageBean;
import com.example.lxc.android_video.entity.VideoBean;
import com.example.lxc.android_video.help.NetWorkUtils;
import com.example.lxc.android_video.help.download_img.BitmapUtils;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lxc on 17-9-8.
 */

public class HomeDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private List<Object> mVideoList;

    List<String> images = new ArrayList<>();

    /**
     * viewType--分别为item以及空view
     */
    public static final int VIEW_TYPE_ITEM_ADD_TEST = 10;
    public static final int VIEW_TYPE_ITEM_ADD_HEAD = 0;
    public static final int VIEW_TYPE_ITEM = 1;
    public static final int VIEW_TYPE_EMPTY = -1;


    class VideoHolder extends RecyclerView.ViewHolder{
        View View;
        ImageView VideoImage;
        TextView VideoName,Clicking_rate,comment_number,to_update;
        public VideoHolder(View itemView) {
            super(itemView);
            View=itemView;
            VideoImage = (ImageView) itemView.findViewById(R.id.video_img);
            VideoName = (TextView) itemView.findViewById(R.id.video_name);
            Clicking_rate = (TextView) itemView.findViewById(R.id.Clicking_rate);
            comment_number = (TextView) itemView.findViewById(R.id.comment_number);
            to_update = (TextView) itemView.findViewById(R.id.To_update);

        }
    }

    class BannerHolder extends RecyclerView.ViewHolder{
        View View;
        Banner BannerImage;
        public BannerHolder(View itemView) {
            super(itemView);
            View=itemView;
            BannerImage = (Banner) itemView.findViewById(R.id.banner);
            setImg();
        }
    }


    public HomeDataAdapter(List<Object> mVideoList) {
        this.mVideoList = mVideoList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //在这里根据不同的viewType进行引入不同的布局
        if (viewType == VIEW_TYPE_EMPTY && NetWorkUtils.isNetworkAvailable(parent.getContext())) {
            View emptyView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_home_main_empty2, parent, false);
            return new HomeDataAdapter.VideoHolder(emptyView){};
        }else if(viewType == VIEW_TYPE_EMPTY ){
            View emptyView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_home_main_empty, parent, false);
            return new HomeDataAdapter.VideoHolder(emptyView){};
        }else if(viewType == VIEW_TYPE_ITEM_ADD_HEAD){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_head_banner_items, parent, false);
            final BannerHolder headholder = new BannerHolder(view);
            return headholder;
        }else {
            //其他的引入正常的
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_video_items, parent, false);
            final VideoHolder holder = new VideoHolder(view);
            holder.View.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = holder.getAdapterPosition();
                    VideoBean videoBean = (VideoBean)mVideoList.get(position);
                    //Toast.makeText(view.getContext(),"点击:"+videoBean.getIntroduction_id(),Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(view.getContext(), VideoPlayMainActivity.class);
                    intent.putExtra("video_introduction_id", videoBean.getIntroduction_id());
                    intent.putExtra("introduction_content", videoBean.getVideo_introduction());
                    intent.putExtra("video_name", videoBean.getVideo_name());
                    intent.putExtra("video_img", videoBean.getVideo_img());
                    view.getContext().startActivity(intent);
                    ((Activity) view.getContext()).overridePendingTransition(R.anim.slide_left_in, R.anim.activity_stay); //R.anim.activity_stay解决打开Acticity的时候会黑屏一下;//动画效果
                }
            });
            return holder;
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(mVideoList.size() != 0){
            if(holder instanceof VideoHolder) {
                VideoHolder viewHolderOne= (VideoHolder) holder;
                VideoBean video = (VideoBean) mVideoList.get(position);
                viewHolderOne.VideoName.setText(video.getVideo_name());
                viewHolderOne.Clicking_rate.setText(computational(video.getVideo_playnumber()));
                viewHolderOne.comment_number.setText(computational(video.getVideo_commentnumber()));
                /**if(video.getVideo_img() == null || video.getVideo_img().equals("")){
                 holder.VideoImage.setImageResource(R.drawable.lxc);
                 }else {
                 new DownloadImageTask(holder.VideoImage).execute(video.getVideo_img());
                 }**/
                new BitmapUtils().disPlay(viewHolderOne.VideoImage, video.getVideo_img());
                viewHolderOne.to_update.setText("更新至:第" + video.getVideo_showupdate() + "集");
            }else if(holder instanceof BannerHolder){
                BannerHolder bannerHolder = (BannerHolder) holder;
                //设置banner样式
                bannerHolder.BannerImage.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
                //设置图片加载器
                bannerHolder.BannerImage.setImageLoader(new GlideImageLoader());
                //设置图片集合
                bannerHolder.BannerImage.setImages(images);
                //设置banner动画效果
                bannerHolder.BannerImage.setBannerAnimation(Transformer.ZoomOutSlide);
                //设置自动轮播，默认为true
                bannerHolder.BannerImage.isAutoPlay(true);
                //设置轮播时间
                bannerHolder.BannerImage.setDelayTime(3000);
                bannerHolder.BannerImage.setOnBannerClickListener(new OnBannerClickListener() {
                    @Override
                    public void OnBannerClick(int position) {

                    }
                });
                //设置指示器位置（当banner模式中有指示器时）
                bannerHolder.BannerImage.setIndicatorGravity(BannerConfig.RIGHT);
                bannerHolder.BannerImage.start();
            }
        }
    }


    @Override
    public int getItemCount() {
        //同时这里也需要添加判断，如果mData.size()为0的话，只引入一个布局，就是emptyView // 那么，这个recyclerView的itemCount为1
        if (mVideoList.size() == 0) { return 1; }
        //如果不为0，按正常的流程跑
        return mVideoList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //在这里进行判断，如果我们的集合的长度为0时，我们就使用emptyView的布局
        if (mVideoList.size() == 0) {
            return VIEW_TYPE_EMPTY;
        }else if (mVideoList.get(position) instanceof HeadMessageBase){
            return VIEW_TYPE_ITEM_ADD_HEAD;
        }else if(mVideoList.get(position) instanceof VideoBean && ((VideoBean) mVideoList.get(position)).getVideo_kind()==10){
            return VIEW_TYPE_ITEM_ADD_TEST;
        }else {
            //如果有数据，则使用ITEM的布局
            return VIEW_TYPE_ITEM;
        }
    }

    /**
     * 数字格式
     */
    private String computational(long data_number){
        double space = data_number;
        if(space/(10000*10000)>1){
            String numberStr = String.valueOf(space/(10000*10000));
            int index = numberStr.indexOf(".");
            String number = numberStr.substring(0,index+2);
            return number+"亿";
        }else if(space/(10000)>1){
            String numberStr = String.valueOf(space/(10000));
            int index = numberStr.indexOf(".");
            String number = numberStr.substring(0,index+2);
            return number+"万";
        }else {
            return (int)space+"";
        }
    }


    private void setImg(){
        android.util.Log.e("lxc","222222222222222");
        if(mVideoList.size() !=0 ){
            for(int i =0 ;i<mVideoList.size();i++){
                android.util.Log.e("lxc","11111111111");
                if(mVideoList.get(i) instanceof HeadMessageBase){
                    for (HeadMessageBean head :((HeadMessageBase) mVideoList.get(i)).getBeanList()) {
                        images.add(head.getImg_url());
                    }
                }
            }
        }
    }

}
